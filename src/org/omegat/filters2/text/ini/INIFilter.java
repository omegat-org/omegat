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

package org.omegat.filters2.text.ini;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.util.OStrings;
import org.omegat.util.LinebreakPreservingReader;

/**
 * Filter to support Files with Key=Value pairs,
 * which are sometimes used for i18n of software.
 *
 * @author Maxym Mykhalchuk
 */
public class INIFilter extends AbstractFilter
{
    
    public String getFileFormatName()
    {
        return OStrings.getString("INIFILTER_FILTER_NAME");
    }
    
    public boolean isSourceEncodingVariable()
    {
        return true;
    }
    
    public boolean isTargetEncodingVariable()
    {
        return true;
    }
    
    public Instance[] getDefaultInstances()
    {
        return new Instance[]
        {
            new Instance("*.ini"),                                              // NOI18N
            new Instance("*.lng"),                                              // NOI18N
        };
    }
    
    /**
     * Trims the string from left.
     */
    private String leftTrim(String s)
    {
        int i;
        for(i=0; i<s.length() && (s.charAt(i)==' ' || s.charAt(i)=='\t'); i++);
        return s.substring(i, s.length());
    }
    
    /**
     * Doing the processing of the file...
     */
    public void processFile(BufferedReader reader, BufferedWriter outfile)
    throws IOException
    {
        LinebreakPreservingReader lbpr = new LinebreakPreservingReader(reader); // fix for bug 1462566
        String str;
        //while( (str=reader.readLine())!=null )
        while( (str=lbpr.readLine())!=null )
        {
            String trimmed = str.trim();
            
            // skipping empty strings and comments
            if( trimmed.length()==0 || 
                    trimmed.charAt(0)=='#' || trimmed.charAt(0)==';' )
            {
                //outfile.write(str+"\n");                          // NOI18N
                outfile.write(str+lbpr.getLinebreak()); // fix for bug 1462566 // NOI18N
                continue;
            }
            
            // key=value pairs
            int equalsPos = str.indexOf('=');
            
            // if there's no separator, assume it's a key w/o a value
            if( equalsPos==-1 )
                equalsPos = str.length()-1;
            
            // advance if there're spaces after =
            while( (equalsPos+1)<str.length() && str.charAt(equalsPos+1)==' ' )
                equalsPos++;
            
            // writing out everything before = (and = itself)
            outfile.write(str.substring(0,equalsPos+1));
            
            String value=str.substring(equalsPos+1);
            
            value = leftTrim(value);
            String trans=processEntry(value);
            outfile.write(trans);
            
            //outfile.write("\n");                                                // NOI18N
            outfile.write(lbpr.getLinebreak()); // fix for bug 1462566            // NOI18N
        }
    }
}
