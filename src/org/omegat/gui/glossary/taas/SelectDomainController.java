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

import gen.taas.TaasCollection;
import gen.taas.TaasDomain;

import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JRadioButton;
import javax.swing.SwingWorker;

import org.omegat.core.Core;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
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

        new SwingWorker<List<TaasDomain>, Void>() {
            protected List<TaasDomain> doInBackground() throws Exception {
                return TaaSPlugin.client.getDomainsList();
            }

            protected void done() {
                try {
                    List<TaasDomain> list = get();

                    for (TaasDomain d : list) {
                        JRadioButton btn = new JRadioButton(d.getName());
                        dialog.list.add(btn);
                    }

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

        DockingUI.displayCentered(dialog);

        dialog.setVisible(true);
    }
}
