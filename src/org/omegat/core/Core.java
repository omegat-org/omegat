/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
               2010 Wildrich Fourie
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

package org.omegat.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.core.spellchecker.SpellCheckerManager;
import org.omegat.core.tagvalidation.ITagValidation;
import org.omegat.core.tagvalidation.TagValidationTool;
import org.omegat.core.threads.IAutoSave;
import org.omegat.core.threads.SaveThread;
import org.omegat.core.threads.VersionCheckThread;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.comments.CommentsTextArea;
import org.omegat.gui.comments.IComments;
import org.omegat.gui.dictionaries.DictionariesTextArea;
import org.omegat.gui.dictionaries.IDictionaries;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.MarkerController;
import org.omegat.gui.editor.mark.IMarker;
import org.omegat.gui.exttrans.IMachineTranslation;
import org.omegat.gui.exttrans.MachineTranslateTextArea;
import org.omegat.gui.filelist.IProjectFilesList;
import org.omegat.gui.filelist.ProjectFilesListController;
import org.omegat.gui.glossary.GlossaryManager;
import org.omegat.gui.glossary.GlossaryTextArea;
import org.omegat.gui.glossary.IGlossaries;
import org.omegat.gui.issues.IIssues;
import org.omegat.gui.issues.IssuesPanelController;
import org.omegat.gui.main.ConsoleWindow;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.main.MainWindow;
import org.omegat.gui.matches.IMatcher;
import org.omegat.gui.matches.MatchesTextArea;
import org.omegat.gui.multtrans.MultipleTransPane;
import org.omegat.gui.notes.INotes;
import org.omegat.gui.notes.NotesTextArea;
import org.omegat.gui.properties.SegmentPropertiesArea;
import org.omegat.languagetools.LanguageToolWrapper;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Preferences;
import org.omegat.util.gui.UIDesignManager;

/**
 * Class which contains all components' instances.
 * <p>
 * Note about threads synchronization: each component must have only local
 * synchronization. It mustn't synchronize around other components or some other
 * objects.
 * <p>
 * Components that work in Swing UI thread can have another synchronization
 * idea: it cannot be synchronized to access to some data that changed only in
 * UI thread.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Wildrich Fourie
 */
public final class Core {

    private Core() {
    }

    private static IProject currentProject;
    private static IMainWindow mainWindow;
    // package-private for test fixture TestCoreInitializer
    static IEditor editor;
    private static ITagValidation tagValidation;
    private static IIssues issuesWindow;
    private static IMatcher matcher;
    private static FilterMaster filterMaster;
    private static IProjectFilesList projWin;

    // package-private for test fixture TestCoreInitializer
    static IAutoSave saveThread;
    private static final ReentrantLock EXCLUSIVE_RUN_LOCK = new ReentrantLock();

    // package-private for test fixture TestCoreInitializer
    static IGlossaries glossary;
    private static GlossaryManager glossaryManager;
    private static MachineTranslateTextArea machineTranslatePane;
    private static DictionariesTextArea dictionaries;
    private static INotes notes;
    private static IComments comments;
    private static Segmenter segmenter;
    private static SegmentPropertiesArea segmentPropertiesArea;

    private static Map<String, String> cmdLineParams = Collections.emptyMap();

    private static final List<String> PLUGINS_LOADING_ERRORS = Collections.synchronizedList(new ArrayList<>());
    private static final List<IMarker> MARKERS = new ArrayList<>();

    /** Get project instance. */
    public static IProject getProject() {
        return currentProject;
    }

    /** Set new current project. */
    public static void setProject(final IProject newCurrentProject) {
        currentProject = newCurrentProject;
    }

    /** Get main window instance. */
    public static IMainWindow getMainWindow() {
        return mainWindow;
    }

    /** Get editor instance. */
    public static IEditor getEditor() {
        return editor;
    }

    /** Get tag validation component instance. */
    public static ITagValidation getTagValidation() {
        return tagValidation;
    }

    public static IIssues getIssues() {
        return issuesWindow;
    }

