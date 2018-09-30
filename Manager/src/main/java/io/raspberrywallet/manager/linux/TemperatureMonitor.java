package io.raspberrywallet.manager.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;
import java.util.regex.Pattern;

public class TemperatureMonitor extends Executable {

    public TemperatureMonitor() {
        this.id = "435a06ba-b2b7-4de8-843c-f5f836c2a523";
    }

    /**
     * Retrieves device temperature
     * @return should return device temperature in milicelsius or 'undefined'
     */

    @Override
    public String run() {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("cat /sys/class/thermal/thermal_zone0/temp");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = "";
        String output = "undefined";
        Pattern pattern = Pattern.compile("^[0-9]+$");
        try {
            while ( (line = reader.readLine()) != null ) {
                if(pattern.matcher(line).matches()) {
                    output = line;
                    break;
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }
}
