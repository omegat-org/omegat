/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2008-2009 Didier Briel
               2010 Wildrich Fourie, Antonio Vilei, Didier Briel
               2011 John Moran, Didier Briel
               2012 Martin Fleurke, Wildrich Fourie, Didier Briel, Thomas Cordonnier,
                    Aaron Madlon-Kay
               2013 Aaron Madlon-Kay, Zoltan Bartko
               2014 Piotr Kulik, Aaron Madlon-Kay
               2015 Aaron Madlon-Kay, Yu Tang, Didier Briel, Hiroshi Miura
               2016 Aaron Madlon-Kay
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import org.omegat.core.segmentation.SRX;
import org.omegat.filters2.master.FilterMaster;

import gen.core.filters.Filters;

/**
 * Class to load & save global OmegaT preferences.
 * <p>
 * Initially this class was implemented with static members and methods directly
 * implementing the interface, backed by XML storage. However this was bad for
 * testing and extensibility, or allowing different persistence formats.
 * <p>
 * This class's static methods remain for compatibility, but now they wrap a
 * singleton instance of a concrete implementation of {@link IPreferences}.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Didier Briel
 * @author Wildrich Fourie
 * @author Antonio Vilei
 * @author Martin Fleurke
 * @author John Moran
 * @author Aaron Madlon-Kay
 * @author Hiroshi Miura
 */
public class Preferences {
    /** OmegaT-wide Preferences Filename */
    public static final String FILE_PREFERENCES = "omegat" + OStrings.getBrandingToken() + ".prefs";

    // preference names
    public static final String SOURCE_LOCALE = "source_lang";
    public static final String TARGET_LOCALE = "target_lang";
    public static final String CURRENT_FOLDER = "current_folder";
    public static final String SOURCE_FOLDER = "source_folder";
    public static final String TARGET_FOLDER = "target_folder";
    public static final String TM_FOLDER = "tm_folder";
    public static final String DICT_FOLDER = "dict_folder";
    public static final String GLOSSARY_FOLDER = "glossary_folder";
    public static final String GLOSSARY_FILE = "glossary_file";

    public static final String TF_SRC_FONT_NAME = "source_font";
    public static final String TF_FONT_DEFAULT = "Dialog";
    public static final String TF_SRC_FONT_SIZE = "source_font_size";
    public static final int TF_FONT_SIZE_DEFAULT = 12;
    
    /** Whether to automatically perform MT requests on entering segment */
    public static final String MT_AUTO_FETCH = "mt_auto_fetch";
    /** Whether to restrict automatic MT requests to only untranslated segments */
    public static final String MT_ONLY_UNTRANSLATED = "mt_only_untranslated";
    
    public static final String GLOSSARY_TBX_DISPLAY_CONTEXT = "glossary_tbx_display_context";
    public static final String GLOSSARY_NOT_EXACT_MATCH = "glossary_not_exact_match";
    public static final String GLOSSARY_STEMMING = "glossary_stemming";
    public static final String GLOSSARY_REPLACE_ON_INSERT = "glossary_replace_on_insert";
    public static final String DICTIONARY_FUZZY_MATCHING = "dictionary_fuzzy_matching";

    public static final String MAINWINDOW_GEOMETRY_PREFIX = "screen";
    public static final String MAINWINDOW_LAYOUT = "docking_layout";

    // Project files window size and position
    public static final String PROJECT_FILES_WINDOW_GEOMETRY_PREFIX = "project_files_window";
    // Using the main font for the Project Files window
    public static final String PROJECT_FILES_USE_FONT = "project_files_use_font";
    // Determines whether or not the Project Files window is shown on project load.
    // Currently not exposed in UI.
    public static final String PROJECT_FILES_SHOW_ON_LOAD = "project_files_show_on_load";

