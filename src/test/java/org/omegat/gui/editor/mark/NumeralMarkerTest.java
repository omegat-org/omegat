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

package org.omegat.gui.editor.mark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.TestPreferencesInitializer;
import org.omegat.util.gui.Styles;

/**
 * @author stephan.pakebusch at zollsoft.de
 */
public class NumeralMarkerTest {

    @Before
    public final void setUp() throws Exception {
        TestPreferencesInitializer.init();
        Styles.EditorColor.COLOR_NUMERALS.setColor(new Color(0x0000ff));
    }

    @After
    public final void tearDown() {
        Styles.EditorColor.COLOR_NUMERALS.setColor(null);
    }

    /** Without a color of its own, the marker paints nothing at all. */
    @Test
    public void testWithoutAColorNothingIsPainted() throws Exception {
        Styles.EditorColor.COLOR_NUMERALS.setColor(null);
        assertNull(new NumeralMarker().getMarksForEntry(null, "misst 1984 Meter", null, true));
    }

    /**
     * Every numeral of the entry is underlined, in the source and in the
     * translation, whatever the writing system.
     */
    @Test
    public void testNumeralsOfBothSidesAreMarked() throws Exception {
        String source = "បានកត់ត្រា ១២៣៤៥៦ គ្រួសារ";
        String translation = "erfasste 123456 Haushalte";
        List<Mark> marks = new NumeralMarker().getMarksForEntry(null, source, translation, true);

        assertEquals(2, marks.size());
        Mark sourceMark = marks.get(0);
        assertEquals(Mark.ENTRY_PART.SOURCE, sourceMark.entryPart);
        assertEquals(source.indexOf("១២៣៤៥៦"), sourceMark.startOffset);
        assertEquals(source.indexOf("១២៣៤៥៦") + "១២៣៤៥៦".length(), sourceMark.endOffset);
        Mark translationMark = marks.get(1);
        assertEquals(Mark.ENTRY_PART.TRANSLATION, translationMark.entryPart);
        assertEquals(translation.indexOf("123456"), translationMark.startOffset);
    }

    /** A numeral inside a tag belongs to the tag checks, not to this marker. */
    @Test
    public void testNumeralsInsideTagsAreNotMarked() throws Exception {
        List<Mark> marks = new NumeralMarker().getMarksForEntry(null, "siehe <x12/> hier: 7",
                "voir <x12/> ici : 7", true);

        assertEquals(2, marks.size());
        assertEquals("siehe <x12/> hier: ".length(), marks.get(0).startOffset);
    }

    /** A separator-written number is one mark, not one mark per digit run. */
    @Test
    public void testSeparatedNumbersMarkAsOneToken() throws Exception {
        String source = "misst 1.000 Meter";
        List<Mark> marks = new NumeralMarker().getMarksForEntry(null, source, null, true);

        assertEquals(1, marks.size());
        assertEquals(source.indexOf("1.000"), marks.get(0).startOffset);
        assertEquals(source.indexOf("1.000") + "1.000".length(), marks.get(0).endOffset);
        assertNull("Both readings of 1.000 are possible, so no tooltip takes sides",
                marks.get(0).toolTipText);
    }

    /**
     * A numeral whose spelling reads differently from plain digits carries
     * its value as the tooltip; a plain digit run needs none.
     */
    @Test
    public void testTooltipCarriesTheValueWhenItReadsDifferently() throws Exception {
        List<Mark> marks = new NumeralMarker().getMarksForEntry(null, "ជំពូក ១២", "Kapitel 12", true);

        assertEquals(2, marks.size());
        assertEquals("= 12", marks.get(0).toolTipText);
        assertNull(marks.get(1).toolTipText);

        marks = new NumeralMarker().getMarksForEntry(null, "misst 0,5 Liter", null, true);
        assertEquals(1, marks.size());
        assertEquals("= 1/2", marks.get(0).toolTipText);
    }

    /** Text without numerals yields no marks. */
    @Test
    public void testPlainTextYieldsNoMarks() throws Exception {
        assertEquals(0, new NumeralMarker().getMarksForEntry(null, "ohne Zahlen", "without numbers", true)
                .size());
    }

    /**
     * A number the custom tag pattern turned into a protected part is still
     * a numeral to this marker - the numeral check compares it by value -
     * while the digits inside a real OmegaT tag stay with the tag checks.
     */
    @Test
    public void testCustomPatternNumbersAreMarkedRealTagsAreNot() throws Exception {
        String source = "Im Jahr 1984 <x12/>";
        SourceTextEntry ste = entry(source, "1984", "<x12/>");
        List<Mark> marks = new NumeralMarker().getMarksForEntry(ste, source, null, true);

        assertEquals(1, marks.size());
        assertEquals(source.indexOf("1984"), marks.get(0).startOffset);
        assertEquals(source.indexOf("1984") + "1984".length(), marks.get(0).endOffset);
    }

    /**
     * The digit runs of a decimal number, protected one by one under the
     * default custom tag pattern, do not break the number's single mark.
     */
    @Test
    public void testProtectedDigitRunsKeepTheDecimalAsOneMark() throws Exception {
        String source = "misst 0,5 Liter";
        SourceTextEntry ste = entry(source, "0", "5");
        List<Mark> marks = new NumeralMarker().getMarksForEntry(ste, source, null, true);

        assertEquals(1, marks.size());
        assertEquals(source.indexOf("0,5"), marks.get(0).startOffset);
        assertEquals(source.indexOf("0,5") + "0,5".length(), marks.get(0).endOffset);
    }

    /** A numeral inside a protected part that is no numeral stays unmarked. */
    @Test
    public void testNumbersInsideNonNumeralProtectedPartsAreNotMarked() throws Exception {
        String source = "siehe %1$s hier: 7";
        SourceTextEntry ste = entry(source, "%1$s");
        List<Mark> marks = new NumeralMarker().getMarksForEntry(ste, source, null, true);

        assertEquals(1, marks.size());
        assertEquals(source.indexOf(": 7") + 2, marks.get(0).startOffset);
    }

    private static SourceTextEntry entry(String source, String... protectedTexts) {
        List<ProtectedPart> protectedParts = new ArrayList<>();
        for (String text : protectedTexts) {
            ProtectedPart part = new ProtectedPart();
            part.setTextInSourceSegment(text);
            part.setDetailsFromSourceFile(text);
            protectedParts.add(part);
        }
        return new SourceTextEntry(new EntryKey("file.txt", source, null, null, null, null), 1, null,
                null, protectedParts);
    }
}
