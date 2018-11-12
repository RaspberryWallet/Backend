package io.raspberrywallet.contract

sealed class Message(val message: String) {
    class InfoMessage(message: String) : Message(message)
    class ErrorMessage(message: String) : Message(message)
    class SuccessMessage(message: String) : Message(message)
}
