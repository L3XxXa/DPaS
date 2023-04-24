CREATE TABLE flight_price AS (
    SELECT departure_airport, arrival_airport, fare_conditions, AVG(amount) amount
        FROM ticket_flights tf
        JOIN flights fl
          ON tf.flight_id = fl.flight_id
        GROUP BY fare_conditions, departure_airport, arrival_airport
);
