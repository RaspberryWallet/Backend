package io.raspberrywallet

import io.raspberrywallet.server.Server

internal fun main(args: Array<String>) {
    val server = Server(ManagerMockup())
    server.start()
}


