/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Zoltan Bartko
               2009 Didier Briel
               2013 Guido Leenders
               2014 Aaron Madlon-Kay
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

package org.omegat.util;

/**
 * OmegaT-wide Constants.
 * <p>
 * // TODO Note: Some constants that are used only in a single class, or are more appropriate in another class (e.g.
 * preference names) are moved in appropriate class definitions.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Zoltan Bartko (bartkozoltan@bartkozoltan.com)
 * @author Didier Briel
 * @author Guido Leenders
 */
public final class OConsts {

    private OConsts() {
    }

    // project file consts
    /** Project Filename */
    public static final String FILE_PROJECT = "omegat.project";
    /** Project Version */
    public static final String PROJ_CUR_VERSION = "1.0";

    public static final String TMX_EXTENSION = ".tmx";
    public static final String TMX_GZ_EXTENSION = ".tmx.gz";
    public static final String TMX_ZIP_EXTENSION = ".tmx.zip";
    public static final String OMEGAT_TMX = "-omegat";
    public static final String LEVEL1_TMX = "-level1";
    public static final String LEVEL2_TMX = "-level2";

    // help
    public static final String HELP_HOME = "index.html";
    public static final String HELP_FIRST_STEPS = "first_steps.html";
    public static final String HELP_DIR = "docs";
    public static final String HELP_LANG_INDEX = "languageIndex.html";

    // licenses
    public static final String LICENSE_FILE = "OmegaT-license.txt";

    // Last changes
    public static final String LAST_CHANGES_FILE = "changes.txt";

    // contributors
    public static final String CONTRIBUTORS_FILE = "contributors.txt";
    public static final String LIBRARIES_FILE = "libraries.txt";

    // status file consts
    public static final String FILES_ORDER_FILENAME = "files_order.txt";
    public static final String STATUS_EXTENSION = "project_save.tmx";
    public static final String BACKUP_EXTENSION = ".bak";
    public static final String NEWFILE_EXTENSION = ".new";

    /** Project subfolder for source files default name. */
    public static final String DEFAULT_SOURCE = "source";
    /** Project subfolder for translated files default name. */
    public static final String DEFAULT_TARGET = "target";
    /** Project subfolder for glossaries default name. */
    public static final String DEFAULT_GLOSSARY = "glossary";
    /** Default name for the project writeable glossary file (inside project) */
    public static final String DEFAULT_W_GLOSSARY = "glossary.txt";
    /** Default suffix for project writeable glossary file (outside of project) */
    public static final String DEFAULT_W_GLOSSARY_SUFF = "-glossary.txt";
    /** Project subfolder for legacy translation memories default name. */
    public static final String DEFAULT_TM = "tm";
    /** Project subfolder for exported translation memories. */
    public static final String DEFAULT_EXPORT_TM = "";
    /**
     * Translation memory levels, space-separated string, to include zero or more of the following values: "omegat",
     * "level1" and/or "level2").
     */
    public static final String DEFAULT_EXPORT_TM_LEVELS = "omegat level1 level2";
    /** Project subfolder for automatically applied translation memories within the tm folder. */
    public static final String AUTO_TM = "auto";
    /**
     * Project subfolder for automatically applied translation memories within the tm folder. Existing translation are
     * overwritten
     */
    public static final String AUTO_ENFORCE_TM = "enforce";
    /** Project subfolder for machine translation memories. */
    public static final String MT_TM = "mt";
    /** Project subfolder for generic penalty based translation memories. */
    public static final String PENALTY_TM = "penalty-xxx";
    /** Project subfolder for translation memories with other languages as alternative sources. */
    public static final String DEFAULT_OTHERLANG = "tmx2source";
    /** Project subfolder for dictionaries default name. */
    public static final String DEFAULT_DICT = "dictionary";
    /** Project subfolder for project's translation memory. */
    public static final String DEFAULT_INTERNAL = "omegat";
    /** Default name for spelling dictionary directory */
    public static final String SPELLING_DICT_DIR = "spelling";

    /**
     * Glossary files extensions
     */
    public static final String EXT_TSV_DEF = ".tab";
    public static final String EXT_TSV_UTF8 = ".utf8";
    public static final String EXT_TSV_TXT = ".txt";
    public static final String EXT_TSV_TSV = ".tsv";
    public static final String EXT_CSV_UTF8 = ".csv";
    public static final String EXT_TBX = ".tbx";

    /**
     * The name of the file with project statistics: segments, words, chars count. The extension
     * is added with the appropriate output format.
     */
    public static final String STATS_FILENAME = "project_stats";

    /** The name of the file with project match statistics. */
    public static final String STATS_MATCH_FILENAME = "project_stats_match.txt";

    /** The name of the file with project match statistics. */
    public static final String STATS_MATCH_PER_FILE_FILENAME = "project_stats_match_per_file.txt";

    /** The name of the file with the last entry number for later reopening. */
    public static final String LAST_ENTRY_NUMBER = "last_entry.properties";

    /** The name of the file with the ignored words: one ignored word per line */
    public static final String IGNORED_WORD_LIST_FILE_NAME = "ignored_words.txt";

    /** The name of the file with the correct (learned) words: one word per line */
    public static final String LEARNED_WORD_LIST_FILE_NAME = "learned_words.txt";

    /** affix file extension */
    public static final String SC_AFFIX_EXTENSION = ".aff";

    /** dictionary file extension */
    public static final String SC_DICTIONARY_EXTENSION = ".dic";

    /** jar file extension */
    public static final String JAR_EXTENSION = ".jar";

    /** zip file extension */
    public static final String ZIP_EXTENSION = ".zip";

    /** The smallest threshold to detect a fuzzy match string */
    public static final int FUZZY_MATCH_THRESHOLD = 30;

    public static final int ST_MAX_SEARCH_RESULTS = 1000;

    public static final String XB_COMMENT_SHORTCUT = "!comment";

    /** Number of fuzzy matches to display */
    public static final int MAX_NEAR_STRINGS = 5;

    public static final int MAX_BACKUPS = 11;

    /**
     * The limit of bytes that various filters may read ahead to inspect files. Increased from 8k (8192 bytes; the
     * default buffer size for {@link java.io.BufferedReader}) to this number because otherwise EOL detection on
     * <code>.sdlxliff</code> files with large headers can fail.
     */
    public static final int READ_AHEAD_LIMIT = 65536;

    /**
     * The maximum "distance" in parent directories a path can be at when relativizing a path for storage. I.e. how many
     * instances of <code>../</code> are in front.
     * <p>
     * This is used when storing project folders (source, target, tm, glossary, etc.) located outside of the project
     * root folder.
     * <p>
     * This limit is based on the following logic:
     * <ul>
     * <li>Users may move their project around locally. This can break relative paths, so absolute paths are desired in
     * this sense.
     * <li>Users may want to share projects via a mechanism that changes the filesystem root (e.g. differing drive
     * letter mapping on Windows). In this case relative paths are desired.
     * </ul>
     * This limit is a heuristic for determining if a path is "related" to the root: If it's "too far" away then it is
     * assumed to be unrelated, whereas a "nearby" path may be part of a related subtree like <code>~/Projects</code>.
     */
    public static final int MAX_PARENT_DIRECTORIES_ABS2REL = 5;

    public static final String SEGMENT_MARKER_STRING = "<" + OStrings.getSegmentMarker() + ">";

    /** Max number of recent projects to remember */
    public static final int MAX_RECENT_PROJECTS = 10;

    /** Default window size */
    public static final int OMEGAT_WINDOW_WIDTH = 1500;
    public static final int OMEGAT_WINDOW_HEIGHT = 1080;
}
