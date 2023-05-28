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

    override fun getRouteWithOneConnection(
        airportCodeDeparture: String,
        airportCodeArrival: String,
        departureDate: String,
        fareCondition: String
    ): MutableSet<RouteEntity> {
        val fareConditionCapitalized = fareCondition.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val query = """
            select distinct fl.flight_no, fl.departure_airport, fl.arrival_airport, tad.amount
            from flights fl
            join total_amount_distinct tad
              on tad.flight_no = fl.flight_no
            where fl.departure_airport='$airportCodeDeparture' and fl.scheduled_arrival::date = to_date('$departureDate', 'YYYY-MM-DD') and fl.arrival_airport='$airportCodeArrival' and tad.fare_conditions='$fareConditionCapitalized' and tad.amount!=0
            order by departure_airport, arrival_airport;
        """.trimIndent()
        val routes: MutableSet<RouteEntity> = mutableSetOf()
        val statement = connection.createStatement()
        val queryRes = statement.executeQuery(query)
        while (queryRes.next()){
            val departureAirports = ArrayList<String>()
            val arrivalAirports = ArrayList<String>()
            val flight = ArrayList<String>()
            departureAirports.add(queryRes.getString("departure_airport"))
            arrivalAirports.add(queryRes.getString("arrival_airport"))
            flight.add(queryRes.getString("flight_no"))
            routes.add(RouteEntity(
                connections = 1,
                departureAirports = departureAirports,
                arrivalAirports = arrivalAirports,
                price = queryRes.getString("amount")!!.toDouble().toInt(),
                flights = flight
            ))
        }
        return routes
    }

    override fun getRouteWithTwoConnection(
        airportCodeDeparture: String,
        airportCodeArrival: String,
        departureDate: String,
        fareCondition: String
    ): MutableSet<RouteEntity> {
        val fareConditionCapitalized = fareCondition.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        val query = """
            select distinct f.flight_no fl1, f.departure_airport departure, tad.amount f1_price, f2.departure_airport connection, f2.flight_no fl2, ta.amount f2_price, f2.departure_airport connection, f2.arrival_airport arrival
            from flights f
            join total_amount_distinct tad
            on tad.flight_no=f.flight_no and tad.fare_conditions='$fareConditionCapitalized' and tad.amount!=0 and f.scheduled_arrival::date = to_date('$departureDate', 'YYYY-MM-DD')
            left join flights f2
            on f.arrival_airport = f2.departure_airport and f2.arrival_airport != f.departure_airport
            join total_amount_distinct ta on f2.flight_no = ta.flight_no and ta.fare_conditions='$fareConditionCapitalized' and ta.amount!=0 and f2.scheduled_arrival::date=to_date('$departureDate', 'YYYY-MM-DD')
            where f.departure_airport = '$airportCodeDeparture'
            and f2.arrival_airport = '$airportCodeArrival';
        """.trimIndent()
        val routes: MutableSet<RouteEntity> = mutableSetOf()
        val statement = connection.createStatement()
        val queryRes = statement.executeQuery(query)
        while (queryRes.next()){
            val departureAirports = ArrayList<String>()
            val arrivalAirports = ArrayList<String>()
            val flight = ArrayList<String>()
            departureAirports.add(queryRes.getString("departure"))
            departureAirports.add(queryRes.getString("connection"))
            arrivalAirports.add(queryRes.getString("connection"))
            arrivalAirports.add(queryRes.getString("arrival"))
            flight.add(queryRes.getString("fl1"))
            flight.add(queryRes.getString("fl2"))
            val price1 = queryRes.getInt("f1_price")
            val price2 = queryRes.getInt("f2_price")
            routes.add(RouteEntity(
                connections = 2,
                departureAirports = departureAirports,
                arrivalAirports = arrivalAirports,
                price = price1 + price2,
                flights = flight
            ))
        }
        return routes
    }

    override fun bookPerson(
        departureDate: String,
        flightNo: String,
        fareCondition: String,
        name: String,
        contactData: String
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
        return TicketEntity("", flightNo, aircraft, departureDate, price)
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