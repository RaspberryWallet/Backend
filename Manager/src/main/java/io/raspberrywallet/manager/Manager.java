package io.raspberrywallet.manager;

import io.raspberrywallet.Response;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.module.ModuleState;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toList;

public class Manager implements io.raspberrywallet.Manager {

    /**
     * Module id -> Module instance
     */
    private ConcurrentHashMap<String, Module> modules = new ConcurrentHashMap<>();

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
        // TODO Auto-generated method stub
        return null;
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
    public void restoreFromBackupPhrase(List<String> arg0) {
        // TODO Auto-generated method stub
    }

}
