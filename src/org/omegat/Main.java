/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Martin Fleurke, Alex Buloichik, Didier Briel
               2012 Aaron Madlon-Kay
               2013 Kyle Katarn, Aaron Madlon-Kay
               2014 Alex Buloichik
               2018 Enrique Estevez Fernandez
               2022 Hiroshi Miura
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

package org.omegat;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PropertyResourceBundle;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.omegat.cli.BaseSubCommand;
import org.omegat.cli.SubCommands;
import org.omegat.core.data.RuntimePreferenceStore;
import org.omegat.core.statistics.Statistics;
import tokyo.northside.logging.ILogger;

import org.omegat.CLIParameters.PSEUDO_TRANSLATE_TYPE;
import org.omegat.CLIParameters.TAG_VALIDATION_MODE;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.statistics.StatOutputFormat;
import org.omegat.core.statistics.StatsResult;
import org.omegat.core.tagvalidation.ErrorReport;
import org.omegat.core.team2.TeamTool;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.gui.scripting.ConsoleBindings;
import org.omegat.gui.scripting.ScriptItem;
import org.omegat.gui.scripting.ScriptRunner;
import org.omegat.languagetools.LanguageClassBroker;
import org.omegat.languagetools.LanguageDataBroker;
import org.omegat.util.FileUtil;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.StaticUtils;
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
 * @author Hiroshi Miura
 */
public final class Main {

    private Main() {
    }

    /** Project location for a load on startup. */
    private static @Nullable File projectLocation = null;

    /** Remote project location. */
    private static @Nullable String remoteProject = null;

    /** Execution command line parameters. */
    private static final Map<String, String> PARAMS = new TreeMap<>();

    /** Execution mode. */
    private static CLIParameters.RUN_MODE runMode = CLIParameters.RUN_MODE.GUI;

    public static void main(String[] args) {
        // Workaround for Java 17 or later support of JAXB.
        // See https://sourceforge.net/p/omegat/feature-requests/1682/#12c5
        System.setProperty("com.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize", "true");

        CLIParameters cliParams = CLIParameters.parseArgs(args);
        PARAMS.putAll(cliParams.getParams());

        if (PARAMS.containsKey("help") || (args.length > 0 && CLIParameters.HELP_SHORT.equals(args[0]))) {
            System.out.println(
                    StringUtil.format(OStrings.getString("COMMAND_LINE_HELP"), OStrings.getNameAndVersion()));
            System.exit(0);
        }

        if (CLIParameters.TEAM_TOOL.equals(cliParams.getSubcommand())) {
            TeamTool.main(cliParams.getArgs().toArray(new String[0]));
        }

        String projectDir = PARAMS.get(CLIParameters.PROJECT_DIR);
        if (projectDir != null) {
            projectLocation = new File(FileUtil.expandTildeHomeDir(projectDir));
        }
        remoteProject = PARAMS.get(CLIParameters.REMOTE_PROJECT);

        applyConfigFile(PARAMS.get(CLIParameters.CONFIG_FILE));

        String modeArg = PARAMS.get(CLIParameters.MODE);
        if (modeArg == null) {
            modeArg = cliParams.getSubcommand();
        }
        runMode = CLIParameters.RUN_MODE.parse(modeArg);

        String resourceBundle = PARAMS.get(CLIParameters.RESOURCE_BUNDLE);
        if (resourceBundle != null) {
            OStrings.loadBundle(FileUtil.expandTildeHomeDir(resourceBundle));
        }

        String configDir = PARAMS.get(CLIParameters.CONFIG_DIR);
        if (configDir != null) {
            RuntimePreferenceStore.getInstance().setConfigDir(FileUtil.expandTildeHomeDir(configDir));
        }

        if (PARAMS.containsKey(CLIParameters.QUIET)) {
            RuntimePreferenceStore.getInstance().setQuietMode(true);
        }

        if (PARAMS.containsKey(CLIParameters.DISABLE_PROJECT_LOCKING)) {
            RuntimePreferenceStore.getInstance().setProjectLockingDisabled();
        }

        if (PARAMS.containsKey(CLIParameters.DISABLE_LOCATION_SAVE)) {
            RuntimePreferenceStore.getInstance().setLocationSaveDisable();
        }

        if (PARAMS.containsKey(CLIParameters.NO_TEAM)) {
            RuntimePreferenceStore.getInstance().setNoTeam();
        }
        String alternateFrom = PARAMS.get(CLIParameters.ALTERNATE_FILENAME_FROM);
        if (alternateFrom != null) {
            RuntimePreferenceStore.getInstance().setAlternateFilenameFrom(alternateFrom);
        }
        String alternateTo = PARAMS.get(CLIParameters.ALTERNATE_FILENAME_TO);
        if (alternateTo != null) {
            RuntimePreferenceStore.getInstance().setAlternateFilenameTo(alternateTo);
        }
        String tokenizerSource = PARAMS.get(CLIParameters.TOKENIZER_SOURCE);
        if (tokenizerSource != null) {
            RuntimePreferenceStore.getInstance().setTokenizerSource(tokenizerSource);
        }
        String tokenizerTarget = PARAMS.get(CLIParameters.TOKENIZER_TARGET);
        if (tokenizerTarget != null) {
            RuntimePreferenceStore.getInstance().setTokenizerTarget(tokenizerTarget);
        }

        // initialize logging backend and loading configuration.
        ILogger logger = Log.getLogger(Main.class);

        logger.atInfo().setMessage("\n{0}\n{1} (started on {2} {3}) Locale {4}")
                .addArgument(StringUtils.repeat('=', 120)).addArgument(OStrings.getNameAndVersion())
                .addArgument(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
                        .withLocale(Locale.getDefault()).format(ZonedDateTime.now(ZoneId.systemDefault())))
                .addArgument(TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT, Locale.getDefault()))
                .addArgument(Locale.getDefault().toLanguageTag()).log();
        logger.atInfo().logRB("LOG_STARTUP_INFO", System.getProperty("java.vendor"),
                System.getProperty("java.version"), System.getProperty("java.home"));

