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

package org.omegat.gui.numberconvert;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import org.omegat.util.NumberAutoConverter;
import org.omegat.util.NumberAutoConverter.Conversion;
import org.omegat.util.NumberAutoConverter.DataType;

/**
 * GUI-free core of the number-only auto-conversion window (feature request
 * #794): it turns a single segment source into at most one conversion proposal,
 * the highest-confidence conversion of the enabled data types. Kept separate
 * from the Swing window so the proposal logic is unit-testable.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class NumberAutoConvertScanner {

    private NumberAutoConvertScanner() {
    }

    /** One convertible segment and the proposed conversion for it. */
    public static final class Proposal {
        private final int segmentNumber;
        private final String source;
        private final Conversion conversion;

        public Proposal(int segmentNumber, String source, Conversion conversion) {
            this.segmentNumber = segmentNumber;
            this.source = source;
            this.conversion = conversion;
        }

        public int getSegmentNumber() {
            return segmentNumber;
        }

        public String getSource() {
            return source;
        }

        public DataType getType() {
            return conversion.getType();
        }

        public String getTarget() {
            return conversion.getTarget();
        }

        public double getConfidence() {
            return conversion.getConfidence();
        }

        /** The named contributions that make up the confidence (base first). */
        public java.util.List<NumberAutoConverter.ConfidenceFactor> getConfidenceFactors() {
            return conversion.getFactors();
        }

        /**
         * The numeric value parsed from the source in the source locale, when
         * the data type has one. Numeric sorting must use this value so it
         * agrees with the value-preservation heuristic.
         */
        public Optional<Double> getNumericValue() {
            return conversion.getSourceValue();
        }
    }

    /**
     * Build a proposal for one segment, or empty when the source is not a
     * number-only value of any enabled type.
     *
     * @param segmentNumber
     *            the segment number, for display
     * @param source
     *            the segment source text
     * @param sourceLocale
     *            locale the value is written in
     * @param targetLocale
     *            locale to render the value in
     * @param types
     *            the data types to consider
     */
    public static Optional<Proposal> propose(int segmentNumber, String source, Locale sourceLocale,
            Locale targetLocale, Set<DataType> types) {
        return propose(segmentNumber, source, sourceLocale, targetLocale, types, true);
    }

    /**
     * As {@link #propose(int, String, Locale, Locale, Set)}, but with control
     * over Roman-numeral recognition (opt-in when scanning free text).
     */
    public static Optional<Proposal> propose(int segmentNumber, String source, Locale sourceLocale,
            Locale targetLocale, Set<DataType> types, boolean allowRoman) {
        return propose(segmentNumber, source, sourceLocale, targetLocale, types, allowRoman,
                NumberAutoConverter.RenderOptions.DEFAULTS);
    }

    /**
     * As {@link #propose(int, String, Locale, Locale, Set, boolean)}, but with
     * explicit rendering options for the proposal.
     */
    public static Optional<Proposal> propose(int segmentNumber, String source, Locale sourceLocale,
            Locale targetLocale, Set<DataType> types, boolean allowRoman,
            NumberAutoConverter.RenderOptions options) {
        return NumberAutoConverter.convert(source, sourceLocale, targetLocale, types, allowRoman, options)
                .stream().findFirst().map(c -> new Proposal(segmentNumber, source, c));
    }
}
