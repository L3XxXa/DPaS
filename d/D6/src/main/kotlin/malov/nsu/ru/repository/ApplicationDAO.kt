package malov.nsu.ru.repository

import malov.nsu.ru.entity.AirportEntity
import malov.nsu.ru.entity.CityEntity

interface ApplicationDAO {
    fun init()
    fun getAirports(): MutableSet<AirportEntity>
    fun getAirportsCity(city: String): MutableSet<AirportEntity>
    fun getCities(): MutableSet<CityEntity>
}