    // Search window size and position
    public static final String SEARCHWINDOW_GEOMETRY_PREFIX = "search_window";
    public static final String SEARCHWINDOW_SEARCH_TYPE = "search_window_search_type";
    public static final String SEARCHWINDOW_REPLACE_TYPE = "search_window_replace_type";
    public static final String SEARCHWINDOW_CASE_SENSITIVE = "search_window_case_sensitive";
    public static final String SEARCHWINDOW_SPACE_MATCH_NBSP = "search_window_space_match_nbsp";
    public static final String SEARCHWINDOW_CASE_SENSITIVE_REPLACE = "search_window_case_sensitive_replace";
    public static final String SEARCHWINDOW_SPACE_MATCH_NBSP_REPLACE = "search_window_space_match_nbsp_replace";
    public static final String SEARCHWINDOW_REPLACE_UNTRANSLATED = "search_window_replace_untranslated";
    public static final String SEARCHWINDOW_SEARCH_SOURCE = "search_window_search_source";
    public static final String SEARCHWINDOW_SEARCH_TRANSLATION = "search_window_search_translation";
    public static final String SEARCHWINDOW_SEARCH_STATE = "search_window_search_state";
    public static final String SEARCHWINDOW_SEARCH_NOTES = "search_window_search_notes";
    public static final String SEARCHWINDOW_SEARCH_COMMENTS = "search_window_search_comments";
    public static final String SEARCHWINDOW_REG_EXPRESSIONS = "search_window_reg_expressions";
    public static final String SEARCHWINDOW_GLOSSARY_SEARCH = "search_window_glossary_search";
    public static final String SEARCHWINDOW_MEMORY_SEARCH = "search_window_memory_search";
    public static final String SEARCHWINDOW_TM_SEARCH = "search_window_tm_search";
    public static final String SEARCHWINDOW_ALL_RESULTS = "search_window_all_results";
    public static final String SEARCHWINDOW_FILE_NAMES = "search_window_file_names";
    public static final String SEARCHWINDOW_ADVANCED_VISIBLE = "search_window_advanced_visible";
    public static final String SEARCHWINDOW_SEARCH_AUTHOR = "search_window_search_author";
    public static final String SEARCHWINDOW_AUTHOR_NAME = "search_window_author_name";
    public static final String SEARCHWINDOW_DATE_FROM = "search_window_date_from";
    public static final String SEARCHWINDOW_DATE_FROM_VALUE = "search_window_date_from_value";
    public static final String SEARCHWINDOW_DATE_TO = "search_window_date_to";
    public static final String SEARCHWINDOW_DATE_TO_VALUE = "search_window_date_to_value";
    public static final String SEARCHWINDOW_NUMBER_OF_RESULTS = "search_window_number_of_results";
    public static final String SEARCHWINDOW_DIR = "search_window_dir";
    public static final String SEARCHWINDOW_SEARCH_FILES = "search_window_search_files";
    public static final String SEARCHWINDOW_RECURSIVE = "search_window_search_recursive";
    public static final String SEARCHWINDOW_AUTO_SYNC = "search_window_auto_sync";
    public static final String SEARCHWINDOW_BACK_TO_INITIAL_SEGMENT = "search_window_back_to_initial_segment";
    public static final String SEARCHWINDOW_EXCLUDE_ORPHANS = "search_window_exclude_orphans";
    public static final String SEARCHWINDOW_FULLHALFWIDTH_INSENSITIVE = "search_window_full_half_width_insensitive";

    // Search history
    public static final String SEARCHWINDOW_HISTORY_SIZE = "search_window_history_size";
    public static final String SEARCHWINDOW_SEARCH_HISTORY_ITEM_PREFIX = "search_window_search_history_item_";
    public static final String SEARCHWINDOW_REPLACE_HISTORY_ITEM_PREFIX = "search_window_replace_history_item_";

    // Tag validation window size and position
    public static final String TAGVWINDOW_WIDTH = "tagv_window_width";
    public static final String TAGVWINDOW_HEIGHT = "tagv_window_height";
    public static final String TAGVWINDOW_X = "tagv_window_x";
    public static final String TAGVWINDOW_Y = "tagv_window_y";

