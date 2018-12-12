CREATE TABLE auth_phone_transactions (
  phone_number     BIGINT       NOT NULL,
  transaction_hash VARCHAR(255) NOT NULL,
  app_id           INT          NOT NULL,
  api_key          VARCHAR(255) NOT NULL,
  device_hash      TEXT         NOT NULL,
  device_info      TEXT         NOT NULL,
  created_at       TIMESTAMP    NOT NULL,
  code_hash        VARCHAR(255) NOT NULL,
  deleted_at       TIMESTAMP,
  PRIMARY KEY (transaction_hash)
);

CREATE TABLE auth_tokens (
  token_id   VARCHAR(255) NOT NULL,
  token_key  VARCHAR(255) NOT NULL,
  deleted_at TIMESTAMP,
  PRIMARY KEY (token_id)
);

CREATE TABLE auth_sessions (
  user_id      INT          NOT NULL,
  token_id     VARCHAR(255) NOT NULL,
  app_id       INT          NOT NULL,
  api_key      VARCHAR(255) NOT NULL,
  device_hash  TEXT         NOT NULL,
  device_info  TEXT         NOT NULL,
  session_time TIMESTAMP    NOT NULL,
  deleted_at   TIMESTAMP    NOT NULL,
  PRIMARY KEY (user_id, token_id)
);

CREATE INDEX idx_auth_sessions_token_id
  ON auth_sessions (token_id);

CREATE TABLE users (
  id           INT          NOT NULL,
  access_salt  TEXT         NOT NULL,
  name         VARCHAR(255) NOT NULL,
  country_code VARCHAR(2)   NOT NULL,
  created_at   TIMESTAMP    NOT NULL,
  nickname     VARCHAR(255),
  about        VARCHAR(255),
  deleted_at   TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE user_phones (
  user_id     INT          NOT NULL,
  id          INT          NOT NULL,
  access_salt VARCHAR(255) NOT NULL,
  number      BIGINT       NOT NULL,
  PRIMARY KEY (user_id, id)
);

-- --------------------------------------------------

CREATE TABLE history_messages (
  user_id                INT       NOT NULL,
  peer_type              INT       NOT NULL,
  peer_id                INT       NOT NULL,
  date                   TIMESTAMP NOT NULL,
  sender_user_id         INT       NOT NULL,
  random_id              BIGINT    NOT NULL,
  message_content_header INT       NOT NULL,
  message_content_data   BYTEA     NOT NULL,
  deleted_at             TIMESTAMP,
  PRIMARY KEY (user_id, peer_type, peer_id, date, sender_user_id, random_id)
);

CREATE TABLE user_dialogs (
  user_id                INT       NOT NULL,
  peer_type              INT       NOT NULL,
  peer_id                INT       NOT NULL,
  owner_last_received_at TIMESTAMP NOT NULL,
  owner_last_read_at     TIMESTAMP NOT NULL,
  created_at             TIMESTAMP DEFAULT NOW(),
  is_favourite           BOOLEAN   DEFAULT FALSE,
  PRIMARY KEY (user_id, peer_type, peer_id)
);

CREATE TABLE dialog_commons (
  dialog_id         VARCHAR(255) NOT NULL,
  last_message_date TIMESTAMP    NOT NULL,
  last_received_at  TIMESTAMP    NOT NULL,
  last_read_at      TIMESTAMP    NOT NULL,
  PRIMARY KEY (dialog_id)
);

CREATE TABLE auth_transactions (
  transaction_hash VARCHAR(255) NOT NULL,
  app_id           INT          NOT NULL,
  api_key          VARCHAR(255) NOT NULL,
  device_hash      BYTEA        NOT NULL,
  device_info      VARCHAR(255) DEFAULT '',
  device_title     VARCHAR(255) NOT NULL,
  access_salt      VARCHAR(255) NOT NULL,
  is_checked       BOOLEAN      NOT NULL,
  deleted_at       TIMESTAMP,
  PRIMARY KEY (transaction_hash)
);

CREATE TABLE auth_phone_transactions (
  phone_number BIGINT NOT NULL,
  PRIMARY KEY (transaction_hash)
)
  INHERITS (auth_transactions);


CREATE TABLE users (
  id           INT          NOT NULL,
  access_salt  TEXT         NOT NULL,
  name         VARCHAR(255) NOT NULL,
  country_code VARCHAR(2)   NOT NULL,
  sex          INT          NOT NULL,
  state        INT          NOT NULL,
  created_at   TIMESTAMP    NOT NULL,
  nickname     VARCHAR(255),
  about        VARCHAR(255),
  is_bot       BOOLEAN      NOT NULL DEFAULT FALSE,
  deleted_at   TIMESTAMP,
  PRIMARY KEY (id)
);

CREATE TABLE user_phones (
  user_id     INT          NOT NULL,
  id          INT          NOT NULL,
  access_salt VARCHAR(255) NOT NULL,
  number      BIGINT       NOT NULL,
  title       VARCHAR(64)  NOT NULL,
  PRIMARY KEY (user_id, id)
);

CREATE TABLE groups (
  id                      INT          NOT NULL,
  creator_user_id         INT          NOT NULL,
  access_hash             BIGINT       NOT NULL,
  title                   VARCHAR(255) NOT NULL,
  created_at              TIMESTAMP    NOT NULL,
  type                    INT          NOT NULL,
  about                   VARCHAR(1024),
  topic                   VARCHAR(255),
  title_changer_user_id   INT          NOT NULL,
  title_changed_at        TIMESTAMP    NOT NULL,
  title_change_random_id  BIGINT       NOT NULL,
  avatar_changer_user_id  INT          NOT NULL,
  avatar_changed_at       TIMESTAMP    NOT NULL,
  avatar_change_random_id BIGINT       NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE group_users (
  group_id        INT       NOT NULL,
  user_id         INT       NOT NULL,
  inviter_user_id INT       NOT NULL,
  invited_at      TIMESTAMP NOT NULL,
  joined_at       TIMESTAMP DEFAULT NULL,
  is_admin        BOOLEAN   DEFAULT FALSE,
  PRIMARY KEY (group_id, user_id)
);

CREATE INDEX idx_group_users_user_id
  ON group_users (user_id);