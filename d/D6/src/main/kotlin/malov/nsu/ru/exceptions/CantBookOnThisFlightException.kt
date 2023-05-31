package malov.nsu.ru.exceptions

class CantBookOnThisFlightException(private val e: String) : Exception(e)