ALTER TABLE users
    DROP INDEX telegram_chat_id;

ALTER TABLE users
    DROP COLUMN telegram_chat_id;
