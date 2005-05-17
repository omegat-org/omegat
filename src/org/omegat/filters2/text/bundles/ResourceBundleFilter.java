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

package org.omegat.filters2.text.bundles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.util.OStrings;

/**
 * Filter to support Java Resource Bundles - the files that are used to I18ze
 * Java applications.
 *
 * @author Maxym Mykhalchuk
 * @author Keith Godfrey
 */
public class ResourceBundleFilter extends AbstractFilter
{
	
    public String getFileFormatName()
    {
        return OStrings.getString("RBFILTER_FILTER_NAME");
    }
    
    public boolean isSourceEncodingVariable()
    {
        return false;
    }

    public boolean isTargetEncodingVariable()
    {
        return false;
    }

    public Instance[] getDefaultInstances()
    {
        return new Instance[] 
            { 
                new Instance("*.properties", ENCODING_AUTO, ENCODING_AUTO,      // NOI18N
                        TFP_NAMEONLY+"_"+TFP_TARGETLOCALE+"."+TFP_EXTENSION)           // NOI18N
            };
    }
	
	/**
	 * Creating an input stream to read the source resource bundle.
     * <p>
	 * NOTE: resource bundles use always ISO-8859-1 encoding.
	 */
    public BufferedReader createReader(File infile, String encoding) 
            throws UnsupportedEncodingException, IOException
	{
		return new BufferedReader(
                new InputStreamReader(new FileInputStream(infile), "ISO-8859-1")); // NOI18N
	}
    
	/**
	 * Creating an output stream to save a localized resource bundle.
     * <p>
	 * NOTE: resource bundles use always ISO-8859-1 encoding.
     * <p>
	 * NOTE: the name of localized resource bundle is different from
	 *       the name of original one.
	 *       e.g. "Bundle.properties" -> Russian = "Bundle_ru.properties"
	 */
    public BufferedWriter createWriter(File outfile, String encoding) 
            throws UnsupportedEncodingException, IOException
    {
		// resource bundles use ASCII encoding
        return new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outfile), "ISO-8859-1")); // NOI18N
    }
    
	/**
	 * Converts ascii-encoded \\uxxxx to normal string
	 */
	protected String getNextLine(BufferedReader reader) throws IOException
	{
		String ascii = reader.readLine();
		if( ascii==null )
			return null;
		
		StringBuffer result = new StringBuffer();
	
		for(int i=0; i<ascii.length(); i++)
		{
			char ch = ascii.charAt(i);
			if( ch == '\\' && i!=ascii.length()-1 )
			{
				i++;
				ch = ascii.charAt(i);
				if( ch == 'u' )
				{
					// checking if the string is long enough
					if( ascii.length() >= i+1+4 )
					{
						ch=(char)Integer.parseInt(ascii.substring(i+1, i+1+4), 16);
						i+=4;
					}
					else
						throw new IOException(OStrings.getString("RBFH_ERROR_ILLEGAL_U_SEQUENCE"));
				} 
				else
				{
					if( ch=='n' )
						ch='\n';
					else if( ch=='r')
						ch='\r';
					else if( ch=='t')
						ch='\t';
				}
			}
			result.append(ch);
		}
		
		return result.toString();
	}
	
	/*
	 * Converts normal strings to ascii-encoded ones
	 */
	public String formatString(String text)
	{
		StringBuffer result = new StringBuffer();
		
		for(int i=0; i<text.length(); i++)
		{
			char ch = text.charAt(i);
			if( ch>=32 && ch<127 )
				result.append(ch);
			else if( ch=='\\' )
				result.append("\\\\");											// NOI18N
			else if( ch=='\n' )
				result.append("\\n");											// NOI18N
			else if( ch=='\r' )
				result.append("\\r");											// NOI18N
			else if( ch=='\t' )
				result.append("\\t");											// NOI18N
			else
			{
				String code = Integer.toString(ch, 16);
				while( code.length()<4 )
					code = '0'+code;
				result.append("\\u"+code);										// NOI18N
			}
		}
		
		return result.toString();
	}

	/**
	 * Trims the string from left.
	 */
	private String leftTrim(String s)
	{
		String trimmed = (s+".").trim();										// NOI18N
		return trimmed.substring(0, trimmed.length()-1);
	}
	
	/**
	 * Doing the processing of the file...
	 */
    public void processFile(BufferedReader reader, BufferedWriter outfile) 
            throws IOException
	{
		String str;
		boolean noi18n=false;
		while( (str=getNextLine(reader))!=null ) 
		{
			String trimmed = str.trim();
			
			// skipping empty strings
            if( trimmed.equals("") )										  // NOI18N
			{
    			outfile.write(formatString(str)+"\n");                       // NOI18N
				continue;
			}
			
			// skipping comments
			if( trimmed.charAt(0)=='#' )									   // NOI18N
			{
				outfile.write(formatString(str)+"\n");				          // NOI18N
				
				// checking if the next string shouldn't be internationalized
				if( trimmed.indexOf("NOI18N")>=0 )							   // NOI18N
					noi18n=true;
				
				continue;
			}

			// reading the glued lines
			while( str.charAt(str.length()-1)=='\\' )
			{
				String next = getNextLine(reader);
				if( next==null )
					next="";													// NOI18N
				
				// gluing this line (w/o '\' on this line) 
				//        with next line (w/o leading spaces)
				str=str.substring(0, str.length()-1)+leftTrim(next);
			}
				
			// key=value pairs
			int equalsPos = str.indexOf('=');									// NOI18N
			
			// if there's no separator, assume it's a key w/o a value
			if( equalsPos==-1 )
				equalsPos = str.length()-1;
            
            // advance if there're spaces after =
            while( (equalsPos+1)<str.length() && str.charAt(equalsPos+1)==' ' )
                equalsPos++;
			
			// writing out everything before = (and = itself)
			outfile.write(formatString(str.substring(0,equalsPos+1)));
			
			String value=str.substring(equalsPos+1);
			
			if( noi18n )
			{	
				// if we don't need to internationalize
				outfile.write(formatString(value));
				noi18n = false;
			}
			else
            {
				String trans=processEntry(value);
				outfile.write(formatString(trans));
            }
			
    		outfile.write("\n");											// NOI18N
		}
	}


	
}
