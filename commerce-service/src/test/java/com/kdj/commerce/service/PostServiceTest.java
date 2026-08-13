package com.kdj.commerce.service;

import com.kdj.commerce.domain.community.Post;
import com.kdj.commerce.domain.community.PostLikeRepository;
import com.kdj.commerce.domain.community.PostRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.member.MemberType;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class PostServiceTest {
    @Autowired PostRepository postRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired PostLikeRepository postLikeRepository;
    @Autowired PostService postService;

    @Autowired
    EntityManager em;

    private final List<Member> members = new ArrayList<>();

    @AfterEach
    public void setUp () {
        System.out.println("==================== 청소 시작");
        postLikeRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
        members.clear();
        System.out.println("==================== 청소 종료");
    }

    @Test
    @DisplayName("게시물 조회수 테스트")
    void viewCountTest() throws InterruptedException {
        Member member = Member.create(
                "testUser",
                "testEmail@gmail.com",
                "testId",
                "testPassword",
                MemberType.USER);
        memberRepository.save(member);

        Post post = Post.create("Test", "Test", member);
        Long postId = postService.save(post.getTitle(), post.getContent(), member);

        int threadCount = 1000;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    postService.increaseViewCount(postId);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        int result = postService.findOne(postId).getViewCount();
        System.out.println("최종 조회수= " + result);
        Assertions.assertThat(result).isEqualTo(1000);
    }

    /*
    @Test
    @DisplayName("게시물 좋아요 테스트")
    void likeCountTest() throws InterruptedException {
        for (int i = 1; i <= 100; i++) {
            Member member = Member.create(
                    "testUser" + i,
                    "testUser" + i + "@test.com",
                    "testId" + i,
                    "testPassword",
                    MemberType.USER
            );
            memberRepository.save(member);
            members.add(member);
        }

        Post post = Post.create("Test", "Test", members.get(0), 0, 0);
        Long postId = postService.save(post);

        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // 4. 100명의 회원이 동시에 좋아요 누르기
        for (int i = 0; i < threadCount; i++) {
            Member member = members.get(i); // 각 쓰레드마다 서로 다른 회원 할당
            executorService.submit(() -> {
                try {
                    postService.like(postId, member);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // 5. 검증: 최종 좋아요 수 카운트 확인
        int result = postService.findOne(postId).getLikeCount();
        System.out.println("최종 좋아요 수= " + result);
        Assertions.assertThat(result).isEqualTo(100);
    }
     */
}