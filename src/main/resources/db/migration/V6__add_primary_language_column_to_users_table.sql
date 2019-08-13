CREATE OR REPLACE FUNCTION language_id_by_lang_code(langCode varchar(2)) RETURNS INT8
    LANGUAGE SQL AS
$$
SELECT id
FROM languages
WHERE lang_code = langCode;
$$;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS primary_language_id INT8 NOT NULL DEFAULT language_id_by_lang_code('en');

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS language_id_foreign_key_from_users;
ALTER TABLE users
    ADD CONSTRAINT language_id_foreign_key_from_users FOREIGN KEY (primary_language_id) REFERENCES languages (id);