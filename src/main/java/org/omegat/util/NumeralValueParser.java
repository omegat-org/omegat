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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormatSymbols;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.RuleBasedNumberFormat;
import com.ibm.icu.util.ULocale;

/**
 * Parses textual numerals of many writing and numbering systems to their
 * integer value.
 *
 * Decimal positional digits of every script are handled directly through their
 * Unicode value ({@link Character#digit}), so Western, Arabic-Indic, Devanagari,
 * Thai, fullwidth and astral-plane decimal digits all work. Non-decimal
 * algorithmic systems are parsed with ICU's {@link RuleBasedNumberFormat}:
 * Roman, Chinese/Japanese ideographic, Ethiopic, Armenian, Greek, Hebrew,
 * Tamil, Georgian and Cyrillic. Inputs are NFKC-normalized first, so single
 * code point forms such as the Roman numeral for twelve at U+216B and fullwidth
 * digits
 * are recognized too.
 *
 * A value is only returned when a candidate system consumes the WHOLE (trimmed)
 * string, so partial or ambiguous input is rejected and the caller can fall
 * back to plain text ordering. Kaktovik/Inuktitut numerals are not available in
 * the bundled ICU version and are therefore not recognized.
 *
 * On top of the integer core, {@link #parseValue} and {@link #firstValue}
 * expose signed real numbers as an exact reduced {@link Rational}
 * (numerator/denominator of arbitrary-precision integers). They recognize a
 * leading sign (ASCII hyphen-minus, Unicode minus U+2212, or plus), a single
 * decimal point, fractions written with an ASCII or a Unicode fraction slash
 * ("a/b"), mixed numbers ("1 1/2"), and the Unicode vulgar fractions.
 * A rational is exact for every one of those inputs, so comparison by
 * cross-multiplication is a provably total, transitive order with no rounding,
 * and values such as 1/3 order correctly against any terminating decimal.
 * A comma is only treated as a decimal separator when the caller names a
 * comma-decimal locale (see {@link #parseValue(String, boolean, Locale)}); the
 * convention is irreducibly locale-ambiguous, so without a locale "1,000" and
 * "1,5" contribute only their leading integer. Division by zero yields no
 * value.
 *
 * As a final step, a single code point that carries a Unicode numeric value but
 * is neither a decimal digit nor an algorithmic numeral is resolved from its
 * value: enclosed and parenthesized numbers and many
 * historic script numerals (Aegean, cuneiform, Aramaic, Greek acrophonic ...).
 * Only single, non-negative integer values are taken; multi-sign additive
 * sequences are not composed. Symbols without a numeric value (such as the emoji
 * for a hundred points or the keycap ten) are correctly ignored.
 *
 * The class is stateless from the caller's perspective; the (non-thread-safe)
 * ICU formatters are cached per thread.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class NumeralValueParser {

    private NumeralValueParser() {
    }

    /** One ICU rule set to try, in priority order. */
    private record RuleSpec(ULocale locale, int type, String ruleSet) {
    }

    private static final List<RuleSpec> SPECS = List.of(
            new RuleSpec(ULocale.ROOT, RuleBasedNumberFormat.NUMBERING_SYSTEM, "%roman-upper"),
            new RuleSpec(ULocale.ROOT, RuleBasedNumberFormat.NUMBERING_SYSTEM, "%roman-lower"),
            new RuleSpec(ULocale.ROOT, RuleBasedNumberFormat.NUMBERING_SYSTEM, "%ethiopic"),
            new RuleSpec(ULocale.ROOT, RuleBasedNumberFormat.NUMBERING_SYSTEM, "%armenian-upper"),
            new RuleSpec(ULocale.ROOT, RuleBasedNumberFormat.NUMBERING_SYSTEM, "%armenian-lower"),
            new RuleSpec(ULocale.ROOT, RuleBasedNumberFormat.NUMBERING_SYSTEM, "%greek-upper"),
            new RuleSpec(ULocale.ROOT, RuleBasedNumberFormat.NUMBERING_SYSTEM, "%greek-lower"),
            new RuleSpec(ULocale.ROOT, RuleBasedNumberFormat.NUMBERING_SYSTEM, "%hebrew"),
            new RuleSpec(ULocale.ROOT, RuleBasedNumberFormat.NUMBERING_SYSTEM, "%tamil"),
            new RuleSpec(ULocale.ROOT, RuleBasedNumberFormat.NUMBERING_SYSTEM, "%georgian"),
            new RuleSpec(ULocale.ROOT, RuleBasedNumberFormat.NUMBERING_SYSTEM, "%cyrillic-lower"),
            new RuleSpec(ULocale.forLanguageTag("zh"), RuleBasedNumberFormat.SPELLOUT, "%spellout-numbering"),
            new RuleSpec(ULocale.forLanguageTag("zh-Hant"), RuleBasedNumberFormat.SPELLOUT,
                    "%spellout-numbering"),
            new RuleSpec(ULocale.forLanguageTag("ja"), RuleBasedNumberFormat.SPELLOUT, "%spellout-numbering"));

    /** RuleBasedNumberFormat is not thread-safe, so build one set of parsers per thread. */
    private static final ThreadLocal<List<RuleBasedNumberFormat>> PARSERS = ThreadLocal
            .withInitial(() -> buildParsers(true));

    /**
     * The same parsers minus the Roman rule sets, for callers that must not
     * read ordinary Latin words ("I", "mix", "XL") as numerals.
     */
    private static final ThreadLocal<List<RuleBasedNumberFormat>> PARSERS_NO_ROMAN = ThreadLocal
            .withInitial(() -> buildParsers(false));

    private static List<RuleBasedNumberFormat> buildParsers(boolean includeRoman) {
        List<RuleBasedNumberFormat> list = new ArrayList<>();
        for (RuleSpec s : SPECS) {
            if (!includeRoman && s.ruleSet().startsWith("%roman")) {
                continue;
            }
            try {
                RuleBasedNumberFormat f = new RuleBasedNumberFormat(s.locale(), s.type());
                f.setDefaultRuleSet(s.ruleSet());
                f.setLenientParseMode(false);
                list.add(f);
            } catch (RuntimeException ignore) {
                // Rule set not available in this ICU build; skip it.
            }
        }
        return list;
    }

    private static final Normalizer2 NFKC = Normalizer2.getNFKCInstance();

    /**
     * The integer value if the whole (trimmed) string is a single numeral of a
     * supported system, otherwise empty. Decimal digits of any script and the
     * ICU algorithmic systems are recognized.
     */
    public static Optional<BigInteger> parseWhole(String s) {
        return parseWhole(s, true);
    }

    /**
     * Like {@link #parseWhole(String)}, but with {@code includeRoman} false the
     * Roman system is not recognized: Latin letters (and the single-codepoint
     * Roman number forms) never count as numerals then.
     */
    public static Optional<BigInteger> parseWhole(String s, boolean includeRoman) {
        if (s == null) {
            return Optional.empty();
        }
        String trimmed = s.trim();
        if (trimmed.isEmpty()) {
            return Optional.empty();
        }
        // Try the text as-is first. NFKC would alter the numeral marks of some
        // systems (Greek/Hebrew/Armenian numeral signs), so raw parsing must be
        // attempted before normalization.
        Optional<BigInteger> value = parseExact(trimmed, includeRoman);
        if (value.isPresent()) {
            return value;
        }
        // Then try NFKC-normalized, which folds single-codepoint Roman numerals
        // (U+216B becomes XII), fullwidth digits, etc. into a parseable form.
        String norm = NFKC.normalize(trimmed);
        if (!norm.equals(trimmed)) {
            value = parseExact(norm, includeRoman);
        }
        return value;
    }

    /** Parse the exact string (no trimming/normalization) as decimal or one ICU system. */
    private static Optional<BigInteger> parseExact(String s, boolean includeRoman) {
        Optional<BigInteger> decimal = allDecimalDigits(s);
        if (decimal.isPresent()) {
            return decimal;
        }
        if (!mayBeAlgorithmicNumeral(s, includeRoman)) {
            return Optional.empty();
        }
        for (RuleBasedNumberFormat parser : (includeRoman ? PARSERS : PARSERS_NO_ROMAN).get()) {
            ParsePosition pp = new ParsePosition(0);
            Number value;
            try {
                value = parser.parse(s, pp);
            } catch (RuntimeException ex) {
                continue;
            }
            if (value != null && pp.getIndex() == s.length()) {
                double d = value.doubleValue();
                if (Double.isNaN(d) || Double.isInfinite(d)) {
                    continue; // ICU parses "NaN"/"Infinity"; those are not numerals
                }
                return Optional.of(BigInteger.valueOf(value.longValue()));
            }
        }
        return Optional.empty();
    }

    private static final String ROMAN_DIGITS = "IVXLCDMivxlcdm";

    /**
     * Digit-group and decimal punctuation accepted by the rule-based parsers'
     * loose number matching: period, comma, apostrophe, right single quote,
     * modifier apostrophe, middle dot, Arabic decimal and thousands
     * separators. "99'999" or "1.234" must reach the parsers (they read such
     * groupings), but these characters alone make no numeral.
     */
    private static final String SEPARATOR_CHARS = ".,'’ʼ·٫٬";

    /**
     * Cheap pre-filter for the rule-based parse attempts: only a string whose
     * every code point can occur in one of the supported algorithmic numeral
     * systems is worth handing to the ICU parsers. Ordinary prose tokens (the
     * overwhelming majority of real inputs, and in Latin scripts almost every
     * word) are rejected here instead of failing through all rule sets, which
     * is orders of magnitude more expensive.
     */
    private static boolean mayBeAlgorithmicNumeral(String s, boolean includeRoman) {
        boolean sawNumeral = false;
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            if (SEPARATOR_CHARS.indexOf(cp) < 0) {
                if (!isNumeralPlausible(cp, includeRoman)) {
                    return false;
                }
                sawNumeral = true;
            }
            i += Character.charCount(cp);
        }
        // Pure punctuation ("...") is not worth the parse attempts.
        return sawNumeral;
    }

    private static boolean isNumeralPlausible(int cp, boolean includeRoman) {
        if (!includeRoman && Character.UnicodeScript.of(cp) == Character.UnicodeScript.LATIN) {
            // Covers both the letters of the Roman system (I, V, X ...) and the
            // single-codepoint Roman number forms (Ⅻ), whose script is Latin.
            return false;
        }
        int type = Character.getType(cp);
        if (type == Character.DECIMAL_DIGIT_NUMBER || type == Character.LETTER_NUMBER
                || type == Character.OTHER_NUMBER) {
            return true;
        }
        if (cp == 0x02B9 || cp == 0x0374 || cp == 0x0375 || cp == 0x00B4) {
            // Greek numeral marks (keraia in its various forms, and the acute
            // accent ICU emits for it); these have script Common.
            return true;
        }
        switch (Character.UnicodeScript.of(cp)) {
        case LATIN:
            // Latin letters form numerals only in the Roman system.
            return ROMAN_DIGITS.indexOf(cp) >= 0;
        case INHERITED:
            // Combining marks (e.g. the Cyrillic titlo) inherit their script.
            return true;
        case GREEK:
        case HEBREW:
        case ARMENIAN:
        case ETHIOPIC:
        case TAMIL:
        case GEORGIAN:
        case CYRILLIC:
        case HAN:
            return true;
        default:
            return false;
        }
    }

    /**
     * The value of the FIRST number occurring in the text, or empty if none.
     * Decimal digit runs are recognized anywhere (even inside a word, e.g.
     * "item2"); numerals of the algorithmic systems are only recognized when
     * they form a whole token delimited by whitespace/punctuation, so ordinary
     * words are not misread as (for example) Roman numerals.
     */
    public static Optional<BigInteger> firstNumber(String text) {
        return firstNumber(text, true);
    }

    /** Like {@link #firstNumber(String)}, optionally without the Roman system. */
    public static Optional<BigInteger> firstNumber(String text, boolean includeRoman) {
        if (text == null || text.isEmpty()) {
            return Optional.empty();
        }
        int i = 0;
        int n = text.length();
        while (i < n) {
            int cp = text.codePointAt(i);
            if (isSeparator(cp)) {
                i += Character.charCount(cp);
                continue;
            }
            int start = i;
            while (i < n) {
                int c = text.codePointAt(i);
                if (isSeparator(c)) {
                    break;
                }
                i += Character.charCount(c);
            }
            Optional<BigInteger> value = tokenNumber(text.substring(start, i), includeRoman);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    /** A number for this token: its first decimal-digit run, else a whole-token algorithmic numeral. */
    private static Optional<BigInteger> tokenNumber(String token, boolean includeRoman) {
        Optional<BigInteger> decimalRun = firstDecimalRun(token);
        if (decimalRun.isPresent()) {
            return decimalRun;
        }
        return parseWhole(token, includeRoman);
    }

    private static Optional<BigInteger> allDecimalDigits(String s) {
        BigInteger value = BigInteger.ZERO;
        boolean any = false;
        int i = 0;
        int n = s.length();
        while (i < n) {
            int cp = s.codePointAt(i);
            int d = Character.digit(cp, 10);
            if (d < 0) {
                return Optional.empty();
            }
            value = value.multiply(BigInteger.TEN).add(BigInteger.valueOf(d));
            any = true;
            i += Character.charCount(cp);
        }
        return any ? Optional.of(value) : Optional.empty();
    }

    private static Optional<BigInteger> firstDecimalRun(String s) {
        int i = 0;
        int n = s.length();
        while (i < n) {
            int cp = s.codePointAt(i);
            if (Character.digit(cp, 10) >= 0) {
                BigInteger value = BigInteger.ZERO;
                while (i < n) {
                    int c = s.codePointAt(i);
                    int d = Character.digit(c, 10);
                    if (d < 0) {
                        break;
                    }
                    value = value.multiply(BigInteger.TEN).add(BigInteger.valueOf(d));
                    i += Character.charCount(c);
                }
                return Optional.of(value);
            }
            i += Character.charCount(cp);
        }
        return Optional.empty();
    }

    /**
     * Whitespace and punctuation delimit tokens. Letters (including Roman
     * numeral letters and CJK ideographs), decimal digits and the number
     * categories (No/Nl, e.g. the Roman numeral code points, Ethiopic digits)
     * stay inside a token so they can be parsed as numerals.
     */
    private static boolean isSeparator(int cp) {
        if (Character.isWhitespace(cp)) {
            return true;
        }
        switch (Character.getType(cp)) {
            case Character.CONNECTOR_PUNCTUATION:
            case Character.DASH_PUNCTUATION:
            case Character.START_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.INITIAL_QUOTE_PUNCTUATION:
            case Character.FINAL_QUOTE_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
            case Character.MATH_SYMBOL:
                return true;
            default:
                return false;
        }
    }

    // ------------------------------------------------------------------------
    // Real values (signed decimals, fractions, mixed numbers, vulgar fractions)
    // ------------------------------------------------------------------------

    /**
     * An exact, reduced rational value: {@code num / den} with {@code den > 0}
     * and {@code gcd(|num|, den) == 1}. Because both denominators are always
     * positive, comparing two rationals by cross-multiplication
     * ({@code num1 * den2} vs {@code num2 * den1}) is a total, transitive order
     * with no rounding, so 1/3 orders correctly against any terminating decimal.
     * The canonical zero is {@code 0/1}, so {@code -0}, {@code 0/5} and the "zero
     * thirds" glyph all compare equal to {@code 0}.
     */
    public static final class Rational implements Comparable<Rational> {

        private final BigInteger num;
        private final BigInteger den;

        private Rational(BigInteger num, BigInteger den) {
            this.num = num;
            this.den = den;
        }

        /** Reduced rational for num/den; the caller must ensure den != 0. */
        static Rational of(BigInteger num, BigInteger den) {
            if (den.signum() < 0) {
                num = num.negate();
                den = den.negate();
            }
            BigInteger g = num.gcd(den); // non-negative; gcd(0, d) == d
            if (g.signum() != 0 && !g.equals(BigInteger.ONE)) {
                num = num.divide(g);
                den = den.divide(g);
            }
            return new Rational(num, den);
        }

        /** Reduced rational for an integer value. */
        static Rational ofInteger(BigInteger value) {
            return new Rational(value, BigInteger.ONE);
        }

        Rational negate() {
            return new Rational(num.negate(), den);
        }

        public BigInteger numerator() {
            return num;
        }

        public BigInteger denominator() {
            return den;
        }

        @Override
        public int compareTo(Rational o) {
            return num.multiply(o.den).compareTo(o.num.multiply(den));
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Rational r && num.equals(r.num) && den.equals(r.den);
        }

        @Override
        public int hashCode() {
            return 31 * num.hashCode() + den.hashCode();
        }

        @Override
        public String toString() {
            return den.equals(BigInteger.ONE) ? num.toString() : num + "/" + den;
        }
    }

    /** Unicode vulgar fractions (Number, Other), mapped to their exact value. */
    private static final Map<Integer, Rational> VULGAR = Map.ofEntries(
            Map.entry(0x00BC, Rational.of(BigInteger.ONE, BigInteger.valueOf(4))),   // 1/4
            Map.entry(0x00BD, Rational.of(BigInteger.ONE, BigInteger.TWO)),          // 1/2
            Map.entry(0x00BE, Rational.of(BigInteger.valueOf(3), BigInteger.valueOf(4))),   // 3/4
            Map.entry(0x2150, Rational.of(BigInteger.ONE, BigInteger.valueOf(7))),   // 1/7
            Map.entry(0x2151, Rational.of(BigInteger.ONE, BigInteger.valueOf(9))),   // 1/9
            Map.entry(0x2152, Rational.of(BigInteger.ONE, BigInteger.TEN)),          // 1/10
            Map.entry(0x2153, Rational.of(BigInteger.ONE, BigInteger.valueOf(3))),   // 1/3
            Map.entry(0x2154, Rational.of(BigInteger.TWO, BigInteger.valueOf(3))),   // 2/3
            Map.entry(0x2155, Rational.of(BigInteger.ONE, BigInteger.valueOf(5))),   // 1/5
            Map.entry(0x2156, Rational.of(BigInteger.TWO, BigInteger.valueOf(5))),   // 2/5
            Map.entry(0x2157, Rational.of(BigInteger.valueOf(3), BigInteger.valueOf(5))),   // 3/5
            Map.entry(0x2158, Rational.of(BigInteger.valueOf(4), BigInteger.valueOf(5))),   // 4/5
            Map.entry(0x2159, Rational.of(BigInteger.ONE, BigInteger.valueOf(6))),   // 1/6
            Map.entry(0x215A, Rational.of(BigInteger.valueOf(5), BigInteger.valueOf(6))),   // 5/6
            Map.entry(0x215B, Rational.of(BigInteger.ONE, BigInteger.valueOf(8))),   // 1/8
            Map.entry(0x215C, Rational.of(BigInteger.valueOf(3), BigInteger.valueOf(8))),   // 3/8
            Map.entry(0x215D, Rational.of(BigInteger.valueOf(5), BigInteger.valueOf(8))),   // 5/8
            Map.entry(0x215E, Rational.of(BigInteger.valueOf(7), BigInteger.valueOf(8))),   // 7/8
            Map.entry(0x2189, Rational.of(BigInteger.ZERO, BigInteger.ONE)));        // 0/3 -> 0

    private static final int MINUS_SIGN = 0x2212;   // U+2212 MINUS SIGN
    private static final int FRACTION_SLASH = 0x2044; // U+2044 FRACTION SLASH

    /**
     * The exact real value if the whole (trimmed) string is a single signed
     * number, otherwise empty. Handles a leading sign, a single decimal point,
     * ASCII/Unicode and mixed fractions, and the Unicode vulgar fractions. The
     * integer numeral systems of {@link #parseWhole} are also accepted (as whole
     * values).
     */
    public static Optional<Rational> parseValue(String s) {
        return parseValue(s, true, null);
    }

    /** Like {@link #parseValue(String)}, optionally without the Roman system. */
    public static Optional<Rational> parseValue(String s, boolean includeRoman) {
        return parseValue(s, includeRoman, null);
    }

    /**
     * Like {@link #parseValue(String)}; {@code numberLocale} (may be null)
     * names the convention decimals are written in. For comma-decimal locales
     * (German etc.) "9,90" is nine point nine and "1.234,56" uses dots for
     * grouping; without a locale only the dot-decimal forms are read.
     */
    public static Optional<Rational> parseValue(String s, boolean includeRoman, Locale numberLocale) {
        if (s == null) {
            return Optional.empty();
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return Optional.empty();
        }
        boolean negative = false;
        int first = t.codePointAt(0);
        if (first == '-' || first == MINUS_SIGN) {
            negative = true;
            t = t.substring(Character.charCount(first));
        } else if (first == '+') {
            t = t.substring(Character.charCount(first));
        }
        String mag = t.trim();
        if (mag.isEmpty()) {
            return Optional.empty();
        }
        Optional<Rational> value = parseMagnitude(mag, includeRoman, isCommaDecimal(numberLocale));
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(negative ? value.get().negate() : value.get());
    }

    /**
     * The exact real value of the FIRST number occurring in the text, or empty
     * if none. The scanner keeps sign, decimal point and fraction slashes inside
     * a token (so "-5", "1.5" and "3/4" are read as one number), but a comma and
     * other punctuation still separate tokens. A token that is not a clean number
     * as a whole falls back to its first decimal-digit run (so "item2" is 2).
     * Mixed numbers with an internal space ("1 1/2") are only recognized by
     * {@link #parseValue} on the whole string, not when embedded in a sentence.
     */
    public static Optional<Rational> firstValue(String text) {
        return firstValue(text, true, null);
    }

    /** Like {@link #firstValue(String)}, optionally without the Roman system. */
    public static Optional<Rational> firstValue(String text, boolean includeRoman) {
        return firstValue(text, includeRoman, null);
    }

    /**
     * Like {@link #firstValue(String)}; {@code numberLocale} (may be null)
     * names the decimal convention, see {@link #parseValue(String, boolean, Locale)}.
     */
    public static Optional<Rational> firstValue(String text, boolean includeRoman, Locale numberLocale) {
        if (text == null || text.isEmpty()) {
            return Optional.empty();
        }
        boolean commaDecimal = isCommaDecimal(numberLocale);
        int i = 0;
        int n = text.length();
        while (i < n) {
            int cp = text.codePointAt(i);
            if (isValueSeparator(cp, commaDecimal)) {
                i += Character.charCount(cp);
                continue;
            }
            int start = i;
            while (i < n) {
                int c = text.codePointAt(i);
                if (isValueSeparator(c, commaDecimal)) {
                    break;
                }
                i += Character.charCount(c);
            }
            Optional<Rational> value = tokenValue(text.substring(start, i), includeRoman, commaDecimal);
            if (value.isPresent()) {
                return value;
            }
        }
        return Optional.empty();
    }

    /**
     * A value for this token: the whole token as a number, else its first decimal
     * run, else the first embedded single Nl/No numeric code point (so a numeral
     * glued to punctuation, an enclosed number before a period say, is found).
     */
    private static Optional<Rational> tokenValue(String token, boolean includeRoman,
            boolean commaDecimal) {
        Optional<Rational> whole = parseMagnitudeToken(token, includeRoman, commaDecimal);
        if (whole.isPresent()) {
            return whole;
        }
        Optional<BigInteger> run = firstDecimalRun(token);
        if (run.isPresent()) {
            return Optional.of(Rational.ofInteger(run.get()));
        }
        for (int i = 0; i < token.length();) {
            int cp = token.codePointAt(i);
            Optional<Rational> v = singleCodePointNumericValue(new String(Character.toChars(cp)), includeRoman);
            if (v.isPresent()) {
                return v;
            }
            i += Character.charCount(cp);
        }
        return Optional.empty();
    }

    /** Non-negative magnitude: vulgar/mixed/fraction/decimal, else an integer numeral. */
    /** A signed token from the scanner: optional sign, then a magnitude. */
    private static Optional<Rational> parseMagnitudeToken(String token, boolean includeRoman,
            boolean commaDecimal) {
        if (token.isEmpty()) {
            return Optional.empty();
        }
        boolean negative = false;
        int first = token.codePointAt(0);
        if (first == '-' || first == MINUS_SIGN) {
            negative = true;
            token = token.substring(Character.charCount(first));
        } else if (first == '+') {
            token = token.substring(Character.charCount(first));
        }
        if (token.isEmpty()) {
            return Optional.empty();
        }
        Optional<Rational> value = parseMagnitude(token, includeRoman, commaDecimal);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(negative ? value.get().negate() : value.get());
    }

    private static boolean isCommaDecimal(Locale numberLocale) {
        return numberLocale != null
                && DecimalFormatSymbols.getInstance(numberLocale).getDecimalSeparator() == ',';
    }

    /**
     * Rewrites a comma-decimal magnitude into the dot-decimal form the rest of
     * the parser reads: "9,90" -> "9.90", "1.234,56" -> "1234.56", and a pure
     * dot-grouped integer "1.234" -> "1234". Anything else is returned as-is.
     */
    private static String normalizeCommaDecimal(String u) {
        if (GROUPED_COMMA_DECIMAL.matcher(u).matches()) {
            return u.replace(".", "").replace(',', '.');
        }
        if (SIMPLE_COMMA_DECIMAL.matcher(u).matches()) {
            return u.replace(',', '.');
        }
        return u;
    }

    private static final Pattern GROUPED_COMMA_DECIMAL = Pattern.compile("\\p{Nd}{1,3}(\\.\\p{Nd}{3})+(,\\p{Nd}+)?");
    private static final Pattern SIMPLE_COMMA_DECIMAL = Pattern.compile("\\p{Nd}+,\\p{Nd}+");

    private static Optional<Rational> parseMagnitude(String u, boolean includeRoman, boolean commaDecimal) {
        if (commaDecimal) {
            u = normalizeCommaDecimal(u);
        }
        // A trailing vulgar fraction char, optionally with an integer prefix.
        int lastCp = u.codePointBefore(u.length());
        Rational vulgar = VULGAR.get(lastCp);
        if (vulgar != null) {
            String prefix = u.substring(0, u.length() - Character.charCount(lastCp));
            if (prefix.isEmpty()) {
                return Optional.of(vulgar);
            }
            String digits = asciiDigits(prefix);
            if (digits != null) {
                BigInteger whole = new BigInteger(digits);
                return Optional.of(Rational.of(
                        whole.multiply(vulgar.denominator()).add(vulgar.numerator()), vulgar.denominator()));
            }
            return Optional.empty();
        }
        // A mixed number "INT<space>FRACTION" (e.g. "1 1/2").
        int space = indexOfWhitespace(u);
        if (space >= 0) {
            String intPart = u.substring(0, space);
            String rest = u.substring(space).trim();
            String intDigits = asciiDigits(intPart);
            if (intDigits != null && !intDigits.isEmpty()) {
                Optional<Rational> frac = parseFraction(rest);
                if (frac.isPresent()) {
                    Rational f = frac.get();
                    // A mixed number's fraction part must be proper (0 <= f < 1).
                    if (f.numerator().signum() >= 0 && f.compareTo(Rational.ofInteger(BigInteger.ONE)) < 0) {
                        BigInteger whole = new BigInteger(intDigits);
                        return Optional.of(Rational.of(
                                whole.multiply(f.denominator()).add(f.numerator()), f.denominator()));
                    }
                }
            }
            return Optional.empty();
        }
        // A slash fraction "a/b".
        Optional<Rational> fraction = parseFraction(u);
        if (fraction.isPresent()) {
            return fraction;
        }
        // A decimal number.
        Optional<Rational> decimal = parseDecimal(u);
        if (decimal.isPresent()) {
            return decimal;
        }
        // Fall back to an integer numeral of one of the algorithmic systems
        // (Roman, CJK, Ethiopic ...). Those never contain decimal (Nd) digits, and
        // every Nd-digit form was already handled above, so a leftover Nd-digit
        // string here is junk (e.g. "1,000", "1.2.3", "3-5") that ICU would wrongly
        // grouping-parse; reject it rather than trust that parse.
        if (hasDecimalDigit(u)) {
            return Optional.empty();
        }
        Optional<Rational> algorithmic = parseWhole(u, includeRoman).map(Rational::ofInteger);
        if (algorithmic.isPresent()) {
            return algorithmic;
        }
        // Last resort: a single Nl/No code point that carries a Unicode numeric
        // value but is neither a decimal digit nor an algorithmic numeral -
        // enclosed/parenthesized numbers and many historic script
        // numerals (Aegean, cuneiform, Aramaic, Greek acrophonic ...). Only a
        // single, non-negative integer value is taken; fractional forms and
        // multi-sign additive sequences are left for a later, dedicated step.
        return singleCodePointNumericValue(u, includeRoman);
    }

    /**
     * The value of a single Nl/No code point via its Unicode numeric value, if it
     * is a non-negative integer. Symbols without a numeric value, the emoji for a
     * hundred points or the keycap ten say, and fractional values yield empty.
     */
    private static Optional<Rational> singleCodePointNumericValue(String u, boolean includeRoman) {
        if (u.codePointCount(0, u.length()) != 1) {
            return Optional.empty();
        }
        int cp = u.codePointAt(0);
        if (!includeRoman && Character.UnicodeScript.of(cp) == Character.UnicodeScript.LATIN) {
            // Roman number forms (Ⅻ, ⅲ) carry Latin script.
            return Optional.empty();
        }
        int type = Character.getType(cp);
        if (type != Character.LETTER_NUMBER && type != Character.OTHER_NUMBER) {
            return Optional.empty();
        }
        double v = UCharacter.getUnicodeNumericValue(cp);
        if (v == UCharacter.NO_NUMERIC_VALUE || v < 0 || Double.isInfinite(v) || v != Math.floor(v)) {
            return Optional.empty();
        }
        return Optional.of(Rational.ofInteger(BigDecimal.valueOf(v).toBigIntegerExact()));
    }

    private static boolean hasDecimalDigit(String s) {
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            if (Character.digit(cp, 10) >= 0) {
                return true;
            }
            i += Character.charCount(cp);
        }
        return false;
    }

    /** A single vulgar fraction char, or "a/b" with either fraction slash, integer parts, non-zero b. */
    private static Optional<Rational> parseFraction(String u) {
        if (u.codePointCount(0, u.length()) == 1) {
            Rational v = VULGAR.get(u.codePointAt(0));
            if (v != null) {
                return Optional.of(v);
            }
        }
        int slash = -1;
        int slashCount = 0;
        for (int i = 0; i < u.length();) {
            int cp = u.codePointAt(i);
            if (cp == '/' || cp == FRACTION_SLASH) {
                slash = i;
                slashCount++;
            }
            i += Character.charCount(cp);
        }
        if (slashCount != 1) {
            return Optional.empty();
        }
        String numerator = asciiDigits(u.substring(0, slash));
        String denominator = asciiDigits(u.substring(slash + 1));
        if (numerator == null || numerator.isEmpty() || denominator == null || denominator.isEmpty()) {
            return Optional.empty();
        }
        BigInteger den = new BigInteger(denominator);
        if (den.signum() == 0) {
            return Optional.empty(); // division by zero -> no value
        }
        return Optional.of(Rational.of(new BigInteger(numerator), den));
    }

    /** A decimal number with at most one '.', at least one digit: "3.14", ".5", "1.", "100". */
    private static Optional<Rational> parseDecimal(String u) {
        int dot = -1;
        int dotCount = 0;
        for (int i = 0; i < u.length();) {
            int cp = u.codePointAt(i);
            if (cp == '.') {
                dot = i;
                dotCount++;
            }
            i += Character.charCount(cp);
        }
        if (dotCount == 0) {
            String digits = asciiDigits(u);
            return digits == null ? Optional.empty() : Optional.of(Rational.ofInteger(new BigInteger(digits)));
        }
        if (dotCount > 1) {
            return Optional.empty();
        }
        String left = u.substring(0, dot);
        String right = u.substring(dot + 1);
        String leftDigits = left.isEmpty() ? "" : asciiDigits(left);
        String rightDigits = right.isEmpty() ? "" : asciiDigits(right);
        if (leftDigits == null || rightDigits == null) {
            return Optional.empty();
        }
        if (leftDigits.isEmpty() && rightDigits.isEmpty()) {
            return Optional.empty(); // "." alone
        }
        BigInteger num = new BigInteger(leftDigits + rightDigits);
        BigInteger den = BigInteger.TEN.pow(rightDigits.length());
        return Optional.of(Rational.of(num, den));
    }

    /** The ASCII-digit form of a string of decimal digits (any script), or null if any char is not Nd. */
    private static String asciiDigits(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            int d = Character.digit(cp, 10);
            if (d < 0) {
                return null;
            }
            sb.append((char) ('0' + d));
            i += Character.charCount(cp);
        }
        return sb.toString();
    }

    private static int indexOfWhitespace(String s) {
        for (int i = 0; i < s.length();) {
            int cp = s.codePointAt(i);
            if (Character.isWhitespace(cp)) {
                return i;
            }
            i += Character.charCount(cp);
        }
        return -1;
    }

    /**
     * Token boundaries for the real-value scanner. Sign, decimal point and the
     * fraction slashes stay inside a token so a signed/decimal/fraction number is
     * read as one unit; a comma, colon and other punctuation still separate.
     */
    private static boolean isValueSeparator(int cp, boolean commaDecimal) {
        if (cp == '-' || cp == '+' || cp == '.' || cp == '/' || cp == MINUS_SIGN || cp == FRACTION_SLASH) {
            return false;
        }
        if (commaDecimal && cp == ',') {
            // The decimal comma stays inside the token; a trailing list comma
            // makes the token unparseable as a whole and the digit-run
            // fallback still finds the number.
            return false;
        }
        if (Character.digit(cp, 10) >= 0 || Character.isLetter(cp)) {
            return false;
        }
        int type = Character.getType(cp);
        // Number-letter (Roman numeral code points) and number-other (vulgar fractions) belong here.
        return type != Character.LETTER_NUMBER && type != Character.OTHER_NUMBER;
    }
}
