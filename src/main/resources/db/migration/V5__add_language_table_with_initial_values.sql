CREATE TABLE IF NOT EXISTS languages
(
    id          BIGSERIAL  NOT NULL,
    lang_code   varchar(2) NOT NULL,
    lang_name   TEXT       NOT NULL,
    native_name TEXT       NOT NULL,
    created     TIMESTAMP,
    updated     TIMESTAMP,
    PRIMARY KEY (id)
);

ALTER TABLE languages
    DROP CONSTRAINT IF EXISTS unique_lang_code;
ALTER TABLE languages
    ADD CONSTRAINT unique_lang_code UNIQUE (lang_code);

ALTER TABLE languages
    DROP CONSTRAINT IF EXISTS check_lower_lang_code;
ALTER TABLE languages
    ADD CONSTRAINT check_lower_lang_code CHECK (LOWER(lang_code) = lang_code);

INSERT INTO languages (lang_code, lang_name, native_name)
VALUES ('en', 'English', 'English'),
       ('ru', 'Russian', 'Русский'),
       ('fr', 'French', 'Français')
ON CONFLICT DO NOTHING;
