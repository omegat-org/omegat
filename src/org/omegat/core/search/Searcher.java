/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Henry Pijffers
               2009 Didier Briel
               2010 Martin Fleurke, Antonio Vilei, Alex Buloichik, Didier Briel
               2013 Aaron Madlon-Kay, Alex Buloichik
               2014 Alex Buloichik, Piotr Kulik, Aaron Madlon-Kay
               2015 Aaron Madlon-Kay
               2017-2018 Thomas Cordonnier
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

package org.omegat.core.search;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.ParseEntry;
import org.omegat.core.data.PrepareTMXEntry;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.threads.LongProcessThread;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.IParseCallback;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.gui.glossary.GlossaryEntry;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.PatternConsts;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;

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
 * @author Piotr Kulik
 * @author Thomas Cordonnier
 */
public class Searcher {

    private volatile List<SearchResultEntry> m_searchResults;
    private boolean m_preprocessResults;
    private final IProject m_project;
    /**
     * keeps track of previous results not from project memory
     */
    private Map<String, Integer> m_tmxMap;
    /**
     * HP: keeps track of previous results, to avoid duplicate entries
     */
    private Map<String, Integer> m_entryMap;
    /**
     * HP: contains a matcher for each search string (multiple if keyword search)
     */
    private List<Matcher> m_matchers;
    private Matcher m_author;

    private int m_numFinds;

    private final SearchExpression searchExpression;
    private LongProcessThread checkStop;
    private final List<SearchMatch> foundMatches = new ArrayList<>();

    // PM entries 0+
    // Only PM and TM are counted (separately) for '+X more' statistics
    private static final int ENTRY_ORIGIN_PROJECT_MEMORY = 0;
    private static final int ENTRY_ORIGIN_TRANSLATION_MEMORY = -1;
    private static final int ENTRY_ORIGIN_ORPHAN = -2;
    private static final int ENTRY_ORIGIN_ALTERNATIVE = -3;
    private static final int ENTRY_ORIGIN_GLOSSARY = -4;
    private static final int ENTRY_ORIGIN_TEXT = -5;

    /**
     * Create new searcher instance.
     *
     * @param project
     *            Current project
     */
    public Searcher(final IProject project, final SearchExpression searchExpression) {
        this.m_project = project;
        this.searchExpression = searchExpression;
    }

    /**
     * Set thread for checking interruption.
     */
    public void setThread(LongProcessThread thread) {
        checkStop = thread;
    }

    public SearchExpression getExpression() {
        return searchExpression;
    }

    /**
     * Returns list of search results
     */
    public List<SearchResultEntry> getSearchResults() {
        if (m_preprocessResults) {
            // function can be called multiple times after search
            // results preprocess should occur only one time
            m_preprocessResults = false;
            if (!searchExpression.allResults) {
                for (SearchResultEntry entry : m_searchResults) {
                    String key = entry.getSrcText() + entry.getTranslation();
                    if (entry.getEntryNum() == ENTRY_ORIGIN_TRANSLATION_MEMORY) {
                        if (m_tmxMap.containsKey(key) && (m_tmxMap.get(key) > 0)) {
                            String newPreamble = StringUtil.format(OStrings.getString("SW_FILE_AND_NR_OF_MORE"),
                                    entry.getPreamble(), m_tmxMap.get(key));
                            entry.setPreamble(newPreamble);
                        }
                    } else if (entry.getEntryNum() > ENTRY_ORIGIN_PROJECT_MEMORY) {
                        // at this stage each PM entry num is increased by 1
                        if (m_entryMap.containsKey(key) && (m_entryMap.get(key) > 0)) {
                            String newPreamble = StringUtil.isEmpty(entry.getPreamble())
                                    ? StringUtil.format(OStrings.getString("SW_NR_OF_MORE"), m_entryMap.get(key))
                                    : StringUtil.format(OStrings.getString("SW_FILE_AND_NR_OF_MORE"),
                                            entry.getPreamble(), m_entryMap.get(key));
                            entry.setPreamble(newPreamble);
                        }
                    }
                }
            }
        }
        return m_searchResults;
    }

