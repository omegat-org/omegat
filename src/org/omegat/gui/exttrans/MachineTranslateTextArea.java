/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.exttrans;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.gui.common.EntryInfoThreadPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Pane for display machine translations.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MachineTranslateTextArea extends EntryInfoThreadPane<MachineTranslationInfo> {
    protected final IMachineTranslation[] translators;

    protected String displayed;

    public MachineTranslateTextArea() {
        super(true);

        setEditable(false);
        String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_MachineTranslate");
        Core.getMainWindow().addDockable(new DockableScrollPane("MACHINE_TRANSLATE", title, this, true));

        List<IMachineTranslation> tr = new ArrayList<IMachineTranslation>();
        for (Class<?> mtc : PluginUtils.getMachineTranslationClasses()) {
            try {
                tr.add((IMachineTranslation) mtc.newInstance());
            } catch (Exception ex) {
                Log.log(ex);
            }
        }
        translators = tr.toArray(new IMachineTranslation[tr.size()]);
    }

    public String getDisplayedTranslation() {
        return displayed;
    }

    @Override
    protected void onProjectClose() {
        UIThreadsUtil.mustBeSwingThread();
        setText("");
    }

    @Override
    protected void startSearchThread(final SourceTextEntry newEntry) {
        UIThreadsUtil.mustBeSwingThread();

        setText("");
        displayed = null;
        for (IMachineTranslation mt : translators) {
            new FindThread(mt, newEntry).start();
        }
    }

    @Override
    protected void setFoundResult(final SourceTextEntry se, final MachineTranslationInfo data) {
        UIThreadsUtil.mustBeSwingThread();

        if (data != null && data.result != null) {
            if (displayed == null) {
                displayed = data.result;
            }
            setText(getText() + data.result + "\n<" + data.translatorName + ">\n\n");
        }
    }

    protected class FindThread extends EntryInfoSearchThread<MachineTranslationInfo> {
        private final IMachineTranslation translator;
        private final String src;

        public FindThread(final IMachineTranslation translator, final SourceTextEntry newEntry) {
            super(MachineTranslateTextArea.this, newEntry);
            this.translator = translator;
            src = newEntry.getSrcText();
        }

        @Override
        protected MachineTranslationInfo search() throws Exception {
            Language source=null;
            Language target=null;
            ProjectProperties pp = Core.getProject().getProjectProperties();
            if (pp != null){
                 source = pp.getSourceLanguage();
                 target = pp.getTargetLanguage();
             }
            if (source == null || target == null) {
                return null;
            }

            MachineTranslationInfo result = new MachineTranslationInfo();
            result.translatorName = translator.getName();
            result.result = translator.getTranslation(source, target, src);
            return result.result != null ? result : null;
        }
    }
}
