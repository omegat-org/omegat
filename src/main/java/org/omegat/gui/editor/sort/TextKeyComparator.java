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
package org.omegat.gui.editor.sort;

import java.util.Comparator;

/**
 * A string comparator whose per-string sort key can be pre-computed. The sort
 * bar runs {@link #prime} for every entry text in a background worker (with a
 * progress bar) before the sort is applied on the UI thread, which then only
 * reads the cached keys. Priming and comparing must not overlap.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
interface TextKeyComparator extends Comparator<String> {

    /** Compute and cache the sort key for {@code s} without comparing. */
    void prime(String s);
}
