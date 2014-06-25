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

import gen.taas.TaasDomain;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;

import org.omegat.core.Core;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.DockingUI;

/**
 * Controller for select TaaS domains.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SelectDomainController {
    static SelectDomainUI dialog;

    public static void show() {
        dialog = new SelectDomainUI(Core.getMainWindow().getApplicationFrame(), true);
        dialog.scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        dialog.labelStatus.setText(OStrings.getString("TAAS_STATUS_DOMAIN_LIST"));

        new SwingWorker<List<TaasDomain>, Void>() {
            protected List<TaasDomain> doInBackground() throws Exception {
                List<TaasDomain> result = TaaSPlugin.client.getDomainsList();

                Collections.sort(result, new Comparator<TaasDomain>() {
                    @Override
                    public int compare(TaasDomain d1, TaasDomain d2) {
                        return d1.getName().compareToIgnoreCase(d2.getName());
                    }
                });
                return result;
            }

            protected void done() {
                try {
                    List<TaasDomain> list = get();
                    String previouslySelected = Preferences.getPreference(Preferences.TAAS_DOMAIN);
                    if (StringUtil.isEmpty(previouslySelected)) {
                        previouslySelected = null;
                        dialog.rbAll.setSelected(true);
                    }

                    for (TaasDomain d : list) {
                        JRadioButton btn = new JRadioButton(d.getName());
                        btn.setName(d.getId());
                        dialog.buttonGroup.add(btn);
                        dialog.list.add(btn);
                        if (previouslySelected != null && previouslySelected.equals(btn.getName())) {
                            btn.setSelected(true);
                        }
                    }
                    dialog.list.revalidate();

                    dialog.labelStatus.setText(" ");
                } catch (ExecutionException e) {
                    Throwable ex = e.getCause();
                    if (ex instanceof TaaSClient.FormatError) {
                        Log.logErrorRB(ex, "TAAS_FORMAT_ERROR", ex.getMessage());
                        dialog.labelStatus.setText(OStrings.getString("TAAS_FORMAT_ERROR"));
                    } else if (ex instanceof TaaSClient.Unauthorized) {
                        Log.logErrorRB(ex, "TAAS_UNAUTHORIZED_ERROR");
                        dialog.labelStatus.setText(OStrings.getString("TAAS_UNAUTHORIZED_ERROR"));
                    } else {
                        Log.logErrorRB(ex, "TAAS_GENERAL_ERROR", ex.getMessage());
                        dialog.labelStatus.setText(MessageFormat.format(
                                OStrings.getString("TAAS_GENERAL_ERROR"), ex.getMessage()));
                    }
                } catch (InterruptedException ex) {
                }
            }
        }.execute();

        dialog.btnSelect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (dialog.rbAll.isSelected()) {
                    Preferences.setPreference(Preferences.TAAS_DOMAIN, "");
                } else {
                    for (Enumeration<AbstractButton> en = dialog.buttonGroup.getElements(); en
                            .hasMoreElements();) {
                        AbstractButton btn = en.nextElement();
                        if (btn.isSelected()) {
                            Preferences.setPreference(Preferences.TAAS_DOMAIN, btn.getName());
                            break;
                        }
                    }
                }
                Preferences.save();
                dialog.dispose();
            }
        });

        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        };
        dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        dialog.getRootPane().getActionMap().put("ESCAPE", escapeAction);

        DockingUI.displayCentered(dialog);

        dialog.setVisible(true);
    }
}
