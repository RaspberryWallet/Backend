package io.raspberrywallet.manager;

import com.stasbar.Logger;
import io.raspberrywallet.Response;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.Shamir;
import io.raspberrywallet.manager.cryptography.sharedsecret.shamir.ShamirKey;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.module.ModuleState;
import org.bitcoinj.core.Base58;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

public class Manager implements io.raspberrywallet.Manager {

    /**
     * Module id -> Module instance
     */
    private final ConcurrentHashMap<String, Module> modules = new ConcurrentHashMap<>();
    private final Bitcoin bitcoin;
    private final TemperatureMonitor tempMonitor;


    public Manager(List<Module> modules, Bitcoin bitcoin, TemperatureMonitor tempMonitor) {
        modules.forEach(module -> this.modules.put(module.getId(), module));
        this.bitcoin = bitcoin;
        this.tempMonitor = tempMonitor;
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
    public boolean unlockWallet() {
        ShamirKey[] shamirKeys = modules.values().stream().map(module -> {
            try {
                return module.getResult();
            } catch (Module.DecryptionException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull)
                .map(ShamirKey::fromByteArray).toArray(ShamirKey[]::new);
        byte[] privateKey = Shamir.calculateLagrange(shamirKeys);
        Logger.d("Private key restored: " + Base58.encode(privateKey));
        bitcoin.importKey(privateKey);
        Arrays.fill(privateKey, (byte) 0);
        modules.values().forEach(Module::destroy);
        return true;
    }

    /*
     * Bitcoin Domain
     */

    @Override
    public void restoreFromBackupPhrase(@NotNull List<String> mnemonicCode) {
        bitcoin.restoreFromBackupPhrase(mnemonicCode);
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

    /*
     * Utilities
     */

    @Override
    public String getCpuTemperature() {
        return tempMonitor.call();
    }

}
