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

import java.util.Date;
import java.util.Locale;

import javax.swing.UIManager;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.CommandThread;
import org.omegat.gui.TagValidationTool;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * The main OmegaT class, used to launch the programm.
 *
 * @author Keith Godfrey
 */
public class Main
{
    public static void main(String[] args)
    {
        for (String arg : args) {
            if (arg.startsWith("locale=")) {
                String language = arg.substring(7, 9);
                String country  = arg.length() > 10 ? arg.substring(10, 12) : null;
                Locale.setDefault(country != null ? new Locale(language, country) : new Locale(language));
            }
            else if (arg.startsWith("resource-bundle=")) {
                String filename = arg.substring(16);
                OStrings.loadBundle(filename);
            }
        }

        Log.log(
            "\n" +                                                                    // NOI18N
            "===================================================================" +   // NOI18N
            "\n" +                                                                    // NOI18N
            OStrings.getDisplayVersion() +                                            // NOI18N
            " ("+new Date()+") " +                                                    // NOI18N
            " Locale "+Locale.getDefault());                                          // NOI18N

        Log.log("Java: " + System.getProperty("java.vendor") + " " + System.getProperty("java.version") + " from "
                + System.getProperty("java.home"));
        Log.log("");
        
        try
        {
            // Workaround for JDK bug 6389282 (OmegaT bug bug 1555809)
            // it should be called before setLookAndFeel() for GTK LookandFeel
            // Contributed by Masaki Katakai (SF: katakai)
            UIManager.getInstalledLookAndFeels();

            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // MacOSX-specfic
            System.setProperty("apple.laf.useScreenMenuBar", "true");           // NOI18N
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "OmegaT"); // NOI18N
        }
        catch (Exception e)
        {
            // do nothing
            Log.logErrorRB("MAIN_ERROR_CANT_INIT_OSLF");
        }
        
        MainWindow mainwindow = new MainWindow();
        
        // bugfix - Serious threading issue, preventing OmegaT from showing up...
        //          http://sourceforge.net/support/tracker.php?aid=1216514
        // we start command thread here...
        CommandThread.core = new CommandThread(mainwindow);
        CommandThread.core.start();
        
        mainwindow.setVisible(true);
        EditorController ec = new EditorController(mainwindow, mainwindow.editor);

        Core.initialize(CommandThread.core, mainwindow, ec, new TagValidationTool(mainwindow));
        CoreEvents.fireApplicationStartup();
    }
}
