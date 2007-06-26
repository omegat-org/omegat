/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Didier Briel
               Home page: http://www.omegat.org/omegat/omegat.html
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.util;

import java.io.File;
import java.awt.GraphicsEnvironment;
import java.text.BreakIterator;
import java.text.MessageFormat;
import java.util.List;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Static functions taken from
 * CommandThread to reduce file size.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Didier Briel
 */
public class StaticUtils
{
	/**
	 * Configuration directory on Windows platforms
	 */
	private final static String WINDOWS_CONFIG_DIR = "\\OmegaT\\";              // NOI18N

	/**
	 * Configuration directory on UNIX platforms
	 */
	private final static String UNIX_CONFIG_DIR = "/.omegat/";                  // NOI18N
	
	/**
	 * Configuration directory on Mac OS X
	 */
	private final static String OSX_CONFIG_DIR = "/Library/Preferences/OmegaT/";// NOI18N
	
	/**
	 * Contains the location of the directory containing the configuration files.
	 */
	private static String m_configDir = null;
    
    /**
     * Builds a list of format tags within the supplied string.
     * Format tags are OmegaT style tags: &lt;xx02&gt; or &lt;/yy01&gt;.
     */
    public static void buildTagList(String str, ArrayList tagList)
    {
        // The code is nearly the same as in listShortTags in Entry.java
        final int STATE_NORMAL = 1;
        final int STATE_COLLECT_TAG = 2;

        String tag = "";							// NOI18N
        char c;
        
        int state = STATE_NORMAL;
        
        // extract tags from source string
        for (int j=0; j<str.length(); j++)
        {
            c = str.charAt(j);
            if (c == '<') // Possible start of a tag
            {
                tag="";                                                         // NOI18N
                state = STATE_COLLECT_TAG;
            }
            else if (c == '>') // Possible end of a tag
            {
                // checking if the tag looks like OmegaT tag, 
                // not 100% correct, but is the best what I can think of now
                if(PatternConsts.OMEGAT_TAG_ONLY.matcher(tag).matches())
                    tagList.add(tag);
                state = STATE_NORMAL;
                tag = "";												// NOI18N
            }
            else if (state == STATE_COLLECT_TAG)
                tag += c;
        }
    }
    
    /**
     * Returns a list of all files under the root directory
     * by absolute path.
     */
    public static void buildFileList(ArrayList lst, File rootDir,
            boolean recursive)
    {
        int i;
        // read all files in current directory, recurse into subdirs
        // append files to supplied list
        File flist[] = null;
        try
        {
            flist = rootDir.listFiles();
        }
        catch( Exception e )
        {
            // don't care what exception is there.
            // by contract, only a SecurityException is possible, but who knows...
        }
        // if IOException occured, flist is null
        // and we simply return
        if( flist==null )
            return;
        
        for (i=0; i<Array.getLength(flist); i++)
        {
            if (flist[i].isDirectory())
            {
                continue;	// recurse into directories later
            }
            lst.add(flist[i].getAbsolutePath());
        }
        if (recursive)
        {
            for (i=0; i<Array.getLength(flist); i++)
            {
                if (flist[i].isDirectory())
                {
                    // now recurse into subdirectories
                    buildFileList(lst, flist[i], true);
                }
            }
        }
    }
    
    // returns a list of all files under the root directory
    //  by absolute path
    public static void buildDirList(ArrayList lst, File rootDir)
    {
        int i;
        // read all files in current directory, recurse into subdirs
        // append files to supplied list
        File [] flist = rootDir.listFiles();
        for (i=0; i<Array.getLength(flist); i++)
        {
            if (flist[i].isDirectory())
            {
                // now recurse into subdirectories
                lst.add(flist[i].getAbsolutePath());
                buildDirList(lst, flist[i]);
            }
        }
    }
    
    private static BreakIterator wordBreaker = null;
    /** Returns an iterator to break sentences into words. */
    public static BreakIterator getWordBreaker()
    {
        //if (wordBreaker==null)
        //    wordBreaker = new WordIterator();
        //return wordBreaker;

        return new WordIterator();

        // HP: This is a fix for bug 1589484. If you use only one
        // WordIterator instance, it will lead to problems when
        // using multiple threads, as OmegaT does. Sometimes, in
        // the middle of breaking a string, another thread may set
        // a different text, and then you get index out of bounds
        // exceptions. By returning a new WordIterator each time
        // one is requested, this problem is solved, and it doesn't
        // hurt performance either.
    }

