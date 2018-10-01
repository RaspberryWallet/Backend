package io.raspberrywallet.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.raspberrywallet.manager.bitcoin.Bitcoin;
import io.raspberrywallet.manager.database.MockDatabaseFactory;
import io.raspberrywallet.manager.linux.TemperatureMonitor;
import io.raspberrywallet.manager.modules.ExampleModule;
import io.raspberrywallet.manager.modules.Module;

public class ExampleMockManager extends Manager {

    public ExampleMockManager(Bitcoin bitcoin, TemperatureMonitor tempMonitor) {
        super(bitcoin, tempMonitor);
        Module mod = new ExampleModule();
        addModule(mod);

        try {
            byte[] json = MockDatabaseFactory.getInstance()
                    .placeKeyPart(new byte[]{1, 2, 3, 4, 5, 6}, 0)
                    .placeKeyPart(new byte[]{100, 101, 102, 103, -101, -102, -103}, 1)
                    .pushWallet()
                    .placeKeyPart(new byte[]{9, 10, 11}, 0)
                    .placeKeyPart(new byte[]{11, 10, 9, 8, 7}, 1)
                    .pushWallet()
                    .getDatabase().getSerialized();
            System.out.println("database: " + new String(json));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println(mod);
    }

}
