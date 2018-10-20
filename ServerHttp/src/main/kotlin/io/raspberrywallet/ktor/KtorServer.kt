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
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.request.receiveParameters
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondRedirect
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
import io.raspberrywallet.ktor.Paths.networks
import io.raspberrywallet.ktor.Paths.nextStep
import io.raspberrywallet.ktor.Paths.ping
import io.raspberrywallet.ktor.Paths.setWifi
import io.raspberrywallet.ktor.Paths.wifiStatus
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
    val networks = prefix + "networks"
    val wifiStatus = prefix + "wifiStatus"
    val setWifi = "/setwifi"
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
        get(wifiStatus) {
            call.respond(mapOf("wifiStatus" to manager.wifiStatus))
        }
        get(networks) {
            call.respond(mapOf("networks" to manager.networkList))
        }
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
        post(setWifi) {
            val params = call.receiveParameters()
            val psk = params["psk"]!!
            val ssid = params["ssid"]!!
            if (psk != null && ssid != null) {
                manager.setWifiConfig(mutableMapOf("ssid" to ssid, "psk" to psk))
            }
            call.respondRedirect("/status");
            //call.respond(HttpStatusCode.PermanentRedirect, "<HTML><HEAD><meta http-equiv=\"refresh\" content=\"0; url=/status\" /></HEAD></HTML>");
        }
        get("/") {
            call.respond(indexPage)
        }
        get("/status") {
            call.respond(status)
        }
        get("/setupwifi") {
            call.respond(setNetwork)
        }
        get("/index") {
            call.respond(indexPage)
        }
        static("/") {
            resources("assets")
        }
    }
}

val setNetwork = HtmlContent {
    head {
        title { +"Change Wi-Fi settings" }
        link(rel = "Stylesheet", type = "text/css", href = "/style.css")
    }
    body {
        h1 { a(href = "/index/") { +"<- Back" } }
        h2 { +"New Wi-Fi config" }
        h3 { +"ESSID:" }
        form(method = FormMethod.post, action = Paths.setWifi) {
            select {
                name = "ssid"
                for (network in manager.networkList) {
                    option {
                        value = network
                        +network
                    }
                }
            }
            h3 { +"Pre shared key:" }
            input( type = InputType.password, name = "psk" ) {}
            input( type = InputType.submit ) {}
        }
    }
}

val status = HtmlContent {
    head {
        title { +"System status" }
        link(rel = "Stylesheet", type = "text/css", href = "/style.css")
    }
    body {
        h1 { a(href = "/index/") { +"<- Back" } }
        h2 { +"System status" }
        div( classes = "temperature") {
            +"Temperature: "
            when {
                manager.cpuTemperature.toFloat() > 47 -> span(classes = "hot") { + (manager.cpuTemperature + " 'C") }
                manager.cpuTemperature.toFloat() < 40 -> span(classes = "cold") { +(manager.cpuTemperature + " 'C") }
                else -> span(classes = "medium") { +(manager.cpuTemperature + " 'C") }
            }
        }
        table {
            for ( (param, value) in manager.wifiStatus) {
                tr {
                    td( classes = "param" ) { +param }
                    td { +value }
                }
            }
            for( (param, value) in manager.wifiConfig ) {
                tr {
                    td( classes = "param" ) { +param }
                    td { +value }
                }
            }
        }
    }
}

val indexPage = HtmlContent {
    head {
        title { +"Raspberry Wallet" }
        link(rel = "Stylesheet", type = "text/css", href = "/style.css")
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
