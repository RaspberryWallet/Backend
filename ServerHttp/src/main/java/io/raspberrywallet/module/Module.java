package io.raspberrywallet.module;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class Module {
    @NotNull
    private final String id;
    @NotNull
    private final String name;
    @NotNull
    private final String description;
    @Nullable
    private final String htmlUi;

    public Module(@NotNull String name, @NotNull String description, @Nullable String htmlUi) {
        this(UUID.randomUUID().toString(), name, description, htmlUi);
    }

    public Module(@NotNull String id, @NotNull String name, @NotNull String description, @Nullable String htmlUi) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.htmlUi = htmlUi;
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

    @Nullable
    public String getHtmlUi() {
        return htmlUi;
    }
}
