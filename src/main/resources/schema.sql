CREATE TABLE patient (
                         id SERIAL PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         health_card_number VARCHAR(14) NOT NULL,
                         age INTEGER NOT NULL,
                         sex VARCHAR(1) NOT NULL CHECK (sex IN ('M', 'F'))
);