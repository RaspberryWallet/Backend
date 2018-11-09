package io.raspberrywallet.contract;

public enum WalletStatus {
    FIRST_TIME, // Fresh start
    UNLOADED, // Persisted on disk, not loaded into RAM
    ENCRYPTED, // NotOperable - requires unlocking
    DECRYPTED // Operable
}
