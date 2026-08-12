/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay
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

package org.omegat.core.tagvalidation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.tagvalidation.ErrorReport.TagError;
import org.omegat.util.Preferences;
import org.omegat.util.TagUtil.Tag;
import org.omegat.util.TestPreferencesInitializer;

/**
 * @author Aaron Madlon-Kay
 */
public class TagValidationTest {

    @Test
    public void testOrderedTagValidation() {

        // No errors
        String[] srcTags = {"<g0>", "<g1>", "</g1>", "</g0>"};
        String[] locTags = {"<g0>", "<g1>", "</g1>", "</g0>"};
        ErrorReport report = new ErrorReport();
        TagValidation.inspectOrderedTags(getList(srcTags), getList(locTags), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.isEmpty());

        // No errors, html 'input' element (<i1>) is not a start tag, but single.
        String[] srcTags1 = {"<s0>", "<i1>", "</s0>"};
        String[] locTags1 = {"<s0>", "<i1>", "</s0>"};
        report = new ErrorReport();
        TagValidation.inspectOrderedTags(getList(srcTags1), getList(locTags1), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.isEmpty());

        // Missing </g0>
        String[] srcTags2 = {"<g0>", "<g1>", "</g1>", "</g0>"};
        String[] locTags2 = {"<g0>", "<g1>", "</g1>"};
        report = new ErrorReport();
        TagValidation.inspectOrderedTags(getList(srcTags2), getList(locTags2), false, report);
        assertTrue(report.srcErrors.get(tag("</g0>")) == TagError.MISSING);
        assertTrue(report.transErrors.get(tag("<g0>")) == TagError.ORPHANED);

        // Count mismatch </g0>
        String[] srcTags3 = {"<g0>", "<g1>", "</g1>", "</g0>"};
        String[] locTags3 = {"<g0>", "<g1>", "</g1>", "</g0>", "</g0>"};
        report = new ErrorReport();
        TagValidation.inspectOrderedTags(getList(srcTags3), getList(locTags3), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(tag("</g0>")) == TagError.DUPLICATE);

        // Extraneous <x2/>
        String[] srcTags4 = {"<g0>", "<g1>", "</g1>", "</g0>"};
        String[] locTags4 = {"<g0>", "<g1>", "<x2/>", "</g1>", "</g0>"};
        report = new ErrorReport();
        TagValidation.inspectOrderedTags(getList(srcTags4), getList(locTags4), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(tag("<x2/>")) == TagError.EXTRANEOUS);

        // Bad nesting <g1></g1>
        String[] srcTags5 = {"<g0>", "</g0>", "<g1>", "</g1>"};
        String[] locTags5 = {"<g0>", "</g0>", "</g1>", "<g1>"};
        report = new ErrorReport();
        TagValidation.inspectOrderedTags(getList(srcTags5), getList(locTags5), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(tag("<g1>")) == TagError.MALFORMED);
        assertTrue(report.transErrors.get(tag("</g1>")) == TagError.MALFORMED);

        // Out of order <g1></g1>
        String[] srcTags6 = {"<g0>", "</g0>", "<g1>", "</g1>"};
        String[] locTags6 = {"<g1>", "</g1>", "<g0>", "</g0>"};
        report = new ErrorReport();
        TagValidation.inspectOrderedTags(getList(srcTags6), getList(locTags6), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(tag("<g1>")) == TagError.ORDER);
        assertTrue(report.transErrors.get(tag("</g1>")) == TagError.ORDER);

        // Out of order <g1></g1> with loose ordering
        report = new ErrorReport();
        TagValidation.inspectOrderedTags(getList(srcTags6), getList(locTags6), true, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.srcErrors.isEmpty());
    }

