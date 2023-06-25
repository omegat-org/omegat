/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Didier Briel, Zoltan Bartko, Alex Buloichik
               2008-2011 Didier Briel
               2012 Martin Fleurke, Didier Briel
               2013 Aaron Madlon-Kay, Zoltan Bartko, Didier Briel, Alex Buloichik
               2014 Aaron Madlon-Kay, Alex Buloichik
               2015 Aaron Madlon-Kay
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

import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.io.FileUtils;

/**
 * Static functions taken from CommandThread to reduce file size.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Didier Briel
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Alex Buloichik
 * @author Martin Fleurke
 * @author Aaron Madlon-Kay
 */
public final class StaticUtils {

    private StaticUtils() {
    }

    /**
     * Configuration directory on Windows platforms
     */
    private static final String WINDOWS_CONFIG_DIR = "\\OmegaT\\";

    /**
     * Configuration directory on UNIX platforms
     */
    private static final String UNIX_CONFIG_DIR = "/.omegat/";

    /**
     * Configuration directory on Mac OS X
     */
    private static final String OSX_CONFIG_DIR = "/Library/Preferences/OmegaT/";

    /**
     * Script directory
     */
    private static final String SCRIPT_DIR = "script";

    /**
     * Char which should be used instead protected parts. It should be
     * non-letter char, to be able to have correct words counter.
     *
     * This char can be placed around protected text for separate words inside
     * protected text and words outside if there are no spaces between they.
     */
    public static final char TAG_REPLACEMENT_CHAR = '\b';
    public static final String TAG_REPLACEMENT = "\b";

    /**
     * Contains the location of the directory containing the configuration
     * files.
     */
    private static String configDir = null;

    /**
     * Contains the location of the script dir containing the exported text
     * files.
     */
    private static String scriptDir = null;

    /**
     * Check if specified key pressed.
     *
     * @param e
     *            pressed key event
     * @param code
     *            required key code
     * @param modifiers
     *            required modifiers
     * @return true if checked key pressed
     */
    public static boolean isKey(KeyEvent e, int code, int modifiers) {
        return e.getKeyCode() == code && e.getModifiersEx() == modifiers;
    }

    /**
     * Returns the names of all font families available.
     */
    public static String[] getFontNames() {
        GraphicsEnvironment graphics;
        graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return graphics.getAvailableFontFamilyNames();
    }

    /** Caching install dir */
    private static String installDir = null;

    /**
     * Returns OmegaT installation directory.
     */
    public static String installDir() {
        if (installDir == null) {
            File file = null;
            try {
                URI sourceUri = StaticUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                if (sourceUri.getScheme().equals("file")) {
                    File uriFile = Paths.get(sourceUri).toFile();
                    // If running from a JAR, get the enclosing folder
                    // (the JAR is assumed to be at the installation root,
                    //  and there is also "readme.txt" file).
                    if (uriFile.getName().endsWith(".jar") && new File(uriFile.getParentFile(),
                            "readme.txt").exists()) {
                        file = uriFile.getParentFile();
                    } else {
                        // Running from an IDE or build tool; use CWD.
                        // Sometimes running from build/libs/OmegaT.jar
                    }
                } else {
                    // Running from Java WebStart; use CWD.
                }
            } catch (URISyntaxException e) {
            }
            if (file == null) {
                file = Paths.get(".").toFile();
            }
            installDir = file.getAbsolutePath();
        }
        return installDir;
    }

