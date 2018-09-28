package io.raspberrywallet

import io.raspberrywallet.module.Module
import io.raspberrywallet.module.ModuleState
import io.raspberrywallet.step.SimpleStep
import java.util.*

class ManagerMockup : Manager {

    class SampleModule(name: String, description: String) : Module(name, description)

    private val modulesList = listOf(SampleModule("PIN", "Module that require enter 4 digits code"),
        SampleModule("Button", "Module that require to push the button"),
        SampleModule("Server", "Module that require to authenticate with external server"),
        SampleModule("Google Authenticator", "Module that require to enter google auth code"))

    private val rand = Random()
    private val currentReceiveAddress = "1BoatSLRHtKNngkdXEeobR76b53LETtpyT"
    private val freshReceiveAddress = "3E53XjqK4Cxt71BGeP2VhpcotM8LZ853C8"
    private val estimatedBalance = "0.0"
    private val currentBalance = "0.0"

    override fun restoreFromBackupPhrase(mnemonicWords: MutableList<String>) {
        val phrase = mnemonicWords.reduce { acc, s -> acc + s }
        //TODO restore privatekey from backup phrase and store it safely
        println(phrase)
    }

    override fun getModules(): List<Module> = modulesList

    override fun getCurrentReceiveAddress(): String {
        return currentReceiveAddress
    }

    override fun getFreshReceiveAddress(): String {
        return freshReceiveAddress
    }

    override fun getEstimatedBalance(): String {
        return estimatedBalance
    }

    override fun getAvailableBalance(): String {
        return currentBalance
    }

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

    override fun ping() = "pong"
}
