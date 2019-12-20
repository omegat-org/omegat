/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009-2010 Alex Buloichik
               2011 Martin Fleurke
               2012 Jean-Christophe Helary
               2015 Aaron Madlon-Kay
               2018 Thomas Cordonnier
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

package org.omegat.gui.exttrans;

import java.awt.Dimension;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.machinetranslators.MachineTranslators;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.gui.common.EntryInfoThreadPane;
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
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Pane for display machine translations.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Martin Fleurke
 * @author Jean-Christophe Helary
 * @author Aaron Madlon-Kay
 * @author Thomas Cordonnier
 */
@SuppressWarnings("serial")
public class MachineTranslateTextArea extends EntryInfoThreadPane<MachineTranslationInfo>
        implements IPaneMenu {

    private static final String EXPLANATION = OStrings.getString("GUI_MACHINETRANSLATESWINDOW_explanation");

    protected Set<MachineTranslationInfo> displayed = new HashSet<>();

    public MachineTranslateTextArea(IMainWindow mw) {
        super(true);

        setEditable(false);
        StaticUIUtils.makeCaretAlwaysVisible(this);

        this.setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));

        String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_MachineTranslate");
        mw.addDockable(new DockableScrollPane("MACHINE_TRANSLATE", title, this, true));

        for (Class<?> mtc : PluginUtils.getMachineTranslationClasses()) {
            try {
                MachineTranslators.add((IMachineTranslation) mtc.getDeclaredConstructor().newInstance());
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
    }

    /** Cycle getDisplayedTranslation **/
    private Iterator<MachineTranslationInfo> cycle;

    public String getDisplayedTranslation() {
        if ((cycle == null) || (!cycle.hasNext())) {
            cycle = displayed.iterator();
        }
        if (!cycle.hasNext()) { // only possible if displayed.isEmpty()
            return null;
        }
        return cycle.next().result;
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
            displayed.add (data);
            setText(getText() + data.result + "\n<" + data.translatorName + ">\n\n");
        }
    }

    @Override
    public void clear() {
        super.clear();
        displayed.clear();
        cycle = null;
    }

    protected class FindThread extends EntryInfoSearchThread<MachineTranslationInfo> {
        private final IMachineTranslation translator;
        private final String src;
        private final boolean force;

        public FindThread(final IMachineTranslation translator, final SourceTextEntry newEntry,
                boolean force) {
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
            if (pp != null){
                 source = pp.getSourceLanguage();
                 target = pp.getTargetLanguage();
             }
            if (source == null || target == null) {
                return null;
            }

            String result = getTranslation(source, target);
            return result == null ? null : new MachineTranslationInfo(translator.getName(), result);
        }

        private String getTranslation(Language source, Language target) throws Exception {
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
            return translator.getTranslation(source, target, src);
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