    /**
     * Search for this.expression and return a list of results.
     *
     * @throws Exception when searching files goes wrong
     */
    public void search() throws Exception {
        String text = searchExpression.text;
        String author = searchExpression.author;

        m_searchResults = new ArrayList<>();
        m_numFinds = 0;
        // ensures that results will be preprocessed only one time
        m_preprocessResults = true;

        m_entryMap = null; // HP

        m_entryMap = new HashMap<>(); // HP

        m_tmxMap = new HashMap<>();

        // create a list of matchers
        m_matchers = new ArrayList<>();

        // determine pattern matching flags
        int flags = searchExpression.caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

        // Normalize width of search string if width insensitivity is requested.
        // Then, instead of modifying the regex, we also normalize the
        // comparison strings later on.
        if (searchExpression.widthInsensitive) {
            text = StringUtil.normalizeWidth(text);
        }

        // if exact search, just use the entire search string as a single
        // search string; otherwise, if keyword, break up the string into
        // separate words (= multiple search strings)

        switch (searchExpression.searchExpressionType) {
        case EXACT:
        default:
            // escape the search string, it's not supposed to be a regular
            // expression
            text = StaticUtils.globToRegex(text, searchExpression.spaceMatchNbsp);

            // create a matcher for the search string
            m_matchers.add(Pattern.compile(text, flags).matcher(""));
            break;
        case KEYWORD:
            // break the search string into keywords,
            // each of which is a separate search string
            Pattern.compile(" ").splitAsStream(text.trim()).filter(word -> !word.isEmpty()).map(word -> {
                String glob = StaticUtils.globToRegex(word, false);
                return Pattern.compile(glob, flags).matcher("");
            }).forEach(m_matchers::add);
            break;
        case REGEXP:
            // space match nbsp (\u00a0)
            if (searchExpression.spaceMatchNbsp) {
                text = text.replaceAll(" ", "( |\u00A0)");
                text = text.replaceAll("\\\\s", "(\\\\s|\u00A0)");
            }

            // create a matcher for the search string
            m_matchers.add(Pattern.compile(text, flags).matcher(""));
            break;
        }
        // create a matcher for the author search string
        if (searchExpression.searchExpressionType != SearchExpression.SearchExpressionType.REGEXP) {
            author = StaticUtils.globToRegex(author, searchExpression.spaceMatchNbsp);
        }

        m_author = Pattern.compile(author, flags).matcher("");


        if (searchExpression.rootDir == null) {
            // if no search directory specified, then we are
            // searching current project only
            searchProject();
        } else {
            searchFiles();
        }
    }

    // ////////////////////////////////////////////////////////////
    // internal functions

    private void addEntry(int num, String preamble, String srcPrefix, String src, String target,
            String note, String property, SearchMatch[] srcMatch, SearchMatch[] targetMatch, SearchMatch[] noteMatch, SearchMatch[] propertyMatch) {
        SearchResultEntry entry = new SearchResultEntry(num, preamble, srcPrefix,
                src, target, note, property, srcMatch, targetMatch, noteMatch, propertyMatch);
        m_searchResults.add(entry);
        m_numFinds++;
    }

    /**
     * Queue found string. Removes duplicate segments (by Henry Pijffers) except if allResults = true
     */
    private void foundString(int entryNum, String intro, String src, String target, String note, String property,
            SearchMatch[] srcMatches, SearchMatch[] targetMatches, SearchMatch[] noteMatches, SearchMatch[] propertyMatches) {
        if (m_numFinds >= searchExpression.numberOfResults) {
            return;
        }

        String key = src + target;

        if (entryNum >= ENTRY_ORIGIN_PROJECT_MEMORY) {
            addProjectMemoryEntry(entryNum, src, target, note, property, srcMatches, targetMatches, noteMatches, propertyMatches, key);
        } else if (entryNum == ENTRY_ORIGIN_TRANSLATION_MEMORY) {
            addTranslationMemoryEntry(entryNum, intro, src, target, note, property, srcMatches, targetMatches, noteMatches, propertyMatches, key);
        } else {
            addEntry(entryNum, intro, null, src, target, note, property,
                    srcMatches, targetMatches, noteMatches, propertyMatches);
        }
    }

