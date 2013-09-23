/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Alex Buloichik
               2009 Wildrich Fourie, Didier Briel, Alex Buloichik
               2013 Aaron Madlon-Kay
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

package org.omegat.gui.glossary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.common.EntryInfoSearchThread;
import org.omegat.tokenizer.DefaultTokenizer;
import org.omegat.tokenizer.ITokenizer;
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
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 */
public class FindGlossaryThread extends EntryInfoSearchThread<List<GlossaryEntry>> {

    private final String src;

    private List<GlossaryEntry> result = new ArrayList<GlossaryEntry>();

    private final GlossaryManager manager;

    public FindGlossaryThread(final GlossaryTextArea pane, final SourceTextEntry newEntry,
            final GlossaryManager manager) {
        super(pane, newEntry);
        src = newEntry.getSrcText();
        this.manager = manager;
    }

    @Override
    protected List<GlossaryEntry> search() {
        ITokenizer tok = Core.getProject().getSourceTokenizer();
        if (tok == null) {
            return null;
        }

        // computer source entry tokens
        Token[] strTokens = tok.tokenizeWords(src, ITokenizer.StemmingMode.GLOSSARY);

        List<GlossaryEntry> entries = manager.getGlossaryEntries(src);
        if (entries != null) {
            for (GlossaryEntry glosEntry : entries) {
                checkEntryChanged();

                // computer glossary entry tokens
                String glosStr = glosEntry.getSrcText();
                Token[] glosTokens = tok.tokenizeWords(glosStr, ITokenizer.StemmingMode.GLOSSARY);
                int glosTokensN = glosTokens.length;
                if (glosTokensN == 0)
                    continue;

                if (DefaultTokenizer.isContainsAll(strTokens, glosTokens)) {
                    result.add(glosEntry);
                }
            }
        }

        // After the matched entries have been tokenized and listed.
        // We reorder entries: 1) by priority, 2) by length, 3) by alphabet
        // Then remove the duplicates and combine the synonyms.
        sortGlossaryEntries(result);
        result = filterGlossary(result);
        return result;
    }

    static void sortGlossaryEntries(List<GlossaryEntry> entries) {
        Collections.sort(entries, new Comparator<GlossaryEntry>() {
            public int compare(GlossaryEntry o1, GlossaryEntry o2) {
                int p1 = o1.getPriority() ? 1 : 2;
                int p2 = o2.getPriority() ? 1 : 2;
                int c = p1 - p2;
                if (c == 0) {
                    c = o2.getSrcText().length() - o1.getSrcText().length();
                }
                if (c == 0) {
                    c = o1.getSrcText().compareToIgnoreCase(o2.getSrcText());
                }
                if (c == 0) {
                    c = o1.getSrcText().compareTo(o2.getSrcText());
                }
                if (c == 0) {
                    c = o1.getLocText().compareToIgnoreCase(o2.getLocText());
                }
                return c;
            }
        });
    }

    static List<GlossaryEntry> filterGlossary(List<GlossaryEntry> result) {
        // First check that entries exist in the list.
        if (result.size() == 0)
            return result;

        List<GlossaryEntry> returnList = new LinkedList<GlossaryEntry>();

        // The default replace entry
        GlossaryEntry replaceEntry = new GlossaryEntry("", "", "", false);

        // ... Remove the duplicates from the list
        // ..............................
        boolean removedDuplicate = false;
        for (int i = 0; i < result.size(); i++) {
            GlossaryEntry nowEntry = result.get(i);

            if (nowEntry.getSrcText().equals(""))
                continue;

            for (int j = i + 1; j < result.size(); j++) {
                GlossaryEntry thenEntry = result.get(j);

                if (thenEntry.getSrcText().equals(""))
                    continue;

                // If the Entries are exactely the same, insert a blank entry.
                if (nowEntry.getSrcText().equals(thenEntry.getSrcText()))
                    if (nowEntry.getLocText().equals(thenEntry.getLocText()))
                        if (nowEntry.getCommentText().equals(thenEntry.getCommentText())) {
                            result.set(j, replaceEntry);
                            removedDuplicate = true;
                        }
            }
        }
        // ......................................................................

        // -- Remove the blank entries from the list
        // ----------------------------
        if (removedDuplicate) {
            Iterator<GlossaryEntry> myIter = result.iterator();
            List<GlossaryEntry> newList = new LinkedList<GlossaryEntry>();

            while (myIter.hasNext()) {
                GlossaryEntry checkEntry = myIter.next();
                if (checkEntry.getSrcText().equals("") || checkEntry.getLocText().equals(""))
                    myIter.remove();
                else
                    newList.add(checkEntry);
            }

            result = newList;
        }
        // ----------------------------------------------------------------------

        // ~~ Group items with same scrTxt
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        for (int i = 0; i < result.size(); i++) {
            List<GlossaryEntry> srcList = new LinkedList<GlossaryEntry>();
            GlossaryEntry nowEntry = result.get(i);

            if (nowEntry.getSrcText().equals(""))
                continue;

            srcList.add(nowEntry);

            for (int j = i + 1; j < result.size(); j++) {
                GlossaryEntry thenEntry = result.get(j);

                // Double check, needed?
                if (thenEntry.getSrcText().equals(""))
                    continue;

                if (nowEntry.getSrcText().equals(thenEntry.getSrcText())) {
                    srcList.add(thenEntry);
                    result.set(j, replaceEntry);
                }
            }

            // == Sort items with same locTxt
            // ==============================
            List<GlossaryEntry> sortList = new LinkedList<GlossaryEntry>();
            if (srcList.size() > 1) {
                for (int k = 0; k < srcList.size(); k++) {
                    GlossaryEntry srcNow = srcList.get(k);

                    if (srcNow.getSrcText().equals(""))
                        continue;

                    sortList.add(srcNow);

                    for (int l = k + 1; l < srcList.size(); l++) {
                        GlossaryEntry srcThen = srcList.get(l);

                        if (srcThen.getSrcText().equals(""))
                            continue;

                        if (srcNow.getLocText().equals(srcThen.getLocText())) {
                            sortList.add(srcThen);
                            srcList.set(l, replaceEntry);
                        }
                    }
                }
            } else {
                sortList = srcList;
            }
            // ==================================================================

            // == Now put the sortedList together
            // ===============================
            String srcTxt = sortList.get(0).getSrcText();
            ArrayList<String> locTxts = new ArrayList<String>();
            ArrayList<String> comTxts = new ArrayList<String>();
            ArrayList<Boolean> prios = new ArrayList<Boolean>();

            for (GlossaryEntry e : sortList) {
                for (String s : e.getLocTerms(false)) {
                    locTxts.add(s);
                }
                for (String s : e.getComments()) {
                    comTxts.add(s);
                }
                for (boolean s : e.getPriorities()) {
                    prios.add(s);
                }
            }
            boolean[] priorities = new boolean[prios.size()];
            for (int j = 0; j < prios.size(); j++) {
                priorities[j] = prios.get(j);
            }

            GlossaryEntry combineEntry = new GlossaryEntry(srcTxt, locTxts.toArray(new String[0]),
                    comTxts.toArray(new String[0]), priorities);
            returnList.add(combineEntry);
            // ==================================================================
        }
        // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
        return returnList;
    }
}
