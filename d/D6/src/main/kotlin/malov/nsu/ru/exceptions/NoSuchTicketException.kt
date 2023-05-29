package malov.nsu.ru.exceptions

class NoSuchTicketException(private val e: String): Exception(e)