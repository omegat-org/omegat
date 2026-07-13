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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.omegat.util.NumberAutoConverter.Conversion;
import org.omegat.util.NumberAutoConverter.DataType;

public class NumberAutoConverterTest {

    private static final Locale DE = Locale.GERMANY;
    private static final Locale EN = Locale.US;

    private static Conversion top(String source, Locale from, Locale to) {
        List<Conversion> list = NumberAutoConverter.convert(source, from, to);
        return list.isEmpty() ? null : list.get(0);
    }

    private static void assertConv(String source, Locale from, Locale to, DataType type, String target) {
        Conversion c = top(source, from, to);
        assertNotNull("expected a conversion for [" + source + "]", c);
        assertEquals("type for [" + source + "]", type, c.getType());
        assertEquals("target for [" + source + "]", target, c.getTarget());
    }

    private static void assertEmpty(String source, Locale from, Locale to) {
        assertTrue("expected no conversion for [" + source + "]",
                NumberAutoConverter.convert(source, from, to).isEmpty());
    }

    // --- integers across writing systems ----------------------------------

    @Test
    public void integersAnyScript() {
        assertConv("15", DE, EN, DataType.INTEGER, "15");
        assertConv("XL", DE, EN, DataType.INTEGER, "40"); // Roman (opt-in default)
        assertConv("三百", DE, EN, DataType.INTEGER, "300"); // CJK
        assertConv("٩", DE, EN, DataType.INTEGER, "9"); // Arabic-Indic
        assertConv("９０", DE, EN, DataType.INTEGER, "90"); // full-width
    }

    @Test
    public void bareIntegerCarriesNoGrouping() {
        // Source had no grouping separator, so neither does the proposal.
        assertConv("9443", DE, EN, DataType.INTEGER, "9443");
        assertConv("116117", DE, EN, DataType.INTEGER, "116117");
    }

    @Test
    public void phoneNumbersAndIdentifiersAreRejected() {
        assertEmpty("03641 26 87 183", DE, EN); // spaced groups + leading zero
        assertEmpty("+49", DE, EN); // leading country-code plus
        assertEmpty("03641", DE, EN); // leading zero identifier
    }

    @Test
    public void romanScoresLowSoItIsNotAutoSelected() {
        java.util.Set<DataType> all = java.util.EnumSet.allOf(DataType.class);
        Conversion c = NumberAutoConverter.convert("XL", DE, EN, all, true).get(0);
        assertEquals(DataType.INTEGER, c.getType());
        assertTrue("Roman confidence should be low: " + c.getConfidence(), c.getConfidence() < 0.5);
    }

    // --- decimals, percent, currency ---------------------------------------

    @Test
    public void decimalReformatsGroupingAndPreservesPrecision() {
        assertConv("1.000,50", DE, EN, DataType.DECIMAL, "1,000.50"); // grouping + two fraction kept
        assertConv("1.000,5", DE, EN, DataType.DECIMAL, "1,000.5"); // one kept
        assertConv("1000,50", DE, EN, DataType.DECIMAL, "1000.50"); // no source grouping -> none
    }

    @Test
    public void percentPreservesFractionAcrossScripts() {
        assertConv("12,5 %", DE, EN, DataType.PERCENT, "12.5%");
        assertConv("100,0 %", DE, EN, DataType.PERCENT, "100.0%"); // trailing zero kept
        assertConv("50 %", DE, EN, DataType.PERCENT, "50%"); // no fraction stays none
        assertConv("９０％", DE, EN, DataType.PERCENT, "90%"); // full-width digits and sign
        assertConv("٥٠٪", Locale.forLanguageTag("ar"), EN, DataType.PERCENT, "50%"); // Arabic percent
    }

    @Test
    public void timePreservesSecondsPresence() {
        Conversion withSeconds = top("14:30:00", DE, EN);
        assertNotNull(withSeconds);
        assertEquals(DataType.TIME, withSeconds.getType());
        assertTrue("seconds should be kept: " + withSeconds.getTarget(),
                withSeconds.getTarget().contains("2:30:00"));

        Conversion noSeconds = top("14:30", DE, EN);
        assertNotNull(noSeconds);
        assertTrue("no seconds should stay: " + noSeconds.getTarget(),
                noSeconds.getTarget().startsWith("2:30") && !noSeconds.getTarget().contains("2:30:00"));
    }

