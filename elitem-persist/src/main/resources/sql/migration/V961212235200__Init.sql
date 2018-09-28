CREATE TABLE history_messages (
       user_id int NOT NULL,
       peer_type int NOT NULL,
       peer_id int NOT NULL,
       date timestamp NOT NULL,
       sender_user_id int NOT NULL,
       random_id bigint NOT NULL,
       message_content_header int NOT NULL,
       message_content_data bytea NOT NULL,
       deleted_at timestamp,
       PRIMARY KEY(user_id, peer_type, peer_id, date, sender_user_id, random_id)
);

CREATE TABLE user_dialogs (
       user_id int NOT NULL,
       peer_type int NOT NULL,
       peer_id int NOT NULL,
       owner_last_received_at timestamp not null,
       owner_last_read_at timestamp not null,
       created_at TIMESTAMP DEFAULT NOW(),
       is_favourite boolean DEFAULT FALSE,
       PRIMARY KEY(user_id, peer_type, peer_id)
);

create table dialog_commons(
    dialog_id varchar(255) not null,
    last_message_date timestamp not null,
    last_received_at timestamp not null,
    last_read_at timestamp not null,
    primary key(dialog_id)
);

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

CREATE TABLE groups (
       id int NOT NULL,
       creator_user_id int NOT NULL,
       access_hash bigint NOT NULL,
       title varchar(255) NOT NULL,
       created_at timestamp NOT NULL,
       type int NOT NULL,
       about varchar(1024),
       topic varchar(255),
       title_changer_user_id int NOT NULL,
       title_changed_at timestamp NOT NULL,
       title_change_random_id bigint NOT NULL,
       avatar_changer_user_id int NOT NULL,
       avatar_changed_at timestamp NOT NULL,
       avatar_change_random_id bigint NOT NULL,
       PRIMARY KEY (id)
);

CREATE TABLE group_users (
       group_id int NOT NULL,
       user_id int NOT NULL,
       inviter_user_id int NOT NULL,
       invited_at timestamp NOT NULL,
       joined_at timestamp DEFAULT NULL,
       is_admin boolean default false,
       PRIMARY KEY (group_id, user_id)
);

CREATE INDEX idx_group_users_user_id on group_users (user_id);