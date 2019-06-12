create table app_user (
    id bigint auto_increment primary key,
    username varchar (255) unique
);

create table account (
    id bigint auto_increment primary key,
    user_id bigint not null,
    balance decimal not null,

    foreign key (user_id) references app_user(id)
);