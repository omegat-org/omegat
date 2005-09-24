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

package org.omegat.filters2.html2;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.htmlparser.Parser;
import org.htmlparser.scanners.ScriptScanner;
import org.htmlparser.util.ParserException;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.AntiCRReader;
import org.omegat.util.EncodingAwareReader;
import org.omegat.util.OStrings;
import org.omegat.util.UTF8Writer;

/**
 * A filter to translate HTML and XHTML files.
 *
 * @author Maxym Mykhalchuk
 */
public class HTMLFilter2 extends AbstractFilter
{
    /** Creates a new instance of HTMLFilter2 */
    public HTMLFilter2()
    {
    }

    /**
     * Customized version of creating input reader for HTML files,
     * aware of encoding by using <code>EncodingAwareReader</code> class.
     *
     * @see org.omegat.util.EncodingAwareReader
     */
    public BufferedReader createReader(File infile, String encoding) 
            throws UnsupportedEncodingException, IOException
    {
        return new BufferedReader(new AntiCRReader(
                new EncodingAwareReader(infile.getAbsolutePath(), EncodingAwareReader.ST_HTML)));
    }
    /**
     * Customized version of creating an output stream for HTML files,
     * always UTF-8 and appending charset meta with UTF-8
     * by using <code>UTF8Writer</code> class.
     *
     * @see org.omegat.util.UTF8Writer
     */
    public BufferedWriter createWriter(File outfile, String encoding) 
            throws UnsupportedEncodingException, IOException
    {
        return new BufferedWriter(
                new UTF8Writer(outfile.getAbsolutePath(), UTF8Writer.ST_HTML));
    }

    public void processFile(BufferedReader infile, BufferedWriter outfile) 
            throws IOException, TranslationException
    {
        StringBuffer all = null;
        try
        {
            all = new StringBuffer();
            char cbuf[] = new char[1000];
            int len = -1;
            while( (len=infile.read(cbuf))>0 )
                all.append(cbuf, 0, len);                
        }
        catch( OutOfMemoryError e )
        {
            // out of memory?
            all = null;
            System.gc();
            throw new IOException(OStrings.getString("HTML__FILE_TOO_BIG"));
        }
        
        Parser parser = new Parser();
        try
        {
            // magic to workaround HTMLParser bug:
            // http://sourceforge.net/tracker/index.php?func=detail&aid=1227213&group_id=24399&atid=381399
            ScriptScanner.STRICT = true;
            
            parser.setInputHTML(all.toString());
            parser.visitAllNodesWith(new FilterVisitor(this, outfile));
        }
        catch( ParserException pe )
        {
            System.out.println(pe);
        }
    }
    
    //////////////////////////////////////////////////////////////////////////
    
    /** Package-internal processEntry to give it to FilterVisitor */
    String privateProcessEntry(String entry)
    {
        return super.processEntry(entry);
    }
    
    //////////////////////////////////////////////////////////////////////////

    public boolean isTargetEncodingVariable()
    {
        return false;
    }

    public boolean isSourceEncodingVariable()
    {
        return true;
    }

    public String getFileFormatName()
    {
        return OStrings.getString("HTML__FILTER_NAME");
    }

    public Instance[] getDefaultInstances()
    {
        return new Instance[]
        {
            new Instance("*.htm", ENCODING_AUTO, "UTF-8"),                      // NOI18N
            new Instance("*.html", ENCODING_AUTO, "UTF-8"),                     // NOI18N
            new Instance("*.xhtml", ENCODING_AUTO, "UTF-8")                     // NOI18N
        };
    }
    
}
