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

package org.omegat.connectors.tracwiki;

import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.gui.preferences.IPreferencesController;
import org.omegat.util.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;

/**
 * Preferences for Trac Wiki connector.
 */
public class TracWikiPreferencesController extends BasePreferencesController implements IPreferencesController {

    private final JPanel panel = new JPanel();
    private final JCheckBox useXmlRpcCb = new JCheckBox();

    @Override
    public Component getGui() {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        useXmlRpcCb.setText("Use XML-RPC (requires permission on Trac server)");
        JLabel hint = new JLabel("If disabled, OmegaT will retrieve pages by scraping the ?action=edit form.");
        hint.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        useXmlRpcCb.setAlignmentX(JComponent.LEFT_ALIGNMENT);

        panel.add(useXmlRpcCb);
        panel.add(Box.createRigidArea(new Dimension(0, 6)));
        panel.add(hint);

        initFromPrefs();

        return panel;
    }

    @Override
    public void persist() {
        boolean useRpc = useXmlRpcCb.isSelected();
        Preferences.setPreference(TracWikiConnector.PREF_USE_XMLRPC, useRpc);
    }

    @Override
    public void restoreDefaults() {
        useXmlRpcCb.setSelected(false);
        persist();
    }

    @Override
    protected void initFromPrefs() {
        boolean useRpc = Preferences.isPreferenceDefault(TracWikiConnector.PREF_USE_XMLRPC, false);
        useXmlRpcCb.setSelected(useRpc);
    }

    @Override
    public String toString() {
        return "Trac Wiki";
    }
}
