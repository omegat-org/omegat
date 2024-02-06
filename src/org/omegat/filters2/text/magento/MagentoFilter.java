/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2011 Michael Zakharov
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters2.text.magento;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.NullBufferedWriter;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Filter to support Files for Magento CE locale. The files are a kind of CSV that looks like
 * "string in code","string to display in a locale"
 *
 * @author Michael Zakharov <trapman.hunt@gmail.com>
 */
public class MagentoFilter extends AbstractFilter {
    protected Map<String, String> align;

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(MagentoFilter.class);
    }

    public static void unloadPlugins() {
    }

    public String getFileFormatName() {
        return OStrings.getString("MAGENTOFILTER_FILTER_NAME");
    }

    public boolean isSourceEncodingVariable() {
        return true;
    }

    public boolean isTargetEncodingVariable() {
        return false;
    }

    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.csv", null, "UTF-8"), };
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
         * Magento CSV looks like "string in the code","translation to display"
         * The pattern below successfully handles cases like:
         * "Use "",""",""","" will be used"
         * The string will be displayed as: Use ","
         * or, after translation: "," will be used
         * The pattern splits it like
         * "Use "",""" (key for translation) and ""","" will be used" (value for translation)
         */
        Pattern splitter = Pattern.compile(",(?=(?:[^\"]*\"[^\"]*\")*(?![^\"]*\"))");

        while ((line = lbpr.readLine()) != null) {

            /**
             * Some lines in Magento locale CSV may look like:
             * "first, second
             * third","first, second, third"
             * It is unknown, if these lines are valid or not, so I inserted a quick workaround.
             */
            String contLine;
            // Continue reading until the line ends with ", or end of file
            while (!line.endsWith("\"") && (contLine = lbpr.readLine()) != null) {
                line += lbpr.getLinebreak() + contLine; // Preserve linebreaks
            }

            String trimmed = line.trim();

            // skipping empty strings
            if (trimmed.isEmpty()) {
                outfile.write(line + lbpr.getLinebreak());
                continue;
            }

            String[] result = splitter.split(trimmed);
            if (result.length < 2) { // Guard for malformed rows
                outfile.write(line + lbpr.getLinebreak());
                continue;
            }
            String key = result[0];
            String value = result[1];

            // Remove ""
            key = key.substring(1, key.length() - 1);
            value = value.substring(1, value.length() - 1);

            // writing out: "string in the code","
            outfile.write("\"" + key + "\",\"");

            String trans = process(key, value);

            outfile.write(trans + "\""); // Translation and closing "
            outfile.write(lbpr.getLinebreak());
        }
        lbpr.close();
    }

    @Override
    protected void alignFile(BufferedReader sourceFile, BufferedReader translatedFile,
            org.omegat.filters2.FilterContext fc) throws Exception {
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
