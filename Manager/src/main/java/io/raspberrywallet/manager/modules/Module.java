package io.raspberrywallet.manager.modules;

public abstract class Module {

    /**
     * Wydaje mi się, że to zwróci server
     */
    @Override
    public String toString() {
        return "{\"id\":\"" + getId() + "\", \"status\":\"" + getStatusString() + "\"}";
    }

    public io.raspberrywallet.module.Module asServerModule() {
        return new io.raspberrywallet.module.Module(getId()) {
        };
    }

    /**
     * Sprawdzamy tym, czy wszystko o co został
     * człowiek poproszony zostało wykonane
     *
     * @return true, jeśli jesteśmy gotowi do deszyfrowania
     */
    public abstract boolean check();

    /**
     * tutaj deszyfrujemy partię klucza
     */
    public abstract void process();

    /**
     * tutaj przygotowujemy moduł do działania
     */
    public abstract void register();

    /**
     * Uruchamiamy tym moduł do oczekiwania na bodźce
     * z zewnątrz
     */
    public void start() {
        checkThread = new Thread(checkRunnable.enable().setSleepTime(100));
        checkThread.start();
    }

    /**
     * Szyfrowanie partii klucza przy tworzeniu struktury portfela
     *
     * @param data   - niezaszyfrowana partia
     * @param params - dodatkowe parametry
     * @return zaszyfrowany payload do zapisania w bazie
     */
    public abstract byte[] encryptInput(byte[] data, Object... params);

    /**
     * Pobieranie teraźniejszego statusu, wiadomości dla użytkownika
     *
     * @return wiadomość informacyjna
     */
    public String getStatusString() {
        return statusString == null ? "null" : statusString;
    }

    /**
     * W implementacji, ustawiamy tu wiadomość, błąd lub instrukcję
     * dla usera
     *
     * @param statusS - nowa wiadomość
     */
    protected void setStatusString(String statusS) {
        this.statusString = statusS;
    }

    public String getId() { return this.getClass().getName(); }

    public static final int STATUS_OK = 200;

    private byte[] payload;
    private int status;
    private String statusString;
    private byte[] decryptedValue;

    public void newSession() {
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

        public void run() {
            while (run) {
                if (check())
                    process();
            }
            try {
                Thread.sleep(sleepTime);
            } catch (Exception e) {
            }
        }
    }

    CheckRunnable checkRunnable = new CheckRunnable();

    //Co ma się stać po wykonaniu wszystkiego, czyli odblokowaniu klucza lub anuluj
    public void destroy() {
        //Kończymy oczekiwanie
        checkRunnable.stop();
        try {
            //Join bo tak ładnie
            checkThread.join(checkRunnable.sleepTime * 2);
        } catch (Exception e) {

        } finally {
            //Zerujemy pamięć
            synchronized (this) {
                zeroFill();
            }
        }
    }


    // Wypełnia wszystko "zerami" w pamięci
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

}