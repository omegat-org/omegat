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

package org.omegat.filters2.text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.util.OStrings;

/**
 * Filter to support plain text files (in various encodings).
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class TextFilter extends AbstractFilter
{
    
    public String getFileFormatName()
    {
        return OStrings.getString("TEXTFILTER_FILTER_NAME");
    }

    public Instance[] getDefaultInstances()
    {
        return new Instance[] 
            { 
                new Instance("*.txt"),                                          // NOI18N
                new Instance("*.txt1", "ISO-8859-1", "ISO-8859-1"),             // NOI18N
                new Instance("*.txt2", "ISO-8859-2", "ISO-8859-2"),             // NOI18N
                new Instance("*.utf8", "UTF-8", "UTF-8")                        // NOI18N
            };
    }

    public boolean isSourceEncodingVariable()
    {
        return true;
    }

    public boolean isTargetEncodingVariable()
    {
        return true;
    }
    
    public void processFile(BufferedReader in, BufferedWriter outfile)
            throws IOException
    {
		String s;
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch!=0xFEFF)
            in.reset();

        String nontrans = "";	                                                // NOI18N
		while( (s=in.readLine())!=null )
		{
			if( s.trim().length()==0 )
			{
				nontrans += s + "\n";	                                        // NOI18N
				continue;
			}
			String srcText = s;
			
            outfile.write(nontrans);
            nontrans = "";	                                                    // NOI18N

            String translation = processEntry(srcText);
            outfile.write(translation);
            
			nontrans += "\n";	                                                // NOI18N
		}
        
		if( nontrans.length()!=0 )
			outfile.write(nontrans);
    }

}
