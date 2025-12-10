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

import org.jspecify.annotations.Nullable;
import org.omegat.connectors.actions.ExternalServiceRetrieval;
import org.omegat.connectors.dto.ExternalResource;
import org.omegat.connectors.dto.ServiceTarget;
import org.omegat.connectors.spi.ConnectorCapability;
import org.omegat.connectors.spi.ConnectorException;
import org.omegat.connectors.spi.IExternalServiceConnector;
import org.omegat.core.Core;
import org.omegat.core.data.CoreState;
import org.omegat.gui.dialogs.PreferencesDialog;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

import javax.swing.JDialog;
import java.awt.Frame;
import java.awt.Window;
import java.util.List;

public class ExternalServiceConnectorPanelController {

    private final ExternalServiceConnectorPanel panel;

    public ExternalServiceConnectorPanelController() {
        panel = new ExternalServiceConnectorPanel();
    }

    private @Nullable IExternalServiceConnector getSelectedConnector() {
        ServiceTarget target = panel.getSelectedTarget();
        if (target == null) {
            return null;
        }
        return CoreState.getInstance().getExternalConnectorsManager().get(target.getConnectorId());
    }

    private void openSearchDialog() {
        ServiceTarget target = panel.getSelectedTarget();
        if (target == null) {
            return;
        }
        IExternalServiceConnector connector = CoreState.getInstance().getExternalConnectorsManager().get(
                target.getConnectorId());
        if (!connector.supports(ConnectorCapability.LIST)) {
            return;
        }
        var keyword = panel.getResourceId();
        if (keyword == null || keyword.isBlank()) {
            return;
        }
        try {
            List<ExternalResource> resources = connector.listResources(target, keyword);
            panel.openSearchDialog(resources);
        } catch (ConnectorException ex) {
            Log.log(ex);
        }
    }

    public void show() {
        Frame owner = Core.getMainWindow().getApplicationFrame();
        JDialog dialog = new JDialog(owner, OStrings.getString("TF_EXTERNAL_SERVICE_IMPORT_TITLE"), true);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);

        activateFields();
        panel.addTargetActionListener(e -> activateFields());
        panel.addSearchButtonActionListener(e -> openSearchDialog());
        panel.addDefineTargetButtonActionListener(e -> openPreferenceDialog());
        panel.getLaunchButton().addActionListener(e -> retrieveResource(dialog));

        StaticUIUtils.setEscapeClosable(dialog);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        panel.cancelButton.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private void activateFields() {
        IExternalServiceConnector connector = getSelectedConnector();
        if (connector == null) {
            return;
        }
        panel.searchPageButton.setEnabled(connector.supports(ConnectorCapability.LIST));
        panel.urlField.setEnabled(connector.allowCustomUrl());
    }
    
    private void openPreferenceDialog() {
        ExternalServiceConnectorCustomizer customizer = new ExternalServiceConnectorCustomizer();
        customizer.show(Core.getMainWindow().getApplicationFrame());
        panel.loadTargetsFromPrefs();
    }

    private void retrieveResource(JDialog dialog) {
        String url = panel.getCustomUrl();
        try {
            IExternalServiceConnector connector = getSelectedConnector();
            if (connector == null) {
                return;
            }
            // Always provide selected target to the connector
            ServiceTarget target = panel.getSelectedTarget();
            if (target == null) {
                return;
            }
            String srcRoot = Core.getProject().getProjectProperties().getSourceRoot();
            ExternalServiceRetrieval externalServiceRetrieval = new ExternalServiceRetrieval();
            if (url != null && !url.trim().isEmpty()) {
                externalServiceRetrieval.retrieveResourceFromUrl(connector, url.trim(), srcRoot);
            } else if (panel.getResourceId() != null) {
                externalServiceRetrieval.retrieveResource(connector, target, panel.getResourceId(), srcRoot);
            } else {
                return;
            }
            ProjectUICommands.projectReload();
        } catch (Exception ex) {
            Log.log(ex);
            Core.getMainWindow().displayErrorRB(ex, "TF_EXTERNAL_SERVICE_IMPORT_FAILED");
        } finally {
            dialog.dispose();
        }
    }

    public static class ExternalServiceConnectorCustomizer {
        private final PreferencesDialog dialog;

        public ExternalServiceConnectorCustomizer() {
            ExternalServiceConnectorPreferencesController view = new ExternalServiceConnectorPreferencesController();
            this.dialog = new PreferencesDialog(view);
        }

        public boolean show(Window parent) {
            return dialog.show(parent);
        }
    }
}
