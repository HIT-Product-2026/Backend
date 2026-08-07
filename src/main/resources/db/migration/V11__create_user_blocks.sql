CREATE TABLE user_blocks (
    id UUID NOT NULL,
    blocker_id UUID NOT NULL,
    blocked_id UUID NOT NULL,
    created_at TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT uk_user_block UNIQUE (blocker_id, blocked_id),

    CONSTRAINT fk_user_blocks_blocker
        FOREIGN KEY (blocker_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_user_blocks_blocked
        FOREIGN KEY (blocked_id)
        REFERENCES users(id)
        ON DELETE CASCADE,

    CHECK (blocker_id <> blocked_id)
);

CREATE INDEX idx_user_blocks_blocker
ON user_blocks(blocker_id);

CREATE INDEX idx_user_blocks_blocked
ON user_blocks(blocked_id);