package io.raspberrywallet.manager.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

public enum Opts {
    SERVER(new Option("server", true, "HTTP Server impl"), "ktor"),
    KTOR(new Option("ktor", "Use Ktor as HTTP Server impl")),
    VERTX(new Option("vertx", "Use VertX as HTTP Server impl")),
    MODULES(new Option("modules", true, "Modules classes directory path"), "modules"),
    SYNC(new Option("sync", "Sync bitcoin blockchain"));

    public final Option option;
    public final String def;

    Opts(Option option) {
        this(option, null);
    }

    Opts(Option option, String def) {
        this.option = option;
        this.def = def;
    }

    public boolean isSet(CommandLine cmd) {
        return cmd.hasOption(this.option.getOpt());
    }

    public String getValue(CommandLine cmd) {
        return cmd.getOptionValue(this.option.getOpt(), def);
    }
}