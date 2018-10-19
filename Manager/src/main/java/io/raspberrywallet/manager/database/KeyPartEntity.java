package io.raspberrywallet.manager.database;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

public class KeyPartEntity {
    @JsonProperty("payload")
    public byte[] payload;
    @JsonProperty("module")
    public String module;

    public KeyPartEntity() {

    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public KeyPartEntity(byte[] payload, String module) {
        this.payload = payload;
        this.module = module;
    }

    /*
     * Filling everything with zeroes to keep RAM safe
     * */
    protected synchronized void clean() {

        if (payload != null) {
            for (int i = 0; i < payload.length; ++i)
                payload[i] = (byte) (i % 120);
        }
    }

    /*
     * Needed to override this so `WalletEntity` can be compared with ease.
     * Two `KeyPartEntity` with different pointer can be equal now.
     * */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof KeyPartEntity) {
            KeyPartEntity kpe = (KeyPartEntity) obj;
            return (
                    this.module.equals(kpe.module)
                            && Arrays.equals(this.payload, kpe.payload)
            );
        }
        return super.equals(obj);
    }
}
