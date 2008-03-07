package org.omegat.gui.main;

import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import junit.framework.TestCase;

/**
 * @author Alex Buloichik
 */
public class MainWindowMenuTest extends TestCase {
    /**
     * Check MainWindow for all menu items action handlers exist.
     * 
     * @throws Exception
     */
    public void testMenuActions() throws Exception {
        int count = 0;

        Map<String, Method> existsMethods = new HashMap<String, Method>();

        for (Method m : MainWindow.class.getDeclaredMethods()) {
            if (Modifier.isProtected(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())
                    && m.getParameterTypes().length == 1 && m.getParameterTypes()[0] == ActionEvent.class) {
                existsMethods.put(m.getName(), m);
            }
        }

        for (Field f : MainWindowMenu.class.getDeclaredFields()) {
            if (JMenuItem.class.isAssignableFrom(f.getType()) && f.getType() != JMenu.class) {
                count++;
                String actionMethodName = f.getName() + "ActionPerformed";
                Method m = existsMethods.remove(actionMethodName);
                assertNotNull("Action method not defined for " + f.getName(), m);
            } else {
                System.out.println("Action not need for " + f);
            }
        }
        assertTrue("menu items not found", count > 30);
        assertTrue("There is action handlers in MainWindow which doesn't used in menu: "+existsMethods.keySet(), existsMethods.isEmpty());
    }
}
