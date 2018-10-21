package io.raspberrywallet.manager;

import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestUtils {
    public static List<String> generateRandomDeterministicMnemonicCode() throws MnemonicException, NoSuchAlgorithmException {
        DeterministicSeed seed = new DeterministicSeed(SecureRandom.getInstanceStrong(),
                DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS, "", System.currentTimeMillis());
        seed.check();
        List<String> mnemonicCode = seed.getMnemonicCode();
        assertNotNull(mnemonicCode);
        return mnemonicCode;
    }

    public static DeterministicSeed generateRandomDeterministicSeed() throws NoSuchAlgorithmException {
        return new DeterministicSeed(SecureRandom.getInstanceStrong(),
                DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS, "", System.currentTimeMillis());
    }
}
