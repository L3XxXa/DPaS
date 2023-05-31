package malov.nsu.ru

import malov.nsu.ru.exceptions.NotValidContactDataException

class Validator {
    companion object{
        fun validatePhone(phone: String){
            val regex = Regex("\\+[0-9]+")
            if (!phone.matches(regex) || phone.length != 12){
                throw NotValidContactDataException("Not a phone number")
            }
        }
        fun validateEmail(email: String){
            val regex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-z]+")
            if (!email.matches(regex)){
                throw NotValidContactDataException("Not an email")
            }
        }
        fun validateId(id: String){
            val regex = Regex("[0-9][0-9][0-9][0-9] [0-9][0-9][0-9][0-9][0-9][0-9]")
            if (!id.matches(regex)){
                throw NotValidContactDataException("Not an id")
            }
        }
    }
}