    /**
     * Returns the location of the configuration directory, depending on the
     * user's platform. Also creates the configuration directory, if necessary.
     * If any problems occur while the location of the configuration directory
     * is being determined, an empty string will be returned, resulting in the
     * current working directory being used.
     *
     * <ul>
     * <li>Windows XP: &lt;Documents and Settings&gt;\&lt;User
     * name&gt;\Application Data\OmegaT
     * <li>Windows Vista: User\&lt;User name&gt;\AppData\Roaming
     * <li>Linux: ~/.omegat
     * <li>Solaris/SunOS: ~/.omegat
     * <li>FreeBSD: ~/.omegat
     * <li>Mac OS X: ~/Library/Preferences/OmegaT
     * <li>Other: User home directory
     * </ul>
     *
     * @return The full path of the directory containing the OmegaT
     *         configuration files, including trailing path separator.
     */
    public static String getConfigDir() {
        // if the configuration directory has already been determined, return it
        if (configDir != null) {
            return configDir;
        }

        String cd = RuntimePreferences.getConfigDir();
        if (cd != null) {
            // use the forced specified directory
            configDir = new File(cd).getAbsolutePath() + File.separator;
            return configDir;
        }

        String home; // user home directory

        // get os and user home properties
        try {
            // get the user's home directory
            home = System.getProperty("user.home");
        } catch (SecurityException e) {
            // access to the os/user home properties is restricted,
            // the location of the config dir cannot be determined,
            // set the config dir to the current working dir
            configDir = new File(".").getAbsolutePath() + File.separator;

            // log the exception, only do this after the config dir
            // has been set to the current working dir, otherwise
            // the log method will probably fail
            Log.logErrorRB("SU_USERHOME_PROP_ACCESS_ERROR");
            Log.log(e.toString());

            return configDir;
        }

        // if os or user home is null or empty, we cannot reliably determine
        // the config dir, so we use the current working dir (= empty string)
        if (StringUtil.isEmpty(home)) {
            // set the config dir to the current working dir
            configDir = new File(".").getAbsolutePath() + File.separator;
            return configDir;
        }

        // check for Windows versions
        if (Platform.isWindows) {
            String appData = null;

            // We do not use %APPDATA%
            // Trying first Vista/7, because "Application Data" exists also as
            // virtual folder,
            // so we would not be able to differentiate with 2000/XP otherwise
            File appDataFile = new File(home, "AppData\\Roaming");
            if (appDataFile.exists()) {
                appData = appDataFile.getAbsolutePath();
            } else {
                // Trying to locate "Application Data" for 2000 and XP
                // C:\Documents and Settings\<User>\Application Data
                appDataFile = new File(home, "Application Data");
                if (appDataFile.exists()) {
                    appData = appDataFile.getAbsolutePath();
                }
            }

            if (!StringUtil.isEmpty(appData)) {
                // if a valid application data dir has been found,
                // append an OmegaT subdir to it
                configDir = appData + WINDOWS_CONFIG_DIR;
            } else {
                // otherwise set the config dir to the user's home directory,
                // usually
                // C:\Documents and Settings\<User>\OmegaT
                configDir = home + WINDOWS_CONFIG_DIR;
            }
            // Check for UNIX varieties
            // Solaris is generally detected as SunOS
        } else if (Platform.isLinux()) {
            // set the config dir to the user's home dir + "/.omegat/", so it's
            // hidden
            configDir = home + UNIX_CONFIG_DIR;
            // check for Mac OS X
        } else if (Platform.isMacOSX()) {
            // set the config dir to the user's home dir +
            // "/Library/Preferences/OmegaT/"
            configDir = home + OSX_CONFIG_DIR;
            // other OSes / default
        } else {
            // use the user's home directory by default
            configDir = home + File.separator;
        }

        // create the path to the configuration dir, if necessary
        if (!configDir.isEmpty()) {
            try {
                // check if the dir exists
                File dir = new File(configDir);
                if (!dir.exists()) {
                    // create the dir
                    boolean created = dir.mkdirs();

                    // if the dir could not be created,
                    // set the config dir to the current working dir
                    if (!created) {
                        Log.logErrorRB("SU_CONFIG_DIR_CREATE_ERROR");
                        configDir = new File(".").getAbsolutePath() + File.separator;
                    }
                }
            } catch (SecurityException e) {
                // the system doesn't want us to write where we want to write
                // reset the config dir to the current working dir
                configDir = new File(".").getAbsolutePath() + File.separator;

                // log the exception, but only after the config dir has been
                // reset
                Log.logErrorRB("SU_CONFIG_DIR_CREATE_ERROR");
                Log.log(e.toString());
            }
        }

        // we should have a correct, existing config dir now
        return configDir;
    }

    public static String getScriptDir() {
        // If the script directory has already been determined, return it
        if (scriptDir != null) {
            return scriptDir;
        }
        scriptDir = getConfigDir() + SCRIPT_DIR + File.separator;

        try {
            // Check if the directory exists
            File dir = new File(scriptDir);
            if (!dir.exists()) {
                // Create the directory
                boolean created = dir.mkdirs();

                // If the directory could not be created,
                // set the script directory to config directory
                if (!created) {
                    Log.logErrorRB("SU_SCRIPT_DIR_CREATE_ERROR");
                    scriptDir = getConfigDir();
                }
            }
        } catch (SecurityException e) {
            // The system doesn't want us to write where we want to write
            // reset the script dir to the current config dir
            scriptDir = getConfigDir();

            // log the exception, but only after the script dir has been reset
            Log.logErrorRB("SU_SCRIPT_DIR_CREATE_ERROR");
            Log.log(e.toString());
        }
        return scriptDir;
    }

