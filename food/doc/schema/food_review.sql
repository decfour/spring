-- Requires member.sql and food.sql.
CREATE TABLE food_review (
    id BIGINT NOT NULL AUTO_INCREMENT,
    food_id BIGINT NOT NULL,
    member_id BIGINT NOT NULL,
    effort TINYINT NOT NULL,
    taste TINYINT NOT NULL,
    value TINYINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_food_review_food_member UNIQUE (food_id, member_id),
    KEY ix_food_review_member_id (member_id),
    CONSTRAINT fk_food_review_food
        FOREIGN KEY (food_id) REFERENCES food (id) ON DELETE CASCADE,
    CONSTRAINT fk_food_review_member
        FOREIGN KEY (member_id) REFERENCES member (id),
    CONSTRAINT ck_food_review_effort CHECK (effort BETWEEN 1 AND 5),
    CONSTRAINT ck_food_review_taste CHECK (taste BETWEEN 1 AND 5),
    CONSTRAINT ck_food_review_value CHECK (value BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