    @Test
    public void testUnorderedTagValidation() {
        // No errors
        String[] srcTags = {"a", "b", "c", "d"};
        String[] locTags = {"a", "b", "c", "d"};
        ErrorReport report = new ErrorReport();
        TagValidation.inspectUnorderedTags(getList(srcTags), getList(locTags), report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.isEmpty());

        // Missing d
        String[] srcTags2 = {"a", "b", "c", "d"};
        String[] locTags2 = {"a", "b", "c"};
        report = new ErrorReport();
        TagValidation.inspectUnorderedTags(getList(srcTags2), getList(locTags2), report);
        assertTrue(report.srcErrors.get(tag("d")) == TagError.MISSING);
        assertTrue(report.transErrors.isEmpty());

        // No error for unordered: Count mismatch d
        String[] srcTags3 = {"a", "b", "c", "d"};
        String[] locTags3 = {"a", "b", "c", "d", "d"};
        report = new ErrorReport();
        TagValidation.inspectUnorderedTags(getList(srcTags3), getList(locTags3), report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.isEmpty());

        // Extraneous e
        String[] srcTags4 = {"a", "b", "c", "d"};
        String[] locTags4 = {"a", "b", "e", "c", "d"};
        report = new ErrorReport();
        TagValidation.inspectOrderedTags(getList(srcTags4), getList(locTags4), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(tag("e")) == TagError.EXTRANEOUS);
    }

    @Test
    public void testPrintfTagValidation() {

        // No error
        ErrorReport report = new ErrorReport("Foo %s bar %d", "Foo %s bar %d");
        TagValidation.inspectPrintfVariables(true, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.isEmpty());

        // Missing %d
        report = new ErrorReport("Foo %s bar %d", "Foo %s bar");
        TagValidation.inspectPrintfVariables(true, report);
        assertTrue(report.srcErrors.get(new Tag(4, "%s")) == TagError.UNSPECIFIED);
        assertTrue(report.srcErrors.get(new Tag(11, "%d")) == TagError.UNSPECIFIED);
        assertTrue(report.transErrors.get(new Tag(4, "%s")) == TagError.UNSPECIFIED);

        // Extraneous %d
        report = new ErrorReport("Foo %s bar %d", "Foo %s bar %d baz %d");
        TagValidation.inspectPrintfVariables(true, report);
        assertTrue(report.srcErrors.get(new Tag(4, "%s")) == TagError.UNSPECIFIED);
        assertTrue(report.srcErrors.get(new Tag(11, "%d")) == TagError.UNSPECIFIED);
        assertTrue(report.transErrors.get(new Tag(4, "%s")) == TagError.UNSPECIFIED);
        assertTrue(report.transErrors.get(new Tag(11, "%d")) == TagError.UNSPECIFIED);
        assertTrue(report.transErrors.get(new Tag(18, "%d")) == TagError.UNSPECIFIED);
    }

    @Test
    public void testRemovePattern() throws Exception {
        TestPreferencesInitializer.init();
        Preferences.setPreference(Preferences.CHECK_REMOVE_PATTERN, "foo");

        // No error
        ErrorReport report = new ErrorReport("foo bar baz", "bar baz");
        TagValidation.inspectRemovePattern(report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.isEmpty());

        // Extraneous foo
        report = new ErrorReport("foo bar baz", "foo bar baz");
        TagValidation.inspectRemovePattern(report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(new Tag(0, "foo")) == TagError.EXTRANEOUS);
    }