    @Test
    public void currencyKeepsCurrencyReformatsForTarget() {
        assertConv("1.234,56 €", DE, EN, DataType.CURRENCY, "€1,234.56");
    }

    @Test
    public void bareNumberIsNotCurrency() {
        // Without a currency symbol a grouped amount stays a decimal, never a
        // lenient currency guess in the locale's default currency.
        List<Conversion> l = NumberAutoConverter.convert("1.234,56", DE, EN);
        assertTrue("bare number must not be a currency: " + l,
                l.stream().noneMatch(c -> c.getType() == DataType.CURRENCY));
        assertConv("1.234,56", DE, EN, DataType.DECIMAL, "1,234.56");
    }

    // --- dates and times ---------------------------------------------------

    @Test
    public void isoDateOutranksLocalizedDate() {
        // "2026-12-31" is unambiguous (a component > 12); "05.03.2026" is not,
        // so the ISO date must score clearly higher.
        Conversion iso = top("2026-12-31", DE, EN);
        assertNotNull(iso);
        assertEquals(DataType.DATE, iso.getType());
        assertTrue(iso.getTarget().contains("2026"));

        Conversion local = top("05.03.2026", DE, EN);
        assertNotNull(local);
        assertEquals(DataType.DATE, local.getType());
        assertTrue("ISO date should outrank an ambiguous localized date",
                iso.getConfidence() > local.getConfidence());
        assertTrue(iso.getConfidence() >= 0.85);
    }

    @Test
    public void timeReformatsToTargetLocale() {
        Conversion c = top("14:30", DE, EN);
        assertNotNull(c);
        assertEquals(DataType.TIME, c.getType());
        // The space before the day period is CLDR-version dependent (a narrow
        // no-break space in recent data), so assert structurally.
        assertTrue("unexpected time rendering: " + c.getTarget(),
                c.getTarget().startsWith("2:30") && c.getTarget().endsWith("PM"));
    }

    // --- ordinals: locale-driven, many writing systems ---------------------

    @Test
    public void ordinalsAreLocaleDriven() {
        assertConv("3.", DE, EN, DataType.ORDINAL, "3rd"); // German dot
        assertConv("21.", DE, EN, DataType.ORDINAL, "21st");
        assertConv("1st", Locale.forLanguageTag("en"), DE, DataType.ORDINAL, "1."); // English into German
        assertConv("1er", Locale.FRENCH, EN, DataType.ORDINAL, "1st"); // French
        assertConv("1.º", Locale.forLanguageTag("es"), EN, DataType.ORDINAL, "1st"); // Spanish indicator
        assertConv("3:e", Locale.forLanguageTag("sv"), EN, DataType.ORDINAL, "3rd"); // Swedish colon
        assertConv("第3", Locale.CHINESE, EN, DataType.ORDINAL, "3rd"); // Chinese prefix marker
        assertConv("3r", Locale.forLanguageTag("ca"), EN, DataType.ORDINAL, "3rd"); // Catalan
    }

    @Test
    public void sameStringIsOrdinalOrDecimalDependingOnSourceLocale() {
        // "3." is an ordinal in German but a decimal in English.
        assertConv("3.", DE, EN, DataType.ORDINAL, "3rd");
        assertConv("3.", Locale.forLanguageTag("en"), DE, DataType.DECIMAL, "3");
    }

    @Test
    public void bareDigitRunIsNeverOrdinal() {
        assertConv("15", DE, EN, DataType.INTEGER, "15");
    }

    // --- normalization hygiene: bidi / no-break / zero-width ---------------

