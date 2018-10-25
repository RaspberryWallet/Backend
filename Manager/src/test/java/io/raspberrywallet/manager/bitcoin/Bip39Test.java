package io.raspberrywallet.manager.bitcoin;

import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Bip39Test {
    @Test
    void testCohesion() throws UnreadableWalletException {
        byte[] seed = "farm hospital shadow common raw neither pond access suggest army prefer expire".getBytes();
        DeterministicSeed deterministicSeed = new DeterministicSeed(new String(seed), null, "", 0);


        assertEquals(new String(deterministicSeed.getSecretBytes()), new String(seed));
    }
}
