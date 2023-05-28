package malov.nsu.ru.exceptions

class NotValidContactData(private val e: String): Exception(e) {
}