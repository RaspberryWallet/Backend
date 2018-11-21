package io.raspberrywallet.contract;


import io.raspberrywallet.contract.module.Module;
import io.raspberrywallet.contract.module.ModuleState;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

public interface Manager {
    String ping(); // for DEBUG purposes

    /*
     * Modules domain
     */

    /**
     * @return all available modules
     */
    @NotNull
    List<Module> getServerModules();

    /**
     * @return state of specified module
     */
    ModuleState getModuleState(@NotNull String moduleId);

    /**
     * Validate input for specified module, and receive next Step or Failure
     *
     * @param moduleId id of corresponding nodule
     * @param inputMap inputs for current Step
     * @return Response with next Step or null if failed
     */
    Response nextStep(@NotNull String moduleId, Map<String, String> inputMap); // pass input for current step and return next step

    /**
     * Restores seed/privateKey from backup phrase (12 mnemonic words)
     *
     * @param mnemonicWords             12 words corresponding to private key
     * @param selectedModulesWithInputs modules selected to encrypt this private key moduleId -> Map(inputName -> inputValue)
     * @param required                  number of modules required to unlock the wallet <= moduleIdsToDecrypt.size
     */
    void restoreFromBackupPhrase(@NotNull List<String> mnemonicWords,
                                 @NotNull Map<String, Map<String, String>> selectedModulesWithInputs, int required) throws WalletNotInitialized, RequiredInputNotFound;

    /**
     * @return current wallet status
     */
    WalletStatus getWalletStatus();

    /**
     * unlock/merge decrypted parts
     *
     * @param moduleToInputsMap map of moduleId => [inputName => inputValue]
     */
    void unlockWallet(Map<String, Map<String, String>> moduleToInputsMap) throws WalletNotInitialized, IncorrectPasswordException;

    /**
     * Decrypt and load wallet from disk
     *
     * @param moduleToInputsMap map of moduleId => [inputName => inputValue]
     */
    void loadWalletFromDisk(@NotNull Map<String, Map<String, String>> moduleToInputsMap);
    /**
     * lock wallet remove key from bitcoinJ, fill zeros on modules decryptedValue props
     *
     * @return true if locking succeeded
     */
    boolean lockWallet() throws WalletNotInitialized, IncorrectPasswordException, IOException;


    /*
     * Bitcoin domain
     */

    /**
     * @return current bitcoin receive address (for current derived key) which is base58( hash160( hash160(publicKey))) encoded
     * @see <a href="https://docs.google.com/document/d/1wW5mRy51MvwghFcwk7K07LozbIV1sD53q4ejCQhjzFw#heading=h.pwiwojq2hjnr"/a>)
     * Bitcoin addresses should be used only once in order to keep your total balance private
     */
    @NotNull
    String getCurrentReceiveAddress() throws WalletNotInitialized;

    /**
     * @return fresh new bitcoin receive address (for new derived key) which is base58( hash160( hash160(publicKey))) encoded
     * @see <a href="https://docs.google.com/document/d/1wW5mRy51MvwghFcwk7K07LozbIV1sD53q4ejCQhjzFw#heading=h.pwiwojq2hjnr"/a>)
     * Bitcoin addresses should be used only once in order to keep your total balance private
     */
    @NotNull
    String getFreshReceiveAddress() throws WalletNotInitialized;

    /**
     * Balance calculated assuming all pending transactions are in fact included into the best chain by miners.
     * This includes the value of immature coinbase transactions.
     *
     * @return current balance in BTC unit
     */
    @NotNull
    String getEstimatedBalance() throws WalletNotInitialized;

    /**
     * Balance that could be safely used to create new spends, if we had all the needed private keys. This is
     * whatever the default coin selector would make available, which by default means transaction outputs with at
     * least 1 confirmation and pending transactions created by our own wallet which have been propagated across
     * the network. Whether we <i>actually</i> have the private keys or not is irrelevant for this balance type.
     *
     * @return current balance in BTC unit
     */
    @NotNull
    String getAvailableBalance() throws WalletNotInitialized;


    /**
     * Send amount of bitcoins to the recipientAddress
     *
     * @param amount           of bitcoins to send to the recipientAddress
     * @param recipientAddress recipient address
     */
    void sendCoins(@NotNull String amount, @NotNull String recipientAddress) throws WalletNotInitialized;

    /*
     * Utilities
     */

    /**
     * Gets temperature of the CPU
     *
     * @return temperature as string in Celsius
     */
    @NotNull
    String getCpuTemperature();


    /**
     * Tap manager, delaying auto lock
     */
    void tap();

    /**
     * Sets database password
     */
    void setDatabasePassword(@NotNull String password) throws Exception;
    /*
     * Network
     */

    /**
     * Lists nearby wireless networks
     *
     * @return String array of found networks
     */
    @NotNull
    String[] getNetworkList();

    /**
     * Get current status of Wi-Fi
     *
     * @return map with status parameters, described in io.raspberrywallet.manager.linux.WifiStatus::call
     */
    @NotNull
    Map<String, String> getWifiStatus();

    /**
     * Gets current config of Wi-Fi: saved SSID and encrypted PSK
     *
     * @return Map with configuration parameters
     */
    @NotNull
    Map<String, String> getWifiConfig();

    @NotNull
    int setWifiConfig(Map<String, String> newConf);

    void addBlockChainProgressListener(@NotNull IntConsumer listener);

    void uploadNewModule(File inputFile, String fileName) throws ModuleUploadException;

    void addAutoLockChannelListener(@NotNull IntConsumer listener);
}
