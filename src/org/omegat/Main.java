/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Martin Fleurke, Alex Buloichik, Didier Briel
               2012 Aaron Madlon-Kay
               2013 Kyle Katarn, Aaron Madlon-Kay
               2014 Alex Buloichik
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

package org.omegat;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.omegat.CLIParameters.PSEUDO_TRANSLATE_TYPE;
import org.omegat.CLIParameters.TAG_VALIDATION_MODE;
import org.omegat.convert.ConvertConfigs;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.tagvalidation.ErrorReport;
import org.omegat.core.team2.TeamTool;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.scripting.ConsoleBindings;
import org.omegat.gui.scripting.ScriptItem;
import org.omegat.gui.scripting.ScriptRunner;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXWriter2;
import org.omegat.util.gui.OSXIntegration;

import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * The main OmegaT class, used to launch the program.
 *
 * @author Keith Godfrey
 * @author Martin Fleurke
 * @author Alex Buloichik
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Kyle Katarn
 */
public final class Main {

    private Main() {
    }

    /** Project location for load on startup. */
    protected static File projectLocation = null;

    /** Remote project location. */
    protected static String remoteProject = null;

    /** Execution command line parameters. */
    protected static final Map<String, String> PARAMS = new TreeMap<>();

    /** Execution mode. */
    protected static CLIParameters.RUN_MODE runMode = CLIParameters.RUN_MODE.GUI;

    public static void main(String[] args) {
        if (args.length > 0 && (CLIParameters.HELP_SHORT.equals(args[0])
                || CLIParameters.HELP.equals(args[0]))) {
            System.out.println(StringUtil.format(OStrings.getString("COMMAND_LINE_HELP"),
                    OStrings.getNameAndVersion()));
            System.exit(0);
        }

        if (args.length > 0 && CLIParameters.TEAM_TOOL.equals(args[0])) {
            TeamTool.main(Arrays.copyOfRange(args, 1, args.length));
        }

        // Workaround for bug #812. Remove this when appropriate; see
        // https://sourceforge.net/p/omegat/bugs/812/
        System.setProperty("jna.encoding", Charset.defaultCharset().name());

        PARAMS.putAll(CLIParameters.parseArgs(args));

        String projectDir = PARAMS.get(CLIParameters.PROJECT_DIR);
        if (projectDir != null) {
            projectLocation = new File(projectDir);
        }
        remoteProject = PARAMS.get(CLIParameters.REMOTE_PROJECT);

        applyConfigFile(PARAMS.get(CLIParameters.CONFIG_FILE));

        runMode = CLIParameters.RUN_MODE.parse(PARAMS.get(CLIParameters.MODE));

        String resourceBundle = PARAMS.get(CLIParameters.RESOURCE_BUNDLE);
        if (resourceBundle != null) {
            OStrings.loadBundle(resourceBundle);
        }

        String configDir = PARAMS.get(CLIParameters.CONFIG_DIR);
        if (configDir != null) {
            RuntimePreferences.setConfigDir(configDir);
        }

        if (PARAMS.containsKey(CLIParameters.QUIET)) {
            RuntimePreferences.setQuietMode(true);
        }

        if (PARAMS.containsKey(CLIParameters.DISABLE_PROJECT_LOCKING)) {
            RuntimePreferences.setProjectLockingEnabled(false);
        }

        if (PARAMS.containsKey(CLIParameters.DISABLE_LOCATION_SAVE)) {
            RuntimePreferences.setLocationSaveEnabled(false);
        }

        Log.log("\n" + "===================================================================" + "\n"
                + OStrings.getNameAndVersion() + " (" + new Date() + ") " + " Locale " + Locale.getDefault());

        Log.logRB("LOG_STARTUP_INFO", System.getProperty("java.vendor"), System.getProperty("java.version"),
                System.getProperty("java.home"));

        System.setProperty("http.agent", OStrings.getDisplayNameAndVersion());

        // Do migration and load various settings. The order is important!
        ConvertConfigs.convert();
        Preferences.init();
        PluginUtils.loadPlugins(PARAMS);
        FilterMaster.setFilterClasses(PluginUtils.getFilterClasses());
        Preferences.initFilters();
        Preferences.initSegmentation();

        int result;
        try {
            switch (runMode) {
            case GUI:
                result = runGUI();
                // GUI has own shutdown code
                break;
            case CONSOLE_TRANSLATE:
                result = runConsoleTranslate();
                PluginUtils.unloadPlugins();
                break;
            case CONSOLE_CREATEPSEUDOTRANSLATETMX:
                result = runCreatePseudoTranslateTMX();
                PluginUtils.unloadPlugins();
                break;
            case CONSOLE_ALIGN:
                result = runConsoleAlign();
                PluginUtils.unloadPlugins();
                break;
            default:
                result = 1;
            }
        } catch (Throwable ex) {
            Log.log(ex);
            showError(ex);
            result = 1;
        }
        if (result != 0) {
            System.exit(result);
        }
    }

