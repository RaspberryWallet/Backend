package io.raspberrywallet.module;

public enum ModuleState {
    READY, WAITING, AUTHORIZED, FAILED;

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
