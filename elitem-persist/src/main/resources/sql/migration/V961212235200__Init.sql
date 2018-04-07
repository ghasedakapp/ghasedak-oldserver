create table auth_transactions (
    transaction_hash varchar(255) not null,
    app_id int not null,
    api_key varchar(255) not null,
    device_hash bytea not null,
    device_info varchar(255) default '',
    device_title varchar(255) not null,
    access_salt varchar(255) not null,
    is_checked boolean not null,
    deleted_at timestamp,
    primary key(transaction_hash)
);

create table auth_phone_transactions (
    phone_number bigint not null,
    primary key(transaction_hash)
) inherits(auth_transactions);


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

CREATE TABLE auth_ids (
    id varchar(255) NOT NULL,
    public_key_hash bigint,
    user_id int,
    deleted_at timestamp,
    PRIMARY KEY (id)
);