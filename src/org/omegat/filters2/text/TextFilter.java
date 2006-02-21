/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

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
    
    public void processFile(BufferedReader in, BufferedWriter out)
            throws IOException
    {
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch!=0xFEFF)
            in.reset();
        
        TextOptions options = (TextOptions)getOptions();
        if (options==null)
            options = new TextOptions();
        
        switch (options.getSegmentOn())
        {
            case TextOptions.SEGMENT_BREAKS:
                processSegLineBreaks(in, out);
                break;
            case TextOptions.SEGMENT_EMPTYLINES:
                processSegEmptyLines(in, out);
                break;
            default:
                processNonSeg(in, out);
        }
    }
    
    /** Process the file without segmenting it. */
    private void processNonSeg(BufferedReader in, Writer out)
            throws IOException
    {
        StringBuffer segment = new StringBuffer();
        char[] buf = new char[4096];
        int len;
        while ((len=in.read(buf))>=0)
            segment.append(buf, 0, len);
        out.write(processEntry(segment.toString()));
    }
    
    /** Processes the file segmenting on line breaks. */
    private void processSegLineBreaks(BufferedReader in, Writer out)
            throws IOException
    {
        String nontrans = "";	                                                // NOI18N
		String s;
		while( (s=in.readLine())!=null )
		{
			if( s.trim().length()==0 )
			{
				nontrans += s + "\n";	                                        // NOI18N
				continue;
			}
			String srcText = s;
			
            out.write(nontrans);
            nontrans = "";	                                                    // NOI18N

            String translation = processEntry(srcText);
            out.write(translation);
            
			nontrans += "\n";	                                                // NOI18N
		}
        
		if( nontrans.length()!=0 )
			out.write(nontrans);
    }

    /** Processes the file segmenting on line breaks. */
    private void processSegEmptyLines(BufferedReader in, Writer out)
            throws IOException
    {
        StringBuffer nontrans = new StringBuffer();
        StringBuffer trans = new StringBuffer();
		String s;
		while( (s=in.readLine())!=null )
		{
            if (s.length()==0)
            {
                out.write(nontrans.toString());
                nontrans.setLength(0);

                out.write(processEntry(trans.toString()));
                trans.setLength(0);
                nontrans.append("\n");                                          // NOI18N
            }
            else
            {
                if( s.trim().length()==0 && trans.length()==0 )
                {
                    nontrans.append(s);
                    nontrans.append("\n");                                      // NOI18N
                }
                else
                {
                    trans.append(s);
                    trans.append("\n");                                         // NOI18N
                }
            }
		}
        
		if( nontrans.length()>=0 )
			out.write(nontrans.toString());
		if( trans.length()>=0 )
            out.write(processEntry(trans.toString()));
    }

    /**
     * Text filter shows a <b>modal</b> dialog to edit its own options.
     * 
     * @param currentOptions Current options to edit.
     * @return Updated filter options if user confirmed the changes, and current options otherwise.
     */
    public Serializable changeOptions(Dialog parent, Serializable currentOptions)
    {
        try
        {
            TextOptions options = (TextOptions) currentOptions;
            TextOptionsDialog dialog = new TextOptionsDialog(parent, options);
            dialog.setVisible(true);
            if( TextOptionsDialog.RET_OK==dialog.getReturnStatus() )
                return dialog.getOptions();
            else
                return currentOptions;
        }
        catch( Exception e )
        {
            StaticUtils.log("Text filter thrown an exception: " +              // NOI18N
                    e.getMessage());
            return currentOptions;
        }
    }

    /**
     * Returns true to indicate that Text filter has options.
     * @return True, because Text filter has options.
     */
    public boolean hasOptions()
    {
        return true;
    }

}
