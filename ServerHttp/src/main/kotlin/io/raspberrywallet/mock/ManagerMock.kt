package io.raspberrywallet.mock

import io.raspberrywallet.Manager
import io.raspberrywallet.Response
import io.raspberrywallet.module.Module
import io.raspberrywallet.module.ModuleState
import io.raspberrywallet.step.SimpleStep
import java.security.SecureRandom

class ManagerMock : Manager {
    private val rand = SecureRandom.getInstanceStrong()

    override fun ping() = "pong"

    class SampleModule(name: String, description: String) : Module(name, description)

    override fun getModules() = listOf(
        SampleModule("PIN", "Module that require enter 4 digits code"),
        SampleModule("Button", "Module that require to push the button"),
        SampleModule("Server", "Module that require to authenticate with external server"),
        SampleModule("Google Authenticator", "Module that require to enter google auth code"))

    override fun getCurrentReceiveAddress() = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT"

    override fun getFreshReceiveAddress() = Base58.encode(rand.generateSeed(20))

    override fun getEstimatedBalance() = "0.0"

    override fun getAvailableBalance() = "0.0"

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

    override fun nextStep(moduleId: String, input: ByteArray?): Response =
        if (rand.nextBoolean())
            Response(SimpleStep("Do something"), Response.Status.OK)
        else
            Response(null, Response.Status.FAILED)

    override fun restoreFromBackupPhrase(mnemonicWords: MutableList<String>) {
        val phrase = mnemonicWords.reduce { acc, s -> acc + s }
        println(phrase)
    }

    override fun getCpuTemperature() = "75 °C"

}
