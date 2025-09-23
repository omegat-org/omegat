/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Briac Pilpre (briacp@gmail.com)
               2013 Alex Buloichik
               2014 Briac Pilpre (briacp@gmail.com), Yu Tang
               2015 Yu Tang, Aaron Madlon-Kay
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
package org.omegat.gui.scripting;

import java.awt.Component;

import javax.swing.JMenuBar;
import javax.swing.JTextArea;

/**
 * Interface for script editors in OmegaT.
 */
public interface IScriptEditor {
    /**
     * Sets the syntax highlighting based on file extension.
     *
     * @param extension The file extension determining the syntax style
     */
    void setHighlighting(String extension);

    /**
     * Enhances the menu bar with editor-specific menu items.
     *
     * @param mb The menu bar to enhance
     */
    void enhanceMenu(JMenuBar mb);

    /**
     * Initializes the editor layout.
     *
     * @param scriptingWindow The scripting window this editor belongs to
     */
    void initLayout(ScriptingWindow scriptingWindow);

    /**
     * Gets the main component panel of this editor.
     *
     * @return The editor's main component
     */
    Component getPanel();

    /**
     * Gets the text area component for editing scripts.
     *
     * @return The text area component
     */
    JTextArea getTextArea();
}
