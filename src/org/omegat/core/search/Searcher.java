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
               2017-2022 Thomas Cordonnier
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

package org.omegat.core.search;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.core.data.EntryKey;
import org.omegat.core.data.ExternalTMX;
import org.omegat.core.data.IProject;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.data.ITMXEntry;
import org.omegat.core.data.ParseEntry;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.threads.CancellationToken;
import org.omegat.core.threads.LongProcessInterruptedException;
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
 * This class implements search functionality. It is non-reentrant: each
 * searcher instance must be used by a single thread.
 * <p>
 * THREAD SAFETY: This class is NOT thread-safe for concurrent access. However,
 * it supports the following safe usage pattern:
 * <ol>
 * <li>Thread A creates Searcher instance and calls search()</li>
 * <li>Thread A completes search() execution completely</li>
 * <li>Thread B can safely call getSearchResults() and other getter methods
 * after Thread A completes</li>
 * </ol>
 * The key requirement is that getSearchResults() and other result access
 * methods are only called AFTER search() completes, and no concurrent access
 * occurs during search() execution.
 * <p>
 * VISIBILITY: The searchCompleted flag ensures proper visibility of search
 * results between threads.
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
@NullMarked
public class Searcher {

    private final List<SearchResultEntry> searchResults = new ArrayList<>();
    private boolean preprocessResults;
    private final IProject project;
    /**
     * keeps track of previous results not from project memory
     */
    private final Map<String, Integer> tmxMap = new HashMap<>();
    /**
     * HP: keeps track of previous results, to avoid duplicate entries
     */
    private final Map<String, Integer> entryMap = new HashMap<>();
    /**
     * HP: contains a matcher for each search string (multiple if keyword
     * search)
     */
    private final List<Matcher> matchers = new ArrayList<>();

    private int numFinds;

    private final SearchExpression searchExpression;
    private final List<SearchMatch> foundMatches = new ArrayList<>();

