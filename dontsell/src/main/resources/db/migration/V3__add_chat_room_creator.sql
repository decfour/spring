ALTER TABLE chat_room
    ADD COLUMN created_by BIGINT NOT NULL REFERENCES member(id);
