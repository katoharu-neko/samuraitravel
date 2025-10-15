CREATE INDEX idx_reservations_house_dates
  ON reservations (house_id, checkin_date, checkout_date);