    /** Use the TAB button to advance to the next segment */
    public static final String USE_TAB_TO_ADVANCE = "tab_advance";
    /** Always confirm Quit, even if the project is saved */
    public static final String ALWAYS_CONFIRM_QUIT = "always_confirm_quit";

    public static final String ALLOW_GOOGLE_TRANSLATE = "allow_google_translate";
    public static final String ALLOW_GOOGLE2_TRANSLATE = "allow_google2_translate";

    public static final String ALLOW_BELAZAR_TRANSLATE = "allow_belazar_translate";

    public static final String ALLOW_APERTIUM_TRANSLATE = "allow_apertium_translate";

    public static final String ALLOW_MICROSOFT_TRANSLATE = "allow_microsoft_translate";
    
    public static final String ALLOW_MYMEMORY_HUMAN_TRANSLATE = "allow_mymemory_human_translate";
    public static final String ALLOW_MYMEMORY_MACHINE_TRANSLATE = "allow_mymemory_machine_translate";
    
    public static final String ALLOW_YANDEX_TRANSLATE = "allow_yandex_translate";
    
    /** Enable TransTips */
    public static final String TRANSTIPS = "transtips";
    /** TransTips Option: Only match exact words */
    public static final String TRANSTIPS_EXACT_SEARCH = "transtips_exact_search";

    /** Mark the segments with a note with a different color */
    public static final String MARK_NOTED_SEGMENTS = "mark_noted_segments";
    
    /** Mark the non-breakable spaces with a different color */
    public static final String MARK_NBSP = "mark_nbsp";
    /** Mark whitespace as symbols */
    public static final String MARK_WHITESPACE = "mark_whitespace";
    /** Mark Bidi controls as symbols */
    public static final String MARK_BIDI = "mark_bidi";
    /** Do aggressive font fallback */
    public static final String FONT_FALLBACK = "font_fallback";

    /** Mark the translated segments with a different color */
    public static final String MARK_TRANSLATED_SEGMENTS = "mark_translated_segments";

    public static final String MARK_AUTOPOPULATED = "mark_autopopulated";

    /** Mark the untranslated segments with a different color */
    public static final String MARK_UNTRANSLATED_SEGMENTS = "mark_untranslated_segments";

    /** Workflow Option: Don't Insert Source Text Into Translated Segment */
    public static final String DONT_INSERT_SOURCE_TEXT = "wf_noSourceText";
    /** Workflow Option: Allow translation to be equal to source */
    public static final String ALLOW_TRANS_EQUAL_TO_SRC = "wf_allowTransEqualToSrc";
    /** Workflow Option: Insert Best Match Into Translated Segment */
    public static final String BEST_MATCH_INSERT = "wf_insertBestMatch";
    /** Workflow Option: Minimal Similarity Of the Best Fuzzy Match to insert */
    public static final String BEST_MATCH_MINIMAL_SIMILARITY = "wf_minimalSimilarity";
    /**
     * Default Value of Workflow Option: Minimal Similarity Of the Best Fuzzy
     * Match to insert
     */
    public static final int BEST_MATCH_MINIMAL_SIMILARITY_DEFAULT = 80;
    /** Workflow Option: Insert Explanatory Text before the Best Fuzzy Match */
    public static final String BEST_MATCH_EXPLANATORY_TEXT = "wf_explanatoryText";
    /** Workflow Option: Export current segment */
    public static final String EXPORT_CURRENT_SEGMENT = "wf_exportCurrentSegment";
    /** Workflow Option: Go To Next Untranslated Segment stops when there is at least one
    alternative translation */
    public static final String STOP_ON_ALTERNATIVE_TRANSLATION="wf_stopOnAlternativeTranslation";
    /** Workflow Option: Attempt to convert numbers when inserting a fuzzy match */
    public static final String CONVERT_NUMBERS = "wf_convertNumbers";
    /** Workflow Option: Save auto-populated status */
    public static final String SAVE_AUTO_STATUS = "save_auto_status";
    /** Workflow Option: Number of segments to load initially in editor */
    public static final String EDITOR_INITIAL_SEGMENT_LOAD_COUNT = "editor_initial_segment_load_count";
    public static final int EDITOR_INITIAL_SEGMENT_LOAD_COUNT_DEFAULT = 2000;
    
