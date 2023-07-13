/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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

import java.awt.Cursor;
import java.awt.Font;
import java.awt.HeadlessException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JFrame;
import javax.swing.JMenu;

import org.madlonkay.supertmxmerge.StmProperties;
import org.madlonkay.supertmxmerge.SuperTmxMerge;
import org.madlonkay.supertmxmerge.data.ITuv;
import org.madlonkay.supertmxmerge.data.Key;
import org.madlonkay.supertmxmerge.data.ResolutionStrategy;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.IProject.DefaultTranslationsIterator;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.threads.IAutoSave;
import org.omegat.gui.editor.EditorSettings;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.editor.IPopupMenuConstructor;
import org.omegat.gui.editor.autocompleter.IAutoCompleter;
import org.omegat.gui.editor.mark.Mark;
import org.omegat.gui.main.IMainMenu;
import org.omegat.gui.main.IMainWindow;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.TestPreferencesInitializer;
import org.omegat.util.gui.MenuExtender.MenuKey;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingDesktop;
import gen.core.project.RepositoryDefinition;

/**
 * Child process for concurrent modification.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class TestTeamIntegrationChild {

    private TestTeamIntegrationChild() {
    }

    static final String CONCURRENT_NAME = "concurrent";

    static long finishTime;
    static String source;
    static String dir;
    static String repo;
    static int maxDelaySeconds;
    static int segCount;
    static EntryKey[] key;
    static SourceTextEntry[] ste;
    static EntryKey keyC;
    static SourceTextEntry steC;
    static long num = 0;
    static long[] v;
    static Map<String, Long> values = new HashMap<String, Long>();

    public static void main(String[] args) throws Exception {
        if (args.length != 6) {
            System.err.println("Wrong arguments count");
            System.exit(1);
        }
        try {
            source = args[0];
            long time = Long.parseLong(args[1]);
            dir = args[2];
            repo = args[3];
            maxDelaySeconds = Integer.parseInt(args[4]);
            segCount = Integer.parseInt(args[5]);

            finishTime = System.currentTimeMillis() + time;

            TestPreferencesInitializer.init();
            Preferences.setPreference(Preferences.TEAM_AUTHOR, source);

            // Init UI stubs. In a CI environment, errors can occur when
            // initializing the RemoteRepositoryProvider, so we need to make
            // sure the "main window" is available for logging first.
            Core.initializeConsole(Collections.emptyMap());
            TestCoreInitializer.initMainWindow(mainWindow);
            TestCoreInitializer.initAutoSave(autoSave);
            TestCoreInitializer.initEditor(editor);

            ProjectProperties config = TestTeamIntegration.createConfig(repo, new File(dir));
            new RemoteRepositoryProvider(config.getProjectRootDir(), config.getRepositories(), config);

            // load project
            ProjectProperties projectProperties = ProjectFileStorage.loadProjectProperties(new File(dir));
            projectProperties.autocreateDirectories();
            String remoteRepoUrl = getRootGitRepositoryMapping(projectProperties.getRepositories());
            if (!repo.equals(remoteRepoUrl)) {
                setRootGitRepositoryMapping(projectProperties.getRepositories(), repo);
            }

            Core.getAutoSave().disable();
            RealProject p = new TestRealProject(projectProperties);
            Core.setProject(p);
            p.loadProject(true);
            if (p.isProjectLoaded()) {
                Core.getAutoSave().enable();
                CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
            } else {
                throw new Exception("Project can't be loaded");
            }

            key = new EntryKey[segCount];
            ste = new SourceTextEntry[segCount];
            for (int c = 0; c < segCount; c++) {
                key[c] = new EntryKey("file", source + "/" + c, null, null, null, null);
                ste[c] = new SourceTextEntry(key[c], 0, null, null, new ArrayList<ProtectedPart>());
            }
            keyC = new EntryKey("file", CONCURRENT_NAME, null, null, null, null);
            steC = new SourceTextEntry(keyC, 0, null, null, new ArrayList<ProtectedPart>());

            v = new long[segCount];
            mc: while (true) {
                for (int c = 1; c < segCount; c++) {
                    // change concurrent segment
                    changeConcurrent();
                    if (System.currentTimeMillis() >= finishTime) {
                        break mc;
                    }
                    // change /0 segment
                    Thread.sleep(ThreadLocalRandom.current().nextInt(maxDelaySeconds * 1000) + 10);
                    checksavecheck(0);

                    // change /1..N segment
                    Thread.sleep(ThreadLocalRandom.current().nextInt(maxDelaySeconds * 1000) + 10);
                    checksavecheck(c);
                }
            }
            Core.getProject().closeProject();

            // load again and check
            ProjectFactory.loadProject(projectProperties, true);
            checkAll();

            // check projectProperties
            checkRepoUrl(projectProperties);

            System.exit(200);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    static void checkRepoUrl(ProjectProperties prop) {
        for (RepositoryDefinition repository : prop.getRepositories()) {
            if (repository.getUrl().equals(repo)) {
                return;
            }
        }
        throw new RuntimeException("Wrong url in repository. expected: " + repo
                + ": actual: " + prop.getRepositories().get(0).getUrl());
    }

    private static String getRootGitRepositoryMapping(List<RepositoryDefinition> repos) {
        String repoUrl = null;
        for (RepositoryDefinition definition : repos) {
            if (definition.getMapping().get(0).getLocal().equals("/") && definition.getMapping().get(0).getRepository().equals("/") && definition.getType().equals("git")) {
                repoUrl = definition.getUrl();
                break;
            }
        }
        return repoUrl;
    }

    private static void setRootGitRepositoryMapping(List<RepositoryDefinition> repos, String repoUrl) {
        for (RepositoryDefinition definition : repos) {
            if (definition.getMapping().get(0).getLocal().equals("/") && definition.getMapping().get(0).getRepository().equals("/") && definition.getType().equals("git")) {
                definition.setUrl(repoUrl);
                repos.set(0, definition);
                break;
            }
        }
    }


    static void changeConcurrent() throws Exception {
        checkAll();

        PrepareTMXEntry prep = new PrepareTMXEntry();
        prep.translation = "" + System.currentTimeMillis();
        Core.getProject().setTranslation(steC, prep, true, null);
        Log.log("Wrote: " + prep.source + "=" + prep.translation);
    }

    static void checksavecheck(int index) throws Exception {
        checkAll();

        v[index] = ++num;
        saveTranslation(ste[index], v[index]);

        checkAll();
    }

    /**
     * Check in memory and in file.
     */
    static void checkAll() throws Exception {
        ProjectTMX tmx = new ProjectTMX(TestTeamIntegration.SRC_LANG, TestTeamIntegration.TRG_LANG, false,
                new File(dir + "/omegat/project_save.tmx"), TestTeamIntegration.checkOrphanedCallback);
        for (int c = 0; c < segCount; c++) {
            checkTranslation(c);
            checkTranslationFromFile(tmx, c);
        }

        Core.getProject().iterateByDefaultTranslations(new DefaultTranslationsIterator() {
            public void iterate(String source, TMXEntry trans) {
                Long prev = values.get(source);
                if (prev == null) {
                    prev = 0L;
                }
                long curr = Long.parseLong(trans.translation);
                if (curr < prev) {
                    throw new RuntimeException(source + ": Wrong value in " + source + ": current(" + curr
                            + ") less than previous(" + prev + ")");
                }
            }
        });
    }

    static void checkTranslation(int index) {
        TMXEntry en = Core.getProject().getTranslationInfo(ste[index]);
        String sv = en == null || !en.isTranslated() ? "" : en.translation;
        if (v[index] == 0 && sv.isEmpty()) {
            return;
        }
        if ((v[index] + "").equals(sv)) {
            return;
        }
        throw new RuntimeException(source + ": Wrong value in " + source + "/" + index + ": expected "
                + v[index] + " but contains " + en.translation);
    }

    static void checkTranslationFromFile(ProjectTMX tmx, int index) throws Exception {
        TMXEntry en = tmx.getDefaultTranslation(ste[index].getSrcText());
        String sv = en == null || !en.isTranslated() ? "" : en.translation;
        if (v[index] == 0 && sv.isEmpty()) {
            return;
        }
        if ((v[index] + "").equals(sv)) {
            return;
        }
        throw new RuntimeException(source + ": Wrong value in TMX " + source + "/" + index + ": expected "
                + v[index] + " but contains " + sv);
    }

    /**
     * Save new translation.
     */
    static void saveTranslation(SourceTextEntry ste, long value) {
        PrepareTMXEntry prep = new PrepareTMXEntry();
        prep.translation = "" + value;
        Core.getProject().setTranslation(ste, prep, true, null);
        Log.log("Wrote: " + prep.source + "=" + prep.translation);
        Core.getProject().saveProject(true);
    }

    static IAutoSave autoSave = new IAutoSave() {
        public void enable() {
        }

        public void disable() {
        }
    };

    static IEditor editor = new IEditor() {

        public void windowDeactivated() {
        }

        public void undo() {
        }

        public void setFilter(IEditorFilter filter) {
        }

        public void setAlternateTranslationForCurrentEntry(boolean alternate) {
        }

        public void requestFocus() {
        }

        public void replaceEditTextAndMark(String text) {
        }

        public void replaceEditText(String text) {
        }

        public void replaceEditTextAndMark(final String text, final String origin) {
        }

        public void removeFilter() {
        }

        public void remarkOneMarker(String markerClassName) {
        }

        public void registerUntranslated() {
        }

        public void registerPopupMenuConstructors(int priority, IPopupMenuConstructor constructor) {
        }

        public void registerIdenticalTranslation() {
        }

        public void registerEmptyTranslation() {
        }

        public void refreshViewAfterFix(List<Integer> fixedEntries) {
        }

        public void refreshView(boolean doCommit) {
        }

        public void redo() {
        }

        public void prevEntryWithNote() {
        }

        public void prevEntry() {
        }

        public void nextXAutoEntry() {
        }

        public void prevXAutoEntry() {
        }

        public void nextXEnforcedEntry() {
        }

        public void prevXEnforcedEntry() {
        }

        public void nextUntranslatedEntry() {
        }

        public void nextUniqueEntry() {
        }

        public void nextTranslatedEntry() {
        }

        public void nextEntryWithNote() {
        }

        public void nextEntry() {
        }

        public void markActiveEntrySource(SourceTextEntry requiredActiveEntry, List<Mark> marks,
                String markerClassName) {
        }

        public void insertText(String text) {
        }

        public void insertTag(String tag) {
        }

        public void gotoHistoryForward() {
        }

        public void gotoHistoryBack() {
        }

        public void gotoFile(int fileIndex) {
        }

        public void gotoEntryAfterFix(int fixedEntry, String fixedSource) {
        }

        public void gotoEntry(String srcString, EntryKey key) {
        }

        public void gotoEntry(int entryNum) {
        }

        public void gotoEntry(int entryNum, CaretPosition pos) {
        }

        public EditorSettings getSettings() {
            return null;
        }

        public String getSelectedText() {
            return null;
        }

        @Override
        public void selectSourceText() {
        }

        public IEditorFilter getFilter() {
            return null;
        }

        public String getCurrentTranslation() {
            return null;
        }

        public String getCurrentFile() {
            return null;
        }

        public int getCurrentEntryNumber() {
            return 0;
        }

        public SourceTextEntry getCurrentEntry() {
            return null;
        }

        public void commitAndLeave() {
        }

        public void commitAndDeactivate() {
        }

        public void changeCase(CHANGE_CASE_TO newCase) {
        }

        @Override
        public void replaceEditText(final String text, final String origin) {

        }

        public void activateEntry() {
        }

        @Override
        public IAutoCompleter getAutoCompleter() {
            return null;
        }

        @Override
        public String getCurrentTargetFile() {
            return null;
        }

        @Override
        public void insertTextAndMark(String text) {
        }
    };

    static IMainWindow mainWindow = new IMainWindow() {
        public void unlockUI() {
        }

        public void showStatusMessageRB(String messageKey, Object... params) {
        }

        public void showTimedStatusMessageRB(String messageKey, Object... params) {
        }

        public void showProgressMessage(String messageText) {
        }

        public void showMessageDialog(String message) {
        }

        public void showLengthMessage(String messageText) {
        }

        public void showErrorDialogRB(String title, String message, Object... args) {
            System.err.println(message);
        }

        public int showConfirmDialog(Object message, String title, int optionType, int messageType)
                throws HeadlessException {
            return 0;
        }

        public void setCursor(Cursor cursor) {
        }

        public void lockUI() {
        }

        IMainMenu menu = new IMainMenu() {

            public JMenu getToolsMenu() {
                return null;
            }

            public JMenu getProjectMenu() {
                return new JMenu();
            }

            public JMenu getOptionsMenu() {
                return null;
            }

            public JMenu getMachineTranslationMenu() {
                return null;
            }

            public JMenu getGlossaryMenu() {
                return null;
            }

            public JMenu getAutoCompletionMenu() {
                return null;
            }

            @Override
            public JMenu getHelpMenu() {
                return null;
            }

            @Override
            public JMenu getMenu(MenuKey marker) {
                return null;
            }

            public void invokeAction(String action, int modifiers) {
            }
        };

        public IMainMenu getMainMenu() {
            return menu;
        }

        @Override
        public DockingDesktop getDesktop() {
            return null;
        }

        public Cursor getCursor() {
            return null;
        }

        public JFrame getApplicationFrame() {
            return null;
        }

        public Font getApplicationFont() {
            return null;
        }

        public void displayWarningRB(String warningKey, String supercedesKey, Object... params) {
            System.err.println(warningKey);
        }

        public void displayWarningRB(String warningKey, Object... params) {
            System.err.println(warningKey);
        }

        public void displayErrorRB(Throwable ex, String errorKey, Object... params) {
            System.err.println(errorKey);
            ex.printStackTrace();
        }

        public void addDockable(Dockable pane) {
        }

        public void showLockInsertMessage(String messageText, String toolTip) {
        }
    };

    /**
     * Override RealProject for own merge.
     */
    static class TestRealProject extends RealProject {
        TestRealProject(final ProjectProperties props) {
            super(props);
        }

        ProjectTMX mergedTMX;
        ProjectTMX baseTMX;
        ProjectTMX headTMX;

        @Override
        protected void mergeTMX(ProjectTMX baseTMX, ProjectTMX headTMX, StringBuilder commitDetails) {
            Log.log("Base:   " + baseTMX);
            Log.log("Mine:   " + projectTMX);
            Log.log("Theirs: " + headTMX);
            if (!checkMergeInput(baseTMX, projectTMX)) {
                Log.log("'Mine' TM is not a valid derivative of 'Base' TM");
                // Exceptions thrown here are suppressed in
                // RealProject.saveProject(boolean) so this is the easiest way
                // to early-exit
                System.exit(1);
            }
            if (!checkMergeInput(baseTMX, headTMX)) {
                Log.log("'Theirs' TM is not a valid derivative of 'Base' TM");
                System.exit(1);
            }
            StmProperties props = new StmProperties()
                    .setLanguageResource(OStrings.getResourceBundle())
                    .setResolutionStrategy(new ResolutionStrategy() {
                        @Override
                        public ITuv resolveConflict(Key key, ITuv baseTuv, ITuv projectTuv, ITuv headTuv) {
                            TMXEntry enBase = baseTuv != null ? (TMXEntry) baseTuv
                                    .getUnderlyingRepresentation() : null;
                            TMXEntry enProject = projectTuv != null ? (TMXEntry) projectTuv
                                    .getUnderlyingRepresentation() : null;
                            TMXEntry enHead = headTuv != null ? (TMXEntry) headTuv
                                    .getUnderlyingRepresentation() : null;
                            String s = "Rebase " + src(enProject) + " base=" + tr(enBase) + " head="
                                    + tr(enHead) + " project=" + tr(enProject);
                            if (enProject != null && CONCURRENT_NAME.equals(enProject.source)) {
                                if (v(enHead) < v(enBase)) {
                                    throw new RuntimeException("Rebase HEAD: wrong concurrent: " + s);
                                }
                                if (v(enProject) < v(enBase)) {
                                    throw new RuntimeException("Rebase project: wrong concurrent: " + s);
                                }
                                if (v(enHead) > v(enProject)) {
                                    System.err.println(s + ": result=head");
                                    return headTuv;
                                } else {
                                    System.err.println(s + ": result=project");
                                    return projectTuv;
                                }
                            } else {
                                throw new RuntimeException("Rebase error: non-concurrent entry: " + s);
                            }
                        }
                    });
            String srcLang = config.getSourceLanguage().getLanguage();
            String trgLang = config.getTargetLanguage().getLanguage();
            synchronized (projectTMX) {
                ProjectTMX mergedTMX = SuperTmxMerge.merge(
                        new SyncTMX(baseTMX, OStrings.getString("TMX_MERGE_BASE"), srcLang, trgLang),
                        new SyncTMX(projectTMX, OStrings.getString("TMX_MERGE_MINE"), srcLang, trgLang),
                        new SyncTMX(headTMX, OStrings.getString("TMX_MERGE_THEIRS"), srcLang, trgLang), props);
                Log.log("Merged: " + mergedTMX);
                if (!checkMergeInput(baseTMX, mergedTMX)) {
                    Log.log("'Merged' TM is not a valid derivative of 'Base' TM");
                    System.exit(1);
                }
                projectTMX.replaceContent(mergedTMX);
            }
            commitDetails.append('\n');
            commitDetails.append(props.getReport().toString());
        }

        /**
         * Check a TM against the base TM to ensure it's a valid modification of
         * the base. This integration test never deletes entries, only adds or
         * modifies them, so modified versions must be supersets of their base
         * versions.
         *
         * @param base
         *            Base TM from which the other TM is derived
         * @param other
         *            Other TM
         * @return Valid or not
         */
        boolean checkMergeInput(ProjectTMX base, ProjectTMX other) {
            return base.defaults.keySet().stream().allMatch(other.defaults::containsKey)
                    && base.alternatives.keySet().stream().allMatch(other.alternatives::containsKey);
        }

        protected void mergeTMXOld(ProjectTMX baseTMX, ProjectTMX headTMX) {
            mergedTMX = new ProjectTMX();
            this.baseTMX = baseTMX;
            this.headTMX = headTMX;
            String s = "info";
            for (TMXEntry e : baseTMX.getDefaults()) {
                use(e);
            }
            for (TMXEntry e : headTMX.getDefaults()) {
                TMXEntry eb = baseTMX.getDefaultTranslation(e.source);
                if (CONCURRENT_NAME.equals(e.source)) { // concurrent
                    if (v(eb) > v(e)) {
                        throw new RuntimeException("Rebase HEAD: wrong concurrent" + s);
                    }
                    use(e);
                } else if (e.source.startsWith(source + "/")) { // my segments
                    if (v(eb) != v(e)) {
                        throw new RuntimeException("Rebase HEAD: not equals for current project" + s);
                    }
                } else { // other segments
                    if (v(eb) > v(e)) {
                        throw new RuntimeException("Rebase HEAD: less value" + s);
                    }
                    use(e);
                }
            }
            for (TMXEntry e : projectTMX.getDefaults()) {
                TMXEntry em = mergedTMX.getDefaultTranslation(e.source);
                if (CONCURRENT_NAME.equals(e.source)) { // concurrent
                    if (v(e) > v(em)) {
                        use(e);
                    }
                } else if (e.source.startsWith(source + "/")) { // my segments
                    if (v(e) < v(em)) {
                        throw new RuntimeException("Rebase me: less value" + s);
                    }
                    use(e);
                } else { // other segments
                    use(em);
                }
            }

            projectTMX.replaceContent(mergedTMX);
        }

        void use(TMXEntry en) {
            EntryKey k = new EntryKey("file", en.source, null, null, null, null);
            SourceTextEntry ste = new SourceTextEntry(k, 0, null, en.source, Collections.emptyList());
            mergedTMX.setTranslation(ste, en, true);
        }

        long v(TMXEntry e) {
            if (e == null) {
                return 0;
            } else {
                return Long.parseLong(e.translation);
            }
        }

        String src(TMXEntry e) {
            if (e == null) {
                return "null";
            } else {
                return e.source;
            }
        }

        String tr(TMXEntry e) {
            if (e == null) {
                return "null";
            } else {
                return e.translation;
            }
        }

        String trans(ProjectTMX tmx, TMXEntry e) {
            TMXEntry en = tmx.getDefaultTranslation(e.source);
            if (en == null) {
                return "null";
            } else {
                return en.translation;
            }
        }
    }
}
