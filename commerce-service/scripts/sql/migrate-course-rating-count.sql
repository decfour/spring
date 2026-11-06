-- MySQL: run against the application database while application writes are stopped.
-- Preserve existing ratings; replace legacy likes with the actual rating count.
SET @ddl = IF(
    EXISTS(SELECT 1 FROM information_schema.columns
           WHERE table_schema = DATABASE() AND table_name = 'walk_course' AND column_name = 'rating_count'),
    'SELECT 1',
    'ALTER TABLE walk_course ADD COLUMN rating_count BIGINT NOT NULL DEFAULT 0'
);
PREPARE migration FROM @ddl;
EXECUTE migration;
DEALLOCATE PREPARE migration;

UPDATE walk_course c
SET rating_count = (SELECT COUNT(*) FROM walk_course_rating r WHERE r.course_id = c.id);

DROP TABLE IF EXISTS walk_course_like;

SET @ddl = IF(
    EXISTS(SELECT 1 FROM information_schema.columns
           WHERE table_schema = DATABASE() AND table_name = 'walk_course' AND column_name = 'like_count'),
    'ALTER TABLE walk_course DROP COLUMN like_count',
    'SELECT 1'
);
PREPARE migration FROM @ddl;
EXECUTE migration;
DEALLOCATE PREPARE migration;
