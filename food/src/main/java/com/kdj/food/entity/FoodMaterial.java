package com.kdj.food.entity;

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
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "food_material",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_food_material_food_material", columnNames = {"food_id", "material_id"}
        ),
        indexes = @Index(name = "ix_food_material_material_id", columnList = "material_id"),
        check = @CheckConstraint(name = "ck_food_material_positive_quantity", constraint = "quantity > 0")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FoodMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "food_id", nullable = false, foreignKey = @ForeignKey(name = "fk_food_material_food"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Food food;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "material_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_food_material_material")
    )
    private Material material;

    @Column(precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(nullable = false, length = 20)
    private String unit;

    public FoodMaterial(
            Food food,
            Material material,
            BigDecimal quantity,
            String unit
    ) {
        this.food = food;
        this.material = material;
        this.quantity = quantity;
        this.unit = unit;
    }
}
