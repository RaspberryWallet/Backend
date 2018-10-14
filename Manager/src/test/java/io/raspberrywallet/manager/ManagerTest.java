package io.raspberrywallet.manager;

import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.database.Database;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.manager.modules.PinModule;
import io.raspberrywallet.manager.modules.PushButtonModule;
import org.bitcoinj.crypto.MnemonicException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static io.raspberrywallet.manager.Utils.println;
import static org.mockito.Mockito.mock;

class ManagerTest {
    private static Manager manager;
    private static Bitcoin bitcoin;
    private static TemperatureMonitor temperatureMonitor;
    private static Database db;
    private static List<Module> modules;

    @BeforeAll
    static void setup() {
        bitcoin = mock(Bitcoin.class);
        temperatureMonitor = mock(TemperatureMonitor.class);
        db = mock(Database.class);
        modules = new ArrayList<>();
        modules.add(new PinModule());
        modules.add(new PushButtonModule());
        manager = new Manager(db, modules, bitcoin, temperatureMonitor);
    }

    @Test
    void ping() {
        assert manager.ping().equals("pong");
    }

    @Test
    void restoreFromBackupPhrase() throws NoSuchAlgorithmException, MnemonicException {
        List<String> mnemonicCode = TestUtils.generateRandomDeterministicMnemonicCode();
        mnemonicCode.forEach(System.out::println);
        //TODO Mock
//        manager.restoreFromBackupPhrase(mnemonicCode, modules.stream().map(Module::getId).map(id -> ).collect(Collectors.toList()), 2);
        Mockito.verify(bitcoin).restoreFromSeed(mnemonicCode);
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

    @Test
    void getCpuTemperature() {
        final String mockCpuTemp = "75 °C";
        Mockito.when(temperatureMonitor.call()).thenReturn(mockCpuTemp);
        String cpuTemperature = manager.getCpuTemperature();
        Mockito.verify(temperatureMonitor).call();
        Mockito.verifyNoMoreInteractions(temperatureMonitor);
        assert cpuTemperature.equals(mockCpuTemp);
    }

}