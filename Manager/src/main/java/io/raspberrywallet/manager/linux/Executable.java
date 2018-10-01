package io.raspberrywallet.manager.linux;

import java.util.UUID;

public abstract class Executable {

    protected String id = UUID.randomUUID().toString();

    /**
     * Runs system application as user 'wallet'
     *
     * @return returns output of the application run
     */
    public abstract String run();

    public String getName() {
        return getClass().getName() + "-" + id;
    }
}
