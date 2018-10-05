package io.raspberrywallet.mock

import io.raspberrywallet.Manager
import io.raspberrywallet.Response
import io.raspberrywallet.module.Module
import io.raspberrywallet.module.ModuleState
import io.raspberrywallet.step.SimpleStep
import java.security.SecureRandom
import java.util.stream.Collectors.toMap

class ManagerMock : Manager {

    class SampleModule(name: String, description: String, val htmlUiForm: String? = null) : Module(name, description)

    private val _modules = listOf(
        SampleModule("PIN", "Module that require enter 4 digits code", """<input type="text" name="pin">"""),
        SampleModule("Button", "Module that require to push the button"),
        SampleModule("Server", "Module that require to authenticate with external server"),
        SampleModule("Google Authenticator", "Module that require to enter google auth code", """<input type="text" name="code">"""))
        .stream().collect(toMap(SampleModule::getId) { it })!!


    private val rand = SecureRandom.getInstanceStrong()

    override fun ping() = "pong"

    override fun getModules() = _modules.values.toList()

    override fun getModuleState(moduleId: String): ModuleState {
        val randomIndex = rand.nextInt(ModuleState.values().size)
        val randomModuleState = ModuleState.values()[randomIndex]
        when (randomModuleState) {
            ModuleState.READY -> randomModuleState.message = null
            ModuleState.WAITING -> randomModuleState.message = "Waiting for user interaction"
            ModuleState.AUTHORIZED -> randomModuleState.message = null
            ModuleState.FAILED -> randomModuleState.message = "Connection failed"
        }
        return randomModuleState
    }

    override fun nextStep(moduleId: String, input: Map<String, String>): Response =
        if (rand.nextBoolean())
            Response(SimpleStep("Do something"), Response.Status.OK)
        else
            Response(null, Response.Status.FAILED)

    override fun getModuleUi(moduleId: String): String? = _modules[moduleId]?.htmlUiForm

    override fun getCurrentReceiveAddress() = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT"

    override fun getFreshReceiveAddress() = Base58.encode(rand.generateSeed(20))

    override fun getEstimatedBalance() = "0.0"

    override fun getAvailableBalance() = "0.0"

    override fun restoreFromBackupPhrase(mnemonicWords: MutableList<String>) {
        val phrase = mnemonicWords.reduce { acc, s -> acc + s }
        println(phrase)
    }

    override fun getCpuTemperature() = "75 °C"

}
