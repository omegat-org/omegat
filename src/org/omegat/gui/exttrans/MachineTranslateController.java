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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.omegat.core.Core;
import org.omegat.core.data.CoreState;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.glossary.GlossaryEntry;

/**
 * Controller for MachineTranslateTextArea following MVC. Responsible for
 * orchestrating MT searches and managing search threads.
 */
public class MachineTranslateController {
    private final MachineTranslateTextArea view;
    private final List<MachineTranslateFindThread> searchThreads = new ArrayList<>();

    /**
     * List displayed hold entries. An index shall be as same as ID attribute
     * value of HTML. Actual displayed entries are sorted, and the order is
     * different from the List.
     */
    private final List<MachineTranslationInfo> displayed = new CopyOnWriteArrayList<>();

    public MachineTranslateController(MachineTranslateTextArea view) {
        this.view = view;
        selectedIndex = -1;
        setGlossaryMap();
    }

    /**
     * Configures the glossary map for machine translation connectors. This
     * method sets the glossary map supplier for all machine translation
     * services by utilizing the `getGlossaryMap` method as the supplier.
     * <p>
     * The glossary map provides the source-to-target text mappings that can be
     * used by machine translation services to improve translation output
     * quality or consistency.
     * <p>
     * This will override for testing purposes.
     */
    @VisibleForTesting
    void setGlossaryMap() {
        CoreState.getInstance().getMachineTranslatorsManager().setGlossaryMap(this::getGlossaryMap);
    }

    /**
     * Retrieves a glossary map containing source-to-target text mappings. This
     * map is constructed using the glossary entries found from the source text
     * currently being processed.
     *
     * @return a map where keys are source text strings, and values are their
     *         corresponding localized text strings, as per the glossary
     *         entries.
     */
    Map<String, String> getGlossaryMap() {
        return Core.getGlossaryManager().searchSourceMatches(view.getCurrentlyProcessedEntry()).stream()
                .collect(Collectors.toMap(GlossaryEntry::getSrcText, GlossaryEntry::getLocText));
    }

    /** Cycle getDisplayedTranslation **/
    private int selectedIndex;

    @Nullable MachineTranslationInfo getDisplayedResult() {
        if (displayed.isEmpty()) {
            return null;
        }
        selectedIndex = (selectedIndex + 1) % displayed.size();
        MachineTranslationInfo info = displayed.get(selectedIndex);
        view.highlightSelected(selectedIndex, info);
        return info;
    }

    void setFoundResult(MachineTranslationInfo data) {
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
        view.setText(sb.toString());
    }

    void clearFoundResult() {
        displayed.clear();
        selectedIndex = -1;
    }

    /**
     * Start MT search for a new entry.
     */
    public void startSearchThread(SourceTextEntry newEntry) {
        startSearchThread(newEntry, false);
    }

    /**
     * Start MT search for a new entry with an option to force network fetch.
     */
    public void startSearchThread(SourceTextEntry newEntry, boolean force) {
        // clear view and stop any running threads first
        view.clear();
        stopSearchThreads();
        synchronized (searchThreads) {
            for (IMachineTranslation mt : getMachineTranslators()) {
                if (mt.isEnabled()) {
                    MachineTranslateFindThread mtSearchThread = new MachineTranslateFindThread(view, mt,
                            newEntry, force);
                    searchThreads.add(mtSearchThread);
                    mtSearchThread.start();
                }
            }
        }
    }

    /**
     * Force reload for the current entry.
     */
    public void forceLoad() {
        SourceTextEntry current = view.getCurrentlyProcessedEntry();
        if (current != null) {
            // check if any thread is running
            synchronized (searchThreads) {
                for (MachineTranslateFindThread thread : searchThreads) {
                    if (thread.isAlive()) {
                        return;
                    }
                }
            }
            startSearchThread(current, true);
        }
    }

    /**
     * Stop all running search threads.
     */
    public void stopSearchThreads() {
        synchronized (searchThreads) {
            for (MachineTranslateFindThread thread : searchThreads) {
                if (thread.isAlive()) {
                    thread.interrupt();
                }
            }
            searchThreads.clear();
        }
    }

    /**
     * Get all machine translation providers.
     */
    private List<IMachineTranslation> getMachineTranslators() {
        return CoreState.getInstance().getMachineTranslatorsManager().getMachineTranslators();
    }
}
