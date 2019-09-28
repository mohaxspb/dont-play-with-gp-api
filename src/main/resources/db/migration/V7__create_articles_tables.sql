CREATE TABLE IF NOT EXISTS articles
(
    id                 BIGSERIAL NOT NULL,

    original_lang_id   BIGINT    NOT NULL,

    author_id          BIGINT,
    approved           BOOLEAN,
    approver_id        BIGINT,
    approved_date      TIMESTAMP,
    published          BOOLEAN,
    publisher_id       BIGINT,
    published_date     TIMESTAMP,
    created            TIMESTAMP,
    updated            TIMESTAMP,
    -- source* is null, if this is original article for dont-play-with-gp. Else - data from other site
    source_title       TEXT,
    source_url         TEXT,
    source_author_name TEXT,

    PRIMARY KEY (id)
);

ALTER TABLE articles
    DROP CONSTRAINT IF EXISTS original_lang_id_foreign_key_from_languages;
ALTER TABLE articles
    ADD CONSTRAINT original_lang_id_foreign_key_from_languages FOREIGN KEY (original_lang_id) REFERENCES languages (id);

ALTER TABLE articles
    DROP CONSTRAINT IF EXISTS author_id_foreign_key_from_users;
ALTER TABLE articles
    ADD CONSTRAINT author_id_foreign_key_from_users FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE articles
    DROP CONSTRAINT IF EXISTS approver_id_foreign_key_from_users;
ALTER TABLE articles
    ADD CONSTRAINT approver_id_foreign_key_from_users FOREIGN KEY (approver_id) REFERENCES users (id);

ALTER TABLE articles
    DROP CONSTRAINT IF EXISTS publisher_id_foreign_key_from_users;
ALTER TABLE articles
    ADD CONSTRAINT publisher_id_foreign_key_from_users FOREIGN KEY (publisher_id) REFERENCES users (id);


CREATE TABLE IF NOT EXISTS article_translations
(
    id                BIGSERIAL NOT NULL,

    title             TEXT      NOT NULL,
    short_description TEXT,
    image_url         TEXT,

    article_id        BIGINT    NOT NULL,
    lang_id           BIGINT    NOT NULL,

    author_id         BIGINT,
    approved          BOOLEAN,
    approver_id       BIGINT,
    approved_date     TIMESTAMP,
    published         BOOLEAN,
    publisher_id      BIGINT,
    published_date    TIMESTAMP,

    created           TIMESTAMP,
    updated           TIMESTAMP,

    PRIMARY KEY (id)
);

ALTER TABLE article_translations
    DROP CONSTRAINT IF EXISTS article_id_foreign_key_from_articles;
ALTER TABLE article_translations
    ADD CONSTRAINT article_id_foreign_key_from_articles FOREIGN KEY (article_id) REFERENCES articles (id);

ALTER TABLE article_translations
    DROP CONSTRAINT IF EXISTS lang_id_foreign_key_from_languages;
ALTER TABLE article_translations
    ADD CONSTRAINT lang_id_foreign_key_from_languages FOREIGN KEY (lang_id) REFERENCES languages (id);

ALTER TABLE article_translations
    DROP CONSTRAINT IF EXISTS author_id_foreign_key_from_users;
ALTER TABLE article_translations
    ADD CONSTRAINT author_id_foreign_key_from_users FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE article_translations
    DROP CONSTRAINT IF EXISTS approver_id_foreign_key_from_users;
ALTER TABLE article_translations
    ADD CONSTRAINT approver_id_foreign_key_from_users FOREIGN KEY (approver_id) REFERENCES users (id);

ALTER TABLE article_translations
    DROP CONSTRAINT IF EXISTS publisher_id_foreign_key_from_users;
ALTER TABLE article_translations
    ADD CONSTRAINT publisher_id_foreign_key_from_users FOREIGN KEY (publisher_id) REFERENCES users (id);

CREATE TABLE IF NOT EXISTS article_translation_versions
(
    id                     BIGSERIAL NOT NULL,

    article_translation_id BIGINT    NOT NULL,
    text                   TEXT      NOT NULL,

    author_id              BIGINT,
    approved               BOOLEAN,
    approver_id            BIGINT,
    approved_date          TIMESTAMP,
    published              BOOLEAN,
    publisher_id           BIGINT,
    published_date         TIMESTAMP,

    created                TIMESTAMP,
    updated                TIMESTAMP,

    PRIMARY KEY (id)
);

ALTER TABLE article_translation_versions
    DROP CONSTRAINT IF EXISTS article_translation_id_foreign_key_from_articles;
ALTER TABLE article_translation_versions
    ADD CONSTRAINT article_translation_id_foreign_key_from_articles FOREIGN KEY (article_translation_id) REFERENCES article_translations (id);

ALTER TABLE article_translation_versions
    DROP CONSTRAINT IF EXISTS author_id_foreign_key_from_users;
ALTER TABLE article_translation_versions
    ADD CONSTRAINT author_id_foreign_key_from_users FOREIGN KEY (author_id) REFERENCES users (id);

ALTER TABLE article_translation_versions
    DROP CONSTRAINT IF EXISTS approver_id_foreign_key_from_users;
ALTER TABLE article_translation_versions
    ADD CONSTRAINT approver_id_foreign_key_from_users FOREIGN KEY (approver_id) REFERENCES users (id);

ALTER TABLE article_translation_versions
    DROP CONSTRAINT IF EXISTS publisher_id_foreign_key_from_users;
ALTER TABLE article_translation_versions
    ADD CONSTRAINT publisher_id_foreign_key_from_users FOREIGN KEY (publisher_id) REFERENCES users (id);