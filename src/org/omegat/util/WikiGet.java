/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Kim Bruning
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

import java.net.URL;
import java.net.URLEncoder;
import java.io.*;
import java.util.regex.*;

/**
 * Import pages from MediaWiki
 *
 * @author Kim Bruning
 */
public class WikiGet 
{
    /** 
     * ~inverse of String.split() 
     * refactor note: In future releases, this might best be moved to a different file 
     */
    public static String joinString(String separator,String[] items) 
    {
        if (items.length < 1) 
            return "";                                                          // NOI18N
        StringBuffer joined = new StringBuffer();
        for (int i=0; i<items.length; i++)	
        {
            joined.append(items[i]);
            if (i != items.length-1)
                joined.append(separator);
        }
        return joined.toString();
    }

    /** 
     * Gets mediawiki wiki-code data from remote server.
     * The get strategy is determined by the url format.
     * @param remote_url string representation of well-formed URL of wikipage 
     * to be retrieved
     * @param projectdir string representation of path to the project-dir 
     * where the file should be saved.
     */
    public static void doWikiGet(String remote_url, String projectdir) 
    {
        try 
        {
            String joined = null; // contains edited url
            String name = null; // contains a useful page name which we can use 
                                // as our filename
            if (remote_url.indexOf("index.php?title=") > 0)                     // NOI18N
            {
                //We're directly calling the mediawiki index.php script
                String[] splitted = remote_url.split("index.php\\?title=");     // NOI18N
                String s = splitted[splitted.length-1];
                name = s;
                s = s.replaceAll(" ", "_");                                     // NOI18N
                //s=URLEncoder.encode(s, "UTF-8"); // breaks previously correctly encoded page names
                splitted[splitted.length-1] = s;
                joined = joinString("index.php?title=", splitted);              // NOI18N
                joined = joined + "&action=raw";                                // NOI18N
            } 
            else 
            {
                // assume script is behind  some sort 
                // of url-rewriting
                String[] splitted = remote_url.split("/");                      // NOI18N
                String s = splitted[splitted.length-1];
                name = s;
                s = s.replaceAll(" ", "_");                                     // NOI18N
                //s=URLEncoder.encode(s, "UTF-8"); 
                splitted[splitted.length-1] = s;
                joined = joinString("/", splitted);                             // NOI18N
                joined = joined + "?action=raw";                                // NOI18N
            }
            String page = getURL(joined);
            saveUTF8(projectdir, name + ".UTF8", page);                         // NOI18N
        } 
        catch (Exception e) 
        { 
            e.printStackTrace();
        }

    }

    /** 
     * Print UTF-8 text to stdout 
     * (useful for debugging) 
     * @param output  The UTF-8 format string to be printed.
     */
    public static void printUTF8(String output) 
    {
        try 
        {
            BufferedWriter out = UTF8WriterBuilder(System.out);
            out.write(output);

            out.flush();
          } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }

    /** 
     * Creates new BufferedWriter configured for UTF-8 output and connects it 
     * to an OutputStream
     * @param out  Outputstream to connect to.
     */
    public static BufferedWriter UTF8WriterBuilder(OutputStream out) throws Exception 
    {
        return new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));        // NOI18N
    }	


    /** 
     * Save UTF-8 format data to file.
     * @param dir	directory to write to.
     * @param filename filename of file to write.
     * @param output  UTF-8 format text to write
     */
    public static void saveUTF8(String dir, String filename, String output) 
    {
        try 
        {
            File path = new File(dir,filename);
            FileOutputStream f = new FileOutputStream(path);
            BufferedWriter out = UTF8WriterBuilder(f);
            out.write(output);
            out.close();
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }


    /** 
     * Obtain UTF-8 format text from remote URL.
     * @param target  String representation of well-formed URL.
     */
    public static String getURL(String target) 
    {
        StringBuffer page = new StringBuffer();
        try 
        {
            URL url = new URL(target);
            InputStream in = url.openStream();
            byte[] b = new byte[4096];	
            for (int n; (n = in.read(b)) != -1;) 
            {
                page.append(new String(b, 0, n, "UTF-8"));                      // NOI18N
            }
        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        } 
        return page.toString();
    }
}
