create table app_user (
    id bigint auto_increment primary key,
    username varchar (255) unique
);

create index app_user_username_idx on app_user(username);

create table account (
    id bigint auto_increment primary key,
    user_id bigint not null,
    balance decimal not null check balance > 0,

    foreign key (user_id) references app_user(id)
);