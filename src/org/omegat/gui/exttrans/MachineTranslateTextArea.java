/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009-2010 Alex Buloichik
               2011 Martin Fleurke
               2012 Jean-Christophe Helary
               2015 Aaron Madlon-Kay
               2018 Thomas Cordonnier
               2022-2025 Hiroshi Miura
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

package org.omegat.gui.exttrans;

import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.common.EntryInfoThreadPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.preferences.PreferencesWindowController;
import org.omegat.gui.preferences.view.MachineTranslationPreferencesController;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.gui.IPaneMenu;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Pane for display machine translations.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Jean-Christophe Helary
 * @author Aaron Madlon-Kay
 * @author Thomas Cordonnier
 * @author Hiroshi Miura
 */
@SuppressWarnings("serial")
public class MachineTranslateTextArea extends EntryInfoThreadPane<MachineTranslationInfo>
        implements IPaneMenu {

    private static final String EXPLANATION = OStrings.getString("GUI_MACHINETRANSLATESWINDOW_explanation");

    private final MachineTranslateController controller;

    protected final DockableScrollPane scrollPane;

    public MachineTranslateTextArea(IMainWindow mw) {
        super(true);

        controller = new MachineTranslateController(this);

        setEditable(false);
        StaticUIUtils.makeCaretAlwaysVisible(this);
        initDocument();

        this.setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));

        String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_MachineTranslate");
        scrollPane = new DockableScrollPane("MACHINE_TRANSLATE", title, this, true);
        mw.addDockable(scrollPane);
    }

    private void initDocument() {
        StyleSheet baseStyleSheet = new StyleSheet();
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        // Add default styles
        baseStyleSheet.addStyleSheet(htmlEditorKit.getStyleSheet());
        Font font = getFont();
        baseStyleSheet
                .addRule("body { font-family: " + font.getName() + "; " + " font-size: " + font.getSize()
                        + "; " + " font-style: " + (font.getStyle() == Font.ITALIC ? "italic" : "normal")
                        + "; " + " font-weight: " + (font.getStyle() == Font.BOLD ? "bold" : "normal") + "; "
                        + " color: " + Styles.EditorColor.COLOR_FOREGROUND.toHex() + "; " + " background: "
                        + Styles.EditorColor.COLOR_BACKGROUND.toHex() + ";} "
                        + ".engine {font-style: italic; text-align: right;}");
        htmlEditorKit.setStyleSheet(baseStyleSheet);
        setEditorKit(htmlEditorKit);
    }

    /**
     * Expose the currently processed entry for the controller.
     */
    SourceTextEntry getCurrentlyProcessedEntry() {
        return currentlyProcessedEntry;
    }

    /**
     * Expose the currently displayed translation for the shortcut.
     * 
     * @return currently displayed translation or null if none is displayed
     */
    public MachineTranslationInfo getDisplayedTranslation() {
        return controller.getDisplayedResult();
    }

    void highlightSelected(final int selectedIndex, final MachineTranslationInfo info) {
        UIThreadsUtil.mustBeSwingThread();
        HTMLDocument doc = (HTMLDocument) getDocument();
        Element rootElement = doc.getDefaultRootElement();
        if (rootElement == null) {
            return;
        }
        Element el = doc.getElement(rootElement, HTML.Attribute.ID, String.valueOf(selectedIndex));
        if (el == null) {
            return;
        }
        int pos = el.getStartOffset();
        try {
            getHighlighter().removeAllHighlights();
            getHighlighter().addHighlight(pos, pos + info.result.length(),
                    new DefaultHighlighter.DefaultHighlightPainter(
                            Styles.EditorColor.COLOR_MACHINETRANSLATE_SELECTED_HIGHLIGHT.getColor()));
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    @Override
    protected void onProjectClose() {
        UIThreadsUtil.mustBeSwingThread();
        this.setText(EXPLANATION);
    }

    public void forceLoad() {
        controller.forceLoad();
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        controller.startSearchThread(newEntry);
    }

    @Override
    protected void setFoundResult(final SourceTextEntry se, final MachineTranslationInfo data) {
        UIThreadsUtil.mustBeSwingThread();
        if (data != null && data.result != null) {
            controller.setFoundResult(data);
        }
    }

    @Override
    public void clear() {
        super.clear();
        getHighlighter().removeAllHighlights();
        controller.clearFoundResult();
    }

    @Override
    public void populatePaneMenu(JPopupMenu menu) {
        final JMenuItem prefs = new JMenuItem(OStrings.getString("GUI_MACHINETRANSLATESWINDOW_OPEN_PREFS"));
        prefs.addActionListener(e -> new PreferencesWindowController().show(
                Core.getMainWindow().getApplicationFrame(), MachineTranslationPreferencesController.class));
        menu.add(prefs);
    }
}
