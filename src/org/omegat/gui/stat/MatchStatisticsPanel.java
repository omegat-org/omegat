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
import javax.swing.SwingUtilities;

/**
 *
 * @author Aaron Madlon-Kay
 */
public class MatchStatisticsPanel extends BaseMatchStatisticsPanel {

    public MatchStatisticsPanel(StatisticsWindow window) {
        super(window);
        setLayout(new BorderLayout());
    }

    @Override
    public void appendTable(String title, String[] headers, String[][] data) {
        // Nothing
    }

    @Override
    public void setTable(final String[] headers, final String[][] data) {
        if (headers == null || headers.length == 0) {
            return;
        }
        if (data == null || data.length == 0) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // A simpler table is first shown, then replaced with a fancier one,
                // so have to remove first.
                removeAll();
                add(generateTableDisplay(null, headers, data));
            }
        });
    }
}
