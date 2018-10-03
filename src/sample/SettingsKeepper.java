package sample;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class SettingsKeepper {

    private static final String HATH_TO_PROPERTIES = "src/sample/resources/config.properties";

    public static void saveSettings(HashMap<String, String> settingsMap) {
        Properties properties = new Properties();
        for(HashMap.Entry<String, String> entry: settingsMap.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        try {
            FileOutputStream fos = new FileOutputStream(SettingsKeepper.HATH_TO_PROPERTIES);
            properties.store(fos, "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static HashMap<String, String> loadSettings() {
        HashMap<String, String> settingsMap = new HashMap<String, String>();
        try {
            FileInputStream fis = new FileInputStream(SettingsKeepper.HATH_TO_PROPERTIES);
            Properties properties = new Properties();
            properties.load(fis);
            for(String propKey: properties.stringPropertyNames()){
                settingsMap.put(propKey, properties.getProperty(propKey));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return settingsMap;
    }
}
