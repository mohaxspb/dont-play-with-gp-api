CREATE TABLE IF NOT EXISTS users
(
    id          BIGSERIAL NOT NULL,
    full_name   TEXT,
    avatar      TEXT,
    enabled     BOOLEAN   NOT NULL,
    facebook_id TEXT,
    google_id   TEXT,
    vk_id       TEXT,
    username    TEXT,
    password    TEXT,
    name_first  TEXT,
    name_second TEXT,
    name_third  TEXT,
    created     TIMESTAMP,
    updated     TIMESTAMP,
    PRIMARY KEY (id)
);

ALTER TABLE users
    DROP CONSTRAINT IF EXISTS unique_username;
ALTER TABLE users
    ADD CONSTRAINT unique_username UNIQUE (username);

CREATE TABLE IF NOT EXISTS authorities
(
    id        BIGSERIAL NOT NULL,
    authority TEXT      NOT NULL,
    created   TIMESTAMP,
    updated   TIMESTAMP,
    PRIMARY KEY (id)
);

ALTER TABLE authorities
    DROP CONSTRAINT IF EXISTS unique_authority;
ALTER TABLE authorities
    ADD CONSTRAINT unique_authority UNIQUE (authority);

ALTER TABLE authorities
    DROP CONSTRAINT IF EXISTS check_upper_authority;
ALTER TABLE authorities
    ADD CONSTRAINT check_upper_authority CHECK (UPPER(authority) = authority);

INSERT INTO authorities (authority)
VALUES ('ADMIN'),
       ('USER')
ON CONFLICT DO NOTHING;

CREATE TABLE IF NOT EXISTS users_authorities
(
    id           BIGSERIAL NOT NULL,
    authority_id INT8      NOT NULL,
    user_id      INT8      NOT NULL,
    created      TIMESTAMP,
    updated      TIMESTAMP,
    PRIMARY KEY (id)
);

ALTER TABLE users_authorities
    DROP CONSTRAINT IF EXISTS unique_user_id_to_authority_id;
ALTER TABLE users_authorities
    ADD CONSTRAINT unique_user_id_to_authority_id UNIQUE (user_id, authority_id);

ALTER TABLE users_authorities
    DROP CONSTRAINT IF EXISTS authority_id_foreign_key_from_users;
ALTER TABLE users_authorities
    ADD CONSTRAINT authority_id_foreign_key_from_users FOREIGN KEY (authority_id) REFERENCES authorities (id);

ALTER TABLE users_authorities
    DROP CONSTRAINT IF EXISTS user_id_foreign_key_from_users;
ALTER TABLE users_authorities
    ADD CONSTRAINT user_id_foreign_key_from_users FOREIGN KEY (user_id) REFERENCES users (id);


CREATE TABLE IF NOT EXISTS oauth_access_token
(
    token_id          TEXT NOT NULL,
    authentication    BYTEA,
    authentication_id TEXT,
    client_id         TEXT,
    created           TIMESTAMP,
    refresh_token     TEXT,
    token             BYTEA,
    updated           TIMESTAMP,
    user_name         TEXT,
    PRIMARY KEY (token_id)
);


CREATE TABLE IF NOT EXISTS oauth_client_details
(
    client_id               TEXT NOT NULL,
    access_token_validity   INT4 NOT NULL,
    additional_information  TEXT,
    authorities             TEXT,
    authorized_grant_types  TEXT,
    autoapprove             TEXT,
    client_secret           TEXT,
    created                 TIMESTAMP,
    refresh_token_validity  INT4 NOT NULL,
    resource_ids            TEXT,
    scope                   TEXT,
    updated                 TIMESTAMP,
    web_server_redirect_uri TEXT,
    PRIMARY KEY (client_id)
);


CREATE TABLE IF NOT EXISTS oauth_client_token
(
    token_id          TEXT NOT NULL,
    authentication_id TEXT,
    client_id         TEXT,
    created           TIMESTAMP,
    token             BYTEA,
    updated           TIMESTAMP,
    user_name         TEXT,
    PRIMARY KEY (token_id)
);


CREATE TABLE IF NOT EXISTS oauth_refresh_token
(
    token_id       TEXT NOT NULL,
    authentication BYTEA,
    created        TIMESTAMP,
    token          BYTEA,
    updated        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (token_id)
);
