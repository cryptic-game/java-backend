create table "user"
(
    id      uuid         not null primary key,
    version bigint       not null,
    name    varchar(255) not null unique,
    created timestamp    not null default current_timestamp,
    last    timestamp    not null default current_timestamp
);

create table "user_oauth"
(
    user_id          uuid         not null references "user",
    version          bigint       not null,
    provider_id      varchar(255) not null,
    provider_user_id varchar(255) not null unique,
    created          timestamp    not null default current_timestamp,
    last             timestamp    not null default current_timestamp,
    primary key (user_id, provider_id),
    unique (provider_id, provider_user_id)
);