    /**
     * Numerals match by value across notations: a source numeral missing
     * verbatim from the translation is satisfied by an equal value in any
     * notation, while a changed value keeps its error and verbatim pairings
     * stay with the ordinary check. Numeral tags leave the ordered tag check
     * when satisfied; numerals outside the tag machinery report their
     * missing values directly.
     */
    @Test
    public void testNumeralsCheckedByValue() throws Exception {
        String khmer = "១២៣៤៥៦";
        String khmerSource = "បានកត់ត្រា ១២៣៤៥៦ គ្រួសារ";

        // The same value in plain digits satisfies the Khmer source numeral.
        ErrorReport report = new ErrorReport(khmerSource, "erfasste 123456 Haushalte");
        List<Tag> srcTags = tagsAt(khmerSource, khmer);
        TagValidation.inspectNumerals(srcTags, new ArrayList<>(), report);
        assertTrue(srcTags.isEmpty());
        assertTrue(report.isEmpty());

        // A changed value stays with the ordered tag check.
        report = new ErrorReport(khmerSource, "erfasste 123457 Haushalte");
        srcTags = tagsAt(khmerSource, khmer);
        TagValidation.inspectNumerals(srcTags, new ArrayList<>(), report);
        assertEquals(1, srcTags.size());

        // A verbatim pairing is not consumed by value matching, and a second
        // source numeral of the same value still finds no counterpart.
        String twoYears = "ឆ្នាំ 1984 និង ១៩៨៤";
        String twoYearsTranslation = "nennt 1984 Tonkrüge";
        report = new ErrorReport(twoYears, twoYearsTranslation);
        srcTags = tagsAt(twoYears, "1984", "១៩៨៤");
        TagValidation.inspectNumerals(srcTags, tagsAt(twoYearsTranslation, "1984"), report);
        assertEquals(2, srcTags.size());

        // Ordinary tags are never value-matched, and their digits do not
        // join the numeral check.
        String tagged = "siehe <x1/> hier";
        report = new ErrorReport(tagged, "ohne Tag");
        srcTags = tagsAt(tagged, "<x1/>");
        TagValidation.inspectNumerals(srcTags, new ArrayList<>(), report);
        assertEquals(1, srcTags.size());
        assertTrue(report.isEmpty());

        // The compatibility spelling satisfies the numeral as well: a Roman
        // code point written out with Latin letters, a vulgar fraction
        // written with a plain slash.
        String chapter = "Chapter Ⅻ opens";
        report = new ErrorReport(chapter, "Kapitel XII beginnt");
        srcTags = tagsAt(chapter, "Ⅻ");
        TagValidation.inspectNumerals(srcTags, new ArrayList<>(), report);
        assertTrue(srcTags.isEmpty());

        String recipe = "add ¼ measure";
        report = new ErrorReport(recipe, "1/4 Tasse Honig");
        srcTags = tagsAt(recipe, "¼");
        TagValidation.inspectNumerals(srcTags, new ArrayList<>(), report);
        assertTrue(srcTags.isEmpty());

        // A compatibility spelling embedded in a different number does not
        // satisfy the numeral: twelve is not part of thirteen, a quarter is
        // not part of eleven forty-seconds.
        report = new ErrorReport(chapter, "Kapitel XIII beginnt");
        srcTags = tagsAt(chapter, "Ⅻ");
        TagValidation.inspectNumerals(srcTags, new ArrayList<>(), report);
        assertEquals(1, srcTags.size());

        report = new ErrorReport(recipe, "genau 11/42 Anteile");
        srcTags = tagsAt(recipe, "¼");
        TagValidation.inspectNumerals(srcTags, new ArrayList<>(), report);
        assertEquals(1, srcTags.size());
    }

    /**
     * The numeral check does not depend on the tag machinery: a source
     * numeral outside every tag list reports its missing value itself, so a
     * mistranslated number surfaces whatever the script and whatever the
     * custom-tag pattern.
     */
    @Test
    public void testNumeralsOutsideTagsAreCheckedAllTheSame() throws Exception {
        String khmerSource = "បានកត់ត្រា ១២៣៤៥៦ គ្រួសារ";

        // An equal value in any notation satisfies the check.
        ErrorReport report = new ErrorReport(khmerSource, "erfasste 123456 Haushalte");
        TagValidation.inspectNumerals(new ArrayList<>(), new ArrayList<>(), report);
        assertTrue(report.isEmpty());

        // A changed value reports the source numeral as missing.
        report = new ErrorReport(khmerSource, "erfasste 123457 Haushalte");
        TagValidation.inspectNumerals(new ArrayList<>(), new ArrayList<>(), report);
        assertEquals(TagError.MISSING,
                report.srcErrors.get(new Tag(khmerSource.indexOf("១២៣៤៥៦"), "១២៣៤៥៦")));

        // Plain digit runs are checked without any tag list too: both
        // dropped values of this translation are reported.
        report = new ErrorReport("5 Kisten und 3 Säcke", "einige Kisten und Säcke");
        TagValidation.inspectNumerals(new ArrayList<>(), new ArrayList<>(), report);
        assertEquals(2, report.srcErrors.size());

        // The compatibility spelling satisfies the plain-text numeral.
        report = new ErrorReport("Chapter Ⅻ opens", "Kapitel XII beginnt");
        TagValidation.inspectNumerals(new ArrayList<>(), new ArrayList<>(), report);
        assertTrue(report.isEmpty());
    }

    private static List<Tag> tagsAt(String text, String... tokens) {
        List<Tag> list = new ArrayList<>();
        for (String token : tokens) {
            list.add(new Tag(text.indexOf(token), token));
        }
        return list;
    }