    /** Tag Validation Option: Don't check printf-tags */
    public static final String DONT_CHECK_PRINTF_TAGS = "tagValidation_noCheck";
    /** Tag Validation Option: check simple printf-tags */
    public static final String CHECK_SIMPLE_PRINTF_TAGS = "tagValidation_simpleCheck";
    /** Tag Validation Option: check all printf-tags */
    public static final String CHECK_ALL_PRINTF_TAGS = "tagValidation_elaborateCheck";
    /** Tag Validation Option: check simple java MessageFormat pattern tags */
    public static final String CHECK_JAVA_PATTERN_TAGS = "tagValidation_javaMessageFormatSimplePatternCheck";
    /** Tag Validation Option: check user defined tags according to regexp.*/
    public static final String CHECK_CUSTOM_PATTERN = "tagValidation_customPattern";
    /** Tag Validation Option: check target for text that should have been removed according to regexp.*/
    public static final String CHECK_REMOVE_PATTERN = "tagValidation_removePattern";

    /** Tag Validation Option: allow tag editing in editor. */
    public static final String ALLOW_TAG_EDITING = "allowTagEditing";

    /** Tag Validation Option: allow tag editing in editor. */
    public static final String TAG_VALIDATE_ON_LEAVE = "tagValidateOnLeave";

    /** Team option: author ID */
    public static final String TEAM_AUTHOR = "team_Author";

    /**
     * allow automatic spell checking or not
     */
    public static final String ALLOW_AUTO_SPELLCHECKING = "allow_auto_spellchecking";

    /**
     * The location of the spell checker dictionaries
     */
    public static final String SPELLCHECKER_DICTIONARY_DIRECTORY = "spellcheker_dir";

    /**
     * URL of the dictionary repository
     */
    public static final String SPELLCHECKER_DICTIONARY_URL = "dictionary_url";

    /** LanguageTool implementation to use */
    public static final String LANGUAGETOOL_BRIDGE_TYPE = "lt_bridgeType";
    /** URL of remote LanguageTool server */
    public static final String LANGUAGETOOL_REMOTE_URL = "lt_remoteURL";
    /** Local path to LanguageTool server jar file */
    public static final String LANGUAGETOOL_LOCAL_SERVER_JAR_PATH = "lt_localServerJarPath";
    /** Disabled categories */
    public static final String LANGUAGETOOL_DISABLED_CATEGORIES_PREFIX = "lt_disabledCategories";
    /** Disabled rules prefix */
    public static final String LANGUAGETOOL_DISABLED_RULES_PREFIX = "lt_disabledRules";
    /** Enabled rules prefix */
    public static final String LANGUAGETOOL_ENABLED_RULES_PREFIX = "lt_enabledRules";

    /**
     * The location of the scripts
     */
    public static final String SCRIPTS_DIRECTORY = "scripts_dir";
    
    /** Quick script names */
    public static final String SCRIPTS_QUICK_PREFIX = "scripts_quick_";
    public static final String SCRIPTS_QUICK_1 = "scripts_quick_1";
    public static final String SCRIPTS_QUICK_2 = "scripts_quick_2";
    public static final String SCRIPTS_QUICK_3 = "scripts_quick_3";
    public static final String SCRIPTS_QUICK_4 = "scripts_quick_4";
    public static final String SCRIPTS_QUICK_5 = "scripts_quick_5";
    public static final String SCRIPTS_QUICK_6 = "scripts_quick_6";
    public static final String SCRIPTS_QUICK_7 = "scripts_quick_7";
    public static final String SCRIPTS_QUICK_8 = "scripts_quick_8";
    public static final String SCRIPTS_QUICK_9 = "scripts_quick_9";
    public static final String SCRIPTS_QUICK_0 = "scripts_quick_0";

    /** Script window */
    public static final String SCRIPTWINDOW_GEOMETRY_PREFIX = "script_window";

