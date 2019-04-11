package com.linghang.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtil {

    private Properties properties;

    public PropertiesUtil(String propertyName) {
        this.properties = new Properties();
        InputStream in;
        try {
            // use relative path
            in = new BufferedInputStream(new FileInputStream("src\\main\\resources\\" + propertyName));
            this.properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getValue (String key) {
        return properties.getProperty(key);
    }

}
