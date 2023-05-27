package malov.nsu.ru.serializators

import kotlinx.serialization.Serializable

@Serializable
data class RoutesResponseSerializer(val connections: Int, val departureAirports: ArrayList<String>, val arrivalAirports: ArrayList<String>, val flights: ArrayList<String>, val price: Int)
