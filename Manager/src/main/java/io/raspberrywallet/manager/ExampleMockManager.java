package io.raspberrywallet.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.database.Database;
import io.raspberrywallet.manager.database.MockDatabaseFactory;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.modules.ExampleModule;
import io.raspberrywallet.manager.modules.Module;
import io.raspberrywallet.manager.modules.PinModule;

import java.util.List;

public class ExampleMockManager extends Manager {

    public ExampleMockManager(Database database, List<Module> modules, Bitcoin bitcoin, TemperatureMonitor tempMonitor) {
        super(database, modules, bitcoin, tempMonitor);
        Module mod = new ExampleModule();
        addModule(mod);
        Module pinModule = new PinModule();
        addModule(pinModule);

        try {
            byte[] json = MockDatabaseFactory.getInstance()
                    .placeKeyPart(new byte[]{1, 2, 3, 4, 5, 6})
                    .placeKeyPart(new byte[]{100, 101, 102, 103, -101, -102, -103})
                    .pushWallet()
                    .placeKeyPart(new byte[]{9, 10, 11})
                    .placeKeyPart(new byte[]{11, 10, 9, 8, 7})
                    .pushWallet()
                    .getDatabase().getSerialized();
            System.out.println("database: " + new String(json));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println(mod);
    }

}
