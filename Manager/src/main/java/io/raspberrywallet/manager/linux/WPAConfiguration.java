package io.raspberrywallet.manager.linux;

import io.raspberrywallet.manager.linux.exceptions.TemperatureNotFoundException;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WPAConfiguration {

    String ssid = "";
    String PSK = "";
    File configFile;

    public WPAConfiguration() {
        configFile = new File("/opt/wallet/wpa_supplicant.conf");
        if(configFile.exists()) {
            loadConfiguration();
        }
    }

    public void loadConfiguration() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(configFile));
            String line;
            Pattern pskPattern = Pattern.compile("^[\t\\s]*psk=([0-9A-Fa-f]+)$");
            Pattern ssidPattern = Pattern.compile("^[\t\\s]*ssid=\"(.*)\"$");
            while( (line = bufferedReader.readLine()) != null) {
                Matcher pskMatcher = pskPattern.matcher(line);
                Matcher ssidMatcher = ssidPattern.matcher(line);
                if(pskMatcher.matches()) {
                    PSK = pskMatcher.group(1);
                } else if(ssidMatcher.matches()) {
                    ssid = ssidMatcher.group(1);
                }
            }
            bufferedReader.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void saveConfiguration() {
        String config = "network={\n\tssid=\""+ssid+"\"\npsk="+PSK+"\n}";
        try {
            OutputStream os = new FileOutputStream(configFile);
            os.write(config.getBytes());
            os.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets encrypted key for the network
     * @param preSharedKey - key for this network
     * @throws IOException
     * @throws InterruptedException
     */
    public void setPSK(String preSharedKey) throws IOException, InterruptedException {
        preSharedKey = preSharedKey.replace("\"", "\\\"");
        Process p = Runtime.getRuntime().exec("/usr/bin/wpa_passphrase \""+ssid+"\" \""+preSharedKey+"\"");
        p.waitFor();
        Pattern pattern = Pattern.compile("^[\t\\s]*psk=([0-9A-Fa-f]+)$");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches())
                    PSK = matcher.group(1);
            }
        }
    }

    /**
     * Sets ssid for the network
     * @param ssid - name of the wireless network
     */
    public void setSSID(String ssid) {
        this.ssid = ssid;
    }

    public void applyConfiguration() throws IOException {
        Runtime.getRuntime().exec("/bin/sh -c /opt/wallet/wpaconfigure.sh");
    }

    public String getSsid() {
        return ssid;
    }

}
