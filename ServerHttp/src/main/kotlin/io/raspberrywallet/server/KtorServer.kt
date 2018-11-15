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
import io.ktor.http.content.*
import io.ktor.jackson.jackson
import io.ktor.network.util.ioCoroutineDispatcher
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.request.receiveParameters
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import io.raspberrywallet.contract.*
import io.raspberrywallet.server.Paths.Bitcoin.availableBalance
import io.raspberrywallet.server.Paths.Bitcoin.currentAddress
import io.raspberrywallet.server.Paths.Bitcoin.estimatedBalance
import io.raspberrywallet.server.Paths.Bitcoin.freshAddress
import io.raspberrywallet.server.Paths.Bitcoin.sendCoins
import io.raspberrywallet.server.Paths.Modules.loadWalletFromDisk
import io.raspberrywallet.server.Paths.Modules.lockWallet
import io.raspberrywallet.server.Paths.Modules.moduleInstall
import io.raspberrywallet.server.Paths.Modules.moduleInstallPost
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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.slf4j.event.Level
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.io.OutputStream
import java.lang.Error
import java.security.KeyStore
import java.time.Duration
import java.util.*


const val PORT = 9090
const val PORT_SSL = 443
lateinit var globalManager: Manager

class KtorServer(val manager: Manager,
                 val basePath: String,
                 private val serverConfig: ServerConfig,
                 private val communicationChannel: CommunicationChannel) {

    private val applicationEngine: ApplicationEngine
    private val keyStoreFile: File = File(".", serverConfig.keystoreName) //TODO move keystore into basePath
    private val keyStore: KeyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
        load(FileInputStream(keyStoreFile), serverConfig.keystorePassword)
    }
    private val blockChainSyncProgressionChannel = Channel<Int>(Channel.CONFLATED)

    init {
        globalManager = manager
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
        applicationEngine = embeddedServer(Netty, env)

        manager.addBlockChainProgressListener { progress ->
            blockChainSyncProgressionChannel.sendBlocking(progress)
        }
    }

    fun startBlocking() {
        applicationEngine.start(wait = true)
    }

    fun start() {
        applicationEngine.start(wait = false)
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

            get(moduleInstall) {
                manager.tap()
                call.respond(uploadModuleForm)
            }

            post(moduleInstallPost) {
                manager.tap()
                val mp = call.receiveMultipart()
                mp.forEachPart { part ->
                    when(part) {
                        is PartData.FileItem -> {
                            val dest = File("/tmp/" + ("" + System.currentTimeMillis() + "_" + Random().nextLong() % 99999) + ".jar")
                            part.streamProvider().use { input -> dest.outputStream().buffered().use { output -> input.copyToSuspend(output) } }
                            try {
                                manager.uploadNewModule(dest)
                            } catch (e:Error) {
                                call.respond(HttpStatusCode.NotAcceptable, errorUpload(e.message))
                            }
                        }
                    }
                }
                call.respondRedirect("/", false)
            }

            get(moduleInstallPost) {
                call.respond("this shouldn't ever happen...")
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
                blockChainSyncProgressionChannel.consumeEach { progress ->
                    outgoing.send(Frame.Text("$progress"))
                    if (progress == 100) close()
                }
            }
            webSocket("/info") {
                communicationChannel.channel
                    .filter { it is Message.InfoMessage }
                    .consumeEach { infoMessage ->
                        outgoing.send(Frame.Text(infoMessage.message))
                    }
            }
            webSocket("/error") {
                communicationChannel.channel
                    .filter { it is Message.ErrorMessage }
                    .consumeEach { errorMessage ->
                        outgoing.send(Frame.Text(errorMessage.message))
                    }
            }
            webSocket("/success") {
                communicationChannel.channel
                    .filter { it is Message.SuccessMessage }
                    .consumeEach { successMessage ->
                        outgoing.send(Frame.Text(successMessage.message))
                    }
            }
        }
    }

    data class RestoreFromBackup(val mnemonicWords: List<String>, val modules: Map<String, Map<String, String>>, val required: Int)
    data class SendCoinBody(val amount: String, val recipient: String)
    data class SetDatabasePassword(val password: String)
}

suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = ioCoroutineDispatcher
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}
