package malov.nsu.ru.serializators.airports

import kotlinx.serialization.Serializable

@Serializable
data class AirportResponseSerializer(val code: String, val airportCity: String, val airportsName: String, val timeZone: String, val coordinates: String)
