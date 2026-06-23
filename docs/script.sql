CREATE TABLE categories (
	id bigserial NOT NULL ,
	name varchar(30) NOT NULL,
	status boolean DEFAULT TRUE,
	
	created_by int8 NOT NULL,
	created_at timestamp NOT NULL DEFAULT NOW(),
	updated_by int8 NOT NULL,
	updated_at timestamp NOT NULL DEFAULT NOW(),
	
	CONSTRAINT pk_category PRIMARY KEY (id),
	CONSTRAINT unq_category_name UNIQUE (name)
);

CREATE TABLE products (
	id bigserial NOT NULL,
	category_id int8 NOT NULL,
	name varchar(50) NOT NULL,
	description varchar(500),
	price int4 NOT NULL,
	stock_quantity int8 NOT NULL,
	thumbnail_url text DEFAULT NULL,
	status boolean DEFAULT TRUE,
	
	created_by int8 NOT NULL,
	created_at timestamp NOT NULL DEFAULT NOW(),
	updated_by int8 NOT NULL,
	updated_at timestamp NOT NULL DEFAULT NOW(),
	
	CONSTRAINT pk_product PRIMARY KEY (id),
	CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES categories (id)
);

CREATE TABLE carts (
	id bigserial NOT NULL,
	customer_id int8 NOT NULL,
	platform varchar(30) NULL DEFAULT 'MAIN',
	status boolean DEFAULT TRUE,
	
	created_by int8 NOT NULL,
	created_at timestamp NOT NULL DEFAULT NOW(),
	updated_by int8 NOT NULL,
	updated_at timestamp NOT NULL DEFAULT NOW(),
	
	CONSTRAINT pk_carts PRIMARY KEY (id)
);

CREATE TABLE cart_items (
	id bigserial NOT NULL,
	cart_id int8 NOT NULL,
	product_id int8 NOT NULL,
	quantity int4 NOT NULL,
	unit_price int4 NOT NULL,
	
	created_by int8 NOT NULL,
	created_at timestamp NOT NULL DEFAULT NOW(),
	updated_by int8 NOT NULL,
	updated_at timestamp NOT NULL DEFAULT NOW(),
	
	CONSTRAINT pk_cart_items PRIMARY KEY (id),
	CONSTRAINT fk_cart FOREIGN KEY (cart_id) REFERENCES carts (id),
	CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products (id), 
	CONSTRAINT unq_cart_items UNIQUE (cart_id, product_id)
);

CREATE TABLE orders (
	id bigserial NOT NULL,
	customer_id int8 NOT NULL,
	cart_id int8 NOT NULL,
	status boolean DEFAULT TRUE,
	total_amount int8 NOT NULL,
	
	created_by int8 NOT NULL,
	created_at timestamp NOT NULL DEFAULT NOW(),
	updated_by int8 NOT NULL,
	updated_at timestamp NOT NULL DEFAULT NOW(),
	
	CONSTRAINT pk_orders PRIMARY KEY (id),
	CONSTRAINT fk_cart FOREIGN KEY (cart_id) REFERENCES carts (id)
);

CREATE TABLE shipping_details (
	id bigserial NOT NULL,
	customer_id int8 NOT NULL,
	order_id int8 NOT NULL,
	line1 text NOT NULL,
	city varchar(50) NOT NULL,
	state varchar(50) NOT NULL,
	postal_code int2 NOT NULL,
	
	created_by int8 NOT NULL,
	created_at timestamp NOT NULL DEFAULT NOW(),
	updated_by int8 NOT NULL,
	updated_at timestamp NOT NULL DEFAULT NOW(),

	CONSTRAINT pk_shipping_details PRIMARY KEY (id),
	CONSTRAINT fk_orders FOREIGN KEY (order_id) REFERENCES orders (id),
	CONSTRAINT unq_orders UNIQUE (order_id)
);


CREATE TABLE order_items (
	id bigserial NOT NULL,
	order_id int8 NOT NULL,
	product_id int8 NOT NULL,
	quantity int4 NOT NULL,
	
	created_by int8 NOT NULL,
	created_at timestamp NOT NULL DEFAULT NOW(),
	updated_by int8 NOT NULL,
	updated_at timestamp NOT NULL DEFAULT NOW(),
	
	CONSTRAINT pk_order_items PRIMARY KEY (id),
	CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES orders (id),
	CONSTRAINT fk_product FOREIGN KEY (product_id) REFERENCES products (id),
	CONSTRAINT unq_product UNIQUE (order_id, product_id) 
);