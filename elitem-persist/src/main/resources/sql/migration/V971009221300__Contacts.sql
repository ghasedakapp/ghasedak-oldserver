CREATE TABLE user_contacts (
   owner_user_id   INT          NOT NULL,
   contact_user_id INT          NOT NULL,
   local_name      VARCHAR(255) NOT NULL,
   has_phone       BOOLEAN      NOT NULL DEFAULT FALSE,
   has_email       BOOLEAN      NOT NULL DEFAULT FALSE,
   is_deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
   PRIMARY KEY (owner_user_id, contact_user_id)
);

CREATE INDEX idx_user_contacts_owner_user_id_is_deleted on user_contacts(owner_user_id, is_deleted);