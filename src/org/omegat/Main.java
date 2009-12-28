/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Martin Fleurke, Alex Buloichik
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.omegat.convert.ConvertConfigs;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.ProjectFactory;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TransEntry;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.RuntimePreferences;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXWriter;

import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * The main OmegaT class, used to launch the program.
 * 
 * @author Keith Godfrey
 * @author Martin Fleurke
 * @author Alex Buloichik
 */
public class Main {
    /** Application execution mode. */
    enum RUN_MODE {
        GUI, CONSOLE_TRANSLATE, CONSOLE_CREATEPSEUDOTRANSLATETMX, CONSOLE_ALIGN;
        public static RUN_MODE parse(String s) {
            try {
                return valueOf(s.toUpperCase().replace('-', '_'));
            } catch (Exception ex) {
                // default mode
                return GUI;
            }
        }
    };
    
    /**
     * Choice of types of translation for all segments in the optional, special 
     * TMX file that contains all segments of the project.
     */
    enum PSEUDO_TRANSLATE_TYPE {
        EQUAL, EMPTY;
        public static PSEUDO_TRANSLATE_TYPE parse(String s) {
            try {
                return valueOf(s.toUpperCase().replace('-', '_'));
            } catch (Exception ex) {
                // default mode
                return EQUAL;
            }
        }
    };


    /** Regexp for parse parameters. */
    protected static final Pattern PARAM = Pattern
            .compile("\\-\\-([A-Za-z\\-]+)(=(.+))?");

    /** Project location for load on startup. */
    protected static File projectLocation = null;

    /** Execution command line parameters. */
    protected static final Map<String, String> params = new TreeMap<String, String>();

    /** Execution mode. */
    protected static RUN_MODE runMode = RUN_MODE.GUI;

    public static void main(String[] args) {

        /*
         * Parse command line arguments info map.
         */
        for (String arg : args) {
            Matcher m = PARAM.matcher(arg);
            if (m.matches()) {
                params.put(m.group(1), m.group(3));
            } else {
                if (arg.startsWith("resource-bundle=")) {
                    // backward compatibility
                    params.put("resource-bundle", arg.substring(16));
                } else {
                    File f = new File(arg);
                    if (f.exists() && f.isDirectory()) {
                        projectLocation = f;
                    }
                }
            }
        }

        runMode = RUN_MODE.parse(params.get("mode"));

        String resourceBundle = params.get("resource-bundle");
        if (resourceBundle != null) {
            OStrings.loadBundle(resourceBundle);
        }

        String configDir = params.get("config-dir");
        if (configDir != null) {
            RuntimePreferences.setConfigDir(configDir);
        }

        if (params.containsKey("quiet")) {
            RuntimePreferences.setQuietMode(true);
        }

        Log.log("\n"
                + 
                "==================================================================="
                + 
                "\n" + 
                OStrings.getDisplayVersion() + 
                " (" + new Date() + ") " + 
                " Locale " + Locale.getDefault()); 

        Log.logRB("LOG_STARTUP_INFO", System.getProperty("java.vendor"), System
                .getProperty("java.version"), System.getProperty("java.home"));

        ConvertConfigs.convert();
        PluginUtils.loadPlugins2();
        
        switch (runMode) {
        case GUI:
            runGUI();
            break;
        case CONSOLE_TRANSLATE:
            runConsoleTranslate();
            break;
        case CONSOLE_CREATEPSEUDOTRANSLATETMX:
            runCreatePseudoTranslateTMX();
            break;
        case CONSOLE_ALIGN:
            runConsoleAlign();
            break;
        }
    }