    private void addTranslationMemoryEntry(int entryNum, String intro, String src, String target, String note, String property,
                                           SearchMatch[] srcMatches, SearchMatch[] targetMatches, SearchMatch[] noteMatches, SearchMatch[] propertyMatches, String key) {
        if (!m_tmxMap.containsKey(key) || searchExpression.allResults) {
            addEntry(entryNum, intro, null, src, target, note, property,
                    srcMatches, targetMatches, noteMatches, propertyMatches);
            if (!searchExpression.allResults) {
                // first occurrence
                m_tmxMap.put(key, 0);
            }
        } else {
            // next occurrence
            m_tmxMap.put(key, m_tmxMap.get(key) + 1);
        }
    }

    private void addProjectMemoryEntry(int entryNum, String src, String target, String note, String property,
                                       SearchMatch[] srcMatches, SearchMatch[] targetMatches, SearchMatch[] noteMatches, SearchMatch[] propertyMatches, String key) {
        if (!m_entryMap.containsKey(key) || searchExpression.allResults) {
            // HP, duplicate entry prevention
            // entries are referenced at offset 1 but stored at offset 0
            String file = searchExpression.fileNames ? getFileForEntry(entryNum + 1) : null;
            addEntry(entryNum + 1, file, (entryNum + 1) + "> ", src, target,
                    note, property, srcMatches, targetMatches, noteMatches, propertyMatches);
            if (!searchExpression.allResults) { // If we filter results
                m_entryMap.put(key, 0); // HP
            }
        } else {
            m_entryMap.put(key, m_entryMap.get(key) + 1);
        }
    }

    private void searchProject() {
        // reset the number of search hits
        m_numFinds = 0;

        try {
            searchMemory();
            searchExternalTM();
            searchGlossary();
        } catch (SearchLimitReachedException ignore) {
        }
    }

    private void searchGlossary() throws SearchLimitReachedException {
        // search the glossary, if requested
        if (searchExpression.glossary) {
            String intro = OStrings.getString("SW_GLOSSARY_RESULT");
            List<GlossaryEntry> entries = Core.getGlossaryManager().getLocalEntries();
            for (GlossaryEntry en : entries) {
                checkEntry(en.getSrcText(), en.getLocText(), null, null, null, ENTRY_ORIGIN_GLOSSARY, intro);
                // stop searching if the max. nr of hits has been reached
                if (m_numFinds >= searchExpression.numberOfResults) {
                    throw new SearchLimitReachedException();
                }
                checkStop.checkInterrupted();
            }
        }
    }

    private void searchExternalTM() throws SearchLimitReachedException {
        // search the TM, if requested
        if (searchExpression.tm) {
            // Search TM entries, unless we search for date or author.
            // They are not loaded from external TM, so skip the search in
            // that case.
            if (!searchExpression.searchAuthor && !searchExpression.searchDateAfter && !searchExpression.searchDateBefore) {
                for (Map.Entry<String, ExternalTMX> tmEn : m_project.getTransMemories().entrySet()) {
                    final String fileTM = tmEn.getKey();
                    searchEntries(tmEn.getValue().getEntries(), fileTM);
                    checkStop.checkInterrupted();
                }
                for (Map.Entry<Language, ProjectTMX> tmEn : m_project.getOtherTargetLanguageTMs().entrySet()) {
                    final Language langTM = tmEn.getKey();
                    searchEntriesAlternative(tmEn.getValue().getDefaults(), langTM.getLanguage());
                    searchEntriesAlternative(tmEn.getValue().getAlternatives(), langTM.getLanguage());
                    checkStop.checkInterrupted();
                }
            }
        }
    }

