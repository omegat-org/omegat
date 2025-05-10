/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2025 Hiroshi Miura
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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.junit.Test;

import org.omegat.core.TestCore;

/**
 * @author Alex Buloichik
 */
public class MainWindowMenuTest extends TestCore {
    /**
     * Tests the actions defined in the main menu of the application to ensure
     * that each menu item has a corresponding action handler method implemented
     * in the `MainWindowMenuHandler` class.
     * <p>
     * It checks for the following:
     * - Each menu item has an associated action method in the handler class
     *   with the naming convention `<menuItemName>ActionPerformed`.
     * - The action methods can optionally accept a single integer argument
     *   representing a modifier key.
     * - All public, non-static methods in the handler class are used by the
     *   menu items, ensuring no unused action handlers exist.
     * Verifies:
     * - There are more than 30 menu items in the main menu.
     * - All defined menu items have corresponding action handler methods.
     * - There are no unused public action handler methods in the handler class.
     *
     * @throws Exception if any menu item lacks a corresponding action handler
     *                   method or any unexpected error occurs during the test.
     */
    @Test
    public void testMenuActions() throws Exception {
        int count = 0;

        Map<String, Method> existsMethods = new HashMap<>();

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

        for (Field f : MainWindowMenu.class.getDeclaredFields()) {
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
        assertTrue("There is action handlers in MainWindow which doesn't used in menu: "
                + existsMethods.keySet(), existsMethods.isEmpty());
    }

    /**
     * Tests that all menu actions referenced in the application's main menu have
     * corresponding action handler methods implemented in the `MainWindowMenuHandler`
     * class.
     * <p>
     * This test scans the source code for all invocations of menu actions and verifies:
     * - Each menu action has a corresponding handler method with the naming convention
     *   `<menuItemName>ActionPerformed`.
     * - The handler methods may optionally accept a single integer argument, representing
     *   a modifier key.
     * - If no matching handler method is found, the test fails with an assertion error.
     *
     * The method uses a source code processing utility to locate method calls in all
     * `.java` files under the `src` directory for validation.
     *
     * @throws Exception if the source code processing fails or any menu action lacks
     *                   a corresponding handler method.
     */
    @Test
    public void testMenuActions_invokeActions() throws Exception {
        Pattern pattern = Pattern.compile("getMainMenu\\(\\).invokeAction\\(\\s*\"([^\"]+)\"\\s*,");
        final String[] target = {"src"};
        processSourceContent(target, (path, chars) -> {
            Matcher matcher = pattern.matcher(chars);
            while (matcher.find()) {
                String actionMethodName = matcher.group(1) + "ActionPerformed";
                Method method = null;
                try {
                    method = MainWindowMenuHandler.class.getMethod(actionMethodName);
                } catch (NoSuchMethodException ignore) {
                    // See if the method accepts a modifier key argument.
                    try {
                        method = MainWindowMenuHandler.class.getMethod(actionMethodName, Integer.TYPE);
                    } catch (NoSuchMethodException ex) {
                        assertNotNull("Action method \"" + actionMethodName + "\" not defined for invoked from " + path,
                                method);
                    }
                }
            }
        });
    }

    /**
     * Process the text content of all .java files under /src with the given consumer.
     *
     * @param consumer
     *            A function that accepts the file path and content
     * @throws IOException
     *             from Files.find()
     */
    public void processSourceContent(String[] targets, BiConsumer<Path, CharSequence> consumer)
            throws IOException {
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder();
        decoder.onMalformedInput(CodingErrorAction.REPORT);
        decoder.onUnmappableCharacter(CodingErrorAction.REPORT);
        for (String root : targets) {
            Path rootPath = Paths.get(".", root);
            assertTrue(rootPath.toFile().isDirectory());
            try (Stream<Path> files = Files.find(rootPath, 100,
                    (path, attrs) -> attrs.isRegularFile() && path.toString().endsWith(".java"))) {
                files.forEach(p -> {
                    try {
                        byte[] bytes = Files.readAllBytes(p);
                        consumer.accept(p, decoder.decode(ByteBuffer.wrap(bytes)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }
}
