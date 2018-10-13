package io.raspberrywallet.manager.linux;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
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
    public Map<String, String> call() {
        HashMap<String, String> values = new HashMap<String, String>();
        try {

            Process process = Runtime.getRuntime().exec("iwconfig wlan0");
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while( (line = reader.readLine()) != null) {
                Matcher essidMatcher = essidPattern.matcher(line);
                Matcher macMatcher = macPattern.matcher(line);
                Matcher freqMatcher = freqPattern.matcher(line);
                Matcher qualityMatcher = qualityPattern.matcher(line);
                Matcher speedMatcher = speedPattern.matcher(line);
                if(essidMatcher.matches()) values.put("essid", essidMatcher.group(1));
                if(macMatcher.matches()) values.put("mac", macMatcher.group(1));
                if(freqMatcher.matches()) values.put("freq", freqMatcher.group(1));
                if(qualityMatcher.matches()) values.put("quality", qualityMatcher.group(1));
                if(speedMatcher.matches()) values.put("speed", speedMatcher.group(1));
            }

            process = Runtime.getRuntime().exec("ifconfig wlan0");
            process.waitFor();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            line = null;
            while( (line = reader.readLine()) != null) {
                Matcher ipMatcher = ipPattern.matcher(line);
                Matcher ipv6Matcher = ipv6Pattern.matcher(line);
                Matcher globalipMatcher = globalipPattern.matcher(line);

                if(ipMatcher.matches()) values.put("ip", ipMatcher.group(1));
                if(ipv6Matcher.matches()) values.put("ipv6", ipv6Matcher.group(1));
                if(globalipMatcher.matches()) values.put("globalip", globalipMatcher.group(1));
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return values;
    }
}
