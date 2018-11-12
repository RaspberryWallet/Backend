package io.raspberrywallet.contract

import kotlinx.coroutines.channels.Channel

class CommunicationChannel(val channel: Channel<Message> = Channel(10)) {
    fun success(string: String) {
        channel.offer(Message.SuccessMessage(string))
    }

    fun error(string: String) {
        channel.offer(Message.ErrorMessage(string))
    }

    fun info(string: String) {
        channel.offer(Message.InfoMessage(string))
    }
}
