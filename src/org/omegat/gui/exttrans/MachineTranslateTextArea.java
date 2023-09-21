/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009-2010 Alex Buloichik
               2011 Martin Fleurke
               2012 Jean-Christophe Helary
               2015 Aaron Madlon-Kay
               2018 Thomas Cordonnier
               2022 Hiroshi Miura
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.machinetranslators.MachineTranslateError;
import org.omegat.core.machinetranslators.MachineTranslators;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.gui.common.EntryInfoThreadPane;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.preferences.PreferencesWindowController;
import org.omegat.gui.preferences.view.MachineTranslationPreferencesController;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
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

    /**
     *  List displayed hold entries. An index shall be as same as ID attribute value of HTML.
     *  Actual displayed entries are sorted, and the order is different from the List.
     */
    protected List<MachineTranslationInfo> displayed = new CopyOnWriteArrayList<>();

    protected final DockableScrollPane scrollPane;

    public MachineTranslateTextArea(IMainWindow mw) {
        super(true);

        setEditable(false);
        StaticUIUtils.makeCaretAlwaysVisible(this);
        initDocument();

        this.setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));
        selectedIndex = -1;

        String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_MachineTranslate");
        scrollPane = new DockableScrollPane("MACHINE_TRANSLATE", title, this, true);
        mw.addDockable(scrollPane);

        for (Class<?> mtc : PluginUtils.getMachineTranslationClasses()) {
            try {
                IMachineTranslation mt = (IMachineTranslation) mtc.getDeclaredConstructor().newInstance();
                mt.setGlossarySupplier(this::getGlossaryMap);
                MachineTranslators.add(mt);
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
    }

    Map<String, String> getGlossaryMap() {
        return Core.getGlossaryManager().searchSourceMatches(currentlyProcessedEntry).stream()
                .collect(Collectors.toMap(GlossaryEntry::getSrcText, GlossaryEntry::getLocText));
    }

    private void initDocument() {
        StyleSheet baseStyleSheet = new StyleSheet();
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        baseStyleSheet.addStyleSheet(htmlEditorKit.getStyleSheet()); // Add default styles
        Font font = getFont();
        baseStyleSheet.addRule("body { font-family: " + font.getName() + "; "
                + " font-size: " + font.getSize() + "; "
                + " font-style: " + (font.getStyle() == Font.ITALIC ? "italic" : "normal") + "; "
                + " font-weight: " + (font.getStyle() == Font.BOLD ? "bold" : "normal") + "; "
                + " color: " + Styles.EditorColor.COLOR_FOREGROUND.toHex() + "; "
                + " background: " + Styles.EditorColor.COLOR_BACKGROUND.toHex() + ";} "
                + ".engine {font-style: italic; text-align: right;}"
                );
        htmlEditorKit.setStyleSheet(baseStyleSheet);
        setEditorKit(htmlEditorKit);
    }

    /** Cycle getDisplayedTranslation **/
    private int selectedIndex;

    public MachineTranslationInfo getDisplayedTranslation() {
        if (displayed.size() == 0) {
            return null;
        }
        selectedIndex = (selectedIndex + 1) % displayed.size();
        MachineTranslationInfo info = displayed.get(selectedIndex);
        highlightSelected(selectedIndex, info);
        return info;
    }

    private void highlightSelected(final int selectedIndex, final MachineTranslationInfo info) {
        UIThreadsUtil.mustBeSwingThread();
        HTMLDocument doc = (HTMLDocument) getDocument();
        Element rootElement = doc.getDefaultRootElement();
        if (rootElement == null) {
            return;
        }
        Element el = doc.getElement(rootElement, HTML.Attribute.ID,  String.valueOf(selectedIndex));
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
        startSearchThread(currentlyProcessedEntry, true);
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        startSearchThread(newEntry, false);
    }

    private void startSearchThread(SourceTextEntry newEntry, boolean force) {
        UIThreadsUtil.mustBeSwingThread();

        clear();
        for (IMachineTranslation mt : MachineTranslators.getMachineTranslators()) {
            if (mt.isEnabled()) {
                new FindThread(mt, newEntry, force).start();
            }
        }
    }

    @Override
    protected void setFoundResult(final SourceTextEntry se, final MachineTranslationInfo data) {
        UIThreadsUtil.mustBeSwingThread();
        if (data != null && data.result != null) {
            displayed.add(data);
            displayed.sort(Comparator.comparing(info -> info.translatorName));
            StringBuilder sb = new StringBuilder("<html>");
            for (int i = 0; i < displayed.size(); i++) {
                MachineTranslationInfo info = displayed.get(i);
                sb.append("<div id=\"").append(i).append("\">");
                sb.append(info.result);
                sb.append("<div class=\"engine\">&lt;");
                sb.append(info.translatorName);
                sb.append("&gt;</div></div>");
            }
            sb.append("</html>");
            setText(sb.toString());
        }
    }

    @Override
    public void clear() {
        super.clear();
        getHighlighter().removeAllHighlights();
        displayed.clear();
        selectedIndex = -1;
    }

    protected class FindThread extends EntryInfoSearchThread<MachineTranslationInfo> {
        private final IMachineTranslation translator;
        private final String src;
        private final boolean force;

        public FindThread(final IMachineTranslation translator, final SourceTextEntry newEntry, boolean force) {
            super(MachineTranslateTextArea.this, newEntry);
            this.translator = translator;
            src = newEntry.getSrcText();
            this.force = force;
        }

        @Override
        protected MachineTranslationInfo search() throws Exception {
            Language source = null;
            Language target = null;
            ProjectProperties pp = Core.getProject().getProjectProperties();
            if (pp != null) {
                 source = pp.getSourceLanguage();
                 target = pp.getTargetLanguage();
            }
            if (source == null || target == null) {
                return null;
            }

            String result = getTranslation(source, target);
            return result == null ? null : new MachineTranslationInfo(translator.getName(), result);
        }

        private String getTranslation(Language source, Language target) {
            if (!force) {
                if (!Preferences.isPreferenceDefault(Preferences.MT_AUTO_FETCH, true)) {
                    return translator.getCachedTranslation(source, target, src);
                }
                if (Preferences.isPreference(Preferences.MT_ONLY_UNTRANSLATED)) {
                    TMXEntry entry = Core.getProject().getTranslationInfo(currentlyProcessedEntry);
                    if (entry.isTranslated()) {
                        return translator.getCachedTranslation(source, target, src);
                    }
                }
            }
            try {
                return translator.getTranslation(source, target, src);
            } catch (MachineTranslateError e) {
                Log.log(e);
                Core.getMainWindow()
                        .showTimedStatusMessageRB("MT_ENGINE_ERROR", translator.getName(), e.getLocalizedMessage());
                return null;
            } catch (Exception e) {
                Log.logErrorRB(e, "MT_ENGINE_EXCEPTION");
                return null;
            }
        }
    }

    @Override
    public void populatePaneMenu(JPopupMenu menu) {
        final JMenuItem prefs = new JMenuItem(OStrings.getString("GUI_MACHINETRANSLATESWINDOW_OPEN_PREFS"));
        prefs.addActionListener(e -> new PreferencesWindowController().show(
                Core.getMainWindow().getApplicationFrame(), MachineTranslationPreferencesController.class));
        menu.add(prefs);
    }
}
