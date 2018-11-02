package io.raspberrywallet.manager.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;

import java.nio.file.Paths;

public enum Opts {
    CONFIG(new Option("config", true, "Config file path"),
            Paths.get("config.yaml").toAbsolutePath().toString());

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