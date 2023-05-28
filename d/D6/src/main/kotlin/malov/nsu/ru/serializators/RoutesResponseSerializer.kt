package malov.nsu.ru.serializators

import kotlinx.serialization.Serializable

@Serializable
data class RoutesResponseSerializer(val route: String, val departureAirport: String, val arrivalAirport: String, val scheduledArrival: String, val flightNo: String, val connections: Int, val price: Int)
