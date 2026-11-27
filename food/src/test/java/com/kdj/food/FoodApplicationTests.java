package com.kdj.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.kdj.food.service.FoodService;
import com.kdj.food.repository.MemberRepository;
import com.kdj.food.auth.service.DuplicateEmailException;
import com.kdj.food.auth.service.MemberSignUpService;
import com.kdj.food.service.FoodReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = "FOOD_JWT_SECRET=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
class FoodApplicationTests {
	@Autowired
	private MemberSignUpService signUpService;

	@Autowired
	private MemberRepository memberRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private FoodService foodService;

	@Autowired
	private FoodReviewService reviewService;

	@Autowired
	private JdbcTemplate jdbc;

	@Test
	@DisplayName("가입 이메일을 정규화하고 비밀번호를 해시로 저장한다")
	@Transactional
	void signUpNormalizesEmailAndStoresOnlyThePasswordHash() {
		String password = "test-password";
		Long memberId = signUpService.signUp("  Researcher@Example.com  ", password, "연구원");

		var member = memberRepository.findById(memberId).orElseThrow();
		assertThat(member.getEmail()).isEqualTo("researcher@example.com");
		assertThat(member.getName()).isEqualTo("연구원");
		assertThat(member.getPassword()).isNotEqualTo(password);
		assertThat(passwordEncoder.matches(password, member.getPassword())).isTrue();
		assertThatThrownBy(() -> signUpService.signUp("RESEARCHER@example.com", password, "다른 연구원"))
				.isInstanceOf(DuplicateEmailException.class);
	}

	@Test
	@DisplayName("요리를 평가 점수로 정렬하고 집계를 갱신한다")
	@Transactional
	void foodSearchSortsByReviewScoresAndUpdatesAfterReviewDeletion() {
		Long author = signUpService.signUp("author@example.com", "test-password", "작성자");
		Long reviewer = signUpService.signUp("reviewer@example.com", "test-password", "평가자");
		Long reviewed = foodService.create(author, "참치밥", "소개", "image-a");
		foodService.create(author, "참치 샐러드", "소개", "image-b");
		Long review = reviewService.create(reviewed, reviewer, 5, 4, 3);

		for (var sort : FoodService.FoodSort.values()) {
			var page = foodService.search("참치", sort, PageRequest.of(0, 1));
			assertThat(page.getTotalElements()).isEqualTo(2);
			if (sort != FoodService.FoodSort.LATEST) {
				assertThat(page.getContent().getFirst().id()).isEqualTo(reviewed);
			}
		}
		assertThat(foodService.get(reviewed).summary().efficiency()).isEqualTo(4.0);
		reviewService.delete(reviewed, review, reviewer);
		assertThat(foodService.get(reviewed).summary().efficiency()).isNull();
	}

	@Test
	@DisplayName("DB가 평가 제약을 적용하고 요리 삭제 시 평가를 지운다")
	@Transactional
	void databaseEnforcesReviewConstraintsAndCascadesFoodDeletion() {
		Long author = signUpService.signUp("schema-author@example.com", "test-password", "작성자");
		Long reviewer = signUpService.signUp("schema-reviewer@example.com", "test-password", "평가자");
		Long foodId = foodService.create(author, "감자구이", "소개", "image-a");
		String insertReview = """
				INSERT INTO food_review (food_id, member_id, effort, taste, value, created_at, updated_at)
				VALUES (?, ?, ?, 4, 4, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6))
				""";
		assertThatThrownBy(() -> jdbc.update(insertReview, foodId, reviewer, 0))
				.isInstanceOf(DataAccessException.class)
				.hasStackTraceContaining("ck_food_review_effort");
		assertThat(jdbc.update(insertReview, foodId, reviewer, 5)).isEqualTo(1);
		assertThatThrownBy(() -> jdbc.update(insertReview, foodId, reviewer, 5))
				.isInstanceOf(DuplicateKeyException.class);

		jdbc.update("DELETE FROM food WHERE id = ?", foodId);
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM food_review WHERE food_id = ?", Long.class, foodId))
				.isZero();
	}
	@Test
	@DisplayName("DB가 재료의 수량·중복·삭제 규칙을 적용한다")
	@Transactional
	void materialTablesEnforceQuantityUniquenessAndDeletionRules() {
		Long author = signUpService.signUp("materials@example.com", "test-password", "작성자");
		Long foodId = foodService.create(author, "감자구이", "소개", "image-a");
		String addMaterial = """
				INSERT INTO material (name, normalized_name, created_at)
				VALUES (?, ?, UTC_TIMESTAMP(6))
				""";
		jdbc.update(addMaterial, "소금", "소금");
		jdbc.update(addMaterial, "감자", "감자");

		String findMaterial = "SELECT id FROM material WHERE normalized_name = ?";
		Long saltId = jdbc.queryForObject(findMaterial, Long.class, "소금");
		Long potatoId = jdbc.queryForObject(findMaterial, Long.class, "감자");
		String linkMaterial = """
				INSERT INTO food_material (food_id, material_id, quantity, unit)
				VALUES (?, ?, ?, ?)
				""";
		assertThat(jdbc.update(linkMaterial, foodId, saltId, null, "약간")).isEqualTo(1);
		assertThatThrownBy(() -> jdbc.update(linkMaterial, foodId, potatoId, 0, "g"))
				.isInstanceOf(DataAccessException.class)
				.hasStackTraceContaining("ck_food_material_positive_quantity");
		assertThat(jdbc.update(linkMaterial, foodId, potatoId, 200, "g")).isEqualTo(1);

		assertThatThrownBy(() -> jdbc.update(addMaterial, "다른 표시 이름", "소금"))
				.isInstanceOf(DuplicateKeyException.class);
		assertThatThrownBy(() -> jdbc.update(linkMaterial, foodId, saltId, null, "취향껏"))
				.isInstanceOf(DuplicateKeyException.class);
		assertThatThrownBy(() -> jdbc.update("DELETE FROM material WHERE id = ?", saltId))
				.isInstanceOf(DataIntegrityViolationException.class);

		jdbc.update("DELETE FROM food WHERE id = ?", foodId);
		assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM food_material WHERE food_id = ?", Long.class, foodId))
				.isZero();
		assertThat(jdbc.queryForObject(findMaterial, Long.class, "소금")).isEqualTo(saltId);
	}
}
