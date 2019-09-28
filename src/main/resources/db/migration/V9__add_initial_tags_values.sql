INSERT INTO tags (title)
VALUES ('Google'),
       ('GooglePlay'),
       ('YouTube'),
       ('ban'),
       ('news')
ON CONFLICT DO NOTHING;

ALTER TABLE articles_tags
    DROP CONSTRAINT IF EXISTS article_id_and_tag_id_unique;
ALTER TABLE articles_tags
    ADD CONSTRAINT article_id_and_tag_id_unique UNIQUE (article_id, tag_id);