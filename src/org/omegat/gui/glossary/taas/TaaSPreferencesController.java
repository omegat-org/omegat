
/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.glossary.taas;

import java.awt.Desktop;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.omegat.core.Core;
import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.gui.preferences.IPreferencesController;
import org.omegat.gui.preferences.view.GlossaryPreferencesController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * @author Aaron Madlon-Kay
 */
public class TaaSPreferencesController extends BasePreferencesController {

    private static final String TAAS_KEY_URL = "https://term.tilde.com/account/keys/create?system=omegaT";

    private TaaSPreferencesPanel panel;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_TAAS");
    }

    private void initGui() {
        panel = new TaaSPreferencesPanel();
        panel.getKeyButton.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(URI.create(TAAS_KEY_URL));
            } catch (Exception ex) {
                JOptionPane.showConfirmDialog(panel, ex.getLocalizedMessage(),
                        OStrings.getString("ERROR_TITLE"), JOptionPane.ERROR_MESSAGE);
            }
        });
        Timer timer = new Timer(500, e -> {
            persistApiKey();
            updateEnabledness();
        });
        timer.setRepeats(false);
        panel.apiKeyTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                update();
            }

            private void update() {
                if (panel.apiKeyTextField.hasFocus()) {
                    timer.restart();
                }
            }
        });
        panel.temporaryCheckBox.addActionListener(e -> persistApiKey());
        panel.selectDomainButton.addActionListener(e -> SelectDomainController.show());
        panel.browseCollectionsButton.addActionListener(e -> BrowseTaasCollectionsController.show());
        updateEnabledness();
    }

    private void persistApiKey() {
        try {
            String key = panel.apiKeyTextField.getText();
            boolean temporary = panel.temporaryCheckBox.isSelected();
            TaaSPlugin.getClient().setApiKey(key, temporary);
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void updateEnabledness() {
        boolean enabled = TaaSPlugin.getClient().isAllowed();
        panel.messagePanel.setVisible(!enabled);
        panel.lookupCheckBox.setEnabled(enabled);
        panel.selectDomainButton.setEnabled(enabled);
        panel.browseCollectionsButton.setEnabled(enabled && Core.getProject().isProjectLoaded());
    }

    @Override
    protected void initFromPrefs() {
        panel.temporaryCheckBox.setSelected(TaaSPlugin.getClient().isApiKeyStoredTemporarily());
        panel.apiKeyTextField.setText(TaaSPlugin.getClient().getApiKey());
        panel.lookupCheckBox.setSelected(Preferences.isPreference(Preferences.TAAS_LOOKUP));
    }

    @Override
    public void restoreDefaults() {
        panel.temporaryCheckBox.setSelected(TaaSPlugin.getClient().isApiKeyStoredTemporarily());
        panel.apiKeyTextField.setText(TaaSPlugin.getClient().getApiKey());
        panel.lookupCheckBox.setSelected(false);
    }

    @Override
    public void persist() {
        persistApiKey();
        Preferences.setPreference(Preferences.TAAS_LOOKUP, panel.lookupCheckBox.isSelected());
    }

    @Override
    public Class<? extends IPreferencesController> getParentViewClass() {
        return GlossaryPreferencesController.class;
    }
}
