package malov.nsu.ru.serializators.flights

import kotlinx.serialization.Serializable
import java.sql.Array

@Serializable
data class ScheduledFlightsResponseSerializer(val from: String, val to: String, val flight: String, val days: String)
