/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Briac Pilpre
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

package org.omegat.core.states;

import org.omegat.util.Log;

/**
 * The SegmentState is used to indicates the state of the translation of a
 * segment. For each of these states, we have the corresponding XLIFF 1.2 state
 * associated with it to allow easier conversion if needed
 * ("<code>needs-translation</code>",
 * "<code>translated</code>","<code>signed-off</code>" and
 * "<code>final</code>"). If we wanted to use XLIFF 2.1 instead, the
 * corresponding states would be "<code>initial</code>",
 * "<code>translated</code>","<code>reviewed</code>" and "<code>final</code>".
 *
 * @see https://docs.oasis-open.org/xliff/v1.2/os/xliff-core.html#state
 * @see https://docs.oasis-open.org/xliff/xliff-core/v2.1/os/xliff-core-v2.1-os.html#state
 */
public enum SegmentState {
    DRAFT("needs-translation"), TRANSLATED("translated"), REVIEWED("signed-off"), FINAL("final");

    private final String xliffState;

    SegmentState(String xliffState) {
        this.xliffState = xliffState;
    }

    /**
     * A state is not required for every segment. The value can sometime be null or
     * another unknown state is provided.
     */
    public static SegmentState getSegmentState(String s) {
        SegmentState state = null;
        try {
            state = s == null ? null : SegmentState.valueOf(s);
        } catch (IllegalArgumentException e) {
            Log.logWarningRB("STATE_WARNING_UNKNOWN", s);
        }
        return state;
    }

    public String toXliffState() {
        return xliffState;
    }

}