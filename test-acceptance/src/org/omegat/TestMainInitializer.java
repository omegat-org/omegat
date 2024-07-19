package org.omegat;

import javax.swing.UIManager;

import org.omegat.filters2.master.PluginUtils;

public final class TestMainInitializer {

    private TestMainInitializer() {
    }

    public static void initClassloader() {
        UIManager.put("ClassLoader", PluginUtils.getThemeClassLoader());
    }

}
