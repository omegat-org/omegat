/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.omegat.core.Core;
import org.omegat.core.data.CommandThread;
import org.omegat.core.data.StringEntry;
import org.omegat.core.glossary.GlossaryEntry;
import org.omegat.core.matching.Tokenizer;
import org.omegat.util.Token;

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
 */
public class FindGlossaryThread extends Thread {
    private final GlossaryTextArea glossaryController;

    /**
     * Entry which processed currently.
     * 
     * If entry in controller was changed, it means user was moved to other
     * entry, and there is no sense to continue.
     */
    private final StringEntry processedEntry;

    private List<GlossaryEntry> result = new ArrayList<GlossaryEntry>();

    public FindGlossaryThread(final GlossaryTextArea glossaryController,
            final StringEntry entry) {
        this.glossaryController = glossaryController;
        this.processedEntry = entry;
    }

    @Override
    public void run() {
        for (GlossaryEntry glosEntry : CommandThread.core.m_glossary
                .getGlossaryEntries()) {
            if (glossaryController.processedEntry != processedEntry) {
                // Processed entry changed, because user moved to other entry.
                // I.e. we don't need to find and display data for old entry.
                return;
            }

            String glosStr = glosEntry.getSrcText();
            Token[] glosTokens = Core.getTokenizer().tokenizeTextWithCache(
                    glosStr);
            int glosTokensN = glosTokens.length;
            if (glosTokensN == 0)
                continue;

            Token[] strTokens = Core.getTokenizer().tokenizeTextWithCache(
                    processedEntry.getSrcText());
            if (Tokenizer.isContainsAll(strTokens, glosTokens)) {
                result.add(glosEntry);
            }
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (glossaryController.processedEntry == processedEntry) {
                    glossaryController.setGlossaryEntries(result);
                }
            }
        });
    }
}
