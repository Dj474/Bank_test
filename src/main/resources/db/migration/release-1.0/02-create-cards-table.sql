CREATE TABLE cards (
       id BIGSERIAL PRIMARY KEY,
       card_number VARCHAR(255) NOT NULL,
       card_holder VARCHAR(100) NOT NULL,
       expiration_date DATE NOT NULL,
       status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
       balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
       user_id BIGINT NOT NULL,
       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

       CONSTRAINT fk_cards_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
