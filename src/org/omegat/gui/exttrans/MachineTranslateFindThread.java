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

import org.jetbrains.annotations.VisibleForTesting;
import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IStopped;
import org.omegat.core.machinetranslators.MachineTranslateError;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.Preferences;

class MachineTranslateFindThread extends EntryInfoSearchThread<MachineTranslationInfo> {
    private final IMachineTranslation translator;
    private final String src;
    private final boolean force;

    MachineTranslateFindThread(MachineTranslateTextArea machineTranslateTextArea,
            final IMachineTranslation translator, final SourceTextEntry newEntry, boolean force) {
        super(machineTranslateTextArea, newEntry);
        this.translator = translator;
        src = newEntry.getSrcText();
        this.force = force;
    }

    @Override
    protected MachineTranslationInfo search() throws Exception {
        try {
            return fetchTranslation(translator, src, currentlyProcessedEntry, force, this::isEntryChanged);
        } catch (StoppedException ex) {
            // Entry changed, cancel processing
            throw new EntryChangedException();
        }
    }

    /**
     * Fetch machine translation (static for testing and clarity).
     *
     * @param translator
     *            MT engine to use
     * @param src
     *            source text
     * @param entry
     *            current entry (for context)
     * @param forceLoad
     *            true to skip cache
     * @param isStopped
     *            callback to check if processing should stop
     * @return translation info, or null if unavailable
     * @throws StoppedException
     *             if isStopped returns true
     */
    @VisibleForTesting
    static MachineTranslationInfo fetchTranslation(IMachineTranslation translator, String src,
            SourceTextEntry entry, boolean forceLoad, IStopped isStopped) throws StoppedException {

        if (isStopped.isStopped()) {
            throw new StoppedException();
        }

        Language source = Core.getProject().getProjectProperties().getSourceLanguage();
        Language target = Core.getProject().getProjectProperties().getTargetLanguage();

        String tr = null;

        try {
            // Try cache first
            if (!forceLoad) {
                tr = translator.getCachedTranslation(source, target, src);
                if (tr != null || !Preferences.isPreferenceDefault(Preferences.MT_AUTO_FETCH, false)
                        || Preferences.isPreference(Preferences.MT_ONLY_UNTRANSLATED)
                                && Core.getProject().getTranslationInfo(entry).isTranslated()) {
                    return new MachineTranslationInfo(translator.getName(), tr);
                }
            }

            // Check before expensive network call
            if (isStopped.isStopped()) {
                throw new StoppedException();
            }

            try {
                tr = translator.getTranslation(source, target, src);
            } catch (MachineTranslateError e) {
                Log.log(e);
                Core.getMainWindow().showTimedStatusMessageRB("MT_ENGINE_ERROR", translator.getName(),
                        e.getLocalizedMessage());
                return null;
            } catch (Exception e) {
                Log.logErrorRB(e, "MT_ENGINE_EXCEPTION");
                return null;
            }

            if (isStopped.isStopped()) {
                throw new StoppedException();
            }
        } catch (Exception ex) {
            Log.log(ex);
        }

        return tr == null ? null : new MachineTranslationInfo(translator.getName(), tr);
    }

    /**
     * Exception thrown when processing should stop.
     */
    static class StoppedException extends Exception {
        private static final long serialVersionUID = 1L;
        StoppedException() {
            super("MT processing stopped");
        }
    }
}
