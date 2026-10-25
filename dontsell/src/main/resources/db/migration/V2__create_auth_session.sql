CREATE TABLE auth_session (
    id UUID PRIMARY KEY,
    member_id BIGINT NOT NULL REFERENCES member(id),
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMP(6) WITH TIME ZONE NOT NULL
);
