package malov.nsu.ru.serializators.book

import kotlinx.serialization.Serializable

@Serializable
data class BookRequestSerializer(val date: String, val passenger_id: String, val flight_no: String, val fare_condition: String, val name: String, val contact_phone: String, val contact_email: String)
