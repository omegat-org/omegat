/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2011-2014 Michael Zakharov
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

package org.omegat.filters2.text.ilias;

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
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.NullBufferedWriter;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Filter to support language files for ILIAS. The files are a kind of UTF8 encoded text where the lines look like
 * module_name#:#identifier#:#string to translate
 * where neither module_name, nor identifier can be modified but must be copied into the translated version as they are including #:# separators.
 * The file contains a header that should be copied into the translated version.
 * The translated stings should not contain any \n\r symbols but may include simple HTML entities such as <p> ... </p> and <br />
 * @see http://www.ilias.de/docu/ilias.php?ref_id=37&from_page=129&obj_id=133&obj_type=PageObject&cmd=layout&cmdClass=illmpresentationgui&cmdNode=ih&baseClass=ilLMPresentationGUI 
 * 
 * @author Michael Zakharov <trapman.hunt@gmail.com>
 */
public class ILIASFilter extends AbstractFilter {
    protected Map<String, String> align;

    private final Pattern patternMark = Pattern.compile("<!-- language file start -->");
    private final Pattern patternText = Pattern.compile("^(\\S+)#:#(\\S+)#:#(.+)$");

    @Override
    public String getFileFormatName() {
        return OStrings.getString("ILIASFILTER_FILTER_NAME");
    }

    @Override
    public boolean isSourceEncodingVariable() {
        return false;
    }

    @Override
    public boolean isTargetEncodingVariable() {
        return false;
    }

    @Override
    public Instance[] getDefaultInstances() {
        String targetFile = "ilias_" + AbstractFilter.TFP_TARGET_LANG_CODE + "." + TFP_EXTENSION;
        return new Instance[] { new Instance
                ("*.lang", null, "UTF-8", targetFile), new Instance("*.lang.local", null, "UTF-8", targetFile),};
    }

    /**
     * Doing the processing of the file...
     * @param reader
     * @param outfile  
     */
    @Override
    public void processFile(BufferedReader reader, BufferedWriter outfile, FilterContext fc) throws IOException {
        LinebreakPreservingReader lbpr = new LinebreakPreservingReader(reader); // fix
                                                                                // for
                                                                                // bug
                                                                                // 1462566
        String line;
        /*
         * ILIAS strings look like module_name#:#identifier#:#string to translate
         * The file usually begins from some text that does not match the pattern
         */

        while ((line = lbpr.readLine()) != null) {
                      
            String trimmed = line.trim();

            // skipping empty strings
            if (trimmed.length() == 0) {
                outfile.write(line + lbpr.getLinebreak());
                continue;
            }

            Matcher mat = patternText.matcher(line);
            if (!mat.matches()) {
                outfile.write(line + lbpr.getLinebreak());
                continue;
            }
            String key = mat.group(1) + "#:#" + mat.group(2);
            String value = mat.group(3);

            if(value.isEmpty()) { // If original text is empty, the translated is empty too
                outfile.write(line + lbpr.getLinebreak()); 
                continue;                
            }

            // writing out: "module_name#:#identifier#:#"
            outfile.write(key + "#:#");

            String trans = process(key, value);
                        
            outfile.write(trans); // Translation
            outfile.write(lbpr.getLinebreak()); 
        }
        lbpr.close();
    }

    @Override
    protected boolean isFileSupported(BufferedReader reader) {
        boolean markFound = false;
        boolean textFound = false;
        final int MAX_LINES_TO_CHECK = 128;

        try {
            String line;
            int more = MAX_LINES_TO_CHECK + 1;
            LinebreakPreservingReader lbpr = new LinebreakPreservingReader(reader);
            while ((line = lbpr.readLine()) != null && --more > 0) {
                line = line.trim();
                if (line.length() == 0) {
                    continue;
                }
                markFound = patternMark.matcher(line).matches();
                if (markFound) {
                    break;
                }
                textFound = patternText.matcher(line).matches();
            }
        } catch (IOException e) {
            return false;
        }
        return markFound & !textFound;
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
    
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    private String process(String key, String value) {
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(key, value, null, false, null, this);
            return value;
        } else if (entryTranslateCallback != null) {
            String trans = entryTranslateCallback.getTranslation(key, value);
            return trans != null ? trans : value;
        } else if (entryAlignCallback != null) {
            align.put(key, value);
        }
        return value;
    }

}
