/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2009 Wildrich Fourie, Didier Briel, Alex Buloichik
               2013 Aaron Madlon-Kay, Alex Buloichik
               2015 Didier Briel, Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.gui.glossary;

import java.util.Collections;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.tokenizer.ITokenizer;

/**
 * Class for find glossary entries for current entry in editor.
 *
 * This process looks up the source string entries, and find matched glossary
 * entries.
 * <p>
 * Test cases wheter a glossary entry matches a string entry text:
 * <ul>
 * <li>"Edit" vs "Editing" - doesn't match
 * <li>"Old Line" vs "Hold Line" - doesn't match
 * <li>"Some Text" vs "There was some text there" - OK!
 * <li>"Edit" vs "Editing the edit" - matches OK!
 * <li>"Edit" vs "Edit" - matches OK!
 * </ul>
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Wildrich Fourie
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public class FindGlossaryThread extends EntryInfoSearchThread<List<GlossaryEntry>> {

    private final SourceTextEntry ste;
    private final GlossaryManager manager;

    public FindGlossaryThread(final GlossaryTextArea pane, final SourceTextEntry newEntry,
            final GlossaryManager manager) {
        super(pane, newEntry);
        this.ste = newEntry;
        this.manager = manager;
    }

    @Override
    protected List<GlossaryEntry> search() {

        ITokenizer tok = Core.getProject().getSourceTokenizer();
        if (tok == null) {
            return Collections.emptyList();
        }

        List<GlossaryEntry> entries = manager.getGlossaryEntries(ste.getSrcText());
        if (entries == null) {
            return Collections.emptyList();
        }

        GlossarySearcher searcher = new GlossarySearcher(tok,
                Core.getProject().getProjectProperties().getSourceLanguage()) {
            @Override
            protected void checkCancelled() {
                checkEntryChanged();
            }
        };

        return searcher.searchSourceMatches(ste, entries);
    }
}
