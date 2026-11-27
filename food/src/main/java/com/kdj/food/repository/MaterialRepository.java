package com.kdj.food.repository;

import com.kdj.food.entity.Material;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Optional<Material> findByNormalizedName(String normalizedName);

    @Modifying
    @Query(value = """
            INSERT INTO material (name, normalized_name, created_at)
            VALUES (:name, :normalizedName, UTC_TIMESTAMP(6))
            ON DUPLICATE KEY UPDATE id = id
            """, nativeQuery = true)
    void insertIfAbsent(String name, String normalizedName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select material from Material material where material.normalizedName = :normalizedName")
    Optional<Material> findByNormalizedNameForUpdate(String normalizedName);

    Page<Material> findByNormalizedNameStartingWithOrderByNormalizedNameAsc(String prefix, Pageable pageable);
}
