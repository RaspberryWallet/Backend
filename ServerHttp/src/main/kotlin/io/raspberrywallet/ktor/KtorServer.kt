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
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.raspberrywallet.Manager
import io.raspberrywallet.ktor.Paths.Bitcoin.availableBalance
import io.raspberrywallet.ktor.Paths.Bitcoin.currentAddress
import io.raspberrywallet.ktor.Paths.Bitcoin.estimatedBalance
import io.raspberrywallet.ktor.Paths.Bitcoin.freshAddress
import io.raspberrywallet.ktor.Paths.Bitcoin.sendCoins
import io.raspberrywallet.ktor.Paths.Modules.lockWallet
import io.raspberrywallet.ktor.Paths.Modules.moduleState
import io.raspberrywallet.ktor.Paths.Modules.modules
import io.raspberrywallet.ktor.Paths.Modules.nextStep
import io.raspberrywallet.ktor.Paths.Modules.restoreFromBackupPhrase
import io.raspberrywallet.ktor.Paths.Modules.unlockWallet
import io.raspberrywallet.ktor.Paths.Modules.walletStatus
import io.raspberrywallet.ktor.Paths.Utils.cpuTemp
import io.raspberrywallet.ktor.Paths.Utils.ping
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
    const val prefix = "/api/"

    object Utils {
        const val ping = prefix + "ping"
        const val cpuTemp = prefix + "cpuTemp"
    }

    object Modules {
        const val modules = prefix + "modules"
        const val moduleState = prefix + "moduleState/{id}"
        const val nextStep = prefix + "nextStep/{id}"
        const val restoreFromBackupPhrase = prefix + "restoreFromBackupPhrase"
        const val unlockWallet = prefix + "unlockWallet"
        const val lockWallet = prefix + "lockWallet"
        const val walletStatus = prefix + "walletStatus"
    }

    object Bitcoin {
        const val currentAddress = prefix + "currentAddress"
        const val freshAddress = prefix + "freshAddress"
        const val estimatedBalance = prefix + "estimatedBalance"
        const val availableBalance = prefix + "availableBalance"
        const val sendCoins = prefix + "sendCoins"
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
        /*Utils*/
        get(ping) {
            manager.tap()
            call.respond(mapOf("ping" to manager.ping()))
        }
        get(cpuTemp) {
            manager.tap()
            call.respond(mapOf("cpuTemp" to manager.cpuTemperature))
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
        get("/") {
            manager.tap()
            call.respond(indexPage)
        }

        static("/") {
            resources("assets")
        }
    }
}

data class RestoreFromBackup(val mnemonicWords: List<String>, val modules: Map<String, Map<String, String>>, val required: Int)
data class SendCoinBody(val amount: String, val recipient: String)

val indexPage = HtmlContent {
    head {
        title { +"Raspberry Wallet" }
    }
    body {
        h1 { a(href = "/index.html") { +"Webapp" } }
        h2 { +"Utils" }
        ul {
            li {
                a(href = ping) { +ping }
            }
            li {
                a(href = cpuTemp) { +cpuTemp }
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
