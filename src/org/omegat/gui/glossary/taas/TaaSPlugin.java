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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.openide.awt.Mnemonics;

/**
 * TaaS plugin.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TaaSPlugin {

    static JMenuItem browse;

    private static class ClientHolder {
        private static TaaSClient CLIENT = new TaaSClient();
    }

    public static TaaSClient getClient() {
        return ClientHolder.CLIENT;
    }

    private TaaSPlugin() {
    }

    static Transformer filterTransformer, filterTransformerContext;

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        TaaSGlossary glossary;
        TaaSClient client = getClient();
        try {
            client.init();
            if (client.isAllowed()) {
                glossary = new TaaSGlossary();
                try (InputStream in = TaaSGlossary.class.getResourceAsStream("filter.xslt")) {
                    if (in == null) {
                        throw new Exception("filter.xslt is unaccessible");
                    }
                    TransformerFactory factory = TransformerFactory.newInstance();
                    Source xslt = new StreamSource(in);
                    filterTransformer = factory.newTransformer(xslt);
                }
            } else {
                glossary = null;
            }
        } catch (Exception ex) {
            Log.log(ex);
            return;
        }

        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            public void onApplicationStartup() {
                JMenu menu = Core.getMainWindow().getMainMenu().getGlossaryMenu();

                browse = new JMenuItem();
                Mnemonics.setLocalizedText(browse, OStrings.getString("TAAS_MENU_BROWSE"));
                browse.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (client.isAllowed()) {
                            BrowseTaasCollectionsController.show();
                        } else {
                            JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                                    OStrings.getString("TAAS_API_KEY_NOT_FOUND"),
                                    OStrings.getString("TF_WARNING"), JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });
                browse.setEnabled(false);
                menu.add(browse);

                JMenuItem select = new JMenuItem();
                Mnemonics.setLocalizedText(select, OStrings.getString("TAAS_MENU_DOMAINS"));
                select.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (client.isAllowed()) {
                            SelectDomainController.show();
                        } else {
                            JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                                    OStrings.getString("TAAS_API_KEY_NOT_FOUND"),
                                    OStrings.getString("TF_WARNING"), JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });
                menu.add(select);

                final JMenuItem lookup = new JCheckBoxMenuItem();
                lookup.setSelected(Preferences.isPreferenceDefault(Preferences.TAAS_LOOKUP, false));
                Mnemonics.setLocalizedText(lookup, OStrings.getString("TAAS_MENU_LOOKUP"));
                lookup.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Preferences.setPreference(Preferences.TAAS_LOOKUP, lookup.isSelected());
                        Preferences.save();
                        if (!client.isAllowed()) {
                            JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                                    OStrings.getString("TAAS_API_KEY_NOT_FOUND"),
                                    OStrings.getString("TF_WARNING"), JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });
                menu.add(lookup);

                if (client.isAllowed()) {
                    Core.getGlossaryManager().addGlossaryProvider(glossary);
                }
            }

            public void onApplicationShutdown() {
            }
        });
        CoreEvents.registerProjectChangeListener(new IProjectEventListener() {
            public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
                if (browse == null) {
                    return;
                }
                switch (eventType) {
                case CLOSE:
                    browse.setEnabled(false);
                    break;
                case CREATE:
                case LOAD:
                    browse.setEnabled(true);
                    break;
                default:
                    // Nothing
                }
            }
        });
    }

    static String filterTaasResult(String xml) throws Exception {
        Source src = new StreamSource(new StringReader(xml));
        StringWriter out = new StringWriter();
        filterTransformer.transform(src, new StreamResult(out));

        return out.toString();
    }

    static void filterTaasResult(InputStream in, OutputStream out) throws Exception {
        Source src = new StreamSource(in);
        filterTransformer.transform(src, new StreamResult(out));
    }

    public static void unloadPlugins() {
    }
}
