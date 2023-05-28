package malov.nsu.ru.repository

import malov.nsu.ru.exceptions.NoFlightsForBookingException
import malov.nsu.ru.entity.*
import malov.nsu.ru.exceptions.NoSeatsException
import java.sql.Connection
import java.sql.DriverManager
import java.util.*
import kotlin.collections.ArrayList


class ApplicationDAOImpl : ApplicationDAO {
    private lateinit var connection: Connection
    private val getAirportsQuery = "select * from airports;"
    private val getAirportsCityQuery = "select * from airports where city ="
    private val getCitiesQuery = "select city from airports;"
    private val getAirportOutboundScheduleQuery = "select * from routes where departure_airport ="
    private val getAirportInboundScheduleQuery = "select * from routes where arrival_airport ="
    override fun init() {
        Class.forName("org.postgresql.Driver")
        val url = "jdbc:postgresql://localhost:5432/demo"
        val user = "postgres"
        val password = "admin"
        this.connection =  DriverManager.getConnection(url, user, password)
    }

    override fun getAirports(): MutableSet<AirportEntity> {
        val airports: MutableSet<AirportEntity> = mutableSetOf()
        val statement = connection.createStatement()
        val queryRes = statement.executeQuery(getAirportsQuery)
        while (queryRes.next()){
            airports.add(AirportEntity(code = queryRes.getString("airport_code"),
                airportCity = queryRes.getString("city"),
                airportsName = queryRes.getString("airport_name"),
                timeZone = queryRes.getString("timezone"),
                coordinates = queryRes.getString("coordinates")))
        }
        return airports
    }

    override fun getAirportsCity(city: String): MutableSet<AirportEntity> {
        val airports: MutableSet<AirportEntity> = mutableSetOf()
        val statement = connection.createStatement()
        val queryRes = statement.executeQuery("$getAirportsCityQuery '$city';")
        while (queryRes.next()){
            airports.add(AirportEntity(code = queryRes.getString("airport_code"),
                airportCity = queryRes.getString("city"),
                airportsName = queryRes.getString("airport_name"),
                timeZone = queryRes.getString("timezone"),
                coordinates = queryRes.getString("coordinates")))
        }
        return airports
    }

    override fun getCities(): MutableSet<CityEntity>{
        val cities: MutableSet<CityEntity> = mutableSetOf()
        val statement = connection.createStatement()
        val queryRes = statement.executeQuery(getCitiesQuery)
        while (queryRes.next()) {
            cities.add(CityEntity(city = queryRes.getString("city")))
        }
        return cities
    }

    override fun getAirportOutboundSchedule(airportCode: String): MutableSet<FlightEntity> {
        val flights: MutableSet<FlightEntity> = mutableSetOf()
        val statement = connection.createStatement()
        val queryRes = statement.executeQuery("$getAirportOutboundScheduleQuery '$airportCode';")
        while (queryRes.next()){
            flights.add(FlightEntity(
                from = queryRes.getString("departure_airport"),
                to = queryRes.getString("arrival_airport"),
                flight = queryRes.getString("flight_no"),
                departureDate = queryRes.getArray("days_of_week").toString()))
        }
        return flights
    }

    override fun getAirportInboundSchedule(airportCode: String): MutableSet<FlightEntity> {
        val flights: MutableSet<FlightEntity> = mutableSetOf()
        val statement = connection.createStatement()
        val queryRes = statement.executeQuery("$getAirportInboundScheduleQuery '$airportCode';")
        while (queryRes.next()){
            flights.add(FlightEntity(
                from = queryRes.getString("departure_airport"),
                to = queryRes.getString("arrival_airport"),
                flight = queryRes.getString("flight_no"),
                departureDate = queryRes.getArray("days_of_week").toString()))
        }
        return flights
    }

