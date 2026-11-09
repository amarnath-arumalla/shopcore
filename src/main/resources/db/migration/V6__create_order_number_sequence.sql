-- One shared counter for order numbers, safe across any number of app servers.
-- Starts at 2000 so new numbers never clash with the sample orders (ORD-1001...).
CREATE SEQUENCE order_number_seq START WITH 2000;