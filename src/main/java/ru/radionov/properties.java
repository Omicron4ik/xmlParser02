package ru.radionov;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class properties {

    static String configFile,baseUrl,apiKey,nevativeResultFile,positiveResultFile,oktmo,deviceType,adapterName,limit;

    public static void loadConfig () throws IOException {

        FileInputStream inputStream = new FileInputStream("src/main/resources/config.properties");

        Properties prop = new Properties();
        prop.load(inputStream);

        baseUrl = prop.getProperty("geocodingUrl");
        apiKey = prop.getProperty("yandexApiAuthToken");
        nevativeResultFile = prop.getProperty("nevativeResultFile");
        positiveResultFile = prop.getProperty("positiveResultFile");
        oktmo = prop.getProperty("oktmo");
        deviceType = prop.getProperty("deviceType");
        adapterName = prop.getProperty("adapterName");
        configFile = prop.getProperty("configFile");
        limit = prop.getProperty("limit");

    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getNevativeResultFile() {
        return nevativeResultFile;
    }

    public String getPositiveResultFile() {
        return positiveResultFile;
    }

    public String getOktmo() {
        return oktmo;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getAdapterName() {
        return adapterName;
    }

    public String getConfigFile() {
        return configFile;
    }

    public String getLimit() {
        return limit;
    }
}
