package io.raspberrywallet.manager.linux;

import io.raspberrywallet.manager.linux.exceptions.TemperatureNotFoundException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WPAConfiguration {

    public static final int WPA_OK = 0;
    public static final int WPA_NOSSID = 1;
    public static final int WPA_NOPSK = 2;
    public static final int WPA_PSKFAILURE = 4;
    public static final int WPA_SAVEFAILURE = 8;


    String ssid = "";
    String PSK = "";
    File configFile;

    public WPAConfiguration() {
        configFile = new File("/opt/wallet/wpa_supplicant.conf");
        if(configFile.exists()) {
            load();
        }
    }

    /**
     * Loads current Wi-Fi configuration from a system file.
     */
    public void load() {
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

    /**
     * Saves current configuration to file which will be loaded after restart.
     */
    public void save() {
        String config = "country=GB\n" +
                "ctrl_interface=DIR=/var/run/wpa_supplicant GROUP=netdev\n" +
                "update_config=1\n" +
                "\n" +
                "network={\n" +
                "    ssid=\""+ssid+"\"\n" +
                "    scan_ssid=1\n" +
                "    #key_mgmt=WPA-PSK\n" +
                "    psk="+PSK+"\n" +
                "}\n";
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
        Process p = Runtime.getRuntime().exec(new String[] {"/usr/bin/wpa_passphrase", ssid, preSharedKey});
        p.waitFor();
        Pattern pattern = Pattern.compile("^[\t\\s]*psk=([0-9A-Fa-f]+)\\s*$");
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

    /**
     * Applies current configuration. Should reconnect to the network.
     * @throws IOException - something bad happened
     */
    public void apply() throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec("/bin/sh -c /opt/wallet/tools/wpaconfigure.sh");
        p.waitFor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line = null;
        while( (line = reader.readLine()) != null) {
            System.out.println(this.getClass().getSimpleName() + "::apply() "+line);
        }
        reader.close();
    }

    /**
     * Shorthand for WPAConfiguration::save() and WPAConfiguration::apply()
     */
    public void saveAndApply() throws IOException, InterruptedException {
        save();
        apply();
    }

    /**
     * Gets current SSID of connected network.
     * @return - ESSID of the network.
     */
    public String getSsid() {
        return ssid;
    }

    public Map<String, String> getAsMap() {
        Map<String, String> config = new HashMap<>();
        config.put("ssid", getSsid());
        config.put("psk", PSK.substring( 0, (int)Math.ceil(PSK.length()*0.2) ) ); //Uncover only 20% of the PSK
        return config;
    }

    public int setFromMap(Map<String, String> params) {

        if( params.containsKey("ssid") && params.get("ssid") != null ) {
            setSSID(params.get("ssid"));
        } else return WPA_NOSSID;

        if( params.containsKey("psk") && params.get("psk") != null ) {
            try {
                setPSK(params.get("psk"));
            } catch(Exception e) {
                System.err.println(getClass().getCanonicalName()+"::setFromMap "+e.getMessage());
                return WPA_PSKFAILURE;
            }
        } else return WPA_NOPSK;

        try {
            saveAndApply();
        } catch (Exception e) {
            System.err.println(getClass().getCanonicalName()+"::setFromMap "+e.getMessage());
            return WPA_SAVEFAILURE;
        }

        return WPA_OK;
    }
}
