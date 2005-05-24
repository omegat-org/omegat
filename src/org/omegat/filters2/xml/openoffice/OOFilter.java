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

package org.omegat.filters2.xml.openoffice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.omegat.filters2.Instance;

import org.omegat.filters2.xml.XMLAbstractFilter;
import org.omegat.util.OStrings;


/**
 * Filter to natively handle OpenOffice XML file format.
 * This format is used by OO Writer, OO Spreadsheet etc
 *
 * @author Keith Godfrey
 */
public class OOFilter extends XMLAbstractFilter
{
    // information about the filter
    public String getFileFormatName()
    {
        return OStrings.getString("OOFILTER_FILTER_NAME");
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
        return new Instance[] {
            new Instance("*.sx?", ENCODING_AUTO, ENCODING_AUTO),                // NOI18N
            new Instance("*.st?", ENCODING_AUTO, ENCODING_AUTO),                // NOI18N
            new Instance("*.od?", ENCODING_AUTO, ENCODING_AUTO),                // NOI18N
            new Instance("*.ot?", ENCODING_AUTO, ENCODING_AUTO),                // NOI18N
        };
    }
    
    // readers and writers
    
	/** holds the input file */
	private File infile;
    
    public BufferedReader createReader(File infile, String encoding) 
            throws UnsupportedEncodingException, IOException
    {
        this.infile = infile;        
		ZipInputStream zis = new ZipInputStream(new FileInputStream(infile));
		ZipEntry zipEntry;
		while( (zipEntry = zis.getNextEntry())!=null )
		{
			if( zipEntry.getName().equals("content.xml") )	                    // NOI18N
				break;
		}
		if( zipEntry==null )
            throw new IOException(OStrings.getString("OOFILTER_ERROR_ILLEGAL_FILE"));
		else
			return new BufferedReader(new InputStreamReader(zis, "UTF-8"));                         // NOI18N
    }
	/**
	 * Writing a zipfile with several components in it.
	 * First copy all unchanged components (i.e. everything but content.xml)
	 * then set the stream for the changed file to be written directly
	 */
    public BufferedWriter createWriter(File outfile, String encoding) throws UnsupportedEncodingException, IOException
    {
		int k_blockSize = 1024;
		int byteCount;
		char [] buf = new char[k_blockSize];

		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outfile));
		zos.setMethod(ZipOutputStream.DEFLATED);
		OutputStreamWriter osw = new OutputStreamWriter(zos, "ISO-8859-1");	    // NOI18N
		BufferedWriter bw = new BufferedWriter(osw);
		ZipEntry zot;
		
		ZipInputStream zis = new ZipInputStream(new FileInputStream(infile));
		InputStreamReader isr = new InputStreamReader(zis, "ISO-8859-1");	    // NOI18N
		BufferedReader br = new BufferedReader(isr);
		ZipEntry zit;
		
		while ((zit = zis.getNextEntry()) != null)
		{
			if (zit.getName().equals("content.xml"))	                        // NOI18N
			{
				// this is the meat of the file - don't copy this over
				// save its contents for the output data stream
				continue;
			}

			// copy this entry to the output file
			zot = new ZipEntry(zit.getName());
			zos.putNextEntry(zot);
			while ((byteCount = br.read(buf, 0, k_blockSize)) >= 0)
				bw.write(buf, 0, byteCount);
			bw.flush();
			zos.closeEntry();
		}
		zos.putNextEntry(new ZipEntry("content.xml"));	                        // NOI18N
		bw.flush();
		
		return new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"));        // NOI18N
    }

    /**
     * Creates a filter for OpenOffice suite files.
     */
	public OOFilter()
	{
		defineFormatTag("text:a", "a");	                                        // NOI18N
		defineFormatTag("text:span", "f");	                                    // NOI18N
		defineFormatTag("text:s", "s");	                                        // NOI18N
		defineFormatTag("text:s/", "s/");	                                    // NOI18N
		defineFormatTag("text:tab-stop", "t");	                                // NOI18N
		defineFormatTag("text:tab-stop/", "t/");	                            // NOI18N
        defineFormatTag("text:line-break", "br");                               // NOI18N
        defineFormatTag("text:line-break/", "br/");                             // NOI18N
        
        // not working in 1.4.5
		// defineVerbatumTag("text:note-citation");	                            // NOI18N
	}


}

