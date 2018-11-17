package io.raspberrywallet.manager;

import com.stasbar.Logger;
import io.raspberrywallet.contract.*;
import io.raspberrywallet.contract.module.ModuleState;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.common.StreamUtils;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.Shamir;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.ShamirException;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.ShamirKey;
import io.raspberrywallet.manager.database.Database;
import io.raspberrywallet.manager.database.KeyPartEntity;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.linux.WPAConfiguration;
import io.raspberrywallet.manager.linux.WifiScanner;
import io.raspberrywallet.manager.linux.WifiStatus;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.manager.modules.ModuleClassLoader;
import kotlin.text.Charsets;
import lombok.Getter;
import org.bitcoinj.core.Sha256Hash;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Manager implements io.raspberrywallet.contract.Manager {

    /**
     * Module id -> Module instance
     */
    @NotNull
    @Getter
    private final ConcurrentHashMap<String, Module> modules = new ConcurrentHashMap<>();
    @NotNull
    private final Bitcoin bitcoin;
    @NotNull
    private final TemperatureMonitor tempMonitor;
    private final WPAConfiguration wpaConfiguration;
    @NotNull
    private final Database database;

    @NotNull
    private final CommunicationChannel frontendChannel;
    private final AutoLockTimer autoLockTask;

    private Configuration configuration;

    Manager(@NotNull Configuration configuration,
            @NotNull Database database,
            @NotNull List<Module> modules,
            @NotNull Bitcoin bitcoin,
            @NotNull TemperatureMonitor tempMonitor,
            @NotNull CommunicationChannel frontendChannel) {
        this.frontendChannel = frontendChannel;
        modules.forEach(module -> this.modules.put(module.getId(), module));
        this.database = database;
        this.bitcoin = bitcoin;
        this.tempMonitor = tempMonitor;
        this.wpaConfiguration = new WPAConfiguration();
        this.configuration = configuration;

        Runnable onLockTriggered = () -> {
            frontendChannel.info("Autolock triggered");
            try {
                lockWallet();
            } catch (WalletNotInitialized ignored) {
                //we don't care about locking if it wasn't even inited
            }
            clearModuleInputs();
        };
        Timer timer = new Timer();
        autoLockTask = new AutoLockTimer(configuration.getAutoLockSeconds(), timer, onLockTriggered);
        timer.scheduleAtFixedRate(autoLockTask, Duration.ofSeconds(1).toMillis(), Duration.ofSeconds(1).toMillis());
    }

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    public Response nextStep(@NotNull String moduleId, Map<String, String> input) {
        modules.get(moduleId).setInputs(input);
        return new Response(null, Response.Status.OK);
    }

    /*
     * Modules
     */

    @NotNull
    @Override
    public List<io.raspberrywallet.contract.module.Module> getServerModules() {
        return modules.values().stream()
                .map(Module::asServerModule)
                .collect(toList());
    }


    @Override
    public ModuleState getModuleState(@NotNull String id) {
        ModuleState state = ModuleState.FAILED;
        state.setMessage("Unknown module!");
        if (modules.containsKey(id)) {
            state = ModuleState.WAITING;
            state.setMessage(modules.get(id).getStatusString());
        }
        return state;
    }

    @Override
    public void restoreFromBackupPhrase(@NotNull List<String> mnemonicCode,
                                        @NotNull Map<String, Map<String, String>> selectedModulesWithInputs,
                                        int required) throws RequiredInputNotFound {

        List<Module> modulesToDecrypt = selectedModulesWithInputs.keySet().stream()
                .map(modules::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        byte[] seed = String.join(" ", mnemonicCode).getBytes(Charsets.UTF_8);
        int numBits = seed.length * 8; //We need bits not bytes
        try {
            BigInteger[] params = Shamir.generateParams(required, numBits, seed);
            ShamirKey[] keys = Shamir.generateKeys(modulesToDecrypt.size(), required, numBits, params);

            List<KeyPartEntity> keyPartEntities = new ArrayList<>();
            for (int i = 0; i < keys.length; i++) {
                Module module = modulesToDecrypt.get(i);
                module.setInputs(selectedModulesWithInputs.get(module.getId()));
                KeyPartEntity keyPartEntity = new KeyPartEntity();
                keyPartEntity.setPayload(module.encryptKeyPart(keys[i].toByteArray()));
                keyPartEntity.setModule(module.getId());
                keyPartEntities.add(keyPartEntity);
            }
            database.addAllKeyParts(keyPartEntities);

            bitcoin.setupWalletFromMnemonic(mnemonicCode, getPrivateKeyHash());

        } catch (ShamirException | EncryptionException | InternalModuleException e) {
            e.printStackTrace();
        }
    }

    @Override
    public WalletStatus getWalletStatus() {
        try {
            if (bitcoin.isFirstTime() || database.isFirstTime()) return WalletStatus.FIRST_TIME;
            return bitcoin.getWallet().isEncrypted() ?
                    WalletStatus.ENCRYPTED : WalletStatus.DECRYPTED;
        } catch (WalletNotInitialized e) {
            return WalletStatus.UNLOADED;
        }
    }


    private String getPrivateKeyHash() {
        byte[] privateKeyHash = Sha256Hash.hash(getPrivateKeyFromModules());
        return new String(privateKeyHash);
    }

    @Override
    public void unlockWallet(Map<String, Map<String, String>> moduleToInputsMap) throws WalletNotInitialized {
        bitcoin.ensureWalletInitialized();
        fillModulesWithInputs(moduleToInputsMap);
        String password = getPrivateKeyHash();
        try {
            bitcoin.decryptWallet(password);
        } catch (IllegalArgumentException e) {
            frontendChannel.error(e.getMessage());
        }
    }

    @Override
    public void loadWalletFromDisk(@NotNull Map<String, Map<String, String>> moduleToInputsMap) {
        fillModulesWithInputs(moduleToInputsMap);
        String password = getPrivateKeyHash();
        bitcoin.setupWalletFromFile(password);
    }

    private void fillModulesWithInputs(@NotNull Map<String, Map<String, String>> moduleToInputsMap) {
        moduleToInputsMap.forEach((moduleId, inputs) -> {
            Module module = modules.get(moduleId);
            Logger.d("Setting inputs for " + module.getId());
            inputs.forEach((name, value) -> Logger.d(name + ": " + value));
            inputs.forEach(module::setInput);
        });
    }

    @Override
    public boolean lockWallet() throws WalletNotInitialized {
        bitcoin.ensureWalletInitialized();
        String password = getPrivateKeyHash();

        try {
            bitcoin.saveEncryptedWallet(password);
            bitcoin.encryptWallet(password);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            frontendChannel.error(e.getMessage());
        } finally {
            clearModuleInputs();
        }
        return false;
    }

    private void clearModuleInputs() {
        modules.values().forEach(Module::clearInputs);
    }

    private byte[] getPrivateKeyFromModules() {
        ShamirKey[] shamirKeys = modules.values().stream()
                .map(module -> {
                    try {
                        Optional<KeyPartEntity> keyPartEntity = database.getKeypartForModuleId(module.getId());
                        if (!keyPartEntity.isPresent())  // could not find module with this module.getId()
                            return null;

                        KeyPartEntity dbEntity = keyPartEntity.get();
                        return module.decryptKeyPart(dbEntity.getPayload());
                    } catch (InternalModuleException | DecryptionException | RequiredInputNotFound e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(ShamirKey::fromByteArray)
                .toArray(ShamirKey[]::new);

        return Shamir.calculateLagrange(shamirKeys);
    }


    /*
     * Bitcoin Domain
     */

    @NotNull
    @Override
    public String getCurrentReceiveAddress() throws WalletNotInitialized {
        return bitcoin.getCurrentReceiveAddress();
    }

    @NotNull
    @Override
    public String getFreshReceiveAddress() throws WalletNotInitialized {
        return bitcoin.getFreshReceiveAddress();
    }

    @NotNull
    @Override
    public String getEstimatedBalance() throws WalletNotInitialized {
        return bitcoin.getEstimatedBalance();
    }

    @NotNull
    @Override
    public String getAvailableBalance() throws WalletNotInitialized {
        return bitcoin.getAvailableBalance();
    }

    @Override
    public void sendCoins(@NotNull String amount, @NotNull String recipientAddress) throws WalletNotInitialized {
        bitcoin.sendCoins(amount, recipientAddress);
    }

    /*
     * Utilities
     */

    @NotNull
    @Override
    public String getCpuTemperature() {
        return tempMonitor.call();
    }

    @Override
    public void tap() {
        autoLockTask.tap();
    }

    @Override
    public void setDatabasePassword(@NotNull String password) throws SecurityException {
        try {
            database.setPassword(password);
        } catch (Exception e) {
            e.printStackTrace();
            throw new SecurityException(e);
        }
    }

    /* Network */

    @NotNull
    @Override
    public String[] getNetworkList() {
        return new WifiScanner().call();
    }

    @NotNull
    @Override
    public Map<String, String> getWifiStatus() {
        return new WifiStatus().call();
    }

    @NotNull
    @Override
    public Map<String, String> getWifiConfig() {
        return this.wpaConfiguration.getAsMap();
    }

    @Override
    public int setWifiConfig(Map<String, String> config) {
        return this.wpaConfiguration.setFromMap(config);
    }

    @Override
    public void addBlockChainProgressListener(@NotNull IntConsumer listener) {
        bitcoin.addBlockChainProgressListener(listener);
    }
    
    public void uploadNewModule(File file, String fileName) throws ModuleUploadException {
        
        //Verify in /tmp
        try {
            if (!ModuleClassLoader.verifyJarSignature(file))
                throw new ModuleUploadException("Jar verification failed for given module.");
        
        } catch (MalformedURLException e) {
            throw new ModuleUploadException("Internal I/O error, uploaded module file path is malformed.");
        }
        
        copyVerifiedModule(file, fileName);
    }
    
    /**
     * This method is copying verified module to a new location.
     * If there is already a module in given location, then it's overwritten.
     */
    private void copyVerifiedModule(File file, String fileName) throws ModuleUploadException {
        
        //Create streams (can throw IOException)
        try (InputStream inputStream = new FileInputStream(file);
             FileOutputStream outputStream = new FileOutputStream(
                     configuration.getBasePathPrefix() + "/modules/" + fileName)) {
            
            StreamUtils.transferTo(inputStream, outputStream);
            
        } catch (IOException e) {
            if (e.getCause() != null)
                throw new ModuleUploadException(e.getClass().getName() + " caught while trying to save module. Cause: "
                        + e.getCause());
            else
                throw new ModuleUploadException(e.getClass().getName() + " caught while trying to save module.");
        }
        
    }
}
