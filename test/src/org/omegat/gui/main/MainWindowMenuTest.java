/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.main;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.util.StaticUtils;

/**
 * @author Alex Buloichik
 */
public class MainWindowMenuTest extends TestCore {
    /**
     * Check MainWindow for all menu items action handlers exist.
     *
     * @throws Exception
     */
    @Test
    public void testMenuActions() throws Exception {
        int count = 0;

        Map<String, Method> existsMethods = new HashMap<String, Method>();

        for (Method m : MainWindowMenuHandler.class.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && !Modifier.isStatic(m.getModifiers())) {
                Class<?>[] params = m.getParameterTypes();
                if (params.length == 0) {
                    existsMethods.put(m.getName(), m);
                }
                // Include menu items that take a modifier key.
                if (params.length == 1 && params[0] == Integer.TYPE) {
                    existsMethods.put(m.getName(), m);
                }
            }
        }

        for (Field f : StaticUtils.getAllModelFields(MainWindowMenu.class)) {
            if (JMenuItem.class.isAssignableFrom(f.getType()) && f.getType() != JMenu.class) {
                count++;
                String actionMethodName = f.getName() + "ActionPerformed";
                Method m;
                try {
                    m = MainWindowMenuHandler.class.getMethod(actionMethodName);
                } catch (NoSuchMethodException ignore) {
                    // See if the method accepts a modifier key argument.
                    m = MainWindowMenuHandler.class.getMethod(actionMethodName, Integer.TYPE);
                }
                assertNotNull("Action method not defined for " + f.getName(), m);
                assertNotNull(existsMethods.remove(actionMethodName));
            }
        }
        assertTrue("menu items not found", count > 30);
        assertTrue("There is action handlers in MainWindow which doesn't used in menu: " + existsMethods.keySet(),
                existsMethods.isEmpty());
    }
}
