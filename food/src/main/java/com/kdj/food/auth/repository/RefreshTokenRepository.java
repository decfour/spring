package com.kdj.food.auth.repository;

import com.kdj.food.auth.entity.RefreshToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByTokenHash(byte[] tokenHash);

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshToken token
            set token.tokenHash = :newHash, token.expiresAt = :expiresAt
            where token.tokenHash = :currentHash and token.expiresAt > :now
            """)
    int rotate(
            @Param("currentHash") byte[] currentHash,
            @Param("newHash") byte[] newHash,
            @Param("expiresAt") LocalDateTime expiresAt,
            @Param("now") LocalDateTime now
    );

    @Transactional
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from RefreshToken token where token.tokenHash = :tokenHash")
    int revoke(@Param("tokenHash") byte[] tokenHash);
}
