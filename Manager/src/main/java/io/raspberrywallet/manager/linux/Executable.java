package io.raspberrywallet.manager.linux;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * Runs system application as user 'wallet'
 */
public abstract class Executable<T> implements Callable<T> {

    String id = UUID.randomUUID().toString();

    public String getName() {
        return getClass().getName() + "-" + id;
    }
}
