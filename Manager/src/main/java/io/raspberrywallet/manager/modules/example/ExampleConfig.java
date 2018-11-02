package io.raspberrywallet.manager.modules.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.raspberrywallet.manager.modules.ModuleConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExampleConfig implements ModuleConfig {
    private String example = "example";
    private String name = "name";
}
