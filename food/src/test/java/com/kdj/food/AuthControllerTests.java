package com.kdj.food;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.kdj.food.entity.FoodMaterial;
import com.kdj.food.entity.Material;
import com.kdj.food.repository.FoodMaterialRepository;
import com.kdj.food.repository.FoodRepository;
import com.kdj.food.repository.MaterialRepository;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(properties = "FOOD_JWT_SECRET=AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
@Transactional
class AuthControllerTests {
    private static final String AUTH_PATH = "/api/v1/auth";
    private static final String PROFILE_PATH = "/api/v1/members/me";
    private static final String DISHES_PATH = "/api/v1/dishes";
    private static final String AUTH_HEADER = "X-FoodLab-Auth";
    private static final String SIGN_UP = """
            {"email":"http@example.com","password":"test-password","name":"연구원"}
            """;
    private static final String SIGN_IN = """
            {"email":"HTTP@example.com","password":"test-password"}
            """;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FoodRepository foods;

    @Autowired
    private MaterialRepository materials;

    @Autowired
    private FoodMaterialRepository foodMaterials;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    @DisplayName("로그인·갱신·로그아웃 시 리프레시 쿠키를 처리한다")
    void authEndpointsIssueRotateAndClearRefreshCookie() throws Exception {
        mvc.perform(post(AUTH_PATH + "/signUp").contentType(MediaType.APPLICATION_JSON).content(SIGN_UP))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.memberId").isNumber());
        mvc.perform(post(AUTH_PATH + "/signUp").contentType(MediaType.APPLICATION_JSON).content(SIGN_UP))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_EMAIL"));

        MvcResult signIn = mvc.perform(post(AUTH_PATH + "/signIn").contentType(MediaType.APPLICATION_JSON).content(SIGN_IN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andReturn();
        String setCookie = signIn.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("HttpOnly", "Secure", "SameSite=Strict", "Path=" + AUTH_PATH, "Max-Age=604800");
        Cookie original = refreshCookie(signIn);

        mvc.perform(post(AUTH_PATH + "/refresh").cookie(original))
                .andExpect(status().isForbidden());
        MvcResult refresh = mvc.perform(post(AUTH_PATH + "/refresh").header(AUTH_HEADER, "1").cookie(original))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andReturn();
        Cookie replacement = refreshCookie(refresh);
        assertThat(replacement.getValue()).isNotEqualTo(original.getValue());

        MvcResult signOut = mvc.perform(post(AUTH_PATH + "/signOut").header(AUTH_HEADER, "1").cookie(replacement))
                .andExpect(status().isNoContent())
                .andReturn();
        assertThat(signOut.getResponse().getHeader(HttpHeaders.SET_COOKIE)).contains("food_refresh=", "Max-Age=0");

        for (Cookie invalidToken : new Cookie[] {original, replacement}) {
            mvc.perform(post(AUTH_PATH + "/refresh").header(AUTH_HEADER, "1").cookie(invalidToken))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
    }

    @Test
    @DisplayName("토큰으로 내 정보를 조회하고 이름을 수정한다")
    void profileUsesMemberFromBearerTokenAndUpdatesOnlyName() throws Exception {
        mvc.perform(get(PROFILE_PATH))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, org.hamcrest.Matchers.startsWith("Bearer")))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mvc.perform(patch(PROFILE_PATH).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"새 이름\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get(PROFILE_PATH).header(HttpHeaders.AUTHORIZATION, "Bearer invalid"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        MvcResult signUp = mvc.perform(post(AUTH_PATH + "/signUp").contentType(MediaType.APPLICATION_JSON).content(SIGN_UP))
                .andExpect(status().isCreated())
                .andReturn();
        Number memberId = JsonPath.read(signUp.getResponse().getContentAsString(), "$.memberId");
        MvcResult signIn = mvc.perform(post(AUTH_PATH + "/signIn").contentType(MediaType.APPLICATION_JSON).content(SIGN_IN))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = JsonPath.read(signIn.getResponse().getContentAsString(), "$.accessToken");
        String bearer = "Bearer " + accessToken;

        mvc.perform(get(PROFILE_PATH).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(memberId))
                .andExpect(jsonPath("$.email").value("http@example.com"))
                .andExpect(jsonPath("$.name").value("연구원"))
                .andExpect(jsonPath("$.password").doesNotExist());
        mvc.perform(patch(PROFILE_PATH).header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"새 이름\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새 이름"));
        mvc.perform(get(PROFILE_PATH).header(HttpHeaders.AUTHORIZATION, bearer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("새 이름"));
        mvc.perform(patch(PROFILE_PATH).header(HttpHeaders.AUTHORIZATION, bearer)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    @Test
    @DisplayName("요리는 공개 조회하고 작성자만 수정·삭제한다")
    void dishesArePublicToReadAndWritableOnlyByTheirAuthor() throws Exception {
        String author = registerAndSignIn("author-http@example.com");
        String other = registerAndSignIn("other-http@example.com");
        String dish = """
                {"name":"참치밥","description":"소개","imageId":"image-a"}
                """;
        String replacement = dish.replace("참치밥", "참치계란밥");

        mvc.perform(post(DISHES_PATH).contentType(MediaType.APPLICATION_JSON).content(dish))
                .andExpect(status().isUnauthorized());
        MvcResult created = mvc.perform(post(DISHES_PATH).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(dish))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.dishId").isNumber())
                .andReturn();
        String detail = created.getResponse().getHeader(HttpHeaders.LOCATION);
        assertThat(detail).startsWith(DISHES_PATH + "/");

        mvc.perform(get(DISHES_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].imageId").value("image-a"))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get(DISHES_PATH).param("q", "참치").param("sort", "efficiency"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("참치밥"));
        mvc.perform(get(DISHES_PATH).param("size", "101"))
                .andExpect(status().isBadRequest());
        mvc.perform(get(DISHES_PATH).param("page", "2147483647"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        mvc.perform(post(DISHES_PATH).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.TEXT_PLAIN).content(dish))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_MEDIA_TYPE"));
        mvc.perform(post(DISHES_PATH).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(dish.replace("소개", "한".repeat(21_846))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        mvc.perform(get(detail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.reviewCount").value(0))
                .andExpect(jsonPath("$.materials").isEmpty());

        Number dishId = JsonPath.read(created.getResponse().getContentAsString(), "$.dishId");
        var food = foods.getReferenceById(dishId.longValue());
        var sweetPotato = materials.save(new Material("고구마", "고구마"));
        var salt = materials.save(new Material("소금", "소금"));
        foodMaterials.save(new FoodMaterial(food, sweetPotato, new BigDecimal("200.125"), "g"));
        foodMaterials.save(new FoodMaterial(food, salt, null, "약간"));

        mvc.perform(get(detail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materials.length()").value(2))
                .andExpect(jsonPath("$.materials[0].materialId").value(sweetPotato.getId()))
                .andExpect(jsonPath("$.materials[0].name").value("고구마"))
                .andExpect(jsonPath("$.materials[0].quantity").value(200.125))
                .andExpect(jsonPath("$.materials[0].unit").value("g"))
                .andExpect(jsonPath("$.materials[1].quantity").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.materials[1].unit").value("약간"));

        mvc.perform(put(detail).header(HttpHeaders.AUTHORIZATION, other)
                        .contentType(MediaType.APPLICATION_JSON).content(replacement))
                .andExpect(status().isForbidden());
        mvc.perform(delete(detail).header(HttpHeaders.AUTHORIZATION, other))
                .andExpect(status().isForbidden());
        mvc.perform(put(detail).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(replacement))
                .andExpect(status().isNoContent());
        mvc.perform(get(detail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.name").value("참치계란밥"));
        mvc.perform(delete(detail).header(HttpHeaders.AUTHORIZATION, author))
                .andExpect(status().isNoContent());
        mvc.perform(get(detail)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("평가 중복과 자기 평가를 막고 작성자만 수정·삭제한다")
    void reviewsArePublicAndOnlyTheirAuthorCanEditThem() throws Exception {
        String author = registerAndSignIn("food-author@example.com");
        String reviewer = registerAndSignIn("review-author@example.com");
        String dish = """
                {"name":"계란밥","description":"소개","imageId":"image-a"}
                """;
        MvcResult created = mvc.perform(post(DISHES_PATH).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(dish))
                .andExpect(status().isCreated())
                .andReturn();
        String detail = created.getResponse().getHeader(HttpHeaders.LOCATION);
        String reviews = detail + "/reviews";
        String scores = """
                {"effort":5,"taste":4,"value":3}
                """;

        mvc.perform(get(reviews)).andExpect(status().isOk()).andExpect(jsonPath("$.totalElements").value(0));
        mvc.perform(post(reviews).contentType(MediaType.APPLICATION_JSON).content(scores))
                .andExpect(status().isUnauthorized());
        mvc.perform(post(reviews).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(scores))
                .andExpect(status().isForbidden());
        mvc.perform(post(reviews).header(HttpHeaders.AUTHORIZATION, reviewer)
                        .contentType(MediaType.APPLICATION_JSON).content(scores.replace("5", "4.5")))
                .andExpect(status().isBadRequest());
        MvcResult posted = mvc.perform(post(reviews).header(HttpHeaders.AUTHORIZATION, reviewer)
                        .contentType(MediaType.APPLICATION_JSON).content(scores))
                .andExpect(status().isCreated())
                .andReturn();
        Number reviewId = JsonPath.read(posted.getResponse().getContentAsString(), "$.reviewId");
        String review = reviews + "/" + reviewId;
        String myDishes = PROFILE_PATH + "/dishes";
        String myReviews = PROFILE_PATH + "/reviews";

        mvc.perform(get(myDishes)).andExpect(status().isUnauthorized());
        mvc.perform(get(myReviews)).andExpect(status().isUnauthorized());
        mvc.perform(get(myDishes).header(HttpHeaders.AUTHORIZATION, author).param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("계란밥"))
                .andExpect(jsonPath("$.items[0].imageId").value("image-a"))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get(myDishes).header(HttpHeaders.AUTHORIZATION, reviewer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
        mvc.perform(get(myReviews).header(HttpHeaders.AUTHORIZATION, reviewer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value(reviewId))
                .andExpect(jsonPath("$.totalElements").value(1));
        mvc.perform(get(myReviews).header(HttpHeaders.AUTHORIZATION, author))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
        mvc.perform(get(myReviews).header(HttpHeaders.AUTHORIZATION, reviewer).param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));

        mvc.perform(post(reviews).header(HttpHeaders.AUTHORIZATION, reviewer)
                        .contentType(MediaType.APPLICATION_JSON).content(scores))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_REVIEW"));

        String update = "{\"taste\":2}";
        mvc.perform(patch(review).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isForbidden());
        mvc.perform(patch(review).header(HttpHeaders.AUTHORIZATION, reviewer)
                        .contentType(MediaType.APPLICATION_JSON).content(update))
                .andExpect(status().isNoContent());
        mvc.perform(get(reviews))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].effort").value(5))
                .andExpect(jsonPath("$.items[0].taste").value(2))
                .andExpect(jsonPath("$.items[0].value").value(3));
        mvc.perform(delete(review).header(HttpHeaders.AUTHORIZATION, author))
                .andExpect(status().isForbidden());
        mvc.perform(delete(review).header(HttpHeaders.AUTHORIZATION, reviewer))
                .andExpect(status().isNoContent());
        mvc.perform(get(detail)).andExpect(status().isOk()).andExpect(jsonPath("$.summary.reviewCount").value(0));
    }

    @Test
    @DisplayName("요리 등록·수정 시 재료를 재사용하고 목록을 교체한다")
    void dishWriteReusesMaterialsAndReplacesOnlyAnExplicitList() throws Exception {
        String author = registerAndSignIn("material-author@example.com");
        String dish = """
                {"name":"감자구이","description":"소개","imageId":"image-a","materials":[
                  {"name":" Olive  Oil ","quantity":0.125,"unit":"ml"},
                  {"name":"소금","unit":"약간"}
                ]}
                """;
        MvcResult created = mvc.perform(post(DISHES_PATH).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(dish))
                .andExpect(status().isCreated())
                .andReturn();
        String detail = created.getResponse().getHeader(HttpHeaders.LOCATION);
        Long oilId = materials.findByNormalizedName("olive oil").orElseThrow().getId();

        mvc.perform(get(detail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materials.length()").value(2))
                .andExpect(jsonPath("$.materials[0].materialId").value(oilId))
                .andExpect(jsonPath("$.materials[0].quantity").value(0.125))
                .andExpect(jsonPath("$.materials[1].unit").value("약간"));

        String replacement = """
                {"name":"감자구이","description":"소개","imageId":"image-a","materials":[
                  {"name":"OLIVE OIL","quantity":2,"unit":"ml"}
                ]}
                """;
        mvc.perform(put(detail).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(replacement))
                .andExpect(status().isNoContent());
        mvc.perform(get(detail))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materials.length()").value(1))
                .andExpect(jsonPath("$.materials[0].materialId").value(oilId))
                .andExpect(jsonPath("$.materials[0].name").value("Olive Oil"))
                .andExpect(jsonPath("$.materials[0].quantity").value(2));

        String byId = """
                {"name":"감자구이","description":"소개","imageId":"image-a","materials":[
                  {"materialId":%d,"quantity":3,"unit":"ml"}
                ]}
                """.formatted(oilId);
        mvc.perform(put(detail).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(byId))
                .andExpect(status().isNoContent());
        String basic = """
                {"name":"감자구이","description":"소개","imageId":"image-a"}
                """;
        mvc.perform(put(detail).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(basic))
                .andExpect(status().isNoContent());
        mvc.perform(get(detail))
                .andExpect(jsonPath("$.materials[0].quantity").value(3));

        String emptyMaterials = basic.replace("}", ",\"materials\":[]}");
        mvc.perform(put(detail).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(emptyMaterials))
                .andExpect(status().isNoContent());
        mvc.perform(get(detail)).andExpect(jsonPath("$.materials").isEmpty());

        String duplicate = byId.replace("]}", ",{\"name\":\"olive oil\",\"unit\":\"ml\"}]}");
        mvc.perform(put(detail).header(HttpHeaders.AUTHORIZATION, author)
                        .contentType(MediaType.APPLICATION_JSON).content(duplicate))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    private String registerAndSignIn(String email) throws Exception {
        mvc.perform(post(AUTH_PATH + "/signUp").contentType(MediaType.APPLICATION_JSON)
                        .content(SIGN_UP.replace("http@example.com", email)))
                .andExpect(status().isCreated());
        MvcResult result = mvc.perform(post(AUTH_PATH + "/signIn").contentType(MediaType.APPLICATION_JSON)
                        .content(SIGN_IN.replace("HTTP@example.com", email)))
                .andExpect(status().isOk())
                .andReturn();
        return "Bearer " + JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private Cookie refreshCookie(MvcResult result) {
        String setCookie = result.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).startsWith("food_refresh=");
        return new Cookie("food_refresh", setCookie.substring("food_refresh=".length(), setCookie.indexOf(';')));
    }
}
