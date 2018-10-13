package io.raspberrywallet.manager.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class WifiStatus extends Executable<Map<String, String>> {

    Pattern essidPattern = Pattern.compile("^.*ESSID:\"(.*)\"$");
    Pattern macPattern = Pattern.compile("^.*Access Point: ([0-9:]+)$");
    Pattern qualityPattern = Pattern.compile("^.*Link Quality=([0-9]+/[0-9]+) .*$");
    Pattern freqPattern = Pattern.compile("^.*Frequency:([0-9.]+ GHz).*$");
    Pattern speedPattern = Pattern.compile("^.*Bit Rate=([0-9]+ M|Gb/s).*$");
    Pattern ipPattern = Pattern.compile("^.*inet ([0-9.]+) .*$");
    Pattern ipv6Pattern = Pattern.compile("^.*inet6 ([0-9a-fA-F:]+).*<link>$");
    Pattern globalipPattern = Pattern.compile("^.*inet6 ([0-9a-fA-F:]+).*<global>$");

    /**
     * returned values:
     * - essid - name of the network currently connected to
     * - mac - access point MAC address
     * - quality - quality of the connection in X/Y
     * - freq -  frequency of the link
     * - speed - speed of the link in Mb/s
     * - ip - ip address
     * - ipv6 - ipv6 address in link scope
     * - globalip - ipv6 in global scope
     * @return
     * @throws Exception
     */
    @Override
    public Map<String, String> call() throws Exception {
        HashMap<String, String> values = new HashMap<String, String>();
        try {
            Process process = Runtime.getRuntime().exec("iwconfig wlan0");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while( (line = reader.readLine()) != null) {

            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return values;
    }
}
