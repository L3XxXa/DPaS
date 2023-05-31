package malov.nsu.ru.exceptions

class NoFlightsForBookingException(private val e: String): Exception(e)