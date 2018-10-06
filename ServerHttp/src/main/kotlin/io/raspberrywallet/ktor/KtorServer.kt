package io.raspberrywallet.ktor

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.html.HtmlContent
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
import io.raspberrywallet.ktor.Paths.availableBalance
import io.raspberrywallet.ktor.Paths.cpuTemp
import io.raspberrywallet.ktor.Paths.currentAddress
import io.raspberrywallet.ktor.Paths.estimatedBalance
import io.raspberrywallet.ktor.Paths.freshAddress
import io.raspberrywallet.ktor.Paths.moduleHtmlUi
import io.raspberrywallet.ktor.Paths.moduleState
import io.raspberrywallet.ktor.Paths.modules
import io.raspberrywallet.ktor.Paths.nextStep
import io.raspberrywallet.ktor.Paths.ping
import io.raspberrywallet.server.Server
import kotlinx.html.*
import org.slf4j.event.Level

private lateinit var manager: Manager
fun startKtorServer(newManager: Manager) {
    manager = newManager
    embeddedServer(Netty, configure = {
        requestQueueLimit = 6
        runningLimit = 4
    }, port = Server.PORT, module = Application::mainModule).start(wait = true)
}

object Paths {
    val prefix = "/api/"
    val ping = prefix + "ping"
    val modules = prefix + "modules"
    val moduleState = prefix + "moduleState/{id}"
    val nextStep = prefix + "nextStep/{id}"
    val moduleHtmlUi = prefix + "moduleHtmlUi/{id}"
    val currentAddress = prefix + "currentAddress"
    val freshAddress = prefix + "freshAddress"
    val estimatedBalance = prefix + "estimatedBalance"
    val availableBalance = prefix + "availableBalance"
    val cpuTemp = prefix + "cpuTemp"
}

fun Application.mainModule() {

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }
    install(CallLogging) {
        level = Level.INFO
    }
    install(DefaultHeaders)

    routing {
        get(ping) {
            call.respond(mapOf("ping" to manager.ping()))
        }
        get(cpuTemp) {
            call.respond(mapOf("cpuTemp" to manager.cpuTemperature))
        }
        get(modules) {
            call.respond(manager.modules)
        }
        get(moduleState) {
            val id = call.parameters["id"]!!
            val moduleState = manager.getModuleState(id)
            call.respond(mapOf("state" to moduleState.name, "message" to moduleState.message))
        }
        post(nextStep) {
            val id = call.parameters["id"]!!
            val input = call.receiveText()
            val inputMap: Map<String, String> = jacksonObjectMapper().readValue(input, object : TypeReference<Map<String, String>>() {})
            val response = manager.nextStep(id, inputMap)
            call.respond(mapOf("response" to response.status))
        }
        get(moduleHtmlUi) {
            val id = call.parameters["id"]!!
            val htmlUiForm = manager.getModuleUi(id) ?: ""
            call.respondText(text = htmlUiForm, contentType = ContentType.Text.Html)
        }
        get(currentAddress) {
            call.respond(mapOf("currentAddress" to manager.currentReceiveAddress))
        }
        get(freshAddress) {
            call.respond(mapOf("freshAddress" to manager.freshReceiveAddress))
        }
        get(estimatedBalance) {
            call.respond(mapOf("estimatedBalance" to manager.estimatedBalance))
        }
        get(availableBalance) {
            call.respond(mapOf("availableBalance" to manager.availableBalance))
        }
        get("/") {
            call.respond(indexPage)
        }
        static("/") {
            resources("assets")
        }
    }
}

val indexPage = HtmlContent {
    head {
        title { +"Raspberry Wallet" }
    }
    body {
        h1 { a(href = "/index.html") { +"Webapp" } }
        h2 { +"Utils" }
        ul {
            li {
                a(href = Paths.ping) { +Paths.ping }
            }
            li {
                a(href = Paths.cpuTemp) { +Paths.cpuTemp }
            }
        }
        h2 { +"Modules" }
        ul {

            li {
                a(href = modules) { +modules }
            }
            li {
                a(href = moduleState) { +moduleState }
            }
            li {
                a(href = nextStep) { +nextStep }
            }
            li {
                a(href = moduleHtmlUi) { +moduleHtmlUi }
            }
        }
        h2 { +"Bitcoin" }
        ul {

            li {
                a(href = currentAddress) { +currentAddress }
            }
            li {
                a(href = freshAddress) { +freshAddress }
            }
            li {
                a(href = estimatedBalance) { +estimatedBalance }
            }
            li {
                a(href = availableBalance) { +availableBalance }
            }
        }
    }
}