    override fun getRoute(
        airportCodeDeparture: String,
        airportCodeArrival: String,
        departureDate: String,
        maxArrivalDate: String,
        fareCondition: String,
        connections: Int
    ): MutableSet<RouteEntity> {
        val sqlQuery = """
            WITH recursive node AS (
                select cast(f.departure_airport as varchar(50)) as route, f.departure_airport, f.arrival_airport, f.scheduled_arrival, cast(f.flight_no as varchar(50)), 0 count, cast(tad.amount as numeric) price
                from flights as f
                join total_amount_distinct tad on f.flight_no = tad.flight_no and fare_conditions='$fareCondition'
                where departure_airport = '$airportCodeDeparture'
                  and f.scheduled_arrival::date = to_date('$departureDate', 'YYYY-MM-DD')
                union

                select cast(n.route || '->' ||  f.arrival_airport as varchar(50)) as route, n.departure_airport, f.arrival_airport, f.scheduled_arrival,  cast(n.flight_no || '->' || f.flight_no as varchar(50)), n.count + 1, n.price + tad.amount
                from node as n
                         join flights as f on f.departure_airport = n.arrival_airport
                         join total_amount_distinct tad on f.flight_no = tad.flight_no and fare_conditions='$fareCondition'
                where f.arrival_airport != n.route
                  and date(f.scheduled_arrival) <= '$maxArrivalDate'
                  and n.scheduled_arrival < f.scheduled_departure
                  and f.departure_airport != '$airportCodeArrival'
                  and count < $connections
            )
            select * from node n
            where n.arrival_airport = '$airportCodeArrival';
        """.trimIndent()
        val routes: MutableSet<RouteEntity> = mutableSetOf()
        val statement = connection.createStatement()
        val queryRes = statement.executeQuery(sqlQuery)
        while (queryRes.next()){
            routes.add(
                RouteEntity(
                queryRes.getString("route"),
                queryRes.getString("departure_airport"),
                queryRes.getString("arrival_airport"),
                queryRes.getString("scheduled_arrival"),
                queryRes.getString("flight_no"),
                queryRes.getInt("count"),
                queryRes.getInt("price")
            )
            )
        }
        return routes
    }

    override fun bookPerson(
        departureDate: String,
        flightNo: String,
        fareCondition: String,
        name: String,
        passengerId: String,
        contactPhone: String,
        contactEmail: String
    ): TicketEntity {
        val sqlQuery = """
        select flight_id, aircraft_code from bookings.flights
        where flight_no='$flightNo' and status='Scheduled' and
        scheduled_departure::date = to_date('$departureDate', 'YYYY-MM-DD'); 
        """.trimIndent()
        val statement = connection.createStatement()
        val queryRes = statement.executeQuery(sqlQuery)
        val aircraftCode: String
        val flightId: String
        if (!queryRes.next()) {
            throw NoFlightsForBookingException("Sorry, no scheduled flights $flightNo")
        } else {
            aircraftCode = queryRes.getString("aircraft_code")
            flightId = queryRes.getString("flight_id")
        }
        val aircraft = findAircraftByCode(aircraftCode)
        val seats = getSeats(fareCondition, aircraftCode, connection, flightId)
        if (seats == 0) {
            throw NoSeatsException("No seats on $flightNo flight for $fareCondition left")
        }
        val price = getPrice(flightNo, fareCondition, connection)
        var bookRef = UUID.randomUUID().toString().replace("-", "").substring(0, 6).uppercase(Locale.getDefault())
        while (!checkBookRef(bookRef, connection)){
            bookRef = UUID.randomUUID().toString().replace("-", "").substring(0, 6).uppercase(Locale.getDefault())
        }
        var ticketNo = (0..9999999999999L).random().toString()
        while (!checkTicketNo(ticketNo, connection)){
            ticketNo = (0..9999999999999L).random().toString()
        }
        addTicketToTableBookings(bookRef, price, connection)
        addTicketToTableBookingsTickets(ticketNo, bookRef, connection, passengerId, name.uppercase(), contactEmail, contactPhone)
        addTicketToTableTicketFlights(ticketNo, flightId, fareCondition, price)
        return TicketEntity(ticketNo, flightNo, aircraft, departureDate, price)
    }

