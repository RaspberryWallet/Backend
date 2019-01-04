package io.raspberrywallet.mock

import io.raspberrywallet.contract.Manager
import io.raspberrywallet.contract.Response
import io.raspberrywallet.contract.TransactionView
import io.raspberrywallet.contract.WalletStatus
import io.raspberrywallet.contract.module.Module
import io.raspberrywallet.contract.module.ModuleState
import io.raspberrywallet.contract.step.SimpleStep
import java.io.File
import java.security.SecureRandom
import java.util.function.DoubleConsumer
import java.util.function.IntConsumer
import java.util.stream.Collectors.toMap

class ManagerMock : Manager {
    val random = SecureRandom()

    class SampleModule(name: String, description: String, val htmlUiForm: String? = null) : Module(name, description, null)

    private val _modules = listOf(
        SampleModule("PIN", "Module that require enter 4 digits code", """<input type="text" name="pin">"""),
        SampleModule("Button", "Module that require to push the button"),
        SampleModule("Server", "Module that require to authenticate with external server"),
        SampleModule("Google Authenticator", "Module that require to enter google auth code", """<input type="text" name="code">"""))
        .stream().collect(toMap(SampleModule::getId) { it })!!


    private var _walletStatus = WalletStatus.ENCRYPTED

    override fun getWalletStatus(): WalletStatus {
        return _walletStatus
    }

    override fun lockWallet(): Boolean {
        _walletStatus = WalletStatus.ENCRYPTED
        return true
    }

    override fun nextStep(moduleId: String, input: Map<String, String>): Response =
        Response(SimpleStep("Do something"), Response.Status.OK)

    override fun unlockWallet(moduleToInputsMap: MutableMap<String, out MutableMap<String, String>>) {
        _walletStatus = WalletStatus.DECRYPTED
    }

    override fun restoreFromBackupPhrase(mnemonicWords: MutableList<String>, selectedModulesWithInputs: MutableMap<String, MutableMap<String, String>>, required: Int) {
        val phrase = mnemonicWords.reduce { acc, s -> "$acc $s" }
        println(phrase)
        _walletStatus = WalletStatus.DECRYPTED
    }


    override fun getAllTransactions() = List(5) { newRandomTransaction() }

    private fun newRandomTransaction() = TransactionView(
        Base58.encode(random.generateSeed(32)),
        random.nextLong(),
        listOf(
            Base58.newAddress(random),
            Base58.newAddress(random)),
        listOf(
            Base58.newAddress(random),
            Base58.newAddress(random)),
        random.nextDouble().toString(),
        random.nextDouble().toString(),
        random.nextDouble().toString(),
        random.nextInt(100))


    override fun addBlockChainProgressListener(listener: DoubleConsumer) {
        listener.accept(100.0)
    }

    override fun addAutoLockChannelListener(listener: IntConsumer) {}
    override fun uploadNewModule(inputFile: File?, filename: String) {}
    override fun loadWalletFromDisk(moduleToInputsMap: MutableMap<String, MutableMap<String, String>>) {}
    override fun setDatabasePassword(password: String) {}
    override fun tap() {}
    override fun sendCoins(amount: String, recipientAddress: String) {}
    override fun ping() = "pong"

    override fun getServerModules() = _modules.values.toList()

    override fun getModuleState(moduleId: String): ModuleState {
        val randomModuleState = ModuleState.READY
        randomModuleState.message = "Waiting for user interaction"
        return randomModuleState
    }

    override fun getCurrentReceiveAddress() = Base58.newAddress(random)

    override fun getFreshReceiveAddress() = Base58.newAddress(random)

    override fun getEstimatedBalance() = random.nextDouble().toString()

    override fun getAvailableBalance() = random.nextDouble().toString()

    override fun getCpuTemperature() = "75 °C"

    override fun getNetworkList() = arrayOf("UPCwifi", "other wifi", "klocuch12")

    override fun getWifiStatus() = mutableMapOf("freq" to "21.37 GHz", "speed" to "21.37 Tb/s")

    override fun getWifiConfig() = mutableMapOf("ssid" to "fakenet")

    override fun setWifiConfig(newConf: MutableMap<String, String>?) = 0
}
