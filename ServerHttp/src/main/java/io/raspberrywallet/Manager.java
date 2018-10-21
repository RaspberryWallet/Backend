package io.raspberrywallet;

import io.raspberrywallet.module.Module;
import io.raspberrywallet.module.ModuleState;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public interface Manager {
    String ping(); // for DEBUG purposes

    /*
     * Modules domain
     */

    /**
     * @return all available modules
     */
    @NonNls
    List<Module> getModules();

    /**
     * @return state of specified module
     */
    ModuleState getModuleState(@NotNull String moduleId);

    /**
     * @return HTML UI form that require some input from user
     */
    @Nullable
    String getModuleUi(@NotNull String moduleId);

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
    void restoreFromBackupPhrase(@NonNls List<String> mnemonicWords,
                                 @NonNls Map<String, Map<String, String>> selectedModulesWithInputs, int required) throws WalletNotInitialized, RequiredInputNotFound;

    /**
     * @return current wallet status
     */
    WalletStatus getWalletStatus();

    /**
     * unlock/merge decrypted parts
     *
     * @return true if unlocking succeeded
     */
    boolean unlockWallet();

    /**
     * lock wallet remove key from bitcoinJ, fill zeros on modules decryptedValue props
     *
     * @return true if locking succeeded
     */
    boolean lockWallet() throws WalletNotInitialized;


    /*
     * Bitcoin domain
     */

    /**
     * @return current bitcoin receive address (for current derived key) which is base58( hash160( hash160(publicKey))) encoded
     * @see <a href="https://docs.google.com/document/d/1wW5mRy51MvwghFcwk7K07LozbIV1sD53q4ejCQhjzFw#heading=h.pwiwojq2hjnr"/a>)
     * Bitcoin addresses should be used only once in order to keep your total balance private
     */
    @NonNls
    String getCurrentReceiveAddress() throws WalletNotInitialized;

    /**
     * @return fresh new bitcoin receive address (for new derived key) which is base58( hash160( hash160(publicKey))) encoded
     * @see <a href="https://docs.google.com/document/d/1wW5mRy51MvwghFcwk7K07LozbIV1sD53q4ejCQhjzFw#heading=h.pwiwojq2hjnr"/a>)
     * Bitcoin addresses should be used only once in order to keep your total balance private
     */
    @NonNls
    String getFreshReceiveAddress() throws WalletNotInitialized;

    /**
     * Balance calculated assuming all pending transactions are in fact included into the best chain by miners.
     * This includes the value of immature coinbase transactions.
     *
     * @return current balance in BTC unit
     */
    @NonNls
    String getEstimatedBalance() throws WalletNotInitialized;

    /**
     * Balance that could be safely used to create new spends, if we had all the needed private keys. This is
     * whatever the default coin selector would make available, which by default means transaction outputs with at
     * least 1 confirmation and pending transactions created by our own wallet which have been propagated across
     * the network. Whether we <i>actually</i> have the private keys or not is irrelevant for this balance type.
     *
     * @return current balance in BTC unit
     */
    @NonNls
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
    @NonNls
    String getCpuTemperature();


    /**
     * Tap manager, delaying auto lock
     */
    void tap();

}
