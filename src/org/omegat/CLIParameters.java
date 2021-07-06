/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

package org.omegat;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.omegat.util.OConsts;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;

/**
 * A class to hold all command-line arguments understood by OmegaT.
 * <p>
 * See also: COMMAND_LINE_HELP in Bundle.properties
 *
 * @author Aaron Madlon-Kay
 */
public final class CLIParameters {

    private CLIParameters() {
    }

    /**
     * Regexp for parse parameters.
     */
    protected static final Pattern PARAM = Pattern.compile("\\-\\-([A-Za-z\\-]+)(=(.+))?");

    // Help
    public static final String HELP_SHORT = "-h";
    public static final String HELP = "--help";

    public static final String PROJECT_DIR = "project";

    /**
     * CLI parameter to specify a remote project to load instead of a local one
     */
    public static final String REMOTE_PROJECT = "remote-project";

    // All modes
    public static final String MODE = "mode";
    public static final String CONFIG_FILE = "config-file";
    public static final String RESOURCE_BUNDLE = "resource-bundle";
    public static final String CONFIG_DIR = "config-dir";
    public static final String DISABLE_PROJECT_LOCKING = "disable-project-locking";
    public static final String DISABLE_LOCATION_SAVE = "disable-location-save";
    /**
     * CLI parameter to disable team functionality (treat as local project)
     */
    public static final String NO_TEAM = "no-team";
    /**
     * CLI parameter to specify source tokenizer
     */
    public static final String TOKENIZER_SOURCE = "ITokenizer";
    /**
     * CLI parameter to specify target tokenizer
     */
    public static final String TOKENIZER_TARGET = "ITokenizerTarget";
    // TODO: Document this; see RealProject.patchFileNameForEntryKey()
    public static final String ALTERNATE_FILENAME_FROM = "alternate-filename-from";
    // TODO: Document this; see RealProject.patchFileNameForEntryKey()
    public static final String ALTERNATE_FILENAME_TO = "alternate-filename-to";

    // Non-GUI modes only
    public static final String QUIET = "quiet";
    public static final String SCRIPT = "script";
    public static final String TAG_VALIDATION = "tag-validation";

    // CONSOLE_TRANSLATE mode
    public static final String SOURCE_PATTERN = "source-pattern";

    // CONSOLE_CREATEPSEUDOTRANSLATETMX mode
    public static final String PSEUDOTRANSLATETMX = "pseudotranslatetmx";
    public static final String PSEUDOTRANSLATETYPE = "pseudotranslatetype";

    // CONSOLE_ALIGN mode
    public static final String ALIGNDIR = "alignDir";

    // Development
    public static final String DEV_MANIFESTS = "dev-manifests";

    // Team tool
    public static final String TEAM_TOOL = "team";

    /**
     * Application execution mode. Value of {@link #MODE}.
     */
    enum RUN_MODE {
        GUI, CONSOLE_TRANSLATE, CONSOLE_CREATEPSEUDOTRANSLATETMX, CONSOLE_ALIGN;

        public static RUN_MODE parse(String s) {
            try {
                return valueOf(normalize(s));
            } catch (Exception ex) {
                // default mode
                return GUI;
            }
        }
    }

    /**
     * Choice of types of translation for all segments in the optional, special
     * TMX file that contains all segments of the project. Value of
     * {@link #PSEUDOTRANSLATETYPE}.
     */
    public enum PSEUDO_TRANSLATE_TYPE {
        EQUAL, EMPTY;

        public static PSEUDO_TRANSLATE_TYPE parse(String s) {
            try {
                return valueOf(normalize(s));
            } catch (Exception ex) {
                // default mode
                return EQUAL;
            }
        }
    }

    /**
     * Behavior when validating tags. Value of {@link #TAG_VALIDATION}.
     */
    public enum TAG_VALIDATION_MODE {
        IGNORE, WARN, ABORT;

        public static TAG_VALIDATION_MODE parse(String s) {
            try {
                return valueOf(normalize(s));
            } catch (Exception ex) {
                // default mode
                return IGNORE;
            }
        }
    }

    private static String normalize(String s) {
        return s.toUpperCase(Locale.ENGLISH).replace('-', '_');
    }

    static TreeMap<String, String> parseArgs(String... args) {
        TreeMap<String, String> params = new TreeMap<>();

        /*
         * Parse command line arguments info map.
         *
         * IMPORTANT: If new argument formats are introduced (e.g. short args
         * like -x), the logic in unparseArgs will need to be adjusted!
         */
        for (String arg : args) {
            // Normalize Unicode here because e.g. OS X filesystem is NFD while
            // in Java land things are NFC
            arg = StringUtil.normalizeUnicode(arg);
            Matcher m = PARAM.matcher(arg);
            if (m.matches()) {
                params.put(m.group(1), m.group(3));
            } else if (arg.startsWith(RESOURCE_BUNDLE + "=")) {
                // backward compatibility
                params.put(RESOURCE_BUNDLE, arg.substring(RESOURCE_BUNDLE.length() + 1));
            } else {
                File f = new File(arg).getAbsoluteFile();
                if (f.getName().equals(OConsts.FILE_PROJECT)) {
                    f = f.getParentFile();
                }
                if (StaticUtils.isProjectDir(f)) {
                    params.put(PROJECT_DIR, f.getPath());
                }
            }
        }

        return params;
    }

    /**
     * "Unparse" a map obtained from {@link #parseArgs(String...)}, i.e. reconstruct
     * the list of string arguments originally passed. Note, however, that
     * {@link #PROJECT_DIR} is not included.
     *
     * @param args such as from {@link #parseArgs(String...)}
     * @return list of reconstructed args like {@code --foo=bar}
     */
    static List<String> unparseArgs(Map<String, String> args) {
        return args.entrySet().stream().filter(e -> !e.getKey().equals(PROJECT_DIR)).map(e -> {
            if (e.getValue() == null) {
                return "--" + e.getKey();
            } else {
                return "--" + e.getKey() + "=" + e.getValue();
            }
        }).collect(Collectors.toList());
    }
}
