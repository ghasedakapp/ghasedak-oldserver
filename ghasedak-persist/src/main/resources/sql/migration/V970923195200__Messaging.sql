CREATE TABLE history_messages (
   chat_id         BIGINT NOT NULL,
   sequence_nr            INT       NOT NULL,
   date                   TIMESTAMP NOT NULL,
   sender_user_id         INT       NOT NULL,
   message_content_header INT       NOT NULL,
   message_content_data   bytea     NOT NULL,
   deleted_at             TIMESTAMP,
   PRIMARY KEY(chat_id, date, sender_user_id, sequence_nr)
);

CREATE TABLE user_dialogs (
   user_id                 INT NOT NULL,
   chat_id         BIGINT NOT NULL,
   owner_last_received_seq INT NOT NULL,
   owner_last_read_seq     INT NOT NULL,
   created_at              TIMESTAMP DEFAULT NOW(),
   PRIMARY KEY(user_id, chat_id)
);

CREATE TABLE dialog_commons(
  chat_id         BIGINT NOT NULL,
  last_message_date TIMESTAMP    NOT NULL,
  last_message_seq  INT          NOT NULL,
  last_received_seq INT          NOT NULL,
  last_read_seq     INT          NOT NULL,
  PRIMARY KEY (chat_id)
);