    private void searchMemory() throws SearchLimitReachedException {
        // search the Memory, if requested
        if (searchExpression.memory) {
            // search through all project entries
            List<SourceTextEntry> allEntries = m_project.getAllEntries();
            for (int i = 0; i < allEntries.size(); i++) {
                // stop searching if the max. nr of hits has been reached
                if (m_numFinds >= searchExpression.numberOfResults) {
                    throw new SearchLimitReachedException();
                }
                // get the source and translation of the next entry
                SourceTextEntry ste = allEntries.get(i);
                TMXEntry te = m_project.getTranslationInfo(ste);

                checkEntry(ste.getSrcText(), te.translation, te.note, ste.getRawProperties(), te, i, null);
                checkStop.checkInterrupted();
            }

            // search in orphaned
            if (!searchExpression.excludeOrphans) {
                m_project.iterateByDefaultTranslations(new IProject.DefaultTranslationsIterator() {
                    final String file = OStrings.getString("CT_ORPHAN_STRINGS");

                    public void iterate(String source, TMXEntry en) {
                        // stop searching if the max. nr of hits has been reached
                        if (m_numFinds >= searchExpression.numberOfResults) {
                            return;
                        }
                        checkStop.checkInterrupted();
                        if (m_project.isOrphaned(source)) {
                            checkEntry(en.source, en.translation, en.note, null, en, ENTRY_ORIGIN_ORPHAN, file);
                        }
                    }
                });
                m_project.iterateByMultipleTranslations(new IProject.MultipleTranslationsIterator() {
                    final String file = OStrings.getString("CT_ORPHAN_STRINGS");

                    public void iterate(EntryKey source, TMXEntry en) {
                        // stop searching if the max. nr of hits has been
                        // reached
                        if (m_numFinds >= searchExpression.numberOfResults) {
                            return;
                        }
                        checkStop.checkInterrupted();
                        if (m_project.isOrphaned(source)) {
                            checkEntry(en.source, en.translation, en.note, null, en, ENTRY_ORIGIN_ORPHAN, file);
                        }
                    }
                });
            }
        }
    }

    private String getFileForEntry(int i) {
        List<FileInfo> fileList = Core.getProject().getProjectFiles();
        for (FileInfo fi : fileList) {
            int first = fi.entries.get(0).entryNum();
            int last = fi.entries.get(fi.entries.size() - 1).entryNum();
            if (i >= first && i <= last) {
                return fi.filePath;
            }
        }
        return null;
    }

    /**
     * Loops over collection of TMXEntries and checks every entry.
     * If max nr of hits have been reached or search has been stopped,
     * the function stops and returns false. Else it finishes and returns true;
     *
     * @param tmEn collection of TMX Entries to check.
     * @param tmxID identifier of the TMX. E.g. the filename or language code
     * @throws SearchLimitReachedException when nr of found matches exceeds requested nr of results
     */
    private void searchEntries(Collection<PrepareTMXEntry> tmEn, final String tmxID) throws SearchLimitReachedException {
        for (PrepareTMXEntry tm : tmEn) {
            // stop searching if the max. nr of hits has been reached
            if (m_numFinds >= searchExpression.numberOfResults) {
                throw new SearchLimitReachedException();
            }

            // for alternative translations:
            // - it is not feasible to get the SourceTextEntry that matches the tm.source, so we cannot get the entryNum
            // and real translation
            // - although the 'translation' is used as 'source', we search it as translation, else we cannot show to
            // which real source it belongs
            checkEntry(tm.source, tm.translation, tm.note, null, null, ENTRY_ORIGIN_TRANSLATION_MEMORY, tmxID);

            checkStop.checkInterrupted();
        }
    }

