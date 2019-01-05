package io.raspberrywallet.manager.modules.pushbutton;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.raspberrywallet.manager.modules.ModuleConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PushButtonConfig implements ModuleConfig {
    private int pin = 4;
}