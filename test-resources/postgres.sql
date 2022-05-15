create table users(
	id int primary key not null,
	name varchar(31) not null,
	age int not null,
	deleted timestamp default null,
	created timestamp default null
);

create sequence orders_seq as integer start 100;
create table orders(
	id integer primary key not null default nextval('orders_seq'),
	article varchar(63) not null,
	created timestamp default null,
	user_id int not null,
	price real not null,
	constraint users_fk foreign key(user_id) references users(id)
);

create table order_status(
	order_id int not null,
	status varchar(31) not null,
	constraint orders_fk foreign key(order_id) references orders(id)
);

insert into users(id, name, age, deleted, created) values
    (1, 'Bob', 52, null, now() - interval '1 day'),
    (2, 'Alice', 16, null, now() - interval '1 day'),
    (3, 'Joe', 8, now(), now() - interval '1 day'),
    (4, 'John', 32, null, now() - interval '2 day'),
    (5, 'Mike', 36, null, now() - interval '2 day'),
    (6, 'Bob Junior', 7, null, now() - interval '3 day');

insert into orders(id, article, created, user_id, price) values
    (1, 'pc', now() - interval '10 day', 1, 1000),
    (2, 'lamp', now() - interval '10 day', 1, 20),
    (3, 'phone', now() + interval '5 day', 2, 300),
    (4, 'laptop', now() + interval '5 day', 4, 800),
    (5, 'charger', now() + interval '5 day', 4, 20),
    (6, 'to_rename', now(), 4, 20);

insert into order_status(order_id, status) values
    (3, 'new'),
    (4, 'paid'),
    (5, 'shipped');