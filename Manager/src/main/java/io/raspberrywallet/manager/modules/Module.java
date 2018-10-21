package io.raspberrywallet.manager.modules;

import io.raspberrywallet.RequiredInputNotFound;
import io.raspberrywallet.manager.modules.exceptions.KeypartDecryptionException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public abstract class Module {
    @NonNls
    private String statusString;

    @NonNls
    private HashMap<String, String> input = new HashMap<>();

    public String getId() {
        return this.getClass().getName();
    }

    public io.raspberrywallet.module.Module asServerModule() {
        return new io.raspberrywallet.module.Module(getId(), getId(), getDescription(), getHtmlUi()) {
        };
    }

    public abstract String getDescription();

    /**
     * Check if needed interaction (User-Module) has been completed
     *
     * @return true, if we are ready to decrypt
     */
    public abstract boolean check();

    /**
     * @param keyPart - unencrypted key part
     * @return encrypted payload
     */
    public abstract byte[] encrypt(byte[] keyPart) throws RequiredInputNotFound;

    /**
     * @param payload - encrypted payload
     * @return decrypted key part
     */
    public abstract byte[] decrypt(byte[] payload) throws KeypartDecryptionException, RequiredInputNotFound;

    /**
     * this function should prepare module before consecutive use.
     * Manager should call this.
     */
    public abstract void register();

    /**
     * this function should return HTML UI form or null if not required
     */
    @Nullable
    public abstract String getHtmlUi();

    /**
     * Returns status of the module to show to the user
     *
     * @return message
     */
    public String getStatusString() {
        return statusString == null ? "null" : statusString;
    }

    /**
     * Setting the status message for the user
     *
     * @param status - new status
     */
    void setStatusString(@NonNls String status) {
        this.statusString = status;
    }

    /**
     * Sets input for this Module from user
     *
     * @param key   - key of the parameter
     * @param value - value of the parameter
     */
    void setInput(String key, String value) {
        input.put(key, value);
    }

    /**
     * Sets inputs for this Module from user
     */
    public void setInputs(Map<String, String> inputs) {
        input.putAll(inputs);
    }

    /**
     * Checks if user has submitted any input
     *
     * @param key - key of the parameter
     * @return - if key exists
     */
    boolean hasInput(String key) {
        return input.containsKey(key);
    }

    /**
     * Gets the value which user has submitted
     *
     * @param key - parameter key
     * @return - value of the parameter
     */
    @Nullable
    String getInput(String key) {
        return input.get(key);
    }

    /**
     * Clear the user inputs, prepare for new
     */
    public void clearInputs() {
        input.clear();
    }


    /**
     * Module info formatted as JSON.
     */
    @Override
    public String toString() {
        return "{\"id\":\"" + getId() + "\", \"status\":\"" + getStatusString() + "\"}";
    }
}