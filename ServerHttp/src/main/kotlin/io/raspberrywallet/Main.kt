package io.raspberrywallet

import io.ktor.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.raspberrywallet.ktor.mainModule
import io.raspberrywallet.mock.ManagerMock
import io.raspberrywallet.server.Server

internal fun main(args: Array<String>) {
    if (args.isNotEmpty() && args[0] == "ktor") {
        embeddedServer(Netty, Server.PORT, module = Application::mainModule).start(wait = true)
    } else {
        val server = Server(ManagerMock())
        server.start()

    }
}


