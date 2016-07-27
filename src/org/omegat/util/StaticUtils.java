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

import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;
import org.omegat.util.Platform.OsType;

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
public class StaticUtils {
    /**
     * Configuration directory on Windows platforms
     */
    private final static String WINDOWS_CONFIG_DIR = "\\OmegaT\\";

    /**
     * Configuration directory on UNIX platforms
     */
    private final static String UNIX_CONFIG_DIR = "/.omegat/";

    /**
     * Configuration directory on Mac OS X
     */
    private final static String OSX_CONFIG_DIR = "/Library/Preferences/OmegaT/";

    /**
     * Script directory
     */
    private final static String SCRIPT_DIR = "script";

    /**
     * Char which should be used instead protected parts. It should be non-letter char, to be able to have
     * correct words counter.
     * 
     * This char can be placed around protected text for separate words inside protected text and words
     * outside if there are no spaces between they.
     */
    public static final char TAG_REPLACEMENT_CHAR = '\b';
    public static final String TAG_REPLACEMENT = "\b";

    /**
     * Contains the location of the directory containing the configuration
     * files.
     */
    private static String m_configDir = null;

    /**
     * Contains the location of the script dir containing the exported text
     * files.
     */
    private static String m_scriptDir = null;

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
        return e.getKeyCode() == code && e.getModifiers() == modifiers;
    }

    /**
     * Returns a list of all files under the root directory by absolute path.
     * 
     * @throws IOException
     */
    public static List<File> buildFileList(File rootDir, boolean recursive) throws IOException {
        int depth = recursive ? Integer.MAX_VALUE : 0;
        return Files.find(rootDir.toPath(), depth, (p, attr) -> p.toFile().isFile(), FileVisitOption.FOLLOW_LINKS)
                .map(Path::toFile)
                .sorted(StreamUtil.localeComparator(File::getPath))
                .collect(Collectors.toList());
    }

    public static List<String> buildRelativeFilesList(File rootDir, List<String> includes, List<String> excludes)
            throws IOException {
        Path root = rootDir.toPath();
        Pattern[] includeMasks = compileFileMasks(includes);
        Pattern[] excludeMasks = compileFileMasks(excludes);
        return Files.find(root, Integer.MAX_VALUE, (p, attr) -> {
            return p.toFile().isFile() && checkFileInclude(root.relativize(p).toString(), includeMasks, excludeMasks);
        }, FileVisitOption.FOLLOW_LINKS).map(p -> root.relativize(p).toString().replace('\\', '/'))
                .sorted(StreamUtil.localeComparator(Function.identity()))
                .collect(Collectors.toList());
    }

    public static boolean checkFileInclude(String filePath, Pattern[] includes, Pattern[] excludes) {
        String normalized = filePath.replace('\\', '/');
        String checkPath = normalized.startsWith("/") ? normalized : '/' + normalized;
        boolean included = Stream.of(includes).map(p -> p.matcher(checkPath)).anyMatch(Matcher::matches);
        boolean excluded = false;
        if (!included) {
            excluded = Stream.of(excludes).map(p -> p.matcher(checkPath)).anyMatch(Matcher::matches);
        }
        return included || !excluded;
    }

    static final Pattern[] NO_PATTERNS = new Pattern[0];

    static Pattern[] compileFileMasks(List<String> masks) {
        if (masks == null) {
            return NO_PATTERNS;
        }
        return masks.stream().map(StaticUtils::compileFileMask).toArray(Pattern[]::new);
    }

    static Pattern compileFileMask(String mask) {
        StringBuilder m = new StringBuilder();
        // "Relative" masks can match at any directory level
        if (!mask.startsWith("/")) {
            mask = "**/" + mask;
        }
        // Masks ending with a slash match everything in subtree
        if (mask.endsWith("/")) {
            mask += "**";
        }
        for (int cp, i = 0; i < mask.length(); i += Character.charCount(cp)) {
            cp = mask.codePointAt(i);
            if (cp >= 'A' && cp <= 'Z') {
                m.appendCodePoint(cp);
            } else if (cp >= 'a' && cp <= 'z') {
                m.appendCodePoint(cp);
            } else if (cp >= '0' && cp <= '9') {
                m.appendCodePoint(cp);
            } else if (cp == '/') {
                if (mask.regionMatches(i, "/**/", 0, 4)) {
                    // The sequence /**/ matches *zero* or more levels
                    m.append("(?:/|/.*/)");
                    i += 3;
                } else if (mask.regionMatches(i, "/**", 0, 3)) {
                    // The sequence /** matches *zero* or more levels
                    m.append("(?:|/.*)");
                    i += 2;
                } else {
                    m.appendCodePoint(cp);
                }
            } else if (cp == '?') {
                // ? matches anything but a directory separator
                m.append("[^/]");
            } else if (cp == '*') {
                if (mask.regionMatches(i, "**/", 0, 3)) {
                    // The sequence **/ matches *zero* or more levels
                    m.append("(?:|.*/)");
                    i += 2;
                } else if (mask.regionMatches(i, "**", 0, 2)) {
                    // **
                    m.append(".*");
                    i++;
                } else {
                    // *
                    m.append("[^/]*");
                }
            } else {
                m.append('\\').appendCodePoint(cp);
            }
        }
        return Pattern.compile(m.toString());
    }

    public interface ITreeIteratorCallback {
        public void processFile(File file) throws Exception;
    }

    public static void iterateFileTree(File rootDir, boolean recursive, ITreeIteratorCallback cb) throws Exception {
        iterateFileTree(rootDir, recursive, new HashSet<File>(), cb);
    }

    private static void iterateFileTree(File rootDir, boolean recursive, Set<File> visited, ITreeIteratorCallback cb)
            throws Exception {
        if (!rootDir.isDirectory()) {
            return;
        }
        File canonical = rootDir.getCanonicalFile();
        if (visited.contains(canonical)) {
            return;
        }
        visited.add(canonical);
        for (File file : rootDir.listFiles()) {
            if (file.isDirectory() && recursive) {
                iterateFileTree(file.getAbsoluteFile(), recursive, visited, cb);
            }
            if (file.isFile()) {
                cb.processFile(file.getAbsoluteFile());
            }
        }
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
    private static String INSTALLDIR = null;

    /**
     * Returns OmegaT installation directory. The code uses this method to look
     * up for OmegaT documentation.
     */
    public static String installDir() {
        if (INSTALLDIR == null) {
            File file = null;
            try {
                URI sourceUri = StaticUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI();
                if (sourceUri.getScheme().equals("file")) {
                    File uriFile = Paths.get(sourceUri).toFile();
                    // If running from a JAR, get the enclosing folder
                    // (the JAR is assumed to be at the installation root)
                    if (uriFile.getName().endsWith(".jar")) {
                        file = uriFile.getParentFile();
                    } else {
                        // Running from an IDE or build tool; use CWD.
                    }
                } else {
                    // Running from Java WebStart; use CWD.
                }
            } catch (URISyntaxException e) {
            }
            if (file == null) {
                file = Paths.get(".").toFile();
            }
            INSTALLDIR = file.getAbsolutePath();
        }
        return INSTALLDIR;
    }

    /**
     * Returns the location of the configuration directory, depending on the
     * user's platform. Also creates the configuration directory, if necessary.
     * If any problems occur while the location of the configuration directory
     * is being determined, an empty string will be returned, resulting in the
     * current working directory being used.
     *
     * <ul><li>Windows XP: &lt;Documents and Settings>\&lt;User name>\Application Data\OmegaT
     * <li>Windows Vista: User\&lt;User name>\AppData\Roaming 
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
        if (m_configDir != null) {
            return m_configDir;
        }

        String cd = RuntimePreferences.getConfigDir();
        if (cd != null) {
            // use the forced specified directory
            m_configDir = new File(cd).getAbsolutePath() + File.separator;
            return m_configDir;
        }

        OsType os = Platform.getOsType(); // name of operating system
        String home; // user home directory

        // get os and user home properties
        try {
            // get the user's home directory
            home = System.getProperty("user.home");
        } catch (SecurityException e) {
            // access to the os/user home properties is restricted,
            // the location of the config dir cannot be determined,
            // set the config dir to the current working dir
            m_configDir = new File(".").getAbsolutePath() + File.separator;

            // log the exception, only do this after the config dir
            // has been set to the current working dir, otherwise
            // the log method will probably fail
            Log.logErrorRB("SU_USERHOME_PROP_ACCESS_ERROR");
            Log.log(e.toString());

            return m_configDir;
        }

        // if os or user home is null or empty, we cannot reliably determine
        // the config dir, so we use the current working dir (= empty string)
        if (os == null || StringUtil.isEmpty(home)) {
            // set the config dir to the current working dir
            m_configDir = new File(".").getAbsolutePath() + File.separator;
            return m_configDir;
        }

        // check for Windows versions
        if (os == OsType.WIN32 || os == OsType.WIN64) {
            String appData = null;

            // We do not use %APPDATA%
            // Trying first Vista/7, because "Application Data" exists also as virtual folder, 
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
                m_configDir = appData + WINDOWS_CONFIG_DIR;
            } else {
                // otherwise set the config dir to the user's home directory,
                // usually
                // C:\Documents and Settings\<User>\OmegaT
                m_configDir = home + WINDOWS_CONFIG_DIR;
            }
        }
        // Check for UNIX varieties
        // Solaris is generally detected as SunOS
        else if (os == OsType.LINUX32 || os == OsType.LINUX64 || os == OsType.OTHER) {
            // set the config dir to the user's home dir + "/.omegat/", so it's
            // hidden
            m_configDir = home + UNIX_CONFIG_DIR;
        }
        // check for Mac OS X
        else if (Platform.isMacOSX()) {
            // set the config dir to the user's home dir +
            // "/Library/Preferences/OmegaT/"
            m_configDir = home + OSX_CONFIG_DIR;
        }
        // other OSes / default
        else {
            // use the user's home directory by default
            m_configDir = home + File.separator;
        }

        // create the path to the configuration dir, if necessary
        if (!m_configDir.isEmpty()) {
            try {
                // check if the dir exists
                File dir = new File(m_configDir);
                if (!dir.exists()) {
                    // create the dir
                    boolean created = dir.mkdirs();

                    // if the dir could not be created,
                    // set the config dir to the current working dir
                    if (!created) {
                        Log.logErrorRB("SU_CONFIG_DIR_CREATE_ERROR");
                        m_configDir = new File(".").getAbsolutePath() + File.separator;
                    }
                }
            } catch (SecurityException e) {
                // the system doesn't want us to write where we want to write
                // reset the config dir to the current working dir
                m_configDir = new File(".").getAbsolutePath() + File.separator;

                // log the exception, but only after the config dir has been
                // reset
                Log.logErrorRB("SU_CONFIG_DIR_CREATE_ERROR");
                Log.log(e.toString());
            }
        }

        // we should have a correct, existing config dir now
        return m_configDir;
    }

    public static String getScriptDir() {
        // If the script directory has already been determined, return it
        if (m_scriptDir != null)
            return m_scriptDir;

        m_scriptDir = getConfigDir() + SCRIPT_DIR + File.separator;

        try {
            // Check if the directory exists
            File dir = new File(m_scriptDir);
            if (!dir.exists()) {
                // Create the directory
                boolean created = dir.mkdirs();

                // If the directory could not be created,
                // set the script directory to config directory
                if (!created) {
                    Log.logErrorRB("SU_SCRIPT_DIR_CREATE_ERROR");
                    m_scriptDir = getConfigDir();
                }
            }
        } catch (SecurityException e) {
            // The system doesn't want us to write where we want to write
            // reset the script dir to the current config dir
            m_scriptDir = getConfigDir();

            // log the exception, but only after the script dir has been reset
            Log.logErrorRB("SU_SCRIPT_DIR_CREATE_ERROR");
            Log.log(e.toString());
        }
        return m_scriptDir;
    }

    /**
     * Encodes the array of bytes to store them in a plain text file.
     */
    public static String uuencode(byte[] buf) {
        if (buf.length <= 0)
            return "";

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
        if (filename.toLowerCase().startsWith(path.toLowerCase()))
            return filename.substring(path.length());
        else
            return filename;
    }

    /**
     * Escapes the passed string for use in regex matching, so special regex
     * characters are interpreted as normal characters during regex searches.
     *
     * This is done by prepending a backslash before each occurrence of the
     * following characters: \^.*+[]{}()&|-:=?!<>
     *
     * @param text
     *            The text to escape
     *
     * @return The escaped text
     */
    public static String escapeNonRegex(String text) {
        return escapeNonRegex(text, true);
    }

    /**
     * Escapes the passed string for use in regex matching, so special regex
     * characters are interpreted as normal characters during regex searches.
     *
     * This is done by prepending a backslash before each occurrence of the
     * following characters: \^.+[]{}()&|-:=!<>
     *
     * If the parameter escapeWildcards is true, asterisks (*) and questions
     * marks (?) will also be escaped. If false, these will be converted to
     * regex tokens (* ->
     *
     * @param text
     *            The text to escape
     * @param escapeWildcards
     *            If true, asterisks and question marks are also escaped. If
     *            false, these are converted to there regex equivalents.
     *
     * @return The escaped text
     */
    public static String escapeNonRegex(String text, boolean escapeWildcards) {
        // handle backslash
        text = text.replaceAll("\\\\", "\\\\\\\\"); // yes, that's the correct
                                                    // nr of backslashes

        // [3021915] Search window - search items containing $ behave strangely
        // If $ is included in "escape" below, it creates a
        // java.lang.StringIndexOutOfBoundsException: String index out of range:
        // 3
        // See http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=5024613
        text = text.replace("$", "\\" + "$");

        // handle rest of characters to be escaped
        // String escape = "^.*+[]{}()&|-:=?!<>";
        for (char c : "^.+[]{}()&|-:=!<>".toCharArray()) {
            text = text.replaceAll("\\" + c, "\\\\" + c);
        }

        // handle "wildcard characters" ? and * (only if requested)
        // do this last, or the additional period (.) will cause trouble
        if (escapeWildcards) {
            // simply escape * and ?
            text = text.replaceAll("\\?", "\\\\?");
            text = text.replaceAll("\\*", "\\\\*");
        } else {
            // convert * (0 or more characters) and ? (1 character)
            // to their regex equivalents (\S* and \S? respectively)
            // text = text.replaceAll("\\?", "\\S?"); // do ? first, or * will
            // be converted twice
            // text = text.replaceAll("\\*", "\\S*");
            // The above lines were not working:
            // [ 1680081 ] Search: simple wilcards do not work
            // The following correction was contributed by Tiago Saboga
            text = text.replaceAll("\\?", "\\\\S"); // do ? first, or * will be
                                                    // converted twice
            text = text.replaceAll("\\*", "\\\\S*");
        }

        return text;
    }

    /**
     * dowload a file from the internet
     */
    public static String downloadFileToString(String urlString) throws IOException {
        URLConnection urlConn;
        InputStream in;

        URL url = new URL(urlString);
        urlConn = url.openConnection();
        //don't wait forever. 10 seconds should be enough.
        urlConn.setConnectTimeout(10000);
        in = urlConn.getInputStream();

        try {
            return IOUtils.toString(in, "UTF-8");
        } finally {
            in.close();
        }
    }

    /**
     * Download a file to the disk
     */
    public static void downloadFileToDisk(String address, String filename) throws MalformedURLException {
        URLConnection urlConn;
        InputStream in = null;
        OutputStream out = null;
        try {
            URL url = new URL(address);
            urlConn = url.openConnection();
            in = urlConn.getInputStream();
            out = new BufferedOutputStream(new FileOutputStream(filename));

            byte[] byteBuffer = new byte[1024];

            int numRead;
            while ((numRead = in.read(byteBuffer)) != -1) {
                out.write(byteBuffer, 0, numRead);
            }
        } catch (IOException ex) {
            Log.logErrorRB("IO exception");
            Log.log(ex);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (IOException ex) {
                // munch this
            }
        }
    }

    public static void extractFileFromJar(File archive, String destination, String... filenames)
            throws IOException {
        InputStream is = new FileInputStream(archive);
        extractFileFromJar(is, destination, filenames);
        is.close();
    }
    
    public static void extractFileFromJar(InputStream in, String destination, String... filenames) throws IOException {
        if (filenames == null || filenames.length == 0) {
            throw new IllegalArgumentException("Caller must provide non-empty list of files to extract.");
        }
        List<String> toExtract = new ArrayList<>(Arrays.asList(filenames));
        try (JarInputStream jis = new JarInputStream(in)) {
            // parse the entries
            JarEntry entry;
            while ((entry = jis.getNextJarEntry()) != null) {
                if (!toExtract.contains(entry.getName())) {
                    continue;
                }
                // match found
                File f = new File(destination, entry.getName());
                f.getParentFile().mkdirs();

                try (FileOutputStream fos = new FileOutputStream(f);
                        BufferedOutputStream out = new BufferedOutputStream(fos)) {
                    byte[] byteBuffer = new byte[1024];
                    int numRead;
                    while ((numRead = jis.read(byteBuffer)) != -1) {
                        out.write(byteBuffer, 0, numRead);
                    }
                }
                toExtract.remove(entry.getName());
            }
        }
        if (!toExtract.isEmpty()) {
            throw new FileNotFoundException("Failed to extract all of the specified files.");
        }
    }

    /**
     * Parse a command line string into arguments, interpreting
     * double and single quotes as Bash does.
     * @param cmd Command string
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
    
} // StaticUtils
