package malov.nsu.ru.serializators

import kotlinx.serialization.Serializable

@Serializable
data class BookRequestSerializer(val date: String, val flight_no: String, val fare_condition: String, val name: String, val contact_data: String)
