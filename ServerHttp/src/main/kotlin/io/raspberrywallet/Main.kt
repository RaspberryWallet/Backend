package io.raspberrywallet

import io.raspberrywallet.mock.ManagerMock
import io.raspberrywallet.server.startKtorServer

internal fun main(args: Array<String>) {
    startKtorServer(ManagerMock())
}


