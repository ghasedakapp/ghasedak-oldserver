CREATE TABLE users (
  id           INT          NOT NULL,
  org_id       INT          NOT NULL,
  name         VARCHAR(255) NOT NULL,
  created_at   TIMESTAMP    NOT NULL,
  deleted_at   TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE users_info (
  user_id INT NOT NULL,
  country_code VARCHAR(2),
  nickname VARCHAR(255),
  about VARCHAR (255),
  PRIMARY KEY (user_id)
);

CREATE TABLE user_phones (
  org_id       INT          NOT NULL,
  user_id           INT          NOT NULL,
  phone_number      BIGINT       NOT NULL,
  PRIMARY KEY (org_id, user_id)
);

CREATE TABLE user_emails (
  org_id       INT          NOT NULL,
  user_id     INT                NOT NULL,
  email       VARCHAR (255)      NOT NULL,
  PRIMARY KEY (org_id, user_id)
);