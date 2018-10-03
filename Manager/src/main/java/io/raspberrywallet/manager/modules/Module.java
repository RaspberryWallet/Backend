package io.raspberrywallet.manager.modules;

import java.util.HashMap;

public abstract class Module {

	/*
	 * Module info formatted as JSON.
	 * */
	
    @Override
    public String toString() {
        return "{\"id\":\"" + getId() + "\", \"status\":\"" + getStatusString() + "\"}";
    }

    public abstract String getDescription();

    public io.raspberrywallet.module.Module asServerModule() {
        return new io.raspberrywallet.module.Module(getId(), getDescription()) {
        };
    }

    /**
     * Check if needed interaction (User-Module) has been completed
     * 
     * @return true, if we are ready to decrypt
     */
    public abstract boolean check();

    /**
     * used for decryption, should include `this.decrypt(Decrypter)`
     */
    public abstract void process();

    /**
     * this function should prepare module before consecutive use.
     * Manager should call this.
     */
    public abstract void register();

    /**
     * this function should return HTML UI form or null if not required
     */
    public abstract String getHtmlUi();

    /**
     * Manager uses this to start the Module after register()
     */
    public void start() {
        checkThread = new Thread(checkRunnable.enable().setSleepTime(100));
        checkThread.start();
    }

    /**
     * Encryption function when creating wallet
     *
     * @param data   - unencrypted key part
     * @param params - additional params
     * @return encrypted payload
     */
    public abstract byte[] encryptInput(byte[] data, Object... params);

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
     * @param statusS - new status
     */
    protected void setStatusString(String statusS) {
        this.statusString = statusS;
    }

    public String getId() {
        return this.getClass().getName();
    }

    public static final int STATUS_OK = 200;
    public static final int STATUS_TIMEOUT = 432;
    public static final int STATUS_WAITING = 100;

    private byte[] payload;
    private int status = STATUS_WAITING;
    private String statusString;
    private byte[] decryptedValue;
    private HashMap<String, String> input = new HashMap<String, String>();

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

    protected interface Decrypter {
        public byte[] decrypt(byte[] payload) throws DecryptionException;
    }

    protected interface Encrypter {
        public byte[] encrypt(byte[] data);
    }

    private Thread checkThread;

    private class CheckRunnable implements Runnable {

        private boolean run = false;
        private long sleepTime = 1000;
        private long timeout = 3000;
        private long startTime;

        public CheckRunnable stop() {
            run = false;
            return this;
        }

        public CheckRunnable enable() {
            run = true;
            return this;
        }

        public CheckRunnable setSleepTime(long sleep) {
            sleepTime = sleep;
            return this;
        }

        public CheckRunnable setTimeout(long tout) {
            timeout = tout;
            return this;
        }

        public void run() {
            startTime = System.currentTimeMillis();
            while (run) {

                if (check()) {
                    process();
                    run = false;
                }

                if(System.currentTimeMillis() - startTime > timeout && status == STATUS_WAITING) {
                    run = false;
                    status = STATUS_TIMEOUT;
                    statusString = "Timed out waiting for Module interaction.";
                }
            }
            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    CheckRunnable checkRunnable = new CheckRunnable();

    /* 
     * Used when everything has been completed, both in "cancel" and "done" cases.
     * Override this to be sure everything else is cleaned.
     * Manager should call this.
     * */
    public void destroy() {

        //Stopping the wait thread
        checkRunnable.stop();
        try {
            //Joining
            checkThread.join(checkRunnable.sleepTime * 2);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //Clearing the RAM
            synchronized (this) {
                zeroFill();
            }
        }
    }


    /*
     * Fill everything with "zeroes"
     */
    public synchronized void zeroFill() {
        if (decryptedValue != null) {
            for (int i = 0; i < decryptedValue.length; ++i)
                decryptedValue[i] = (byte) (i % 120);
        }
        if (payload != null) {
            for (int i = 0; i < payload.length; ++i)
                payload[i] = (byte) (i % 60);
        }
    }

    public class DecryptionException extends Throwable {

        public static final int NO_DATA = -1;
        public static final int BAD_KEY = -2;
        public static final int UNKNOWN = -3;

        private int code = UNKNOWN;

        public DecryptionException(int code) {
            this.code = code;
        }

        public int getCode() {
            return this.code;
        }

        public String getMessage() {
            switch (code) {
                case NO_DATA:
                    return "-1: No paylaod data specified.";
                case BAD_KEY:
                    return "-2: Wrong key provided for decryption.";
                case UNKNOWN:
                    return "-3: Unknown error.";
            }
            return "??: This is an error LOL.";
        }

    }

    public synchronized void decrypt(Decrypter decrypter) {
        try {
            decryptedValue = decrypter.decrypt(payload);
            this.status = Module.STATUS_OK;
            this.statusString = "OK: 200: Decrypted keypart";
        } catch (DecryptionException de) {
            this.status = de.getCode();
            this.statusString = "Error: " + de.getMessage();
        }
    }

    /**
     * Sets input for this Module from user
     * @param key - key of the parameter
     * @param value - value of the parameter
     */
    public void setInput(String key, String value) {
        input.put(key, value);
    }

    /**
     * Checks if user has submitted any input
     * @param key - key of the parameter
     * @return - if key exists
     */
    protected boolean hasInput(String key) {
        return input.containsKey(key);
    }

    /**
     * Gets the value which user has submitted
     * @param key - parameter key
     * @return - value of the parameter
     */
    protected String getInput(String key) {
        return input.get(key);
    }

}