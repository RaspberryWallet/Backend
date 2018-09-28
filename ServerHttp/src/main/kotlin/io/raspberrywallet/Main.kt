package io.raspberrywallet

import io.raspberrywallet.mock.ManagerMock
import io.raspberrywallet.server.Server

internal fun main(args: Array<String>) {
    val server = Server(ManagerMock())
    server.start()
}


