create table order_status
(
    id        bigserial primary key,
    name      varchar(30) not null unique
);

create table client_order
(
    id                bigserial primary key,
    status_id bigint not null references order_status,
    date_order     timestamp with time zone,

);

