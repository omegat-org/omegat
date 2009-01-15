/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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
import java.util.Date;
import java.util.Locale;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.RealProject;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.RuntimePreferences;

import com.vlsolutions.swing.docking.DockingDesktop;

/**
 * The main OmegaT class, used to launch the program.
 * 
 * @author Keith Godfrey
 */
public class Main {
    enum RUN_MODE {
        GUI, CONSOLE_TRANSLATE
    };

    protected static RUN_MODE runMode = RUN_MODE.GUI;

    public static void main(String[] args) {
        String projectLocation = "";
        for (String arg : args) {
            if (arg.startsWith("locale=")) {
                String language = arg.substring(7, 9);
                String country = arg.length() > 10 ? arg.substring(10, 12)
                        : null;
                Locale.setDefault(country != null ? new Locale(language,
                        country) : new Locale(language));
            } else if (arg.startsWith("resource-bundle=")) {
                String filename = arg.substring(16);
                OStrings.loadBundle(filename);
            } else if (arg.startsWith("project=")) {
                projectLocation = arg.substring(8);
                runMode = RUN_MODE.CONSOLE_TRANSLATE;
            } else if (arg.startsWith("config-dir=")) {
                RuntimePreferences.setConfigDir(arg.substring(11));
            } else if (arg.startsWith("-quiet")) {
                RuntimePreferences.setQuietMode(true);
            }
        }

        Log.log("\n"
                + // NOI18N
                "==================================================================="
                + // NOI18N
                "\n" + // NOI18N
                OStrings.getDisplayVersion() + // NOI18N
                " (" + new Date() + ") " + // NOI18N
                " Locale " + Locale.getDefault()); // NOI18N

        Log.logRB("LOG_STARTUP_INFO", System.getProperty("java.vendor"), System
                .getProperty("java.version"), System.getProperty("java.home"));

        switch (runMode) {
        case GUI:
            runGUI(args);
            break;
        case CONSOLE_TRANSLATE:
            runConsoleTranslate(args, projectLocation);
            break;
        }
    }

    /**
     * Execute standard GUI.
     */
    protected static void runGUI(String[] args) {
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
            System.setProperty("apple.laf.useScreenMenuBar", "true"); // NOI18N
            System
                    .setProperty(
                            "com.apple.mrj.application.apple.menu.about.name",
                            "OmegaT"); // NOI18N
        } catch (Exception e) {
            // do nothing
            Log.logErrorRB("MAIN_ERROR_CANT_INIT_OSLF");
        }

        try {
            Core.initializeGUI(args);
        } catch (Throwable ex) {
            showError(ex);
        }

        CoreEvents.fireApplicationStartup();
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
    protected static void runConsoleTranslate(String[] args,
            String projectLocation) {
        Log.log("Console mode");
        Log.log("");

        System.out.println("Initializing");
        try {
            Core.initializeConsole(args);
        } catch (Throwable ex) {
            showError(ex);
        }
        try {
            System.out.println("Loading Project");
            File projectRootFolder = new File(projectLocation);

            // check if project okay
            ProjectProperties projectProperties = null;
            try {
                projectProperties = ProjectFileStorage
                        .loadProjectProperties(projectRootFolder);
                if (!projectProperties.verifyProject()) {
                    System.out.println("Project kan niet geverifieerd worden");
                    System.exit(1);
                }
            } catch (Exception ex) {
                Log.logErrorRB(ex, "PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                System.out.println("PP_ERROR_UNABLE_TO_READ_PROJECT_FILE");
                System.exit(1);
            }

            RealProject p = new RealProject(projectProperties, false);
            Core.setProject(p);

            System.out.println("Translating Project");
            p.compileProject();

            System.out.println("Finished");
        } catch (Exception e) {
            System.err.println("An error has occured: " + e.toString());
            System.exit(1);
        }
    }

    public static void showError(Throwable ex) {
        switch (runMode) {
        case GUI:
            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), ex
                    .getMessage(),
                    OStrings.getString("STARTUP_ERRORBOX_TITLE"),
                    JOptionPane.ERROR_MESSAGE);
            break;
        case CONSOLE_TRANSLATE:
            System.err.println(ex.getMessage());
            break;
        }
        System.exit(1);
    }
}
