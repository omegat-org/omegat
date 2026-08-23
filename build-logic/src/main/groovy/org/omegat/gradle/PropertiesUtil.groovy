package org.omegat.gradle

class PropertiesUtil {
    static Properties loadProperties(File propFile) {
        def config = new Properties()
        if (propFile.canRead()) {
            propFile.withInputStream { config.load(it) }
        }
        config
    }
}