    /** Get matcher component instance. */
    public static IMatcher getMatcher() {
        return matcher;
    }

    private static SpellCheckerManager spellCheckerManager;

    /** Get spell checker instance. */
    public static ISpellChecker getSpellChecker() {
        return spellCheckerManager.getCurrentSpellChecker();
    }

    public static FilterMaster getFilterMaster() {
        return filterMaster;
    }

    public static void setFilterMaster(FilterMaster newFilterMaster) {
        filterMaster = newFilterMaster;
        EntryKey.setIgnoreFileContext(newFilterMaster.getConfig().isIgnoreFileContext());
    }

    public static IProjectFilesList getProjectFilesList() {
        return projWin;
    }

    public static MachineTranslateTextArea getMachineTranslatePane() {
        return machineTranslatePane;
    }

    public static IAutoSave getAutoSave() {
        return saveThread;
    }

    /** Get glossary instance. */
    public static IGlossaries getGlossary() {
        return glossary;
    }

    public static GlossaryManager getGlossaryManager() {
        return glossaryManager;
    }

    /** Get notes instance. */
    public static INotes getNotes() {
        return notes;
    }

    /** Get segment properties area */
    public static SegmentPropertiesArea getSegmentPropertiesArea() {
        return segmentPropertiesArea;
    }

    /**
     * Get comments area
     *
     * @return the comment area
     */
    public static IComments getComments() {
        return comments;
    }

    public static IDictionaries getDictionaries() {
        return dictionaries;
    }

    public static Segmenter getSegmenter() {
        return segmenter;
    }

    public static void setSegmenter(Segmenter newSegmenter) {
        segmenter = newSegmenter;
    }

    /**
     * initialize GUI.
     * <p>
     * An interface that was introduced in v5.6.0 when supporting theme plugin.
     *
     * @param cl class loader.
     * @param params CLI parameters.
     * @throws Exception when error occurred.
     * @deprecated since 6.1.0
     */
    @Deprecated(since = "6.1.0", forRemoval = true)
    public static void initializeGUI(ClassLoader cl, Map<String, String> params) throws Exception {
        initializeGUI(params);
    }

    /**
     * Initialize application components.
     */
    public static void initializeGUI(final Map<String, String> params) throws Exception {
        cmdLineParams = params;

        // 1. Initialize project
        currentProject = new NotLoadedProject();

        // 2. Initialize theme
        UIDesignManager.initialize();

        // 3. Initialize application frame
        MainWindow me = new MainWindow();
        mainWindow = me;

        initializeGUIimpl(me);

        SaveThread th = new SaveThread();
        saveThread = th;
        th.start();
        new VersionCheckThread(10).start();
    }

    /**
     * initialize GUI body.
     * @throws Exception when an unexpected error happened.
     */
    static void initializeGUIimpl(IMainWindow me) throws Exception {
        MarkerController.init();
        LanguageToolWrapper.init();

        segmenter = new Segmenter(Preferences.getSRX());
        filterMaster = new FilterMaster(Preferences.getFilters());

        // 4. Initialize other components. They add themselves to the main window.
        editor = new EditorController(me);
        tagValidation = new TagValidationTool();
        issuesWindow = new IssuesPanelController(me.getApplicationFrame());
        matcher = new MatchesTextArea(me);
        GlossaryTextArea glossaryArea = new GlossaryTextArea(me);
        glossary = glossaryArea;
        glossaryManager = new GlossaryManager(glossaryArea);
        notes = new NotesTextArea(me);
        comments = new CommentsTextArea(me);
        machineTranslatePane = new MachineTranslateTextArea(me);
        dictionaries = new DictionariesTextArea(me);
        spellCheckerManager = new SpellCheckerManager();
        // Create an independent instance updated from SearchThead.
        new MultipleTransPane(me);
        // Create an independent instance updated by events.
        segmentPropertiesArea = new SegmentPropertiesArea(me);
        projWin = new ProjectFilesListController();
    }

