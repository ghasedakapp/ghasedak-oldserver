CREATE TABLE users (
  id           INT           NOT NULL,
  org_id       INT           NOT NULL,
  name         VARCHAR(255)  NOT NULL,
  created_at   TIMESTAMP     NOT NULL,
  about        VARCHAR (255),
  deleted_at   TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE users_auth (
  org_id       INT           NOT NULL,
  user_id      INT           NOT NULL,
  phone_number BIGINT,
  email        VARCHAR (255),
  nickname     VARCHAR(255),
  country_code VARCHAR(2),
  is_deleted   BOOLEAN       NOT NULL DEFAULT FALSE,
  PRIMARY KEY (org_id, user_id)
);

CREATE INDEX idx_users_auth_phone_number
  ON users_auth (phone_number);

CREATE INDEX idx_users_auth_email
  ON users_auth (email);

CREATE INDEX idx_users_auth_nickname
  ON users_auth (nickname);