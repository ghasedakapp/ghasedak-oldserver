CREATE TABLE user_contacts (
   owner_user_id INT NOT NULL ,
   contact_user_id INT NOT NULL ,
   name VARCHAR (255),
   is_deleted BOOLEAN NOT NULL DEFAULT FALSE ,
   PRIMARY KEY (owner_user_id, contact_user_id)
);

CREATE INDEX idx_user_contacts_owner_user_id_is_deleted on user_contacts(owner_user_id, is_deleted);

CREATE TABLE user_phone_contacts (
   phone_number BIGINT NOT NULL ,
   PRIMARY KEY (owner_user_id, contact_user_id)
) inherits(user_contacts);