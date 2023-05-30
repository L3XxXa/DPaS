package malov.nsu.ru.repository

import malov.nsu.ru.exceptions.NoFlightsForBookingException
import malov.nsu.ru.entity.*
import malov.nsu.ru.exceptions.AlreadyCheckedInException
import malov.nsu.ru.exceptions.NoSeatsException
import malov.nsu.ru.exceptions.NoSuchTicketException
import java.sql.Connection
import java.sql.DriverManager
import java.util.*


class ApplicationDAOImpl : ApplicationDAO {
    private lateinit var connection: Connection
    override fun init() {
        Class.forName("org.postgresql.Driver")
        val url = "jdbc:postgresql://localhost:5432/demo"
        val user = "postgres"
        val password = "admin"
        this.connection =  DriverManager.getConnection(url, user, password)
    }

    override fun getAirports(): MutableSet<AirportEntity> {
        val airports: MutableSet<AirportEntity> = mutableSetOf()
        val getAirportsQuery = "select * from airports;"
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
        val sqlQuery = "select * from airports where city =?"
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, city)
        val queryRes = statement.executeQuery()
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
        val getCitiesQuery = "select city from airports;"
        val statement = connection.createStatement()
        val queryRes = statement.executeQuery(getCitiesQuery)
        while (queryRes.next()) {
            cities.add(CityEntity(city = queryRes.getString("city")))
        }
        return cities
    }

    override fun getAirportOutboundSchedule(airportCode: String): MutableSet<FlightEntity> {
        val flights: MutableSet<FlightEntity> = mutableSetOf()
        val sqlQuery = "select * from routes where departure_airport =?"
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, airportCode)
        val queryRes = statement.executeQuery()
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
        val sqlQuery = "select * from routes where arrival_airport = ?"
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, airportCode)
        val queryRes = statement.executeQuery()
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
                join total_amount_distinct tad on f.flight_no = tad.flight_no and fare_conditions=?
                where departure_airport = ?
                  and f.scheduled_arrival::date = to_date(?, 'YYYY-MM-DD')
                union

                select cast(n.route || '->' ||  f.departure_airport as varchar(50)) as route, n.departure_airport, f.arrival_airport, f.scheduled_arrival,  cast(n.flight_no || '->' || f.flight_no as varchar(50)), n.count + 1, n.price + tad.amount
                from node as n
                         join flights as f on f.departure_airport = n.arrival_airport
                         join total_amount_distinct tad on f.flight_no = tad.flight_no and fare_conditions=?
                where f.arrival_airport != n.route
                  and date(f.scheduled_arrival) <= to_date(?, 'YYYY-MM-DD')
                  and n.scheduled_arrival < f.scheduled_departure
                  and f.departure_airport != ?
                  and count < ?
            )
            select * from node n
            where n.arrival_airport = ?;
        """.trimIndent()
        val routes: MutableSet<RouteEntity> = mutableSetOf()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, fareCondition)
        statement.setString(2, airportCodeDeparture)
        statement.setString(3, departureDate)
        statement.setString(4, fareCondition)
        statement.setString(5, maxArrivalDate)
        statement.setString(6, airportCodeArrival)
        statement.setInt(7, connections)
        statement.setString(8, airportCodeArrival)
        val queryRes = statement.executeQuery()
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
        where flight_no= ? and status='Scheduled' and
        scheduled_departure::date = to_date(?, 'YYYY-MM-DD'); 
        """.trimIndent()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, flightNo)
        statement.setString(2, departureDate)
        val queryRes = statement.executeQuery()
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
        val price = getPrice(flightNo, fareCondition)
        var bookRef = UUID.randomUUID().toString().replace("-", "").substring(0, 6).uppercase(Locale.getDefault())
        while (!checkBookRef(bookRef, connection)){
            bookRef = UUID.randomUUID().toString().replace("-", "").substring(0, 6).uppercase(Locale.getDefault())
        }
        var ticketNo = (0..9999999999999L).random().toString()
        while (!checkTicketNo(ticketNo)){
            ticketNo = (0..9999999999999L).random().toString()
        }
        addTicketToTableBookings(bookRef, price)
        addTicketToTableBookingsTickets(ticketNo, bookRef, passengerId, name.uppercase(), contactEmail, contactPhone)
        addTicketToTableTicketFlights(ticketNo, flightId, fareCondition, price)
        return TicketEntity(ticketNo, flightNo, aircraft, departureDate, price)
    }

    override fun checkin(ticketNo: String, flightNo: String): CheckinEntity {
        val seatNo: String
        val boardingNo: String
        try {
            val flightId = getFlightIdAndCheckIfTicketWasBooked(ticketNo)
            val fareCondition = getFareCondition(ticketNo)
            checkIfUserAlreadyCheckedIn(flightId, ticketNo, fareCondition)
            boardingNo = createBoardingNo(flightId)
            seatNo = getAvailableSeat(fareCondition, flightId)
            checkinUser(ticketNo, flightId, boardingNo, seatNo)
        } catch (e: Exception){
            throw e
        }
        return CheckinEntity(seatNo, boardingNo)
    }

    private fun checkinUser(ticketNo: String, flightId: String, boardingNo: String, seat: String){
        val sqlQuery = """
            insert into boarding_passes (ticket_no, flight_id, boarding_no, seat_no) values (?, ?, ?, ?) returning *;
        """.trimIndent()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, ticketNo)
        statement.setInt(2, flightId.toInt())
        statement.setInt(3, boardingNo.toInt())
        statement.setString(4, seat)
        statement.use {
            it.executeQuery()
        }
    }

    private fun getAvailableSeat(fareCondition: String, flightId: String): String{
        val sqlQuery = """
            select s.seat_no seat from seats1 as s
            left join boarding_passes bp on s.seat_no = bp.seat_no and bp.flight_id=?
            where fare_conditions=?
            and bp.seat_no IS NULL limit 1;
        """.trimIndent()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setInt(1, flightId.toInt())
        statement.setString(2, fareCondition)
        val queryRes = statement.executeQuery()
        var seat = ""
        while (queryRes.next()){
            seat = queryRes.getString("seat")
        }
        return seat
    }

    private fun createBoardingNo(flightId: String): String{
        val sqlQuery = """
            select count(*) from boarding_passes where flight_id=?;
        """.trimIndent()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setInt(1, flightId.toInt())
        val queryRes = statement.executeQuery()
        var counts = 0
        while (queryRes.next()){
            counts = queryRes.getInt("count")
        }
        return "${counts + 1}"
    }

    private fun checkIfUserAlreadyCheckedIn(flightId: String, ticketNo: String, fareCondition: String){
        val sqlQuery = """
            select * from boarding_passes
            join aircraft_flight_seats_conditions afsc on boarding_passes.flight_id = afsc.flight_id and boarding_passes.seat_no = afsc.seat_no
            where afsc.fare_conditions=? and boarding_passes.ticket_no=?
            and boarding_passes.flight_id=?;
        """.trimIndent()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, fareCondition)
        statement.setString(2, ticketNo)
        statement.setInt(3, flightId.toInt())
        val queryRes = statement.executeQuery()
        if (queryRes.next()){
            throw AlreadyCheckedInException("User with $ticketNo has already checked in")
        }
    }

    private fun getFlightIdAndCheckIfTicketWasBooked(ticketNo: String): String{
        val flightId: String
        val sqlQuery = """
            select flight_id from ticket_flights where ticket_no=?;
        """.trimIndent()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, ticketNo)
        val queryRes = statement.executeQuery()
        if (!queryRes.next()){
            throw NoSuchTicketException("Sorry, not found ticket for $ticketNo")
        }
        else{
            flightId = queryRes.getString("flight_id")
        }
        return flightId
    }

    private fun getFareCondition(ticketNo: String): String {
        val sqlQuery = """
            select fare_conditions from ticket_flights where ticket_no=?;
        """.trimIndent()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, ticketNo)
        val queryRes = statement.executeQuery()
        var fareCondition = ""
        while (queryRes.next()){
            fareCondition = queryRes.getString("fare_conditions")
        }
        return fareCondition
    }

    private fun addTicketToTableTicketFlights(ticketNo: String, flightId: String, fareCondition: String, price: Int){
        val sqlQuery = """
            insert into bookings.ticket_flights (ticket_no, flight_id, fare_conditions, amount) 
            values (?, ?, ?, ?)
            returning *;
        """.trimIndent()
        connection.autoCommit = false
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, ticketNo)
        statement.setInt(2, flightId.toInt())
        statement.setString(3, fareCondition)
        statement.setInt(4, price)
        statement.executeQuery()
        connection.commit()
        connection.autoCommit = true
    }

    private fun addTicketToTableBookings(bookRef: String, amount: Int){
        val sqlQuery = """
            insert into bookings.bookings (book_ref, book_date, total_amount) 
            values (?, now(), ?)
            returning *;
        """.trimIndent()
        connection.autoCommit = false
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, bookRef)
        statement.setInt(2, amount)
        statement.executeQuery()
        connection.commit()
        connection.autoCommit = true
    }

    private fun addTicketToTableBookingsTickets(ticketNo: String, bookRef: String, passengerId: String, passengerName: String, email: String, phone: String){
        val sqlQuery = """
            insert into bookings.tickets (ticket_no, book_ref, passenger_id, passenger_name, contact_data)
            values (?, ?, ?, ?, '{"email": "$email", "phone": "$phone"}')
            returning *;
        """.trimIndent()
        connection.autoCommit = false
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, ticketNo)
        statement.setString(2, bookRef)
        statement.setString(3, passengerId)
        statement.setString(4, passengerName)
        statement.executeQuery()
        connection.commit()
        connection.autoCommit = true
    }

    private fun checkTicketNo(ticketNo: String): Boolean{
        val sqlQuery = """
            select ticket_no from bookings.tickets where ticket_no=?;
        """.trimIndent()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, ticketNo)
        statement.use {
            val resSet = it.executeQuery()
            if (resSet.next()){
                connection.rollback()
                connection.autoCommit = true
                return false
            }
            return true
        }
    }
    private fun checkBookRef(bookRef: String, connection: Connection): Boolean{
        val sqlQuery = """
            select book_ref
            from bookings.bookings
            where book_ref=?;
        """.trimIndent()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, bookRef)
        statement.use {
            val resSet = it.executeQuery()
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
            select count(*)-(select count(*) from bookings.ticket_flights where fare_conditions = ? and flight_id = ?) left_places 
            from bookings.seats1
            where aircraft_code = ?;
        """.trimIndent()
        connection.autoCommit = false
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, fareCondition)
        statement.setInt(2, flightId.toInt())
        statement.setString(3, aircraftCode)
        val resSet = statement.executeQuery()
        while (resSet.next()) {
            seats = resSet.getInt("left_places")
        }
        return seats
    }

    private fun findAircraftByCode(code: String): String {
        val sqlQuery = """
             select model -> 'en' as model from aircrafts_data
             where aircraft_code=?;
        """.trimIndent()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, code)
        val queryRes = statement.executeQuery()
        var aircraft = ""
        while (queryRes.next()){
            aircraft = queryRes.getString("model")
        }
        return aircraft
    }



    private fun getPrice(flightNo: String, fareCondition: String): Int{
        var price = 0
        val sqlQuery="""
            select amount
            from total_amount_distinct
            where flight_no= ? and fare_conditions=?;
        """.trimIndent()
        val statement = connection.prepareStatement(sqlQuery)
        statement.setString(1, flightNo)
        statement.setString(2, fareCondition)
        val resSet = statement.executeQuery()
        while (resSet.next()){
            price = resSet.getInt("amount")
        }
        return price
    }
}