    /**
     * Execute standard GUI.
     */
    protected static void runGUI() {
        Log.log("Docking Framework version: "
                + DockingDesktop.getDockingFrameworkVersion());
        Log.log("");
        try {
            // Workaround for JDK bug 6389282 (OmegaT bug bug 1555809)
            // it should be called before setLookAndFeel() for GTK LookandFeel
            // Contributed by Masaki Katakai (SF: katakai)
            UIManager.getInstalledLookAndFeels();

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            // MacOSX-specific
            System.setProperty("apple.laf.useScreenMenuBar", "true"); 
            System
                    .setProperty(
                            "com.apple.mrj.application.apple.menu.about.name",
                            "OmegaT"); 
        } catch (Exception e) {
            // do nothing
            Log.logErrorRB("MAIN_ERROR_CANT_INIT_OSLF");
        }

        try {
            Core.initializeGUI(params);
        } catch (Throwable ex) {
            showError(ex);
        }

        CoreEvents.fireApplicationStartup();

        if (projectLocation != null) {
            try {
                ProjectProperties props = ProjectFileStorage
                        .loadProjectProperties(projectLocation);
                ProjectFactory.loadProject(props);
            } catch (Exception ex) {
                showError(ex);
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // setVisible can't be executed directly, because we need to
                // call all application startup listeners for initialize UI
                Core.getMainWindow().getApplicationFrame().setVisible(true);
            }
        });
    }

    /**
     * Execute in console mode for translate.
     */
    protected static void runConsoleTranslate() {
        Log.log("Console mode");
        Log.log("");

        System.out.println("Initializing");
        try {
            Core.initializeConsole(params);
        } catch (Throwable ex) {
            showError(ex);
        }
        try {
            System.out.println("Loading Project");

            // check if project okay
            ProjectProperties projectProperties = null;
            try {
                projectProperties = ProjectFileStorage
                        .loadProjectProperties(projectLocation);
                if (!projectProperties.verifyProject()) {
                    System.out.println("The project cannot be verified");
                    System.exit(1);
                }
            } catch (Exception ex) {
                Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                System.out.println(OStrings.getString
                        ("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE"));
                System.exit(1);
            }

            RealProject p = new RealProject(projectProperties);
            p.loadProject();
            Core.setProject(p);

            System.out.println("Translating Project");
            p.compileProject();

            System.out.println("Finished");
        } catch (Exception e) {
            System.err.println("An error has occured: " + e.toString());
            System.exit(1);
        }
    }

    /**
     * Execute in console mode for translate.
     */
    protected static void runCreatePseudoTranslateTMX() {
        Log.log("Console mode");
        Log.log("");

        System.out.println("Initializing");
        try {
            Core.initializeConsole(params);
        } catch (Throwable ex) {
            showError(ex);
        }
        try {
            System.out.println("Loading Project");

            // check if project okay
            ProjectProperties projectProperties = null;
            try {
                projectProperties = ProjectFileStorage
                        .loadProjectProperties(projectLocation);
                if (!projectProperties.verifyProject()) {
                    System.out.println("The project cannot be verified");
                    System.exit(1);
                }
            } catch (Exception ex) {
                Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                System.out.println(OStrings.getString
                        ("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE"));
                System.exit(1);
            }

            RealProject p = new RealProject(projectProperties);
            p.loadProject();
            Core.setProject(p);

            System.out.println("Create pseudo-translate TMX");

            ProjectProperties m_config = p.getProjectProperties();
            List<SourceTextEntry> entries = p.getAllEntries();
            String pseudoTranslateTMXFilename = params.get("pseudotranslatetmx");
            PSEUDO_TRANSLATE_TYPE pseudoTranslateType = PSEUDO_TRANSLATE_TYPE.parse(params.get("pseudotranslatetype"));

            String fname;
            if (pseudoTranslateTMXFilename != null && pseudoTranslateTMXFilename.length()>0) {
                if (!pseudoTranslateTMXFilename.endsWith(OConsts.TMX_EXTENSION)) {
                    fname = pseudoTranslateTMXFilename+"."+OConsts.TMX_EXTENSION;
                } else {
                    fname = pseudoTranslateTMXFilename;
                }
                
            } else {
                fname="";
            }
            
            // prepare tmx
            Map<String, TransEntry> data = new HashMap<String, TransEntry>();
            for(SourceTextEntry ste: entries) {
                switch (pseudoTranslateType) {
                case EQUAL:
                    data.put(ste.getSrcText(), new TransEntry(ste.getSrcText()));
                    break;
                case EMPTY:
                    data.put(ste.getSrcText(), new TransEntry(""));
                    break;
                }
            }
            
            try {
                TMXWriter.buildTMXFile(fname, false, true, m_config, data);
            } catch (IOException e) {
                Log.logErrorRB("CT_ERROR_CREATING_TMX");
                Log.log(e);
                throw new IOException(OStrings.getString("CT_ERROR_CREATING_TMX") +
                        "\n" +                                                      
                        e.getMessage());
            }

            System.out.println("Finished");
        } catch (Exception e) {
            System.err.println("An error has occured: " + e.toString());
            System.exit(1);
        }
    }
    
    public static void runConsoleAlign() {
        Log.log("Alignment mode");
        Log.log("");

        if (projectLocation == null) {
            System.out.println("Project location not defined");
            System.exit(1);
        }

        String dir = params.get("alignDir");
        if (dir == null) {
            System.out.println("Translated files location not defined");
            System.exit(1);
        }

        System.out.println("Initializing");
        try {
            Core.initializeConsole(params);
        } catch (Throwable ex) {
            showError(ex);
        }
        try {
            System.out.println("Loading Project");

            // check if project okay
            ProjectProperties projectProperties = null;
            try {
                projectProperties = ProjectFileStorage
                        .loadProjectProperties(projectLocation);
                if (!projectProperties.verifyProject()) {
                    System.out.println("The project cannot be verified");
                    System.exit(1);
                }
            } catch (Exception ex) {
                Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                System.out.println(OStrings
                        .getString("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE"));
                System.exit(1);
            }

            RealProject p = new RealProject(projectProperties);
            Core.setProject(p);

            System.out.println("Align project against " + dir);

            Map<String, TransEntry> data = p.align(p.getProjectProperties(),
                    new File(dir));

            String tmxFile = p.getProjectProperties().getProjectInternal()
                    + "align.tmx";

            TMXWriter.buildTMXFile(tmxFile, false, false, p
                    .getProjectProperties(), data);

            System.out.println("Finished");
        } catch (Exception e) {
            System.err.println("An error has occured: " + e.toString());
            System.exit(1);
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
                    OStrings.getString("STARTUP_ERRORBOX_TITLE"),
                    JOptionPane.ERROR_MESSAGE);
            break;
        case CONSOLE_TRANSLATE:
            System.err.println(msg);
            break;
        }
        System.exit(1);
    }
}
