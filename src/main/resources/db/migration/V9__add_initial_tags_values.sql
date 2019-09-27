INSERT INTO tags (title)
VALUES ('Google'),
       ('GooglePlay'),
       ('YouTube'),
       ('ban'),
       ('news')
ON CONFLICT DO NOTHING;
