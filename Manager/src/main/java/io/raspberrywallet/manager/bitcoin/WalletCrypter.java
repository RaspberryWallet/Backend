package io.raspberrywallet.manager.bitcoin;

import com.google.protobuf.ByteString;
import com.stasbar.Logger;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.wallet.Protos;
import org.bitcoinj.wallet.Wallet;
import org.jetbrains.annotations.NotNull;
import org.spongycastle.crypto.params.KeyParameter;

import static com.google.common.base.Preconditions.checkNotNull;

public class WalletCrypter {
    // These params were determined empirically on a top-range (as of 2014) MacBook Pro with native scrypt support,
    // using the scryptenc command line tool from the original scrypt distribution, given a memory limit of 40mb.
    private static final Protos.ScryptParameters SCRYPT_PARAMETERS = Protos.ScryptParameters.newBuilder()
            .setP(6)
            .setR(8)
            .setN(32768)
            .setSalt(ByteString.copyFrom(KeyCrypterScrypt.randomSalt()))
            .build();


    void decryptWallet(@NotNull Wallet wallet, @NotNull String password) {
        if (password.length() == 0 || password.length() < 4) {
            throw new IllegalArgumentException("Bad password. The password you entered is empty or too short.");
        }

        final KeyCrypterScrypt scrypt = (KeyCrypterScrypt) wallet.getKeyCrypter();
        checkNotNull(scrypt);   // We should never arrive at this GUI if the wallet isn't actually encrypted.
        KeyParameter aesKey = scrypt.deriveKey(password);
        if (wallet.checkAESKey(aesKey)) {
            wallet.decrypt(aesKey);
        } else {
            Logger.err("User entered incorrect password");
            Logger.err("Wrong password");
            Logger.err("Please try entering your password again, carefully checking for typos or spelling errors.");
        }
    }

    public void encryptWallet(@NotNull Wallet wallet, @NotNull String password) {
        // This is kind of arbitrary and we could do much more to help people pick strong passwords.
        if (password.length() < 4) {
            throw new IllegalArgumentException("Password too short. You need to pick a password at least five characters or longer.");
        }

        KeyCrypterScrypt script = new KeyCrypterScrypt(SCRYPT_PARAMETERS);
        KeyParameter aesKey = script.deriveKey(password);
        Logger.info("Key derived, now encrypting");
        wallet.encrypt(script, aesKey);
        Logger.info("Encryption done");
        Logger.info("Wallet encrypted");
        Logger.info("You can remove the password at any time from the settings screen.");
    }
}
