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

package org.omegat.gui.align;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.omegat.util.Language;

import net.loomchild.maligna.coretypes.Alignment;

/**
 * A container class used to store data for alignment. "Bead" is used in NLP literature to refer to a grouping
 * of source and target sentences that are held to correlate (are aligned together).
 *
 * @author Aaron Madlon-Kay
 */
class MutableBead {

    /**
     * Status flags applied by the user; for user-directed data management only.
     */
    enum Status {
        DEFAULT, ACCEPTED, NEEDS_REVIEW
    }

    public final float score;
    public final List<String> sourceLines;
    public final List<String> targetLines;
    public boolean enabled;
    public MutableBead.Status status;

    private MutableBead(float score, List<String> sourceLines, List<String> targetLines) {
        this.score = score;
        this.sourceLines = new ArrayList<String>(sourceLines);
        this.targetLines = new ArrayList<String>(targetLines);
        boolean srcEqualsTrg = sourceLines.equals(targetLines);
        this.enabled = !srcEqualsTrg;
        this.status = srcEqualsTrg ? MutableBead.Status.ACCEPTED : MutableBead.Status.DEFAULT;
    }

    MutableBead(Alignment alignment) {
        this(alignment.getScore(), alignment.getSourceSegmentList(), alignment.getTargetSegmentList());
    }

    MutableBead(List<String> sourceLines, List<String> targetLines) {
        this(Float.MAX_VALUE, sourceLines, targetLines);
    }

    MutableBead(String source, String target) {
        this(Arrays.asList(source), Arrays.asList(target));
    }

    /**
     * Create an empty new bead. {@link #enabled} is <code>true</code> by
     * default because it is assumed that the caller will populate the bead with
     * interesting data.
     */
    MutableBead() {
        this(Collections.emptyList(), Collections.emptyList());
        this.enabled = true;
        this.status = Status.DEFAULT;
    }

    /**
     * Get whether or not the bead contains the same number of source and target lines.
     *
     * @return
     */
    public boolean isBalanced() {
        return sourceLines.size() == targetLines.size();
    }

    /**
     * Get whether or not the bead is entirely empty (has 0 source lines and 0 target lines).
     *
     * @return
     */
    public boolean isEmpty() {
        return sourceLines.isEmpty() && targetLines.isEmpty();
    }

    /**
     * Convert a list of beads to a list of flattened (see {@link #join(Language, List)}) pairs where
     * <ol>
     * <li>key = source text
     * <li>value = target text
     * </ol>
     *
     * @param beads
     *            List of beads to convert
     * @return List of squashed pairs
     */
    static List<Entry<String, String>> beadsToEntries(Language srcLang, Language trgLang,
            List<MutableBead> beads) {
        return beads.stream().filter(bead -> bead.enabled).map(bead -> {
            String srcOut = bead.sourceLines.isEmpty() ? null : Util.join(srcLang, bead.sourceLines);
            String trgOut = bead.targetLines.isEmpty() ? null : Util.join(trgLang, bead.targetLines);
            return new AbstractMap.SimpleImmutableEntry<String, String>(srcOut, trgOut);
        }).collect(Collectors.toList());
    }

    /**
     * Get the average score of the list of beads. In mALIGNa the "score" is <code>-ln(probability)</code> of
     * the alignment, so lower scores are better. We use {@link Double#MAX_VALUE} as a sentinel for failure to
     * calculate (empty list, etc.).
     *
     * @param beads
     * @return Average score, or {@link Double#MAX_VALUE} if incalculable
     */
    static double calculateAvgDist(List<MutableBead> beads) {
        return beads.stream().mapToDouble(bead -> bead.score).average().orElse(Double.MAX_VALUE);
    }
}
