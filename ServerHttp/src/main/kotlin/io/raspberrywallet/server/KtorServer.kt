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
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.raspberrywallet.Manager
import io.raspberrywallet.server.Paths.Bitcoin.availableBalance
import io.raspberrywallet.server.Paths.Bitcoin.currentAddress
import io.raspberrywallet.server.Paths.Bitcoin.estimatedBalance
import io.raspberrywallet.server.Paths.Bitcoin.freshAddress
import io.raspberrywallet.server.Paths.Bitcoin.sendCoins
import io.raspberrywallet.server.Paths.Modules.lockWallet
import io.raspberrywallet.server.Paths.Modules.moduleState
import io.raspberrywallet.server.Paths.Modules.modules
import io.raspberrywallet.server.Paths.Modules.nextStep
import io.raspberrywallet.server.Paths.Modules.restoreFromBackupPhrase
import io.raspberrywallet.server.Paths.Modules.unlockWallet
import io.raspberrywallet.server.Paths.Modules.walletStatus
import io.raspberrywallet.server.Paths.Network.networks
import io.raspberrywallet.server.Paths.Network.setWifi
import io.raspberrywallet.server.Paths.Network.setupWiFi
import io.raspberrywallet.server.Paths.Network.statusEndpoint
import io.raspberrywallet.server.Paths.Network.wifiStatus
import io.raspberrywallet.server.Paths.Utils.cpuTemp
import io.raspberrywallet.server.Paths.Utils.ping
import kotlinx.html.*
import org.slf4j.event.Level

const val PORT = 9090
private lateinit var manager: Manager
fun startKtorServer(newManager: Manager) {
    manager = newManager
    embeddedServer(Netty, configure = {
        requestQueueLimit = 6
        runningLimit = 4
    }, port = PORT, module = Application::mainModule).start(wait = true)
}

sealed class Paths {
    companion object {
        const val prefix = "/api/"
    }

    object Utils : Paths() {
        const val ping = prefix + "ping"
        const val cpuTemp = prefix + "cpuTemp"
    }

    object Modules : Paths() {
        const val modules = prefix + "modules"
        const val moduleState = prefix + "moduleState/{id}"
        const val nextStep = prefix + "nextStep/{id}"
        const val restoreFromBackupPhrase = prefix + "restoreFromBackupPhrase"
        const val unlockWallet = prefix + "unlockWallet"
        const val lockWallet = prefix + "lockWallet"
        const val walletStatus = prefix + "walletStatus"
    }

    object Bitcoin : Paths() {
        const val currentAddress = prefix + "currentAddress"
        const val freshAddress = prefix + "freshAddress"
        const val estimatedBalance = prefix + "estimatedBalance"
        const val availableBalance = prefix + "availableBalance"
        const val sendCoins = prefix + "sendCoins"
    }

    object Network : Paths() {
        const val cpuTemp = prefix + "cpuTemp"
        const val networks = prefix + "networks"
        const val wifiStatus = prefix + "wifiStatus"
        const val setupWiFi = "/setupWiFi"
        const val setWifi = "/setWiFi"
        const val statusEndpoint = "/status"
    }
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
        /* Index */
        get(statusEndpoint) {
            manager.tap()
            call.respond(status)
        }
        get("/") {
            manager.tap()
            call.respond(indexPage)
        }
        get("/index") {
            manager.tap()
            call.respond(indexPage)
        }
        static("/") {
            resources("assets")
        }

        /*Utils*/
        get(ping) {
            manager.tap()
            call.respond(mapOf("ping" to manager.ping()))
        }
        get(cpuTemp) {
            manager.tap()
            call.respond(mapOf("cpuTemp" to manager.cpuTemperature))
        }

        /* Network */
        get(wifiStatus) {
            call.respond(mapOf("wifiStatus" to manager.wifiStatus))
        }
        get(networks) {
            call.respond(mapOf("networks" to manager.networkList))
        }
        post(setWifi) {
            val params = call.receiveParameters()
            val psk = params["psk"]!!
            val ssid = params["ssid"]!!

            manager.wifiConfig = mutableMapOf("ssid" to ssid, "psk" to psk)

            call.respondRedirect(statusEndpoint)
        }
        get(setupWiFi) {
            call.respond(setNetwork)
        }

