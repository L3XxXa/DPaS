package malov.nsu.ru.exceptions

class AlreadyCheckedInException(private val e: String): Exception(e)