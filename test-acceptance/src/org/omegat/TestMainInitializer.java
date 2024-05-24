package org.omegat;

import javax.swing.UIManager;

public final class TestMainInitializer {

    private TestMainInitializer() {
    }

    public static void initClassloader() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        MainClassLoader mainClassLoader = (cl instanceof MainClassLoader) ? (MainClassLoader) cl
                : new MainClassLoader(cl);
        UIManager.put("ClassLoader", mainClassLoader);
    }

}
