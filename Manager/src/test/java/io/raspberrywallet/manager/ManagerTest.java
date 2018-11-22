package io.raspberrywallet.manager;

import io.raspberrywallet.contract.*;
import io.raspberrywallet.contract.module.ModuleState;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.ShamirKey;
import io.raspberrywallet.manager.database.Database;
import io.raspberrywallet.manager.database.KeyPartEntity;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.manager.modules.example.ExampleModule;
import io.raspberrywallet.manager.modules.pin.PinModule;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static io.raspberrywallet.manager.TestUtils.generateRandomDeterministicSeed;
import static io.raspberrywallet.manager.Utils.println;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ManagerTest {
    private Manager manager;
    private Bitcoin bitcoin;
    private TemperatureMonitor temperatureMonitor;
    private Database database;
    private PinModule pinModule;
    private ExampleModule exampleModule;
    private static DeterministicSeed seed;

    static {
        try {
            seed = generateRandomDeterministicSeed();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static File walletFile;

    @BeforeAll
    static void setupAll() throws IOException {
        walletFile = File.createTempFile("test_wallet", "wallet");
        walletFile.deleteOnExit();
    }

    @BeforeEach
    void setup() throws IllegalAccessException, InstantiationException, ModuleInitializationException {
        bitcoin = mock(Bitcoin.class);
        temperatureMonitor = mock(TemperatureMonitor.class);
        database = mock(Database.class);
        List<Module> modules = new ArrayList<>();
        exampleModule = new ExampleModule();
        pinModule = new PinModule();
        modules.add(exampleModule);
        modules.add(pinModule);

        CommunicationChannel channel = new CommunicationChannel();
        manager = new Manager(Configuration.testConfiguration(), database, modules, bitcoin, temperatureMonitor, channel);
    }


    @Test
    void ping() {
        assert manager.ping().equals("pong");
    }

    @Test
    void getModules() {
        manager.getServerModules().forEach(module -> assertTrue(module instanceof io.raspberrywallet.contract.module.Module));
    }

    @Test
    void getWalletStatusUnset() throws WalletNotInitialized {
        when(bitcoin.getWallet()).thenThrow(new WalletNotInitialized());
        assertEquals(manager.getWalletStatus(), WalletStatus.UNLOADED);
    }

    @Test
    @Disabled("Long running test")
    void getWalletStatusSet() throws MnemonicException, NoSuchAlgorithmException, RequiredInputNotFound, WalletNotInitialized {
        restoreFromBackupPhrase();
        when(bitcoin.getWallet()).thenReturn(Wallet.fromSeed(TestNet3Params.get(), generateRandomDeterministicSeed()));

        assertEquals(manager.getWalletStatus(), WalletStatus.DECRYPTED);
    }

    @Test
    void getModuleState() {
        ModuleState pinState = manager.getModuleState(pinModule.getId());
        assertEquals(0, pinState.compareTo(ModuleState.WAITING));
        assertEquals(pinState.getMessage(), pinModule.getStatusString());

    }

    @Test
    @Disabled("Long running test")
    void restoreFromBackupPhrase() throws NoSuchAlgorithmException, MnemonicException, RequiredInputNotFound {
        List<String> mnemonicCode = TestUtils.generateRandomDeterministicMnemonicCode();
        mnemonicCode.forEach(System.out::println);

        Map<String, String> pinInputs = new HashMap<>();
        pinInputs.put(PinModule.PIN, "1234");

        Map<String, Map<String, String>> selectedModulesWithInputs = new HashMap<>();
        selectedModulesWithInputs.put(pinModule.getId(), pinInputs);
        selectedModulesWithInputs.put(exampleModule.getId(), new HashMap<>());

        manager.restoreFromBackupPhrase(mnemonicCode, selectedModulesWithInputs, 2);
        Mockito.verify(bitcoin).setupWalletFromMnemonic(mnemonicCode, null);
    }

    @Test
    void unlockWalletWhenUnlocked() throws WalletNotInitialized, RequiredInputNotFound, EncryptionException, InternalModuleException {
        when(bitcoin.getWallet()).thenReturn(Wallet.fromSeed(TestNet3Params.get(), seed));
        pinModule.setInput(PinModule.PIN, "1234");

        ShamirKey exampleKey = new ShamirKey(BigInteger.ONE, BigInteger.TEN, BigInteger.ONE);
        ShamirKey pinKey = new ShamirKey(BigInteger.TEN, BigInteger.ONE, BigInteger.TEN);
        final KeyPartEntity exampleKeyPart = new KeyPartEntity(exampleModule.encryptKeyPart(exampleKey.toByteArray()), exampleModule.getId());
        final KeyPartEntity pinKeyPart = new KeyPartEntity(pinModule.encryptKeyPart(pinKey.toByteArray()), pinModule.getId());

        when(database.getKeypartForModuleId(exampleModule.getId())).thenReturn(Optional.of(exampleKeyPart));
        when(database.getKeypartForModuleId(pinModule.getId())).thenReturn(Optional.of(pinKeyPart));


        Map<String, String> pinInputs = new HashMap<>();
        pinInputs.put(PinModule.PIN, "1234");

        Map<String, Map<String, String>> selectedModulesWithInputs = new HashMap<>();
        selectedModulesWithInputs.put(pinModule.getId(), pinInputs);
        selectedModulesWithInputs.put(exampleModule.getId(), new HashMap<>());

        assertThrows(IllegalStateException.class, () -> manager.unlockWallet(selectedModulesWithInputs));
    }

    @Test
    void lockWalletWhenUnlocked() throws WalletNotInitialized, EncryptionException, InternalModuleException, RequiredInputNotFound, IOException, IncorrectPasswordException {
        when(bitcoin.getWallet()).thenReturn(Wallet.fromSeed(TestNet3Params.get(), seed));
        pinModule.setInput(PinModule.PIN, "1234");

        ShamirKey exampleKey = new ShamirKey(BigInteger.ONE, BigInteger.TEN, BigInteger.ONE);
        ShamirKey pinKey = new ShamirKey(BigInteger.TEN, BigInteger.ONE, BigInteger.TEN);
        final KeyPartEntity exampleKeyPart = new KeyPartEntity(exampleModule.encryptKeyPart(exampleKey.toByteArray()), exampleModule.getId());
        final KeyPartEntity pinKeyPart = new KeyPartEntity(pinModule.encryptKeyPart(pinKey.toByteArray()), pinModule.getId());
        when(database.getKeypartForModuleId(exampleModule.getId())).thenReturn(Optional.of(exampleKeyPart));
        when(database.getKeypartForModuleId(pinModule.getId())).thenReturn(Optional.of(pinKeyPart));

        when(bitcoin.getWalletFile()).thenReturn(walletFile);

        manager.lockWallet();

        assertTrue(walletFile.exists());
        println(manager.getWalletStatus());
        assertEquals(manager.getWalletStatus(), WalletStatus.ENCRYPTED);
        assertFalse(pinModule.hasInput(PinModule.PIN));
    }

    @Test
    void unlockWalletWhenLocked() throws WalletNotInitialized, RequiredInputNotFound, EncryptionException, InternalModuleException, IncorrectPasswordException, IOException {
        lockWalletWhenUnlocked();

        Map<String, Map<String, String>> moduleToInputsMap = new HashMap<>();
        HashMap<String, String> pinInputs = new HashMap<>();
        pinInputs.put(PinModule.PIN, "1234");
        moduleToInputsMap.put(pinModule.getId(), pinInputs);

        manager.unlockWallet(moduleToInputsMap);

        assertEquals(manager.getWalletStatus(), WalletStatus.DECRYPTED);
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