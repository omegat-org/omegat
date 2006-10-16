/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
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

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.BreakIterator;
import java.util.List;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Random;


/**
 * Static functions taken from
 * CommandThread to reduce file size.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
public class StaticUtils
{
	/**
	 * Name of the log file
	 */
	private final static String FILE_LOG = "log.txt";                           // NOI18N
	
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
        String tag = "";														// NOI18N
        char c;
        int state = 1;
        // extract tags from source string
        for (int j=0; j<str.length(); j++)
        {
            c = str.charAt(j);
            switch (state)
            {
                // 'normal' mode
                case 1:
                    if (c == '<')
                        state = 2;
                    break;
                    
                    // found < - see if double or single
                case 2:
                    if (c == '<')
                    {
                        // "<<" - let it slide
                        state = 1;
                    }
                    else
                    {
                        tag = "";												// NOI18N
                        tag += c;
                        state = 3;
                    }
                    break;
                    
                    // copy tag
                case 3:
                    if (c == '>')
                    {
                        // checking if the tag looks like OmegaT tag, 
                        // not 100% correct, but is the best what I can think of now
                        if(PatternConsts.OMEGAT_TAG_ONLY.matcher(tag).matches())
                            tagList.add(tag);
                        state = 1;
                        tag = "";												// NOI18N
                    }
                    else
                        tag += c;
                    break;
            }
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
        if (wordBreaker==null)
            wordBreaker = new WordIterator();
        return wordBreaker;
    }
    
    /**
     * Builds a list of tokens and a list of their offsets w/in a file.
     * <p>
     * It breaks string into tokens like in the following examples:
     * <ul>
     * <li> This is a semi-good way. -> "this", "is", "a", "semi-good", "way"
     * <li> Fine, thanks, and you? -> "fine", "thanks", "and", "you"
     * <li> C&all this action -> "call", "this", "action" ('&' is eaten)
     * </ul>
     * <p>
     * Also skips OmegaT tags.
     *
     * @param str string to tokenize
     * @param tokenList the list to add tokens to
     * @return number of tokens
     */
    public static int tokenizeText(String str, List tokenList)
    {
        int len = str.length();
        if (len==0)
            return 0;  // fixes bug nr. 1382810 (StringIndexOutOfBoundsException)
        
        if (tokenList!=null)
            str = str.toLowerCase();
        int nTokens = 0;
        
        BreakIterator breaker = getWordBreaker();
        breaker.setText(str);
        
        int start = breaker.first();
        for (int end = breaker.next(); end!=BreakIterator.DONE; 
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
            if (word && !PatternConsts.OMEGAT_TAG.matcher(tokenStr).matches())
            {
                nTokens++;
                if (tokenList!=null)
                {
                    Token token = new Token(tokenStr, start);
                    tokenList.add(token);
                }
            }
        }
        return nTokens;
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

private static SessionPrintStream log = null;
    /**
     * Returns a log stream.
     */
    public static PrintStream getLogStream() {
        if (log == null) {
            try {
                // create a new session print stream for the log file
                log = new SessionPrintStream( // encapsulated to output session ID
                    new PrintStream(
                        new FileOutputStream(getConfigDir() + FILE_LOG, true), 
                        true, "UTF-8")); // NOI18N
            }
            catch(Exception e) {
                // in case we cannot create a log file on dist,
                // redirect to system out
               log = new SessionPrintStream(System.out);
            }

            // get the session ID from the log session print stream
            String sessionID = log.getSessionID();

            // also encapsulate the system out and err in a session print stream
            // make sure they use the same session ID
            SessionPrintStream sessionOut = new SessionPrintStream(System.out);
            sessionOut.setSessionID(sessionID);
            System.setOut(sessionOut);
            //SessionPrintStream sessionErr = new SessionPrintStream(System.err);
            //sessionErr.setSessionID(sessionID);
            //System.setErr(sessionErr);
        }

        return log;
    }

    /**
     * Logs what otherwise would go to System.out
     */
    public static void log(String s)
    {
        try
        {
            PrintStream fout = getLogStream();
            fout.println(s);
            //fout.close();
            fout.flush(); // don't close it, otherwise the session ID is lost
        }
        catch( Exception e )
        {
            // doing nothing
        }

        System.out.println(s);
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
            log(e.toString());
            
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
                    if (!created)
                        m_configDir = new File(".").getAbsolutePath();          // NOI18N
                }
            }
            catch (SecurityException e)
            {
                // the system doesn't want us to write where we want to write
                // reset the config dir to the current working dir
                m_configDir = new File(".").getAbsolutePath();                  // NOI18N
                
                // log the exception, but only after the config dir has been reset
                log(e.toString());
            }
        }
        
        // we should have a correct, existing config dir now
        return m_configDir;
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
        // handle backslash
        text = text.replaceAll("\\\\", "\\\\\\\\"); // yes, that's the correct nr of backslashes

        // handle rest of characters to be escaped
        String escape = "^.*+[]{}()&|-:=?!<>";
        for (int i = 0; i < escape.length(); i++)
           text = text.replaceAll("\\" + escape.charAt(i), "\\\\" + escape.charAt(i));

        return text;
    }

    /**
      * Print stream that writes a session ID before each line of output
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    private static class SessionPrintStream extends PrintStream {

        /**
          * Print stream to write all output to.
          */
        //private PrintStream out;

        /**
          * Session ID
          */
        String sessionID;

        /**
          * Indicates whether the last character output was a newline.
          */
        private boolean lastIsNewline = false;

        /**
          * Constructs a new SessionPrintStream
          *
          * @param out The print stream to write all output to
          */
        public SessionPrintStream(PrintStream out) {
            super(out);

            // get a positive random number
            Random generator = new Random();
            generator.setSeed(System.currentTimeMillis()); // use current time as seed
            int random = Math.abs(generator.nextInt());

            // convert the number to string, 5 chars max, pad with zero's if necessary
            sessionID = String.valueOf(random);
            if (sessionID.length() > 5)
                sessionID = sessionID.substring(0, 5);
            else if (sessionID.length() < 5)
                for (int i = 5; i > sessionID.length(); i++)
                    sessionID = "0" + sessionID;
        }

        /**
          * Retrieves the session ID.
          *
          * @return The session ID of this SessionPrintStream
          */
        public String getSessionID() {
            return sessionID;
        }

        /**
          * Overrides the generated session ID.
          *
          * @param sessionID The session ID to use
          */
        public void setSessionID(String sessionID) {
            this.sessionID = sessionID;
        }

        /**
          * Writes the session ID to the output stream when at the start of a new line.
          */
        void printSessionID() {
            printSessionID(false);
        }

        /**
          * Writes the session ID to the output stream when at the start of a new line.
          *
          * @param forceWrite When true, the session ID is always writen,
          *                   even if not at the start of a new line.
          */
        void printSessionID(boolean forceWrite) {
            if (forceWrite || lastIsNewline)
                super.print(sessionID + ": ");
        }

        public void print(boolean b) {
            print(String.valueOf(b));
        }

        public void print(char c) {
            print(String.valueOf(c));
        }

        public void print(char[] s) {
            for (int i = 0; i < s.length; i++)
                print(s[i]);
        }

        public void print(double d) {
            print(String.valueOf(d));
        }

        public void print(float f) {
            print(String.valueOf(f));
        }

        public void print(int i) {
            print(String.valueOf(i));
        }

        public void print(long l) {
            print(String.valueOf(l));
        }

        public void print(Object o) {
            print(String.valueOf(o));
        }

        public void print(String s) {
            if (s == null)
                s = "null";
            byte[] bytes = s.getBytes();
            for (int i = 0; i < bytes.length; i++)
                write((int)bytes[i]);
        }

        public void println() {
            printSessionID();
            super.println();
            lastIsNewline = true;
        }

        public void println(boolean b) {
            print(b);
            println();
        }

        public void println(char c) {
            print(c);
            println();
        }

        public void println(char[] s) {
            print(s);
            println();
        }

        public void println(double d) {
            print(d);
            println();
        }

        public void println(float f) {
            print(f);
            println();
        }

        public void println(int i) {
            print(i);
            println();
        }

        public void println(long l) {
            print(l);
            println();
        }

        public void println(Object o) {
            print(o);
            println();
        }

        public void println(String s) {
            print(s);
            println();
        }

        /*
        public void write(byte[] bytes, int off, int len) {
            len = Math.min(off + len, bytes.length);
            for (int i = off; i < len; i++)
                write((int)bytes[i]);
        }
        */

        public void write(int b) {
            printSessionID();
            super.write(b);
            lastIsNewline = (((char)b) == '\n');
        }

    } // SessionPrintStream

} // StaticUtils