    public static void restartGUI(String projectDir) {
        Log.log("===         Restart OmegaT           ===");
        String javaBin = String.join(File.separator, System.getProperty("java.home"), "bin", "java");
        // Build command: java -cp ... org.omegat.Main
        List<String> command = new ArrayList<>();
        command.add(javaBin);
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        command.addAll(runtimeMxBean.getInputArguments()); // JVM args
        command.add("-cp");
        command.add(runtimeMxBean.getClassPath());
        command.add(Main.class.getName());
        command.addAll(CLIParameters.unparseArgs(PARAMS));
        if (projectDir != null) {
            command.add(projectDir);
        }
        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Load System properties from a specified .properties file. In order to
     * allow this to reliably change the display language, it must called before
     * any use of {@link Log#log}, thus it logs to {@link System#out}.
     *
     * @param path
     *            to config file
     */
    private static void applyConfigFile(String path) {
        if (path == null) {
            return;
        }
        File configFile = new File(path);
        if (!configFile.exists()) {
            return;
        }
        System.out.println("Reading config from " + path);
        try (FileInputStream in = new FileInputStream(configFile)) {
            PropertyResourceBundle config = new PropertyResourceBundle(in);
            // Put config properties into System properties and into OmegaT params.
            for (String key : config.keySet()) {
                String value = config.getString(key);
                System.setProperty(key, value);
                PARAMS.put(key, value);
                System.out.println("Read from config: " + key + "=" + value);
            }
            // Apply language preferences, if present.
            // This must be done with Locale.setDefault(). Merely doing
            // System.setProperty() will not work.
            if (config.containsKey("user.language")) {
                String userLanguage = config.getString("user.language");
                Locale userLocale = config.containsKey("user.country")
                        ? new Locale(userLanguage, config.getString("user.country"))
                        : new Locale(userLanguage);
                Locale.setDefault(userLocale);
            }
        } catch (FileNotFoundException exception) {
            System.err.println("Config file not found: " + path);
        } catch (IOException exception) {
            System.err.println("Error while reading config file: " + path);
        }
    }

    /**
     * Execute standard GUI.
     */
    protected static int runGUI() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();
        MainClassLoader mainClassLoader = (cl instanceof MainClassLoader) ? (MainClassLoader) cl : new MainClassLoader(cl);
        PluginUtils.getThemePluginJars().forEach(mainClassLoader::add);
        UIManager.put("ClassLoader", mainClassLoader);

        // macOS-specific - they must be set BEFORE any GUI calls
        if (Platform.isMacOSX()) {
            OSXIntegration.init();
        }

        Log.log("Docking Framework version: " + DockingDesktop.getDockingFrameworkVersion());
        Log.log("");

        // Set X11 application class name to make some desktop user interfaces
        // (like Gnome Shell) recognize OmegaT
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Class<?> cls = toolkit.getClass();
        try {
            if (cls.getName().equals("sun.awt.X11.XToolkit")) {
                Field field = cls.getDeclaredField("awtAppClassName");
                field.setAccessible(true);
                field.set(toolkit, "OmegaT");
            }
        } catch (Exception e) {
            // do nothing
        }

        System.setProperty("swing.aatext", "true");
        try {
            Core.initializeGUI(mainClassLoader, PARAMS);
        } catch (Throwable ex) {
            Log.log(ex);
            showError(ex);
            return 1;
        }

        if (!Core.getPluginsLoadingErrors().isEmpty()) {
            String err = String.join("\n", Core.getPluginsLoadingErrors());
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), err,
                    OStrings.getString("STARTUP_ERRORBOX_TITLE"), JOptionPane.ERROR_MESSAGE);
        }

        CoreEvents.fireApplicationStartup();