    /**
      * Contains a list of tokens for each *unique* string.
      * By not storing a list of tokens for every string,
      * memory is saved. Token lists are not saved when
      * all tokens are requested. Again to save memory.
      */
    private static java.util.Map tokenCache = new java.util.Hashtable();

    /** Removes all token lists from the cache. */
    public static void clearTokenCache() {
        tokenCache.clear();
    }

    /**
      * Breaks a string into tokens.
      * <p>
      * Examples:
      * <ul>
      * <li> This is a semi-good way. -> "this", "is", "a", "semi-good", "way"
      * <li> Fine, thanks, and you? -> "fine", "thanks", "and", "you"
      * <li> C&all this action -> "call", "this", "action" ('&' is eaten)
      * </ul>
      * <p>
      * Also skips OmegaT tags.
      *
      * @param str string to tokenize
      * @return List of all tokens (words only)
      */
    public static List tokenizeText(String str) {
        return tokenizeText(str, false);
    }

    /**
      * Breaks a string into tokens.
      * <p>
      * Examples:
      * <ul>
      * <li> This is a semi-good way. -> "this", "is", "a", "semi-good", "way"
      * <li> Fine, thanks, and you? -> "fine", "thanks", "and", "you"
      * <li> C&all this action -> "call", "this", "action" ('&' is eaten)
      * </ul>
      * <p>
      * OmegaT tags and other non-word tokens are skipped if the parameter "all" is false.
      *
      * @param str string to tokenize
      * @param all If true, numbers, tags, and other non-word tokens are included in the list
      * @return List of tokens (all)
      */
    public static List tokenizeText(String str, boolean all) {
        // check if we've already tokenized this string
        // no sense in retokenizing identical strings
        // don't check if the caller wants all tokens
        List tokens = null;
        if (!all) {
            tokens = (List)tokenCache.get(str);
            if (tokens != null)
                return tokens;
        }

        // create a new token list
        // and put it in the cache if not all tokens are requested
        tokens = new ArrayList();
        if (!all)
            tokenCache.put(str, tokens);

        // fixes bug nr. 1382810 (StringIndexOutOfBoundsException)
        if (str.length() == 0)
            return tokens;

        // get a word breaker
        str = str.toLowerCase(); // HP: possible error, this makes "A" and "a" match, CHECK AND FIX
        BreakIterator breaker = getWordBreaker();
        breaker.setText(str);

        int start = breaker.first();
        for (int end = breaker.next();
             end!=BreakIterator.DONE; 
             start = end, end = breaker.next())
        {
            String tokenStr = str.substring(start,end);
            boolean word = false;
            for (int i=0; i<tokenStr.length(); i++)
            {
                char ch = tokenStr.charAt(i);
                if (Character.isLetter(ch))
                {
                    word = true;
                    break;
                }
            }

            if (all || (word && !PatternConsts.OMEGAT_TAG.matcher(tokenStr).matches()))
            {
                Token token = new Token(tokenStr, start);
                tokens.add(token);
            }
        }

        return tokens;
    }

    /**
     * Returns the names of all font families available.
     */
    public static String[] getFontNames()
    {
        GraphicsEnvironment graphics;
        graphics = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return graphics.getAvailableFontFamilyNames();
    }
    
    /**
     * Tests, whether one list of tokens is fully contained (is-a subset)
     * in other list of tokens
     */
    public static boolean isSubset(List maybeSubset, List maybeSuperset)
    {
        for(int i=0; i<maybeSubset.size(); i++)
            if( !maybeSuperset.contains(maybeSubset.get(i)) )
                return false;
        return true;
    }
    
    /**
     * Converts a single char into valid XML.
     * Output stream must convert stream to UTF-8 when saving to disk.
     */
    public static String makeValidXML(char c)
    {
        switch( c )
        {
            //case '\'':
            //    return "&apos;";	// NOI18N
            case '&':
                return "&amp;";	// NOI18N
            case '>':
                return "&gt;";	// NOI18N
            case '<':
                return "&lt;";	// NOI18N
            case '"':
                return "&quot;";	// NOI18N
            default:
                return String.valueOf(c);
        }
    }
    
