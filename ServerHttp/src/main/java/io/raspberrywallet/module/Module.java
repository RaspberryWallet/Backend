package io.raspberrywallet.module;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class Module {
    @NotNull private final String id = UUID.randomUUID().toString();
    @NotNull private final String name;
    @NotNull private final String description;

    public Module(@NotNull String name, @NotNull String description) {
        this.name = name;
        this.description = description;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getDescription() {
        return description;
    }
}