        SwingUtilities.invokeLater(() -> {
            // setVisible can't be executed directly, because we need to
            // call all application startup listeners for initialize UI
            Core.getMainWindow().getApplicationFrame().setVisible(true);

            if (remoteProject != null) {
                ProjectUICommands.projectRemote(remoteProject);
            } else if (projectLocation != null) {
                ProjectUICommands.projectOpen(projectLocation);
            }
        });
        return 0;
    }

    /**
     * Execute in console mode for translate.
     */
    protected static int runConsoleTranslate() throws Exception {
        Log.log("Console translation mode");
        Log.log("");

        System.out.println(OStrings.getString("CONSOLE_INITIALIZING"));
        Core.initializeConsole(PARAMS);

        RealProject p = selectProjectConsoleMode(true);

        validateTagsConsoleMode();

        System.out.println(OStrings.getString("CONSOLE_TRANSLATING"));

        String sourceMask = PARAMS.get(CLIParameters.SOURCE_PATTERN);
        if (sourceMask != null) {
            p.compileProject(sourceMask, false);
        } else {
            p.compileProject(".*", false);
        }

        // Called *after* executing post processing command (unlike the
        // regular PROJECT_CHANGE_TYPE.COMPILE)
        executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.COMPILE);

        p.closeProject();
        executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE);
        System.out.println(OStrings.getString("CONSOLE_FINISHED"));

        return 0;
    }

    /**
     * Validates tags according to command line specs:
     * <code>--tag-validation=[abort|warn]</code>
     * <p>
     * On abort, the program is aborted when tag validation finds errors. On
     * warn the errors are printed but the program continues. In all other cases
     * no tag validation is done.
     */
    private static void validateTagsConsoleMode() {
        TAG_VALIDATION_MODE mode = TAG_VALIDATION_MODE.parse(PARAMS.get(CLIParameters.TAG_VALIDATION));

        List<ErrorReport> stes;

        switch (mode) {
        case ABORT:
            System.out.println(OStrings.getString("CONSOLE_VALIDATING_TAGS"));
            stes = Core.getTagValidation().listInvalidTags();
            if (!stes.isEmpty()) {
                Core.getTagValidation().logTagValidationErrors(stes);
                System.out.println(OStrings.getString("CONSOLE_TAGVALIDATION_FAIL"));
                System.out.println(OStrings.getString("CONSOLE_TAGVALIDATION_ABORT"));
                System.exit(1);
            }
            break;
        case WARN:
            System.out.println(OStrings.getString("CONSOLE_VALIDATING_TAGS"));
            stes = Core.getTagValidation().listInvalidTags();
            if (!stes.isEmpty()) {
                Core.getTagValidation().logTagValidationErrors(stes);
                System.out.println(OStrings.getString("CONSOLE_TAGVALIDATION_FAIL"));
            }
            break;
        default:
            //do not validate tags = default
        }
    }

    /**
     * Execute in console mode for translate.
     */
    protected static int runCreatePseudoTranslateTMX() throws Exception {
        Log.log("Console pseudo-translate mode");
        Log.log("");

        System.out.println(OStrings.getString("CONSOLE_INITIALIZING"));
        Core.initializeConsole(PARAMS);

        RealProject p = selectProjectConsoleMode(true);

        validateTagsConsoleMode();

        System.out.println(OStrings.getString("CONSOLE_CREATE_PSEUDOTMX"));

        ProjectProperties config = p.getProjectProperties();
        List<SourceTextEntry> entries = p.getAllEntries();
        String pseudoTranslateTMXFilename = PARAMS.get(CLIParameters.PSEUDOTRANSLATETMX);
        PSEUDO_TRANSLATE_TYPE pseudoTranslateType = PSEUDO_TRANSLATE_TYPE
                .parse(PARAMS.get(CLIParameters.PSEUDOTRANSLATETYPE));

        String fname;
        if (!StringUtil.isEmpty(pseudoTranslateTMXFilename)) {
            if (!pseudoTranslateTMXFilename.endsWith(OConsts.TMX_EXTENSION)) {
                fname = pseudoTranslateTMXFilename + "." + OConsts.TMX_EXTENSION;
            } else {
                fname = pseudoTranslateTMXFilename;
            }
        } else {
            fname = "";
        }

        // Write OmegaT-project-compatible TMX:
        try (TMXWriter2 wr = new TMXWriter2(new File(fname), config.getSourceLanguage(), config.getTargetLanguage(),
                config.isSentenceSegmentingEnabled(), false, false)) {
            for (SourceTextEntry ste : entries) {
                switch (pseudoTranslateType) {
                    case EQUAL:
                        wr.writeEntry(ste.getSrcText(), ste.getSrcText(), null, null, 0, null, 0, null);
                        break;
                    case EMPTY:
                        wr.writeEntry(ste.getSrcText(), "", null, null, 0, null, 0, null);
                        break;
                }
            }
        } catch (IOException e) {
            Log.logErrorRB("CT_ERROR_CREATING_TMX");
            Log.log(e);
            throw new IOException(OStrings.getString("CT_ERROR_CREATING_TMX") + "\n" + e.getMessage());
        }
        p.closeProject();
        System.out.println(OStrings.getString("CONSOLE_FINISHED"));
        return 0;
    }

    public static int runConsoleAlign() throws Exception {
        Log.log("Console alignment mode");
        Log.log("");

        if (projectLocation == null) {
            System.out.println(OStrings.getString("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE"));
            return 1;
        }

        String dir = PARAMS.get(CLIParameters.ALIGNDIR);
        if (dir == null) {
            System.out.println(OStrings.getString("CONSOLE_TRANSLATED_FILES_LOC_UNDEFINED"));
            return 1;
        }

        System.out.println(OStrings.getString("CONSOLE_INITIALIZING"));
        Core.initializeConsole(PARAMS);
        RealProject p = selectProjectConsoleMode(true);

        validateTagsConsoleMode();

        System.out.println(StringUtil.format(OStrings.getString("CONSOLE_ALIGN_AGAINST"), dir));

        String tmxFile = p.getProjectProperties().getProjectInternal() + "align.tmx";
        ProjectProperties config = p.getProjectProperties();

        try (TMXWriter2 wr = new TMXWriter2(new File(tmxFile), config.getSourceLanguage(), config.getTargetLanguage(),
                config.isSentenceSegmentingEnabled(), false, false)) {
            wr.writeEntries(p.align(p.getProjectProperties(), new File(dir)));
        }
        p.closeProject();
        System.out.println(OStrings.getString("CONSOLE_FINISHED"));
        return 0;
    }

    /**
     * creates the project class and adds it to the Core. Loads the project if
     * specified. An exit occurs on error loading the project. This method is
     * for the different console modes, to prevent code duplication.
     *
     * @param loadProject
     *            load the project or not
     * @return the project.
     */
    private static RealProject selectProjectConsoleMode(boolean loadProject) {
        System.out.println(OStrings.getString("CONSOLE_LOADING_PROJECT"));

        // check if project okay
        ProjectProperties projectProperties = null;
        try {
            projectProperties = ProjectFileStorage.loadProjectProperties(projectLocation);
            projectProperties.verifyProject();
        } catch (Exception ex) {
            Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
            System.out.println(OStrings.getString("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE"));
            System.exit(1);
        }

        RealProject p = new RealProject(projectProperties);
        Core.setProject(p);
        if (loadProject) {
            p.loadProject(true);
            if (!p.isProjectLoaded()) {
                Core.setProject(new NotLoadedProject());
            } else {
                executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
            }

        }
        return p;
    }

    /**
     * Execute script as PROJECT_CHANGE events. We can't use the regular project
     * listener because the SwingUtilities.invokeLater method used in CoreEvents
     * doesn't stop the project processing in console mode.
     */
    private static void executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
        if (PARAMS.containsKey(CLIParameters.SCRIPT)) {
            File script = new File(PARAMS.get("script").toString());
            Log.log(OStrings.getString("CONSOLE_EXECUTE_SCRIPT", script, eventType));
            if (script.isFile()) {
                HashMap<String, Object> binding = new HashMap<>();
                binding.put("eventType", eventType);

                ConsoleBindings consoleBindigs = new ConsoleBindings();
                binding.put(ScriptRunner.VAR_CONSOLE, consoleBindigs);
                binding.put(ScriptRunner.VAR_GLOSSARY, consoleBindigs);
                binding.put(ScriptRunner.VAR_EDITOR, consoleBindigs);

                try {
                    String result = ScriptRunner.executeScript(new ScriptItem(script), binding);
                    Log.log(result);
                } catch (Exception ex) {
                    Log.log(ex);
                }
            } else {
                Log.log(OStrings.getString("SCW_SCRIPT_LOAD_ERROR", "the script is not a file"));
            }
        }
    }

    public static void showError(Throwable ex) {
        String msg;
        if (StringUtil.isEmpty(ex.getMessage())) {
            msg = ex.getClass().getName();
        } else {
            msg = ex.getMessage();
        }
        switch (runMode) {
        case GUI:
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg,
                    OStrings.getString("STARTUP_ERRORBOX_TITLE"), JOptionPane.ERROR_MESSAGE);
            break;
        default:
            System.err.println(MessageFormat.format(OStrings.getString("CONSOLE_ERROR"), msg));
            break;
        }
    }
}
