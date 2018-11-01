package io.raspberrywallet.manager;

import com.stasbar.Logger;
import io.raspberrywallet.contract.WalletNotInitialized;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.cli.Opts;
import io.raspberrywallet.manager.cryptography.common.Password;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.DecryptionException;
import io.raspberrywallet.manager.cryptography.crypto.exceptions.EncryptionException;
import io.raspberrywallet.manager.database.Database;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.manager.modules.ModuleClassLoader;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.raspberrywallet.manager.cli.CliUtils.parseArgs;
import static io.raspberrywallet.server.KtorServerKt.startKtorServer;

public class Main {

    public static void main(String... args) throws IOException, DecryptionException, EncryptionException {
        CommandLine cmd = parseArgs(args);

        File yamlConfigFile = new File(Opts.CONFIG.getValue(cmd));
        Configuration configuration = Configuration.fromYamlFile(yamlConfigFile);

        Bitcoin bitcoin = new Bitcoin(configuration);

        List<Module> modules = ModuleClassLoader.getModules(configuration);
        modules.forEach(Module::register);

        TemperatureMonitor temperatureMonitor = new TemperatureMonitor();

        //TODO change this to read from user
        Password password = new Password("changeme".toCharArray());

        Database db = new Database(configuration, password);

        Manager manager = new Manager(db, modules, bitcoin, temperatureMonitor);

        startKtorServer(manager);

        prepareShutdownHook(bitcoin, manager);
    }

    private static void prepareShutdownHook(Bitcoin bitcoin, Manager manager) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Logger.info("Finishing...");
            try {
                if (manager.lockWallet()) Logger.info("Wallet Encrypted");
                else Logger.err("Failed Wallet Encryption");

                bitcoin.getKit().stopAsync();
                bitcoin.getKit().awaitTerminated(3, TimeUnit.SECONDS);
            } catch (NullPointerException e) {
                Logger.err("Failed Wallet Encryption");
                e.printStackTrace();
            } catch (WalletNotInitialized walletNotInitialized) {
                Logger.d("Wallet was not inited so there is nothing to encrypt");
                walletNotInitialized.printStackTrace();
            } catch (TimeoutException e) {
                Logger.err(e.getMessage());
            }
            // Forcibly terminate the JVM because Orchid likes to spew non-daemon threads everywhere.
            Runtime.getRuntime().exit(0);
        }));
    }
}
