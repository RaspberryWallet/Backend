package io.raspberrywallet.manager;

import com.stasbar.Logger;
import io.raspberrywallet.RequiredInputNotFound;
import io.raspberrywallet.Response;
import io.raspberrywallet.WalletNotInitialized;
import io.raspberrywallet.WalletStatus;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.Shamir;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.ShamirException;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.ShamirKey;
import io.raspberrywallet.manager.database.Database;
import io.raspberrywallet.manager.database.KeyPartEntity;
import io.raspberrywallet.manager.database.WalletEntity;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.manager.modules.exceptions.KeypartDecryptionException;
import io.raspberrywallet.module.ModuleState;
import kotlin.text.Charsets;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.crypto.KeyCrypterException;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.wallet.Wallet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class Manager implements io.raspberrywallet.Manager {

    /**
     * Module id -> Module instance
     */
    @NonNls
    private final ConcurrentHashMap<String, Module> modules = new ConcurrentHashMap<>();
    @NonNls
    private final Bitcoin bitcoin;
    @NonNls
    private final TemperatureMonitor tempMonitor;
    @NonNls
    private final Database database;

    private int autoLockRemainingMinutes = 10;

    public Manager(@NonNls Database database,
                   @NonNls List<Module> modules,
                   @NonNls Bitcoin bitcoin,
                   @NonNls TemperatureMonitor tempMonitor) {
        modules.forEach(module -> this.modules.put(module.getId(), module));
        this.database = database;
        this.bitcoin = bitcoin;
        this.tempMonitor = tempMonitor;
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (autoLockRemainingMinutes == 1) {
                    Logger.d("Autolock triggered");
                    try {
                        lockWallet();
                    } catch (WalletNotInitialized ignored) {
                        //we don't care about locking if it wasn't even inited
                    }
                    timer.cancel();
                }
                --autoLockRemainingMinutes;

            }
        }, 60 * 1000 /* second */, 60 * 1000 /* second */);
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

    @Override
    public List<io.raspberrywallet.module.Module> getModules() {
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
    public void restoreFromBackupPhrase(@NotNull List<String> mnemonicCode, Map<String, Map<String,
            String>> selectedModulesWithInputs, int required) throws RequiredInputNotFound {

        List<Module> modulesToDecrypt = selectedModulesWithInputs.keySet().stream()
                .map(modules::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        bitcoin.restoreFromSeed(mnemonicCode);
        byte[] seed = String.join(" ", mnemonicCode).getBytes(Charsets.UTF_8);
        int numBits = seed.length * 8; //We need bits not bytes
        try {
            BigInteger[] params = Shamir.generateParams(required, numBits, seed);
            ShamirKey[] keys = Shamir.generateKeys(modulesToDecrypt.size(), required, numBits, params);

            WalletEntity walletEntity = new WalletEntity();
            List<KeyPartEntity> keyPartEntities = new ArrayList<>();
            for (int i = 0; i < keys.length; i++) {
                Module module = modulesToDecrypt.get(i);
                module.setInputs(selectedModulesWithInputs.get(module.getId()));
                KeyPartEntity keyPartEntity = new KeyPartEntity();
                keyPartEntity.payload = module.encrypt(keys[i].toByteArray());
                keyPartEntity.module = module.getId();
                keyPartEntities.add(keyPartEntity);
            }
            walletEntity.parts = keyPartEntities;
            database.saveWallet(walletEntity);

        } catch (ShamirException | IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public WalletStatus getWalletStatus() {
        try {
            return bitcoin.getWallet().isEncrypted() ?
                    WalletStatus.ENCRYPTED : WalletStatus.DECRYPTED;
        } catch (IllegalStateException | WalletNotInitialized e) {
            return WalletStatus.UNSET;
        }
    }

    @Override
    public void unlockWallet() throws WalletNotInitialized {
        byte[] privateKeyHash = Sha256Hash.hash(getPrivateKeyFromModules());
        KeyParameter key = new KeyParameter(privateKeyHash);
        try {
            bitcoin.getWallet().decrypt(key);
        } catch (KeyCrypterException | WalletNotInitialized e) {
            e.printStackTrace();
            throw e;
        } finally {
            Arrays.fill(key.getKey(), (byte) 0);
        }
    }

    @Override
    public boolean lockWallet() throws WalletNotInitialized {
        KeyCrypter keyCrypter = Optional
                .ofNullable(bitcoin.getWallet().getKeyCrypter())
                .orElseGet(KeyCrypterScrypt::new);

        byte[] privateKeyHash = Sha256Hash.hash(getPrivateKeyFromModules());
        KeyParameter key = new KeyParameter(privateKeyHash);

        try {
            Wallet wallet = bitcoin.getWallet();
            wallet.encrypt(keyCrypter, key);
            wallet.saveToFile(bitcoin.getWalletFile());
            return true;
        } catch (KeyCrypterException | IOException e) {
            Logger.err(e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            Arrays.fill(key.getKey(), (byte) 0);
            clearModules();
        }
    }

    private void clearModules() {
        modules.values().forEach(Module::clearInputs);
    }

    public byte[] getPrivateKeyFromModules() {
        ShamirKey[] shamirKeys = modules.values().stream()
                .map(module -> {
                    try {
                        Optional<KeyPartEntity> keyPartEntity = database.getKeypartForModuleId(module.getId());
                        if (!keyPartEntity.isPresent())  // could not find module with this module.getId()
                            return null;

                        KeyPartEntity dbEntity = keyPartEntity.get();
                        return module.decrypt(dbEntity.payload);
                    } catch (KeypartDecryptionException | RequiredInputNotFound e) {
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

    @Override
    public String getCurrentReceiveAddress() throws WalletNotInitialized {
        return bitcoin.getCurrentReceiveAddress();
    }

    @Override
    public String getFreshReceiveAddress() throws WalletNotInitialized {
        return bitcoin.getFreshReceiveAddress();
    }

    @Override
    public String getEstimatedBalance() throws WalletNotInitialized {
        return bitcoin.getEstimatedBalance();
    }

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

    @Override
    public String getCpuTemperature() {
        return tempMonitor.call();
    }

    @Override
    public void tap() {
        autoLockRemainingMinutes = 10;
    }

}
