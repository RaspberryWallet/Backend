package io.raspberrywallet;

import io.raspberrywallet.module.Module;
import io.raspberrywallet.module.ModuleState;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface Manager {
    String ping(); // for DEBUG purposes

    /*
     * Modules domain
     */

    /**
     * @return all available modules
     */
    List<Module> getModules();

    /**
     * @return state of specified module
     */
    ModuleState getModuleState(@NotNull String moduleId);

    /**
     * Validate input for specified module, and receive next Step or Failure
     *
     * @param moduleId id of corresponding nodule
     * @param input    input for current Step
     * @return Response with next Step or null if failed
     */
    Response nextStep(@NotNull String moduleId, byte[] input); // pass input for current step and return next step




    /*
     * Bitcoin domain
     */

    /**
     * @return current bitcoin receive address (for current derived key) which is base58( hash160( hash160(publicKey))) encoded
     * @see <a href="https://docs.google.com/document/d/1wW5mRy51MvwghFcwk7K07LozbIV1sD53q4ejCQhjzFw#heading=h.pwiwojq2hjnr"/a>)
     * Bitcoin addresses should be used only once in order to keep your total balance private
     */
    @NonNls
    String getCurrentReceiveAddress();

    /**
     * @return fresh new bitcoin receive address (for new derived key) which is base58( hash160( hash160(publicKey))) encoded
     * @see <a href="https://docs.google.com/document/d/1wW5mRy51MvwghFcwk7K07LozbIV1sD53q4ejCQhjzFw#heading=h.pwiwojq2hjnr"/a>)
     * Bitcoin addresses should be used only once in order to keep your total balance private
     */
    @NonNls
    String getFreshReceiveAddress();

    /**
     * Balance calculated assuming all pending transactions are in fact included into the best chain by miners.
     * This includes the value of immature coinbase transactions.
     *
     * @return current balance in BTC unit
     */
    @NonNls
    String getEstimatedBalance();

    /**
     * Balance that could be safely used to create new spends, if we had all the needed private keys. This is
     * whatever the default coin selector would make available, which by default means transaction outputs with at
     * least 1 confirmation and pending transactions created by our own wallet which have been propagated across
     * the network. Whether we <i>actually</i> have the private keys or not is irrelevant for this balance type.
     *
     * @return current balance in BTC unit
     */
    @NonNls
    String getAvailableBalance();

    /**
     * Restores seed/privateKey from backup phrase (12 mnemonic words)
     *
     * @param mnemonicWords 12 words corresponding to private key
     */
    void restoreFromBackupPhrase(@NotNull List<String> mnemonicWords);

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

}
