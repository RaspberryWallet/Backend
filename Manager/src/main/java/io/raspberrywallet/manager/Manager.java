package io.raspberrywallet.manager;

import io.raspberrywallet.Response;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.module.ModuleState;
import org.bitcoinj.wallet.DeterministicSeed;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

public class Manager implements io.raspberrywallet.Manager {

    /**
     * Module id -> Module instance
     */
    private final ConcurrentHashMap<String, Module> modules = new ConcurrentHashMap<>();
    private final Bitcoin bitcoin;

    public Manager(Bitcoin bitcoin) {
        this.bitcoin = bitcoin;
    }

    public List<io.raspberrywallet.module.Module> getModules() {
        return modules.values().stream()
                .map(Module::asServerModule)
                .collect(toList());
    }

    void addModule(Module module) {
        modules.put(module.getId(), module);
    }

    protected Module getModule(String id) {
        return modules.getOrDefault(id, null);
    }

    @Override
    public byte[] getAddress() {
        return bitcoin.kit.wallet().currentReceiveAddress().getHash160();
    }

    @Override
    public ModuleState getModuleState(String id) {
        ModuleState state = ModuleState.FAILED;
        state.setMessage("Unknown module!");
        if (modules.containsKey(id)) {
            state = ModuleState.WAITING;
            state.setMessage(modules.get(id).getStatusString());
        }
        return state;
    }

    @Override
    public Response nextStep(String arg0, byte[] arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    public void restoreFromBackupPhrase(@NotNull List<String> mnemonicCode) {
        DeterministicSeed seed = new DeterministicSeed(mnemonicCode, null, "", 0L);
        bitcoin.kit.restoreWalletFromSeed(seed);
    }

}
