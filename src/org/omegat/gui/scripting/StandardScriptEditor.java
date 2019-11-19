/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Briac Pilpre (briacp@gmail.com)
               2013 Alex Buloichik
               2014 Briac Pilpre (briacp@gmail.com), Yu Tang
               2015 Yu Tang, Aaron Madlon-Kay
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
package org.omegat.gui.scripting;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class StandardScriptEditor extends AbstractScriptEditor {
    private JTextArea m_scriptEditor;
    private JScrollPane m_scrollPaneEditor;

    @Override
    public void setHighlighting(String extension) {

    }

    @Override
    public void enhanceMenu(JMenuBar menubar) {

    }

    @Override
    public void initLayout(ScriptingWindow scriptingWindow) {
        m_scriptEditor = new JTextArea();

        m_scriptEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN,
                m_scriptEditor.getFont().getSize()));
        m_scrollPaneEditor = new JScrollPane(m_scriptEditor);

    }

    @Override
    public Component getPanel() {
        return m_scrollPaneEditor;
    }

    @Override
    public JTextArea getTextArea() {
        return m_scriptEditor;
    }

}