    /** Most recent projects list */
    public static final String MOST_RECENT_PROJECTS_SIZE = "most_recent_projects_size";
    public static final String MOST_RECENT_PROJECTS_PREFIX = "most_recent_projects_";

    /**
     * display the segment sources
     */
    public static final String DISPLAY_SEGMENT_SOURCES = "display_segment_sources";

    /**
     * mark unique segments
     */
    public static final String MARK_NON_UNIQUE_SEGMENTS = "mark_non_unique_segments";

    /**
     * display modification info (author and modification date)
     */
    public static final String DISPLAY_MODIFICATION_INFO = "display_modification_info";

    /** External TMX options: Display level 2 tags */
    public static final String EXT_TMX_SHOW_LEVEL2 = "ext_tmx_show_level2";
    /** External TMX options: Use / for stand-alone tags */
    public static final String EXT_TMX_USE_SLASH = "ext_tmx_use_slash";
    /** External TMX options: Display template **/
    public static final String EXT_TMX_MATCH_TEMPLATE = "ext_tmx_match_template";
    /** External TMX options: Fuzzy match sort key **/
    public static final String EXT_TMX_SORT_KEY = "ext_tmx_sort_key";

    /** View options: Show all sources in bold */
    public static final String VIEW_OPTION_SOURCE_ALL_BOLD = "view_option_source_all_bold";
    /** View options: Mark first non-unique */
    public static final String VIEW_OPTION_UNIQUE_FIRST = "view_option_unique_first";
    /** View options: Simplify protected parts tooltips */
    public static final String VIEW_OPTION_PPT_SIMPLIFY = "view_option_ppt_simplify";
    /** View options: Modification Info display templates **/
    public static final String VIEW_OPTION_TEMPLATE_ACTIVE = "view_option_template_active";
    public static final String VIEW_OPTION_MOD_INFO_TEMPLATE = "view_option_mod_info_template";
    public static final String VIEW_OPTION_MOD_INFO_TEMPLATE_WO_DATE = "view_option_mod_info_template_wo_date";

    /** Proxy options: User name for proxy access */
    public static final String PROXY_USER_NAME = "proxy_user_name";
    /** Proxy options: Password for proxy  access */
    public static final String PROXY_PASSWORD = "proxy_password";

    /** Automatic save interval in seconds */
    public static final String AUTO_SAVE_INTERVAL = "auto_save_interval";

    /** Default number of seconds to auto save project */
    public static final int AUTO_SAVE_DEFAULT = 180;
    
    /** Custom external command for post-processing */
    public static final String EXTERNAL_COMMAND = "external_command";
    
    /** Allow per-project external commands */
    public static final String ALLOW_PROJECT_EXTERN_CMD = "allow_project_extern_cmd";

    /**
     * Version of file filters. Unfortunately cannot put it into filters itself
     * for backwards compatibility reasons.
     */
    public static final String FILTERS_VERSION = "filters_version";

    public static final String LT_DISABLED = "lt_disabled";

    public static final String LOOSE_TAG_ORDERING = "loose_tag_ordering";

    public static final String TAGS_VALID_REQUIRED = "tags_valid_required";

    public static final String STAT_COUNTING_PROTECTED_TEXT = "stat_counting_protected_text";
    public static final String STAT_COUNTING_CUSTOM_TAGS = "stat_counting_custom_tags";

    /**
     * Prefix for keys used to record default tokenizer behavior settings.
     * Prepend to the full name of the tokenizer, e.g.
     * 
     * <code>TOK_BEHAVIOR_PREFIX + tokenizer.class.getName()</code> to obtain
     * <code>tokenizer_behavior_org.omegat.tokenizer.LuceneXXTokenizer</code>
     */
    public static final String TOK_BEHAVIOR_PREFIX = "tokenizer_behavior_";

    public static final String AC_SHOW_SUGGESTIONS_AUTOMATICALLY = "ac_show_suggestions_automatically";
    
