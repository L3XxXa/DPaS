package malov.nsu.ru.repository

import io.ktor.server.application.*
import malov.nsu.ru.entity.AirportEntity
import java.sql.Connection
import java.sql.DriverManager


class ApplicationDAOImpl : ApplicationDAO {
    private lateinit var connection: Connection
    private val getAirportsQuery = "select * from airports;"
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
}