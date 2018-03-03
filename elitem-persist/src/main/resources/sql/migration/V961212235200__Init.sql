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
