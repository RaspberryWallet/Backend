package io.raspberrywallet;

public class RequiredInputNotFound extends Throwable {
    public RequiredInputNotFound(String moduleName, String inputName) {
        super("Could not find input: " + inputName + " for module: " + moduleName);
    }
}
