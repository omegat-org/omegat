/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.gui.multtrans;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.data.EntryKey;
import org.omegat.core.data.IProject;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.gui.common.EntryInfoSearchThread;

/**
 * Class for iterate by all translation and find entries for multiple translations pane.
 * 
 * There is no sense to store multiple translations into map with source text key, because full iterate is
 * enough fast since we just check strings for equals.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MultipleTransFindThread extends EntryInfoSearchThread<List<MultipleTransFoundEntry>> {
    private final IProject project;

    private final String sourceText;

    public MultipleTransFindThread(final MultipleTransPane pane, final IProject project,
            final SourceTextEntry entry) {
        super(pane, entry);
        this.project = project;
        this.sourceText = entry.getSrcText();
    }

    protected List<MultipleTransFoundEntry> search() throws Exception {
        final List<MultipleTransFoundEntry> result = new ArrayList<MultipleTransFoundEntry>();
        project.iterateByDefaultTranslations(new IProject.DefaultTranslationsIterator() {
            public void iterate(String source, TMXEntry trans) {
                if (sourceText.equals(source)) {
                    result.add(new MultipleTransFoundEntry(source, trans));
                }
            }
        });
        project.iterateByMultipleTranslations(new IProject.MultipleTranslationsIterator() {
            public void iterate(EntryKey source, TMXEntry trans) {
                if (sourceText.equals(source.sourceText)) {
                    result.add(new MultipleTransFoundEntry(source, trans));
                }
            }
        });

        return result;
    }
}
