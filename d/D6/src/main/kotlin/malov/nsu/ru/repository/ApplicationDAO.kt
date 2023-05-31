package malov.nsu.ru.repository

import malov.nsu.ru.entity.*

interface ApplicationDAO {
    fun init()

    fun getAirports(): MutableSet<AirportEntity>

    fun getAirportsCity(city: String): MutableSet<AirportEntity>

    fun getCities(): MutableSet<CityEntity>

    fun getAirportOutboundSchedule(airportCode: String): MutableSet<FlightEntity>

    fun getAirportInboundSchedule(airportCode: String): MutableSet<FlightEntity>

    fun getRoute(airportCodeDeparture: String, airportCodeArrival: String, departureDate: String, maxArrivalDate: String, fareCondition: String, connections: Int): MutableSet<RouteEntity>

    fun bookPerson(departureDate: String, flightNo: String, fareCondition: String, name: String, passengerId: String, contactPhone: String, contactEmail: String): TicketEntity

    fun checkin(ticketNo: String, flightNo: String): CheckinEntity
}