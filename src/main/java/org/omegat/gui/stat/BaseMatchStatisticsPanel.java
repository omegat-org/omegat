/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat.gui.stat;

import java.text.MessageFormat;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.jspecify.annotations.Nullable;

import org.omegat.core.Core;
import org.omegat.core.matching.MatchEquivalence;
import org.omegat.core.threads.Completion;
import org.omegat.util.OStrings;

/**
 *
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public abstract class BaseMatchStatisticsPanel extends BaseStatisticsPanel {

    private final StringBuilder buffer = new StringBuilder();

    public BaseMatchStatisticsPanel(StatisticsWindow window) {
        super(window);
    }

    @Override
    public void appendTextData(final String result) {
        buffer.append(result);
        setTextData(buffer.toString());
    }

    @Override
    public void onComplete(Completion completion) {
        super.onComplete(completion);
        buffer.setLength(0);
    }

    /**
     * Hint label naming the character equivalence classes active in the
     * project (#1681): folding changes the numbers shown, so the window says
     * when it is in effect. Null when no folding applies.
     */
    protected static @Nullable JLabel buildEquivalenceHint() {
        if (!Core.getProject().isProjectLoaded()) {
            return null;
        }
        Set<MatchEquivalence> active = Core.getProject().getProjectProperties()
                .getActiveMatchEquivalences();
        if (active.isEmpty()) {
            return null;
        }
        String names = active.stream().map(MatchEquivalence::getLocalizedName)
                .collect(Collectors.joining(", "));
        JLabel hint = new JLabel(
                MessageFormat.format(OStrings.getString("CT_STATSMATCH_EQUIVALENCE_ACTIVE"), names));
        hint.setBorder(new EmptyBorder(4, 8, 4, 8));
        return hint;
    }
}
