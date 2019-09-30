CREATE TABLE IF NOT EXISTS comments
(
    id         BIGSERIAL NOT NULL,
    text       TEXT      NOT NULL,
    article_id BIGINT,
    author_id  BIGINT,
    created    TIMESTAMP,
    updated    TIMESTAMP,

    PRIMARY KEY (id)
);

ALTER TABLE comments
    DROP CONSTRAINT IF EXISTS author_id_foreign_key_from_users;
ALTER TABLE comments
    ADD CONSTRAINT author_id_foreign_key_from_users FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE comments
    DROP CONSTRAINT IF EXISTS article_id_foreign_key_from_articles;
ALTER TABLE comments
    ADD CONSTRAINT article_id_foreign_key_from_articles FOREIGN KEY (article_id) REFERENCES articles (id);
