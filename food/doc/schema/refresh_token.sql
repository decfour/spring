-- Requires member.sql.
CREATE TABLE refresh_token (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    token_hash BINARY(32) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_refresh_token_hash UNIQUE (token_hash),
    KEY ix_refresh_token_member_id (member_id),
    CONSTRAINT fk_refresh_token_member
        FOREIGN KEY (member_id) REFERENCES member (id)
) ENGINE=InnoDB;
