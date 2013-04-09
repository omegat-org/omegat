/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.filters2.rc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.NullBufferedWriter;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Filter for support Windows resource files.
 * 
 * Format described on
 * http://msdn.microsoft.com/en-us/library/aa380599(VS.85).aspx
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class RcFilter extends AbstractFilter {

    protected static final Pattern RE_DIALOG = Pattern.compile("(\\S+)\\s+DIALOG(EX)?\\s+.+");
    protected static final Pattern RE_DIALOG_CAPTION = Pattern.compile("CAPTION\\s+.+");
    protected static final Pattern RE_MENU = Pattern.compile("(\\S+)\\s+MENU(EX)?\\s*.*");
    protected static final Pattern RE_MESSAGETABLE = Pattern.compile("(\\S+)\\s+MESSAGETABLE\\s*.*");
    protected static final Pattern RE_STRINGTABLE = Pattern.compile("STRINGTABLE\\s*.*");

    enum PART {
        DIALOG, MENU, MESSAGETABLE, STRINGTABLE, OTHER, UNKNOWN
    };

    protected String blockId;
    protected int b, e;

    protected Map<String, String> align;

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

    @Override
    protected void processFile(BufferedReader inFile, BufferedWriter outFile, FilterContext fc) throws IOException,
            TranslationException {
        PART cPart = PART.UNKNOWN;
        int cLevel = 0;

        blockId = null;
        String s;
        while ((s = inFile.readLine()) != null) {
            b = -1;
            e = -1;
            String id = null;
            String strim = s.trim();

            if (strim.startsWith("//") || strim.startsWith("#")) {
                outFile.write(s);
                outFile.newLine();
                continue;
            }

            if (strim.length() == 0) {
                if (cLevel == 0) {
                    cPart = PART.UNKNOWN;
                }
            } else if (cPart == PART.UNKNOWN) {
                cPart = parseFirstLineInBlock(strim);
            } else if ("{".equals(strim) || "BEGIN".equalsIgnoreCase(strim)) {
                cLevel++;
            } else if ("}".equals(strim) || "END".equalsIgnoreCase(strim)) {
                cLevel--;
                if (cLevel == 0) {
                    cPart = PART.UNKNOWN;
                }
            } else if (cLevel > 0 && cPart != PART.OTHER && cPart != PART.UNKNOWN) {
                markForTranslation(s);
                if (b >= 0 && e >= 0 && b < e && e > 0) {
                    id = parseId(cPart, s, b, e);
                }
            } else if (cLevel == 0 && cPart == PART.DIALOG) {
                if (RE_DIALOG_CAPTION.matcher(strim).matches()) {
                    markForTranslation(s);
                    id = "__CAPTION__";
                }
            }

            if (b >= 0 && e >= 0 && b < e && e > 0) {
                // extract source
                String loc = s.substring(b + 1, e);
                /*
                 * Some software produce escaped quotes, but valid are only
                 * double quotes
                 */
                loc = loc.replace("\\\"", "\"").replace("\"\"", "\"");
                if (entryParseCallback != null) {
                    entryParseCallback.addEntry(blockId + "/" + id, loc, null, false, null, null, this, null);
                } else if (entryTranslateCallback != null) {
                    // replace translation
                    String trans = entryTranslateCallback.getTranslation(blockId + "/" + id, loc, null);
                    if (trans == null) {
                        trans = loc;
                    }
                    trans = trans.replace("\"", "\"\"");
                    s = s.substring(0, b + 1) + trans + s.substring(e);
                } else if (entryAlignCallback != null && id != null) {
                    align.put(blockId + "/" + id, loc);
                }
            }
            outFile.write(s);
            outFile.newLine();
        }
    }

    @Override
    protected void alignFile(BufferedReader sourceFile, BufferedReader translatedFile, org.omegat.filters2.FilterContext fc) throws Exception {
        Map<String, String> source = new HashMap<String, String>();
        Map<String, String> translated = new HashMap<String, String>();

        align = source;
        processFile(sourceFile, new NullBufferedWriter(), fc);
        align = translated;
        processFile(translatedFile, new NullBufferedWriter(), fc);
        for (Map.Entry<String, String> en : source.entrySet()) {
            String tr = translated.get(en.getKey());
            if (!StringUtil.isEmpty(tr)) {
                entryAlignCallback.addTranslation(en.getKey(), en.getValue(), tr, false, null, this);
            }
        }
    }

    private PART parseFirstLineInBlock(String line) {
        Matcher m;
        if ((m = RE_DIALOG.matcher(line)).matches()) {
            blockId = m.group(1);
            return PART.DIALOG;
        }
        if ((m = RE_MENU.matcher(line)).matches()) {
            blockId = m.group(1);
            return PART.MENU;
        }
        if ((m = RE_MESSAGETABLE.matcher(line)).matches()) {
            blockId = m.group(1);
            return PART.MESSAGETABLE;
        }
        if (RE_STRINGTABLE.matcher(line).matches()) {
            blockId = "";
            return PART.STRINGTABLE;
        }
        return PART.OTHER;
    }

    private String parseId(PART cPart, String line, int b, int e) {
        String[] w;
        switch (cPart) {
        case DIALOG:
        case MENU:
            w = line.substring(e).split(",");
            return w.length > 1 ? w[1].trim() : null;
        case MESSAGETABLE:
        case STRINGTABLE:
            w = line.substring(0, b).split(",");
            return w[0].trim();
        }
        return null;
    }

    private void markForTranslation(String s) {
        b = s.indexOf('"');
        if (b < 0) {
            return;
        }
        e = b;
        while (true) {
            e = s.indexOf('"', e + 1);
            if (e < 0) {
                break;
            }
            if (s.charAt(e - 1) == '\\') {
                // skip escaped quote
                continue;
            }
            if (e < s.length() - 1) {
                if (s.charAt(e + 1) == '"') {
                    // skip double quote
                    e++;
                    continue;
                }
            }
            break;
        }
    }
}