        System.setProperty("http.agent", OStrings.getDisplayNameAndVersion());

        // Do migration and load various settings. The order is important!
        Preferences.init();
        StaticUtils.ensureUserScriptsDir();
        // broker should be loaded before module loading
        JLanguageTool.setClassBrokerBroker(new LanguageClassBroker());
        JLanguageTool.setDataBroker(new LanguageDataBroker());
        PluginUtils.loadPlugins(PARAMS);
        FilterMaster.setFilterClasses(PluginUtils.getFilterClasses());
        Preferences.initFilters();
        Preferences.initSegmentation();

        int result;
        try {
            if (runMode != null) {
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
                case CONSOLE_STATS:
                    result = runConsoleStats();
                    PluginUtils.unloadPlugins();
                    break;
                default:
                    result = 1;
                }
            } else {
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
        // Check we have `java` command in java.home
        Path javaBin = Paths.get(System.getProperty("java.home")).resolve("bin/java");
        String installDir = StaticUtils.installDir();
        Path parent = null;
        if (installDir != null) {
            parent = Paths.get(installDir).getParent();
        }
        if (!javaBin.toFile().exists()) {
            // on Windows
            javaBin = Paths.get(System.getProperty("java.home")).resolve("bin/java.exe");
        }
        List<String> command = new ArrayList<>();
        if (javaBin.toFile().exists()) {
            // Build command: java -cp ... org.omegat.Main
            command.add(javaBin.toString());
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            command.addAll(runtimeMxBean.getInputArguments()); // JVM args
            command.add("-cp");
            command.add(runtimeMxBean.getClassPath());
            command.add(Main.class.getName());
            command.addAll(CLIParameters.unparseArgs(PARAMS));
        } else if (parent != null) {
            // assumes jpackage or Windows installer
            javaBin = parent.resolve("bin/OmegaT");
            if (!javaBin.toFile().exists()) {
                javaBin = parent.resolve("OmegaT.exe");
            }
            if (!javaBin.toFile().exists()) {
                // abort restart
                Core.getMainWindow().displayWarningRB("LOG_RESTART_FAILED_NOT_FOUND");
                return;
            }
            command.add(javaBin.toString());
            command.addAll(CLIParameters.unparseArgs(PARAMS));
        }
        if (projectDir != null) {
            command.add(projectDir);
        }
        // Now ready to restart.
        Log.log("===         Restart OmegaT           ===");
        ProcessBuilder builder = new ProcessBuilder(command);
        try {
            builder.start();
            System.exit(0);
        } catch (IOException e) {
            Log.log(e);
            System.exit(1);
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
    private static void applyConfigFile(@Nullable String path) {
        if (path == null) {
            return;
        }
        File configFile = new File(FileUtil.expandTildeHomeDir(path));
        if (!configFile.exists()) {
            return;
        }
        System.out.println("Reading config from " + path);
        try (FileInputStream in = new FileInputStream(configFile)) {
            PropertyResourceBundle config = new PropertyResourceBundle(in);
            // Put config properties into System properties and into OmegaT
            // params.
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
    private static int runGUI() {
        UIManager.put("ClassLoader", PluginUtils.getClassLoader(PluginUtils.PluginType.THEME));

        // macOS-specific - they must be set BEFORE any GUI calls
        if (Platform.isMacOSX()) {
            OSXIntegration.init();
        }

        Log.logInfoRB("STARTUP_GUI_DOCKING_FRAMEWORK", DockingDesktop.getDockingFrameworkVersion());
        if (Platform.isUnixLike()) {
            tweakX11AppName();
        }
        System.setProperty("swing.aatext", "true");
        try {
            Core.initializeGUI(PARAMS);
        } catch (Throwable ex) {
            Log.log(ex);
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

    private static void tweakX11AppName() {
        try {
            // Set X11 application class name to make some desktop user interfaces
            // (like Gnome Shell) recognize OmegaT
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            Class<?> cls = toolkit.getClass();
            if (cls.getName().equals("sun.awt.X11.XToolkit")) {
                Field field = cls.getDeclaredField("awtAppClassName");
                if (field.trySetAccessible()) {
                    field.set(toolkit, "OmegaT");
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
    }

    /**
     * Execute in console mode for translate.
     */
    private static int runConsoleTranslate() throws Exception {
        Log.logInfoRB("STARTUP_CONSOLE_TRANSLATION_MODE");

        System.out.println(OStrings.getString("CONSOLE_INITIALIZING"));
        Core.initializeConsole(PARAMS);

        RealProject p = selectProjectConsoleMode(true);

        validateTagsConsoleMode();

        System.out.println(OStrings.getString("CONSOLE_TRANSLATING"));

        String sourceMask = PARAMS.get(CLIParameters.SOURCE_PATTERN);
        p.compileProject(Objects.requireNonNullElse(sourceMask, ".*"), false);

        // Called *after* executing post processing command (unlike the
        // regular PROJECT_CHANGE_TYPE.COMPILE)
        executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.COMPILE);

        p.closeProject();
        executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE);
        System.out.println(OStrings.getString("CONSOLE_FINISHED"));

        return 0;
    }

    /**
     * Displays or writes project statistics.
     * <p>
     * takes two optional arguments
     * <code>[--output-file=(file path) [--stats-type=[XML|JSON|TEXT]]]</code>
     * when omitted, display stats text(localized). When file I/O error
     * occurred, especially when parent directory does not exist warns it and
     * return 1.
     */
    private static int runConsoleStats() throws Exception {
        Log.logInfoRB("STARTUP_CONSOLE_STATS_MODE");

        Core.initializeConsole(PARAMS);

        RealProject p = selectProjectConsoleMode(true);
        StatsResult projectStats = Statistics.buildProjectStats(p);

        if (!PARAMS.containsKey(CLIParameters.STATS_OUTPUT)) {
            // no output file specified, print to console.
            System.out.println(projectStats.getTextData());
            p.closeProject();
            return 0;
        }

        String outputFilename = PARAMS.get(CLIParameters.STATS_OUTPUT);
        StatOutputFormat statsMode;
        if (PARAMS.containsKey(CLIParameters.STATS_MODE)) {
            statsMode = StatOutputFormat.parse(PARAMS.get(CLIParameters.STATS_MODE));
        } else {
            // when no stats type specified, try to detect from file extension,
            // otherwise XML.
            if (outputFilename.toLowerCase().endsWith(StatOutputFormat.JSON.getFileExtension())) {
                statsMode = StatOutputFormat.JSON;
            } else if (outputFilename.toLowerCase().endsWith(StatOutputFormat.XML.getFileExtension())) {
                statsMode = StatOutputFormat.XML;
            } else if (outputFilename.toLowerCase().endsWith(StatOutputFormat.TEXT.getFileExtension())) {
                statsMode = StatOutputFormat.TEXT;
            } else {
                statsMode = StatOutputFormat.XML;
            }
        }
        try (OutputStreamWriter writer = new OutputStreamWriter(
                Files.newOutputStream(Paths.get(FileUtil.expandTildeHomeDir(outputFilename)), CREATE,
                        TRUNCATE_EXISTING, WRITE),
                StandardCharsets.UTF_8)) {
            switch (statsMode) {
            case TEXT:
                writer.write(projectStats.getTextData());
                break;
            case JSON:
                writer.write(projectStats.getJsonData());
                break;
            case XML:
                writer.write(projectStats.getXmlData());
                break;
            default:
                Log.logWarningRB("CONSOLE_STATS_WARNING_TYPE");
                break;
            }
        } catch (NoSuchFileException nsfe) {
            Log.logErrorRB("CONSOLE_STATS_FILE_OPEN_ERROR");
            return 1;
        } finally {
            p.closeProject();
        }
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
            // do not validate tags = default
        }
    }

    /**
     * Execute in console mode for translate.
     */
    private static int runCreatePseudoTranslateTMX() throws Exception {
        Log.logInfoRB("CONSOLE_PSEUDO_TRANSLATION_MODE");

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
        try (TMXWriter2 wr = new TMXWriter2(new File(fname), config.getSourceLanguage(),
                config.getTargetLanguage(), config.isSentenceSegmentingEnabled(), false, false)) {
            for (SourceTextEntry ste : entries) {
                switch (pseudoTranslateType) {
                case EQUAL:
                    wr.writeEntry(ste.getSrcText(), ste.getSrcText(), null, null, 0, null, 0, null);
                    break;
                case EMPTY:
                    wr.writeEntry(ste.getSrcText(), "", null, null, 0, null, 0, null);
                    break;
                default:
                    // should not come here
                    throw new IllegalArgumentException();
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
        Log.logInfoRB("CONSOLE_ALIGNMENT_MODE");
        if (!SubCommands.containsCommand("Align")) {
            return 1;
        }

        if (projectLocation == null) {
            System.out.println(OStrings.getString("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE"));
            return 1;
        }

        System.out.println(OStrings.getString("CONSOLE_INITIALIZING"));
        Core.initializeConsole(PARAMS);
        BaseSubCommand command = SubCommands.getCommand("Align").getDeclaredConstructor().newInstance();
        command.setParameters(PARAMS);
        selectProjectConsoleMode(command.isProjectRequired());
        validateTagsConsoleMode();
        int status = command.call();
        Log.logInfoRB("CONSOLE_FINISHED");
        return status;
    }

    /**
     * creates the project class and adds it to the Core. Loads the project if
     * specified. An exit occurs on error loading the project. This method is
     * for the different console modes to prevent code duplication.
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
     * Execute a script as PROJECT_CHANGE events. We can't use the regular
     * project listener because the SwingUtilities.invokeLater method used in
     * CoreEvents doesn't stop the project processing in console mode.
     */
    private static void executeConsoleScript(IProjectEventListener.PROJECT_CHANGE_TYPE eventType) {
        if (PARAMS.containsKey(CLIParameters.SCRIPT)) {
            File script = new File(PARAMS.get("script"));
            Log.logInfoRB("CONSOLE_EXECUTE_SCRIPT", script, eventType);
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
                Log.logInfoRB("SCW_SCRIPT_LOAD_ERROR", "the script is not a file");
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
        if (CLIParameters.RUN_MODE.GUI == runMode) {
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg,
                    OStrings.getString("STARTUP_ERRORBOX_TITLE"), JOptionPane.ERROR_MESSAGE);
        } else {
            System.err.println(MessageFormat.format(OStrings.getString("CONSOLE_ERROR"), msg));
        }
    }
}
