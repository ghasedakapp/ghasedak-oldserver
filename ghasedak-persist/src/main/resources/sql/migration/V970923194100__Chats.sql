CREATE TABLE chats (
       id BIGINT NOT NULL,
       type  INT NOT NULL,
       creator_user_id int NOT NULL,
       title varchar(255) NOT NULL,
       created_at timestamp NOT NULL,
       title_changer_user_id int NOT NULL,
       title_changed_at timestamp NOT NULL,
       title_change_random_id bigint NOT NULL,
       avatar_changer_user_id int NOT NULL,
       avatar_changed_at timestamp NOT NULL,
       avatar_change_random_id bigint NOT NULL,
       about varchar(1024),
       nick varchar(255),
       PRIMARY KEY (id)
);

CREATE TABLE chat_users (
       chat_id BIGINT NOT NULL,
       user_id INT NOT NULL,
       inviter_user_id int NOT NULL,
       invited_at timestamp NOT NULL,
       joined_at timestamp,
       is_admin   BOOLEAN  NOT NULL DEFAULT FALSE,
       PRIMARY KEY (chat_id, user_id)
);