    /** glossary auto-completion */
    public static final String AC_GLOSSARY_ENABLED = "ac_glossary_enabled";
    public static final String AC_GLOSSARY_SHOW_SOURCE = "ac_glossary_show_source";
    public static final String AC_GLOSSARY_SHOW_TARGET_BEFORE_SOURCE = "ac_glossary_show_target_before_source";
    public static final String AC_GLOSSARY_SORT_BY_SOURCE = "ac_glossary_sort_by_source";
    public static final String AC_GLOSSARY_SORT_BY_LENGTH = "ac_glossary_sort_by_length";
    public static final String AC_GLOSSARY_SORT_ALPHABETICALLY = "ac_glossary_sort_alphabetically";
    public static final String AC_GLOSSARY_CAPITALIZE = "ac_glossary_capitalize";

    /** autotext auto-completion */
    public static final String AC_AUTOTEXT_ENABLED = "ac_autotext_enabled";
    public static final String AC_AUTOTEXT_SORT_BY_LENGTH = "ac_autotext_sort_by_length";
    public static final String AC_AUTOTEXT_SORT_ALPHABETICALLY = "ac_autotext_sort_alphabetically";
    public static final String AC_AUTOTEXT_SORT_FULL_TEXT = "ac_autotext_sort_full_text";

    /** char table auto-completion */
    public static final String AC_CHARTABLE_ENABLED = "ac_chartable_enabled";
    public static final String AC_CHARTABLE_USE_CUSTOM_CHARS = "ac_chartable_use_custom_chars";
    public static final String AC_CHARTABLE_CUSTOM_CHAR_STRING = "ac_chartable_custom_char_string";
    public static final String AC_CHARTABLE_UNIQUE_CUSTOM_CHARS = "ac_chartable_unique_custom_chars";

    /** history completion and prediction */
    public static final String AC_HISTORY_COMPLETION_ENABLED = "allow_history_completer";
    public static final String AC_HISTORY_PREDICTION_ENABLED = "history_completer_prediction_enabled";

    /** status bar progress mode */
    public static final String SB_PROGRESS_MODE = "sb_progress_mode";

    /** TaaS glossary client.*/
    public static final String TAAS_LOOKUP = "taas_lookup";
    public static final String TAAS_DOMAIN = "taas_domain";

    /** Segment Properties Area preferences */
    public static final String SEGPROPS_INITIAL_MODE = "segment_properties_initial_mode";
    public static final String SEGPROPS_SHOW_RAW_KEYS = "segment_properties_show_raw_keys";
    public static final String SEGPROPS_NOTIFY_PROPS = "segment_properties_notify_props";
    public static final String SEGPROPS_NOTIFY_DEFAULT_PROPS = "comment, note";

    /** Notification preferences */
    public static final String NOTIFY_FUZZY_MATCHES = "notify_fuzzy_matches";
    public static final String NOTIFY_GLOSSARY_HITS = "notify_glossary_hits";
    public static final String NOTIFY_COMMENTS = "notify_comments";
    public static final String NOTIFY_DICTIONARY_HITS = "notify_dictionary_hits";
    public static final String NOTIFY_MULTIPLE_TRANSLATIONS = "notify_multiple_translations";
    public static final String NOTIFY_NOTES = "notify_notes";

    /** Aligner settings */
    public static final String ALIGNER_HIGHLIGHT_PATTERN = "aligner_highlight_pattern";
    public static final String ALIGNER_HIGHLIGHT_PATTERN_DEFAULT = "\\d+";

    /** Create Glossary Entry dialog */
    public static final String CREATE_GLOSSARY_GEOMETRY_PREFIX = "create_glossary_dialog";

    public static final String PROPERTY_SRX = "srx";
    public static final String PROPERTY_FILTERS = "filters";

    /** Private constructor, because this file is singleton */
    private Preferences() {
    }

    /**
     * Returns the defaultValue of some preference out of OmegaT's preferences
     * file.
     * <p>
     * If the key is not found, returns the empty string.
     * 
     * @param key
     *            key of the key to look up, usually a static string from this
     *            class
     * @return preference defaultValue as a string
     */
    public static String getPreference(String key) {
        return m_preferences.getPreference(key);
    }
    
