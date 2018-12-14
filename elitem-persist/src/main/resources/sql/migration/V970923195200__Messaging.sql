CREATE TABLE history_messages (
       chat_id bigint NOT NULL,
       peer_id int NOT NULL,
       peer_type int NOT NULL,
       sequence_nr int NOT NULL,
       date timestamp NOT NULL,
       sender_user_id int NOT NULL,
       message_content_header int NOT NULL,
       message_content_data bytea NOT NULL,
       deleted_at timestamp,
       PRIMARY KEY(chat_id, date, sender_user_id, sequence_nr)
);

CREATE TABLE user_dialogs (
       user_id int NOT NULL,
       peer_type int NOT NULL,
       peer_id int NOT NULL,
       owner_last_received_seq int not null,
       owner_last_read_seq int not null,
       created_at TIMESTAMP DEFAULT NOW(),
       PRIMARY KEY(user_id, peer_type, peer_id)
);

create table dialog_commons(
    chat_id bigint NOT NULL,
    last_message_date timestamp not null,
    last_received_seq int not null,
    last_read_seq int not null,
    primary key(chat_id)
);
