package org.omegat;

import javax.swing.UIManager;

import org.omegat.filters2.master.PluginUtils;

public final class TestMainInitializer {

    private TestMainInitializer() {
    }

    public static void initClassloader() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        MainClassLoader mainClassLoader = (cl instanceof MainClassLoader) ? (MainClassLoader) cl
                : new MainClassLoader(cl);
        PluginUtils.getThemePluginJars().forEach(mainClassLoader::add);
        UIManager.put("ClassLoader", mainClassLoader);
    }

}
