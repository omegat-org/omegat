/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
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

import org.omegat.core.threads.CommandThread;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

/**
 * The main OmegaT class, used to launch the programm.
 *
 * @author Keith Godfrey
 */
public class Main
{
    public static void main(String[] args)
    {
        StaticUtils.log(
                "\n" +                                                                    // NOI18N
                "===================================================================" +   // NOI18N
                "\n" +                                                                    // NOI18N
                OStrings.OMEGAT_VERSION+                                                         // NOI18N
                " ("+new Date()+") " +                                                    // NOI18N
                " Locale "+Locale.getDefault()+                                           // NOI18N
                "\n");                                                                    // NOI18N
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // MacOSX-specfic
            System.setProperty("apple.laf.useScreenMenuBar", "true");           // NOI18N
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "OmegaT"); // NOI18N
        }
        catch (Exception e)
        {
            // do nothing
            StaticUtils.log(OStrings.getString("MAIN_ERROR_CANT_INIT_OSLF"));
        }
        
        MainWindow mainwindow = new MainWindow();
        
        // bugfix - Serious threading issue, preventing OmegaT from showing up...
        //          http://sourceforge.net/support/tracker.php?aid=1216514
        // we start command thread here...
        CommandThread.core = new CommandThread(mainwindow);
        CommandThread.core.start();
        
        mainwindow.setVisible(true);
    }
}

