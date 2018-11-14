package io.raspberrywallet.manager.commons;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.raspberrywallet.manager.Configuration;

import java.io.IOException;

public abstract class CustomConfig {
    
    public static Configuration.ModulesConfiguration getConfig(String moduleName,  String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json);
    
        Configuration.ModulesConfiguration config = new Configuration.ModulesConfiguration();
        config.put(moduleName, jsonNode);
        return config;
    }
    
}
