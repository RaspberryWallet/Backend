package io.raspberrywallet.manager;

import io.raspberrywallet.Response;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.module.ModuleState;
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

    @Override
    public String ping() {
        return "pong";
    }

    @Override
    public Response nextStep(@NotNull String moduleId, byte[] input) {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Modules
     */

    @Override
    public List<io.raspberrywallet.module.Module> getModules() {
        return modules.values().stream()
                .map(Module::asServerModule)
                .collect(toList());
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

    void addModule(Module module) {
        modules.put(module.getId(), module);
    }

    protected Module getModule(String id) {
        return modules.getOrDefault(id, null);
    }


    /**
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

    /**
     * System utilities (Linux)
     */

    /**
     * Gets temperature of the CPU
     * @return temperature as string in Celsius
     */
    public String getCPUTemperature() {
        String val = new TemperatureMonitor().run();
        float value = Float.parseFloat(val);
        if( Float.isNaN(value) )
            return "undefined";
        else
            return String.format("%.3f", value/1000);
    }

}
