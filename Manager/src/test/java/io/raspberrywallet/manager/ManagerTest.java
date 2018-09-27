package io.raspberrywallet.manager;

import io.raspberrywallet.manager.bitcoin.Bitcoin;
import org.bitcoinj.crypto.MnemonicException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import static io.raspberrywallet.manager.Utils.println;

class ManagerTest {
    private static Manager manager;
    static Bitcoin bitcoin;

    @BeforeAll
    static void setup() {
        bitcoin = Mockito.mock(Bitcoin.class);
        manager = new Manager(bitcoin);
    }

    @Test
    void ping() {
        assert manager.ping().equals("pong");
    }

    @Test
    void restoreFromBackupPhrase() throws NoSuchAlgorithmException, MnemonicException {
        List<String> mnemonicCode = TestUtils.generateRandomDeterministicMnemonicCode();
        mnemonicCode.forEach(System.out::println);

        manager.restoreFromBackupPhrase(mnemonicCode);
        Mockito.verify(bitcoin).restoreFromBackupPhrase(mnemonicCode);
        Mockito.verifyNoMoreInteractions(bitcoin);
    }

    @Test
    void getCurrentReceiveAddress() {
        Mockito.when(bitcoin.getCurrentReceiveAddress()).thenReturn("mwrHAGCN2kLFGB2eZF7F93fC4yVss3iDDj");
        String currentAddress = manager.getCurrentReceiveAddress();
        Mockito.verify(bitcoin).getCurrentReceiveAddress();
        Mockito.verifyNoMoreInteractions(bitcoin);
        assert currentAddress != null;
        assert currentAddress.length() == 34;
        println(currentAddress);
    }

    @Test
    void getFreshReceiveAddress() {
        Mockito.when(bitcoin.getFreshReceiveAddress()).thenReturn("mwrHAGCN2kLFGB2eZF7F93fC4yVss3iDDj");
        String freshAddress = manager.getFreshReceiveAddress();
        Mockito.verify(bitcoin).getFreshReceiveAddress();
        Mockito.verifyNoMoreInteractions(bitcoin);
        assert freshAddress != null;
        assert freshAddress.length() == 34;
        println(freshAddress);
    }

    @Test
    void getEstimatedBalance() {
        final String mockEstimatedBalance = "1.23 BTC";
        Mockito.when(bitcoin.getEstimatedBalance()).thenReturn(mockEstimatedBalance);
        String estimatedBalance = manager.getEstimatedBalance();
        Mockito.verify(bitcoin).getEstimatedBalance();
        Mockito.verifyNoMoreInteractions(bitcoin);
        assert estimatedBalance.equals(mockEstimatedBalance);
    }

    @Test
    void getAvailableBalance() {
        final String mockAvailableBalance = "0.00 BTC";
        Mockito.when(bitcoin.getAvailableBalance()).thenReturn(mockAvailableBalance);
        String availableBalance = manager.getAvailableBalance();
        Mockito.verify(bitcoin).getAvailableBalance();
        Mockito.verifyNoMoreInteractions(bitcoin);
        assert availableBalance.equals(mockAvailableBalance);
    }

}