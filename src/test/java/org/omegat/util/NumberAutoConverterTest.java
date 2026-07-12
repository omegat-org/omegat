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
        assertConv("XL", DE, EN, DataType.INTEGER, "40"); // Roman
        assertConv("三百", DE, EN, DataType.INTEGER, "300"); // CJK
        assertConv("٩", DE, EN, DataType.INTEGER, "9"); // Arabic-Indic
        assertConv("９０", DE, EN, DataType.INTEGER, "90"); // full-width
    }

    // --- decimals, percent, currency ---------------------------------------

    @Test
    public void decimalReformatsGroupingAndSeparators() {
        assertConv("1.000,50", DE, EN, DataType.DECIMAL, "1,000.5");
    }

    @Test
    public void percentPreservesFractionAcrossScripts() {
        assertConv("12,5 %", DE, EN, DataType.PERCENT, "12.5%");
        assertConv("９０％", DE, EN, DataType.PERCENT, "90%"); // full-width digits and sign
        assertConv("٥٠٪", Locale.forLanguageTag("ar"), EN, DataType.PERCENT, "50%"); // Arabic percent
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
        Conversion iso = top("2026-12-31", DE, EN);
        assertNotNull(iso);
        assertEquals(DataType.DATE, iso.getType());
        assertTrue(iso.getTarget().contains("2026"));
        assertEquals(0.8, iso.getConfidence(), 0.0001);

        Conversion local = top("05.03.2026", DE, EN);
        assertNotNull(local);
        assertEquals(DataType.DATE, local.getType());
        assertTrue(local.getTarget().contains("2026"));
        assertEquals(0.55, local.getConfidence(), 0.0001);
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
        assertConv("5 ", DE, EN, DataType.INTEGER, "5"); // trailing no-break space
        assertConv("‎5", DE, EN, DataType.INTEGER, "5"); // leading LRM
        assertConv("5‏", DE, EN, DataType.INTEGER, "5"); // trailing RLM
        assertConv("5​", DE, EN, DataType.INTEGER, "5"); // trailing zero-width space
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
    public void documentedOrdinalLimits() {
        // ICU cannot parse these ordinal forms; they are intentionally out of
        // scope and must not be mis-detected (for example as "0th").
        assertEmpty("1º", Locale.ITALIAN, EN);
        assertEmpty("3:s", Locale.forLanguageTag("fi"), EN);
        assertEmpty("1,5×10³", DE, EN); // scientific notation, not in scope
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

        File xliff = new File("test/data/editor/sort/numeric-sort-demo.xliff");
        assertTrue("fixture missing: " + xliff.getAbsolutePath(), xliff.isFile());
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
