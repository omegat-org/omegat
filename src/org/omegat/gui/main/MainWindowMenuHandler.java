/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
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

package org.omegat.gui.main;

import java.awt.event.ActionEvent;
import java.io.IOException;

import org.omegat.core.Core;
import org.omegat.core.spellchecker.SpellChecker;
import org.omegat.core.threads.CommandThread;
import org.omegat.filters2.TranslationException;
import org.omegat.gui.dialogs.SpellcheckerConfigurationDialog;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Handler for main menu items.
 * 
 * @author Keith Godfrey
 * @author Benjamin Siband
 * @author Maxym Mykhalchuk
 * @author Kim Bruning
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MainWindowMenuHandler {
    private final MainWindow mainWindow;

    public MainWindowMenuHandler(final MainWindow mainWindow) {
        this.mainWindow = mainWindow;
    }

    /**
     * Create translated documents.
     */
    public void projectCompileMenuItemActionPerformed()
    {
        if (!mainWindow.isProjectLoaded())
            return;
        
        try
        {
            Core.getDataEngine().compileProject();
        }
        catch(IOException e)
        {
            Core.getMainWindow().displayError(OStrings.getString("TF_COMPILE_ERROR"), e);
        }
        catch(TranslationException te)
        {
            Core.getMainWindow().displayError(OStrings.getString("TF_COMPILE_ERROR"), te);
        }
    }
    
    /**
     * Opens the spell checking window
     */
    public void optionsSpellCheckMenuItemActionPerformed() {
        SpellcheckerConfigurationDialog sd = new SpellcheckerConfigurationDialog(mainWindow, Core.getDataEngine()
                .getProjectProperties().getTargetLanguage());
        sd.setVisible(true);
        if (sd.getReturnStatus() == SpellcheckerConfigurationDialog.RET_OK) {
            mainWindow.m_autoSpellChecking = Preferences.isPreference(Preferences.ALLOW_AUTO_SPELLCHECKING);
            if (mainWindow.m_autoSpellChecking) {
                SpellChecker sc = CommandThread.core.getSpellchecker();
                sc.destroy();
                sc.initialize();
            }
            mainWindow.commitEntry(false);
            mainWindow.loadDocument();
            mainWindow.activateEntry();
        }
    }
}
