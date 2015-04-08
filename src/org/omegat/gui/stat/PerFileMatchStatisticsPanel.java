/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.gui.stat;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import org.omegat.util.gui.StaticUIUtils;

/**
 *
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class PerFileMatchStatisticsPanel extends BaseMatchStatisticsPanel {

    private final JPanel tablesPanel;
    private final JScrollPane scrollPane;
    
    public PerFileMatchStatisticsPanel(StatisticsWindow window) {
        super(window);
        setLayout(new BorderLayout());
        tablesPanel = new ReasonablySizedPanel();
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(tablesPanel);
        scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane);
    }
    
    private static class ReasonablySizedPanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return getFont().getSize();
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return getFont().getSize();
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
        
    }
    
    @Override
    public void appendTable(final String title, final String[] headers, final String[][] data) {
        if (headers == null || headers.length == 0) {
            return;
        }
        if (data == null || data.length == 0) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                TitledTablePanel panel = generateTableDisplay(title, headers, data);
                panel.scrollPane.addMouseWheelListener(mouseWheelListener);
                tablesPanel.add(panel);
            }
        });
    }
    
    private final MouseWheelListener mouseWheelListener = new MouseAdapter() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            StaticUIUtils.forwardMouseWheelEvent(scrollPane, e);
        }
    };

    @Override
    public void setTable(String[] headers, String[][] data) {
        // Nothing
    }
}
