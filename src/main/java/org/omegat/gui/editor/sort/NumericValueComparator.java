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

import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.omegat.util.NumeralValueParser;
import org.omegat.util.NumeralValueParser.Rational;

/**
 * Orders strings by the exact numeric value of the number they contain. The
 * value is the whole string parsed as a single signed number if possible (so
 * negatives, decimals, fractions and systems with internal marks such as
 * Greek/Hebrew all work), otherwise the first number found in the text
 * ({@link NumeralValueParser}). The value is an exact {@link Rational}, so the
 * comparison is a total, transitive order with no rounding (1/3 orders correctly
 * against any decimal) and can never feed NaN or infinity into the sort. Strings
 * that carry a number sort before strings that do not; equal values (and
 * number-less strings) fall back to the supplied locale-aware {@link Collator}
 * for a stable total order.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class NumericValueComparator implements Comparator<String> {

    private final Collator collator;

    /**
     * Numeric keys are parsed once per distinct string, not once per
     * comparison: a sort makes O(n log n) comparisons, and re-parsing on every
     * one of them froze the UI for minutes on large projects. Instances are
     * built per sort pass, so the cache lives exactly as long as one sort.
     */
    private final Map<String, Optional<Rational>> cache = new HashMap<>();

    public NumericValueComparator(Collator collator) {
        this.collator = collator;
    }

    @Override
    public int compare(String a, String b) {
        Optional<Rational> va = cachedValue(a);
        Optional<Rational> vb = cachedValue(b);
        if (va.isPresent() && vb.isPresent()) {
            int byValue = va.get().compareTo(vb.get());
            return byValue != 0 ? byValue : collator.compare(a, b);
        }
        if (va.isPresent()) {
            return -1;
        }
        if (vb.isPresent()) {
            return 1;
        }
        return collator.compare(a, b);
    }

    private Optional<Rational> cachedValue(String s) {
        return cache.computeIfAbsent(s, NumericValueComparator::value);
    }

    private static Optional<Rational> value(String s) {
        Optional<Rational> whole = NumeralValueParser.parseValue(s);
        return whole.isPresent() ? whole : NumeralValueParser.firstValue(s);
    }
}
