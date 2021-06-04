/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers,
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2009-2010 Alex Buloichik
               2014 Yu Tang
               2015 Aaron Madlon-Kay
               2021 Hiroshi Miura
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.gui.theme;

import java.awt.Color;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;

import org.omegat.util.gui.ResourcesUtil;
import org.omegat.util.gui.UIDesignManager;


public class DefaultClassicTheme {

    private static final String CLASSIC_THEME_NAME = "Classic";
    private static final String DEFAULT_LAF_CLASS = UIManager.getSystemLookAndFeelClassName();

    public static void loadPlugins() {
        UIDesignManager.registerTheme(new DefaultClassicThemeDesignInitializer());
    }

    public static void unloadPlugins() {
    }

    public static class DefaultClassicThemeDesignInitializer extends UIManager.LookAndFeelInfo implements IThemeInitializer {

        public DefaultClassicThemeDesignInitializer() {
            super(CLASSIC_THEME_NAME, DEFAULT_LAF_CLASS);
        }

        public void setup() {
            installClassicDesign();
        }

        private static void installClassicDesign() {
            UIManager.put("OmegaTStatusArea.border", new MatteBorder(1, 1, 1, 1, Color.BLACK));

            UIManager.put("DockViewTitleBar.hide", getIcon("minimize.gif"));
            UIManager.put("DockViewTitleBar.hide.rollover", getIcon("minimize.rollover.gif"));
            UIManager.put("DockViewTitleBar.hide.pressed", getIcon("minimize.pressed.gif"));
            UIManager.put("DockViewTitleBar.maximize", getIcon("maximize.gif"));
            UIManager.put("DockViewTitleBar.maximize.rollover", getIcon("maximize.rollover.gif"));
            UIManager.put("DockViewTitleBar.maximize.pressed", getIcon("maximize.pressed.gif"));
            UIManager.put("DockViewTitleBar.restore", getIcon("restore.gif"));
            UIManager.put("DockViewTitleBar.restore.rollover", getIcon("restore.rollover.gif"));
            UIManager.put("DockViewTitleBar.restore.pressed", getIcon("restore.pressed.gif"));
            UIManager.put("DockViewTitleBar.dock", getIcon("restore.gif"));
            UIManager.put("DockViewTitleBar.dock.rollover", getIcon("restore.rollover.gif"));
            UIManager.put("DockViewTitleBar.dock.pressed", getIcon("restore.pressed.gif"));
            UIManager.put("DockViewTitleBar.float", getIcon("undock.gif"));
            UIManager.put("DockViewTitleBar.float.rollover", getIcon("undock.rollover.gif"));
            UIManager.put("DockViewTitleBar.float.pressed", getIcon("undock.pressed.gif"));
            UIManager.put("DockViewTitleBar.attach", getIcon("dock.gif"));
            UIManager.put("DockViewTitleBar.attach.rollover", getIcon("dock.rollover.gif"));
            UIManager.put("DockViewTitleBar.attach.pressed", getIcon("dock.pressed.gif"));

            UIManager.put("DockViewTitleBar.menu.hide", getIcon("minimize.gif"));
            UIManager.put("DockViewTitleBar.menu.maximize", getIcon("maximize.gif"));
            UIManager.put("DockViewTitleBar.menu.restore", getIcon("restore.gif"));
            UIManager.put("DockViewTitleBar.menu.dock", getIcon("restore.gif"));
            UIManager.put("DockViewTitleBar.menu.float", getIcon("undock.gif"));
            UIManager.put("DockViewTitleBar.menu.attach", getIcon("dock.gif"));

            UIManager.put("DockTabbedPane.menu.hide", getIcon("empty.gif"));
            UIManager.put("DockTabbedPane.menu.maximize", getIcon("empty.gif"));
            UIManager.put("DockTabbedPane.menu.float", getIcon("empty.gif"));
            UIManager.put("DockTabbedPane.menu.closeAll", getIcon("empty.gif"));
            UIManager.put("DockTabbedPane.menu.close\n" +
                    "    @SuppressWarnings(\"unused\")\n" +
                    "    private static void installClassicDesign() {\n" +
                    "        UIManager.put(\"OmegaTStatusArea.border\", new MatteBorder(1, 1, 1, 1, Color.BLACK));\n" +
                    "\n" +
                    "        UIManager.put(\"DockViewTitleBar.hide\", getIcon(\"minimize.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.hide.rollover\", getIcon(\"minimize.rollover.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.hide.pressed\", getIcon(\"minimize.pressed.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.maximize\", getIcon(\"maximize.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.maximize.rollover\", getIcon(\"maximize.rollover.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.maximize.pressed\", getIcon(\"maximize.pressed.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.restore\", getIcon(\"restore.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.restore.rollover\", getIcon(\"restore.rollover.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.restore.pressed\", getIcon(\"restore.pressed.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.dock\", getIcon(\"restore.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.dock.rollover\", getIcon(\"restore.rollover.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.dock.pressed\", getIcon(\"restore.pressed.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.float\", getIcon(\"undock.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.float.rollover\", getIcon(\"undock.rollover.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.float.pressed\", getIcon(\"undock.pressed.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.attach\", getIcon(\"dock.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.attach.rollover\", getIcon(\"dock.rollover.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.attach.pressed\", getIcon(\"dock.pressed.gif\"));\n" +
                    "\n" +
                    "        UIManager.put(\"DockViewTitleBar.menu.hide\", getIcon(\"minimize.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.menu.maximize\", getIcon(\"maximize.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.menu.restore\", getIcon(\"restore.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.menu.dock\", getIcon(\"restore.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.menu.float\", getIcon(\"undock.gif\"));\n" +
                    "        UIManager.put(\"DockViewTitleBar.menu.attach\", getIcon(\"dock.gif\"));\n" +
                    "\n" +
                    "        UIManager.put(\"DockTabbedPane.menu.hide\", getIcon(\"empty.gif\"));\n" +
                    "        UIManager.put(\"DockTabbedPane.menu.maximize\", getIcon(\"empty.gif\"));\n" +
                    "        UIManager.put(\"DockTabbedPane.menu.float\", getIcon(\"empty.gif\"));\n" +
                    "        UIManager.put(\"DockTabbedPane.menu.closeAll\", getIcon(\"empty.gif\"));\n" +
                    "        UIManager.put(\"DockTabbedPane.menu.closeAllOther\", getIcon(\"empty.gif\"));\n" +
                    "\n" +
                    "        UIManager.put(\"DragControler.detachCursor\", getIcon(\"undock.gif\").getImage());\n" +
                    "    }\nAllOther", getIcon("empty.gif"));

            UIManager.put("DragControler.detachCursor", getIcon("undock.gif").getImage());
        }

        /**
         * Load icon from classpath.
         *
         * @param iconName
         *            icon file name
         * @return icon instance
         */
        private static ImageIcon getIcon(String iconName) {
            Image image = ResourcesUtil.getBundledImage(iconName);
            return image == null ? null : new ImageIcon(image);
        }
    }
}
