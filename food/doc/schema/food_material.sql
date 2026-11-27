-- Requires food.sql and material.sql.
CREATE TABLE food_material (
    id BIGINT NOT NULL AUTO_INCREMENT,
    food_id BIGINT NOT NULL,
    material_id BIGINT NOT NULL,
    quantity DECIMAL(10,3) NULL,
    unit VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_food_material_food_material UNIQUE (food_id, material_id),
    KEY ix_food_material_material_id (material_id),
    CONSTRAINT fk_food_material_food
        FOREIGN KEY (food_id) REFERENCES food (id) ON DELETE CASCADE,
    CONSTRAINT fk_food_material_material
        FOREIGN KEY (material_id) REFERENCES material (id) ON DELETE RESTRICT,
    CONSTRAINT ck_food_material_positive_quantity CHECK (quantity > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
