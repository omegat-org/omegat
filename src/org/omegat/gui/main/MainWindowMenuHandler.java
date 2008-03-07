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

import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.omegat.core.Core;
import org.omegat.core.spellchecker.SpellChecker;
import org.omegat.core.threads.CommandThread;
import org.omegat.filters2.TranslationException;
import org.omegat.gui.HelpFrame;
import org.omegat.gui.dialogs.AboutDialog;
import org.omegat.gui.dialogs.SpellcheckerConfigurationDialog;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;

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
     * Create new project.
     */
    public void projectNewMenuItemActionPerformed() {
        mainWindow.doCreateProject();
    }

    /**
     * Open project.
     */
    public void projectOpenMenuItemActionPerformed() {
        mainWindow.doLoadProject();
    }

    /**
     * Close project.
     */
    public void projectCloseMenuItemActionPerformed() {
        mainWindow.doCloseProject();
    }

    /**
     * Create translated documents.
     */
    public void projectCompileMenuItemActionPerformed() {
        if (!mainWindow.isProjectLoaded())
            return;

        try {
            Core.getDataEngine().compileProject();
        } catch (IOException e) {
            Core.getMainWindow().displayError(OStrings.getString("TF_COMPILE_ERROR"), e);
        } catch (TranslationException te) {
            Core.getMainWindow().displayError(OStrings.getString("TF_COMPILE_ERROR"), te);
        }
    }

    /** Quits OmegaT */
    public void projectExitMenuItemActionPerformed() {
        boolean projectModified = false;
        if (mainWindow.isProjectLoaded())
            projectModified = CommandThread.core.isProjectModified();

        // RFE 1302358
        // Add Yes/No Warning before OmegaT quits
        if (projectModified || Preferences.isPreference(Preferences.ALWAYS_CONFIRM_QUIT)) {
            if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(mainWindow, OStrings
                    .getString("MW_QUIT_CONFIRM"), OStrings.getString("CONFIRM_DIALOG_TITLE"),
                    JOptionPane.YES_NO_OPTION)) {
                return;
            }
        }

        mainWindow.saveScreenLayout();
        Preferences.save();

        if (mainWindow.isProjectLoaded())
            mainWindow.doSave();

        // shut down
        if (CommandThread.core != null)
            CommandThread.core.interrupt();

        // waiting for CommandThread to finish for 1 minute
        for (int i = 0; i < 600 && CommandThread.core != null; i++) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }

        System.exit(0);
    }

    /** Set active match to #1. */
    public void editSelectFuzzy1MenuItemActionPerformed() {
        mainWindow.matches.setActiveMatch(0);
    }

    /** Set active match to #2. */
    public void editSelectFuzzy2MenuItemActionPerformed() {
        mainWindow.matches.setActiveMatch(1);
    }

    /** Set active match to #3. */
    public void editSelectFuzzy3MenuItemActionPerformed() {
        mainWindow.matches.setActiveMatch(2);
    }

    /** Set active match to #4. */
    public void editSelectFuzzy4MenuItemActionPerformed() {
        mainWindow.matches.setActiveMatch(3);
    }

    /** Set active match to #5. */
    public void editSelectFuzzy5MenuItemActionPerformed() {
        mainWindow.matches.setActiveMatch(4);
    }

    public void gotoPreviousSegmentMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        mainWindow.doPrevEntry();
    }

    /**
     * Asks the user for a segment number and then displays the segment.
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public void gotoSegmentMenuItemActionPerformed() {
        synchronized (mainWindow) {
            // Create a dialog for input
            final JOptionPane input = new JOptionPane(OStrings.getString("MW_PROMPT_SEG_NR_MSG"),
                    JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION); // create
            // option
            // pane
            input.setWantsInput(true); // make it require input
            final JDialog dialog = new JDialog(mainWindow, OStrings.getString("MW_PROMPT_SEG_NR_TITLE"), true); // create
            // dialog
            dialog.setContentPane(input); // add option pane to dialog

            // Make the dialog verify the input
            input.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent event) {
                    // Handle the event
                    if (dialog.isVisible() && (event.getSource() == input)) {
                        // If user pressed Enter or OK, check the input
                        String property = event.getPropertyName();
                        Object value = input.getValue();

                        // Don't do the checks if no option has been selected
                        if (value == JOptionPane.UNINITIALIZED_VALUE)
                            return;

                        if (property.equals(JOptionPane.INPUT_VALUE_PROPERTY)
                                || (property.equals(JOptionPane.VALUE_PROPERTY) && ((Integer) value).intValue() == JOptionPane.OK_OPTION)) {
                            // Prevent the checks from being done twice
                            input.setValue(JOptionPane.UNINITIALIZED_VALUE);

                            // Get the value entered by the user
                            String inputValue = (String) input.getInputValue();

                            // Check if the user entered a value at all
                            if ((inputValue == null) || (inputValue.trim().length() == 0)) {
                                // Show error message
                                displayErrorMessage();
                                return;
                            }

                            // Check if the user really entered a number
                            int segmentNr = -1;
                            try {
                                // Just parse it. If parsed, it's a number.
                                segmentNr = Integer.parseInt(inputValue);
                            } catch (NumberFormatException e) {
                                // If the exception is thrown, the user didn't
                                // enter a number
                                // Show error message
                                displayErrorMessage();
                                return;
                            }

                            // Check if the segment number is within bounds
                            if (segmentNr < 1 || segmentNr > CommandThread.core.numEntries()) {
                                // Tell the user he has to enter a number within
                                // certain bounds
                                displayErrorMessage();
                                return;
                            }
                        }

                        // If we're here, the user has either pressed
                        // Cancel/Esc,
                        // or has entered a valid number. In all cases, close
                        // the dialog.
                        dialog.setVisible(false);
                    }
                }

                private void displayErrorMessage() {
                    JOptionPane.showMessageDialog(dialog, StaticUtils.format(OStrings
                            .getString("MW_SEGMENT_NUMBER_ERROR"), new Object[] { new Integer(CommandThread.core
                            .numEntries()) }), OStrings.getString("TF_ERROR"), JOptionPane.ERROR_MESSAGE);
                }
            });

            // Show the input dialog
            dialog.pack(); // make it look good
            dialog.setLocationRelativeTo(mainWindow); // center it on the main
            // window
            dialog.setVisible(true); // show it

            // Get the input value, if any
            Object inputValue = input.getInputValue();
            if ((inputValue != null) && !inputValue.equals(JOptionPane.UNINITIALIZED_VALUE)) {
                // Go to the segment the user requested
                try {
                    mainWindow.doGotoEntry((String) inputValue);
                } catch (ClassCastException e) {
                    // Shouldn't happen, but still... Just eat silently.
                }
            }
        }
    }

    public void gotoNextSegmentMenuItemActionPerformed(java.awt.event.ActionEvent evt) {
        mainWindow.doNextEntry();
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

    /**
     * Show help.
     */
    public void helpContentsMenuItemActionPerformed() {
        HelpFrame hf = HelpFrame.getInstance();
        hf.setVisible(true);
        hf.toFront();
    }

    /**
     * Shows About dialog
     */
    public void helpAboutMenuItemActionPerformed() {
        new AboutDialog(mainWindow).setVisible(true);
    }
}
