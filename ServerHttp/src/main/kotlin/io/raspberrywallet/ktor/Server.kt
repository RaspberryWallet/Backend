package io.raspberrywallet.ktor

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.gson.gson
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing
import io.raspberrywallet.mock.ManagerMock
import org.slf4j.event.Level


fun Application.mainModule() {
    val manager = ManagerMock()
    install(ContentNegotiation) {
        gson {
            setPrettyPrinting()
        }
    }
    install(CallLogging) {
        level = Level.INFO
    }

    routing {
        get("/api/ping") {
            call.respond(mapOf("ping" to manager.ping()))
        }
        get("/api/modules") {
            call.respond(manager.modules)
        }
        get("/api/moduleState/{id}") {
            val id = call.parameters["id"]!!
            val moduleState = manager.getModuleState(id)
            call.respond(mapOf("state" to moduleState.name, "message" to moduleState.message))
        }
        get("/api/currentAddress") {
            call.respond(manager.currentReceiveAddress)
        }
        get("/api/freshAddress") {
            call.respond(manager.freshReceiveAddress)
        }
        get("/api/estimatedBalance") {
            call.respond(manager.estimatedBalance)
        }
        get("/api/availableBalance") {
            call.respond(manager.availableBalance)
        }
        static("/webapp") {
            resources("assets")
        }
    }
}
