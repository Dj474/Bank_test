CREATE TABLE block_requests (
        id BIGSERIAL PRIMARY KEY,
        card_id BIGINT NOT NULL,
        user_id BIGINT NOT NULL,
        status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
        reason VARCHAR(500),
        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        processed_at TIMESTAMP,

        CONSTRAINT fk_block_requests_card FOREIGN KEY (card_id)
            REFERENCES cards (id) ON DELETE CASCADE,

        CONSTRAINT fk_block_requests_user FOREIGN KEY (user_id)
            REFERENCES users (id) ON DELETE CASCADE
);