	/**
     * Returns true if the preference is in OmegaT's preferences file.
     * <p>
     * If the key is not found return false
     * 
     * @param key
     *            key of the key to look up, usually a static string from this
     *            class
     * @return true if preferences exists
     */
    public static boolean existsPreference(String key) {
        return m_preferences.existsPreference(key);
    }

    /**
     * Returns the boolean defaultValue of some preference.
     * <p>
     * Returns true if the preference exists and is equal to "true", false
     * otherwise (no such preference, or it's equal to "false", etc).
     * 
     * @param key
     *            preference key, usually a static string from this class
     * @return preference defaultValue as a boolean
     */
    public static boolean isPreference(String key) {
        return m_preferences.isPreference(key);
    }

    /**
     * Returns the boolean value of some preference out of OmegaT's preferences
     * file, if it exists.
     * <p>
     * If the key is not found, returns the default value provided and sets the
     * preference to the default value.
     * 
     * @param key
     *            name of the key to look up, usually a static string from this
     *            class
     * @param defaultValue
     *            default value for the key
     * @return preference value as an boolean
     */
    public static boolean isPreferenceDefault(String key, boolean defaultValue) {
        return m_preferences.isPreferenceDefault(key, defaultValue);
    }

    /**
     * Returns the value of some preference out of OmegaT's preferences file, if
     * it exists.
     * <p>
     * If the key is not found, returns the default value provided and sets the
     * preference to the default value.
     * 
     * @param key
     *            name of the key to look up, usually a static string from this
     *            class
     * @param defaultValue
     *            default value for the key
     * @return preference value as a string
     */
    public static String getPreferenceDefault(String key, String defaultValue) {
        return m_preferences.getPreferenceDefault(key, defaultValue);
    }

    /**
     * Returns the value of some preference out of OmegaT's preferences file, if
     * it exists.
     * <p>
     * If the key is not found, returns the default value provided and sets the
     * preference to the default value.
     * 
     * @param key
     *            name of the key to look up, usually a static string from this
     *            class
     * @param defaultValue
     *            default value for the key
     * @return preference value as enum
     */
    public static <T extends Enum<T>> T getPreferenceEnumDefault(String key, T defaultValue) {
        return m_preferences.getPreferenceEnumDefault(key, defaultValue);
    }

    /**
     * Returns the integer value of some preference out of OmegaT's preferences
     * file, if it exists.
     * <p>
     * If the key is not found, returns the default value provided and sets the
     * preference to the default value.
     * 
     * @param key
     *            name of the key to look up, usually a static string from this
     *            class
     * @param defaultValue
     *            default value for the key
     * @return preference value as an integer
     */
    public static int getPreferenceDefault(String key, int defaultValue) {
        return m_preferences.getPreferenceDefault(key, defaultValue);
    }

    /**
     * Sets the value of some preference. The value will be persisted to disk as
     * XML, serialized via value.toString().
     * 
     * @param name
     *            preference key name, usually Preferences.PREF_...
     * @param value
     *            preference value as an object
     */
    public static void setPreference(String name, Object value) {
        Object oldValue = m_preferences.setPreference(name, value);
        m_propChangeSupport.firePropertyChange(name, oldValue, value);
    }

    /**
     * Register to receive notifications when preferences change.
     * <p>
     * Note: The value returned by {@link PropertyChangeEvent#getNewValue()}
     * will be of the "correct" type (Integer, Boolean, Enum, etc.) but the
     * value returned by {@link PropertyChangeEvent#getOldValue()} will be the
     * String equivalent for storing in XML.
     * 
     * @param listener
     */
    public static void addPropertyChangeListener(PropertyChangeListener listener) {
        m_propChangeSupport.addPropertyChangeListener(listener);
    }

