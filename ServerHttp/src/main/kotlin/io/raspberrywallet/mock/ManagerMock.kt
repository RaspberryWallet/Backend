package io.raspberrywallet.mock

import io.raspberrywallet.Manager
import io.raspberrywallet.Response
import io.raspberrywallet.module.Module
import io.raspberrywallet.module.ModuleState
import io.raspberrywallet.step.SimpleStep
import java.util.stream.Collectors.toMap

class ManagerMock : Manager {


    class SampleModule(name: String, description: String, val htmlUiForm: String? = null) : Module(name, description)

    private val _modules = listOf(
        SampleModule("PIN", "Module that require enter 4 digits code", """<input type="text" name="pin">"""),
        SampleModule("Button", "Module that require to push the button"),
        SampleModule("Server", "Module that require to authenticate with external server"),
        SampleModule("Google Authenticator", "Module that require to enter google auth code", """<input type="text" name="code">"""))
        .stream().collect(toMap(SampleModule::getId) { it })!!


    override fun ping() = "pong"

    override fun getModules() = _modules.values.toList()

    override fun getModuleState(moduleId: String): ModuleState {
        val randomModuleState = ModuleState.READY
        randomModuleState.message = "Waiting for user interaction"
        return randomModuleState
    }

    override fun nextStep(moduleId: String, input: Map<String, String>): Response =
        Response(SimpleStep("Do something"), Response.Status.OK)

    override fun unlockWallet() = true

    override fun getModuleUi(moduleId: String): String? = _modules[moduleId]?.htmlUiForm

    override fun getCurrentReceiveAddress() = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT"

    override fun getFreshReceiveAddress() = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT"

    override fun getEstimatedBalance() = "0.0"

    override fun getAvailableBalance() = "0.0"

    override fun restoreFromBackupPhrase(mnemonicWords: MutableList<String>) {
        val phrase = mnemonicWords.reduce { acc, s -> acc + s }
        println(phrase)
    }

    override fun getCpuTemperature() = "75 °C"

}
