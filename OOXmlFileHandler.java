//-------------------------------------------------------------------------
//  
//  OOXmlFileHandler.java - 
//  
//  Copyright (C) 2002, Keith Godfrey
//  
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//  
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//  
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  
//  Build date:  16Apr2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.io.*;
import java.util.*;
import java.text.*;
import java.util.zip.*;

class OOXmlFileHandler extends XmlFileHandler
{
	public OOXmlFileHandler()
	{
		super("OpenOffice", "sxw");

		defineFormatTag("text:a", "a");
		defineFormatTag("text:span", "f");
		defineFormatTag("text:s", "s");
		defineFormatTag("text:s/", "s/");
		defineFormatTag("text:tab-stop", "t");
		defineFormatTag("text:tab-stop/", "t/");

		defineVerbatumTag("text:footnote", "foot");
	}


	public BufferedReader createInputStream(String filename) 
		throws IOException
	{
		File ifp = new File(filename);
		
		ZipInputStream zis = new ZipInputStream(new FileInputStream(ifp));
		InputStreamReader isr = new InputStreamReader(zis, "UTF8");
		BufferedReader br = new BufferedReader(isr);
		ZipEntry zit = null;
		while ((zit = zis.getNextEntry()) != null)
		{
			if (zit.getName().equals("content.xml"))
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
		OutputStreamWriter osw = new OutputStreamWriter(zos, "ISO-8859-1");
		BufferedWriter bw = new BufferedWriter(osw);
		ZipEntry zot = null;
		
		File ifp = new File(inFile);
		ZipInputStream zis = new ZipInputStream(new FileInputStream(ifp));
		InputStreamReader isr = new InputStreamReader(zis, "ISO-8859-1");
		BufferedReader br = new BufferedReader(isr);
		ZipEntry zit = null;
		
		while ((zit = zis.getNextEntry()) != null)
		{
			if (zit.getName().equals("content.xml"))
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
		zos.putNextEntry(new ZipEntry("content.xml"));
		bw.flush();
		
		osw = new OutputStreamWriter(zos, "UTF8");
		bw = new BufferedWriter(osw);

		return bw;
	}
}