    /**
     * Initialize application components.
     */
    public static void initializeConsole(final Map<String, String> params) {
        cmdLineParams = params;
        tagValidation = new TagValidationTool();
        currentProject = new NotLoadedProject();
        mainWindow = new ConsoleWindow();
    }

    /**
     * Register class for calculate marks.
     *
     * @param marker
     *            marker implementation
     */
    public static void registerMarker(IMarker marker) {
        MARKERS.add(marker);
    }

    public static List<IMarker> getMarkers() {
        return MARKERS;
    }

    public static Map<String, String> getParams() {
        return cmdLineParams;
    }

    public static void registerFilterClass(Class<? extends IFilter> clazz) {
        PluginUtils.getFilterClasses().add(clazz);
    }

    public static void registerMachineTranslationClass(Class<? extends IMachineTranslation> clazz) {
        PluginUtils.getMachineTranslationClasses().add(clazz);
    }

    public static void registerMarkerClass(Class<? extends IMarker> clazz) {
        PluginUtils.getMarkerClasses().add(clazz);
    }

    public static void registerTokenizerClass(Class<? extends ITokenizer> clazz) {
        PluginUtils.getTokenizerClasses().add(clazz);
    }

    /**
     * Register spellchecker plugin.
     * @param clazz spellchecker class.
     */
    public static void registerSpellCheckClass(Class<? extends ISpellChecker> clazz) {
        PluginUtils.getSpellCheckClasses().add(clazz);
    }

    /**
     * Get all plugin loading errors.
     */
    public static List<String> getPluginsLoadingErrors() {
        return PLUGINS_LOADING_ERRORS;
    }

    /**
     * Any plugin can call this method for say about error on loading.
     */
    public static void pluginLoadingError(String errorText) {
        PLUGINS_LOADING_ERRORS.add(errorText);
    }

    /**
     * Use this to perform operations that must not be run concurrently.
     * <p>
     * For instance project load/save/compile/autosave operations must not be executed in parallel because it will break
     * project files, especially during team synchronization. For guaranteed non-parallel execution, all such operations
     * must be executed via this method.
     *
     * @param waitForUnlock
     *            should execution wait for unlock 3 minutes
     * @param run
     *            code for execute
     * @throws Exception
     */
    public static void executeExclusively(boolean waitForUnlock, RunnableWithException run)
            throws Exception {
        if (!EXCLUSIVE_RUN_LOCK.tryLock(waitForUnlock ? 180000 : 1, TimeUnit.MILLISECONDS)) {
            Exception ex = new TimeoutException("Timeout waiting for previous exclusive execution");
            Exception cause = new Exception("Previous exclusive execution");
            if (runningStackTrace != null) {
                cause.setStackTrace(runningStackTrace);
                ex.initCause(cause);
            }
            throw ex;
        }
        try {
            runningStackTrace = new Exception().getStackTrace();
            run.run();
        } finally {
            runningStackTrace = null;
            EXCLUSIVE_RUN_LOCK.unlock();
        }
    }

    private static StackTraceElement[] runningStackTrace;

    public interface RunnableWithException {
        void run() throws Exception;
    }

    // -- methods for testing

    /**
     * Set main window instance for unit tests.
     *
     * @param mainWindow main window object to hold.
     */
    @VisibleForTesting
    static void setMainWindow(@Nullable IMainWindow mainWindow) {
        Core.mainWindow = mainWindow;
    }

    /**
     * Set project instance for unit tests.
     *
     * @param currentProject project object to hold.
     */
    @VisibleForTesting
    static void setCurrentProject(IProject currentProject) {
        Core.currentProject = currentProject;
    }

    @VisibleForTesting
    static void setEditor(IEditor newEditor) {
        editor = newEditor;
    }

    @VisibleForTesting
    static void setSaveThread(IAutoSave newSewAutoSave) {
        saveThread = newSewAutoSave;
    }

    @VisibleForTesting
    static void setGlossary(IGlossaries newGlossary) {
        glossary = newGlossary;
    }

    @VisibleForTesting
    static void setNotes(INotes newNotes) {
        notes = newNotes;
    }

}
