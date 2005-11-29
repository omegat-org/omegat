/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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
import java.util.List;
import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * Static functions taken from
 * CommandThread to reduce file size.
 *
 * @author Keith Godfrey
 */
public class StaticUtils
{
    
    /**
     * Builds a list of format tags within the supplied string.
     * Format tags are HTML style <xxx> tags.
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
    
    
    /**
     * Builds a list of tokens and a list of their offsets w/in a file.
     * It breaks string into tokens like in the following examples:
     * <ul>
     * <li> This is a semi-good way. -> "this", "is", "a", "semi-good", "way"
     * <li> Fine, thanks, and you? -> "fine", "thanks", "and", "you"
     * <li> C&all this action -> "call", "this", "action" ('&' is eaten)
     * </ul>
     *
     * @param str string to tokenize
     * @param tokenList the list to add tokens to
     * @return number of tokens
     */
    public static int tokenizeText(String str, List tokenList)
    {
        int len = str.length();
        if (len==0)
            return 0;
        
        str = str.toLowerCase();
        
        boolean word = false;
        boolean tagstart = false;
        StringBuffer tokenBuff = new StringBuffer(len);
        int tokenStart = 0;
        int nTokens = 0;
        char ch = str.charAt(0);
        for(int i=0; i<len; i++)
        {
            char pch = ch;
            ch = str.charAt(i);
            if( word )
            {
                if( Character.isLetterOrDigit(ch) || 
                        (ch=='&' && pch!='&') ) // "Bro&wse" is a single word
                                                // but "Foo&&Foo" is not
                {
                    // word continues
                    tokenBuff.append(ch);
                }
                else
                {
                    if( tokenBuff.length()<=1 )
                    {
                        // too short (one char) for a word
                        word = false;
                    }
                    else if( ch=='>' && tagstart && Character.isDigit(pch) )
                    {
                        // this is an OmegaT tag!
                        word = false;
                    }
                    else
                    {
                        // finally, it's really a word
                        nTokens++;
                        word = false;
                        if( tokenList!=null )
                        {
                            Token token = new Token(tokenBuff.toString(), tokenStart);
                            tokenList.add(token);
                        }
                    }
                }
            }
            else
            {
                if( Character.isLetterOrDigit(ch) )
                {
                    if( !CJKUtils.isCJK(ch) )
                    {
                        word = true;
                        tokenStart = i;
                        tokenBuff.setLength(0);
                        tokenBuff.append(ch);
                    }
                    else
                    {
                        nTokens++;
                        if( tokenList!=null )
                        {
                            Token token = new Token(Character.toString(ch), i);
                            tokenList.add(token);
                        }
                    }
                }
                else if( ch=='<' )
                {
                    tagstart = true;
                }
            }
        }
        
        if( word )
        {
            nTokens++;
            if( tokenList!=null )
            {
                Token token = new Token(tokenBuff.toString(), tokenStart);
                tokenList.add(token);
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
    
    /**
     * Returns a log stream.
     */
    public static PrintStream getLogStream()
    {
        try
        {
            return new PrintStream(new FileOutputStream("log.txt", true));      // NOI18N
        }
        catch( Exception e )
        {
            // in case we cannot create a log file on dist,
            // redirect to single out
            return System.out;
        }
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
            fout.close();
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
     * @param posInsideElement position inside a class path string, that fits inside some classpath element.
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
    
    
}
