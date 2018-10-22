package io.raspberrywallet.manager.modules;

import io.raspberrywallet.manager.common.ArrayDestroyer;
import io.raspberrywallet.manager.common.interfaces.Destroyable;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public abstract class Module implements Destroyable {
    
    private static final int STATUS_OK = 200;
    private static final int STATUS_TIMEOUT = 432;
    private static final int STATUS_WAITING = 100;
    
    private byte[] payload;
    private int status = STATUS_WAITING;
    
    @Setter
    @Getter
    private String statusString = "null";
    
    private byte[] decryptedValue;
    private HashMap<String, String> input = new HashMap<>();
    
    /**
     * Returns module info.
     * @return Info formatted as JSONObject casted to string.
     */

    @Override
    public String toString() {
        JSONObject idAndStatus = new JSONObject();
        idAndStatus.put("id", getId()).put("status", getStatusString());
        return idAndStatus.toString();
        
    }

    public abstract String getDescription();

    public io.raspberrywallet.module.Module asServerModule() {
        return new io.raspberrywallet.module.Module(getId(), getId(), getDescription()) {
        };
    }

    /**
     * Check if needed interaction (User-Module) has been completed.
     * @return true, if we are ready to decrypt
     */
    public abstract boolean check();

    /**
     * @param keyPart - unencrypted key part
     * @return encrypted payload
     */
    public abstract byte[] encrypt(byte[] keyPart) throws EncryptionException;

    /**
     * @param payload - encrypted payload
     * @return decrypted key part
     */
    public abstract byte[] decrypt(byte[] payload) throws DecryptionException;

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

    public String getId() {
        return this.getClass().getName();
    }

    public void newSession() {
        input.clear();
        register();
    }

    public void setPayload(byte[] payload) {
        this.payload = payload.clone();
    }

    public int getStatus() {
        return this.status;
    }

    public byte[] getResult() throws DecryptionException {
        if (getStatus() != STATUS_OK) throw new DecryptionException(getStatus());
        else return decryptedValue;
    }
    
    /**
     * Sets input for this Module from user
     * @param key   - key of the parameter
     * @param value - value of the parameter
     */
    public void setInput(String key, String value) {
        input.put(key, value);
    }
    
    /**
     * Sets inputs for this Module from user
     * @param inputs Map with key-value inputs.
     */
    public void setInputs(Map<String, String> inputs) {
        input.putAll(inputs);
    }

    @Override
    public void destroy() {
        ArrayDestroyer.destroy(decryptedValue);
    }
    
    /**
     * Checks if user has submitted any input
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
    String getInput(String key) {
        return input.get(key);
    }

}