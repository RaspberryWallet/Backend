package io.raspberrywallet.manager;

import io.raspberrywallet.RequiredInputNotFound;
import io.raspberrywallet.WalletNotInitialized;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.database.Database;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.modules.ExampleModule;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.manager.modules.PinModule;
import io.raspberrywallet.module.ModuleState;
import org.bitcoinj.crypto.MnemonicException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.raspberrywallet.manager.Utils.println;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ManagerTest {
    private static Manager manager;
    private static Bitcoin bitcoin;
    private static TemperatureMonitor temperatureMonitor;
    private static Database db;
    private static List<Module> modules;
    private static PinModule pinModule;
    private static ExampleModule exampleModule;

    @BeforeAll
    static void setup() {
        bitcoin = mock(Bitcoin.class);
        temperatureMonitor = mock(TemperatureMonitor.class);
        db = mock(Database.class);
        modules = new ArrayList<>();
        exampleModule = new ExampleModule();
        pinModule = new PinModule();
        modules.add(exampleModule);

        modules.add(pinModule);
        manager = new Manager(db, modules, bitcoin, temperatureMonitor);
    }

    @Test
    void ping() {
        assert manager.ping().equals("pong");
    }

    @Test
    void getModules() {
        manager.getModules().forEach(module -> assertTrue(module instanceof io.raspberrywallet.module.Module));
    }

    @Test
    void getModuleState() {
        ModuleState pinState = manager.getModuleState(pinModule.getId());
        assertEquals(0, pinState.compareTo(ModuleState.WAITING));
        assertEquals(pinState.getMessage(), pinModule.getStatusString());

    }

    @Test
    void restoreFromBackupPhrase() throws NoSuchAlgorithmException, MnemonicException, RequiredInputNotFound {
        List<String> mnemonicCode = TestUtils.generateRandomDeterministicMnemonicCode();
        mnemonicCode.forEach(System.out::println);

        Map<String, String> pinInputs = new HashMap<>();
        pinInputs.put(PinModule.Inputs.PIN, "1234");

        Map<String, Map<String, String>> selectedModulesWithInputs = new HashMap<>();
        selectedModulesWithInputs.put(pinModule.getId(), pinInputs);
        selectedModulesWithInputs.put(exampleModule.getId(), new HashMap<>());

        manager.restoreFromBackupPhrase(mnemonicCode, selectedModulesWithInputs, 2);
        Mockito.verify(bitcoin).restoreFromSeed(mnemonicCode);
        Mockito.verifyNoMoreInteractions(bitcoin);
    }


    @Test
    void getCurrentReceiveAddress() throws WalletNotInitialized {
        Mockito.when(bitcoin.getCurrentReceiveAddress()).thenReturn("mwrHAGCN2kLFGB2eZF7F93fC4yVss3iDDj");
        String currentAddress = manager.getCurrentReceiveAddress();
        Mockito.verify(bitcoin).getCurrentReceiveAddress();
        Mockito.verifyNoMoreInteractions(bitcoin);
        assertNotNull(currentAddress);
        assertEquals(currentAddress.length(), 34);
        println(currentAddress);
    }

    @Test
    void getFreshReceiveAddress() throws WalletNotInitialized {
        Mockito.when(bitcoin.getFreshReceiveAddress()).thenReturn("mwrHAGCN2kLFGB2eZF7F93fC4yVss3iDDj");
        String freshAddress = manager.getFreshReceiveAddress();
        Mockito.verify(bitcoin).getFreshReceiveAddress();
        Mockito.verifyNoMoreInteractions(bitcoin);
        assertNotNull(freshAddress);
        assertEquals(freshAddress.length(), 34);
        println(freshAddress);
    }

    @Test
    void getEstimatedBalance() throws WalletNotInitialized {
        final String mockEstimatedBalance = "1.23 BTC";
        Mockito.when(bitcoin.getEstimatedBalance()).thenReturn(mockEstimatedBalance);
        String estimatedBalance = manager.getEstimatedBalance();
        Mockito.verify(bitcoin).getEstimatedBalance();
        Mockito.verifyNoMoreInteractions(bitcoin);
        assertEquals(estimatedBalance, mockEstimatedBalance);
    }

    @Test
    void getAvailableBalance() throws WalletNotInitialized {
        final String mockAvailableBalance = "0.00 BTC";
        Mockito.when(bitcoin.getAvailableBalance()).thenReturn(mockAvailableBalance);
        String availableBalance = manager.getAvailableBalance();
        Mockito.verify(bitcoin).getAvailableBalance();
        Mockito.verifyNoMoreInteractions(bitcoin);
        assertEquals(availableBalance, mockAvailableBalance);
    }

    @Test
    void getCpuTemperature() {
        final String mockCpuTemp = "75 °C";
        Mockito.when(temperatureMonitor.call()).thenReturn(mockCpuTemp);
        String cpuTemperature = manager.getCpuTemperature();
        Mockito.verify(temperatureMonitor).call();
        Mockito.verifyNoMoreInteractions(temperatureMonitor);
        assertEquals(cpuTemperature, mockCpuTemp);
    }

}