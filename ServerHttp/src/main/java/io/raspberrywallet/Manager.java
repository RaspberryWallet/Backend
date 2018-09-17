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
     * @return public address which is base58(publicKey)
     */
    @NonNls
    byte[] getAddress();

    /**
     * Restores private key from backup phrase (12 mnemonic words)
     *
     * @param mnemonicWords 12 words corresponding to private key
     */
    void restoreFromBackupPhrase(@NotNull List<String> mnemonicWords);


}