    private fun addTicketToTableTicketFlights(ticketNo: String, flightId: String, fareCondition: String, price: Int){
        val sqlQuery = """
            insert into bookings.ticket_flights (ticket_no, flight_id, fare_conditions, amount) 
            values ('$ticketNo', $flightId, '$fareCondition', $price)
            returning *;
        """.trimIndent()
        val statement = connection.createStatement()
        statement.use {
            it.executeQuery(sqlQuery)
        }
        connection.commit()
    }

    private fun addTicketToTableBookings(bookRef: String, amount: Int, connection: Connection){
        val sqlQuery = """
            insert into bookings.bookings (book_ref, book_date, total_amount) 
            values ('$bookRef', now(), $amount)
            returning *;
        """.trimIndent()
        val statement = connection.createStatement()
        statement.use {
            it.executeQuery(sqlQuery)
        }
        connection.commit()
    }

    private fun addTicketToTableBookingsTickets(ticketNo: String, bookRef: String, connection: Connection, passengerId: String, passengerName: String, email: String, phone: String){
        val sqlQuery = """
            insert into bookings.tickets (ticket_no, book_ref, passenger_id, passenger_name, contact_data)
            values ('$ticketNo', '$bookRef', '$passengerId', '$passengerName', '{"email": "$email", "phone": "$phone"}')
            returning *;
        """.trimIndent()
        val statement = connection.createStatement()
        statement.use {
            it.executeQuery(sqlQuery)
        }
        connection.commit()
    }

    private fun checkTicketNo(ticketNo: String, connection: Connection): Boolean{
        val sqlQuery = """
            select ticket_no from bookings.tickets where ticket_no='$ticketNo';
        """.trimIndent()
        val statement = connection.createStatement()
        statement.use {
            val resSet = it.executeQuery(sqlQuery)
            if (resSet.next()){
                connection.rollback()
                connection.autoCommit = true
                return false
            }
            return true
        }
    }
    private fun checkBookRef(bookRef: String, connection: Connection): Boolean{
        val sqlRequest = """
            select book_ref
            from bookings.bookings
            where book_ref='$bookRef';
        """.trimIndent()
        val statement = connection.createStatement()
        statement.use {
            val resSet = it.executeQuery(sqlRequest)
            if (resSet.next()){
                connection.rollback()
                connection.autoCommit = true
                return false
            }
            return true
        }
    }
    private fun getSeats(fareCondition: String, aircraftCode: String, connection: Connection, flightId: String): Int{
        var seats = 0
        val sqlQuery = """
            select count(*)-(select count(*) from bookings.ticket_flights where fare_conditions = '${fareCondition}' and flight_id = '$flightId') left_places 
            from bookings.seats 
            where aircraft_code = '$aircraftCode';
        """.trimIndent()
        connection.autoCommit = false
        val seatsStatement = connection.createStatement()
        seatsStatement.use {
            val resSet = it.executeQuery(sqlQuery)
            while (resSet.next()) {
                seats = resSet.getInt("left_places")
            }
        }
        return seats
    }

    private fun findAircraftByCode(code: String): String {
        val sqlQuery = """
             select model -> 'en' as model from aircrafts_data
             where aircraft_code='$code';
        """.trimIndent()
        val statement = connection.createStatement()
        val queryRes = statement.executeQuery(sqlQuery)
        var aircraft = ""
        while (queryRes.next()){
            aircraft = queryRes.getString("model")
        }
        return aircraft
    }



    private fun getPrice(flightNo: String, fareCondition: String, connection: Connection): Int{
        var price = 0
        var sqlQuery="""
            select amount
            from total_amount_distinct
            where flight_no='$flightNo' and fare_conditions='$fareCondition';
        """.trimIndent()
        var statement = connection.createStatement()
        statement.use {
            val resSet = it.executeQuery(sqlQuery)
            while (resSet.next()){
                price = resSet.getInt("amount")
            }
        }
        return price
    }
}