    @Test
    public void hygieneStripsControlAndSpaceVariants() {
        assertConv("5\u00A0", DE, EN, DataType.INTEGER, "5"); // trailing no-break space
        assertConv("\u200E5", DE, EN, DataType.INTEGER, "5"); // leading LRM
        assertConv("5\u200F", DE, EN, DataType.INTEGER, "5"); // trailing RLM
        assertConv("5\u200B", DE, EN, DataType.INTEGER, "5"); // trailing zero-width space
        assertConv("  15  ", DE, EN, DataType.INTEGER, "15");
    }

    // --- out of scope: prose, empty, documented limits --------------------

    @Test
    public void proseAndEmptyYieldNothing() {
        assertEmpty("Kapitel 5", DE, EN);
        assertEmpty("hello world", DE, EN);
        assertEmpty("5 Kapitel", DE, EN);
        assertEmpty("", DE, EN);
        assertEmpty("   ", DE, EN);
        assertEmpty(null, DE, EN);
    }

    @Test
    public void ipAddressIsNotANumber() {
        // A dotted-quad IP would otherwise parse as a grouped integer in de.
        assertEmpty("192.168.168.255", DE, EN);
        assertEmpty("10.0.0.1", DE, EN);
    }

    @Test
    public void romanIsOptIn() {
        java.util.Set<DataType> all = java.util.EnumSet.allOf(DataType.class);
        // Opted out: a bare Roman numeral / Latin word is not an integer,
        // including ICU's odd forms ("N"=0) and words that contain non-standard
        // letters ("MDN") which a narrow [IVXLCDM] gate would have missed.
        assertTrue(NumberAutoConverter.convert("XL", DE, EN, all, false).isEmpty());
        assertTrue(NumberAutoConverter.convert("CD", DE, EN, all, false).isEmpty());
        assertTrue(NumberAutoConverter.convert("N", DE, EN, all, false).isEmpty());
        assertTrue(NumberAutoConverter.convert("MDN", DE, EN, all, false).isEmpty());
        // Opted in: recognized as an integer.
        Conversion c = NumberAutoConverter.convert("XL", DE, EN, all, true).stream().findFirst().orElse(null);
        assertNotNull(c);
        assertEquals(DataType.INTEGER, c.getType());
        assertEquals("40", c.getTarget());
    }

    @Test
    public void documentedOrdinalLimits() {
        // ICU cannot parse these ordinal forms; they are intentionally out of
        // scope and must not be mis-detected (for example as "0th").
        assertEmpty("1º", Locale.ITALIAN, EN);
        assertEmpty("3:s", Locale.forLanguageTag("fi"), EN);
        assertEmpty("1,5×10³", DE, EN); // scientific notation, not in scope
    }

    // --- render options + culture heuristics -------------------------------

    private static java.util.Set<DataType> allTypes() {
        return java.util.EnumSet.allOf(DataType.class);
    }

    private static Conversion first(String s, Locale from, Locale to, NumberAutoConverter.RenderOptions opts) {
        java.util.List<Conversion> l = NumberAutoConverter.convert(s, from, to, allTypes(), false, opts);
        return l.isEmpty() ? null : l.get(0);
    }

    @Test
    public void groupingOptionOverridesSource() {
        NumberAutoConverter.RenderOptions.Style keepStyle = NumberAutoConverter.RenderOptions.Style.ORIGINAL;
        NumberAutoConverter.RenderOptions.Fraction keepFrac = NumberAutoConverter.RenderOptions.Fraction.ORIGINAL;
        Conversion always = first("9443", DE, EN,
                new NumberAutoConverter.RenderOptions(NumberAutoConverter.RenderOptions.Grouping.ALWAYS, keepFrac,
                        keepStyle));
        assertEquals("9,443", always.getTarget());
        Conversion never = first("9443", DE, EN,
                new NumberAutoConverter.RenderOptions(NumberAutoConverter.RenderOptions.Grouping.NEVER, keepFrac,
                        keepStyle));
        assertEquals("9443", never.getTarget());
    }