    /**
     * Volatile flag to ensure proper visibility of search completion between
     * threads. This provides happens-before relationship for safe result
     * access.
     */
    private volatile boolean searchCompleted = false;

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
        this.project = project;
        this.searchExpression = searchExpression;
    }

    /**
     * Used to cooperatively stop long searches.
     * If null, the search is not cancellable (legacy behavior).
     */
    private volatile CancellationToken cancellationToken;

    /**
     * Set cancellation token for checking stop requests.
     * Preferred over {@link #setThread(LongProcessThread)}.
     */
    public void setCancellationToken(CancellationToken token) {
        this.cancellationToken = token;
    }

    /**
     * Set thread for checking interruption.
     */
    @Deprecated(since = "6.1.0")
    public void setThread(LongProcessThread thread) {
        this.cancellationToken = new CancellationToken() {
            @Override
            public boolean isCancelled() {
                return thread.isInterrupted();
            }

            @Override
            public void throwIfCancelled() {
                if (isCancelled()) {
                    throw new RuntimeException(new LongProcessInterruptedException());
                }
            }
        };
    }

    /**
     * Helper for internal loops: call this at safe points.
     */
    private void checkInterrupted() {
        CancellationToken token = this.cancellationToken;
        if (token.isCancelled() || Thread.currentThread().isInterrupted()) {
            // Use whatever your Searcher currently uses to abort (exception, return, etc.).
            // If Searcher already throws a specific stop exception, throw that instead.
            throw new RuntimeException(new LongProcessInterruptedException());
        }
    }

    public SearchExpression getExpression() {
        return searchExpression;
    }

    /**
     * Returns list of search results.
     */
    public List<SearchResultEntry> getSearchResults() {
        if (!searchCompleted) {
            throw new IllegalStateException("Search not completed yet");
        }

        if (!preprocessResults) {
            return searchResults;
        }

        // function can be called multiple times after search
        // results preprocess should occur only one time
        preprocessResults = false;
        if (searchExpression.allResults) {
            return searchResults;
        }

        for (SearchResultEntry entry : searchResults) {
            String key = entry.getSrcText() + entry.getTranslation();
            if (entry.getEntryNum() == ENTRY_ORIGIN_TRANSLATION_MEMORY) {
                if (tmxMap.containsKey(key) && (tmxMap.get(key) > 0)) {
                    entry.setPreamble(updatePreamble(entry, tmxMap.get(key)));
                }
            } else if (entry.getEntryNum() > ENTRY_ORIGIN_PROJECT_MEMORY) {
                // at this stage each PM entry num is increased by 1
                if (entryMap.containsKey(key) && (entryMap.get(key) > 0)) {
                    entry.setPreamble(updatePreamble(entry, entryMap.get(key)));
                }
            }
        }

        return searchResults;
    }

    private String updatePreamble(SearchResultEntry entry, int matchNumber) {
        String preamble = entry.getPreamble();
        if (preamble == null || StringUtil.isEmpty(preamble)) {
            return OStrings.getString("SW_NR_MATCHES", 1 + matchNumber);
        }
        return OStrings.getString("SW_FILE_AND_NR_OF_MORE", preamble, matchNumber);
    }

    /**
     * Search for this.expression and return a list of results.
     *
     * @throws IOException
     *             when searching files goes wrong
     */
    public void search() throws IOException {
        String textSearchExpression = searchExpression.text;

        numFinds = 0;
        // ensures that results will be preprocessed only one time
        preprocessResults = true;
        searchCompleted = false;

        searchResults.clear();
        tmxMap.clear();
        entryMap.clear();
        matchers.clear();

        // determine pattern matching flags
        int flags = searchExpression.caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;

        // Normalize width of search string if width insensitivity is requested.
        // Then, instead of modifying the regex, we also normalize the
        // comparison strings later on.
        if (searchExpression.widthInsensitive) {
            textSearchExpression = StringUtil.normalizeWidth(textSearchExpression);
        }

        // if exact search, just use the entire search string as a single
        // search string; otherwise, if keyword, break up the string into
        // separate words (= multiple search strings)

        switch (searchExpression.searchExpressionType) {
        case EXACT:
            // escape the search string, it's not supposed to be a regular
            // expression
            textSearchExpression = StaticUtils.globToRegex(textSearchExpression,
                    searchExpression.spaceMatchNbsp);

            // create a matcher for the search string
            matchers.add(Pattern.compile(textSearchExpression, flags).matcher(""));
            break;
        case KEYWORD:
            // break the search string into keywords,
            // each of which is a separate search string
            Pattern.compile(" ").splitAsStream(textSearchExpression.trim()).filter(word -> !word.isEmpty())
                    .map(word -> {
                        String glob = StaticUtils.globToRegex(word, false);
                        return Pattern.compile(glob, flags).matcher("");
                    }).forEach(matchers::add);
            break;
        case REGEXP:
            // space match nbsp (\u00a0)
            if (searchExpression.spaceMatchNbsp) {
                textSearchExpression = textSearchExpression.replace(" ", "( |\u00A0)");
                textSearchExpression = textSearchExpression.replace("\\\\s", "(\\\\s|\u00A0)");
            }

            // create a matcher for the search string
            matchers.add(Pattern.compile(textSearchExpression, flags).matcher(""));
            break;
        default:
            throw new IllegalStateException("Unknown search expression type");
        }

        try {
            if (searchExpression.rootDir == null) {
                // if no search directory specified, then we are
                // searching current project only
                searchProject();
            } else {
                searchFiles();
            }
        } finally {
            // Mark search as completed - provides happens-before edge for safe
            // result access
            // This ensures all search results are visible to other threads
            searchCompleted = true;
        }
    }

    /**
     * Indicates whether the search operation has been completed.
     *
     * @return true if the search has been completed; false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean isSearchCompleted() {
        return searchCompleted;
    }

    /** create a matcher for the author search string. */
    private Matcher createAuthorSearchExpression() {
        String authorSearchExpression = searchExpression.author;
        int flags = searchExpression.caseSensitive ? 0 : Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        if (searchExpression.searchExpressionType != SearchExpression.SearchExpressionType.REGEXP) {
            authorSearchExpression = StaticUtils.globToRegex(authorSearchExpression,
                    searchExpression.spaceMatchNbsp);
        }
        return Pattern.compile(authorSearchExpression, flags).matcher("");
    }

    // ////////////////////////////////////////////////////////////
    // internal functions

    private void addEntry(SearchResultEntry entry) {
        searchResults.add(entry);
        numFinds++;
    }

    /**
     * Queue found string. Removes duplicate segments (by Henry Pijffers) except
     * if allResults = true
     */
    private void foundString(int entryNum, @Nullable String intro, String src, @Nullable String target,
                             @Nullable String note, @Nullable String property, SearchMatch @Nullable [] srcMatches,
                             SearchMatch @Nullable [] targetMatches, SearchMatch @Nullable [] noteMatches,
                             SearchMatch @Nullable [] propertyMatches) {
        if (numFinds >= searchExpression.numberOfResults) {
            return;
        }

        String key = src + target;

        if (entryNum >= ENTRY_ORIGIN_PROJECT_MEMORY) {
            addProjectMemoryEntry(entryNum, src, target, note, property, srcMatches, targetMatches,
                    noteMatches, propertyMatches, key);
        } else if (entryNum == ENTRY_ORIGIN_TRANSLATION_MEMORY) {
            addTranslationMemoryEntry(entryNum, intro, src, target, note, property, srcMatches, targetMatches,
                    noteMatches, propertyMatches, key);
        } else {
            addEntry(SearchResultEntry.builder().entryNum(entryNum).preambleText(intro).srcPrefix(null)
                    .sourceText(src).targetText(target).note(note).propertiesString(property)
                    .srcMatch(srcMatches).targetMatch(targetMatches).noteMatch(noteMatches)
                    .propertiesMatch(propertyMatches).build());
        }
    }

    private void addTranslationMemoryEntry(int entryNum, @Nullable String intro, String src, @Nullable String target,
                                           @Nullable String note, @Nullable String property,
                                           SearchMatch @Nullable [] srcMatches, SearchMatch @Nullable [] targetMatches,
                                           SearchMatch @Nullable [] noteMatches,
                                           SearchMatch @Nullable [] propertyMatches, String key) {
        if (!tmxMap.containsKey(key) || searchExpression.allResults) {
            addEntry(SearchResultEntry.builder().entryNum(entryNum).preambleText(intro).srcPrefix(null)
                    .sourceText(src).targetText(target).note(note).propertiesString(property)
                    .srcMatch(srcMatches).targetMatch(targetMatches).noteMatch(noteMatches)
                    .propertiesMatch(propertyMatches).build());
            if (!searchExpression.allResults) {
                // first occurrence
                tmxMap.put(key, 0);
            }
        } else {
            // next occurrence
            tmxMap.put(key, tmxMap.get(key) + 1);
        }
    }

    private void addProjectMemoryEntry(int entryNum, String src, @Nullable String target, @Nullable String note,
                                       @Nullable String property, SearchMatch @Nullable [] srcMatches,
                                       SearchMatch @Nullable [] targetMatches, SearchMatch @Nullable [] noteMatches,
                                       SearchMatch @Nullable [] propertyMatches, String key) {
        if (!entryMap.containsKey(key) || searchExpression.allResults) {
            // HP, duplicate entry prevention
            // entries are referenced at offset 1 but stored at offset 0
            String file = searchExpression.fileNames ? getFileForEntry(entryNum + 1) : null;
            addEntry(SearchResultEntry.builder().entryNum(entryNum + 1).preambleText(file)
                    .srcPrefix((entryNum + 1) + "> ").sourceText(src).targetText(target).note(note)
                    .propertiesString(property).srcMatch(srcMatches).targetMatch(targetMatches)
                    .noteMatch(noteMatches).propertiesMatch(propertyMatches).build());
            if (!searchExpression.allResults) { // If we filter results
                entryMap.put(key, 0); // HP
            }
        } else {
            entryMap.put(key, entryMap.get(key) + 1);
        }
    }

    private void searchProject() {
        // reset the number of search hits
        numFinds = 0;

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
                if (numFinds >= searchExpression.numberOfResults) {
                    throw new SearchLimitReachedException();
                }
                checkInterrupted();
            }
        }
    }

    private void searchExternalTM() throws SearchLimitReachedException {
        // search the TM, if requested
        if (searchExpression.tm) {
            // Search TM entries, unless we search for date or author.
            // They are not loaded from external TM, so skip the search in
            // that case.
            Path projectRoot = Paths.get(project.getProjectProperties().getProjectRoot());
            if (!searchExpression.searchAuthor && !searchExpression.searchDateAfter
                    && !searchExpression.searchDateBefore) {
                for (Map.Entry<String, ExternalTMX> tmEn : project.getTransMemories().entrySet()) {
                    final String fileTM = searchExpression.fileNames
                            ? projectRoot.relativize(Paths.get(tmEn.getKey())).toString()
                            : null;
                    searchEntries(tmEn.getValue().getEntries(), ENTRY_ORIGIN_TRANSLATION_MEMORY, fileTM);
                    checkInterrupted();
                }
                for (Map.Entry<Language, ProjectTMX> tmEn : project.getOtherTargetLanguageTMs().entrySet()) {
                    final Language langTM = tmEn.getKey();
                    searchEntries(tmEn.getValue().getDefaults(), ENTRY_ORIGIN_ALTERNATIVE,
                            langTM.getLanguage());
                    searchEntries(tmEn.getValue().getAlternatives(), ENTRY_ORIGIN_ALTERNATIVE,
                            langTM.getLanguage());
                    checkInterrupted();
                }
            }
        }
    }

    private void searchMemory() throws SearchLimitReachedException {
        // search the Memory, if requested
        if (searchExpression.memory) {
            // search through all project entries
            List<SourceTextEntry> allEntries = project.getAllEntries();
            for (int i = 0; i < allEntries.size(); i++) {
                // stop searching if the max. nr of hits has been reached
                if (numFinds >= searchExpression.numberOfResults) {
                    throw new SearchLimitReachedException();
                }
                // get the source and translation of the next entry
                SourceTextEntry ste = allEntries.get(i);
                TMXEntry te = project.getTranslationInfo(ste);

                checkEntry(ste.getSrcText(), te.translation, te.note, ste.getRawProperties(), te, i, null);
                checkInterrupted();
            }

            // search in orphaned
            if (!searchExpression.excludeOrphans) {
                project.iterateByDefaultTranslations(new IProject.DefaultTranslationsIterator() {
                    final String file = OStrings.getString("CT_ORPHAN_STRINGS");

                    @Override
                    public void iterate(String source, TMXEntry en) {
                        // stop searching if the max. nr of hits has been
                        // reached
                        if (numFinds >= searchExpression.numberOfResults) {
                            return;
                        }
                        checkInterrupted();
                        if (project.isOrphaned(source)) {
                            checkEntry(en.source, en.translation, en.note, null, en, ENTRY_ORIGIN_ORPHAN,
                                    file);
                        }
                    }
                });
                project.iterateByMultipleTranslations(new IProject.MultipleTranslationsIterator() {
                    final String file = OStrings.getString("CT_ORPHAN_STRINGS");

                    @Override
                    public void iterate(EntryKey source, TMXEntry en) {
                        // stop searching if the max. nr of hits has been
                        // reached
                        if (numFinds >= searchExpression.numberOfResults) {
                            return;
                        }
                        checkInterrupted();
                        if (project.isOrphaned(source)) {
                            checkEntry(en.source, en.translation, en.note, null, en, ENTRY_ORIGIN_ORPHAN,
                                    file);
                        }
                    }
                });
            }
        }
    }

    private @Nullable String getFileForEntry(int i) {
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
     * Loops over collection of TMXEntries and checks every entry. If max nr of
     * hits have been reached or search has been stopped, the function stops and
     * returns false. Else it finishes and returns true;
     *
     * @param tmEn
     *            collection of TMX Entries to check.
     * @param tmxID
     *            identifier of the TMX. E.g. the filename or language code
     * @throws SearchLimitReachedException
     *             when nr of found matches exceeds requested nr of results
     */
    private void searchEntries(Iterable<? extends ITMXEntry> tmEn, int origin, @Nullable String tmxID)
            throws SearchLimitReachedException {
        for (ITMXEntry tm : tmEn) {
            // stop searching if the max. nr of hits has been reached
            if (numFinds >= searchExpression.numberOfResults) {
                throw new SearchLimitReachedException();
            }

            // for alternative translations:
            // - it is not feasible to get the SourceTextEntry that matches the
            // tm.source, so we cannot get the entryNum
            // and real translation
            // - although the 'translation' is used as 'source', we search it as
            // translation, else we cannot show to
            // which real source it belongs
            checkEntry(tm.getSourceText(), tm.getTranslationText(), tm.getNote(), null, null, origin, tmxID);

            checkInterrupted();
        }
    }

    /**
     * Check if the specified entry should be found.
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
     *            entry. Null for external tmx entries (so we can only search
     *            for source and translation in external tmx)
     * @param entryNum
     *            entry number
     * @param intro
     *            file
     */
    void checkEntry(String srcText, @Nullable String locText, @Nullable String note, String @Nullable [] properties,
                    @Nullable ITMXEntry entry, int entryNum, @Nullable String intro) {
        SearchMatch[] srcMatches = null;
        SearchMatch[] targetMatches = null;
        SearchMatch[] srcOrTargetMatches = null;
        SearchMatch[] noteMatches = null;
        SearchMatch[] propertyMatches = null;
        String firstMatchedProperty = null;

        if (Objects.requireNonNull(searchExpression.mode) == SearchMode.SEARCH) {
            if (searchExpression.searchTranslated && !searchExpression.searchUntranslated
                    && locText == null) {
                return;
            }
            if (!searchExpression.searchTranslated && searchExpression.searchUntranslated
                    && locText != null) {
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
            if (searchExpression.searchSource && searchExpression.searchTarget && locText != null
                    && srcMatches == null && targetMatches == null
                    && searchString(srcText + '\ue000' + locText)) {
                srcOrTargetMatches = foundMatches.toArray(new SearchMatch[0]);
            }
            if (searchExpression.searchNotes && searchString(note)) {
                noteMatches = foundMatches.toArray(new SearchMatch[0]);
            }
            if (searchExpression.searchComments && properties != null) {
                // loop over values only, not keys.
                for (int i = 1; i <= properties.length; i = i + 2) {
                    if (searchString(properties[i], true)) {
                        propertyMatches = foundMatches.toArray(new SearchMatch[0]);
                        firstMatchedProperty = properties[i];
                        break;
                    }
                }
            }
        } else if (searchExpression.mode == SearchMode.REPLACE) {
            if (searchExpression.replaceTranslated && locText != null) {
                if (searchString(locText, false)) {
                    targetMatches = foundMatches.toArray(new SearchMatch[0]);
                }
            } else if (searchExpression.replaceUntranslated && locText == null) {
                if (searchString(srcText, false)) {
                    srcMatches = foundMatches.toArray(new SearchMatch[0]);
                }
            }
        }
        // if the search expression is satisfied, report the hit
        if ((srcMatches != null || targetMatches != null || srcOrTargetMatches != null || noteMatches != null
                || propertyMatches != null)
                && (!searchExpression.searchAuthor || searchAuthor(entry))
                && (!searchExpression.searchDateBefore || entry != null && entry.getChangeDate() != 0
                        && entry.getChangeDate() < searchExpression.dateBefore)
                && (!searchExpression.searchDateAfter || entry != null && entry.getChangeDate() != 0
                        && entry.getChangeDate() > searchExpression.dateAfter)) {
            // found
            foundString(entryNum, intro, srcText, locText, note, firstMatchedProperty, srcMatches,
                    targetMatches, noteMatches, propertyMatches);
        }
    }

    private void searchFiles() throws IOException {
        Path root = Paths.get(searchExpression.rootDir);

        FilterMaster fm = Core.getFilterMaster();

        final SearchCallback searchCallback = new SearchCallback(project.getProjectProperties());

        int depth = searchExpression.recursive ? Integer.MAX_VALUE : 0;
        try (Stream<Path> walker = Files.walk(root, depth, FileVisitOption.FOLLOW_LINKS)) {
            walker.filter(Files::isRegularFile).forEach(path -> {
                String filename = path.toString();
                // determine actual file name w/ no root path info
                FileInfo fi = new FileInfo(root.relativize(path).toString());

                searchCallback.setCurrentFile(fi);
                try {
                    // Check for interruption before processing each file.
                    checkInterrupted();
                    fm.loadFile(filename, new FilterContext(project.getProjectProperties()), searchCallback);
                } catch (IOException | TranslationException ex) {
                    Log.log("Search error in file" + fi.filePath + ": " + ex.getMessage());
                } catch (RuntimeException ex) {
                    Log.log(ex);
                    throw ex; // Re-throw to stop processing
                }
                searchCallback.fileFinished();
            });
        }
    }

    @VisibleForTesting
    void addToMatcher(String text) {
        String textSearchExpression = StaticUtils.globToRegex(text, true);
        matchers.add(Pattern.compile(textSearchExpression, 0).matcher(""));
    }

    @VisibleForTesting
    List<SearchResultEntry> getRawSearchResults() {
        return Collections.unmodifiableList(searchResults);
    }

    protected class SearchCallback extends ParseEntry implements IParseCallback {
        private @Nullable String filename;

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
                List<ProtectedPart> protectedParts, String segmentTranslation,
                boolean segmentTranslationFuzzy, String[] props, String prevSegment, String nextSegment,
                String path) {
            searchText(segmentSource, segmentTranslation, filename);
        }
    }

    // /////////////////////////////////////////////////////////////////////
    // search algorithm

    /**
     * Looks for an occurrence of the search string(s) in the supplied text
     * string.
     *
     * @param origText
     *            The text string to search in
     *
     * @return True if the text string contains all search strings
     */
    public boolean searchString(@Nullable String origText) {
        return searchString(origText, true);
    }

    public boolean searchString(@Nullable String origText, boolean collapseResults) {
        if (origText == null || matchers.isEmpty()) {
            return false;
        }

        String normalizedText = normalizeText(origText);
        foundMatches.clear();
        // check the text against all matchers
        OUT_LOOP: for (Matcher matcher : matchers) {
            // check the text against the current matcher
            // if one of the search strings is not found, don't
            // bother looking for the rest of the search strings
            matcher.reset(normalizedText);
            if (!matcher.find()) {
                return false;
            }

            while (true) {
                int start = matcher.start();
                int end = matcher.end();
                if (!normalizedText.substring(start, end).equals(origText.substring(start, end))) {
                    // In case of normalization, check whenever the string to
                    // search is still present but shifted
                    int find = origText.indexOf(normalizedText.substring(start, end));
                    if (find >= 0) {
                        end = find + (end - start);
                        start = find;
                    } else {
                        // If the string to search contains normalized
                        // characters, then we cannot find this match
                        // Try to find it using normalization of substrings
                        String foundText = normalizedText.substring(start, end);
                        if (!findMatchUsingNormalization(origText, foundText)) {
                            // No way, we cannot find the match at all. Do not
                            // highlight but return true
                            break OUT_LOOP;
                        }
                    }
                }
                if (processMatch(matcher, end, start)) {
                    break;
                }
                if (start >= normalizedText.length()) {
                    // Reached the end of the text
                    break;
                }
                // Check for additional matches (matcher will now contain data
                // of next match).
                // Prevent infinite loop when match was zero-length by forcibly
                // incrementing
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
            collapseFoundMatches();
        }
        return true;
    }

    /**
     * Searches for a substring within a given string, starting from a specified
     * index. The method uses normalization to ensure width equivalency during
     * the search process.
     *
     * @param origText
     *            The original text in which to search for the substring.
     * @param foundText
     *            The substring to match within the original text.
     * @return True if the normalized version of the substring is found within
     *         the original text, false otherwise.
     */
    boolean findMatchUsingNormalization(String origText, String foundText) {
        for (int currentIndex = 0; currentIndex < origText.length(); currentIndex++) {
            if (StringUtil.normalizeWidth(origText.substring(currentIndex)).startsWith(foundText)) {
                int end = currentIndex;
                while (end < origText.length()) {
                    end++;
                    if (StringUtil.normalizeWidth(origText.substring(currentIndex, end)).equals(foundText)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String normalizeText(String text) {
        return searchExpression.widthInsensitive ? StringUtil.normalizeWidth(text) : text;
    }

    private void collapseFoundMatches() {
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

    /**
     * Processes a match found during a search operation. This method handles
     * both search-only and search-and-replace modes. In search-and-replace
     * mode, it applies the replacement logic through another helper method. In
     * search-only mode, it adds the match to the list of found matches if the
     * matched region is not empty.
     *
     * @param matcher
     *            The Matcher object used for finding matches in the text.
     * @param end
     *            The end position of the match in the text being searched.
     * @param start
     *            The start position of the match in the text being searched.
     * @return true if the match requires further processing or if a replacement
     *         was applied; false otherwise.
     */
    private boolean processMatch(Matcher matcher, int end, int start) {
        if (searchExpression.mode == SearchMode.REPLACE) {
            return searchReplaceImpl(searchExpression, foundMatches, matcher, end, start,
                    project.getProjectProperties().getTargetLanguage().getLocale());
        } else if (end > start) {
            // Add a match only if the matched region is not empty.
            // We still return true so the hit will still be recorded.
            foundMatches.add(new SearchMatch(start, end));
        }
        return false;
    }

    /**
     * Implements the logic for searching and replacing based on a given search
     * expression. Handles both regular expression-based replacements and simple
     * replacements, adding matches to the found matches list.
     *
     * @param newSearchExpression
     *            The search expression containing the search criteria and
     *            replacement text.
     * @param foundMatchesList
     *            A list to which all found matches, including their start, end
     *            positions, and replacements, are added.
     * @param matcher
     *            The matcher object used for searching and extracting matches
     *            based on the search expression.
     * @param end
     *            The end position of the match in the text being searched.
     * @param start
     *            The start position of the match in the text being searched.
     * @param targetLocale
     *            The locale used for formatting or case-sensitive replacement
     *            if applicable.
     * @return Always returns false after completing the replacement process.
     * @throws IndexOutOfBoundsException
     *             Throws this exception if a replacement group in the search
     *             expression refers to a matcher group that does not exist.
     */
    boolean searchReplaceImpl(SearchExpression newSearchExpression, List<SearchMatch> foundMatchesList,
            Matcher matcher, int end, int start, Locale targetLocale) {
        if (newSearchExpression.searchExpressionType == SearchExpression.SearchExpressionType.REGEXP) {
            if ((end == start) && (start > 0)) {
                return true;
            }
            String repl = newSearchExpression.replacement;
            Matcher replaceMatcher = PatternConsts.REGEX_VARIABLE.matcher(repl);
            while (replaceMatcher.find()) {
                int varId = Integer.parseInt(replaceMatcher.group(2));
                if (varId > matcher.groupCount()) {
                    // Group wasn't even present in search regex.
                    throw new IndexOutOfBoundsException(
                            OStrings.getString("ST_REGEXP_REPLACEGROUP_ERROR", varId));
                }
                String substitution = matcher.group(varId);
                // yes, from source matcher!
                if (substitution == null) {
                    // If group was present in search regex but didn't match
                    // anything, replace with empty string.
                    substitution = "";
                }
                // avoid re-eval inside replaceCase;
                substitution = substitution.replace("\\", "\\\\").replace("$", "\\$");
                repl = repl.substring(0, replaceMatcher.start()) + substitution
                        + repl.substring(replaceMatcher.end());
                replaceMatcher.reset(repl);
            }
            foundMatchesList.add(new SearchMatch(start, end, StringUtil.replaceCase(repl, targetLocale)));

        } else {
            foundMatchesList.add(new SearchMatch(start, end, newSearchExpression.replacement));
        }
        return false;
    }

    /**
     * Retrieves a list of matches found during the search operation.
     *
     * @return a list of {@link SearchMatch} objects, where each object provides
     *         information about the match's start and end positions, and any
     *         associated replacement text.
     */
    public List<SearchMatch> getFoundMatches() {
        if (!searchCompleted) {
            throw new IllegalStateException("Search not completed yet.");
        }
        return foundMatches;
    }

    /**
     * Looks for an occurrence of the author search string in the supplied
     * TMXEntry.
     *
     * @param te
     *            The TMXEntry to search in
     *
     * @return True if the text string contains the search string
     */
    private boolean searchAuthor(@Nullable ITMXEntry te) {
        Matcher author = createAuthorSearchExpression();
        if (te == null) {
            return false;
        }

        if (author.pattern().pattern().isEmpty()) {
            // Handle search for null author.
            return te.getChanger() == null && te.getCreator() == null;
        }

        if (te.getChanger() != null) {
            author.reset(te.getChanger());
            if (author.find()) {
                return true;
            }
        }

        if (te.getCreator() != null) {
            author.reset(te.getCreator());
            return author.find();
        }

        return false;
    }

    // ///////////////////////////////////////////////////////////////
    // interface used by FileHandlers

    public void searchText(String seg, @Nullable String translation, @Nullable String filename) {
        // don't look further if the max. nr of hits has been reached
        if (numFinds >= searchExpression.numberOfResults) {
            return;
        }

        checkInterrupted();

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
