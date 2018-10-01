package io.raspberrywallet.manager.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

public class TemperatureMonitor extends Executable {

    public TemperatureMonitor() {
        this.id = "435a06ba-b2b7-4de8-843c-f5f836c2a523";
    }

    /**
     * Retrieves device temperature
     *
     * @return should return device temperature in milicelsius or 'undefined'
     */

    @Override
    public String run() {
        try {
            Process p = Runtime.getRuntime().exec("cat /sys/class/thermal/thermal_zone0/temp");
            p.waitFor();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                Pattern pattern = Pattern.compile("^[0-9]+$");
                String line;
                while ((line = reader.readLine()) != null)
                    if (pattern.matcher(line).matches())
                        return line;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "error";
        }
        return "undefined";

    }
}
