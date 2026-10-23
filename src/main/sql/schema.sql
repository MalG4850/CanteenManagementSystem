-- Create Database
CREATE DATABASE IF NOT EXISTS canteen_db;
USE canteen_db;

-- Drop tables in reverse FK order (clean slate for fresh installs)
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS food_orders;
DROP TABLE IF EXISTS food_menu;

-- Food Menu Table
CREATE TABLE food_menu (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL UNIQUE,
    price DECIMAL(10, 2) NOT NULL CHECK (price >= 0),
    category VARCHAR(50) DEFAULT 'General',
    is_available BOOLEAN DEFAULT TRUE
);

-- Main Order Summary Table
CREATE TABLE food_orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_name VARCHAR(100) NOT NULL,
    customer_mobile VARCHAR(15) NOT NULL,
    base_amount DECIMAL(10, 2) NOT NULL,
    gst_amount DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    order_status ENUM('PENDING','PREPARING','READY','SERVED','CANCELLED') DEFAULT 'PENDING',
    qr_token VARCHAR(64) NOT NULL UNIQUE,
    whatsapp_sent BOOLEAN DEFAULT FALSE,
    short_code CHAR(3) NOT NULL,
    code_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uq_short_code_day UNIQUE (short_code, code_date)
);

-- Line Items Table
CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    item_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES food_orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES food_menu(id)
);

-- Index for faster kitchen queries
CREATE INDEX idx_orders_status ON food_orders(order_status);
CREATE INDEX idx_orders_qr_token ON food_orders(qr_token);

-- Category table
CREATE TABLE IF NOT EXISTS category (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Seed default categories
INSERT IGNORE INTO category (name) VALUES ('Snacks'), ('Breakfast'), ('Lunch'), ('Beverages'), ('Desserts');

-- Seed default menu items
INSERT IGNORE INTO food_menu (item_name, price, category, is_available) VALUES
('Vada Pav', 25.00, 'Snacks', TRUE),
('Bread Pattice', 40.00, 'Snacks', TRUE),
('Peri Peri Fries', 70.00, 'Snacks', TRUE);