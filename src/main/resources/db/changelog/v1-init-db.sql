CREATE SEQUENCE user_id_gen START WITH 1 INCREMENT BY 20;
CREATE SEQUENCE subscr_id_gen START WITH 1 INCREMENT BY 20;
CREATE SEQUENCE service_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE payment_id_seq START WITH 1 INCREMENT BY 35;

CREATE TABLE subscribers (
    id BIGINT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP,
    last_logged_at TIMESTAMP
);

CREATE TABLE services (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    service_category VARCHAR(50) NOT NULL,
    subscriber_id BIGINT REFERENCES subscribers(id) ON DELETE SET NULL
);

CREATE TABLE subscriptions (
    id BIGINT PRIMARY KEY,
    service_id BIGINT NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    subscriber_id BIGINT NOT NULL REFERENCES subscribers(id) ON DELETE CASCADE,
    price NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    billing_period VARCHAR(50) NOT NULL,
    next_payment DATE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE
);

CREATE TABLE payments (
    id BIGINT PRIMARY KEY,
    subscription_id BIGINT NOT NULL REFERENCES subscriptions(id) ON DELETE CASCADE,
    subscriber_id BIGINT NOT NULL REFERENCES subscribers(id) ON DELETE CASCADE,
    amount NUMERIC(19, 2) NOT NULL,
    payment_date DATE NOT NULL,
    status VARCHAR(50)
);