    private void searchEntriesAlternative(Collection<TMXEntry> tmEn, final String tmxID) throws SearchLimitReachedException {
        for (TMXEntry tm : tmEn) {
            // stop searching if the max. nr of hits has been reached
            if (m_numFinds >= searchExpression.numberOfResults) {
                throw new SearchLimitReachedException();
            }

            // for alternative translations:
            // - it is not feasible to get the SourceTextEntry that matches the tm.source, so we cannot get the entryNum
            // and real translation
            // - although the 'translation' is used as 'source', we search it as translation, else we cannot show to
            // which real source it belongs
            checkEntry(tm.source, tm.translation, tm.note, null, null, ENTRY_ORIGIN_ALTERNATIVE, tmxID);

            checkStop.checkInterrupted();
        }
    }

    /**
     * Check if specified entry should be found.
     *
     * @param srcText
     *            source text
     * @param locText
     *            translation text
     * @param note
     *            note text
     * @param properties
     *            properties
     * @param entry
     *            entry. Null for external tmx entries (so we can only search for source and translation in external
     *            tmx)
     * @param entryNum
     *            entry number
     * @param intro
     *            file
     */
    protected void checkEntry(String srcText, String locText, String note,
            String[] properties, TMXEntry entry, int entryNum, String intro) {
        SearchMatch[] srcMatches = null;
        SearchMatch[] targetMatches = null;
        SearchMatch[] srcOrTargetMatches = null;
        SearchMatch[] noteMatches = null;
        SearchMatch[] propertyMatches = null;
        String firstMatchedProperty = null;

        switch (searchExpression.mode) {
        case SEARCH:
            if (searchExpression.searchTranslated && !searchExpression.searchUntranslated && locText == null) {
                return;
            }
            if (!searchExpression.searchTranslated && searchExpression.searchUntranslated && locText != null) {
                return;
            }
            if (searchExpression.searchSource && searchString(srcText)) {
                srcMatches = foundMatches.toArray(new SearchMatch[0]);
            }
            if (searchExpression.searchTarget && searchString(locText)) {
                targetMatches = foundMatches.toArray(new SearchMatch[0]);
            }
            // If
            // - we are searching both source and target
            // - we and haven't found a match in either so far
            // - we have a target
            // then we also search the concatenation of source and target per
            // https://sourceforge.net/p/omegat/feature-requests/1185/
            // We join with U+E000 (private use) to prevent spuriously matching
            // e.g. "abc" in "fab" + "cat"
            if (searchExpression.searchSource && searchExpression.searchTarget && locText != null && srcMatches == null
                    && targetMatches == null && searchString(srcText + '\ue000' + locText)) {
                srcOrTargetMatches = foundMatches.toArray(new SearchMatch[0]);
            }
            if (searchExpression.searchNotes && searchString(note)) {
                noteMatches = foundMatches.toArray(new SearchMatch[0]);
            }
            if (searchExpression.searchComments && properties != null) {
                for (int i = 1; i <= properties.length; i = i + 2) { // loop over values only, not keys.
                    if (searchString(properties[i], true)) {
                        propertyMatches = foundMatches.toArray(new SearchMatch[0]);
                        firstMatchedProperty = properties[i];
                        break;
                    }
                }
            }
            break;
        case REPLACE:
            if (searchExpression.replaceTranslated && locText != null) {
                if (searchString(locText, false)) {
                    targetMatches = foundMatches.toArray(new SearchMatch[0]);
                }
            } else if (searchExpression.replaceUntranslated && locText == null) {
                if (searchString(srcText, false)) {
                    srcMatches = foundMatches.toArray(new SearchMatch[0]);
                }
            }
            break;
        }
        // if the search expression is satisfied, report the hit
        if ((srcMatches != null || targetMatches != null || srcOrTargetMatches != null || noteMatches != null
                || propertyMatches != null)
                && (!searchExpression.searchAuthor || searchAuthor(entry))
                && (!searchExpression.searchDateBefore
                        || entry != null && entry.changeDate != 0 && entry.changeDate < searchExpression.dateBefore)
                && (!searchExpression.searchDateAfter
                        || entry != null && entry.changeDate != 0 && entry.changeDate > searchExpression.dateAfter)) {
            // found
            foundString(entryNum, intro, srcText, locText, note, firstMatchedProperty,
                    srcMatches, targetMatches, noteMatches, propertyMatches);
        }
    }

