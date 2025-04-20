CREATE TABLE review_queue_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    item_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    item_data TEXT NOT NULL,
    processing BOOLEAN NOT NULL
);

CREATE INDEX idx_review_queue_items_item_id ON review_queue_items(item_id);
CREATE INDEX idx_review_queue_items_processing ON review_queue_items(processing);
