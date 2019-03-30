CREATE TABLE users (
  id           INT           NOT NULL,
  org_id       INT           NOT NULL,
  name         VARCHAR(255)  NOT NULL,
  sex int NOT NULL,
  created_at   TIMESTAMP     NOT NULL,
  is_bot       BOOLEAN   NOT NULL DEFAULT FALSE,
  about        VARCHAR (255),
  deleted_at   TIMESTAMP,
  nickname     VARCHAR(255),
  country_code VARCHAR(2),
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_users_nickname ON users (nickname);

CREATE TABLE user_emails (
       id int NOT NULL,
       user_id int NOT NULL,
       email varchar(255) NOT NULL,
       title varchar(255) NOT NULL,
       PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_user_emails_email ON user_emails (email);
CREATE INDEX idx_user_emails_user_id ON user_emails (user_id);

CREATE TABLE user_phones (
  id int NOT NULL,
  user_id int NOT NULL,
  org_id  INT NOT NULL,
  number bigint NOT NULL,
  title varchar(64) NOT NULL,
  PRIMARY KEY (user_id, id)
);

CREATE UNIQUE INDEX user_phones_number_idx ON user_phones (number);
