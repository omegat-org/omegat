/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2004  Keith Godfrey et al
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

package org.omegat.filters.xml.openoffice;

import org.omegat.filters.xml.XMLFileHandler;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Filter to natively handle OpenOffice XML file format.
 * This format is used by OO Writer, OO Spreadsheet etc
 *
 * @author Keith Godfrey
 */
public class OOFileHandler extends XMLFileHandler
{
	public OOFileHandler()
	{
		super("OpenOffice", "sxw");	 // NOI18N

		defineFormatTag("text:a", "a");	 // NOI18N
		defineFormatTag("text:span", "f");	 // NOI18N
		defineFormatTag("text:s", "s");	 // NOI18N
		defineFormatTag("text:s/", "s/");	 // NOI18N
		defineFormatTag("text:tab-stop", "t");	 // NOI18N
		defineFormatTag("text:tab-stop/", "t/");	 // NOI18N

		defineVerbatumTag("text:footnote", "foot");	 // NOI18N
	}


	public BufferedReader createInputStream(String filename) 
		throws IOException
	{
		File ifp = new File(filename);
		
		ZipInputStream zis = new ZipInputStream(new FileInputStream(ifp));
		InputStreamReader isr = new InputStreamReader(zis, "UTF8");	 // NOI18N
		BufferedReader br = new BufferedReader(isr);
		ZipEntry zit = null;
		while ((zit = zis.getNextEntry()) != null)
		{
			if (zit.getName().equals("content.xml"))	 // NOI18N
				break;
		}
		if (zit == null)
			return null;
		else
			return br;
	}

	// writing a zipfile with several components in it
	// first copy all unchanged components (i.e. everything but content.xml)
	//  then set the stream for the changed file to be written directly
	public BufferedWriter createOutputStream(String inFile, String outFile)
			throws IOException
	{
		int k_blockSize = 1024;
		int byteCount;
		char [] buf = new char[k_blockSize];

		File ofp = new File(outFile);
		ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(ofp));
		zos.setMethod(ZipOutputStream.DEFLATED);
		OutputStreamWriter osw = new OutputStreamWriter(zos, "ISO-8859-1");	 // NOI18N
		BufferedWriter bw = new BufferedWriter(osw);
		ZipEntry zot = null;
		
		File ifp = new File(inFile);
		ZipInputStream zis = new ZipInputStream(new FileInputStream(ifp));
		InputStreamReader isr = new InputStreamReader(zis, "ISO-8859-1");	 // NOI18N
		BufferedReader br = new BufferedReader(isr);
		ZipEntry zit = null;
		
		while ((zit = zis.getNextEntry()) != null)
		{
			if (zit.getName().equals("content.xml"))	 // NOI18N
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
		zos.putNextEntry(new ZipEntry("content.xml"));	 // NOI18N
		bw.flush();
		
		osw = new OutputStreamWriter(zos, "UTF8");	 // NOI18N
		bw = new BufferedWriter(osw);

		return bw;
	}
}

