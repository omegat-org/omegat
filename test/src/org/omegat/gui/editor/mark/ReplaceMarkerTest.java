/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2024 Hiroshi Miura.
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.omegat.gui.editor.mark;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.search.SearchExpression;
import org.omegat.core.search.SearchMatch;
import org.omegat.core.search.Searcher;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.IEditorFilter;
import org.omegat.gui.editor.MockEditor;
import org.omegat.gui.editor.filter.ReplaceFilter;

/**
 * @author Hiroshi Miura
 */
public class ReplaceMarkerTest extends MarkerTestBase  {
    final String sourceText = "source text";
    final String replaceText = "text";
    private SourceTextEntry ste;

    @Before
    public void preUp() {
        EntryKey key = new EntryKey("file", sourceText, "id", "prev", "next", "path");
        ste = new SourceTextEntry(key, 1, new String[0], sourceText, Collections.emptyList());
        IEditor editor = new ReplaceMarkerMockEditor();
        TestCoreInitializer.initEditor(editor);
        Core.setProject(new NotLoadedProject() {
            @Override
            public List<SourceTextEntry> getAllEntries() {
                return Collections.singletonList(ste);
            }
        });
    }

    @Test
    public void testReplaceMarker() throws Exception {
        IMarker marker = new ReplaceMarker();
        List<Mark> result = marker.getMarksForEntry(ste, sourceText, sourceText, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(7, result.get(0).startOffset);
        assertEquals(11, result.get(0).endOffset);
        assertEquals("TRANSLATION", result.get(0).entryPart.toString());
    }

    class ReplaceMarkerMockEditor extends MockEditor {
        ReplaceMarkerMockEditor() {
            super(editorSettings);
        }

        @Override
        public IEditorFilter getFilter() {
            SearchExpression s = new SearchExpression();
            s.searchExpressionType = SearchExpression.SearchExpressionType.KEYWORD;
            return new ReplaceFilter(Collections.emptyList(), new MockSearcher(sourceText, replaceText));
        }
    }

    static class MockSearcher extends Searcher {
        private final String sourceText;
        private final String replaceText;

        MockSearcher(String sourceText, String replaceText) {
            super(null, null);
            this.sourceText = sourceText;
            this.replaceText = replaceText;
        }

        @Override
        public boolean searchString(String translationText, boolean b) {
            return true;
        }

        @Override
        public List<SearchMatch> getFoundMatches() {
            int s = sourceText.indexOf(replaceText);
            return Collections.singletonList(new SearchMatch(s, s + replaceText.length()));
        }
    }

}
