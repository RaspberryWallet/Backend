package io.raspberrywallet.server

sealed class Paths {
    companion object {
        const val prefix = "/api/"
    }

    object Utils : Paths() {
        const val ping = prefix + "ping"
        const val allTransactions = prefix + "allTransactions"
        const val cpuTemp = prefix + "cpuTemp"
        const val setDatabasePassword = prefix + "setDatabasePassword";
    }

    object Modules : Paths() {
        const val modules = prefix + "modules"
        const val moduleState = prefix + "moduleState/{id}"
        const val nextStep = prefix + "nextStep/{id}"
        const val restoreFromBackupPhrase = prefix + "restoreFromBackupPhrase"
        const val unlockWallet = prefix + "unlockWallet"
        const val lockWallet = prefix + "lockWallet"
        const val loadWalletFromDisk = prefix + "loadWalletFromDisk"
        const val walletStatus = prefix + "walletStatus"
        const val moduleInstall = "/installModule"
        const val moduleInstallPost = prefix + "installModuleUpload"
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
