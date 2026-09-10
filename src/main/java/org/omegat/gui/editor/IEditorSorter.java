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

package org.omegat.gui.editor;

import java.util.Comparator;

/**
 * Hook for ordering the editor's displayed segments: it decides in which
 * order the segments of the currently displayed file appear, without any
 * influence on which segments the editor displays.
 * This is an extension point: the core applies the comparator to the
 * segments being displayed while it loads the editor document. The sorter
 * itself (criteria, UI, etc.) can live outside the core, e.g. in a plugin
 * that calls {@link IEditor#setSort}.
 * <p>
 * <b>Scope:</b> the editor loads one source file at a time, so the comparator
 * only reorders the segments <em>within the currently displayed file</em>. It
 * does not establish a project-wide order across files; navigation still moves
 * from file to file in natural project order.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public interface IEditorSorter {

    /**
     * The comparator used to order the displayed list of segments. It must
     * define a stable, total order; implementations should fall back to the
     * natural project order (by {@code entryNum}) for otherwise-equal segments so
     * that the result is fully deterministic.
     */
    Comparator<SegmentBuilder> getComparator();
}
