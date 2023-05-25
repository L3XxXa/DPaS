package malov.nsu.ru.repository

import malov.nsu.ru.entity.AirportEntity

interface ApplicationDAO {
    fun init()
    fun getAirports(): MutableSet<AirportEntity>
    fun getAirportsCity(city: String): MutableSet<AirportEntity>
    fun getCities()
}