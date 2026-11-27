package com.kdj.food.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
        name = "material",
        uniqueConstraints = @UniqueConstraint(name = "uk_material_normalized_name", columnNames = "normalized_name")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Material {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(
            name = "normalized_name",
            nullable = false,
            length = 100,
            columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_as_cs"
    )
    private String normalizedName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Material(String name, String normalizedName) {
        this.name = name;
        this.normalizedName = normalizedName;
    }

    @PrePersist
    protected void setCreatedAt() {
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
    }
}
