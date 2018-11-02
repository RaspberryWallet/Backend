package io.raspberrywallet.manager.session;

import io.raspberrywallet.manager.Configuration;
import io.raspberrywallet.manager.common.ArrayDestroyer;
import io.raspberrywallet.manager.common.interfaces.Destroyable;

import java.security.SecureRandom;
import java.util.Date;

public class SessionToken implements Destroyable, Runnable {
    private final Configuration configuration;
    private final static int SESSION_KEY_LENGTH = 32;

    private UserCredentials userCredentials;
    private char[] sessionKey = new char[SESSION_KEY_LENGTH];
    private Date sessionValidUntil;
    private boolean isDestroyed = false;

    public SessionToken(UserCredentials userCredentials, Configuration configuration) {
        this.configuration = configuration;
        this.userCredentials = userCredentials;
        SecureRandom secureRandom = new SecureRandom();
        byte[] tempSessionKey = new byte[SESSION_KEY_LENGTH];
        secureRandom.nextBytes(tempSessionKey);
        for (int i = 0; i < SESSION_KEY_LENGTH; i++)
            sessionKey[i] = (char)tempSessionKey[i];
            
        refresh();
        
    }
    
    public UserCredentials getUserCredentials() {
        return userCredentials;
    }
    
    public Date getSessionValidUntil() {
        return sessionValidUntil;
    }
    
    public char[] getSessionKey() {
        return sessionKey;
    }
    
    public void refresh() {
        Date currentDate = new Date();
        sessionValidUntil = new Date(currentDate.getTime() + configuration.getSessionLength());
    }
    
    public boolean isSessionValid() {
        Date currentDate = new Date();
        return currentDate.getTime() <= sessionValidUntil.getTime();
    }
    
    synchronized public void destroy() {
        userCredentials = null;
        ArrayDestroyer.destroy(sessionKey);
        sessionValidUntil = new Date(0);
        isDestroyed = true;
        notifyAll();
    }
    
    @Override
    public void run() {
        try {
            while (isSessionValid()) {
                Thread.sleep(1000);
            }
            destroy();
        } catch (InterruptedException ignored) {
            if (!isDestroyed)
                destroy();
        }
    }
}
