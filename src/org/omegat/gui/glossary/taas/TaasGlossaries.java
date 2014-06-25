/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.gui.glossary.taas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.util.OStrings;
import org.openide.awt.Mnemonics;

/**
 * TaaS plugin.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TaasGlossaries {
    public static TaaSClient client;

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        try {        
            client = new TaaSClient();
        } catch (Exception ex) {
            // Eat error silently
        }

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            public void onApplicationStartup() {
                JMenu menu = Core.getMainWindow().getMainMenu().getGlossaryMenu();
                menu.setEnabled(true);

                JMenuItem browse = new JMenuItem();
                Mnemonics.setLocalizedText(browse, OStrings.getString("TAAS_MENU_BROWSE"));
                browse.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        BrowseTaasCollectionsController.show();
                    }
                });
                menu.add(browse);

                JMenuItem lookup = new JMenuItem();
                Mnemonics.setLocalizedText(lookup, OStrings.getString("TAAS_MENU_LOOKUP"));
                lookup.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                    }
                });
                menu.add(lookup);
            }

            public void onApplicationShutdown() {
            }
        });
    }

    public static void unloadPlugins() {
    }
}
