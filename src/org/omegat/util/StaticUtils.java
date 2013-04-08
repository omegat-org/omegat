/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Didier Briel, Zoltan Bartko, Alex Buloichik
               2008 - 2011 Didier Briel
               2012 Martin Fleurke, Didier Briel
               2013 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util;

import java.awt.GraphicsEnvironment;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.data.SourceTextEntry;

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
     * Char which should be used instead protected parts. It should be
     * non-letter char, to be able to have correct words counter.
     */
    public static final char TAG_REPLACEMENT = '\b';

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
     * Builds a list of format tags within the supplied string. Format tags are
     * 'protected parts' and OmegaT style tags: &lt;xx02&gt; or &lt;/yy01&gt;.
     */
    public static void buildTagList(String str, SourceTextEntry.ProtectedParts protectedParts, List<String> tagList) {
        List<TagOrder> tags = new ArrayList<TagOrder>();
        if (protectedParts != null) {
            for (String tag : protectedParts.getParts()) {
                int pos = -1;
                while ((pos = str.indexOf(tag, pos + 1)) >= 0) {
                    tags.add(new TagOrder(pos, tag));
                }
            }
        }

        // add tags which not a 'protected parts' but looks like tags
        Pattern placeholderPattern = PatternConsts.OMEGAT_TAG;
        Matcher placeholderMatcher = placeholderPattern.matcher(str);
        while (placeholderMatcher.find()) {
            String foundTag = placeholderMatcher.group(0);
            if (protectedParts == null || !protectedParts.contains(foundTag)) {
                tagList.add(foundTag);
            }
        }

        if (tags.isEmpty()) {
            return;
        }
        Collections.sort(tags, new Comparator<TagOrder>() {
            @Override
            public int compare(TagOrder o1, TagOrder o2) {
                return o1.pos - o2.pos;
            }
        });
        for (TagOrder t : tags) {
            tagList.add(t.tag);
        }
    }

    /**
     * Builds a list of format tags within the supplied string. Format tags are
     * OmegaT style tags: &lt;xx02&gt; or &lt;/yy01&gt;.
     * @return a string containing the tags
     */
    public static String buildTagListForRemove(String str) {
        String res = "";
        Pattern placeholderPattern = PatternConsts.OMEGAT_TAG;
        Matcher placeholderMatcher = placeholderPattern.matcher(str);
        while (placeholderMatcher.find()) {
            res += placeholderMatcher.group(0);
        }
        return res;
    }

    static class TagOrder {
        final int pos;
        final String tag;

        public TagOrder(int pos, String tag) {
            this.pos = pos;
            this.tag = tag;
        }
    }

    /**
     * Indicates the type of a tag, e.g.:
     * <ul>
     * <li>&lt;foo> = START</li>
     * <li>&lt;/foo> = END</li>
     * <li>&lt;bar/> = SINGLE</li>
     * </ul>
     */
    public static enum TagType {
        START, END, SINGLE
    }

    /**
     * Detect the type of a tag, e.g. one of {@link TagType}.
     * @param tag String containing full text of tag
     * @return The type of the tag
     */
    public static TagType getTagType(String tag) {
        if (tag.length() < 4 || (!tag.startsWith("<") && !tag.endsWith(">"))) {
            return TagType.SINGLE;
        }
        
        if (tag.startsWith("</")) {
            return TagType.END;
        } else if (tag.endsWith("/>")) {
            return TagType.SINGLE;
        }

        return TagType.START;
    }

    /**
     * Retrieve info about a tag.
     * @param tag String containing full text of tag
     * @return A {@link TagInfo} with tag's name and type
     */
    public static TagInfo getTagInfo(String tag) {
        Matcher m = PatternConsts.OMEGAT_TAG_DECOMPILE.matcher(tag);
        String name = m.find() ? m.group(2) + m.group(3) : tag;
        return new TagInfo(name, getTagType(tag));
    }

        
    /**
     * For a given tag, retrieve its pair e.g. &lt;/foo> for &lt;foo>.
     * @param info A {@link TagInfo} describing the tag
     * @return The tag's pair as a string, or null for self-contained tags
     */
    public static String getPairedTag(TagInfo info) {
        switch(info.type) {
        case START:
            return String.format("</%s>", info.name);
        case END:
            return String.format("<%s>", info.name);
        case SINGLE:
        default:
            return null;
        }
    }

    
    /**
     * A tuple containing 
     * <ul><li>A tag's name</li>
     * <li>The tag's {@link TagType} type</li>
     * </ul>
     */
    public static class TagInfo {
        public final TagType type;
        public final String name;
        
        public TagInfo (String name, TagType type) {
            this.name = name;
            this.type = type;
        }
    }

    /**
     * Returns a list of all files under the root directory by absolute path.
     */
    public static void buildFileList(List<String> lst, File rootDir, boolean recursive) {
        // read all files in current directory, recurse into subdirs
        // append files to supplied list
        File flist[] = null;
        try {
            flist = rootDir.listFiles();
        } catch (Exception e) {
            // don't care what exception is there.
            // by contract, only a SecurityException is possible, but who
            // knows...
        }
        // if IOException occured, flist is null
        // and we simply return
        if (flist == null)
            return;

        for (File file : flist) {
            if (file.isDirectory()) {
                continue; // recurse into directories later
            }
            lst.add(file.getAbsolutePath());
        }
        if (recursive) {
            for (File file : flist) {
                if (isProperDirectory(file)) // Ignores some directories
                {
                    // now recurse into subdirectories
                    buildFileList(lst, file, true);
                }
            }
        }
    }

    // returns a list of all files under the root directory
    // by absolute path
    public static void buildDirList(List<String> lst, File rootDir) {
        // read all files in current directory, recurse into subdirs
        // append files to supplied list
        File[] flist = rootDir.listFiles();
        for (File file : flist) {
            if (isProperDirectory(file)) // Ignores some directories
            {
                // now recurse into subdirectories
                lst.add(file.getAbsolutePath());
                buildDirList(lst, file);
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

    // List of CVS or SVN folders
    private static final String CVS_SVN_FOLDERS = "(CVS)|(.svn)|(_svn)";

    private static final Pattern IGNORED_FOLDERS = Pattern.compile(CVS_SVN_FOLDERS);

    /**
     * Tests whether a directory has to be used
     *
     * @return <code>true</code> or <code>false</code>
     */
    private static boolean isProperDirectory(File file) {
        if (file.isDirectory()) {
            Matcher directoryMatch = IGNORED_FOLDERS.matcher(file.getName());
            if (directoryMatch.matches())
                return false;
            else
                return true;
        } else
            return false;
    }

    /**
     * Converts a single char into valid XML. Output stream must convert stream
     * to UTF-8 when saving to disk.
     */
    public static String makeValidXML(char c) {
        switch (c) {
        // case '\'':
        // return "&apos;";
        case '&':
            return "&amp;";
        case '>':
            return "&gt;";
        case '<':
            return "&lt;";
        case '"':
            return "&quot;";
        default:
            return String.valueOf(c);
        }
    }

    /**
     * Converts XML entities to characters.
     */
    public static String entitiesToCharacters(String text) {

        if (text.indexOf("&gt;") >= 0) {
            text = text.replaceAll("&gt;", ">");
        }
        if (text.indexOf("&lt;") >= 0) {
            text = text.replaceAll("&lt;", "<");
        }
        if (text.indexOf("&quot;") >= 0) {
            text = text.replaceAll("&quot;", "\"");
        }
       // If makeValidXML converts ' to apos;, the following lines should be uncommented
        /* if (text.indexOf("&apos;") >= 0) {
            text = text.replaceAll("&apos;", "'");
        }*/
        if (text.indexOf("&amp;") >= 0) {
            text = text.replaceAll("&amp;", "&");
        }
        return text;
    }

    /**
     * Converts a stream of plaintext into valid XML. Output stream must convert
     * stream to UTF-8 when saving to disk.
     */
    public static String makeValidXML(String plaintext) {
        char c;
        StringBuffer out = new StringBuffer();
        String text = fixChars(plaintext);
        for (int i = 0; i < text.length(); i++) {
            c = text.charAt(i);
            out.append(makeValidXML(c));
        }
        return out.toString();
    }

    /** Compresses spaces in case of non-preformatting paragraph. */
    public static String compressSpaces(String str) {
        int strlen = str.length();
        StringBuffer res = new StringBuffer(strlen);
        boolean wasspace = true;
        for (int i = 0; i < strlen; i++) {
            char ch = str.charAt(i);
            boolean space = Character.isWhitespace(ch);
            if (space) {
                if (!wasspace)
                    wasspace = true;
            } else {
                if (wasspace && res.length() > 0)
                    res.append(' ');
                res.append(ch);
                wasspace = false;
            }
        }
        return res.toString();
    }

    /**
     * Extracts an element of a class path.
     *
     * @param fullcp
     *            the classpath
     * @param posInsideElement
     *            position inside a class path string, that fits inside some
     *            classpath element.
     */
    private static String classPathElement(String fullcp, int posInsideElement) {
        // semicolon before the path to the Jar
        int semicolon1 = fullcp.lastIndexOf(File.pathSeparatorChar, posInsideElement);
        // semicolon after the path to the Jar
        int semicolon2 = fullcp.indexOf(File.pathSeparatorChar, posInsideElement);
        if (semicolon1 < 0)
            semicolon1 = -1;
        if (semicolon2 < 0)
            semicolon2 = fullcp.length();
        return fullcp.substring(semicolon1 + 1, semicolon2);
    }

    /** Trying to see if this ending is inside the classpath */
    private static String tryThisClasspathElement(String cp, String ending) {
        try {
            int pos = cp.indexOf(ending);
            if (pos >= 0) {
                String path = classPathElement(cp, pos);
                path = path.substring(0, path.indexOf(ending));
                return path;
            }
        } catch (Exception e) {
            // should never happen, but just in case ;-)
        }
        return null;
    }

    /** Caching install dir */
    private static String INSTALLDIR = null;

    /**
     * Returns OmegaT installation directory. The code uses this method to look
     * up for OmegaT documentation.
     */
    public static String installDir() {
        if (INSTALLDIR != null)
            return INSTALLDIR;

        String cp = System.getProperty("java.class.path");
        String path;

        // running from a Jar ?
        path = tryThisClasspathElement(cp, OConsts.APPLICATION_JAR);

        // again missed, we're not running from Jar, most probably debug mode
        if (path == null)
            path = tryThisClasspathElement(cp, OConsts.DEBUG_CLASSPATH);

        // WTF?!! using current directory
        if (path == null)
            path = ".";

        // absolutizing the path
        path = new File(path).getAbsolutePath();

        INSTALLDIR = path;
        return path;
    }

    /**
     * Returns the location of the configuration directory, depending on the
     * user's platform. Also creates the configuration directory, if necessary.
     * If any problems occur while the location of the configuration directory
     * is being determined, an empty string will be returned, resulting in the
     * current working directory being used.
     *
     * Windows XP : <Documents and Settings>\<User name>\Application Data\OmegaT
     * Windows Vista : User\<User name>\AppData\Roaming Linux: <User
     * Home>/.omegat Solaris/SunOS: <User Home>/.omegat FreeBSD: <User
     * Home>/.omegat Mac OS X: <User Home>/Library/Preferences/OmegaT Other:
     * User home directory
     *
     * @return The full path of the directory containing the OmegaT
     *         configuration files, including trailing path separator.
     *
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public static String getConfigDir() {
        // if the configuration directory has already been determined, return it
        if (m_configDir != null)
            return m_configDir;

        String cd = RuntimePreferences.getConfigDir();
        if (cd != null) {
            // use the forced specified directory
            m_configDir = new File(cd).getAbsolutePath() + File.separator;
            return m_configDir;
        }

        String os; // name of operating system
        String home; // user home directory

        // get os and user home properties
        try {
            // get the name of the operating system
            os = System.getProperty("os.name");

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
        if ((os == null) || (os.length() == 0) || (home == null) || (home.length() == 0)) {
            // set the config dir to the current working dir
            m_configDir = new File(".").getAbsolutePath() + File.separator;
            return m_configDir;
        }

        // check for Windows versions
        if (os.startsWith("Windows")) {
            // Trying to locate "Application Data" for 2000 and XP
            // C:\Documents and Settings\<User>\Application Data
            // We do not use %APPDATA%
            File appDataFile = new File(home, "Application Data");
            String appData = null;
            if (appDataFile.exists())
                appData = appDataFile.getAbsolutePath();
            else // No "Application Data", we're trying Vista
            {
                File appDataFileVista = new File(home, "AppData\\Roaming");
                if (appDataFileVista.exists())
                    appData = appDataFileVista.getAbsolutePath();
            }

            if ((appData != null) && (appData.length() > 0)) {
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
        else if (os.equals("Linux") || os.equals("SunOS") || os.equals("Solaris") || os.equals("FreeBSD") ||
                 os.equals("NetBSD") || os.equals("DragonFly")) {
            // set the config dir to the user's home dir + "/.omegat/", so it's
            // hidden
            m_configDir = home + UNIX_CONFIG_DIR;
        }
        // check for Mac OS X
        else if (os.equals("Mac OS X")) {
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
        if (m_configDir.length() > 0) {
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
     * Returns true if running on Mac OS X
     */
    public static boolean onMacOSX() {
        // get os property
        String os; // name of operating system
        try {
            // get the name of the operating system
            os = System.getProperty("os.name");
        } catch (SecurityException e) {
            // access to the os property is restricted,
            // assume we're not on a Mac
            return false;
        }

        return os.equals("Mac OS X");
    }

    /**
     * Strips all protected parts.
     * 
     * @param str
     *            source string
     * @param ste
     *            info about protected parts
     * @param changeToReplacement
     *            true if should be replaced by special char, false if should be
     *            just removed
     */
    public static String stripProtectedParts(String str, SourceTextEntry ste) {
        String s = str;
        for (String tag : ste.getProtectedParts().getParts()) {
            s = s.replace(tag, TAG_REPLACEMENT + "");
        }
        return s;
    }

    /**
     * Strips all XML tags (converts to plain text). Tags detected only by
     * pattern. Protected parts are not used.
     */
    public static String stripXmlTags(String xml) {
        return PatternConsts.OMEGAT_TAG.matcher(xml).replaceAll("");
    }

    /**
     * Compares two strings for equality. Handles nulls: if both strings are
     * nulls they are considered equal.
     */
    public static boolean equal(String one, String two) {
        return (one == null && two == null) || (one != null && one.equals(two));
    }

    /**
     * Encodes the array of bytes to store them in a plain text file.
     */
    public static String uuencode(byte[] buf) {
        if (buf.length <= 0)
            return new String();

        StringBuffer res = new StringBuffer();
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
     *
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
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
     *
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
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
        String escape = "^.+[]{}()&|-:=!<>";
        for (int i = 0; i < escape.length(); i++)
            text = text.replaceAll("\\" + escape.charAt(i), "\\\\" + escape.charAt(i));

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
     * Formats UI strings.
     *
     * Note: This is only a first attempt at putting right what goes wrong in
     * MessageFormat. Currently it only duplicates single quotes, but it doesn't
     * even test if the string contains parameters (numbers in curly braces),
     * and it doesn't allow for string containg already escaped quotes.
     *
     * @param str
     *            The string to format
     * @param arguments
     *            Arguments to use in formatting the string
     *
     * @return The formatted string
     *
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public static String format(String str, Object... arguments) {
        // MessageFormat.format expects single quotes to be escaped
        // by duplicating them, otherwise the string will not be formatted
        str = str.replaceAll("'", "''");
        return MessageFormat.format(str, arguments);
    }

    /**
     * dowload a file from the internet
     */
    public static String downloadFileToString(String urlString) throws IOException {
        URLConnection urlConn = null;
        InputStream in = null;

        URL url = new URL(urlString);
        urlConn = url.openConnection();
        //don't wait forever. 10 seconds should be enough.
        urlConn.setConnectTimeout(10000);
        in = urlConn.getInputStream();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            LFileCopy.copy(in, out);
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                // munch this
            }
        }
        return new String(out.toByteArray(), "UTF-8");
    }

    /**
     * Download a file to the disk
     */
    public static void downloadFileToDisk(String address, String filename) throws MalformedURLException {
        URLConnection urlConn = null;
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

    public static void extractFileFromJar(String archive, List<String> filenames, String destination)
            throws IOException {
        // open the jar (zip) file
        JarFile jar = new JarFile(archive);

        // parse the entries
        Enumeration<JarEntry> entryEnum = jar.entries();
        while (entryEnum.hasMoreElements()) {
            JarEntry file = entryEnum.nextElement();
            if (filenames.contains(file.getName())) {
                // match found
                File f = new File(destination + File.separator + file.getName());
                InputStream in = jar.getInputStream(file);
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(f));

                byte[] byteBuffer = new byte[1024];

                int numRead = 0;
                while ((numRead = in.read(byteBuffer)) != -1) {
                    out.write(byteBuffer, 0, numRead);
                }

                in.close();
                out.close();
            }
        }
        jar.close();
    }

    /**
     * Replace invalid XML chars by spaces. See supported chars at
     * http://www.w3.org/TR/2006/REC-xml-20060816/#charsets.
     *
     * @param str
     *            input stream
     * @return result stream
     */
    public static String fixChars(String str) {
        char[] result = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c < 0x20) {
                if (c != 0x09 && c != 0x0A && c != 0x0D) {
                    c = ' ';
                }
            } else if (c >= 0x20 && c <= 0xD7FF) {
            } else if (c >= 0xE000 && c <= 0xFFFD) {
            } else if (c >= 0x10000 && c <= 0x10FFFF) {
            } else {
                c = ' ';
            }
            result[i] = c;
        }
        return new String(result);
    }

    /**
     * Reconstruct a tag from its {@link TagInfo}.
     * 
     * @param info
     *            Description of tag
     * @return Reconstructed original tag
     */
    public static String getOriginalTag(TagInfo info) {
        switch (info.type) {
        case START:
            return String.format("<%s>", info.name);
        case END:
            return String.format("</%s>", info.name);
        case SINGLE:
            return String.format("<%s/>", info.name);
        }
        return null;
    }

    /**
     * Sort tags by order of their appearance in a reference string.
     */
    public static class TagComparator implements Comparator<String> {

        private final String source;

        public TagComparator(String source) {
            super();
            this.source = source;
        }

        public int compare(String tag1, String tag2) {
            // Check for equality
            if (tag1.equals(tag2)) {
                return 0;
            }
            // Check to see if one tag encompases the other
            if (tag1.startsWith(tag2)) {
                return -1;
            } else if (tag2.startsWith(tag1)) {
                return 1;
            }
            // Check which tag comes first
            int index1 = source.indexOf(tag1);
            int index2 = source.indexOf(tag2);
            if (index1 == index2) {
                int len1 = tag1.length();
                int len2 = tag2.length();
                if (len1 > len2) {
                    return -1;
                } else if (len2 > len1) {
                    return 1;
                } else {
                    return tag1.compareTo(tag2);
                }
            }
            return index1 > index2 ? 1 : -1;
        }
    }

} // StaticUtils
