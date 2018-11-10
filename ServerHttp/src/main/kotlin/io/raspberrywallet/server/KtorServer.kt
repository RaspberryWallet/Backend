package io.raspberrywallet.server

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
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
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.engine.sslConnector
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import io.raspberrywallet.contract.Manager
import io.raspberrywallet.contract.ServerConfig
import io.raspberrywallet.contract.WalletNotInitialized
import io.raspberrywallet.server.Paths.Bitcoin.availableBalance
import io.raspberrywallet.server.Paths.Bitcoin.currentAddress
import io.raspberrywallet.server.Paths.Bitcoin.estimatedBalance
import io.raspberrywallet.server.Paths.Bitcoin.freshAddress
import io.raspberrywallet.server.Paths.Bitcoin.sendCoins
import io.raspberrywallet.server.Paths.Modules.loadWalletFromDisk
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
import io.raspberrywallet.server.Paths.Utils.setDatabasePassword
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.slf4j.event.Level
import java.io.File
import java.io.FileInputStream
import java.security.KeyStore
import java.time.Duration


const val PORT = 9090
const val PORT_SSL = 443
lateinit var manager: Manager
lateinit var serverConfig: ServerConfig
lateinit var basePath: String

val keyStoreFile: File by lazy {
    File(".", serverConfig.keystoreName)
}
val keyStore: KeyStore by lazy {
    KeyStore.getInstance(KeyStore.getDefaultType())
        .apply { load(FileInputStream(keyStoreFile), serverConfig.keystorePassword) }
}

fun startKtorServer(newManager: Manager, newBasePath: String, config: ServerConfig) {
    manager = newManager
    basePath = newBasePath
    serverConfig = config

    val env = applicationEngineEnvironment {
        module {
            mainModule()
        }
        connector {
            port = PORT
        }
        sslConnector(keyStore, "ssl", { serverConfig.keystorePassword }, { serverConfig.keystorePassword }) {
            port = PORT_SSL
            keyStorePath = keyStoreFile.absoluteFile
        }
    }
    embeddedServer(Netty, env).start(wait = true)
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
    install(CORS) {
        anyHost()
        allowCredentials = true
    }
    install(DefaultHeaders)
    install(StatusPages) {
        exception<WalletNotInitialized> {
            call.respond(HttpStatusCode.MethodNotAllowed, mapOf("message" to "Wallet not initialized"))
        }
        exception<SecurityException> { cause ->
            call.respond(HttpStatusCode.Forbidden, mapOf("message" to cause))
        }
    }
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(60) // Disabled (null) by default
        timeout = Duration.ofSeconds(15)
        maxFrameSize = kotlin.Long.MAX_VALUE // Disabled (max value). The connection will be closed if surpassed this length.
        masking = false
    }

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
            call.respond(manager.serverModules)
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
        post(unlockWallet) {
            manager.tap()
            val moduleToInputsMap = call.receive<Map<String, Map<String, String>>>()
            call.respond(manager.unlockWallet(moduleToInputsMap))
        }
        post(loadWalletFromDisk) {
            manager.tap()
            val moduleToInputsMap = call.receive<Map<String, Map<String, String>>>()
            call.respond(manager.loadWalletFromDisk(moduleToInputsMap))
        }
        post(setDatabasePassword) {
            manager.tap()
            val setDatabasePassword = call.receive<SetDatabasePassword>()
            call.respond(manager.setDatabasePassword(setDatabasePassword.password))
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

        webSocket("/pingCounter") {
            var counter = 0L
            while (true) {
                delay(1000)
                outgoing.send(Frame.Text("$counter"))
                counter++
            }
        }
        webSocket("/blockChainSyncProgress") {
            manager.addBlockChainProgressListener { progress ->
                launch {
                    outgoing.send(Frame.Text("$progress"))
                }
            }

            while (isActive) delay(1000)
        }
    }
}

data class RestoreFromBackup(val mnemonicWords: List<String>, val modules: Map<String, Map<String, String>>, val required: Int)
data class SendCoinBody(val amount: String, val recipient: String)
data class SetDatabasePassword(val password: String)