    /**
     * Encodes the array of bytes to store them in a plain text file.
     */
    public static String uuencode(byte[] buf) {
        if (buf.length <= 0) {
            return "";
        }
        StringBuilder res = new StringBuilder();
        res.append(buf[0]);
        for (int i = 1; i < buf.length; i++) {
            res.append('#');
            res.append(buf[i]);
        }
        return res.toString();
    }

    /**
     * Decodes the array of bytes that was stored in a plain text file as a
     * string, back to array of bytes.
     */
    public static byte[] uudecode(String buf) {
        String[] bytes = buf.split("#");
        byte[] res = new byte[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            try {
                res[i] = Byte.parseByte(bytes[i]);
            } catch (NumberFormatException e) {
                res[i] = 0;
            }
        }
        return res;
    }

    /**
     * Makes the file name relative to the given path.
     */
    public static String makeFilenameRelative(String filename, String path) {
        if (filename.toLowerCase().startsWith(path.toLowerCase())) {
            return filename.substring(path.length());
        } else {
            return filename;
        }
    }

    /**
     * Translates a string containing word-processing "glob"-style wildcards
     * (<code>?</code> matches a single non-whitespace character, <code>*</code>
     * matches zero or more non-whitespace characters) to standard regex.
     * <p>
     * If <code>spaceMatchesNbsp</code> is <code>true</code>, non-breaking
     * spaces (<code>U+00A0</code>) will also be considered whitespace.
     * <ul>
     * <li><code>?</code> is translated to <code>\S</code> (or
     * <code>[^\s\u00A0]</code>)
     * <li><code>*</code> is translated to <code>\S*</code> (or
     * <code>[^\s\u00A0]*</code>)
     * <li>If <code>spaceMatchesNbsp</code> is <code>true</code>, then
     * '<code> </code>' is translated to <code>( |\u00A0)</code>
     * <li>All other special regex characters are escaped as literals
     * </ul>
     *
     * @param text
     *            The text to escape
     * @param spaceMatchesNbsp
     *            Whether to consider regular spaces to also match non-breaking
     *            spaces
     * @return The escaped text
     */
    public static String globToRegex(String text, boolean spaceMatchesNbsp) {
        String quoted = Pattern.quote(text);

        StringBuilder sb = new StringBuilder(quoted);

        // We should blow up if the standard library implementation ever
        // switches to a scheme other than using \Q and \E.
        assert quoted.startsWith("\\Q");
        assert quoted.endsWith("\\E");

        if (spaceMatchesNbsp) {
            replaceGlobs(sb, "*", "[^\\s\u00A0]*");
            replaceGlobs(sb, "?", "[^\\s\u00A0]");
            replaceGlobs(sb, " ", "(?: |\u00A0)");
        } else {
            replaceGlobs(sb, "*", "\\S*");
            replaceGlobs(sb, "?", "\\S");
        }

        return sb.toString();
    }

    private static void replaceGlobs(StringBuilder haystack, String needle, String replacement) {
        replacement = "\\E" + replacement + "\\Q";
        int current = 0;
        int globIndex = 0;
        while ((globIndex = haystack.indexOf(needle, current)) != -1) {
            haystack.replace(globIndex, globIndex + 1, replacement);
            current = globIndex + replacement.length();
        }
    }

    /**
     * Download a file to memory.
     * 
     * @Deprecated This method is replaced to HttpConnectionUtils.getURL(url,
     *             timeout)
     */
    @Deprecated
    public static String downloadFileToString(URL url, int timeout) throws IOException {
        return HttpConnectionUtils.getURL(url, timeout);
    }

