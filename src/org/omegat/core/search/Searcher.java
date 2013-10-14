/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Henry Pijffers
               2009 Didier Briel
               2010 Martin Fleurke, Antonio Vilei, Alex Buloichik, Didier Briel
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

package org.omegat.core.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.omegat.core.Core;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.ParseEntry;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

/**
 * This class implements search functionality. It is non-reentrant: each searcher instance must be used by a
 * single thread.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Antonio Vilei
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class Searcher {

    /**
     * Create new searcher instance.
     * 
     * @param project
     *            Current project
     */
    public Searcher(final IProject project, final ISearchCheckStop stopCallback) {
        m_project = project;
        this.stopCallback = stopCallback;
    }

    /**
     * Search for an expression and return a list of results.
     * 
     * @param expression
     *            what to search for (search text and options)
     * @param maxResults
     *            maximum number of search results
     * @return list of search results
     */
    public List<SearchResultEntry> getSearchResults(SearchExpression expression) throws TranslationException,
            PatternSyntaxException, IOException {
        m_searchExpression = expression;
        String text = expression.text;
        String author = expression.author;

        m_searchResults = new ArrayList<SearchResultEntry>();
        m_numFinds = 0;

        m_entrySet = null; // HP

        m_maxResults = expression.numberOfResults;

        m_searchDir = expression.rootDir;
        m_searchRecursive = expression.recursive;
        m_allResults = expression.allResults;
        m_searchSource = expression.searchSource;
        m_searchTarget = expression.searchTarget;
        m_searchNotes = expression.searchNotes;
        m_searchAuthor = expression.searchAuthor;
        m_searchDateAfter = expression.searchDateAfter;
        m_searchDateBefore = expression.searchDateBefore;

        m_entrySet = new HashSet<String>(); // HP

        // create a list of matchers
        m_matchers = new ArrayList<Matcher>();

        // determine pattern matching flags
        int flags = expression.caseSensitive ? 0 : Pattern.CASE_INSENSITIVE + Pattern.UNICODE_CASE;

        // if exact search, just use the entire search string as a single
        // search string; otherwise, if keyword, break up the string into
        // separate words (= multiple search strings)

        if (expression.exact) {
            // escape the search string, it's not supposed to be a regular
            // expression
            text = StaticUtils.escapeNonRegex(text, false);

            // create a matcher for the search string
            m_matchers.add(Pattern.compile(text, flags).matcher(""));
        } else if (expression.regex) {
            // create a matcher for the search string
            m_matchers.add(Pattern.compile(text, flags).matcher(""));
        } else {
            // break the search string into keywords,
            // each of which is a separate search string
            text = text.trim();
            if (text.length() > 0) {
                int wordStart = 0;
                while (wordStart < text.length()) {
                    // get the location of the next space
                    int spacePos = text.indexOf(' ', wordStart);

                    // get the next word
                    String word = (spacePos == -1) // last word reached
                    ? text.substring(wordStart, text.length()).trim()
                            : text.substring(wordStart, spacePos).trim();

                    if (word.length() > 0) {
                        // escape the word, if it's not supposed to be a regular
                        // expression
                        if (!expression.regex)
                            word = StaticUtils.escapeNonRegex(word, false);

                        // create a matcher for the word
                        m_matchers.add(Pattern.compile(word, flags).matcher(""));
                    }

                    // set the position for the start of the next word
                    wordStart = (spacePos == -1) ? text.length() : spacePos + 1;
                }
            }
        }
        // create a matcher for the author search string
        if (!expression.regex)
            author = StaticUtils.escapeNonRegex(author, false);

        m_author = Pattern.compile(author, flags).matcher("");

        m_dateBefore = expression.dateBefore;
        m_dateAfter = expression.dateAfter;

        if (m_searchDir == null) {
            // if no search directory specified, then we are
            // searching current project only
            searchProject();
        } else {
            searchFiles();
        }

        return (m_searchResults);
    }

    // ////////////////////////////////////////////////////////////
    // internal functions

    private void addEntry(int num, String preamble, String srcPrefix, String src, String target,
            SearchMatch[] srcMatch, SearchMatch[] targetMatch) {
        SearchResultEntry entry = new SearchResultEntry(num, preamble, srcPrefix, src, target, srcMatch,
                targetMatch);
        m_searchResults.add(entry);
        m_numFinds++;
    }

    /**
     * Queue found string. Removes duplicate segments (by Henry Pijffers) except if m_allResults = true
     */
    private void foundString(int entryNum, String intro, String src, String target, SearchMatch[] srcMatches,
            SearchMatch[] targetMatches) {
        if (m_numFinds >= m_maxResults) {
            return;
        }

        if (entryNum >= 0) {
            if (!m_entrySet.contains(src + target) || m_allResults) {
                // HP, duplicate entry prevention
                // entries are referenced at offset 1 but stored at offset 0
                addEntry(entryNum + 1, null, (entryNum + 1) + "> ", src, target, srcMatches, targetMatches);
                if (!m_allResults) // If we filter results
                    m_entrySet.add(src + target); // HP
            }
        } else {
            addEntry(entryNum, intro, null, src, target, srcMatches, targetMatches);
        }
    }

    private void searchProject() {
        // reset the number of search hits
        m_numFinds = 0;

        // search the Memory, if requested
        if (m_searchExpression.memory) {
            // search through all project entries
            IProject dataEngine = m_project;
            for (int i = 0; i < m_project.getAllEntries().size(); i++) {
                // stop searching if the max. nr of hits has been reached
                if (m_numFinds >= m_maxResults) {
                    return;
                }
                // get the source and translation of the next entry
                SourceTextEntry ste = dataEngine.getAllEntries().get(i);
                TMXEntry te = m_project.getTranslationInfo(ste);

                checkEntry(ste.getSrcText(), te.translation, te, i, null);
                if (stopCallback.isStopped()) {
                    return;
                }
            }

            // search in orphaned
            
            m_project.iterateByDefaultTranslations(new IProject.DefaultTranslationsIterator() {
                final String file = OStrings.getString("CT_ORPHAN_STRINGS");

                public void iterate(String source, TMXEntry en) {
                    // stop searching if the max. nr of hits has been reached
                    if (m_numFinds >= m_maxResults) {
                        return;
                    }
                    if (stopCallback.isStopped()) {
                        return;
                    }
                    if (m_project.isOrphaned(source)) {
                        checkEntry(en.source, en.translation, en, -1, file);
                    }
                }
            });

            m_project.iterateByMultipleTranslations(new IProject.MultipleTranslationsIterator() {
                final String file = OStrings.getString("CT_ORPHAN_STRINGS");

                public void iterate(EntryKey source, TMXEntry en) {
                    // stop searching if the max. nr of hits has been reached
                    if (m_numFinds >= m_maxResults) {
                        return;
                    }
                    if (stopCallback.isStopped()) {
                        return;
                    }
                    if (m_project.isOrphaned(source)) {
                        checkEntry(en.source, en.translation, en, -1, file);
                    }
                }
            });
        }

        // search the TM, if requested
        if (m_searchExpression.tm) {
            // Search TM entries, unless we search for date or author.
            // They are not loaded from external TM, so skip the search in
            // that case.
            if (!m_searchAuthor && !m_searchDateAfter && !m_searchDateBefore) {
                for (Map.Entry<String, ExternalTMX> tmEn : m_project.getTransMemories().entrySet()) {
                    final String fileTM = tmEn.getKey();
                    if (!searchEntries(tmEn.getValue().getEntries(), fileTM, false)) return;
                    if (stopCallback.isStopped()) {
                        return;
                    }
                }
                for (Map.Entry<Language, ProjectTMX> tmEn : m_project.getOtherTargetLanguageTMs().entrySet()) {
                    final Language langTM = tmEn.getKey();
                    if (!searchEntries(tmEn.getValue().getDefaults(), langTM.getLanguage(), true)) return;
                    if (!searchEntries(tmEn.getValue().getAlternatives(), langTM.getLanguage(), true)) return;
                    if (stopCallback.isStopped()) {
                        return;
                    }
                }
            }
        }

        // search the TM, if requested
        if (m_searchExpression.glossary) {
            String intro = OStrings.getString("SW_GLOSSARY_RESULT");
            List<GlossaryEntry> entries = Core.getGlossaryManager().getGlossaryEntries(m_searchExpression.text);
            for (GlossaryEntry en : entries) {
                checkEntry(en.getSrcText(), en.getLocText(), null, -1, intro);
                // stop searching if the max. nr of hits has been reached
                if (m_numFinds >= m_maxResults) {
                    return;
                }
                if (stopCallback.isStopped()) {
                    return;
                }
            }
        }
    }

    /**
     * Loops over collection of TMXEntries and checks every entry.
     * If max nr of hits have been reached or serach has been stopped,
     * the function stops and returns false. Else it finishes and returns true;
     * 
     * @param tmEn collection of TMX Entries to check.
     * @param tmxID identifier of the TMX. E.g. the filename or language code
     * @return true when finished and all entries checked,
     *         false when search has stopped before all entries have been checked.
     */
    private boolean searchEntries(Collection<TMXEntry> tmEn,
        final String tmxID, boolean isAlternativeSource) {
        for (TMXEntry tm : tmEn) {
            // stop searching if the max. nr of hits has been
            // reached
            if (m_numFinds >= m_maxResults) {
                return false;
            }

            //for alternative translations:
            //- it is not feasible to get the sourcetextentry that matches the tm.source, so we cannot get the entryNum and real translation
            //- although the 'trnalsation' is used as 'source', we search it as translation, else we cannot show to which real source it belongs
            checkEntry(tm.source, tm.translation, null, -1, tmxID);

            if (stopCallback.isStopped()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if specified entry should be found.
     * 
     * @param srcText
     *            source text
     * @param locText
     *            translation text
     * @param entry
     *            entry. Null for external tmx entries (so we can only search for source and translation in external tmx)
     * @param entryNum
     *            entry number
     * @param intro
     *            file
     */
    protected void checkEntry(String srcText, String locText, TMXEntry entry, int entryNum, String intro) {
        SearchMatch[] srcMatches = null;
        if (m_searchSource) {
            if (searchString(srcText)) {
                srcMatches = foundMatches.toArray(new SearchMatch[foundMatches.size()]);
            }
        }
        SearchMatch[] targetMatches = null;
        if (m_searchTarget) {
            if (searchString(locText)) {
                targetMatches = foundMatches.toArray(new SearchMatch[foundMatches.size()]);
            }
        }
        SearchMatch[] noteMatches = null;
        if (m_searchNotes) {
            if (entry != null && searchString(entry.note)) {
                noteMatches = foundMatches.toArray(new SearchMatch[foundMatches.size()]);
            }
        }

        if (m_searchExpression.searchTranslatedOnly) {
            if (locText == null) {
                return;
            }
        }
        // if the search expression is satisfied, report the hit
        if ((srcMatches != null || targetMatches != null || noteMatches != null)
                && (!m_searchAuthor || entry != null && searchAuthor(entry))
                && (!m_searchDateBefore || entry != null && entry.changeDate != 0
                        && entry.changeDate < m_dateBefore)
                && (!m_searchDateAfter || entry != null && entry.changeDate != 0
                        && entry.changeDate > m_dateAfter)) {
            // found
            foundString(entryNum, intro, srcText, locText, srcMatches, targetMatches);
        }
    }

    private void searchFiles() throws IOException, TranslationException {
        List<String> fileList = new ArrayList<String>(256);
        if (!m_searchDir.endsWith(File.separator))
            m_searchDir += File.separator;
        StaticUtils.buildFileList(fileList, new File(m_searchDir), m_searchRecursive);

        FilterMaster fm = Core.getFilterMaster();

        SearchCallback searchCallback = new SearchCallback(m_project.getProjectProperties());
        
        for (String filename : fileList) {
            FileInfo fi = new FileInfo();
            // determine actual file name w/ no root path info
            fi.filePath = filename.substring(m_searchDir.length());

            searchCallback.setCurrentFile(fi);
            fm.loadFile(filename, new FilterContext(m_project.getProjectProperties()), searchCallback);
            searchCallback.fileFinished();
            
            if (stopCallback.isStopped()) {
                return;
            }
        }
    }

    protected class SearchCallback extends ParseEntry implements IParseCallback {
        private String filename;

        public SearchCallback(ProjectProperties config) {
            super(config);
        }

        @Override
        public void setCurrentFile(FileInfo fi) {
            super.setCurrentFile(fi);
            filename = fi.filePath;
        }

        @Override
        protected void fileFinished() {
            super.fileFinished();
        }

        @Override
        protected void addSegment(String id, short segmentIndex, String segmentSource,
                List<ProtectedPart> protectedParts, String segmentTranslation, boolean segmentTranslationFuzzy,
                String comment, String prevSegment, String nextSegment, String path) {
            searchText(segmentSource, segmentTranslation, filename);
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // search algorithm

    /**
     * Looks for an occurrence of the search string(s) in the supplied text string.
     * 
     * @param text
     *            The text string to search in
     * 
     * @return True if the text string contains all search strings
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    private boolean searchString(String text) {
        if (text == null || m_matchers == null || m_matchers.isEmpty())
            return false;

        foundMatches.clear();
        // check the text against all matchers
        for (Matcher matcher : m_matchers) {
            // check the text against the current matcher
            // if one of the search strings is not found, don't
            // bother looking for the rest of the search strings
            matcher.reset(text);
            if (!matcher.find())
                return false;

            while (true) {
                foundMatches.add(new SearchMatch(matcher.start(), matcher.end() - matcher.start()));
                int pos = matcher.start();
                if (pos >= text.length() || !matcher.find(pos + 1)) {
                    break;
                }
            }
        }

        // if we arrive here, all search strings have been matched,
        // so this is a hit

        // merge overlapped matches for better performance to mark on UI
        Collections.sort(foundMatches);
        for (int i = 1; i < foundMatches.size();) {
            SearchMatch pr = foundMatches.get(i - 1);
            SearchMatch cu = foundMatches.get(i);
            // check for overlapped
            if (pr.start <= cu.start && pr.start + pr.length >= cu.start) {
                int end = Math.max(cu.start + cu.length, pr.start + pr.length);
                pr.length = end - pr.start;
                // leave only one region
                foundMatches.remove(i);
            } else {
                i++;
            }
        }

        return true;
    }

    /**
     * Looks for an occurrence of the author search string in the supplied text string.
     * 
     * @param author
     *            The text string to search in
     * 
     * @return True if the text string contains the search string
     */
    private boolean searchAuthor(TMXEntry te) {
        if (te == null || m_author == null)
            return false;
        String author = te.changer;
        
        if (author == null) {
            // Handle search for null author.
            if (te.translation != null && m_author.pattern().pattern().equals("")) {
                return true;
            }
            return false;
        } else if (m_author.pattern().pattern().equals("")) {
            // Don't match non-null authors when searching for null author.
            return false;
        }

        // check the text against the author matcher
        m_author.reset(author);
        if (!m_author.find())
            return false;

        // if we arrive here, the search string has been matched,
        // so this is a hit
        return true;
    }

    // ///////////////////////////////////////////////////////////////
    // interface used by FileHandlers

    public void searchText(String seg, String translation, String filename) {
        // don't look further if the max. nr of hits has been reached
        if (m_numFinds >= m_maxResults)
            return;

        if (stopCallback.isStopped()) {
            return;
        }

        if (m_searchExpression.searchTranslatedOnly) {
            if (translation == null) {
                return;
            }
        }
        if (searchString(seg)) {
            SearchMatch[] matches = foundMatches.toArray(new SearchMatch[foundMatches.size()]);
            // found a match - do something about it
            foundString(-1, filename, seg, null, matches, null);
        }
    }

    public interface ISearchCheckStop {
        boolean isStopped();
    }

    private List<SearchResultEntry> m_searchResults;
    private IProject m_project;
    private String m_searchDir;
    private boolean m_searchRecursive;
    private boolean m_allResults;
    private boolean m_searchSource;
    private boolean m_searchTarget;
    private boolean m_searchNotes;
    private boolean m_searchAuthor;
    private boolean m_searchDateAfter;
    private boolean m_searchDateBefore;
    private Set<String> m_entrySet; // HP: keeps track of previous results, to
                                    // avoid duplicate entries
    private List<Matcher> m_matchers; // HP: contains a matcher for each search
                                      // string
    // (multiple if keyword search)
    private Matcher m_author;
    private long m_dateBefore;
    private long m_dateAfter;

    private int m_numFinds;
    private int m_maxResults;

    private SearchExpression m_searchExpression;

    private final ISearchCheckStop stopCallback;
    private final List<SearchMatch> foundMatches = new ArrayList<SearchMatch>();
}
