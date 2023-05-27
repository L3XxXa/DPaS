package malov.nsu.ru.repository

import malov.nsu.ru.entity.AirportEntity
import malov.nsu.ru.entity.CityEntity
import malov.nsu.ru.entity.FlightEntity
import malov.nsu.ru.entity.RouteEntity

interface ApplicationDAO {
    fun init()

    fun getAirports(): MutableSet<AirportEntity>

    fun getAirportsCity(city: String): MutableSet<AirportEntity>

    fun getCities(): MutableSet<CityEntity>

    fun getAirportOutboundSchedule(airportCode: String): MutableSet<FlightEntity>

    fun getAirportInboundSchedule(airportCode: String): MutableSet<FlightEntity>

    fun getRouteWithOneConnection(airportCodeDeparture: String, airportCodeArrival: String, departureDate: String, fareCondition: String): MutableSet<RouteEntity>

    fun getRouteWithTwoConnection(airportCodeDeparture: String, airportCodeArrival: String, departureDate: String, fareCondition: String): MutableSet<RouteEntity>
}