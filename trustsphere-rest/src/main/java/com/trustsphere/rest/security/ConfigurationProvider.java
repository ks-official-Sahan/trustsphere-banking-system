package com.trustsphere.rest.security;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class ConfigurationProvider {

    private static final Logger LOGGER = Logger.getLogger(ConfigurationProvider.class.getName());
    private static final String CONFIG_FILE = "application.properties";

    private Properties properties;

    public ConfigurationProvider() {
        loadProperties();
    }

    private void loadProperties() {
        properties = new Properties();

        // Load from classpath
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
                LOGGER.info("Loaded configuration from " + CONFIG_FILE);
            } else {
                LOGGER.warning("Configuration file " + CONFIG_FILE + " not found in classpath");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error loading configuration file", e);
        }

        // Override with system properties
        properties.putAll(System.getProperties());
    }

    /**
     * Get property value with fallback
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    /**
     * Get property value
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
}