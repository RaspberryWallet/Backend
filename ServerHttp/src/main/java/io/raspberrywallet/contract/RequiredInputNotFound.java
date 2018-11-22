package io.raspberrywallet.contract;

public class RequiredInputNotFound extends Exception {
    
    public RequiredInputNotFound(String moduleName, String inputName) {
        super("Could not find input: " + inputName + " for module: " + moduleName);
    }
    
    public RequiredInputNotFound(String msg) {
        super(msg);
    }
    
}
