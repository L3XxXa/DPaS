package malov.nsu.ru.serializators

import kotlinx.serialization.Serializable

@Serializable
data class BookRequestSerializer(val flight: String, val booking_class: String, val name: String, val contact_data: String)
