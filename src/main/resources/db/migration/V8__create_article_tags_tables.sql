CREATE TABLE IF NOT EXISTS tags
(
    id        BIGSERIAL NOT NULL,
    title     TEXT      NOT NULL,
    author_id BIGINT,
    created   TIMESTAMP,
    updated   TIMESTAMP,

    PRIMARY KEY (id)
);

ALTER TABLE tags
    DROP CONSTRAINT IF EXISTS author_id_foreign_key_from_users;
ALTER TABLE tags
    ADD CONSTRAINT author_id_foreign_key_from_users FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE tags
    DROP CONSTRAINT IF EXISTS title_unique;
ALTER TABLE tags
    ADD CONSTRAINT title_unique UNIQUE (title);


CREATE TABLE IF NOT EXISTS articles_tags
(
    id         BIGSERIAL NOT NULL,
    article_id BIGINT    NOT NULL,
    tag_id     BIGINT    NOT NULL,
    author_id  BIGINT,
    created    TIMESTAMP,
    updated    TIMESTAMP,

    PRIMARY KEY (id)
);

ALTER TABLE articles_tags
    DROP CONSTRAINT IF EXISTS article_id_foreign_key_from_articles;
ALTER TABLE articles_tags
    ADD CONSTRAINT article_id_foreign_key_from_articles FOREIGN KEY (article_id) REFERENCES articles (id);

ALTER TABLE articles_tags
    DROP CONSTRAINT IF EXISTS tag_id_foreign_key_from_tags;
ALTER TABLE articles_tags
    ADD CONSTRAINT tag_id_foreign_key_from_tags FOREIGN KEY (tag_id) REFERENCES tags (id);

ALTER TABLE articles_tags
    DROP CONSTRAINT IF EXISTS author_id_foreign_key_from_users;
ALTER TABLE articles_tags
    ADD CONSTRAINT author_id_foreign_key_from_users FOREIGN KEY (author_id) REFERENCES users (id);