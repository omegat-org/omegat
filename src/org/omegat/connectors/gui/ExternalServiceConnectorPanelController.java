/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.connectors.gui;

import org.omegat.connectors.actions.ExternalServiceRetrieval;
import org.omegat.connectors.spi.ExternalServiceConnector;
import org.omegat.core.Core;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

import javax.swing.JDialog;
import java.awt.Frame;

public class ExternalServiceConnectorPanelController {

    private final ExternalServiceRetrieval externalServiceRetrieval = new ExternalServiceRetrieval();

    public void show() {
        Frame owner = Core.getMainWindow().getApplicationFrame();
        ExternalServiceConnectorPanel panel = new ExternalServiceConnectorPanel();
        JDialog dialog = new JDialog(owner, OStrings.getString("TF_CMS_IMPORT_TITLE"), true);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);

        panel.getLaunchButton().addActionListener(e -> {
            String url = panel.getCustomUrl();
            try {
                ExternalServiceConnector connector = panel.getSelectedConnector();
                if (connector == null) {
                    return;
                }
                String projectId = panel.getProjectId();
                String srcRoot = Core.getProject().getProjectProperties().getSourceRoot();
                if (url != null && !url.trim().isEmpty()) {
                    externalServiceRetrieval.retrieveResourceFromUrl(connector, url.trim(), srcRoot);
                } else {
                    String resourceId = panel.getResourceId();
                    externalServiceRetrieval.retrieveResource(connector, projectId, resourceId, srcRoot);
                }
                ProjectUICommands.projectReload();
            } catch (Exception ex) {
                Log.log(ex);
                Core.getMainWindow().displayErrorRB(ex, "TF_CMS_IMPORT_FAILED");
            } finally {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }
}
