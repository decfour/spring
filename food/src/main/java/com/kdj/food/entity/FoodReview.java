package com.kdj.food.entity;

import com.kdj.food.common.persistence.AuditedEntity;
import jakarta.persistence.CheckConstraint;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "food_review",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_food_review_food_member",
                columnNames = {"food_id", "member_id"}
        ),
        indexes = @Index(name = "ix_food_review_member_id", columnList = "member_id"),
        check = {
                @CheckConstraint(name = "ck_food_review_effort", constraint = "effort BETWEEN 1 AND 5"),
                @CheckConstraint(name = "ck_food_review_taste", constraint = "taste BETWEEN 1 AND 5"),
                @CheckConstraint(name = "ck_food_review_value", constraint = "value BETWEEN 1 AND 5")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodReview extends AuditedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false, foreignKey = @ForeignKey(name = "fk_food_review_food"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Food food;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false, foreignKey = @ForeignKey(name = "fk_food_review_member"))
    private Member member;

    @Column(nullable = false)
    private byte effort;

    @Column(nullable = false)
    private byte taste;

    @Column(nullable = false)
    private byte value;

    public FoodReview(
            Food food,
            Member member,
            byte effort,
            byte taste,
            byte value
    ) {
        this.food = food;
        this.member = member;
        updateScores(effort, taste, value);
    }

    public void updateScores(byte effort, byte taste, byte value) {
        this.effort = effort;
        this.taste = taste;
        this.value = value;
    }
}