    private void searchFiles() throws IOException {
        Path root = Paths.get(searchExpression.rootDir);

        FilterMaster fm = Core.getFilterMaster();

        final SearchCallback searchCallback = new SearchCallback(m_project.getProjectProperties());

        int depth = searchExpression.recursive ? Integer.MAX_VALUE : 0;
        Files.walk(root, depth, FileVisitOption.FOLLOW_LINKS).forEach(path -> {
            String filename = path.toString();
            FileInfo fi = new FileInfo();
            // determine actual file name w/ no root path info
            fi.filePath = root.relativize(path).toString();

            searchCallback.setCurrentFile(fi);
            try {
                fm.loadFile(filename, new FilterContext(m_project.getProjectProperties()), searchCallback);
            } catch (TranslationException | IOException ex) {
                Log.log(ex);
            }
            searchCallback.fileFinished();

            checkStop.checkInterrupted();
        });
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
                String[] props, String prevSegment, String nextSegment, String path) {
            searchText(segmentSource, segmentTranslation, filename);
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // search algorithm

    /**
     * Looks for an occurrence of the search string(s) in the supplied text string.
     *
     * @param origText
     *            The text string to search in
     *
     * @return True if the text string contains all search strings
     */
    public boolean searchString(String origText) {
        return searchString(origText, true);
    }

    /**
     * Looks for an occurrence of the search string(s) in the supplied text string.
     * IF matches are found, they are added to this.foundMatches.
     *
     * @param origText
     *            The text string to search in
     * @param collapseResults
     *            True if the adjacent results should be collapsed. This can happen on search, but not on replace.
     *
     * @return True if the text string contains all search strings
     */
    public boolean searchString(String origText, boolean collapseResults) {
        if (origText == null || m_matchers == null || m_matchers.isEmpty()) {
            return false;
        }

        String text = searchExpression.widthInsensitive ? StringUtil.normalizeWidth(origText) : origText;

        foundMatches.clear();
        // check the text against all matchers
        for (Matcher matcher : m_matchers) {
            // check the text against the current matcher
            // if one of the search strings is not found, don't
            // bother looking for the rest of the search strings
            matcher.reset(text);
            if (!matcher.find()) {
                return false;
            }

            // Check if we searched a string of different length from the
            // original. If so, then we give up on highlighting this hit
            // because the offsets and length will not match. We still return
            // true so the hit will still be recorded.
            //noinspection StringEquality
            if (text != origText && text.length() != origText.length()) {
                continue;
            }
            while (true) {
                int start = matcher.start();
                int end = matcher.end();
                if (searchExpression.mode == SearchMode.REPLACE) {
                    if (searchExpression.searchExpressionType == SearchExpression.SearchExpressionType.REGEXP) {
                        if ((end == start) && (start > 0)) {
                            break; // do not replace the last occurrence of (.*)
                        }
                        String repl = searchExpression.replacement;
                        Matcher replaceMatcher = PatternConsts.REGEX_VARIABLE.matcher(repl);
                        while (replaceMatcher.find()) {
                            int varId = Integer.parseInt(replaceMatcher.group(2));
                            if (varId > matcher.groupCount()) {
                                // Group wasn't even present in search regex.
                                throw new IndexOutOfBoundsException(
                                        OStrings.getString("ST_REGEXP_REPLACEGROUP_ERROR", varId));
                            }
                            String substitution = matcher.group(varId); // yes, from source matcher!
                            if (substitution == null) {
                                // If group was present in search regex but didn't match anything,
                                // replace with empty string.
                                substitution = "";
                            }
                            substitution = substitution.replace("\\", "\\\\").replace("$", "\\$");    // avoid re-eval inside replaceCase;
                            repl = repl.substring(0, replaceMatcher.start()) + replaceMatcher.group(1) + substitution
                                    + repl.substring(replaceMatcher.end());
                            replaceMatcher.reset(repl);
                        }
                        foundMatches.add(new SearchMatch(start, end, StringUtil.replaceCase(repl,
                                m_project.getProjectProperties().getTargetLanguage().getLocale())));
                    } else {
                        foundMatches.add(new SearchMatch(start, end, searchExpression.replacement));
                    }
                } else if (end > start) {
                    // Add a match only if the matched region is not empty.
                    // We still return true so the hit will still be recorded.
                    foundMatches.add(new SearchMatch(start, end));
                }
                if (start >= text.length()) {
                    // Reached the end of the text
                    break;
                }
                // Check for additional matches (matcher will now contain data of next match).
                // Prevent infinite loop when match was zero-length by forcibly incrementing
                // next start index.
                int nextStart = end == start ? end + 1 : end;
                if (!matcher.find(nextStart)) {
                    // No more matches
                    break;
                }
            }
        }

        // if we arrive here, all search strings have been matched,
        // so this is a hit

        // merge overlapped matches for better performance to mark on UI
        Collections.sort(foundMatches);

        // We should not collapse results when doing a search/replace
        // see https://sourceforge.net/p/omegat/bugs/675/
        if (collapseResults) {
            for (int i = 1; i < foundMatches.size();) {
                SearchMatch pr = foundMatches.get(i - 1);
                SearchMatch cu = foundMatches.get(i);
                // check for overlapped
                if (pr.getStart() <= cu.getStart() && pr.getEnd() >= cu.getStart()) {
                    int end = Math.max(cu.getEnd(), pr.getEnd());
                    // leave only one region
                    pr = new SearchMatch(pr.getStart(), end, pr.getReplacement());
                    foundMatches.set(i - 1, pr);
                    foundMatches.remove(i);
                } else {
                    i++;
                }
            }
        }
        return true;
    }

    public List<SearchMatch> getFoundMatches() {
        return foundMatches;
    }

    /**
     * Looks for an occurrence of the author search string in the supplied TMXEntry.
     *
     * @param te
     *            The TMXEntry to search in
     *
     * @return True if the text string contains the search string
     */
    private boolean searchAuthor(TMXEntry te) {
        if (te == null || m_author == null) {
            return false;
        }

        if (m_author.pattern().pattern().equals("")) {
            // Handle search for null author.
            return te.changer == null && te.creator == null;
        }

        if (te.changer != null) {
            m_author.reset(te.changer);
            if (m_author.find()) {
                return true;
            }
        }

        if (te.creator != null) {
            m_author.reset(te.creator);
            if (m_author.find()) {
                return true;
            }
        }

        return false;
    }

    // ///////////////////////////////////////////////////////////////
    // interface used by FileHandlers

    public void searchText(String seg, String translation, String filename) {
        // don't look further if the max. nr of hits has been reached
        if (m_numFinds >= searchExpression.numberOfResults) {
            return;
        }

        checkStop.checkInterrupted();

        if (!searchExpression.searchTranslated) {
            if (translation == null) {
                return;
            }
        }
        if (searchString(seg)) {
            SearchMatch[] matches = foundMatches.toArray(new SearchMatch[0]);
            // found a match - do something about it
            foundString(ENTRY_ORIGIN_TEXT, filename, seg, null, null, null, matches, null, null, null);
        }
    }

    @SuppressWarnings("serial")
    private static class SearchLimitReachedException extends Exception {

    }
}
