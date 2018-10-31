package io.raspberrywallet.manager.modules.pin;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.raspberrywallet.manager.modules.ModuleConfig;

public class PinConfig implements ModuleConfig {
    @JsonAlias("max-retry")
    public int maxRetry = 3;
    @JsonAlias("min-length")
    public int minLength = 4;
    @JsonAlias("max-length")
    public int maxLength = 9;
}
