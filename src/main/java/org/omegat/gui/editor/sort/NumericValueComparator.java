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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.jspecify.annotations.Nullable;
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
public class NumericValueComparator implements TextKeyComparator {

    private final Collator collator;

    /**
     * Numeric keys are parsed once per distinct string, not once per
     * comparison: a sort makes O(n log n) comparisons, and re-parsing on every
     * one of them froze the UI for minutes on large projects. Instances are
     * built per sort pass, so the cache lives exactly as long as one sort.
     */
    private final Map<String, Optional<Rational>> cache = new HashMap<>();

    /** When false, Roman numerals count as plain text, not as numbers. */
    private final boolean includeRoman;

    /** Decimal convention of the sorted text (null: dot-decimal only). */
    private final @Nullable Locale numberLocale;

    public NumericValueComparator(Collator collator) {
        this(collator, true, null);
    }

    public NumericValueComparator(Collator collator, boolean includeRoman) {
        this(collator, includeRoman, null);
    }

    public NumericValueComparator(Collator collator, boolean includeRoman,
            @Nullable Locale numberLocale) {
        this.collator = collator;
        this.includeRoman = includeRoman;
        this.numberLocale = numberLocale;
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

    /**
     * Compute and cache the value for {@code s} without comparing. Used to
     * pre-fill the cache from a background thread before the sort runs; the
     * later sort (on the EDT) then only reads. Callers must ensure the two
     * phases do not overlap (SwingWorker's done() gives that ordering).
     */
    @Override
    public void prime(String s) {
        cachedValue(s);
    }

    private Optional<Rational> cachedValue(String s) {
        return cache.computeIfAbsent(s, this::value);
    }

    private Optional<Rational> value(String s) {
        Optional<Rational> whole = NumeralValueParser.parseValue(s, includeRoman, numberLocale);
        return whole.isPresent() ? whole : NumeralValueParser.firstValue(s, includeRoman, numberLocale);
    }
}