    /**
     * A decimal or grouped spelling means the same value: a vulgar fraction
     * may become 0,5 or 0.25, a plain thousand may come back grouped, while
     * a decimal of a different value keeps the error. An enumeration is not
     * a decimal: its numbers keep their individual checks.
     */
    @Test
    public void testSeparatorSpellingsCountByValue() throws Exception {
        ErrorReport report = new ErrorReport("add ¼ measure", "füge 0,25 Maß hinzu");
        TagValidation.inspectNumerals(new ArrayList<>(), new ArrayList<>(), report);
        assertTrue(report.isEmpty());

        report = new ErrorReport("misst 1000 Meter", "misst 1.000 Meter");
        TagValidation.inspectNumerals(new ArrayList<>(), new ArrayList<>(), report);
        assertTrue(report.isEmpty());

        report = new ErrorReport("misst 0,5 Liter", "misst ½ Liter");
        TagValidation.inspectNumerals(new ArrayList<>(), new ArrayList<>(), report);
        assertTrue(report.isEmpty());

        report = new ErrorReport("add ¼ measure", "füge 0,3 Maß hinzu");
        TagValidation.inspectNumerals(new ArrayList<>(), new ArrayList<>(), report);
        assertEquals(1, report.srcErrors.size());

        // 5, 6 and 8 are three numbers, not a decimal: the unmatched 7
        // reports alone.
        report = new ErrorReport("Seiten 5, 6, 7", "Seiten 5, 6, 8");
        TagValidation.inspectNumerals(new ArrayList<>(), new ArrayList<>(), report);
        assertEquals(TagError.MISSING, report.srcErrors.get(new Tag(13, "7")));
        assertEquals(1, report.srcErrors.size());
    }

    /**
     * A compatibility spelling inside an ordinary word is not a numeral:
     * the Roman twelve hides in taxiing, but the plane does not satisfy the
     * chapter number.
     */
    @Test
    public void testCompatibilitySpellingNeedsWordBoundaries() throws Exception {
        ErrorReport report = new ErrorReport("chapter ⅻ opens", "the taxiing plane");
        TagValidation.inspectNumerals(new ArrayList<>(), new ArrayList<>(), report);
        assertEquals(TagError.MISSING, report.srcErrors.get(new Tag("chapter ".length(), "ⅻ")));
    }

    /**
     * The full pipeline through {@link TagValidation#inspectOmegaTTags}:
     * custom-tag numerals pair verbatim first and by value beyond that
     * budget, digits inside an OmegaT tag stay out of the numeral check,
     * and the digit runs of a decimal number do not demand verbatim
     * pairings of their own.
     */
    @Test
    public void testInspectOmegaTTagsChecksNumeralsEndToEnd() throws Exception {
        TestPreferencesInitializer.init();

        // A protected numeral rewritten in another notation raises no
        // issue; the digits inside the OmegaT tag are no numerals.
        SourceTextEntry ste = entry("Im Jahr 1984 <x12/>", "1984", "<x12/>");
        ErrorReport report = new ErrorReport(ste.getSrcText(), "ⅯⅭⅯⅬⅩⅩⅩⅣ年 <x12/>");
        TagValidation.inspectOmegaTTags(ste, report);
        assertTrue(report.isEmpty());

        // A changed value keeps its error.
        ste = entry("Im Jahr 1984", "1984");
        report = new ErrorReport(ste.getSrcText(), "ⅯⅭⅯⅬⅩⅩⅩⅤ年");
        TagValidation.inspectOmegaTTags(ste, report);
        assertEquals(TagError.MISSING, report.srcErrors.get(new Tag("Im Jahr ".length(), "1984")));

        // One verbatim pairing consumes one source tag; the second tag of
        // the same spelling is satisfied by an equal value in another
        // notation.
        ste = entry("12 Kisten und 12 Säcke", "12");
        report = new ErrorReport(ste.getSrcText(), "12 Äpfel und ١٢ Birnen");
        TagValidation.inspectOmegaTTags(ste, report);
        assertTrue(report.isEmpty());

        // The digit runs of a decimal number leave the verbatim tag check
        // to the value comparison of the number as a whole.
        ste = entry("misst 0,5 Liter", "0", "5");
        report = new ErrorReport(ste.getSrcText(), "misst ½ Liter");
        TagValidation.inspectOmegaTTags(ste, report);
        assertTrue(report.isEmpty());
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

    protected static List<Tag> getList(String[] array) {
        List<Tag> list = new ArrayList<Tag>();
        for (String item : array) {
            list.add(tag(item));
        }
        return list;
    }

    private static Tag tag(String tag) {
        return new Tag(-1, tag);
    }
}
