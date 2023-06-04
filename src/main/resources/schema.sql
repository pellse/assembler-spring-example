CREATE TABLE IF NOT EXISTS patient
(
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    health_card_number TEXT NOT NULL
);