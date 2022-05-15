create table users(
	id int primary key not null,
	name varchar(31) not null,
	age int not null,
	deleted datetime default null,
	created datetime default null
);

create table orders(
	id int identity(100, 1) primary key not null,
	article varchar(63) not null,
	created datetime default null,
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
    (1, 'Bob', 52, null, dateadd(day, -1, getdate())),
    (2, 'Alice', 16, null, dateadd(day, -1, getdate())),
    (3, 'Joe', 8, getdate(), dateadd(day, -1, getdate())),
    (4, 'John', 32, null, dateadd(day, -2, getdate())),
    (5, 'Mike', 36, null, dateadd(day, -2, getdate())),
    (6, 'Bob Junior', 7, null, dateadd(day, -3, getdate()));

set identity_insert orders on;
insert into orders(id, article, created, user_id, price) values
    (1, 'pc', dateadd(day, -10, getdate()), 1, 1000),
    (2, 'lamp', dateadd(day, -10, getdate()), 1, 20),
    (3, 'phone', dateadd(day, 5, getdate()), 2, 300),
    (4, 'laptop', dateadd(day, 5, getdate()), 4, 800),
    (5, 'charger', dateadd(day, 5, getdate()), 4, 20),
    (6, 'to_rename', getdate(), 4, 20);
set identity_insert orders off;

insert into order_status(order_id, status) values
    (3, 'new'),
    (4, 'paid'),
    (5, 'shipped');