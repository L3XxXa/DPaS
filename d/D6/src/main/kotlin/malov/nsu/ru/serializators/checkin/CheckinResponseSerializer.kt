package malov.nsu.ru.serializators.checkin

import kotlinx.serialization.Serializable

@Serializable
data class CheckinResponseSerializer(val seatNo: String, val boardingNo: String)
