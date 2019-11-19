/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
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

package org.omegat.gui.editor.autocompleter;

import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.util.Java8Compat;
import org.omegat.util.Preferences;

/**
 * A container class for all standard AutoCompleter keys. Plugins and other
 * derived classes can handle additional keys by overriding
 * <code>processKeys()</code>.
 *
 * @author Aaron Madlon-Kay
 */
public class AutoCompleterKeys {
    // AutoCompleter
    public final KeyStroke trigger;
    public final KeyStroke nextView;
    public final KeyStroke prevView;
    public final KeyStroke confirmAndClose;
    public final KeyStroke confirmWithoutClose;
    public final KeyStroke close;

    // List View
    public final KeyStroke listUp;
    public final KeyStroke listUpEmacs;
    public final KeyStroke listDown;
    public final KeyStroke listDownEmacs;
    public final KeyStroke listPageUp;
    public final KeyStroke listPageDown;

    // Table View
    public final KeyStroke tableUp;
    public final KeyStroke tableUpEmacs;
    public final KeyStroke tableDown;
    public final KeyStroke tableDownEmacs;
    public final KeyStroke tableLeft;
    public final KeyStroke tableLeftEmacs;
    public final KeyStroke tableRight;
    public final KeyStroke tableRightEmacs;
    public final KeyStroke tablePageUp;
    public final KeyStroke tablePageDown;
    public final KeyStroke tableFirst;
    public final KeyStroke tableLast;
    public final KeyStroke tableFirstInRow;
    public final KeyStroke tableLastInRow;

    public AutoCompleterKeys() {
        PropertiesShortcuts shortcuts = PropertiesShortcuts.getEditorShortcuts();

        // AutoCompleter
        trigger = shortcuts.getKeyStroke("autocompleterTrigger");
        boolean useLeftRight = Preferences.isPreference(Preferences.AC_SWITCH_VIEWS_WITH_LR);
        int mask = Java8Compat.getMenuShortcutKeyMaskEx();
        nextView = useLeftRight ? KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, mask)
                : shortcuts.getKeyStroke("autocompleterNextView");
        prevView = useLeftRight ? KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, mask)
                : shortcuts.getKeyStroke("autocompleterPrevView");
        confirmAndClose = shortcuts.getKeyStroke("autocompleterConfirmAndClose");
        confirmWithoutClose = shortcuts.getKeyStroke("autocompleterConfirmWithoutClose");
        close = shortcuts.getKeyStroke("autocompleterClose");

        // List View
        listUp = shortcuts.getKeyStroke("autocompleterListUp");
        listUpEmacs = KeyStroke.getKeyStroke("ctrl P");
        listDown = shortcuts.getKeyStroke("autocompleterListDown");
        listDownEmacs = KeyStroke.getKeyStroke("ctrl N");
        listPageUp = shortcuts.getKeyStroke("autocompleterListPageUp");
        listPageDown = shortcuts.getKeyStroke("autocompleterListPageDown");

        // Table View
        tableUp = shortcuts.getKeyStroke("autocompleterTableUp");
        tableUpEmacs = KeyStroke.getKeyStroke("ctrl P");
        tableDown = shortcuts.getKeyStroke("autocompleterTableDown");
        tableDownEmacs = KeyStroke.getKeyStroke("ctrl N");
        tableLeft = shortcuts.getKeyStroke("autocompleterTableLeft");
        tableLeftEmacs = KeyStroke.getKeyStroke("ctrl B");
        tableRight = shortcuts.getKeyStroke("autocompleterTableRight");
        tableRightEmacs = KeyStroke.getKeyStroke("ctrl F");
        tablePageUp = shortcuts.getKeyStroke("autocompleterTablePageUp");
        tablePageDown = shortcuts.getKeyStroke("autocompleterTablePageDown");
        tableFirst = shortcuts.getKeyStroke("autocompleterTableFirst");
        tableLast = shortcuts.getKeyStroke("autocompleterTableLast");
        tableFirstInRow = shortcuts.getKeyStroke("autocompleterTableFirstInRow");
        tableLastInRow = shortcuts.getKeyStroke("autocompleterTableLastInRow");
    }
}
