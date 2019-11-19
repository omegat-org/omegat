/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Chihiro Hio
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

package org.omegat.externalfinder.item;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.omegat.core.Core;
import org.omegat.externalfinder.ExternalFinder;
import org.omegat.externalfinder.item.ExternalFinderItem.SCOPE;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.openide.awt.Mnemonics;

public class ExternalFinderItemMenuGenerator implements IExternalFinderItemMenuGenerator {

    private final ExternalFinderItem.TARGET target;
    private final boolean popup;

    public ExternalFinderItemMenuGenerator(ExternalFinderItem.TARGET target, boolean popup) {
        this.target = target;
        this.popup = popup;
    }

    @Override
    public List<JMenuItem> generate() {
        List<ExternalFinderItem> finderItems = ExternalFinder.getItems();
        if (finderItems.isEmpty()) {
            return Collections.emptyList();
        }
        List<JMenuItem> menuItems = new ArrayList<>();

        // generate menu
        for (ExternalFinderItem finderItem : finderItems) {
            if (popup && finderItem.isNopopup()) {
                continue;
            }
            if (target == ExternalFinderItem.TARGET.ASCII_ONLY
                    && finderItem.isNonAsciiOnly()) {
                continue;
            } else if (target == ExternalFinderItem.TARGET.NON_ASCII_ONLY
                    && finderItem.isAsciiOnly()) {
                continue;
            }

            JMenuItem item = new JMenuItem();
            Mnemonics.setLocalizedText(item, finderItem.getName());

            // set keyboard shortcut
            if (!popup) {
                item.setAccelerator(finderItem.getKeystroke());
            }
            item.addActionListener(new ExternalFinderItemActionListener(finderItem));

            menuItems.add(item);
        }
        return menuItems;
    }

    private static class ExternalFinderItemActionListener implements ActionListener {

        private final SCOPE scope;
        private final List<ExternalFinderItemURL> urls;
        private final List<ExternalFinderItemCommand> commands;

        ExternalFinderItemActionListener(ExternalFinderItem finderItem) {
            this.urls = finderItem.getURLs();
            this.commands = finderItem.getCommands();
            this.scope = finderItem.getScope();
        }

        public void actionPerformed(ActionEvent e) {
            final String selection = Core.getEditor().getSelectedText();
            if (selection == null) {
                return;
            }

            final String targetWords = selection; // selection.trim();
            final boolean isASCII = ExternalFinderItem.isASCII(targetWords);

            new Thread(() -> {
                for (ExternalFinderItemURL url : urls) {
                    if ((isASCII && (url.getTarget() == ExternalFinderItem.TARGET.NON_ASCII_ONLY))
                            || (!isASCII && (url.getTarget() == ExternalFinderItem.TARGET.ASCII_ONLY))) {
                        continue;
                    }

                    try {
                        Desktop.getDesktop().browse(url.generateURL(targetWords));
                    } catch (Exception ex) {
                        Logger.getLogger(ExternalFinderItemMenuGenerator.class.getName()).log(Level.SEVERE,
                                null, ex);
                        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), ex.getLocalizedMessage(),
                                OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }).start();

            new Thread(() -> {
                for (ExternalFinderItemCommand command : commands) {
                    if ((isASCII && (command.getTarget() == ExternalFinderItem.TARGET.NON_ASCII_ONLY))
                            || (!isASCII && (command.getTarget() == ExternalFinderItem.TARGET.ASCII_ONLY))) {
                        continue;
                    }
                    if (scope == SCOPE.PROJECT && !Preferences
                            .isPreference(Preferences.EXTERNAL_FINDER_ALLOW_PROJECT_COMMANDS)) {
                        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(),
                                OStrings.getString("EXTERNALFINDER_PROJECT_COMMANDS_DISALLOWED_MESSAGE"),
                                OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try {
                        Runtime.getRuntime().exec(command.generateCommand(targetWords));
                    } catch (Exception ex) {
                        Logger.getLogger(ExternalFinderItemMenuGenerator.class.getName()).log(Level.SEVERE,
                                null, ex);
                        JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), ex.getLocalizedMessage(),
                                OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }).start();
        }
    }
}
