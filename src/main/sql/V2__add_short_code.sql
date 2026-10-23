-- Migration: add daily incremental short_code to food_orders
ALTER TABLE food_orders
    ADD COLUMN short_code CHAR(3) NOT NULL,
    ADD COLUMN code_date DATE NOT NULL DEFAULT (CURRENT_DATE),
    ADD CONSTRAINT uq_short_code_day UNIQUE (short_code, code_date);