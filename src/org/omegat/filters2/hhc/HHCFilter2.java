/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
 Copyright (C) 2006 Didier Briel              
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

package org.omegat.filters2.hhc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.html2.HTMLFilter2;
import org.omegat.util.OStrings;

/**
 * A filter to translate HHC and HHK files.
 * <p>
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class HHCFilter2 extends HTMLFilter2 {
    
    @Override
    protected boolean requirePrevNextFields() {
        return true;
    }

    @Override
    public void processFile(BufferedReader infile, BufferedWriter outfile, FilterContext fc) throws IOException,
            TranslationException {
        StringBuffer all = null;
        try {
            all = new StringBuffer();
            char cbuf[] = new char[1000];
            int len = -1;
            while ((len = infile.read(cbuf)) > 0)
                all.append(cbuf, 0, len);
        } catch (OutOfMemoryError e) {
            // out of memory?
            all = null;
            System.gc();
            throw new IOException(OStrings.getString("HHC__FILE_TOO_BIG"));
        }

        Parser parser = new Parser();
        try {
            parser.setInputHTML(all.toString());
            parser.visitAllNodesWith(new HHCFilterVisitor(this, outfile));
        } catch (ParserException pe) {
            System.out.println(pe);
        }
    }

    // ////////////////////////////////////////////////////////////////////////

    @Override
    public String getFileFormatName() {
        return OStrings.getString("HHC__FILTER_NAME");
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.hhc"), new Instance("*.hhk") };
    }

    /**
     * Returns the editing hint for HHC filter.
     * <p>
     * In English, the hint is as follows: <br>
     * Note: Source File Encoding setting affects only the HHC and HHK files
     * that have no encoding declaration inside. If a HHC or HHK file has an
     * encoding declaration, it will be used disregarding any value you set in
     * this dialog.
     */
    @Override
    public String getHint() {
        return OStrings.getString("HHC_NOTE");
    }

    /**
     * Returns true to indicate that a filter has options.
     * 
     * @return False, because HHC filter has no options.
     */
    @Override
    public boolean hasOptions() {
        return false;
    }

}
