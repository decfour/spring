package com.kdj.food.auth.entity;

import com.kdj.food.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "refresh_token",
        uniqueConstraints = @UniqueConstraint(name = "uk_refresh_token_hash", columnNames = "token_hash"),
        indexes = @Index(name = "ix_refresh_token_member_id", columnList = "member_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_refresh_token_member"))
    private Member member;

    @Column(nullable = false, columnDefinition = "BINARY(32)")
    private byte[] tokenHash;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public RefreshToken(Member member, byte[] tokenHash, LocalDateTime expiresAt) {
        this.member = member;
        this.tokenHash = tokenHash.clone();
        this.expiresAt = expiresAt;
    }

    public byte[] getTokenHash() {
        return tokenHash.clone();
    }

    @PrePersist
    protected void setCreatedAt() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