    public static void setFilters(Filters newFilters) {
        Filters oldValue = m_filters;
        m_filters = newFilters;

        File filtersFile = new File(StaticUtils.getConfigDir(), FilterMaster.FILE_FILTERS);
        try {
            FilterMaster.saveConfig(m_filters, filtersFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        m_propChangeSupport.firePropertyChange(Preferences.PROPERTY_FILTERS, oldValue, newFilters);
    }

    public static Filters getFilters() {
        return m_filters;
    }

    public static void setSRX(SRX newSrx) {
        SRX oldValue = m_srx;
        m_srx = newSrx;

        File srxFile = new File(StaticUtils.getConfigDir() + SRX.CONF_SENTSEG);
        try {
            SRX.saveTo(m_srx, srxFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        m_propChangeSupport.firePropertyChange(Preferences.PROPERTY_SRX, oldValue, newSrx);
    }

    public static SRX getSRX() {
        return m_srx;
    }

    public static void save() {
        m_preferences.save();
    }

    public interface IPreferences {

        String getPreference(String key);

        boolean existsPreference(String key);

        boolean isPreference(String key);

        boolean isPreferenceDefault(String key, boolean defaultValue);

        String getPreferenceDefault(String key, String value);

        <T extends Enum<T>> T getPreferenceEnumDefault(String key, T defaultValue);

        int getPreferenceDefault(String key, int defaultValue);

        /** Return the old value, or null if not set */
        Object setPreference(String key, Object value);

        void save();
    }

    /**
     * Initialize the preferences system. This will load
     * <ul>
     * <li>general user prefs
     * <li>user filter settings
     * <li>user segmentation settings
     * </ul>
     * from existing files in {@link StaticUtils#getConfigDir()} (and others for
     * general prefs; see {@link #getPreferencesFile()}) and set things up to
     * create them via {@link #save()} if they don't yet exist.
     * <p>
     * When the preferences system is required but actual user preferences
     * shouldn't be loaded or altered (testing scenarios), use
     * {@link TestPreferencesInitializer} methods or be sure to set the config
     * dir with {@link RuntimePreferences#setConfigDir(String)} before calling
     * this method.
     */
    public static synchronized void init() {
        if (didInit) {
            return;
        }
        didInit = true;
        File srxFile = new File(StaticUtils.getConfigDir(), SRX.CONF_SENTSEG);
        SRX srx = SRX.loadSRX(srxFile);
        if (srx == null) {
            srx = SRX.getDefault();
        }
        m_srx = srx;

        File filtersFile = new File(StaticUtils.getConfigDir(), FilterMaster.FILE_FILTERS);
        Filters filters = null;
        try {
            filters = FilterMaster.loadConfig(filtersFile);
        } catch (Exception ex) {
            Log.log(ex);
        }
        if (filters == null) {
            filters = FilterMaster.createDefaultFiltersConfig();
        }
        m_filters = filters;

        File loadFile = getPreferencesFile();
        File saveFile = new File(StaticUtils.getConfigDir(), Preferences.FILE_PREFERENCES);
        m_preferences = new PreferencesImpl(new PreferencesXML(loadFile, saveFile));
    }

    private static volatile boolean didInit = false;
    private static IPreferences m_preferences;
    private static SRX m_srx;
    private static Filters m_filters;

    // Support for firing property change events
    private static PropertyChangeSupport m_propChangeSupport = new PropertyChangeSupport(Preferences.class);

    /**
     * Gets the prefs file to use. Looks in these places in this order:
     * <ol>
     * <li>omegat.prefs in config dir
     * <li>omegat.prefs in install dir (defaults supplied with local install)
     * </ol>
     */
    private static File getPreferencesFile() {
        File prefsFile = new File(StaticUtils.getConfigDir(), FILE_PREFERENCES);
        if (prefsFile.exists()) {
            return prefsFile;
        }
        // If user prefs don't exist, fall back to defaults (possibly) bundled with OmegaT.
        prefsFile = new File(StaticUtils.installDir(), FILE_PREFERENCES);
        if (prefsFile.exists()) {
            return prefsFile;
        }
        return null;
    }
}
