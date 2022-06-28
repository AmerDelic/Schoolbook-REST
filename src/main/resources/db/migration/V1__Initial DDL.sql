create table if not exists authority
(
    id   int          not null
        primary key,
    name varchar(255) null
);

create table if not exists hibernate_sequence
(
    next_val bigint null
);

create table if not exists role
(
    id   int          not null
        primary key,
    name varchar(255) null
);

create table if not exists user
(
    id                      bigint       not null
        primary key,
    created_at              datetime(6)  not null,
    last_modified_date      datetime(6)  not null,
    email                   varchar(255) null,
    first_name              varchar(255) null,
    last_login_date         datetime(6)  null,
    last_login_date_display datetime(6)  null,
    last_name               varchar(255) null,
    password                varchar(255) null,
    profile_image_url       varchar(255) null,
    public_id               varchar(255) null,
    username                varchar(255) null
);

create table if not exists user_authorities
(
    users_id       bigint not null,
    authorities_id int    not null,
    primary key (users_id, authorities_id),
    constraint FKdd8lhvujos470g40gikxj22mb
        foreign key (authorities_id) references authority (id),
    constraint FKn6acb7bng2ljsl1bdwbqdfb4p
        foreign key (users_id) references user (id)
);

create table if not exists user_roles
(
    users_id bigint not null,
    roles_id int    not null,
    primary key (users_id, roles_id),
    constraint FK7ecyobaa59vxkxckg6t355l86
        foreign key (users_id) references user (id),
    constraint FKj9553ass9uctjrmh0gkqsmv0d
        foreign key (roles_id) references role (id)
);

