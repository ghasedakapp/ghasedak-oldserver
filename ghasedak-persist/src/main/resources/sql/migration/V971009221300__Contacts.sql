CREATE TABLE user_contacts (
   owner_user_id   INT          NOT NULL,
   contact_user_id INT          NOT NULL,
   local_name      VARCHAR(255) NOT NULL,
   deleted_at   TIMESTAMP,
   PRIMARY KEY (owner_user_id, contact_user_id)
);

CREATE INDEX idx_user_contacts_owner_user_id_is_deleted on user_contacts(owner_user_id, deleted_at);

CREATE TABLE user_phone_contacts (
       phone_number bigint NOT NULL
) inherits(user_contacts);

CREATE TABLE user_email_contacts (
       email VARCHAR(255) NOT NULL
) inherits(user_contacts);

