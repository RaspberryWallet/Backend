package io.raspberrywallet.manager.modules;

import io.raspberrywallet.contract.InternalModuleException;
import io.raspberrywallet.contract.RequiredInputNotFound;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface IModule {
    /**
     * Used in all sort of identifications like config.yaml, internal module mapping and UI naming.
     * For now, it's just simplified SimpleClassName
     *
     * @return module identifier.
     */
    @NotNull
    String getId();


    /**
     * @return Server-side module representation
     */
    @NotNull
    io.raspberrywallet.contract.module.Module asServerModule();

    /*
     * (De/En)cryption
     */

    /**
     * this wrapper enforce module to validateInputs and throw exception if they are absent
     *
     * @param keyPart - unencrypted key part
     * @return encrypted payload
     */
    @NotNull
    byte[] encryptKeyPart(@NotNull byte[] keyPart) throws EncryptionException, RequiredInputNotFound, InternalModuleException;

    /**
     * this wrapper enforce module to validateInputs and throw exception if they are absent
     *
     * @param payload - encrypted payload
     * @return decrypted key part
     */
    @NotNull
    byte[] decryptKeyPart(@NotNull byte[] payload) throws DecryptionException, RequiredInputNotFound, InternalModuleException;

    /*
     * View State
     */
    @NotNull
    String getDescription();

    /**
     * this function should return HTML UI form or null if not required
     */
    @Nullable
    String getHtmlUi();

    /**
     * Returns status of the module to show to the user
     *
     * @return message
     */
    @NotNull
    String getStatusString();

    /**
     * Setting the status message for the user
     *
     * @param status - new status
     */
    void setStatusString(@NotNull String status);

    /*
      Inputs CRUD
     */

    /**
     * Gets the value which user has submitted
     *
     * @param key - parameter key
     * @return - value of the parameter
     */
    @Nullable
    String getInput(String key);

    /**
     * Sets input for this Module from user
     *
     * @param key   - key of the parameter
     * @param value - value of the parameter
     */
    void setInput(@NotNull String key, @NotNull String value);

    /**
     * Sets inputs for this Module from user
     */
    void setInputs(@NotNull Map<String, String> inputs);

    /**
     * Checks if user has submitted any input
     *
     * @param key - key of the parameter
     * @return - if key exists
     */
    boolean hasInput(@NotNull String key);

    /**
     * Clear the user inputs, prepare for new
     */
    void clearInputs();
}
