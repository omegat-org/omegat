/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Arno Peters
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters2.pdf;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.util.OStrings;

/**
 * PDF input filter
 * @author Arno Peters
 */
public class PdfFilter  extends AbstractFilter {

    @Override
    public String getFileFormatName() {
	return OStrings.getString("PDFFILTER_FILTER_NAME");
    }

    @Override
    public Instance[] getDefaultInstances() {
	return new Instance[] {
	    new Instance("*.pdf", null, null, TFP_NAMEONLY+".txt")
	};
    }

    @Override
    public boolean isSourceEncodingVariable() {
	return true;
    }

    @Override
    public boolean isTargetEncodingVariable() {
	return true;
    }
	
    @Override
    public BufferedReader createReader(File infile, String encoding)
	throws IOException {
	PDFTextStripper stripper;
	stripper = new PDFTextStripper();
	stripper.setLineSeparator("\n");
	stripper.setSortByPosition(true);

	PDDocument document = PDDocument.load(infile.getAbsolutePath());
	String text = stripper.getText(document);
	document.close();

	return new BufferedReader(new ReaderFromString(text));
    }
    
    @Override
    public void processFile(BufferedReader in, BufferedWriter out, FilterContext fc) {
	StringBuffer sb = new StringBuffer();
	String find = ("^\\s*?$");
	Pattern p = Pattern.compile(find);
		
	String s = "";
	try {
	    while ( (s = in.readLine()) != null ) {
		Matcher m = p.matcher(s);
				
		if (m.find()) {
		    out.write(processEntry(sb.toString()));
		    sb.setLength(0);
		    out.write("\n\n");
		} else {
		    sb.append(s);
		    sb.append(" ");
		}
	    }
			
	    if (sb.length() > 0) {
		out.write(processEntry(sb.toString()));
		sb.setLength(0);
		out.write("\n");				
	    }
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }
}
