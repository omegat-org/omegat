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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.omegat.cli.BaseSubCommand;
import org.omegat.cli.SubCommands;
import org.omegat.core.data.CoreState;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.machinetranslators.MachineTranslatorsManager;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.core.spellchecker.ISpellChecker;
import org.omegat.core.spellchecker.SpellCheckerManager;
import org.omegat.core.tagvalidation.ITagValidation;
import org.omegat.core.tagvalidation.TagValidationTool;
import org.omegat.core.threads.IAutoSave;
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
import org.omegat.gui.editor.autocompleter.AbstractAutoCompleterView;
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
@NullMarked
public final class Core {

    private Core() {
    }

    // package-private for test fixture TestCoreInitializer
    private static final ReentrantLock EXCLUSIVE_RUN_LOCK = new ReentrantLock();

    private static final List<String> PLUGINS_LOADING_ERRORS = Collections
            .synchronizedList(new ArrayList<>());

    private static final List<IMarker> MARKERS = new ArrayList<>();

    /** Get project instance. */
    public static IProject getProject() {
        return CoreState.getInstance().getProject();
    }

    /** Set new current project. */
    public static void setProject(final IProject newCurrentProject) {
        CoreState.getInstance().setProject(newCurrentProject);
    }

    /** Get main window instance. */
    public static IMainWindow getMainWindow() {
        return CoreState.getInstance().getMainWindow();
    }

    /** Get editor instance. */
    public static IEditor getEditor() {
        return CoreState.getInstance().getEditor();
    }

    /** Get tag validation component instance. */
    public static ITagValidation getTagValidation() {
        return CoreState.getInstance().getTagValidation();
    }

    public static IIssues getIssues() {
        return CoreState.getInstance().getIssuesWindow();
    }

    /** Get matcher component instance. */
    public static IMatcher getMatcher() {
        return CoreState.getInstance().getMatcher();
    }

    /** Get spell checker instance. */
    public static ISpellChecker getSpellChecker() {
        return CoreState.getInstance().getCurrentSpellChecker();
    }

    public static FilterMaster getFilterMaster() {
        return CoreState.getInstance().getFilterMaster();
    }

    public static void setFilterMaster(FilterMaster newFilterMaster) {
        CoreState.getInstance().setFilterMaster(newFilterMaster);
        EntryKey.setIgnoreFileContext(newFilterMaster.getConfig().isIgnoreFileContext());
    }

    public static IProjectFilesList getProjectFilesList() {
        return CoreState.getInstance().getProjWin();
    }

    public static MachineTranslateTextArea getMachineTranslatePane() {
        return CoreState.getInstance().getMachineTranslatePane();
    }

    public static IAutoSave getAutoSave() {
        return CoreState.getInstance().getAutoSave();
    }

    /** Get glossary instance. */
    public static IGlossaries getGlossary() {
        return CoreState.getInstance().getGlossaries();
    }

    public static GlossaryManager getGlossaryManager() {
        return CoreState.getInstance().getGlossaryManager();
    }

    /** Get notes instance. */
    public static INotes getNotes() {
        return CoreState.getInstance().getNotes();
    }

    /** Get segment properties area */
    @SuppressWarnings("unused")
    public static SegmentPropertiesArea getSegmentPropertiesArea() {
        return CoreState.getInstance().getSegmentPropertiesArea();
    }

    /**
     * Get comments area
     *
     * @return the comment area
     */
    public static IComments getComments() {
        return CoreState.getInstance().getComments();
    }

    public static IDictionaries getDictionaries() {
        return CoreState.getInstance().getDictionaries();
    }

    public static Segmenter getSegmenter() {
        return CoreState.getInstance().getSegmenter();
    }

    public static void setSegmenter(Segmenter newSegmenter) {
        CoreState.getInstance().setSegmenter(newSegmenter);
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
    @SuppressWarnings("unused")
    public static void initializeGUI(ClassLoader cl, Map<String, String> params) throws Exception {
        initializeGUI(params);
    }

    /**
     * Initialize application components.
     */
    public static void initializeGUI(final Map<String, String> params) throws Exception {
        CoreState coreState = CoreState.getInstance();
        coreState.setCmdLineParams(params);

        // 1. Initialize project
        coreState.setProject(new NotLoadedProject());

        // 2. Initialize theme
        UIDesignManager.initialize();

        // 3. Initialize application frame
        MainWindow me = new MainWindow();
        coreState.setMainWindow(me);

        initializeGUIimpl(me);

        coreState.initializeSaveThread();
        coreState.initializeVersionCheckThread();
    }

    /**
     * initialize GUI body.
     * @throws Exception when an unexpected error happened.
     */
    static void initializeGUIimpl(IMainWindow me) throws Exception {
        MarkerController.init();
        LanguageToolWrapper.init();

        CoreState coreState = CoreState.getInstance();
        coreState.setSegmenter(new Segmenter(Preferences.getSRX()));
        coreState.setFilterMaster(new FilterMaster(Preferences.getFilters()));
        coreState.setMachineTranslatorsManager(new MachineTranslatorsManager());

        // 4. Initialize other components. They add themselves to the main
        // window.
        coreState.setEditor(new EditorController(me));
        coreState.setTagValidation(new TagValidationTool());
        coreState.setIssuesWindow(new IssuesPanelController(me.getApplicationFrame()));
        coreState.setMatcher(new MatchesTextArea(me));
        GlossaryTextArea glossaryArea = new GlossaryTextArea(me);
        coreState.setGlossaries(glossaryArea);
        coreState.setGlossaryManager(new GlossaryManager(glossaryArea));
        coreState.setNotes(new NotesTextArea(me));
        coreState.setComments(new CommentsTextArea(me));
        coreState.setMachineTranslatePane(new MachineTranslateTextArea(me));
        coreState.setDictionaries(new DictionariesTextArea(me));
        coreState.setSpellCheckerManager(new SpellCheckerManager());
        // Create an independent instance updated from SearchThead.
        new MultipleTransPane(me);
        // Create an independent instance updated by events.
        coreState.setSegmentPropertiesArea(new SegmentPropertiesArea(me));
        coreState.setProjWin(new ProjectFilesListController());
    }

    /**
     * Initialize application components.
     */
    public static void initializeConsole(final Map<String, String> params) {
        CoreState coreState = CoreState.getInstance();
        coreState.setCmdLineParams(params);
        coreState.setTagValidation(new TagValidationTool());
        coreState.setProject(new NotLoadedProject());
        coreState.setMainWindow(new ConsoleWindow());
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
        return CoreState.getInstance().getCmdLineParams();
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
     * Register autocompleter.
     */
    public static void registerAutoCompleterClass(Class<? extends AbstractAutoCompleterView> clazz) {
        PluginUtils.getAutoCompleterViewsClasses().add(clazz);
    }

    /**
     * Registers a console command by associating a command name with its corresponding subcommand class.
     * This method adds the specified command to a collection of subcommands, enabling it to be later
     * utilized or executed through the command-line interface.
     *
     * @param name       the subcommand name of the console command to be registered.
     * @param subcommand the class representing the subcommand implementation associated with the given name.
     */
    public static void registerConsoleCommand(String name, Class<? extends BaseSubCommand> subcommand) {
        SubCommands.registerConsoleCommand(name, subcommand);
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
     *            Throw an exception from runnable if received.
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

    private static StackTraceElement @Nullable [] runningStackTrace;

    public interface RunnableWithException {
        void run() throws Exception;
    }
}
