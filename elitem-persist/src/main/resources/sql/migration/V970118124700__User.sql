CREATE TABLE users (
       id int NOT NULL,
       access_salt text NOT NULL,
       name varchar(255) NOT NULL,
       country_code varchar(2) NOT NULL,
       sex int NOT NULL,
       state int NOT NULL,
       created_at TIMESTAMP NOT NULL,
       nickname varchar(255),
       about varchar(255),
       is_bot boolean NOT NULL DEFAULT FALSE,
       deleted_at TIMESTAMP,
       PRIMARY KEY (id)
);

CREATE TABLE user_phones (
  user_id int NOT NULL,
  id int NOT NULL,
  access_salt varchar(255) NOT NULL,
  number bigint NOT NULL,
  title varchar(64) NOT NULL,
  PRIMARY KEY (user_id, id)
);