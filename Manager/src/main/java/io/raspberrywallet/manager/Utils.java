package io.raspberrywallet.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.File;
import java.io.IOException;

public class Utils {

    public static Configuration processConfiguration(ObjectMapper mapper, String path) {
        File configFile = new File(path);
        Configuration config;
        try {
            config = mapper.readValue(configFile, Configuration.class);
        } catch (IOException e) {
            e.printStackTrace();
            config = new Configuration();
        }
        println(ReflectionToStringBuilder.toString(config, ToStringStyle.MULTI_LINE_STYLE));
        return config;
    }

    /**
     * Aliases
     */
    public static void println() {
        System.out.println();
    }

    public static void println(boolean it) {
        System.out.println(it);
    }

    public static void println(float it) {
        System.out.println(it);
    }

    public static void println(double it) {
        System.out.println(it);
    }

    public static void println(long it) {
        System.out.println(it);
    }

    public static void println(char it) {
        System.out.println(it);
    }

    public static void println(int it) {
        System.out.println(it);
    }

    public static void println(String it) {
        System.out.println(it);
    }

    public static void println(Object it) {
        System.out.println(it);
    }


}
