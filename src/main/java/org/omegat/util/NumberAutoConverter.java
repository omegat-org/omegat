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

package org.omegat.util;

import java.math.BigInteger;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.util.CurrencyAmount;
import com.ibm.icu.util.ULocale;

/**
 * Locale-aware conversion of number-only segments for feature request #794.
 *
 * A number-only segment is a segment whose whole source text is a single value
 * of some data type (an integer in any writing system, a decimal, a percent, a
 * currency amount, a date, a time or an ordinal) with no surrounding prose. For
 * such a segment this converter parses the value in the source locale and
 * re-renders it in the target locale, so that for example a German amount
 * "1.234,56 &#8364;" becomes the English "&#8364;1,234.56".
 *
 * Recognition is driven by full consumption: a data type is only reported when
 * a strict parser consumes the entire cleaned source. A segment that contains
 * surrounding text therefore yields no conversion and is out of scope, which is
 * exactly the ticket's "number only" restriction. Cross-writing-system integer
 * recognition (Roman, CJK, Arabic-Indic, Ethiopic and so on) is delegated to
 * {@link NumeralValueParser}; ordinals are recognized locale by locale through
 * ICU's rule-based ordinal formatter, so "3." is an ordinal in German but not
 * in English.
 *
 * The source is first cleaned of formatting control characters (bidirectional
 * marks, zero-width and no-break spaces). Numeric types additionally see an
 * NFKC-folded form so that full-width digits and percent signs are handled, but
 * the ordinal path deliberately uses the unfolded form because NFKC would
 * destroy the Spanish and Italian ordinal indicators.
 *
 * Each reported {@link Conversion} carries a confidence in the range 0 to 1 so
 * that a caller can offer a minimum-confidence threshold. Ambiguous types such
 * as a localized numeric date score lower than an unambiguous ISO date.
 *
 * The class is stateless; the ICU formatters, which are not thread-safe, are
 * created per call and never shared.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class NumberAutoConverter {

    private NumberAutoConverter() {
    }

    /** Data types that a number-only segment may hold. */
    public enum DataType {
        INTEGER, DECIMAL, PERCENT, CURRENCY, DATE, TIME, ORDINAL
    }

    /** A single conversion proposal for one data type. */
    public static final class Conversion {
        private final DataType type;
        private final String target;
        private final double confidence;

        Conversion(DataType type, String target, double confidence) {
            this.type = type;
            this.target = target;
            this.confidence = confidence;
        }

        public DataType getType() {
            return type;
        }

        /** The source value re-rendered in the target locale. */
        public String getTarget() {
            return target;
        }

        /** Confidence in the range 0 to 1. */
        public double getConfidence() {
            return confidence;
        }

        @Override
        public String toString() {
            return type + "=" + target + " (" + confidence + ")";
        }
    }

    /**
     * Convert with every data type enabled.
     */
    public static List<Conversion> convert(String source, Locale sourceLocale, Locale targetLocale) {
        return convert(source, sourceLocale, targetLocale, EnumSet.allOf(DataType.class));
    }

    /**
     * Detect and convert the given source segment.
     *
     * @param source
     *            the segment source text
     * @param sourceLocale
     *            the locale the value is written in
     * @param targetLocale
     *            the locale to render the value in
     * @param types
     *            the data types to consider
     * @return every matching conversion, highest confidence first; empty when
     *         the source is not a number-only value of any enabled type
     */
    public static List<Conversion> convert(String source, Locale sourceLocale, Locale targetLocale,
            Set<DataType> types) {
        List<Conversion> out = new ArrayList<>();
        if (source == null || types == null || types.isEmpty()) {
            return out;
        }
        String cleaned = clean(source);
        if (cleaned.isEmpty()) {
            return out;
        }
        String folded = Normalizer2.getNFKCInstance().normalize(cleaned);
        ULocale src = ULocale.forLocale(sourceLocale == null ? Locale.ROOT : sourceLocale);
        ULocale tgt = ULocale.forLocale(targetLocale == null ? Locale.ROOT : targetLocale);

        // Ordinals are resolved first: a matched ordinal (for example the
        // German "3.") is not simultaneously offered as a bare integer or
        // decimal, which would be noise.
        Optional<Conversion> ordinal =
                types.contains(DataType.ORDINAL) ? tryOrdinal(cleaned, src, tgt) : Optional.empty();
        boolean ordinalWins = ordinal.isPresent();

        for (DataType t : types) {
            Optional<Conversion> c;
            switch (t) {
            case INTEGER:
                c = ordinalWins ? Optional.empty() : tryInteger(folded, tgt);
                break;
            case DECIMAL:
                c = ordinalWins ? Optional.empty() : tryDecimal(folded, src, tgt);
                break;
            case PERCENT:
                c = tryPercent(folded, src, tgt);
                break;
            case CURRENCY:
                c = tryCurrency(folded, src, tgt);
                break;
            case DATE:
                c = tryDate(folded, src, tgt);
                break;
            case TIME:
                c = tryTime(folded, src, tgt);
                break;
            case ORDINAL:
                c = ordinal;
                break;
            default:
                c = Optional.empty();
            }
            c.ifPresent(out::add);
        }
        out.sort((a, b) -> Double.compare(b.confidence, a.confidence));
        return out;
    }

    // --- per-type handlers -------------------------------------------------

    private static Optional<Conversion> tryInteger(String s, ULocale tgt) {
        // A grouping or decimal separator means this is the DECIMAL type's job.
        if (hasGroupingOrDecimal(s)) {
            return Optional.empty();
        }
        Optional<BigInteger> value = NumeralValueParser.parseWhole(s);
        if (!value.isPresent()) {
            return Optional.empty();
        }
        String rendered = NumberFormat.getIntegerInstance(tgt).format(value.get());
        return Optional.of(new Conversion(DataType.INTEGER, rendered, 0.9));
    }

    private static Optional<Conversion> tryDecimal(String s, ULocale src, ULocale tgt) {
        if (!hasGroupingOrDecimal(s)) {
            return Optional.empty();
        }
        NumberFormat parser = NumberFormat.getNumberInstance(src);
        parser.setParseStrict(true);
        Number value = parseFull(parser, s);
        if (value == null) {
            return Optional.empty();
        }
        NumberFormat renderer = NumberFormat.getNumberInstance(tgt);
        renderer.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
        return Optional.of(new Conversion(DataType.DECIMAL, renderer.format(value), 0.8));
    }

    private static Optional<Conversion> tryPercent(String s, ULocale src, ULocale tgt) {
        // ASCII, full-width and Arabic percent signs.
        if (s.indexOf('%') < 0 && s.indexOf('٪') < 0) {
            return Optional.empty();
        }
        // Lenient: the required percent sign already guards against false
        // positives, and leniency tolerates locale spacing variants.
        NumberFormat parser = NumberFormat.getPercentInstance(src);
        parser.setParseStrict(false);
        Number value = parseFull(parser, s);
        if (value == null) {
            return Optional.empty();
        }
        NumberFormat renderer = NumberFormat.getPercentInstance(tgt);
        renderer.setMinimumFractionDigits(0);
        renderer.setMaximumFractionDigits(MAX_FRACTION_DIGITS);
        return Optional.of(new Conversion(DataType.PERCENT, renderer.format(value), 0.85));
    }

    private static Optional<Conversion> tryCurrency(String s, ULocale src, ULocale tgt) {
        // Require a currency symbol so a bare number is never lenient-parsed as
        // an amount in the locale's default currency.
        if (!hasCurrencySymbol(s)) {
            return Optional.empty();
        }
        // Lenient parsing then tolerates locale spacing between symbol and value.
        NumberFormat parser = NumberFormat.getCurrencyInstance(src);
        parser.setParseStrict(false);
        ParsePosition pp = new ParsePosition(0);
        CurrencyAmount amount = parser.parseCurrency(s, pp);
        if (amount == null || pp.getIndex() != s.length()) {
            return Optional.empty();
        }
        NumberFormat renderer = NumberFormat.getCurrencyInstance(tgt);
        renderer.setCurrency(amount.getCurrency());
        String rendered = renderer.format(amount.getNumber());
        return Optional.of(new Conversion(DataType.CURRENCY, rendered, 0.8));
    }

    private static Optional<Conversion> tryDate(String s, ULocale src, ULocale tgt) {
        // ISO 8601 first: unambiguous, so it scores highest.
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", ULocale.ROOT);
        iso.setLenient(false);
        Date isoDate = parseFull(iso, s);
        if (isoDate != null) {
            String rendered = DateFormat.getDateInstance(DateFormat.MEDIUM, tgt).format(isoDate);
            return Optional.of(new Conversion(DataType.DATE, rendered, 0.8));
        }
        int[] styles = { DateFormat.MEDIUM, DateFormat.SHORT, DateFormat.LONG };
        for (int style : styles) {
            DateFormat parser = DateFormat.getDateInstance(style, src);
            parser.setLenient(false);
            Date value = parseFull(parser, s);
            if (value != null) {
                String rendered = DateFormat.getDateInstance(DateFormat.MEDIUM, tgt).format(value);
                // Localized numeric dates are inherently ambiguous (day/month).
                return Optional.of(new Conversion(DataType.DATE, rendered, 0.55));
            }
        }
        return Optional.empty();
    }

    private static Optional<Conversion> tryTime(String s, ULocale src, ULocale tgt) {
        int[] styles = { DateFormat.SHORT, DateFormat.MEDIUM };
        for (int style : styles) {
            DateFormat parser = DateFormat.getTimeInstance(style, src);
            parser.setLenient(false);
            Date value = parseFull(parser, s);
            if (value != null) {
                String rendered = DateFormat.getTimeInstance(DateFormat.SHORT, tgt).format(value);
                return Optional.of(new Conversion(DataType.TIME, rendered, 0.7));
            }
        }
        return Optional.empty();
    }

    private static Optional<Conversion> tryOrdinal(String s, ULocale src, ULocale tgt) {
        // A bare digit run is a cardinal integer, not an ordinal. An ordinal
        // must carry a locale marker (a trailing dot, a letter suffix, a
        // leading CJK marker), so it is never a pure digit sequence.
        if (isBareDigitRun(s)) {
            return Optional.empty();
        }
        RuleBasedNumberFormat parser = new RuleBasedNumberFormat(src, RuleBasedNumberFormat.ORDINAL);
        ParsePosition pp = new ParsePosition(0);
        Number value = parser.parse(s, pp);
        // value < 1 guards against locales whose ICU ordinal rules parse a
        // non-zero input to 0 while consuming it (Italian, for example);
        // "0th" is never a wanted result.
        if (value == null || pp.getIndex() != s.length() || value.longValue() < 1) {
            return Optional.empty();
        }
        RuleBasedNumberFormat renderer = new RuleBasedNumberFormat(tgt, RuleBasedNumberFormat.ORDINAL);
        return Optional.of(new Conversion(DataType.ORDINAL, renderer.format(value.longValue()), 0.75));
    }

    // --- helpers -----------------------------------------------------------

    private static final int MAX_FRACTION_DIGITS = 6;

    /**
     * Strip formatting control characters (bidirectional marks, zero-width and
     * no-break spaces), fold every whitespace variant to a plain space and trim.
     * Compatibility folding is deliberately not done here so that the ordinal
     * path can still see the Spanish and Italian ordinal indicators.
     */
    private static String clean(String s) {
        StringBuilder b = new StringBuilder(s.length());
        s.codePoints().forEach(cp -> {
            if (Character.getType(cp) == Character.FORMAT) {
                return;
            }
            if (Character.isSpaceChar(cp) || Character.isWhitespace(cp)) {
                b.append(' ');
            } else {
                b.appendCodePoint(cp);
            }
        });
        return b.toString().strip();
    }

    private static boolean hasGroupingOrDecimal(String s) {
        return s.indexOf('.') >= 0 || s.indexOf(',') >= 0;
    }

    private static boolean hasCurrencySymbol(String s) {
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            if (Character.getType(cp) == Character.CURRENCY_SYMBOL) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBareDigitRun(String s) {
        boolean sawDigit = false;
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            if (Character.isDigit(cp)) {
                sawDigit = true;
            } else if (cp != '+' && cp != '-' && cp != ' ') {
                return false;
            }
        }
        return sawDigit;
    }

    private static Number parseFull(NumberFormat format, String s) {
        ParsePosition pp = new ParsePosition(0);
        Number value = format.parse(s, pp);
        return (value != null && pp.getIndex() == s.length()) ? value : null;
    }

    private static Date parseFull(DateFormat format, String s) {
        ParsePosition pp = new ParsePosition(0);
        Date value = format.parse(s, pp);
        return (value != null && pp.getIndex() == s.length()) ? value : null;
    }
}
