package malov.nsu.ru.serializators.book

import kotlinx.serialization.Serializable

@Serializable
data class BookResponseSerializer(val ticket: String, val flight: String, val aircraft: String, val date: String, val price: Int)
