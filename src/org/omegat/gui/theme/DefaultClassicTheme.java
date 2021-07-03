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
import java.io.IOException;

import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;

import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.gui.UIDesignManager;


public class DefaultClassicTheme extends DelegatingLookAndFeel {

    private static final String NAME = OStrings.getString("THEME_OMEGAT_CLASSIC_NAME");

    public static void loadPlugins() {
        UIManager.installLookAndFeel(NAME, DefaultClassicTheme.class.getName());
    }

    public static void unloadPlugins() {
    }

    public DefaultClassicTheme() throws Exception {
        super();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getID() {
        return "OmegaT"; // NOI18N
    }

    @Override
    public String getDescription() {
        return OStrings.getString("THEME_OMEGAT_CLASSIC_DESCRIPTION");
    }

    public UIDefaults getDefaults() {
        return setDefaults(systemLookAndFeel.getDefaults());
    }

    /**
     * Apply theme default values to the supplied {@link UIDefaults} object.
     * Suitable for third-party themes that want to modify the default theme.
     *
     * @param defaults the {@link UIDefaults} object such as from
     *                 {@link LookAndFeel#getDefaults()}
     * @return the modified {@link UIDefaults} object
     */
    public static UIDefaults setDefaults(UIDefaults defaults) {
        defaults.put("OmegaTStatusArea.border", new MatteBorder(1, 1, 1, 1, Color.BLACK));

        try {
            UIDesignManager.loadDefaultColors(defaults);
        } catch (IOException e) {
            Log.log(e);
        }
        // FIXME: VLDocking values have to be set to the "developer defaults"
        // not the "LAF defaults" because that's where
        // DockingUISettings#installUI puts them
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
        UIManager.put("DockTabbedPane.menu.closeAllOther", getIcon("empty.gif"));
        UIManager.put("DragControler.detachCursor", getIcon("undock.gif").getImage());
        return defaults;
    }
}
