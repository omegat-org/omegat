/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2009 Wildrich Fourie
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
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.omegat.core.Core;
import org.omegat.core.data.StringEntry;
import org.omegat.core.matching.ITokenizer;
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
 * @author Wildrich Fourie
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
        // computer source entry tokens
        Token[] strTokens = Core.getTokenizer().tokenizeWords(
                processedEntry.getSrcText(), ITokenizer.StemmingMode.GLOSSARY);

        List<GlossaryEntry> entries = glossaryController.manager.getGlossaryEntries();
        if (entries != null) {
            for (GlossaryEntry glosEntry : entries) {
                if (glossaryController.processedEntry != processedEntry) {
                    // Processed entry changed, because user moved to other
                    // entry.
                    // I.e. we don't need to find and display data for old
                    // entry.
                    return;
                }

                // computer glossary entry tokens
                String glosStr = glosEntry.getSrcText();
                Token[] glosTokens = Core.getTokenizer().tokenizeWords(glosStr,
                        ITokenizer.StemmingMode.GLOSSARY);
                int glosTokensN = glosTokens.length;
                if (glosTokensN == 0)
                    continue;

                if (Tokenizer.isContainsAll(strTokens, glosTokens)) {
                    result.add(glosEntry);
                }
            }
        }

        // After the matched entries have been tokenized and listed.
        // We remove the duplicates and combine the synonyms.
        // Then the matches are ordered to display the biggest matches first.
        result = FilterGlossary(result);
        for(int z=0; z < result.size(); z++)
            for(int x=z+1; x < result.size()-1; x++)
            {
                GlossaryEntry zEntry = (GlossaryEntry)result.get(z);
                GlossaryEntry xEntry = (GlossaryEntry)result.get(x);

                if(xEntry.getSrcText().length() > zEntry.getSrcText().length())
                {
                    Object temp = result.get(x);
                    result.set(x, result.get(z));
                    result.set(z,(GlossaryEntry) temp);
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

    
    // Filter out doubles, and combine synonyms.
    private List FilterGlossary(List glosEntries) {
        List compactEntries = new LinkedList();
        int entriesNum = glosEntries.size();

        if (entriesNum != 0) {
            if (entriesNum == 1) {
                compactEntries.add(glosEntries.get(0));
            } else {
                for (int h = 0; h < entriesNum; h++) {
                    List LocList = new LinkedList();

                    GlossaryEntry nowEntry = (GlossaryEntry) glosEntries.get(h);

                    // If the entry isn't empty split the seperate terms.
                    if (!nowEntry.getSrcText().equals("")) {                    // NOI18N
                        String[] SrcSplit = nowEntry.getLocText().split(", ");  // NOI18N
                        for (int b = 0; b < SrcSplit.length; b++) {
                            LocList.add(SrcSplit[b]);
                        }

                        for (int g = h + 1; g < entriesNum; g++) {
                            GlossaryEntry thenEntry = 
                                    (GlossaryEntry) glosEntries.get(g);

                            if ( (nowEntry.getSrcText().
                                    equals(thenEntry.getSrcText())) &&
                                  (thenEntry.getCommentText(). 
                                     equals(nowEntry.getCommentText())) 
                                     // We only merge entries with equal 
                                     // (or none) comments
                               )
                            {
                                String[] LocArray = 
                                        thenEntry.getLocText().split(", ");     // NOI18N
                                for (int d = 0; d < LocArray.length; d++) {
                                    if (!LocList.contains(LocArray[d])) {
                                        LocList.add(LocArray[d]);
                                        nowEntry.setLocText
                                                (nowEntry.getLocText() + 
                                                ", " + LocArray[d]);            // NOI18N
                                    }
                                }

                                GlossaryEntry replaceEntry = 
                                        new GlossaryEntry("", "", "");          // NOI18N
                                glosEntries.set(g, replaceEntry);
                            }
                        }

                        compactEntries.add(nowEntry);
                    }
                }
            }
        }

        // Clean the GlossaryEntries
        for(int c = glosEntries.size() - 1; c >= 0; c--)
        {
            if (((GlossaryEntry) glosEntries.get(c)).getSrcText().equals(""))
            {
                glosEntries.remove(c);
            }
        }

        return compactEntries;
    }
}
