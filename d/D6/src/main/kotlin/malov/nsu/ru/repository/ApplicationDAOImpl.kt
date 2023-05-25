package malov.nsu.ru.repository

import malov.nsu.ru.entity.AirportEntity
import malov.nsu.ru.entity.CityEntity
import malov.nsu.ru.entity.FlightEntity
import java.sql.Connection
import java.sql.DriverManager


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
                days = queryRes.getArray("days_of_week").toString()))
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
                days = queryRes.getArray("days_of_week").toString()))
        }
        return flights
    }
}