package malov.nsu.ru.exceptions

class NotValidContactDataException(private val e: String): Exception(e)