package io.raspberrywallet.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.raspberrywallet.Response;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.module.ModuleState;

public class Manager implements io.raspberrywallet.Manager {

	private HashMap<String, Module> modules = new HashMap<String, Module>();
	
	public List<io.raspberrywallet.module.Module> getModules() {
		ArrayList<io.raspberrywallet.module.Module> list = new ArrayList<io.raspberrywallet.module.Module>();
		for(Module m:modules.values())
			list.add(m.asServerModule());
		return list;
	}
	
	protected void addModule(Module module) {
		modules.put(module.getId(), module);
	}
	
	protected Module getModule(String id) {
		if(modules.containsKey(id))
			return modules.get(id);
		return null;
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
		if(modules.containsKey(id)) {
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void restoreFromBackupPhrase(List<String> arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
