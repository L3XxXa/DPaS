package malov.nsu.ru.serializators.cities

import kotlinx.serialization.Serializable

@Serializable
data class CitiesResponseSerializer(val city: String)
