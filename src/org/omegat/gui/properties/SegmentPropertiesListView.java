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

package org.omegat.gui.properties;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.omegat.core.Core;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.ReasonablySizedPanel;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * A list-based view of key=value properties of the current segment.
 * <p>
 * This is not a JList but instead a collection of distinct instances of {@link SegmentPropertiesListCell}.
 * This is because the initial JList-based implementation had rendering issues when trying to make the gear
 * menu icons appear interactive.
 *
 * @author Aaron Madlon-Kay
 */
public class SegmentPropertiesListView implements ISegmentPropertiesView {

    private SegmentPropertiesArea parent;
    private JPanel panel;

    public void install(final SegmentPropertiesArea parent) {
        this.parent = parent;
        panel = new ReasonablySizedPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setFont(Core.getMainWindow().getApplicationFont());
        panel.setOpaque(true);
        parent.scrollPane.setViewportView(panel);
    }

    @Override
    public void update() {
        UIThreadsUtil.mustBeSwingThread();
        panel.removeAll();
        for (int i = 0; i < parent.properties.size(); i += 2) {
            final SegmentPropertiesListCell cell = new SegmentPropertiesListCell();
            String key = parent.properties.get(i);
            cell.key = key;
            cell.label.setText(getDisplayKey(key));
            cell.value.setText(parent.properties.get(i + 1));
            cell.value.setFont(panel.getFont());
            cell.settingsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    parent.showContextMenu(
                            SwingUtilities.convertPoint(cell, cell.settingsButton.getLocation(),
                                    parent.scrollPane));
                }
            });
            panel.add(cell);
        }
        panel.validate();
        panel.repaint();
    }

    private String getDisplayKey(String key) {
        if (Preferences.isPreference(Preferences.SEGPROPS_SHOW_RAW_KEYS)) {
            return key;
        }
        try {
            return OStrings.getString(PROPERTY_TRANSLATION_KEY + key.toUpperCase(Locale.ENGLISH));
        } catch (MissingResourceException ex) {
            return key;
        }
    }

    @Override
    public JComponent getViewComponent() {
        return panel;
    }

    @Override
    public void notifyUser(List<Integer> notify) {
        UIThreadsUtil.mustBeSwingThread();
        for (int i : notify) {
            try {
                ((SegmentPropertiesListCell) panel.getComponent(i / 2)).value.flash();
            } catch (IndexOutOfBoundsException ex) {
                // Contents of panel have changed. Don't bother continuing.
                break;
            }
        }
    }

    @Override
    public String getKeyAtPoint(Point p) {
        Component comp = panel.getComponentAt(p);
        if (comp instanceof SegmentPropertiesListCell) {
            return ((SegmentPropertiesListCell) comp).key;
        }
        return null;
    }
}
