package org.omegat;

import java.awt.Toolkit;
import java.lang.reflect.Field;

import javax.swing.UIManager;

import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Log;

import com.vlsolutions.swing.docking.DockingDesktop;

public final class TestMainInitializer {

    private TestMainInitializer() {
    }

    public static void initClassloader() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        MainClassLoader mainClassLoader = (cl instanceof MainClassLoader) ? (MainClassLoader) cl
                : new MainClassLoader(cl);
        PluginUtils.getThemePluginJars().forEach(mainClassLoader::add);
        UIManager.put("ClassLoader", mainClassLoader);

        Log.logInfoRB("STARTUP_GUI_DOCKING_FRAMEWORK", DockingDesktop.getDockingFrameworkVersion());

        // Set X11 application class name to make some desktop user interfaces
        // (like Gnome Shell) recognize OmegaT
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Class<?> cls = toolkit.getClass();
        try {
            if (cls.getName().equals("sun.awt.X11.XToolkit")) {
                Field field = cls.getDeclaredField("awtAppClassName");
                if (field.trySetAccessible()) {
                    field.set(toolkit, "OmegaT");
                }
            }
        } catch (Exception ignored) {
        }

    }

}
