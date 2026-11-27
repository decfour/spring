package com.kdj.food.repository;

import com.kdj.food.entity.FoodMaterial;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FoodMaterialRepository extends JpaRepository<FoodMaterial, Long> {
    @EntityGraph(attributePaths = "material")
    List<FoodMaterial> findAllByFood_IdOrderByIdAsc(Long foodId);

    @Modifying(flushAutomatically = true)
    @Query("delete from FoodMaterial link where link.food.id = :foodId")
    void deleteLinks(Long foodId);
}
