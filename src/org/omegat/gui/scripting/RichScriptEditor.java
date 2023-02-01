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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ui.CollapsibleSectionPanel;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.FindDialog;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.ReplaceDialog;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;
import org.omegat.util.Java8Compat;
import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

@SuppressWarnings("serial")
public class RichScriptEditor extends AbstractScriptEditor implements SearchListener {

    private RSyntaxTextArea m_scriptEditor;
    private CollapsibleSectionPanel m_csp;
    private FindDialog m_findDialog;
    private ReplaceDialog m_replaceDialog;
    private FindToolBar m_findToolBar;
    private ReplaceToolBar m_replaceToolBar;
    private ScriptingWindow m_scriptingWindow;

    @Override
    public JTextArea getTextArea() {
        return m_scriptEditor;
    }

    @Override
    public void setHighlighting(String extension) {
        switch (extension) {
        case "groovy":
            m_scriptEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
            break;
        case "js":
            m_scriptEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
            break;
        case "py":
            m_scriptEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
            break;
        default:
            m_scriptEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        }

    }

    public void initSearchDialogs() {

        m_findDialog = new FindDialog(m_scriptingWindow.frame, this);
        m_replaceDialog = new ReplaceDialog(m_scriptingWindow.frame, this);

        // This ties the properties of the two dialogs together (match case, regex, etc.).
        SearchContext context = m_findDialog.getSearchContext();
        m_replaceDialog.setSearchContext(context);

        // Create tool bars and tie their search contexts together also.
        m_findToolBar = new FindToolBar(this);
        m_findToolBar.setSearchContext(context);
        m_replaceToolBar = new ReplaceToolBar(this);
        m_replaceToolBar.setSearchContext(context);
    }

    @Override
    public void searchEvent(SearchEvent e) {
        SearchEvent.Type type = e.getType();
        SearchContext context = e.getSearchContext();
        SearchResult result = null;

        switch (type) {
        default: // Prevent FindBugs warning later
        case MARK_ALL:
            result = SearchEngine.markAll(m_scriptEditor, context);
            break;
        case FIND:
            result = SearchEngine.find(m_scriptEditor, context);
            if (!result.wasFound()) {
                UIManager.getLookAndFeel().provideErrorFeedback(m_scriptEditor);
            }
            break;
        case REPLACE:
            result = SearchEngine.replace(m_scriptEditor, context);
            if (!result.wasFound()) {
                UIManager.getLookAndFeel().provideErrorFeedback(m_scriptEditor);
            }
            break;
        case REPLACE_ALL:
            result = SearchEngine.replaceAll(m_scriptEditor, context);
            JOptionPane.showMessageDialog(null, result.getCount() + " occurrences replaced.");
            break;
        }

    }

    private class ShowReplaceDialogAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (m_findDialog.isVisible()) {
                m_findDialog.setVisible(false);
            }
            m_replaceDialog.setVisible(true);
        }
    }

    private class GoToLineAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (m_findDialog.isVisible()) {
                m_findDialog.setVisible(false);
            }
            if (m_replaceDialog.isVisible()) {
                m_replaceDialog.setVisible(false);
            }
            GoToDialog dialog = new GoToDialog(m_scriptingWindow.frame);
            dialog.setMaxLineNumberAllowed(m_scriptEditor.getLineCount());
            dialog.setVisible(true);
            int line = dialog.getLineNumber();
            if (line > 0) {
                try {
                    m_scriptEditor.setCaretPosition(m_scriptEditor.getLineStartOffset(line - 1));
                } catch (BadLocationException ble) { // Never happens
                    UIManager.getLookAndFeel().provideErrorFeedback(m_scriptEditor);
                    ble.printStackTrace();
                }
            }
        }

    }

    private class ShowFindDialogAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (m_replaceDialog.isVisible()) {
                m_replaceDialog.setVisible(false);
            }
            m_findDialog.setVisible(true);
        }
    }

    @Override
    public void enhanceMenu(JMenuBar mb) {
        JMenu menu = new JMenu();

        Mnemonics.setLocalizedText(menu, OStrings.getString("SCW_MENU_EDIT"));

        JMenuItem item = new JMenuItem();
        Mnemonics.setLocalizedText(item, OStrings.getString("SCW_MENU_FIND"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, Java8Compat.getMenuShortcutKeyMaskEx()));
        item.addActionListener(new ShowFindDialogAction());
        menu.add(item);

        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, OStrings.getString("SCW_MENU_REPLACE"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, Java8Compat.getMenuShortcutKeyMaskEx()));
        item.addActionListener(new ShowReplaceDialogAction());
        menu.add(item);

        item = new JMenuItem();
        Mnemonics.setLocalizedText(item, OStrings.getString("SCW_MENU_GOTO_LINE"));
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, Java8Compat.getMenuShortcutKeyMaskEx()));
        item.addActionListener(new GoToLineAction());
        menu.add(item);

        menu.addSeparator();

        int metaShiftMask = Java8Compat.getMenuShortcutKeyMaskEx() | InputEvent.SHIFT_DOWN_MASK;
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F, metaShiftMask);
        Action a = m_csp.addBottomComponent(ks, m_findToolBar);
        a.putValue(Action.NAME, OStrings.getString("SCW_MENU_SHOW_FIND_BAR"));
        menu.add(new JMenuItem(a));
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_G, metaShiftMask);
        a = m_csp.addBottomComponent(ks, m_replaceToolBar);
        a.putValue(Action.NAME, OStrings.getString("SCW_MENU_SHOW_REPLACE_BAR"));
        menu.add(new JMenuItem(a));

        mb.add(menu);
    }

    @Override
    public void initLayout(ScriptingWindow scriptingWindow) {
        m_scriptingWindow = scriptingWindow;
        m_scriptEditor = new RSyntaxTextArea();
        m_scriptEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, m_scriptEditor.getFont().getSize()));

        CompletionProvider provider = new DefaultCompletionProvider();
        AutoCompletion ac = new AutoCompletion(provider);
        ac.install(m_scriptEditor);

        m_scriptEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        m_scriptEditor.setCodeFoldingEnabled(true);
        RTextScrollPane scrollPaneEditor = new RTextScrollPane(m_scriptEditor);
        // scrollPaneEditor.setMinimumSize(minimumSize1);
        m_csp = new CollapsibleSectionPanel();
        m_scriptingWindow.frame.getContentPane().add(m_csp);
        m_csp.add(scrollPaneEditor);

        initSearchDialogs();

    }

    @Override
    public Component getPanel() {
        return m_csp;
    }

}
