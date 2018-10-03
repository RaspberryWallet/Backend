package io.raspberrywallet

import io.raspberrywallet.ktor.startKtorServer
import io.raspberrywallet.mock.ManagerMock
import io.raspberrywallet.server.Server

internal fun main(args: Array<String>) {
    if (args.isNotEmpty() && args[0] == "ktor")
        startKtorServer(ManagerMock())
    else
        Server(ManagerMock()).start()

}