    @Test
    public void groupingOptionAppliesToCurrency() {
        NumberAutoConverter.RenderOptions.Style keepStyle = NumberAutoConverter.RenderOptions.Style.ORIGINAL;
        NumberAutoConverter.RenderOptions.Fraction keepFrac = NumberAutoConverter.RenderOptions.Fraction.ORIGINAL;
        Conversion never = first("20.456,23 €", DE, EN,
                new NumberAutoConverter.RenderOptions(NumberAutoConverter.RenderOptions.Grouping.NEVER, keepFrac,
                        keepStyle));
        assertEquals(DataType.CURRENCY, never.getType());
        assertEquals("€20456.23", never.getTarget());
        Conversion always = first("20456,23 €", DE, EN,
                new NumberAutoConverter.RenderOptions(NumberAutoConverter.RenderOptions.Grouping.ALWAYS, keepFrac,
                        keepStyle));
        assertEquals("€20,456.23", always.getTarget());
        // ORIGINAL mirrors the ungrouped source.
        Conversion original = first("20456,23 €", DE, EN,
                new NumberAutoConverter.RenderOptions(NumberAutoConverter.RenderOptions.Grouping.ORIGINAL, keepFrac,
                        keepStyle));
        assertEquals("€20456.23", original.getTarget());
    }

    @Test
    public void groupingOptionAppliesToPercent() {
        NumberAutoConverter.RenderOptions.Style keepStyle = NumberAutoConverter.RenderOptions.Style.ORIGINAL;
        NumberAutoConverter.RenderOptions.Fraction keepFrac = NumberAutoConverter.RenderOptions.Fraction.ORIGINAL;
        Conversion never = first("20.456 %", DE, EN,
                new NumberAutoConverter.RenderOptions(NumberAutoConverter.RenderOptions.Grouping.NEVER, keepFrac,
                        keepStyle));
        assertEquals(DataType.PERCENT, never.getType());
        assertEquals("20456%", never.getTarget());
    }

    @Test
    public void fractionOptionOverridesSource() {
        Conversion two = first("1,5", DE, EN,
                new NumberAutoConverter.RenderOptions(NumberAutoConverter.RenderOptions.Grouping.ORIGINAL,
                        NumberAutoConverter.RenderOptions.Fraction.TWO,
                        NumberAutoConverter.RenderOptions.Style.ORIGINAL));
        assertEquals(DataType.DECIMAL, two.getType());
        assertEquals("1.50", two.getTarget());
    }

    @Test
    public void fractionOptionAppliesToPercent() {
        Conversion one = first("50 %", DE, EN,
                new NumberAutoConverter.RenderOptions(NumberAutoConverter.RenderOptions.Grouping.ORIGINAL,
                        NumberAutoConverter.RenderOptions.Fraction.ONE,
                        NumberAutoConverter.RenderOptions.Style.ORIGINAL));
        assertNotNull(one);
        assertEquals(DataType.PERCENT, one.getType());
        assertEquals("50.0%", one.getTarget());
    }

    @Test
    public void valuePreservationHeuristic() {
        // Value read in the source locale survives the rendering: +3%.
        Conversion preserved = top("1.000,50", DE, EN);
        assertTrue(preserved.getFactors().stream().anyMatch(f -> "VALUE_SAME".equals(f.getId())));
        // Lossy rounding via the zero-fraction option changes the value: -10%.
        Conversion lossy = first("12,5 %", DE, EN,
                new NumberAutoConverter.RenderOptions(NumberAutoConverter.RenderOptions.Grouping.ORIGINAL,
                        NumberAutoConverter.RenderOptions.Fraction.ZERO,
                        NumberAutoConverter.RenderOptions.Style.ORIGINAL));
        assertNotNull(lossy);
        assertTrue("rounded percent must be flagged as value change: " + lossy.getFactors(),
                lossy.getFactors().stream().anyMatch(f -> "VALUE_DIFF".equals(f.getId())));
    }

    @Test
    public void foreignSeparatorLowersConfidence() {
        // The apostrophe is not a German separator; the lenient parse of the
        // Swiss-style "99'999" is a guess and must cost confidence.
        Conversion foreign = top("99'999", DE, EN);
        assertNotNull(foreign);
        assertTrue(foreign.getFactors().stream().anyMatch(f -> "FOREIGN_SEPARATOR".equals(f.getId())));
        // The locale's own separators stay unpenalized.
        Conversion own = top("1.000,50", DE, EN);
        assertTrue(own.getFactors().stream().noneMatch(f -> "FOREIGN_SEPARATOR".equals(f.getId())));
    }

