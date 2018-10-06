package io.raspberrywallet.ktor

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.raspberrywallet.Manager
import io.raspberrywallet.server.Server
import org.slf4j.event.Level

private lateinit var manager: Manager
fun startKtorServer(newManager: Manager) {
    manager = newManager
    embeddedServer(Netty, configure = {
        requestQueueLimit = 4
        runningLimit = 1
    }, port = Server.PORT, module = Application::mainModule).start(wait = true)
}

fun Application.mainModule() {

    install(ContentNegotiation) {
        jackson {

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
        post("/api/nextStep/{id}") {
            val id = call.parameters["id"]!!
            val input = call.receiveText()
            val inputMap: Map<String, String> = jacksonObjectMapper().readValue(input, object : TypeReference<Map<String, String>>() {})
            val response = manager.nextStep(id, inputMap)
            call.respond(mapOf("response" to response.status))
        }
        get("api/moduleHtmlUi/{id}") {
            val id = call.parameters["id"]!!
            val htmlUiForm = manager.getModuleUi(id) ?: ""
            call.respondText(text = htmlUiForm, contentType = ContentType.Text.Html)
        }
        get("/api/currentAddress") {
            call.respond(mapOf("currentAddress" to manager.currentReceiveAddress))
        }
        get("/api/freshAddress") {
            call.respond(mapOf("freshAddress" to manager.freshReceiveAddress))
        }
        get("/api/estimatedBalance") {
            call.respond(mapOf("estimatedBalance" to manager.estimatedBalance))
        }
        get("/api/availableBalance") {
            call.respond(mapOf("availableBalance" to manager.availableBalance))
        }
        get("/api/availableBalance") {
            call.respond(mapOf("availableBalance" to manager.availableBalance))
        }
        get("/api/cpuTemp") {
            call.respond(mapOf("cpuTemp" to manager.cpuTemperature))
        }
        static("/") {
            resources("assets")
        }
    }
}
