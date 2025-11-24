package org.omegat.gui.firsttime;

import java.util.ResourceBundle;

public final class FirstTimeConfigurationWizardUtil {

    private static final ResourceBundle bundle = ResourceBundle.getBundle("org.omegat.gui.firsttime.Bundle");

    private FirstTimeConfigurationWizardUtil() {
    }

    static String getString(String key, String deflt) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return deflt;
        }
    }
}