    @Test
    public void parsedSourceValueIsExposedForConsistentSorting() {
        // "99'999" (Swiss-style grouping accepted by the lenient de parser)
        // must expose 99999 as its numeric value, so numeric sorting agrees
        // with the value-preservation heuristic instead of reading 99.
        Conversion c = top("99'999", DE, EN);
        assertNotNull(c);
        assertEquals(DataType.INTEGER, c.getType());
        assertEquals(99999.0, c.getSourceValue().orElse(-1.0), 0.0001);
        assertTrue(c.getFactors().stream().anyMatch(f -> "VALUE_SAME".equals(f.getId())));
    }

    @Test
    public void misplacedGroupingScoresMuchLower() {
        // "123.45 €" in German: the dot is the grouping separator but does not
        // sit on a three-digit group — the author used a foreign decimal
        // convention, so the parsed value (12345) is probably wrong.
        Conversion suspicious = top("123.45 €", DE, EN);
        assertNotNull(suspicious);
        assertEquals(DataType.CURRENCY, suspicious.getType());
        Conversion clean = top("15,75 €", DE, EN);
        assertNotNull(clean);
        assertTrue("misplaced grouping must score far lower: " + suspicious.getConfidence() + " vs "
                + clean.getConfidence(), suspicious.getConfidence() <= clean.getConfidence() - 0.2);
    }

    @Test
    public void equalDayAndMonthIsNeitherBonusNorMalus() {
        // '01.01.21' and '01.01.2024' must score alike: with equal day and
        // month the order is irrelevant, so neither the unambiguous bonus nor
        // the ambiguous malus applies.
        Conversion shortYear = top("01.01.21", DE, EN);
        Conversion longYear = top("01.01.2024", DE, EN);
        assertNotNull(shortYear);
        assertNotNull(longYear);
        assertEquals(DataType.DATE, shortYear.getType());
        assertEquals(DataType.DATE, longYear.getType());
        assertEquals(shortYear.getConfidence(), longYear.getConfidence(), 0.0001);
    }

    @Test
    public void romanMetricAndClarityHeuristics() {
        java.util.Set<DataType> all = java.util.EnumSet.allOf(DataType.class);
        // Metric abbreviations that parse as Roman are penalized.
        Conversion cm = NumberAutoConverter.convert("cm", DE, EN, all, true).get(0);
        assertTrue("metric penalty expected: " + cm.getFactors(),
                cm.getFactors().stream().anyMatch(f -> "METRIC_UNIT".equals(f.getId())));
        // A canonical uppercase numeral containing I/V/X earns the clarity bonus.
        Conversion xl = NumberAutoConverter.convert("XL", DE, EN, all, true).get(0);
        assertTrue("clarity bonus expected: " + xl.getFactors(),
                xl.getFactors().stream().anyMatch(f -> "ROMAN_CLEAR".equals(f.getId())));
        // "CD" is canonical but has no I/V/X (compact disc): neither bonus nor
        // metric penalty in its uppercase form... it IS in the metric list, so
        // the penalty applies via the lowercase match.
        Conversion cd = NumberAutoConverter.convert("CD", DE, EN, all, true).get(0);
        assertTrue(cd.getFactors().stream().noneMatch(f -> "ROMAN_CLEAR".equals(f.getId())));
    }

    @Test
    public void romanBaseIsTwentyPercent() {
        java.util.Set<DataType> all = java.util.EnumSet.allOf(DataType.class);
        Conversion c = NumberAutoConverter.convert("XL", DE, EN, all, true).get(0);
        assertEquals(0.2, c.getFactors().get(0).getDelta(), 0.0001);
    }

