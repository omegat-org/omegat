/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.editor.filter;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Editor filter bar shown while the editor is filtered to the segments of one
 * match statistics category.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
public class FilterBarMatchRange extends JPanel {

    final JButton btnRemoveFilter;

    public FilterBarMatchRange(String categoryLabel, int segmentCount) {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
        add(Box.createHorizontalGlue());
        add(new JLabel(StringUtil.format(OStrings.getString("STATSMATCH_FILTER_BAR_LABEL"), categoryLabel,
                segmentCount)));
        add(Box.createHorizontalStrut(10));
        btnRemoveFilter = new JButton();
        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveFilter,
                OStrings.getString("BUTTON_FILTER_REMOVE_FILTER"));
        add(btnRemoveFilter);
        add(Box.createHorizontalGlue());
        Border border = UIManager.getBorder("OmegaTEditorFilter.border");
        if (border != null) {
            setBorder(border);
        }
    }
}
