package io.raspberrywallet.manager;

import java.util.List;

public class Configuration {
    private long sessionLength = 3600000;
    private String name = "Staszek";
    private List<String> modules;

    public long getSessionLength() {
        return sessionLength;
    }

    public String getName() {
        return name;
    }

    public List<String> getModules() {
        return modules;
    }
}
