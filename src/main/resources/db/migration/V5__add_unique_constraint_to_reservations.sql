ALTER TABLE reservations
ADD CONSTRAINT uq_inn_date UNIQUE (house_id, checkin_date, checkout_date);