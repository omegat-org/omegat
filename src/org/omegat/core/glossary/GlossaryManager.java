/**************************************************************************
 * OmegaT - Java based Computer Assisted Translation (CAT) tool
 * Copyright (C) 2002-2004  Keith Godfrey et al
 * keithgodfrey@users.sourceforge.net
 * 907.223.2039
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.core.glossary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.omegat.core.StringEntry;
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
    
    /** Creates a new instance of GlossaryLoader */
    public GlossaryManager()
    {
        glossaryEntries = new ArrayList();
    }
    
    /**
     * Loads the glossary files
     * and builds the cash of glossary entries.
     * Supports
     * <ul>
     * <li>tab-separated files in default system encoding - with .tab extension
     * <li>tab-separated files in utf-8 encoding - with .utf8 extension
     * </ul>
     * 
     * @param folder - folder to look for the glossary files
     */
    public void loadGlossaryFiles(File folder)  throws IOException
    {
		if (folder.isDirectory())
		{
			String fileList[] = folder.list();
			for (int i=0; i<fileList.length; i++)
			{
				String fname = folder.getAbsolutePath() + File.separator + fileList[i];
				System.out.println(OStrings.getString("CT_LOADING_GLOSSARY") + fname);
				loadGlossaryFile(new File(fname));
			}
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
    private void loadGlossaryFile(File file) 
            throws FileNotFoundException, UnsupportedEncodingException, IOException
    {
        String fname = file.getName();
        Reader reader;
        if( fname.endsWith(".tab") )                                         // NOI18N
        {
            reader = new FileReader(file);
        }
        else if( fname.endsWith(".utf8") )                                  // NOI18N
        {
            InputStream fis = new FileInputStream(file);
            reader = new InputStreamReader(fis, "UTF-8");                   // NOI18N
        }
        else
        {
            System.out.println(OStrings.CT_DONT_RECOGNIZE_GLOS_FILE + file.getName());
            throw new IOException(OStrings.CT_DONT_RECOGNIZE_GLOS_FILE + file.getName());
        }
		BufferedReader in = new BufferedReader(reader);
        for( String s = in.readLine(); s!=null; s = in.readLine() )
		{
			// skip lines that start with '#'
			if( s.startsWith("#") ) // NOI18N
				continue;
            
            // divide lines on tabs
            String tokens[] = s.split("\t");
			// check token list to see if it has a valid string
            if( tokens.length<2 || tokens[0].length()==0 )
                continue;
            
            // creating glossary entry and add it to the hash 
            // (even if it's already there!)
            String comment = "";                                               // NOI18N
            if( tokens.length>=3 )
                comment=tokens[2];
            GlossaryEntry glosEntry = new GlossaryEntry(tokens[0], tokens[1], comment);
            glossaryEntries.add(glosEntry);
		}
		in.close();
	}

    /**
     * Builds the Glossary.
     * This process looks up the source string entries, 
     * and adds the glossary entries there.
     */
    public void buildGlossary(List strEntryList)
	{
		for(int i=0; i<glossaryEntries.size(); i++)
		{
			GlossaryEntry glosEntry = (GlossaryEntry)glossaryEntries.get(i);
            String glosStrLow = glosEntry.getSrcText().toLowerCase();
            
            for(int j=0; j<strEntryList.size(); j++)
            {
                StringEntry strEntry = (StringEntry)strEntryList.get(j);
                String strStrLow = strEntry.getSrcTextLow();
                if( strStrLow.indexOf(glosEntry.getSrcText())>=0 )
                    strEntry.addGlossaryEntry(glosEntry);
            }
		}
	}

	private List glossaryEntries;
    
}
