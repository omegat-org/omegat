/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.filters2.rc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.OStrings;

/**
 * Filter for support Windows resource files.
 * 
 * Format described on
 * http://msdn.microsoft.com/en-us/library/aa380599(VS.85).aspx
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class RcFilter extends AbstractFilter {

    protected static final Pattern RE_DIALOG = Pattern
            .compile("\\S+\\s+DIALOG(EX)?\\s+.+");
    protected static final Pattern RE_MENU = Pattern
            .compile("\\S+\\s+MENU(EX)?\\s*");
    protected static final Pattern RE_MESSAGETABLE = Pattern
            .compile("\\S+\\s+MESSAGETABLE\\s*");
    protected static final Pattern RE_STRINGTABLE = Pattern
            .compile("STRINGTABLE\\s*");

    enum PART {
        DIALOG, MENU, MESSAGETABLE, STRINGTABLE, OTHER, UNKNOWN
    };

    public String getFileFormatName() {
        return OStrings.getString("RCFILTER_FILTER_NAME");
    }

    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.rc") };
    }

    public boolean isSourceEncodingVariable() {
        return true;
    }

    public boolean isTargetEncodingVariable() {
        return true;
    }

    protected void processFile(BufferedReader inFile, BufferedWriter outFile)
            throws IOException, TranslationException {
        PART cPart = PART.UNKNOWN;
        int cLevel = 0;

        String s;
        while ((s = inFile.readLine()) != null) {
            String strim = s.trim();
            if (strim.length() == 0) {
                if (cLevel == 0) {
                    cPart = PART.UNKNOWN;
                }
            } else if (cPart == PART.UNKNOWN) {
                cPart = parseFirstLineInBlock(strim);
            } else if ("{".equals(strim)) {
                cLevel++;
            } else if ("}".equals(strim)) {
                cLevel--;
                if (cLevel == 0) {
                    cPart = PART.UNKNOWN;
                }
            } else if (cLevel > 0 && cPart != PART.OTHER
                    && cPart != PART.UNKNOWN) {
                String loc;
                int b = s.indexOf('"');
                int e = s.lastIndexOf('"');
                if (b < e && e > 0) {
                    // extract source
                    loc = s.substring(b + 1, e);
                    if (entryParseCallback != null) {
                        entryParseCallback.addEntry(null, loc, null, false,
                                null, this);
                    } else if (entryTranslateCallback != null) {
                        // replace translation
                        String trans = entryTranslateCallback.getTranslation(
                                null, loc);
                        s = s.substring(0, b + 1) + trans + s.substring(e);
                    }
                }
            }
            outFile.write(s);
            outFile.newLine();
        }
    }

    private PART parseFirstLineInBlock(String line) {
        if (RE_DIALOG.matcher(line).matches()) {
            return PART.DIALOG;
        }
        if (RE_MENU.matcher(line).matches()) {
            return PART.MENU;
        }
        if (RE_MESSAGETABLE.matcher(line).matches()) {
            return PART.MESSAGETABLE;
        }
        if (RE_STRINGTABLE.matcher(line).matches()) {
            return PART.STRINGTABLE;
        }
        return PART.OTHER;
    }
}
