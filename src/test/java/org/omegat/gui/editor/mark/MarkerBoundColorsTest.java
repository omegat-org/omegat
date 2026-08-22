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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.PatternConsts;
import org.omegat.util.TagUtil;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.Styles.EditorColor;

/**
 * Marks that color text must bind their foreground to the palette entry:
 * segment text already carries a bound state color, and painting lets a
 * binding win over a plain foreground attribute, so a mark with only a
 * static color would be shadowed by the state color of the segment.
 *
 * @author Stephan Pakebusch
 */
public class MarkerBoundColorsTest extends MarkerTestBase {

    @Before
    public void preUp() {
        TestCoreInitializer.initEditor(editor);
    }

    @Test
    public void testProtectedPartsMarkBindsTheTagColor() throws Exception {
        String sourceText = "source %s text.";
        List<ProtectedPart> protectedParts = TagUtil.applyCustomProtectedParts(sourceText,
                PatternConsts.PRINTF_VARS, null);
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText, protectedParts);

        List<Mark> result = new ProtectedPartsMarker().getMarksForEntry(ste, sourceText, null, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNotNull(result.getFirst().attributes);
        assertEquals(EditorColor.COLOR_PLACEHOLDER.getColor(),
                Styles.resolveBoundForeground(result.getFirst().attributes));
    }

    @Test
    public void testRemoveTagMarkBindsTheTargetColorOnly() throws Exception {
        RemoveTagMarker marker = new RemoveTagMarker();
        // set directly instead of via the project change event to avoid
        // interference among test cases
        marker.pattern = Pattern.compile("%remove");
        String sourceText = "plain source";
        String translationText = "target %remove text";
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        SourceTextEntry ste = new SourceTextEntry(key, 1, new String[0], sourceText,
                Collections.emptyList());

        List<Mark> result = marker.getMarksForEntry(ste, sourceText, translationText, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Mark.ENTRY_PART.TRANSLATION, result.getFirst().entryPart);
        assertNotNull(result.getFirst().attributes);
        assertEquals(EditorColor.COLOR_REMOVETEXT_TARGET.getColor(),
                Styles.resolveBoundForeground(result.getFirst().attributes));

        // in the source the matched text is only italicized, never colored
        String matchingSource = "source %remove";
        EntryKey sourceKey = new EntryKey("file", matchingSource, "id2", "prev", "next", "path");
        SourceTextEntry sourceSte = new SourceTextEntry(sourceKey, 2, new String[0], matchingSource,
                Collections.emptyList());
        List<Mark> sourceOnly = marker.getMarksForEntry(sourceSte, matchingSource, null, true);
        assertNotNull(sourceOnly);
        assertEquals(1, sourceOnly.size());
        assertNull(Styles.resolveBoundForeground(sourceOnly.getFirst().attributes));
    }
}
