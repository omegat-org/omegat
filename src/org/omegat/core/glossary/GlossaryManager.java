/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/
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

package org.omegat.core.glossary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Class that loads glossary files and adds glossary entries 
 * to strings of the source files.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class GlossaryManager
{
    
    private final String EXT_DEF_ENC = ".tab";      // NOI18N
    private final String EXT_UTF8_ENC = ".utf8";    // NOI18N
    
    /**
     * Loads the glossary files
     * and builds the cash of glossary entries.
     * Supports
     * <ul>
     * <li>tab-separated files in default system encoding - with .tab extension
     * <li>tab-separated files in utf-8 encoding - with .utf8 extension
     * </ul>
     * Files with other extensions are ignored
     * 
     * @param folder - folder to look for the glossary files
     */
    public void loadGlossaryFiles(File folder)  throws IOException
    {
        if (folder.isDirectory())
        {
            List<GlossaryEntry> result=new ArrayList<GlossaryEntry>();
            for (String file : folder.list())
            {
                String fname = folder.getAbsolutePath() + File.separator + file;
                String fname_lower=fname.toLowerCase();
                // ignoring files with unrecognized extensions - http://sf.net/tracker/index.php?func=detail&aid=1088247&group_id=68187&atid=520347
                if( fname_lower.endsWith(EXT_DEF_ENC) || fname_lower.endsWith(EXT_UTF8_ENC) ) {
                    Log.logRB("CT_LOADING_GLOSSARY", new Object[] {fname});
                    loadGlossaryFile(new File(fname), result);
                }
            }
            glossaryEntries = Collections.unmodifiableList(result);
        }
        else
        {
            // uh oh - something is screwed up here
            throw new IOException(OStrings.getString("CT_ERROR_ACCESS_GLOSSARY_DIR"));
        }        
    }

    /**
     * Loads one glossary file.
     * Detects a file format and loads a file in appropriate encoding.
     */
    private void loadGlossaryFile(final File file, final List<GlossaryEntry> result) 
            throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
        String fname_lower = file.getName().toLowerCase();
        InputStreamReader reader = null;
        if( fname_lower.endsWith(EXT_DEF_ENC) )
        {
            reader = new InputStreamReader(new FileInputStream(file));
        }
        else if( fname_lower.endsWith(EXT_UTF8_ENC) )
        {
            InputStream fis = new FileInputStream(file);
            reader = new InputStreamReader(fis, "UTF-8");                   // NOI18N
        }

        BufferedReader in = new BufferedReader(reader);
        
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch!=0xFEFF)
            in.reset();
        
        for( String s = in.readLine(); s!=null; s = in.readLine() )
        {
            // skip lines that start with '#'
            if( s.startsWith("#") ) // NOI18N
                continue;
            
            // divide lines on tabs
            String tokens[] = s.split("\t");                                    // NOI18N
            // check token list to see if it has a valid string
            if( tokens.length<2 || tokens[0].length()==0 )
                continue;
            
            // creating glossary entry and add it to the hash 
            // (even if it's already there!)
            String comment = "";                                               // NOI18N
            if( tokens.length>=3 )
                comment=tokens[2];
            result.add(new GlossaryEntry(tokens[0], tokens[1], comment));
        }
        in.close();
    }

    /**
     * Get glossary entries.
     * @return all entries
     */
    public List<GlossaryEntry> getGlossaryEntries() {
        return glossaryEntries;
    }

    /**
     * Clear data about glossaries.
     */
    public void clear() {
        glossaryEntries = null;
    }

    private List<GlossaryEntry> glossaryEntries;    
}
