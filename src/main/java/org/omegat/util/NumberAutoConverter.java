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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.DecimalFormatSymbols;
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

    /**
     * How a matched value is rendered into the target. Every choice defaults to
     * mirroring the source, so the defaults reproduce the source's own format.
     */
    public static final class RenderOptions {
        public enum Grouping {
            ORIGINAL, NEVER, ALWAYS
        }

        public enum Fraction {
            ORIGINAL, ZERO, ONE, TWO
        }

        /** Notation style, applied to dates, times and ordinals. */
        public enum Style {
            ORIGINAL, SHORT, MEDIUM, LONG, SPELLOUT
        }

        public static final RenderOptions DEFAULTS =
                new RenderOptions(Grouping.ORIGINAL, Fraction.ORIGINAL, Style.ORIGINAL);

        private final Grouping grouping;
        private final Fraction fraction;
        private final Style style;

        public RenderOptions(Grouping grouping, Fraction fraction, Style style) {
            this.grouping = grouping;
            this.fraction = fraction;
            this.style = style;
        }

        public Grouping getGrouping() {
            return grouping;
        }

        public Fraction getFraction() {
            return fraction;
        }

        public Style getStyle() {
            return style;
        }
    }

    /** One named contribution to a conversion's confidence. */
    public static final class ConfidenceFactor {
        private final String id;
        private final double delta;

        ConfidenceFactor(String id, double delta) {
            this.id = id;
            this.delta = delta;
        }

        /** Stable identifier (a bundle-key suffix), e.g. "INTEGER", "IDENTICAL". */
        public String getId() {
            return id;
        }

        /** Signed contribution to the confidence; the first factor is the base. */
        public double getDelta() {
            return delta;
        }
    }

    /** A single conversion proposal for one data type. */
    public static final class Conversion {
        private final DataType type;
        private final String target;
        private final double confidence;
        private final List<ConfidenceFactor> factors;
        private final Double sourceValue;

        Conversion(DataType type, String target, double confidence) {
            this(type, target, Collections.singletonList(new ConfidenceFactor(type.name(), confidence)), null);
        }

        Conversion(DataType type, String target, List<ConfidenceFactor> factors, Double sourceValue) {
            this.type = type;
            this.target = target;
            this.sourceValue = sourceValue;
            this.factors = Collections.unmodifiableList(new ArrayList<>(factors));
            double sum = 0.0;
            for (ConfidenceFactor f : factors) {
                sum += f.getDelta();
            }
            this.confidence = Math.max(0.0, Math.min(1.0, sum));
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

        /** The named contributions that make up the confidence (base first). */
        public List<ConfidenceFactor> getFactors() {
            return factors;
        }

        /**
         * The numeric value parsed from the source in the source locale, when
         * the data type has one (integer, decimal, percent, currency). Numeric
         * sorting must use this so it agrees with the value heuristics.
         */
        public Optional<Double> getSourceValue() {
            return Optional.ofNullable(sourceValue);
        }

        @Override
        public String toString() {
            return type + "=" + target + " (" + confidence + ")";
        }
    }

    private static Conversion base(DataType type, String target, String baseId, double value) {
        return new Conversion(type, target, Collections.singletonList(new ConfidenceFactor(baseId, value)),
                null);
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
        return convert(source, sourceLocale, targetLocale, types, true);
    }

    /**
     * As {@link #convert(String, Locale, Locale, Set)}, but with control over
     * Roman-numeral recognition.
     *
     * @param allowRoman
     *            when false, a bare Roman numeral is not recognized as an
     *            integer; Roman numerals collide with ordinary Latin words
     *            (CD, MIX, XL), so a caller scanning free text should opt in
     */
    public static List<Conversion> convert(String source, Locale sourceLocale, Locale targetLocale,
            Set<DataType> types, boolean allowRoman) {
        return convert(source, sourceLocale, targetLocale, types, allowRoman, RenderOptions.DEFAULTS);
    }

    /**
     * As {@link #convert(String, Locale, Locale, Set, boolean)}, but with
     * explicit rendering options for the proposal.
     */
    public static List<Conversion> convert(String source, Locale sourceLocale, Locale targetLocale,
            Set<DataType> types, boolean allowRoman, RenderOptions options) {
        List<Conversion> out = new ArrayList<>();
        if (source == null || types == null || types.isEmpty()) {
            return out;
        }
        String cleaned = clean(source);
        if (cleaned.isEmpty()) {
            return out;
        }
        String folded = Normalizer2.getNFKCInstance().normalize(cleaned);
        if (folded.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
            // A dotted-quad IP address is not a number to convert (and would
            // otherwise parse as a grouped integer where '.' groups digits).
            return out;
        }
        ULocale src = ULocale.forLocale(sourceLocale == null ? Locale.ROOT : sourceLocale);
        ULocale tgt = ULocale.forLocale(targetLocale == null ? Locale.ROOT : targetLocale);

        // Ordinals are resolved first: a matched ordinal (for example the
        // German "3.") is not simultaneously offered as a bare integer or
        // decimal, which would be noise.
        Optional<Conversion> ordinal =
                types.contains(DataType.ORDINAL) ? tryOrdinal(cleaned, src, tgt, options) : Optional.empty();
        boolean ordinalWins = ordinal.isPresent();

        for (DataType t : types) {
            Optional<Conversion> c;
            switch (t) {
            case INTEGER:
                c = ordinalWins ? Optional.empty() : tryInteger(folded, tgt, allowRoman, options);
                break;
            case DECIMAL:
                c = ordinalWins ? Optional.empty() : tryDecimal(folded, src, tgt, options);
                break;
            case PERCENT:
                c = tryPercent(folded, src, tgt, options);
                break;
            case CURRENCY:
                c = tryCurrency(folded, src, tgt, options);
                break;
            case DATE:
                c = tryDate(folded, src, tgt, options);
                break;
            case TIME:
                c = tryTime(folded, src, tgt, options);
                break;
            case ORDINAL:
                c = ordinal;
                break;
            default:
                c = Optional.empty();
            }
            c.map(conv -> adjustConfidence(conv, source, folded, src, tgt)).ifPresent(out::add);
        }
        out.sort((a, b) -> Double.compare(b.confidence, a.confidence));
        return out;
    }

    /**
     * Adjust a conversion's confidence with content- and culture-aware
     * heuristics. Safe transformations gain confidence; culturally ambiguous
     * ones lose it. The result is clamped to the range 0 to 1.
     */
    private static Conversion adjustConfidence(Conversion conv, String source, String folded, ULocale src,
            ULocale tgt) {
        String original = source == null ? "" : source.trim();
        boolean identical = conv.getTarget().equals(original);
        List<ConfidenceFactor> factors = new ArrayList<>(conv.getFactors());
        // Identical rendering is a safe no-op.
        if (identical) {
            factors.add(new ConfidenceFactor("IDENTICAL", 0.06));
        } else if (conv.getTarget().length() == original.length()) {
            // Same shape is a mild safety signal; a changed length means the
            // value's presentation really moved.
            factors.add(new ConfidenceFactor("LENGTH_SAME", 0.02));
        } else {
            factors.add(new ConfidenceFactor("LENGTH_DIFF", -0.02));
        }
        // Pure digit-script normalization (full-width / non-Latin -> target) is
        // value-preserving and safe.
        if (hasNonAsciiDigit(original)) {
            factors.add(new ConfidenceFactor("SCRIPT", 0.06));
        }
        // Dropping a leading zero the source had may lose a code or padding.
        if (firstDigitIsZero(original) && !firstDigitIsZero(conv.getTarget())) {
            factors.add(new ConfidenceFactor("LEADING_ZERO", -0.10));
        }
        // Roman refinement: SI/metric abbreviations that also parse as Roman
        // (m, l, mm, cm, c, d, cc, cd) are most likely units; a canonical
        // uppercase numeral containing I, V or X is unambiguously Roman.
        if ("ROMAN".equals(factors.get(0).getId())) {
            if (METRIC_ABBREVIATIONS.contains(original.toLowerCase(Locale.ROOT))) {
                factors.add(new ConfidenceFactor("METRIC_UNIT", -0.02));
            } else if (original.equals(original.toUpperCase(Locale.ROOT))
                    && original.matches("M{0,3}(CM|CD|D?C{0,3})(XC|XL|L?X{0,3})(IX|IV|V?I{0,3})")
                    && original.matches(".*[IVX].*")) {
                factors.add(new ConfidenceFactor("ROMAN_CLEAR", 0.05));
            }
        }
        switch (conv.getType()) {
        case INTEGER:
        case DECIMAL:
            // A leading zero on a quantity is unusual; it hints at a code.
            if (firstDigitIsZero(original)) {
                factors.add(new ConfidenceFactor("LEADING_ZERO_PRESENT", -0.05));
            }
            break;
        default:
            break;
        }
        Double numericSource = null;
        switch (conv.getType()) {
        case INTEGER:
        case DECIMAL:
        case PERCENT:
        case CURRENCY:
            // Compare the numeric value read in the source locale with the one
            // read back from the rendering in the target locale: a preserved
            // value is a strong safety signal, a changed one (lossy rounding,
            // reinterpreted separators) a strong warning.
            Optional<Double> sourceValue = quantityValue(original, src, conv.getType());
            Optional<Double> targetValue = quantityValue(conv.getTarget(), tgt, conv.getType());
            numericSource = sourceValue.orElse(null);
            if (sourceValue.isPresent() && targetValue.isPresent()) {
                double a = sourceValue.get();
                double b = targetValue.get();
                boolean same = Math.abs(a - b) <= 1e-9 * Math.max(1.0, Math.max(Math.abs(a), Math.abs(b)));
                factors.add(same ? new ConfidenceFactor("VALUE_SAME", 0.03)
                        : new ConfidenceFactor("VALUE_DIFF", -0.10));
            }
            // A separator the source locale does not use ("99'999" in de) was
            // accepted leniently; the author may have meant something else.
            if (hasForeignSeparator(folded, src)) {
                factors.add(new ConfidenceFactor("FOREIGN_SEPARATOR", -0.05));
            }
            break;
        default:
            break;
        }
        switch (conv.getType()) {
        case DECIMAL:
            // A single separator with exactly three following digits ("1.234")
            // is maximally ambiguous between decimal and grouping across locales.
            if (folded.matches("\\d{1,3}[.,]\\d{3}")) {
                factors.add(new ConfidenceFactor("AMBIGUOUS_DECIMAL", -0.15));
            }
            if (hasMisplacedGrouping(folded, src)) {
                factors.add(new ConfidenceFactor("MISPLACED_GROUPING", -0.25));
            }
            break;
        case CURRENCY:
            // Symbols shared by many currencies ($, kr, ¥, £) are ambiguous.
            if (hasAmbiguousCurrencySymbol(original)) {
                factors.add(new ConfidenceFactor("CURRENCY_AMBIGUOUS", -0.10));
            }
            if (hasMisplacedGrouping(folded, src)) {
                factors.add(new ConfidenceFactor("MISPLACED_GROUPING", -0.25));
            }
            break;
        case DATE:
            // Day/month order: clear when one component exceeds 12, irrelevant
            // when both are equal (01.01.x), ambiguous otherwise.
            switch (dateOrderClarity(folded)) {
            case 1:
                factors.add(new ConfidenceFactor("DATE_UNAMBIGUOUS", 0.10));
                break;
            case -1:
                factors.add(new ConfidenceFactor("DATE_AMBIGUOUS", -0.10));
                break;
            default:
                break;
            }
            break;
        default:
            break;
        }
        return new Conversion(conv.getType(), conv.getTarget(), factors, numericSource);
    }

    /**
     * A grouping separator that does not sit on proper three-digit groups
     * ("123.45" in a locale where the dot groups thousands) usually means the
     * author used another locale's decimal convention; the parsed value is then
     * probably wrong.
     */
    private static boolean hasMisplacedGrouping(String folded, ULocale src) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(src);
        char grouping = symbols.getGroupingSeparator();
        if (folded.indexOf(grouping) < 0) {
            return false;
        }
        char decimal = symbols.getDecimalSeparator();
        StringBuilder core = new StringBuilder();
        for (char c : folded.toCharArray()) {
            if (Character.isDigit(c) || c == grouping || c == decimal) {
                core.append(c);
            }
        }
        String g = java.util.regex.Pattern.quote(String.valueOf(grouping));
        String d = java.util.regex.Pattern.quote(String.valueOf(decimal));
        return !core.toString().matches("\\d{1,3}(?:" + g + "\\d{3})+(?:" + d + "\\d+)?");
    }

    /**
     * Characters that group or punctuate digits in some locale. Whitespace is
     * excluded: it appears legitimately between value and unit symbol, and
     * space-grouped digit runs are rejected before conversion anyway.
     */
    // . , ' right single quote, modifier apostrophe, middle dot, Arabic
    // decimal and thousands separators
    private static final String SEPARATOR_CANDIDATES = ".,'\u2019\u02BC\u00B7\u066B\u066C";

    /**
     * A separator-like character the source locale uses neither for grouping
     * nor as decimal mark ("99'999" in de) means the author followed another
     * locale's convention; the lenient parse may not match the intent.
     */
    private static boolean hasForeignSeparator(String folded, ULocale src) {
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(src);
        String own = "" + symbols.getGroupingSeparator() + symbols.getDecimalSeparator()
                + symbols.getMonetaryGroupingSeparator() + symbols.getMonetaryDecimalSeparator();
        for (int i = 0; i < folded.length(); i++) {
            char c = folded.charAt(i);
            if (SEPARATOR_CANDIDATES.indexOf(c) >= 0 && own.indexOf(c) < 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * The numeric value of a quantity string read in the given locale with the
     * type-appropriate parser, falling back to the cross-script numeral parser
     * for non-Latin or Roman digits.
     */
    private static Optional<Double> quantityValue(String s, ULocale locale, DataType type) {
        String t = s.trim();
        if (type == DataType.CURRENCY) {
            NumberFormat parser = NumberFormat.getCurrencyInstance(locale);
            parser.setParseStrict(false);
            ParsePosition pp = new ParsePosition(0);
            CurrencyAmount amount = parser.parseCurrency(t, pp);
            if (amount != null && pp.getIndex() == t.length()) {
                return Optional.of(amount.getNumber().doubleValue());
            }
            return Optional.empty();
        }
        NumberFormat parser = type == DataType.PERCENT ? NumberFormat.getPercentInstance(locale)
                : NumberFormat.getNumberInstance(locale);
        parser.setParseStrict(false);
        Number value = parseFull(parser, t);
        if (value != null) {
            return Optional.of(value.doubleValue());
        }
        return NumeralValueParser.parseWhole(Normalizer2.getNFKCInstance().normalize(t))
                .map(BigInteger::doubleValue);
    }

    /** SI/metric abbreviations that would otherwise parse as Roman numerals. */
    private static final Set<String> METRIC_ABBREVIATIONS =
            new HashSet<>(Arrays.asList("m", "l", "mm", "cm", "c", "d", "cc", "cd"));

    private static boolean firstDigitIsZero(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) {
                return s.charAt(i) == '0';
            }
        }
        return false;
    }

    private static boolean hasNonAsciiDigit(String s) {
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            i += Character.charCount(cp);
            if (cp > 0x7F && Character.isDigit(cp)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Day/month order clarity of a numeric date: 1 when a day/month component
     * exceeds 12 (the order is pinned), 0 when day and month candidates are
     * equal (the order does not matter, "01.01.2024"), -1 otherwise. The year
     * component (three or more digits, or the last group when all groups are
     * short) is excluded so "01.01.21" and "01.01.2024" behave alike.
     */
    private static int dateOrderClarity(String folded) {
        List<String> groups = new ArrayList<>();
        for (String group : folded.split("\\D+")) {
            if (!group.isEmpty()) {
                groups.add(group);
            }
        }
        List<Integer> dayMonth = new ArrayList<>();
        int yearIndex = -1;
        for (int i = 0; i < groups.size(); i++) {
            if (groups.get(i).length() >= 3) {
                yearIndex = i;
            }
        }
        if (yearIndex < 0 && groups.size() == 3) {
            yearIndex = groups.size() - 1;
        }
        for (int i = 0; i < groups.size(); i++) {
            if (i != yearIndex && groups.get(i).length() <= 2) {
                try {
                    dayMonth.add(Integer.parseInt(groups.get(i)));
                } catch (NumberFormatException ignore) {
                    // not a small integer group
                }
            }
        }
        if (dayMonth.size() < 2) {
            return 0;
        }
        int a = dayMonth.get(0);
        int b = dayMonth.get(1);
        if (a == b) {
            return 0;
        }
        return (a > 12 || b > 12) ? 1 : -1;
    }

    private static boolean hasAmbiguousCurrencySymbol(String s) {
        return s.indexOf('$') >= 0 || s.indexOf('¥') >= 0 || s.indexOf('£') >= 0
                || s.contains("kr");
    }

    // --- per-type handlers -------------------------------------------------

    private static Optional<Conversion> tryInteger(String s, ULocale tgt, boolean allowRoman,
            RenderOptions opts) {
        // A grouping or decimal separator means this is the DECIMAL type's job.
        if (hasGroupingOrDecimal(s)) {
            return Optional.empty();
        }
        // Phone numbers and identifiers, not quantities: a leading plus (country
        // code), a leading zero, or space-separated digit groups.
        if (s.startsWith("+") || s.indexOf(' ') >= 0 || s.matches("0[0-9]+")) {
            return Optional.empty();
        }
        // Roman is the only ASCII-letter numeral system (ICU also parses odd
        // forms like "N"=0 or "MDN"), and it collides with ordinary Latin words
        // (cm, mm, CD, MIX). Any all-ASCII-letter token is recognized only when
        // the caller opts in; digit and non-Latin numerals (CJK, Arabic-Indic,
        // Ethiopic) are unaffected.
        boolean roman = s.matches("[A-Za-z]+");
        if (!allowRoman && roman) {
            return Optional.empty();
        }
        Optional<BigInteger> value = NumeralValueParser.parseWhole(s);
        if (!value.isPresent()) {
            return Optional.empty();
        }
        NumberFormat renderer = NumberFormat.getIntegerInstance(tgt);
        // A bare digit run has no grouping of its own, so ORIGINAL/NEVER render
        // without grouping and only ALWAYS adds it.
        renderer.setGroupingUsed(groupingFor(opts.getGrouping(), false));
        // Roman stays ambiguous with words even when opted in, so it scores low
        // and is not auto-selected at the default confidence threshold.
        double confidence = roman ? 0.2 : 0.9;
        return Optional.of(base(DataType.INTEGER, renderer.format(value.get()), roman ? "ROMAN" : "INTEGER",
                confidence));
    }

    private static Optional<Conversion> tryDecimal(String s, ULocale src, ULocale tgt, RenderOptions opts) {
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
        int fractionDigits = fractionFor(opts.getFraction(), sourceFractionDigits(s, src));
        renderer.setMinimumFractionDigits(fractionDigits);
        renderer.setMaximumFractionDigits(fractionDigits);
        char grouping = DecimalFormatSymbols.getInstance(src).getGroupingSeparator();
        renderer.setGroupingUsed(groupingFor(opts.getGrouping(), s.indexOf(grouping) >= 0));
        return Optional.of(base(DataType.DECIMAL, renderer.format(value), "DECIMAL", 0.8));
    }

    private static Optional<Conversion> tryPercent(String s, ULocale src, ULocale tgt, RenderOptions opts) {
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
        // Fraction digits follow the option, defaulting to the source's own
        // precision: "100,0 %" stays "100.0%", "50%" stays "50%".
        int fractionDigits = fractionFor(opts.getFraction(), sourceFractionDigits(s, src));
        renderer.setMinimumFractionDigits(fractionDigits);
        renderer.setMaximumFractionDigits(fractionDigits);
        char grouping = DecimalFormatSymbols.getInstance(src).getGroupingSeparator();
        renderer.setGroupingUsed(groupingFor(opts.getGrouping(), s.indexOf(grouping) >= 0));
        return Optional.of(base(DataType.PERCENT, renderer.format(value), "PERCENT", 0.85));
    }

    private static Optional<Conversion> tryCurrency(String s, ULocale src, ULocale tgt, RenderOptions opts) {
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
        int fractionDigits = fractionFor(opts.getFraction(), sourceFractionDigits(s, src));
        renderer.setMinimumFractionDigits(fractionDigits);
        renderer.setMaximumFractionDigits(fractionDigits);
        // Amounts use the monetary grouping separator, which some locales
        // distinguish from the plain numeric one.
        char grouping = DecimalFormatSymbols.getInstance(src).getMonetaryGroupingSeparator();
        renderer.setGroupingUsed(groupingFor(opts.getGrouping(), s.indexOf(grouping) >= 0));
        String rendered = renderer.format(amount.getNumber());
        return Optional.of(base(DataType.CURRENCY, rendered, "CURRENCY", 0.8));
    }

    private static Optional<Conversion> tryDate(String s, ULocale src, ULocale tgt, RenderOptions opts) {
        int renderStyle = dateRenderStyle(opts.getStyle());
        // ISO 8601 first: unambiguous, so it scores highest.
        SimpleDateFormat iso = new SimpleDateFormat("yyyy-MM-dd", ULocale.ROOT);
        iso.setLenient(false);
        Date isoDate = parseFull(iso, s);
        if (isoDate != null) {
            String rendered = DateFormat.getDateInstance(renderStyle, tgt).format(isoDate);
            return Optional.of(base(DataType.DATE, rendered, "DATE", 0.8));
        }
        int[] parseStyles = { DateFormat.MEDIUM, DateFormat.SHORT, DateFormat.LONG };
        for (int parseStyle : parseStyles) {
            DateFormat parser = DateFormat.getDateInstance(parseStyle, src);
            parser.setLenient(false);
            Date value = parseFull(parser, s);
            if (value != null) {
                String rendered = DateFormat.getDateInstance(renderStyle, tgt).format(value);
                // Localized numeric dates are inherently ambiguous (day/month).
                return Optional.of(base(DataType.DATE, rendered, "DATE", 0.55));
            }
        }
        return Optional.empty();
    }

    private static Optional<Conversion> tryTime(String s, ULocale src, ULocale tgt, RenderOptions opts) {
        int[] styles = { DateFormat.SHORT, DateFormat.MEDIUM };
        for (int style : styles) {
            DateFormat parser = DateFormat.getTimeInstance(style, src);
            parser.setLenient(false);
            Date value = parseFull(parser, s);
            if (value != null) {
                int renderStyle;
                if (opts.getStyle() == RenderOptions.Style.ORIGINAL) {
                    // Keep the source's precision: seconds in, seconds out.
                    boolean hasSeconds = s.chars().filter(c -> c == ':').count() >= 2;
                    renderStyle = hasSeconds ? DateFormat.MEDIUM : DateFormat.SHORT;
                } else {
                    renderStyle = dateRenderStyle(opts.getStyle());
                }
                String rendered = DateFormat.getTimeInstance(renderStyle, tgt).format(value);
                return Optional.of(base(DataType.TIME, rendered, "TIME", 0.7));
            }
        }
        return Optional.empty();
    }

    /**
     * Count the digits after the source locale's decimal separator, so a
     * conversion can reproduce the source's own precision.
     */
    private static int sourceFractionDigits(String s, ULocale locale) {
        char decimal = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
        int at = s.lastIndexOf(decimal);
        if (at < 0) {
            return 0;
        }
        int digits = 0;
        for (int i = at + 1; i < s.length() && Character.isDigit(s.charAt(i)); i++) {
            digits++;
        }
        return digits;
    }

    private static int fractionFor(RenderOptions.Fraction option, int sourceDigits) {
        switch (option) {
        case ZERO:
            return 0;
        case ONE:
            return 1;
        case TWO:
            return 2;
        case ORIGINAL:
        default:
            return sourceDigits;
        }
    }

    private static boolean groupingFor(RenderOptions.Grouping option, boolean sourceHasGrouping) {
        switch (option) {
        case NEVER:
            return false;
        case ALWAYS:
            return true;
        case ORIGINAL:
        default:
            return sourceHasGrouping;
        }
    }

    private static int dateRenderStyle(RenderOptions.Style style) {
        switch (style) {
        case SHORT:
            return DateFormat.SHORT;
        case LONG:
            return DateFormat.LONG;
        case SPELLOUT:
            return DateFormat.FULL;
        case MEDIUM:
        case ORIGINAL:
        default:
            return DateFormat.MEDIUM;
        }
    }

    private static Optional<Conversion> tryOrdinal(String s, ULocale src, ULocale tgt, RenderOptions opts) {
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
        String rendered = null;
        if (opts.getStyle() == RenderOptions.Style.SPELLOUT) {
            // Spelled-out ordinal ("third") where the target locale has the
            // rule set; otherwise fall back to the digit ordinal below.
            RuleBasedNumberFormat spell = new RuleBasedNumberFormat(tgt, RuleBasedNumberFormat.SPELLOUT);
            for (String ruleSet : spell.getRuleSetNames()) {
                if ("%spellout-ordinal".equals(ruleSet)) {
                    spell.setDefaultRuleSet(ruleSet);
                    rendered = spell.format(value.longValue());
                    break;
                }
            }
        }
        if (rendered == null) {
            rendered = new RuleBasedNumberFormat(tgt, RuleBasedNumberFormat.ORDINAL)
                    .format(value.longValue());
        }
        return Optional.of(base(DataType.ORDINAL, rendered, "ORDINAL", 0.75));
    }

    // --- helpers -----------------------------------------------------------

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
