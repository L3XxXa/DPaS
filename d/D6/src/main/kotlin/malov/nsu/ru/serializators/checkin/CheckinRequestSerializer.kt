package malov.nsu.ru.serializators.checkin

import kotlinx.serialization.Serializable

@Serializable
data class CheckinRequestSerializer(val ticket_no: String, val flight: String)
