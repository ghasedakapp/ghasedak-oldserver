CREATE TABLE history_messages (
   user_id INT NOT NULL,
   peer_id INT NOT NULL,
   peer_type INT NOT NULL,
   sequence_nr INT NOT NULL,
   date TIMESTAMP NOT NULL,
   sender_user_id INT NOT NULL,
   message_content_header INT NOT NULL,
   message_content_data bytea NOT NULL,
   deleted_at TIMESTAMP,
   PRIMARY KEY(user_id, peer_id,peer_type, date, sender_user_id, sequence_nr)
);

CREATE TABLE user_dialogs (
   user_id INT NOT NULL,
   peer_type INT NOT NULL,
   peer_id INT NOT NULL,
   owner_last_received_seq INT NOT NULL,
   owner_last_read_seq INT NOT NULL,
   created_at TIMESTAMP DEFAULT NOW(),
   PRIMARY KEY(user_id, peer_type, peer_id)
);

create table dialog_commons(
  dialog_id VARCHAR(255) NOT NULL,
  last_message_date TIMESTAMP NOT NULL,
  last_message_seq INT NOT NULL,
  last_received_seq INT NOT NULL,
  last_read_seq INT NOT NULL,
  PRIMARY KEY (dialog_id)
);
