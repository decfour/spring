package com.kdj.commerce.service;

import com.kdj.commerce.domain.community.Post;
import com.kdj.commerce.domain.community.PostLikeRepository;
import com.kdj.commerce.domain.community.PostRepository;
import com.kdj.commerce.domain.member.Member;
import com.kdj.commerce.domain.member.MemberRepository;
import com.kdj.commerce.domain.member.MemberType;
import java.util.concurrent.Executors;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PostConcurrencyTest {
    @Autowired PostRepository postRepository;
    @Autowired MemberRepository memberRepository;
    @Autowired PostLikeRepository postLikeRepository;
    @Autowired PostService postService;

    @AfterEach
    void cleanUp() {

        postLikeRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("조회수 증가 요청 1000개가 동시에 실행돼도 누락 없이 반영된다")
    void viewCountTest() throws Exception {
        Member member = Member.create(
                "testUser",
                "testEmail@gmail.com",
                "testId",
                "testPassword",
                MemberType.USER);
        memberRepository.save(member);

        Post post = Post.create("Test", "Test", member);
        Long postId = postService.save(post.getTitle(), post.getContent(), member);

        var executor = Executors.newFixedThreadPool(32);
        try {
            var tasks = java.util.stream.IntStream.range(0, 1000)
                    .mapToObj(i -> executor.submit(() -> postService.increaseViewCount(postId))).toList();
            for (var task : tasks) task.get(30, java.util.concurrent.TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }

        int result = postService.findById(postId).getViewCount();

        Assertions.assertThat(result).isEqualTo(1000);
    }

}
