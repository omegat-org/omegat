/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
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

package org.omegat.gui.issues;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.WindowConstants;

import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.StaticUIUtils;

/**
 * @author Aaron Madlon-Kay
 */
public class IssueProvidersSelectorController {

    private final Map<String, JCheckBox> providerSettings = new HashMap<>();
    private boolean userDidConfirm;

    public boolean show(Window parent) {
        JDialog dialog = new JDialog(parent, OStrings.getString("ISSUE_PROVIDERS_SELECTOR_TITLE"));
        dialog.setModal(true);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        StaticUIUtils.setEscapeClosable(dialog);
        StaticUIUtils.setWindowIcon(dialog);

        IssueProvidersSelectorPanel panel = new IssueProvidersSelectorPanel();

        dialog.getRootPane().setDefaultButton(panel.okButton);
        dialog.getContentPane().add(panel);

        // Tags item is hard-coded because it is not disableable and is implemented differently from all
        // others.
        JCheckBox tagsCB = new JCheckBox(OStrings.getString("ISSUES_TAGS_PROVIDER_NAME"));
        tagsCB.setSelected(true);
        tagsCB.setEnabled(false);
        panel.providersPanel.add(tagsCB);

        Set<String> disabledIds = IssueProviders.getDisabledProviderIds();
        for (IIssueProvider provider : IssueProviders.getIssueProviders()) {
            JCheckBox cb = new JCheckBox(provider.getName());
            providerSettings.put(provider.getId(), cb);
            cb.setSelected(!disabledIds.contains(provider.getId()));
            panel.providersPanel.add(cb);
        }

        panel.dontAskCheckBox.setSelected(Preferences.isPreference(Preferences.ISSUE_PROVIDERS_DONT_ASK));

        panel.okButton.addActionListener(ev -> {
            userDidConfirm = true;
            List<String> toEnable = providerSettings.entrySet().stream()
                    .filter(e -> e.getValue().isSelected()).map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            List<String> toDisable = providerSettings.entrySet().stream()
                    .filter(e -> !e.getValue().isSelected()).map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            IssueProviders.setProviders(toEnable, toDisable);
            Preferences.setPreference(Preferences.ISSUE_PROVIDERS_DONT_ASK,
                    panel.dontAskCheckBox.isSelected());
            StaticUIUtils.closeWindowByEvent(dialog);
        });

        panel.cancelButton.addActionListener(e -> {
            userDidConfirm = false;
            StaticUIUtils.closeWindowByEvent(dialog);
        });

        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                // Pack again to make sure the height is right (due to the wrapped JTextArea)
                dialog.pack();
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return userDidConfirm;
    }
}