    /**
     * Extracts files from an InputStream representing a zip archive to the
     * specified destination path.
     *
     * @param in
     *            InputStream representing a zip archive
     * @param destination
     *            Path where archive entries will be saved
     * @param filenameFilter
     *            Filter for entry names. Return false to skip extracting an
     *            entry
     * @return List of extracted entry names
     * @throws IOException
     */
    public static List<String> extractFromZip(InputStream in, File destination,
            Predicate<String> filenameFilter) throws IOException {
        List<String> extracted = new ArrayList<>();
        try (PushbackInputStream pis = new PushbackInputStream(in, 2)) {
            byte[] sig = new byte[2];
            pis.read(sig);
            if (!(sig[0] == 0x50 && sig[1] == 0x4b)) {
                throw new IllegalArgumentException("Input stream was not a zip file");
            }
            pis.unread(sig);
            try (ZipInputStream zis = new ZipInputStream(pis)) {
                // parse the entries
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (filenameFilter.test(entry.getName())) {
                        // match found
                        File f = new File(destination, entry.getName());
                        FileUtils.copyToFile(zis, f);
                        extracted.add(entry.getName());
                    }
                }
            }
        }
        return extracted;
    }

    /**
     * Parse a command line string into arguments, interpreting double and
     * single quotes as Bash does.
     * 
     * @param cmd
     *            Command string
     * @return Array of arguments
     */
    public static String[] parseCLICommand(String cmd) {
        cmd = cmd.trim();
        if (cmd.isEmpty()) {
            return new String[] { "" };
        }

        StringBuilder arg = new StringBuilder();
        List<String> result = new ArrayList<String>();

        final char noQuote = '\0';
        char currentQuote = noQuote;
        for (int cp, i = 0; i < cmd.length(); i += Character.charCount(cp)) {
            cp = cmd.codePointAt(i);
            if (cp == currentQuote) {
                currentQuote = noQuote;
            } else if (cp == '"' && currentQuote == noQuote) {
                currentQuote = '"';
            } else if (cp == '\'' && currentQuote == noQuote) {
                currentQuote = '\'';
            } else if (cp == '\\' && i + 1 < cmd.length()) {
                int ncp = cmd.codePointAt(cmd.offsetByCodePoints(i, 1));
                if ((currentQuote == noQuote && Character.isWhitespace(ncp))
                        || (currentQuote == '"' && ncp == '"')) {
                    arg.appendCodePoint(ncp);
                    i += Character.charCount(ncp);
                } else {
                    arg.appendCodePoint(cp);
                }
            } else {
                if (Character.isWhitespace(cp) && currentQuote == noQuote) {
                    if (arg.length() > 0) {
                        result.add(arg.toString());
                        arg = new StringBuilder();
                    } else {
                        // Discard
                    }
                } else {
                    arg.appendCodePoint(cp);
                }
            }
        }
        // Catch last arg
        if (arg.length() > 0) {
            result.add(arg.toString());
        }
        return result.toArray(new String[result.size()]);
    }

    public static boolean isProjectDir(File f) {
        if (f == null || f.getName().isEmpty()) {
            return false;
        }
        File projFile = new File(f.getAbsolutePath(), OConsts.FILE_PROJECT);
        return projFile.isFile();
    }

    /**
     * Check to see if an array contains another array.
     *
     * @param needles
     *            The contained array
     * @param haystack
     *            The containing array
     * @param offset
     *            The offset of {@code haystack} at which to start checking
     * @return Whether or not {@code haystack} contains {@code needles} at
     *         {@code offset}
     * @throws ArrayIndexOutOfBoundsException
     *             If {@code offset} is not a valid index in {@code haystack}
     */
    public static <T> boolean arraysMatchAt(T[] needles, T[] haystack, int offset) {
        if (offset < 0 || offset >= haystack.length) {
            throw new ArrayIndexOutOfBoundsException(offset);
        }
        if (haystack.length - offset < needles.length) {
            return false;
        }
        for (int i = 0; i < needles.length; i++) {
            if (!Objects.equals(haystack[i + offset], needles[i])) {
                return false;
            }
        }
        return true;
    }

    public static String getSupportInfo() {
        Runtime runtime = Runtime.getRuntime();
        String memory = String.format("%dMiB total / %dMiB free / %dMiB max", getMB(runtime.totalMemory()),
                getMB(runtime.freeMemory()), getMB(runtime.maxMemory()));
        return String.format("Version: %s%nPlatform: %s %s%nJava: %s %s%nMemory: %s",
                OStrings.getNameAndVersion(), System.getProperty("os.name"), System.getProperty("os.version"),
                System.getProperty("java.version"), System.getProperty("os.arch"), memory);
    }

    /** Convert bytes into Megabytes */
    public static int getMB(long bytes) {
        return (int) (bytes >> 20);
    }

    /**
     * Get fields declared in the class and its super classes.
     * {@see https://stackoverflow.com/questions/1667854/copy-all-values-from-fields-in-one-class-to-another-through-reflection/35103361#35103361}
     * 
     * @param aClass
     *            target model class.
     * @return list of fields.
     */
    public static List<Field> getAllModelFields(Class<?> aClass) {
        List<Field> fields = new ArrayList<>();
        do {
            Collections.addAll(fields, aClass.getDeclaredFields());
            aClass = aClass.getSuperclass();
        } while (aClass != null);
        return fields;
    }
} // StaticUtils
