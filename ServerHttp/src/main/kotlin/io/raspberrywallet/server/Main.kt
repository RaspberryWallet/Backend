package io.raspberrywallet.server

import io.raspberrywallet.contract.CommunicationChannel
import io.raspberrywallet.contract.ServerConfig
import io.raspberrywallet.mock.ManagerMock
import java.io.File

internal fun main(args: Array<String>) {
    KtorServer(
        ManagerMock(),
        File(".").absolutePath,
        ServerConfig().apply { keystorePassword = "raspberrywallet".toCharArray() },
        CommunicationChannel()
    ).startBlocking()
}
