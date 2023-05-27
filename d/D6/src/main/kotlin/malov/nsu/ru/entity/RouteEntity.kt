package malov.nsu.ru.entity

data class RouteEntity(val connections: Int, val departureAirports: ArrayList<String>, val arrivalAirports: ArrayList<String>, val flights: ArrayList<String>, val price: Int)
