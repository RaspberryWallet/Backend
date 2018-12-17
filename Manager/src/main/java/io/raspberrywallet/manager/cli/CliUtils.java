package io.raspberrywallet.manager.cli;

import org.apache.commons.cli.*;

public class CliUtils {
    public static CommandLine parseArgs(String[] args) {
        Options options = new Options() {{
            addOption(Opts.CONFIG.option);
            addOption(Opts.MOCK_MANAGER.option);
        }};
        HelpFormatter helpFormatter = new HelpFormatter();
        DefaultParser parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            e.printStackTrace();
            helpFormatter.printHelp("java -jar Manager.jar", options, true);
            System.exit(0);
            return null;
        }
    }
}