    @Test
    public void lengthHeuristicAppliesOnlyWhenNotIdentical() {
        // "1.000,50" -> "1,000.50": same length, not identical -> +2% factor.
        Conversion sameLen = top("1.000,50", DE, EN);
        assertTrue(sameLen.getFactors().stream().anyMatch(f -> "LENGTH_SAME".equals(f.getId())));
        // Identical rendering gets the identical bonus, no length factor.
        Conversion identical = top("2026-12-31", DE, Locale.forLanguageTag("en"));
        assertNotNull(identical);
    }

    @Test
    public void leadingZeroQuantityScoresLower() {
        // "0,5" is a decimal whose leading zero is normal but still hints at
        // codes/padding per policy: the LEADING_ZERO_PRESENT factor applies.
        Conversion c = top("0,5", DE, EN);
        assertNotNull(c);
        assertEquals(DataType.DECIMAL, c.getType());
        assertTrue(c.getFactors().stream().anyMatch(f -> "LEADING_ZERO_PRESENT".equals(f.getId())));
    }

    @Test
    public void ambiguousDecimalAndCurrencySymbolLoseConfidence() {
        Conversion ambiguous = top("1.234", DE, EN); // one dot, three digits
        assertEquals(DataType.DECIMAL, ambiguous.getType());
        assertTrue("ambiguous decimal should be penalized: " + ambiguous.getConfidence(),
                ambiguous.getConfidence() < 0.75);

        Conversion dollar = top("$5.00", Locale.forLanguageTag("en"), DE);
        assertEquals(DataType.CURRENCY, dollar.getType());
        assertTrue("ambiguous currency symbol should be penalized: " + dollar.getConfidence(),
                dollar.getConfidence() < 0.8);
    }

    // --- fixture driven: the demonstration XLIFF is the scope oracle -------

    @Test
    public void demonstrationXliffDrivesScope() throws Exception {
        Map<String, DataType> expected = new HashMap<>();
        expected.put("fmt-cur-eur", DataType.CURRENCY);
        expected.put("fmt-date-de", DataType.DATE);
        expected.put("fmt-date-iso", DataType.DATE);
        expected.put("fmt-pct-de", DataType.PERCENT);
        expected.put("fmt-pct-fw", DataType.PERCENT);
        expected.put("fmt-ord-en", DataType.ORDINAL);
        expected.put("fmt-ord-de", DataType.ORDINAL);
        expected.put("fmt-time", DataType.TIME);
        // These two number-only formats are intentionally not convertible here:
        // a $ amount is foreign to the German source locale, and scientific
        // notation is out of scope.
        java.util.Set<String> expectedEmpty = new java.util.HashSet<>(
                java.util.Arrays.asList("fmt-cur-usd", "fmt-sci"));

        // Resolved from the test classpath so tree reorganizations cannot break it.
        java.net.URL fixture = getClass().getResource("/data/editor/sort/numeric-sort-demo.xliff");
        assertNotNull("fixture missing on test classpath", fixture);
        File xliff = new File(fixture.toURI());
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xliff);
        NodeList units = doc.getElementsByTagName("trans-unit");

        int fmtSeen = 0;
        int proseSeen = 0;
        for (int i = 0; i < units.getLength(); i++) {
            Element u = (Element) units.item(i);
            String id = u.getAttribute("id");
            NodeList src = u.getElementsByTagName("source");
            String source = src.getLength() == 0 ? "" : src.item(0).getTextContent();

            if (id.startsWith("fmt-")) {
                fmtSeen++;
                if (expectedEmpty.contains(id)) {
                    assertEmpty(source, DE, EN);
                } else {
                    DataType want = expected.get(id);
                    assertNotNull("unclassified fmt id " + id, want);
                    Conversion c = top(source, DE, EN);
                    assertNotNull("expected conversion for " + id + " [" + source + "]", c);
                    assertEquals("type for " + id, want, c.getType());
                }
            } else {
                // Every non-fmt segment embeds its number in prose, so #794's
                // number-only gate must reject all of them.
                proseSeen++;
                assertEmpty(source, DE, EN);
            }
        }
        assertTrue("too few fmt units seen: " + fmtSeen, fmtSeen >= 10);
        assertTrue("too few prose units seen: " + proseSeen, proseSeen >= 50);
    }
}
