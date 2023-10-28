--liquibase formatted sql
--changeset evgen:1
CREATE TABLE notification_task (
id bigserial primary key,
chat_id bigint,
message varchar,
date TIMESTAMP
)
