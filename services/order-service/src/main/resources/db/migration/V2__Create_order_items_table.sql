CREATE TABLE order_schema.order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    price_at_order DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order FOREIGN KEY (order_id) REFERENCES order_schema.orders(id) ON DELETE CASCADE
);

CREATE INDEX idx_order_items_order_id ON order_schema.order_items(order_id);