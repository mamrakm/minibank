CREATE SCHEMA IF NOT EXISTS bank;

CREATE SEQUENCE IF NOT EXISTS bank.account_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS bank.customer_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE bank.account
(
    account_id  BIGINT NOT NULL,
    balance     BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    CONSTRAINT pk_account PRIMARY KEY (account_id)
);

CREATE TABLE bank.customer
(
    customer_id BIGINT       NOT NULL,
    first_name  VARCHAR(255) NOT NULL,
    middle_name VARCHAR(255),
    last_name   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_customer PRIMARY KEY (customer_id)
);

ALTER TABLE bank.account
    ADD CONSTRAINT FK_ACCOUNT_ON_CUSTOMER FOREIGN KEY (customer_id) REFERENCES bank.customer (customer_id);