        /*Modules*/
        get(modules) {
            manager.tap()
            call.respond(manager.modules)
        }
        get(moduleState) {
            manager.tap()
            val id = call.parameters["id"]!!
            val moduleState = manager.getModuleState(id)
            call.respond(mapOf("state" to moduleState.name, "message" to moduleState.message))
        }
        post(nextStep) {
            manager.tap()
            val id = call.parameters["id"]!!
            val input = call.receiveText()
            val inputMap: Map<String, String> = jacksonObjectMapper().readValue(input, object : TypeReference<Map<String, String>>() {})
            val response = manager.nextStep(id, inputMap)
            call.respond(mapOf("response" to response.status))
        }
        post(restoreFromBackupPhrase) {
            manager.tap()
            val (mnemonicWords, modules, required) = call.receive<RestoreFromBackup>()
            call.respond(manager.restoreFromBackupPhrase(mnemonicWords, modules, required))
        }
        get(walletStatus) {
            manager.tap()
            call.respond(mapOf("walletStatus" to manager.walletStatus))
        }
        get(unlockWallet) {
            manager.tap()
            call.respond(manager.unlockWallet())
        }
        get(lockWallet) {
            call.respond(manager.lockWallet())
        }

        /*Bitcoin*/
        post(sendCoins) {
            manager.tap()
            val (amount, recipient) = call.receive<SendCoinBody>()
            manager.sendCoins(amount, recipient)
            call.respond(HttpStatusCode.OK)
        }
        get(currentAddress) {
            manager.tap()
            call.respond(mapOf("currentAddress" to manager.currentReceiveAddress))
        }
        get(freshAddress) {
            manager.tap()
            call.respond(mapOf("freshAddress" to manager.freshReceiveAddress))
        }
        get(estimatedBalance) {
            manager.tap()
            call.respond(mapOf("estimatedBalance" to manager.estimatedBalance))
        }
        get(availableBalance) {
            manager.tap()
            call.respond(mapOf("availableBalance" to manager.availableBalance))
        }
    }
}

data class RestoreFromBackup(val mnemonicWords: List<String>, val modules: Map<String, Map<String, String>>, val required: Int)
data class SendCoinBody(val amount: String, val recipient: String)

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
                a(href = ping) { +Paths.Utils.ping }
            }
            li {
                a(href = cpuTemp) { +cpuTemp }
            }
        }
        h2 { +"Network" }
        ul {
            li {
                a(href = wifiStatus) { +wifiStatus }
            }
            li {
                a(href = networks) { +networks }
            }
            li {
                a(href = setWifi) { +setWifi }
            }
            li {
                a(href = setupWiFi) { +setupWiFi }
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
                a(href = restoreFromBackupPhrase) { +restoreFromBackupPhrase }
            }
            li {
                a(href = walletStatus) { +walletStatus }
            }
            li {
                a(href = unlockWallet) { +unlockWallet }
            }
            li {
                a(href = lockWallet) { +lockWallet }
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

val setNetwork = HtmlContent {
    head {
        title { +"Change Wi-Fi settings" }
        link(rel = "Stylesheet", type = "text/css", href = "/style.css")
        script { src="/scripts.js"; type = "text/javascript" }
        script { src="/jquery.min.js"; type = "text/javascript" }
    }
    body {
        h1 { a(href = "/index/") { +"<- Back" } }
        h2 { +"New Wi-Fi config" }
        h3 { +"ESSID:" }
        form(method = FormMethod.post, action = setWifi) {
            select {
                id="ssid"
                name = "ssid"
                for (network in manager.networkList) {
                    option {
                        value = network
                        +network
                    }
                }
            }
            span {
                onClick = "refreshNetworks()"
                style = "cursor: pointer, link, hand"
                + "Refresh"
            }
            h3 { +"Pre shared key:" }
            input(type = InputType.password, name = "psk") {}
            input(type = InputType.submit) {}
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
        div(classes = "temperature") {
            +"Temperature: "
            when {
                manager.cpuTemperature.toFloat() > 47 -> span(classes = "hot") { +(manager.cpuTemperature + " 'C") }
                manager.cpuTemperature.toFloat() < 40 -> span(classes = "cold") { +(manager.cpuTemperature + " 'C") }
                else -> span(classes = "medium") { +(manager.cpuTemperature + " 'C") }
            }
        }
        a(href = setupWiFi) {
            +"Configure Wi-Fi"
        }
        table {
            for ((param, value) in manager.wifiStatus) {
                tr {
                    td(classes = "param") { +param }
                    td { +value }
                }
            }
            for ((param, value) in manager.wifiConfig) {
                tr {
                    td(classes = "param") { +param }
                    td { +value }
                }
            }
        }
    }
}
