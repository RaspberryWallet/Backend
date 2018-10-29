package io.raspberrywallet.server

import io.raspberrywallet.mock.ManagerMock

internal fun main(args: Array<String>) {
    startKtorServer(ManagerMock())
}
