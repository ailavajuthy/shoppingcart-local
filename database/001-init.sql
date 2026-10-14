CREATE TABLE users (
 user_id SERIAL PRIMARY KEY, name TEXT NOT NULL, email TEXT NOT NULL UNIQUE,
 password TEXT NOT NULL, phone TEXT NOT NULL UNIQUE,
 CONSTRAINT chk_phone CHECK (phone ~ '^[0-9]{10,15}$')
);
CREATE TABLE product (
 product_id SERIAL PRIMARY KEY, product_name TEXT NOT NULL, description TEXT,
 price NUMERIC(12,2) NOT NULL CHECK(price>=0), stock_quantity INT NOT NULL CHECK(stock_quantity>=0), category TEXT NOT NULL
);
CREATE TABLE shopping_cart (cart_id SERIAL PRIMARY KEY, user_id INT NOT NULL UNIQUE REFERENCES users(user_id));
CREATE TABLE cart_items (
 cart_id INT REFERENCES shopping_cart(cart_id) ON DELETE CASCADE,
 product_id INT REFERENCES product(product_id), quantity INT NOT NULL CHECK(quantity>0), PRIMARY KEY(cart_id,product_id)
);
CREATE TABLE orders (order_id SERIAL PRIMARY KEY, user_id INT REFERENCES users(user_id), order_date TIMESTAMPTZ NOT NULL DEFAULT NOW(), total_amount NUMERIC(12,2) NOT NULL, payment_status TEXT NOT NULL DEFAULT 'PAID');
CREATE TABLE order_items (order_id INT REFERENCES orders(order_id), product_id INT REFERENCES product(product_id), quantity INT NOT NULL, unit_price NUMERIC(12,2) NOT NULL, PRIMARY KEY(order_id,product_id));
CREATE TABLE app_sessions (token TEXT PRIMARY KEY, user_id INT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE, expires_at TIMESTAMPTZ NOT NULL);
CREATE INDEX app_sessions_expiry ON app_sessions(expires_at);
CREATE FUNCTION add_to_cart(uid INT, pid INT, qty INT) RETURNS TEXT LANGUAGE plpgsql AS $$
DECLARE cid INT; stock INT; existing INT;
BEGIN
 IF qty<=0 THEN RAISE EXCEPTION 'Quantity must be positive'; END IF;
 INSERT INTO shopping_cart(user_id) VALUES(uid) ON CONFLICT(user_id) DO NOTHING;
 SELECT cart_id INTO cid FROM shopping_cart WHERE user_id=uid FOR UPDATE;
 SELECT stock_quantity INTO stock FROM product WHERE product_id=pid;
 IF NOT FOUND THEN RAISE EXCEPTION 'Product not found'; END IF;
 SELECT COALESCE((SELECT quantity FROM cart_items WHERE cart_id=cid AND product_id=pid),0) INTO existing;
 IF existing+qty>stock THEN RAISE EXCEPTION 'Insufficient stock'; END IF;
 INSERT INTO cart_items(cart_id,product_id,quantity) VALUES(cid,pid,qty)
 ON CONFLICT(cart_id,product_id) DO UPDATE SET quantity=cart_items.quantity+EXCLUDED.quantity;
 RETURN 'SUCCESS';
END $$;
CREATE FUNCTION get_cart_total(uid INT) RETURNS NUMERIC LANGUAGE sql AS $$
 SELECT COALESCE(SUM(p.price*ci.quantity),0) FROM shopping_cart sc JOIN cart_items ci USING(cart_id) JOIN product p USING(product_id) WHERE sc.user_id=uid;
$$;
CREATE FUNCTION checkout_cart(uid INT) RETURNS TEXT LANGUAGE plpgsql AS $$
DECLARE cid INT; oid INT; total NUMERIC; item RECORD;
BEGIN
 SELECT cart_id INTO cid FROM shopping_cart WHERE user_id=uid FOR UPDATE;
 IF cid IS NULL OR NOT EXISTS(SELECT 1 FROM cart_items WHERE cart_id=cid) THEN RETURN 'Cart is empty'; END IF;
 -- Lock products in the same order across transactions before checking stock.
 PERFORM p.product_id FROM product p JOIN cart_items ci USING(product_id) WHERE ci.cart_id=cid ORDER BY p.product_id FOR UPDATE OF p;
 IF EXISTS(SELECT 1 FROM cart_items ci JOIN product p USING(product_id) WHERE ci.cart_id=cid AND ci.quantity>p.stock_quantity) THEN RETURN 'Insufficient stock'; END IF;
 total := get_cart_total(uid);
 INSERT INTO orders(user_id,total_amount) VALUES(uid,total) RETURNING order_id INTO oid;
 INSERT INTO order_items SELECT oid,ci.product_id,ci.quantity,p.price FROM cart_items ci JOIN product p USING(product_id) WHERE ci.cart_id=cid;
 UPDATE product p SET stock_quantity=p.stock_quantity-ci.quantity FROM cart_items ci WHERE ci.cart_id=cid AND ci.product_id=p.product_id;
 DELETE FROM cart_items WHERE cart_id=cid;
 RETURN 'SUCCESS order ' || oid;
END $$;
INSERT INTO product(product_name,description,price,stock_quantity,category) VALUES
 ('Headphones','Wireless demo headphones',1499,10000,'Electronics'),
 ('Running Shoes','Demo sports shoes',2499,10000,'Footwear'),
 ('DevOps Handbook','Demo learning book',699,10000,'Books'),
 ('Backpack','Demo backpack',999,10000,'Accessories'),
 ('Football','Demo football',599,10000,'Sports');
INSERT INTO users(name,email,password,phone) VALUES('Demo Student','demo@example.com','demo123','9876543210');
