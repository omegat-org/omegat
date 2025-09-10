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
import java.awt.Frame;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.core.team2.operation.TMXRebaseOperation;
import org.omegat.core.team2.operation.TestingTMXRebaseOperation;
import org.omegat.core.threads.IAutoSave;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.editor.EditorSettingsStub;
import org.omegat.gui.editor.EditorStub;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.gui.glossary.GlossaryManager;
import org.omegat.gui.glossary.GlossaryReaderTSV;
import org.omegat.gui.glossary.IGlossaries;
import org.omegat.gui.main.ConsoleWindow;
import org.omegat.gui.main.IMainMenu;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.main.MainMenuStub;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.TestPreferencesInitializer;

import com.vlsolutions.swing.docking.Dockable;
import gen.core.project.RepositoryDefinition;

/**
 * Child process for concurrent modification.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class TestTeamIntegrationChild {

    private TestTeamIntegrationChild() {
    }

    public static final String PLUGINS_LIST_FILE = "test-integration/plugins.properties";

    public static final String CONCURRENT_NAME = "concurrent";

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
    static Map<String, Long> values = new HashMap<>();
    static Set<String> glossaries = new HashSet<>();
    static long glossaryIndex = 0;

    public static void main(String[] args) {
        if (args.length != 6) {
            System.err.println("Wrong arguments count");
            System.exit(1);
        }
        Thread.currentThread().setName(args[0]);
        try {
            source = args[0];
            long time = Long.parseLong(args[1]);
            dir = args[2];
            repo = args[3];
            maxDelaySeconds = Integer.parseInt(args[4]);
            segCount = Integer.parseInt(args[5]);

            finishTime = System.currentTimeMillis() + time;

            Properties props = new Properties();
            try (InputStream fis = Files.newInputStream(Paths.get(PLUGINS_LIST_FILE))) {
                props.load(fis);
                PluginUtils.loadPluginFromProperties(props);
            }
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
            RemoteRepositoryProvider remoteRepositoryProvider = new RemoteRepositoryProvider(
                    config.getProjectRootDir(), config.getRepositories(), config);
            remoteRepositoryProvider.switchToVersion(OConsts.FILE_PROJECT, null);
            remoteRepositoryProvider.copyFilesFromReposToProject(OConsts.FILE_PROJECT, "", false);

            // Prepare project
            ProjectProperties projectProperties = ProjectFileStorage.loadProjectProperties(new File(dir));
            String remoteRepoUrl = getRootGitRepositoryMapping(projectProperties.getRepositories());
            if (!repo.equals(remoteRepoUrl)) {
                setRootGitRepositoryMapping(projectProperties.getRepositories(), repo);
            }
            projectProperties.autocreateDirectories();
            Core.setProject(new NotLoadedProject());
            TestCoreState.getInstance().setGlossaryManager(new GlossaryManager(new GlossaryTextAreaStub()));
            loadProject(projectProperties);

            key = new EntryKey[segCount];
            ste = new SourceTextEntry[segCount];
            for (int c = 0; c < segCount; c++) {
                key[c] = new EntryKey("file", source + "/" + c, null, null, null, null);
                ste[c] = new SourceTextEntry(key[c], 0, null, null, new ArrayList<>());
            }
            keyC = new EntryKey("file", CONCURRENT_NAME, null, null, null, null);
            steC = new SourceTextEntry(keyC, 0, null, null, new ArrayList<>());

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

                    updateGlossary();

                    // change /1..N segment
                    Thread.sleep(ThreadLocalRandom.current().nextInt(maxDelaySeconds * 1000) + 10);
                    checksavecheck(c);

                    checkGlossaryEntries();
                }
            }
            // do closeProject as same way as OmegaT close
            Core.executeExclusively(true, () -> {
                Core.getProject().saveProject(true);
                ProjectFactory.closeProject();
            });

            // load again and check
            loadProject(projectProperties);
            checkAll();

            checkGlossaryEntries();

            // check projectProperties
            checkRepoUrl(projectProperties);

            System.exit(200);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * replacement of ProjectFactory.loadProject for test.
     * 
     * @param projectProperties
     *            target project to load
     */
    static void loadProject(ProjectProperties projectProperties) throws Exception {
        Core.getAutoSave().disable();
        RealProject p = new TestRealProject(projectProperties);
        Core.setProject(p);
        // load project
        p.loadProject(true);
        if (p.isProjectLoaded()) {
            Core.getAutoSave().enable();
            CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
        } else {
            throw new Exception("Project can't be loaded");
        }
    }

    static void checkRepoUrl(ProjectProperties prop) {
        for (RepositoryDefinition repository : prop.getRepositories()) {
            if (repository.getUrl().equals(repo)) {
                return;
            }
        }
        throw new RuntimeException("Wrong url in repository. expected: " + repo + ": actual: "
                + prop.getRepositories().get(0).getUrl());
    }

    private static String getRootGitRepositoryMapping(List<RepositoryDefinition> repos) {
        String repoUrl = null;
        for (RepositoryDefinition definition : repos) {
            if (definition.getMapping().get(0).getLocal().equals("/")
                    && definition.getMapping().get(0).getRepository().equals("/")
                    && definition.getType().equals("git")) {
                repoUrl = definition.getUrl();
                break;
            }
        }
        return repoUrl;
    }

    private static void setRootGitRepositoryMapping(List<RepositoryDefinition> repos, String repoUrl) {
        for (RepositoryDefinition definition : repos) {
            if (definition.getMapping().get(0).getLocal().equals("/")
                    && definition.getMapping().get(0).getRepository().equals("/")
                    && definition.getType().equals("git")) {
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
        ProjectTMX tmx = new ProjectTMX(TestTeamIntegration.checkOrphanedCallback);
        tmx.load(TestTeamIntegration.SRC_LANG, TestTeamIntegration.TRG_LANG, false,
                new File(dir + "/omegat/project_save.tmx"), Core.getSegmenter());
        for (int c = 0; c < segCount; c++) {
            checkTranslation(c);
            checkTranslationFromFile(tmx, c);
        }

        Core.getProject().iterateByDefaultTranslations(new IProject.DefaultTranslationsIterator() {
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

    static void checkGlossaryEntries() {
        List<GlossaryEntry> entries = TestCoreState.getInstance().getGlossaryManager().getLocalEntries();
        for (String s : glossaries) {
            boolean found = false;
            for (GlossaryEntry entry : entries) {
                if (entry.getSrcText().equals(s)) {
                    final long index = getGlossaryIndex(s);
                    final String loc = getGlossaryLoc(index);
                    final String com = getGlossaryCom(index);
                    if (!loc.equals(entry.getLocText())) {
                        throw new RuntimeException("Glossary error : " + entry.getSrcText()
                                + " should have loc: " + loc + " but it is " + entry.getLocText());
                    }
                    if (!com.equals(entry.getCommentText())) {
                        throw new RuntimeException("Glossary error : " + entry.getSrcText()
                                + " should have comment: " + com + " but it is " + entry.getCommentText());
                    }
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new RuntimeException("Glossary error : glossary entry missing! \"" + s + "\"");
            }
        }
        Log.log("Checked my added" + glossaries.size() + " entries in total " + entries.size()
                + " writable glossary entries.");
    }

    static void checkTranslation(int index) {
        TMXEntry en = Core.getProject().getTranslationInfo(ste[index]);
        String sv;
        if (en == null || !en.isTranslated()) {
            sv = "";
        } else {
            sv = en.translation;
        }
        if (v[index] == 0 && sv.isEmpty()) {
            return;
        }
        if ((v[index] + "").equals(sv)) {
            return;
        }
        throw new RuntimeException(source + ": Wrong value in " + source + "/" + index + ": expected "
                + v[index] + " but contains " + sv);
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

    static long getGlossaryIndex(String term) {
        return Long.parseLong(term.substring(term.lastIndexOf('/') + 1));
    }

    static String getGlossaryTerm(long index) {
        return "term/" + source + "/" + index;
    }

    static String getGlossaryLoc(long index) {
        return "loc/" + source + "/" + index;
    }

    static String getGlossaryCom(long index) {
        return "com/" + source + "/" + index;
    }

    static void updateGlossary() throws Exception {
        ProjectProperties props = Core.getProject().getProjectProperties();
        final File out = new File(props.getWriteableGlossary());
        final String src = getGlossaryTerm(glossaryIndex);
        final String loc = getGlossaryLoc(glossaryIndex);
        final String com = getGlossaryCom(glossaryIndex);
        GlossaryReaderTSV.append(out, new GlossaryEntry(src, loc, com, true, out.getPath()));
        Log.log("Add glossary entry " + src);
        glossaryIndex++;
    }

    static IAutoSave autoSave = new IAutoSave() {
        public void enable() {
        }

        public void disable() {
        }
    };

    static IEditor editor = new EditorStub(new EditorSettingsStub());

    static IMainWindow mainWindow = new ConsoleWindow() {
        @Override
        public void unlockUI() {
        }

        @Override
        public void showProgressMessage(String messageText) {
        }

        @Override
        public void showLengthMessage(String messageText) {
        }

        @Override
        public void lockUI() {
        }

        final IMainMenu menu = new MainMenuStub();

        public IMainMenu getMainMenu() {
            return menu;
        }

        public Cursor getCursor() {
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
    };

    /**
     * Override RealProject for own merge.
     */
    static class TestRealProject extends RealProject {
        TestRealProject(final ProjectProperties props) {
            super(props);
        }

        @Override
        TMXRebaseOperation getTMXRebaseOperation() {
            return new TestingTMXRebaseOperation(projectTMX, config);
        }
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
    }

    private static class GlossaryTextAreaStub implements IGlossaries {
        @Override
        public List<GlossaryEntry> getDisplayedEntries() {
            return null;
        }

        @Override
        public void showCreateGlossaryEntryDialog(final Frame parent) {
        }

        @Override
        public void refresh() {
        }
    }
}
