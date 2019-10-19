INSERT INTO languages (lang_code, lang_name, native_name)
VALUES ('de', 'German', 'Deutsch'),
       ('it', 'Italian', 'Italiano'),
       ('es', 'Spanish', 'Español'),
       ('pt', 'Portuguese', 'Português'),
       ('zh', 'Chinese', '中文')
ON CONFLICT DO NOTHING;