-- liquibase formatted sql

-- changeset romax:1
CREATE TABLE notification_task (
    id SERIAL PRIMARY KEY,
    chat_id BIGINT,
    notification TEXT,
    time TIMESTAMP
)