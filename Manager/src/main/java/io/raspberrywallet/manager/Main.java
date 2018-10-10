package io.raspberrywallet.manager;

import com.google.common.util.concurrent.Service;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.cli.Opts;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.manager.modules.ModuleClassLoader;
import io.raspberrywallet.server.Server;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.util.List;

import static io.raspberrywallet.ktor.KtorServerKt.startKtorServer;
import static io.raspberrywallet.manager.cli.CliUtils.parseArgs;

public class Main {

    public static void main(String... args) {
        CommandLine cmd = parseArgs(args);

        Bitcoin bitcoin = new Bitcoin();
        if (Opts.SYNC.isSet(cmd)) {
            Service blockchainSyncing = bitcoin.startBlockchainAsync();
            Runtime.getRuntime().addShutdownHook(new Thread(blockchainSyncing::stopAsync));
        }

        File modulesDir = new File(Opts.MODULES.getValue(cmd));
        List<Module> modules = ModuleClassLoader.getModulesFrom(modulesDir);

        TemperatureMonitor temperatureMonitor = new TemperatureMonitor();

        Manager manager = new ExampleMockManager(modules, bitcoin, temperatureMonitor);

        if (Opts.VERTX.isSet(cmd) || Opts.SERVER.getValue(cmd).equals(Opts.VERTX.name()))
            new Server(manager).start();
        else
            startKtorServer(manager);
    }





}
