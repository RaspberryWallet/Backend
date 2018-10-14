package io.raspberrywallet.manager;

import io.raspberrywallet.Response;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.Shamir;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.ShamirException;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.ShamirKey;
import io.raspberrywallet.manager.database.Database;
import io.raspberrywallet.manager.database.KeyPartEntity;
import io.raspberrywallet.manager.database.WalletEntity;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.module.ModuleState;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

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
                    lockWallet();
                    timer.cancel();
                }
                --autoLockRemainingMinutes;

            }
        }, 60000, 60000);
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
    public String getModuleUi(@NotNull String moduleId) {
        return modules.get(moduleId).getHtmlUi();
    }


    void addModule(Module module) {
        modules.put(module.getId(), module);
    }

    protected Module getModule(String id) {
        return modules.getOrDefault(id, null);
    }

    @Override
    public boolean isLocked() {
        try {
            bitcoin.kit.wallet();
        } catch (IllegalStateException e) {
            return true;
        }
        return false;
    }

    @Override
    public boolean unlockWallet() {
        ShamirKey[] shamirKeys = modules.values().stream()
                .map(module -> {
                    try {
                        KeyPartEntity dbEntity = database.getKeypartForModuleId(module.getId()).get();
                        return module.decrypt(dbEntity.payload);
                    } catch (Module.DecryptionException e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(ShamirKey::fromByteArray)
                .toArray(ShamirKey[]::new);

        byte[] privateKey = Shamir.calculateLagrange(shamirKeys);
        bitcoin.restoreFromSeed(privateKey.clone());
        Arrays.fill(privateKey, (byte) 0);
        return true;
    }

    @Override
    public boolean lockWallet() {
        //todo not sure if it works
        bitcoin.kit.wallet().reset();
        modules.values().forEach(Module::destroy);
        return true;
    }

    /*
     * Bitcoin Domain
     */

    @Override
    public void restoreFromBackupPhrase(@NotNull List<String> mnemonicCode, Map<String, Map<String, String>> selectedModulesWithInputs, int required) {

        List<Module> modulesToDecrypt = selectedModulesWithInputs.keySet().stream()
                .map(modules::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        byte[] seed = bitcoin.restoreFromSeed(mnemonicCode);

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
    public String getCurrentReceiveAddress() {
        return bitcoin.getCurrentReceiveAddress();
    }

    @Override
    public String getFreshReceiveAddress() {
        return bitcoin.getFreshReceiveAddress();
    }

    @Override
    public String getEstimatedBalance() {
        return bitcoin.getEstimatedBalance();
    }

    @Override
    public String getAvailableBalance() {
        return bitcoin.getAvailableBalance();
    }

    @Override
    public void sendCoins(@NotNull String amount, @NotNull String recipientAddress) {
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