    /**
     * Converts a stream of plaintext into valid XML.
     * Output stream must convert stream to UTF-8 when saving to disk.
     */
    public static String makeValidXML(String plaintext)
    {
        char c;
        StringBuffer out = new StringBuffer();
        for (int i=0; i<plaintext.length(); i++)
        {
            c = plaintext.charAt(i);
            out.append(makeValidXML(c));
        }
        return out.toString();
    }
    
    /** Compresses spaces in case of non-preformatting paragraph. */
    public static String compressSpaces(String str)
    {
        int strlen = str.length();
        StringBuffer res = new StringBuffer(strlen);
        boolean wasspace = true;
        for(int i=0; i<strlen; i++)
        {
            char ch = str.charAt(i);
            boolean space = Character.isWhitespace(ch);
            if( space )
            {
                if( !wasspace )
                    wasspace = true;
            }
            else
            {
                if( wasspace && res.length()>0 )
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
     * @param fullcp the classpath
     * @param posInsideElement position inside a class path string, that fits 
     *                          inside some classpath element.
     */
    private static String classPathElement(String fullcp, int posInsideElement)
    {
        // semicolon before the path to the Jar
        int semicolon1 = fullcp.lastIndexOf(File.pathSeparatorChar, posInsideElement);
        // semicolon after the path to the Jar
        int semicolon2 = fullcp.indexOf(File.pathSeparatorChar, posInsideElement);
        if( semicolon1<0 )
            semicolon1 = -1;
        if( semicolon2<0 )
            semicolon2 = fullcp.length();
        return fullcp.substring(semicolon1+1, semicolon2);
    }
    
    /** Trying to see if this ending is inside the classpath */
    private static String tryThisClasspathElement(String cp, String ending)
    {
        try
        {
            int pos = cp.indexOf(ending);
            if( pos>=0 )
            {
                String path = classPathElement(cp, pos);
                path = path.substring(0, path.indexOf(ending));
                return path;
            }
        }
        catch( Exception e )
        {
            // should never happen, but just in case ;-)
        }
        return null;
    }
    
    /** Caching install dir */
    private static String INSTALLDIR = null;
    
    /**
     * Returns OmegaT installation directory.
     * The code uses this method to look up for OmegaT documentation.
     */
    public static String installDir()
    {
        if( INSTALLDIR!=null )
            return INSTALLDIR;
        
        String cp = System.getProperty("java.class.path");                      // NOI18N
        String path;
        
        // running from a Jar ?
        path = tryThisClasspathElement(cp, OConsts.APPLICATION_JAR);
        
        // again missed, we're not running from Jar, most probably debug mode
        if( path==null )
            path = tryThisClasspathElement(cp, OConsts.DEBUG_CLASSPATH);
        
        // WTF?!! using current directory
        if( path==null )
            path = ".";                                                         // NOI18N
        
        // absolutizing the path
        path = new File(path).getAbsolutePath();
        
        INSTALLDIR = path;
        return path;
    }
    
    /**
     * Returns the location of the configuration directory, depending on
     * the user's platform. Also creates the configuration directory,
     * if necessary. If any problems occur while the location of the
     * configuration directory is being determined, an empty string will
     * be returned, resulting in the current working directory being used.
     *
     * Windows:  <Documents and Settings>\<User name>\Application Data\OmegaT
     * Linux:    <User Home>/.omegat
     * Solaris:  <User Home>/.omegat
     * FreeBSD:  <User Home>/.omegat
     * Mac OS X: <User Home>/Library/Preferences/OmegaT
     * Other:    User home directory
     *
     * @return The full path of the directory containing the OmegaT
     *         configuration files, including trailing path separator.
     *
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public static String getConfigDir()
    {
        // if the configuration directory has already been determined, return it
        if (m_configDir != null)
            return m_configDir;
        
        String os;   // name of operating system
        String home; // user home directory
        
        // get os and user home properties
        try
        {
            // get the name of the operating system
            os = System.getProperty("os.name");                                 // NOI18N

            // get the user's home directory
            home = System.getProperty("user.home");                             // NOI18N
        }
        catch (SecurityException e)
        {
            // access to the os/user home properties is restricted,
            // the location of the config dir cannot be determined,
            // set the config dir to the current working dir
            m_configDir = new File(".").getAbsolutePath();                      // NOI18N

            // log the exception, only do this after the config dir
            // has been set to the current working dir, otherwise
            // the log method will probably fail
            Log.logErrorRB("SU_USERHOME_PROP_ACCESS_ERROR");
            Log.log(e.toString());

            return m_configDir;
        }

        // if os or user home is null or empty, we cannot reliably determine
        // the config dir, so we use the current working dir (= empty string)
        if ( (os == null) || (os.length() == 0) ||
                (home == null) || (home.length() == 0))
        {
            // set the config dir to the current working dir
            m_configDir = new File(".").getAbsolutePath();                      // NOI18N
            return m_configDir;
        }
        
        // check for Windows versions
        if (os.startsWith("Windows"))                                           // NOI18N
        {
            // get the user's application data directory through the environment
            // variable %APPDATA%, which usually points to the directory
            // C:\Documents and Settings\<User>\Application Data
            File appDataFile = new File(home, "Application Data");              // NOI18N
            String appData;
            if (appDataFile.exists())
                appData = appDataFile.getAbsolutePath();
            else
                appData = null;                                                 // NOI18N
            
            if ((appData != null) && (appData.length() > 0))
            {
                // if a valid application data dir has been found, 
                // append an OmegaT subdir to it
                m_configDir = appData + WINDOWS_CONFIG_DIR;
            }
            else
            {
                // otherwise set the config dir to the user's home directory, usually
                // C:\Documents and Settings\<User>
                m_configDir = home;
            }
        }
        // check for UNIX varieties
        else if (os.equals("Linux") || os.equals("Solaris") ||                  // NOI18N
                os.equals("FreeBSD"))                                           // NOI18N
        {
            // set the config dir to the user's home dir + "/.omegat", so it's hidden
            m_configDir = home + UNIX_CONFIG_DIR;
        }
        // check for Mac OS X
        else if (os.equals("Mac OS X"))                                         // NOI18N
        {
            // set the config dir to the user's home dir + "/Library/Preferences/OmegaT"
            m_configDir = home + OSX_CONFIG_DIR;
        }
        // other OS'es / default
        else
        {
            // use the user's home directory by default
            m_configDir = home;
        }
        
        // create the path to the configuration dir, if necessary
        if (m_configDir.length() > 0)
        {
            try
            {
                // check if the dir exists
                File dir = new File(m_configDir);
                if (!dir.exists())
                {
                    // create the dir
                    boolean created = dir.mkdirs();
                    
                    // if the dir could not be created,
                    // set the config dir to the current working dir
                    if (!created) {
                        Log.logErrorRB("SU_CONFIG_DIR_CREATE_ERROR");
                        m_configDir = new File(".").getAbsolutePath();          // NOI18N
                    }
                }
            }
            catch (SecurityException e)
            {
                // the system doesn't want us to write where we want to write
                // reset the config dir to the current working dir
                m_configDir = new File(".").getAbsolutePath();                  // NOI18N
                
                // log the exception, but only after the config dir has been reset
                Log.logErrorRB("SU_CONFIG_DIR_CREATE_ERROR");
                Log.log(e.toString());
            }
        }
        
        // we should have a correct, existing config dir now
        return m_configDir;
    }
    
    /**
      * Returns true if running on Mac OS X
      */
    public static boolean onMacOSX() {
        // get os property
        String os;   // name of operating system
        try
        {
            // get the name of the operating system
            os = System.getProperty("os.name");                                 // NOI18N
        }
        catch (SecurityException e)
        {
            // access to the os property is restricted,
            // assume we're not on a Mac
            return false;
        }

        return os.equals("Mac OS X");

    }
    
    /**
     * Strips all XML tags (converts to plain text).
     */
    public static String stripTags(String xml)
    {
        return PatternConsts.OMEGAT_TAG.matcher(xml).replaceAll("");            // NOI18N
    }
    
    /**
     * Compares two strings for equality.
     * Handles nulls: if both strings are nulls they are considered equal.
     */
    public static boolean equal(String one, String two)
    {
        return (one==null && two==null) || (one!=null && one.equals(two));         
    }

    /**
     * Encodes the array of bytes to store them in a plain text file.
     */
    public static String uuencode(byte[] buf)
    {
        if (buf.length<=0)
            return new String();
        
        StringBuffer res = new StringBuffer();
        res.append(buf[0]);
        for (int i=1; i<buf.length; i++)
        {
            res.append('#');
            res.append(buf[i]);
        }
        return res.toString();
    }
    
    /**
     * Decodes the array of bytes that was stored in a plain text file
     * as a string, back to array of bytes.
     */
    public static byte[] uudecode(String buf)
    {
        String[] bytes = buf.split("#");                                        // NOI18N
        byte[] res = new byte[bytes.length];
        for (int i=0; i<bytes.length; i++)
        {
            try
            {
                res[i]=Byte.parseByte(bytes[i]);
            }
            catch (NumberFormatException e)
            {
                res[i]=0;
            }
        }
        return res;
    }

    /**
     * Makes the file name relative to the given path.
     */
    public static String makeFilenameRelative(String filename, String path)
    {
        if (filename.toLowerCase().startsWith(path.toLowerCase()))
            return filename.substring(path.length());
        else
            return filename;
    }

    /**
      * Escapes the passed string for use in regex matching,
      * so special regex characters are interpreted as normal
      * characters during regex searches.
      *
      * This is done by prepending a backslash before each
      * occurrence of the following characters: \^.*+[]{}()&|-:=?!<>
      *
      * @param text The text to escape
      *
      * @return The escaped text
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static String escapeNonRegex(String text) {
        return escapeNonRegex(text, true);
    }

    /**
      * Escapes the passed string for use in regex matching,
      * so special regex characters are interpreted as normal
      * characters during regex searches.
      *
      * This is done by prepending a backslash before each
      * occurrence of the following characters: \^.+[]{}()&|-:=!<>
      *
      * If the parameter escapeWildcards is true, asterisks (*) and
      * questions marks (?) will also be escaped. If false, these
      * will be converted to regex tokens (* -> 
      *
      * @param text            The text to escape
      * @param escapeWildcards If true, asterisks and question marks are also escaped.
      *                        If false, these are converted to there regex equivalents.
      *
      * @return The escaped text
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static String escapeNonRegex(String text, boolean escapeWildcards) {
        // handle backslash
        text = text.replaceAll("\\\\", "\\\\\\\\"); // yes, that's the correct nr of backslashes

        // handle rest of characters to be escaped
        //String escape = "^.*+[]{}()&|-:=?!<>";
        String escape = "^.+[]{}()&|-:=!<>";
        for (int i = 0; i < escape.length(); i++)
            text = text.replaceAll("\\" + escape.charAt(i), "\\\\" + escape.charAt(i));

        // handle "wildcard characters" ? and * (only if requested)
        // do this last, or the additional period (.) will cause trouble
        if (escapeWildcards) 
        {
            // simply escape * and ?
            text = text.replaceAll("\\?", "\\\\?");
            text = text.replaceAll("\\*", "\\\\*");
        }
        else 
        {
            // convert * (0 or more characters) and ? (0 or 1 character)
            // to their regex equivalents (\S* and \S? respectively)
//            text = text.replaceAll("\\?", "\\S?"); // do ? first, or * will be converted twice
//            text = text.replaceAll("\\*", "\\S*");
//          The above lines were not working:
//          [ 1680081 ] Search: simple wilcards do not work
//          The following correction was contributed by Tiago Saboga
            text = text.replaceAll("\\?", "\\\\S?"); // do ? first, or * will be converted twice
            text = text.replaceAll("\\*", "\\\\S*");
        }

        return text;
    }

    /**
      * Formats UI strings.
      *
      * Note: This is only a first attempt at putting right what goes
      *       wrong in MessageFormat. Currently it only duplicates
      *       single quotes, but it doesn't even test if the string
      *       contains parameters (numbers in curly braces), and it
      *       doesn't allow for string containg already escaped quotes.
      *
      * @param str       The string to format
      * @param arguments Arguments to use in formatting the string
      *
      * @return The formatted string
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static String format(String str, Object[] arguments) {
        // MessageFormat.format expects single quotes to be escaped
        // by duplicating them, otherwise the string will not be formatted
        str = str.replaceAll("'", "''");
        return MessageFormat.format(str, arguments);
    }

} // StaticUtils
