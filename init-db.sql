-- Product Service
CREATE USER product_user WITH PASSWORD 'product_pass';
CREATE DATABASE product_db OWNER product_user;
GRANT ALL PRIVILEGES ON DATABASE product_db TO product_user;

-- Cart Service
CREATE USER cart_user WITH PASSWORD 'cart_pass';
CREATE DATABASE cart_db OWNER cart_user;
GRANT ALL PRIVILEGES ON DATABASE cart_db TO cart_user;

-- Order Service
CREATE USER order_user WITH PASSWORD 'order_pass';
CREATE DATABASE order_db OWNER order_user;
GRANT ALL PRIVILEGES ON DATABASE order_db TO order_user;
