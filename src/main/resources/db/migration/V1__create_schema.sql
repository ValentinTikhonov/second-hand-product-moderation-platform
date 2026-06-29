-- V1__create_schema.sql

CREATE TABLE users (
    id            BIGINT       PRIMARY KEY,
    username      VARCHAR(100) NOT NULL UNIQUE,
    display_name  VARCHAR(200),
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL CHECK (role IN ('SELLER', 'MODERATOR')),
    blocked       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE products (
    id             VARCHAR(50)  PRIMARY KEY,
    seller_id      BIGINT       NOT NULL REFERENCES users(id),
    title          VARCHAR(255) NOT NULL,
    description    TEXT         NOT NULL,
    category       VARCHAR(20)  NOT NULL,
    size           VARCHAR(20)  NOT NULL,
    condition      VARCHAR(20)  NOT NULL,
    state          VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    price_currency VARCHAR(3)   NOT NULL,
    price_amount   BIGINT       NOT NULL,
    price_exponent INT          NOT NULL,
    terms_accepted BOOLEAN      NOT NULL DEFAULT FALSE,
    reviewer_id    BIGINT       REFERENCES users(id),
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL
);

CREATE TABLE product_image_urls (
    id         BIGINT       PRIMARY KEY AUTO_INCREMENT,
    product_id VARCHAR(50)  NOT NULL REFERENCES products(id),
    url        VARCHAR(500) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0
);

CREATE TABLE product_events (
    id         VARCHAR(50)  PRIMARY KEY,
    product_id VARCHAR(50)  NOT NULL REFERENCES products(id),
    event_type VARCHAR(50)  NOT NULL,
    actor_id   BIGINT       REFERENCES users(id),
    timestamp  TIMESTAMP    NOT NULL,
    metadata   TEXT
);

CREATE INDEX idx_products_seller_id   ON products(seller_id);
CREATE INDEX idx_products_state       ON products(state);
CREATE INDEX idx_products_state_created ON products(state, created_at);
CREATE INDEX idx_product_events_product_id ON product_events(product_id);
