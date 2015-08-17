/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.tagvalidation;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.tagvalidation.ErrorReport.TagError;
import org.omegat.util.Preferences;
import org.omegat.util.TagUtil.Tag;

/**
 * @author Aaron Madlon-Kay
 */
public class TagValidationTest extends TestCase {
    
    public void testOrderedTagValidation() {
        
        // No errors
        String[] srcTags = {"<g0>", "<g1>", "</g1>", "</g0>"};
        String[] locTags = {"<g0>", "<g1>", "</g1>", "</g0>"};
        ErrorReport report = new ErrorReport(null, null);
        TagValidation.inspectOrderedTags(getList(srcTags), getList(locTags), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.isEmpty());
        
        // Missing </g0>
        String[] srcTags2 = {"<g0>", "<g1>", "</g1>", "</g0>"};
        String[] locTags2 = {"<g0>", "<g1>", "</g1>"};
        report = new ErrorReport(null, null);
        TagValidation.inspectOrderedTags(getList(srcTags2), getList(locTags2), false, report);
        assertTrue(report.srcErrors.get(tag("</g0>")) == TagError.MISSING);
        assertTrue(report.transErrors.get(tag("<g0>")) == TagError.ORPHANED);
        
        // Count mismatch </g0>
        String[] srcTags3 = {"<g0>", "<g1>", "</g1>", "</g0>"};
        String[] locTags3 = {"<g0>", "<g1>", "</g1>", "</g0>", "</g0>"};
        report = new ErrorReport(null, null);
        TagValidation.inspectOrderedTags(getList(srcTags3), getList(locTags3), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(tag("</g0>")) == TagError.DUPLICATE);
        
        // Extraneous <x2/>
        String[] srcTags4 = {"<g0>", "<g1>", "</g1>", "</g0>"};
        String[] locTags4 = {"<g0>", "<g1>", "<x2/>", "</g1>", "</g0>"};
        report = new ErrorReport(null, null);
        TagValidation.inspectOrderedTags(getList(srcTags4), getList(locTags4), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(tag("<x2/>")) == TagError.EXTRANEOUS);
        
        // Bad nesting <g1></g1>
        String[] srcTags5 = {"<g0>", "</g0>", "<g1>", "</g1>"};
        String[] locTags5 = {"<g0>", "</g0>", "</g1>", "<g1>"};
        report = new ErrorReport(null, null);
        TagValidation.inspectOrderedTags(getList(srcTags5), getList(locTags5), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(tag("<g1>")) == TagError.MALFORMED);
        assertTrue(report.transErrors.get(tag("</g1>")) == TagError.MALFORMED);
        
        // Out of order <g1></g1>
        String[] srcTags6 = {"<g0>", "</g0>", "<g1>", "</g1>"};
        String[] locTags6 = {"<g1>", "</g1>", "<g0>", "</g0>"};
        report = new ErrorReport(null, null);
        TagValidation.inspectOrderedTags(getList(srcTags6), getList(locTags6), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(tag("<g1>")) == TagError.ORDER);
        assertTrue(report.transErrors.get(tag("</g1>")) == TagError.ORDER);
        
        // Out of order <g1></g1> with loose ordering
        report = new ErrorReport(null, null);
        TagValidation.inspectOrderedTags(getList(srcTags6), getList(locTags6), true, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.srcErrors.isEmpty());
    }
    
    public void testUnorderedTagValidation() {
        // No errors
        String[] srcTags = {"a", "b", "c", "d"};
        String[] locTags = {"a", "b", "c", "d"};
        ErrorReport report = new ErrorReport(null, null);
        TagValidation.inspectUnorderedTags(getList(srcTags), getList(locTags), report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.isEmpty());
        
        // Missing d
        String[] srcTags2 = {"a", "b", "c", "d"};
        String[] locTags2 = {"a", "b", "c"};
        report = new ErrorReport(null, null);
        TagValidation.inspectUnorderedTags(getList(srcTags2), getList(locTags2), report);
        assertTrue(report.srcErrors.get(tag("d")) == TagError.MISSING);
        assertTrue(report.transErrors.isEmpty());
        
        // No error for unordered: Count mismatch d
        String[] srcTags3 = {"a", "b", "c", "d"};
        String[] locTags3 = {"a", "b", "c", "d", "d"};
        report = new ErrorReport(null, null);
        TagValidation.inspectUnorderedTags(getList(srcTags3), getList(locTags3), report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.isEmpty());
        
        // Extraneous e
        String[] srcTags4 = {"a", "b", "c", "d"};
        String[] locTags4 = {"a", "b", "e", "c", "d"};
        report = new ErrorReport(null, null);
        TagValidation.inspectOrderedTags(getList(srcTags4), getList(locTags4), false, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(tag("e")) == TagError.EXTRANEOUS);
    }
    
    public void testPrintfTagValidation() {
        
        // No error
        ErrorReport report = makeReport("Foo %s bar %d", "Foo %s bar %d");
        TagValidation.inspectPrintfVariables(true, report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.isEmpty());
        
        // Missing %d
        report = makeReport("Foo %s bar %d", "Foo %s bar");
        TagValidation.inspectPrintfVariables(true, report);
        assertTrue(report.srcErrors.get(new Tag(4, "%s")) == TagError.UNSPECIFIED);
        assertTrue(report.srcErrors.get(new Tag(11, "%d")) == TagError.UNSPECIFIED);
        assertTrue(report.transErrors.get(new Tag(4, "%s")) == TagError.UNSPECIFIED);
        
        // Extraneous %d
        report = makeReport("Foo %s bar %d", "Foo %s bar %d baz %d");
        TagValidation.inspectPrintfVariables(true, report);
        assertTrue(report.srcErrors.get(new Tag(4, "%s")) == TagError.UNSPECIFIED);
        assertTrue(report.srcErrors.get(new Tag(11, "%d")) == TagError.UNSPECIFIED);
        assertTrue(report.transErrors.get(new Tag(4, "%s")) == TagError.UNSPECIFIED);
        assertTrue(report.transErrors.get(new Tag(11, "%d")) == TagError.UNSPECIFIED);
        assertTrue(report.transErrors.get(new Tag(18, "%d")) == TagError.UNSPECIFIED);
    }
    
    public void testRemovePattern() {
        Preferences.setPreference(Preferences.CHECK_REMOVE_PATTERN, "foo");
        
        // No error
        ErrorReport report = makeReport("foo bar baz", "bar baz");
        TagValidation.inspectRemovePattern(report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.isEmpty());
        
        // Extraneous foo
        report = makeReport("foo bar baz", "foo bar baz");
        TagValidation.inspectRemovePattern(report);
        assertTrue(report.srcErrors.isEmpty());
        assertTrue(report.transErrors.get(new Tag(0, "foo")) == TagError.EXTRANEOUS);
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
    
    private ErrorReport makeReport(String source, String translation) {
        EntryKey key = new EntryKey(null, source, null, null, null, null);
        SourceTextEntry ste = new SourceTextEntry(key, 0, null, null, new ArrayList<ProtectedPart>());
        ErrorReport report = new ErrorReport(ste, translation);
        return report;
    }
}
