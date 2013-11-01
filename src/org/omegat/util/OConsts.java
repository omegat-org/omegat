/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Zoltan Bartko
               2009 Didier Briel
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

package org.omegat.util;

import java.io.File;

/**
 * OmegaT-wide Constants.
 * <p>
 * // TODO Note: Some constants that are used only in a single class, or are
 * more appropriate in another class (e.g. preference names) are moved in
 * appropriate class definitions.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Zoltan Bartko (bartkozoltan@bartkozoltan.com)
 * @author Didier Briel
 */
public class OConsts {

    // project file consts
    /** Project Filename */
    public static final String FILE_PROJECT = "omegat.project";
    /** Project Version */
    public static final String PROJ_CUR_VERSION = "1.0";

    public static final String TMX_EXTENSION = ".tmx";
    public static final String TMX_GZ_EXTENSION = ".tmx.gz";
    public static final String OMEGAT_TMX = "-omegat";
    public static final String LEVEL1_TMX = "-level1";
    public static final String LEVEL2_TMX = "-level2";

    // help
    public static final String HELP_HOME = "index.html";
    public static final String HELP_INSTANT_START = "instantStartGuideNoTOC.html";
    public static final String HELP_DIR = "docs";
    public static final String HELP_LANG_INDEX = "languageIndex.html";

    // licenses
    public static final String LICENSE_FILE = "OmegaT-license.txt";
    
    // Last changes
    public static final String LAST_CHANGES_FILE = "changes.txt";

    // status file consts
    public static final String STATUS_EXTENSION = "project_save.tmx";
    public static final String STATUS_RECOVER_EXTENSION = ".recover";
    public static final String BACKUP_EXTENSION = ".bak";
    public static final String NEWFILE_EXTENSION = ".new";

    /** Project subfolder for source files default name. */
    public static final String DEFAULT_SOURCE = "source";
    /** Project subfolder for translated files default name. */
    public static final String DEFAULT_TARGET = "target";
    /** Project subfolder for glossaries default name. */
    public static final String DEFAULT_GLOSSARY = "glossary";
    /** Default name for the project writeable glossary file */
    public static final String DEFAULT_W_GLOSSARY = "-glossary.txt";
    /** Project subfolder for legacy translation memories default name. */
    public static final String DEFAULT_TM = "tm";
    /** Project subfolder for automatically applied translation memories within the tm folder. */
    public static final String AUTO_TM = "auto";
    /** Project subfolder for dictionaries default name. */
    public static final String DEFAULT_DICT = "dictionary";
    /** Project subfolder for project's translation memory. */
    public static final String DEFAULT_INTERNAL = "omegat";
    /** Project subfolder for translation memories with other languages as alternative sources. */
    public static final String DEFAULT_OTHERLANG = "tmx2source";

    /**
     * Glossary files extensions
     */
    public static final String EXT_TSV_DEF = ".tab";
    public static final String EXT_TSV_UTF8 = ".utf8";
    public static final String EXT_TSV_TXT = ".txt";
    public static final String EXT_CSV_UTF8 = ".csv";
    public static final String EXT_TBX = ".tbx";

    /**
     * A marker that tells OmegaT that project's subfolder has default location.
     */
    public static final String DEFAULT_FOLDER_MARKER = "__DEFAULT__";

    /**
     * The name of the file with project statistics: segments, words, chars
     * count.
     */
    public static final String STATS_FILENAME = "project_stats.txt";

    /** The name of the file with project match statistics. */
    public static final String STATS_MATCH_FILENAME = "project_stats_match.txt";

    /** The name of the file with the ignored words: one ignored word per line */
    public static final String IGNORED_WORD_LIST_FILE_NAME = "ignored_words.txt";

    /** The name of the file with the correct (learned) words: one word per line */
    public static final String LEARNED_WORD_LIST_FILE_NAME = "learned_words.txt";

    /** the native library directory */
    public static final String NATIVE_LIBRARY_DIR = "native";

    /** affix file extension */
    public static final String SC_AFFIX_EXTENSION = ".aff";

    /** dictionary file extension */
    public static final String SC_DICTIONARY_EXTENSION = ".dic";

    /** The name of the file with the source exported segment */
    public static final String SOURCE_EXPORT = "source.txt";
    /** The name of the file with the target exported segment */
    public static final String TARGET_EXPORT = "target.txt";
    /** The name of the file with the exported selection */
    public static final String SELECTION_EXPORT = "selection.txt";

    /** The smallest threshold to detect a fuzzy match string */
    public static final int FUZZY_MATCH_THRESHOLD = 30;

    public static final int ST_MAX_SEARCH_RESULTS = 1000;

    public static final String TF_SRC_FONT_NAME = "source_font";
    public static final String TF_SRC_FONT_SIZE = "source_font_size";
    public static final String TF_FONT_DEFAULT = "Dialog";
    public static final int TF_FONT_SIZE_DEFAULT = 12;

    public static final String XB_COMMENT_SHORTCUT = "!comment";

    /** Number of fuzzy matches to display */
    public static final int MAX_NEAR_STRINGS = 5;
    /** Number of fuzzy matches to store */
    public static final int MAX_STORED_NEAR_STRINGS = 50;

    /**
     * The limit of bytes that AbstractFilter.isFileSupported may read. 8k (8192
     * bytes) for now, as this is the default buffer size for BufferedReader.
     */
    public static final int READ_AHEAD_LIMIT = 65536;

    /**
     * The name of the OmegaT Jar file. It is used to calculate the installation
     * directory.
     */
    public static final String APPLICATION_JAR = "OmegaT.jar";

    /**
     * Application debug classpath. It is used to calculate the installation
     * directory (in case of debugging -- the sources directory).
     */
    public static final String DEBUG_CLASSPATH = File.separator + "classes";

    /** Encoding: "UTF-8". */
    public static final String UTF8 = "UTF-8";
    /** Encoding: "ISO-8859-1". */
    public static final String ISO88591 = "ISO-8859-1";
    /** Encoding: "ISO-8859-2". */
    public static final String ISO88592 = "ISO-8859-2";
    /** Encoding: "UTF-16LE". */
    public static final String UTF16LE = "UTF-16LE";

    public static final String REMOTE_SC_DICTIONARY_LIST_LOCATION = "http://download.services.openoffice.org/files/contrib/dictionaries/";

    public static final String segmentMarkerString = "<" + OStrings.getSegmentMarker() + ">";
}
