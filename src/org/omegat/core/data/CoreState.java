/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.core.data;

import org.jetbrains.annotations.VisibleForTesting;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IEditorEventListener;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.core.events.IFontChangedEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.core.spellchecker.SpellCheckerManager;
import org.omegat.core.tagvalidation.ITagValidation;
import org.omegat.core.threads.IAutoSave;
import org.omegat.core.threads.SaveThread;
import org.omegat.core.threads.VersionCheckThread;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.comments.IComments;
import org.omegat.gui.dictionaries.DictionariesTextArea;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.exttrans.MachineTranslateTextArea;
import org.omegat.gui.filelist.IProjectFilesList;
import org.omegat.gui.glossary.GlossaryManager;
import org.omegat.gui.glossary.IGlossaries;
import org.omegat.gui.issues.IIssues;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.matches.IMatcher;
import org.omegat.gui.notes.INotes;
import org.omegat.gui.properties.SegmentPropertiesArea;
import org.omegat.util.Log;

import javax.swing.SwingUtilities;
import java.awt.Font;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class CoreState {

    protected static volatile CoreState instance = new CoreState();

    private IAutoSave saveThread;

    protected CoreState() {
        project = new NotLoadedProject();
    }

    public void initializeSaveThread() {
        SaveThread th = new SaveThread();
        saveThread = th;
        th.start();
    }

    public void initializeVersionCheckThread() {
        VersionCheckThread th = new VersionCheckThread(10);
        th.start();
    }

    public IAutoSave getAutoSave() {
        return saveThread;
    }

    // For testing purposes
    @VisibleForTesting
    void setSaveThread(IAutoSave thread) {
        if (saveThread != null) {
            saveThread.fin();
        }
        saveThread = thread;
    }

    @VisibleForTesting
    static void setInstance(CoreState instance) {
        CoreState.instance = instance;
    }

    public static CoreState getInstance() {
        return instance;
    }

    private Map<String, String> cmdLineParams = Collections.emptyMap();
    private IProject project;
    private Segmenter segmenter;
    private FilterMaster filterMaster;
    private GlossaryManager glossaryManager;
    private ITagValidation tagValidation;
    private IIssues issuesWindow;

    // GUI panes
    private IMainWindow mainWindow;
    private IEditor editor;
    private IGlossaries glossaries;
    private INotes notes;
    private IMatcher matcher;
    private IProjectFilesList projWin;
    private IComments comments;
    private MachineTranslateTextArea machineTranslatePane;
    private DictionariesTextArea dictionaries;
    private SegmentPropertiesArea segmentPropertiesArea;
    private SpellCheckerManager spellCheckerManager;

    public boolean isProjectLoaded() {
        if (project == null) {
            return false;
        }
        return project.isProjectLoaded();
    }

    public Map<String, String> getCmdLineParams() {
        return cmdLineParams;
    }

    public void setCmdLineParams(Map<String, String> cmdLineParams) {
        this.cmdLineParams = cmdLineParams;
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }

    public IMainWindow getMainWindow() {
        return mainWindow;
    }

    public void setMainWindow(IMainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    public IEditor getEditor() {
        return editor;
    }

    public void setEditor(IEditor editor) {
        this.editor = editor;
    }

    public IGlossaries getGlossaries() {
        return glossaries;
    }

    public void setGlossaries(IGlossaries glossaries) {
        this.glossaries = glossaries;
    }

    public GlossaryManager getGlossaryManager() {
        return glossaryManager;
    }

    public void setGlossaryManager(GlossaryManager glossaryManager) {
        this.glossaryManager = glossaryManager;
    }

    public Segmenter getSegmenter() {
        return segmenter;
    }

    public void setSegmenter(Segmenter segmenter) {
        this.segmenter = segmenter;
    }

    public FilterMaster getFilterMaster() {
        return filterMaster;
    }

    public void setFilterMaster(FilterMaster filterMaster) {
        this.filterMaster = filterMaster;
    }

    public ITagValidation getTagValidation() {
        return tagValidation;
    }

    public void setTagValidation(ITagValidation tagValidation) {
        this.tagValidation = tagValidation;
    }

    public INotes getNotes() {
        return notes;
    }

    public void setNotes(INotes notes) {
        this.notes = notes;
    }

    public IIssues getIssuesWindow() {
        return issuesWindow;
    }

    public void setIssuesWindow(IIssues issuesWindow) {
        this.issuesWindow = issuesWindow;
    }

    public IMatcher getMatcher() {
        return matcher;
    }

    public void setMatcher(IMatcher matcher) {
        this.matcher = matcher;
    }

    public IProjectFilesList getProjWin() {
        return projWin;
    }

    public void setProjWin(IProjectFilesList projWin) {
        this.projWin = projWin;
    }

    public IComments getComments() {
        return comments;
    }

    public void setComments(IComments comments) {
        this.comments = comments;
    }

    public MachineTranslateTextArea getMachineTranslatePane() {
        return machineTranslatePane;
    }

    public void setMachineTranslatePane(MachineTranslateTextArea machineTranslatePane) {
        this.machineTranslatePane = machineTranslatePane;
    }

    public DictionariesTextArea getDictionaries() {
        return dictionaries;
    }

    public void setDictionaries(DictionariesTextArea dictionaries) {
        this.dictionaries = dictionaries;
    }

    public SegmentPropertiesArea getSegmentPropertiesArea() {
        return segmentPropertiesArea;
    }

    public void setSegmentPropertiesArea(SegmentPropertiesArea segmentPropertiesArea) {
        this.segmentPropertiesArea = segmentPropertiesArea;
    }

    @SuppressWarnings("unused")
    public SpellCheckerManager getSpellCheckerManager() {
        return spellCheckerManager;
    }

    public void setSpellCheckerManager(SpellCheckerManager spellCheckerManager) {
        this.spellCheckerManager = spellCheckerManager;
    }

    /** Get spell checker instance. */
    public ISpellChecker getCurrentSpellChecker() {
        return spellCheckerManager.getCurrentSpellChecker();
    }

    private final List<IProjectEventListener> projectEventListeners = new CopyOnWriteArrayList<>();
    private final List<IApplicationEventListener> applicationEventListeners = new CopyOnWriteArrayList<>();
    private final List<IEntryEventListener> entryEventListeners = new CopyOnWriteArrayList<>();
    private final List<IFontChangedEventListener> fontChangedEventListeners = new CopyOnWriteArrayList<>();
    private final List<IEditorEventListener> editorEventListeners = new CopyOnWriteArrayList<>();


    /** Register listener. */
    public void registerProjectChangeListener(final IProjectEventListener listener) {
        projectEventListeners.add(listener);
    }

    /** Unregister listener. */
    public void unregisterProjectChangeListener(final IProjectEventListener listener) {
        projectEventListeners.remove(listener);
    }

    /** Register listener. */
    public void registerApplicationEventListener(final IApplicationEventListener listener) {
        applicationEventListeners.add(listener);
    }

    /** Unregister listener. */
    public void unregisterApplicationEventListener(final IApplicationEventListener listener) {
        applicationEventListeners.remove(listener);
    }

    /** Register listener. */
    public void registerEntryEventListener(final IEntryEventListener listener) {
        entryEventListeners.add(listener);
    }

    /** Unregister listener. */
    public void unregisterEntryEventListener(final IEntryEventListener listener) {
        entryEventListeners.remove(listener);
    }

    /** Register listener. */
    public void registerFontChangedEventListener(final IFontChangedEventListener listener) {
        fontChangedEventListeners.add(listener);
    }

    /** Unregister listener. */
    public void unregisterFontChangedEventListener(final IFontChangedEventListener listener) {
        fontChangedEventListeners.remove(listener);
    }

    /** Register listener. */
    public void registerEditorEventListener(final IEditorEventListener listener) {
        editorEventListeners.add(listener);
    }

    /** Unregister listener. */
    public void unregisterEditorEventListener(final IEditorEventListener listener) {
        editorEventListeners.remove(listener);
    }

    /** Fire event. */
    public void fireProjectChange(final IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
        SwingUtilities.invokeLater(() -> {
            Log.logInfoRB("LOG_INFO_EVENT_PROJECT_CHANGE", eventType);
            for (IProjectEventListener listener : projectEventListeners) {
                try {
                    listener.onProjectChanged(eventType);
                } catch (Throwable t) {
                    log("ERROR_EVENT_PROJECT_CHANGE", t);
                }
            }
        });
    }

    /** Fire event. */
    public void fireApplicationStartup() {
        SwingUtilities.invokeLater(() -> {
            Log.logInfoRB("LOG_INFO_EVENT_APPLICATION_STARTUP");
            for (IApplicationEventListener listener : applicationEventListeners) {
                try {
                    listener.onApplicationStartup();
                } catch (Throwable t) {
                    log("ERROR_EVENT_APPLICATION_STARTUP", t);
                }
            }
        });
    }

    /** Fire event. */
    public void fireApplicationShutdown() {
        // We shouldn't invoke it later, because need to shutdown immediately.
        Log.logInfoRB("LOG_INFO_EVENT_APPLICATION_SHUTDOWN");
        for (IApplicationEventListener listener : applicationEventListeners) {
            try {
                listener.onApplicationShutdown();
            } catch (Throwable t) {
                log("ERROR_EVENT_APPLICATION_SHUTDOWN", t);
            }
        }
    }

    /** Fire event. */
    public void fireEntryNewFile(final String activeFileName) {
        SwingUtilities.invokeLater(() -> {
            Log.logInfoRB("LOG_INFO_EVENT_ENTRY_NEWFILE", activeFileName);
            for (IEntryEventListener listener : entryEventListeners) {
                try {
                    listener.onNewFile(activeFileName);
                } catch (Throwable t) {
                    log("ERROR_EVENT_ENTRY_NEWFILE", t);
                }
            }
        });
    }

    /** Fire event. */
    public void fireEntryActivated(final SourceTextEntry newEntry) {
        SwingUtilities.invokeLater(() -> {
            Log.logInfoRB("LOG_INFO_EVENT_ENTRY_ACTIVATED");
            for (IEntryEventListener listener : entryEventListeners) {
                try {
                    listener.onEntryActivated(newEntry);
                } catch (Throwable t) {
                    log("ERROR_EVENT_ENTRY_ACTIVATED", t);
                }
            }
        });
    }

    /** Fire event. */
    public void fireFontChanged(final Font newFont) {
        SwingUtilities.invokeLater(() -> {
            Log.logInfoRB("LOG_INFO_EVENT_FONT_CHANGED");
            for (IFontChangedEventListener listener : fontChangedEventListeners) {
                try {
                    listener.onFontChanged(newFont);
                } catch (Throwable t) {
                    log("ERROR_EVENT_FONT_CHANGED", t);
                }
            }
        });
    }

    /** Fire event. */
    public void fireEditorNewWord(final String newWord) {
        SwingUtilities.invokeLater(() -> {
            for (IEditorEventListener listener : editorEventListeners) {
                try {
                    listener.onNewWord(newWord);
                } catch (Throwable t) {
                    log("ERROR_EVENT_EDITOR_NEW_WORD", t);
                }
            }
        });
    }

    private void log(String msgKey, Throwable t) {
        Log.logErrorRB(t, msgKey);
        if (getMainWindow() != null) {
            getMainWindow().displayErrorRB(t, msgKey);
        }
    }
}
