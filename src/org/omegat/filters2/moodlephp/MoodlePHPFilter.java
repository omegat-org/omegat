/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2018 Enrique Estevez Fernandez
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
     **************************************************************************/

package org.omegat.filters2.moodlephp;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.Log;
import org.omegat.util.NullBufferedWriter;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Filter for support Moodle PHP files.
 *
 * Code adapted from the file: MozillaDTDFilter.java
 *
 * @author Enrique Estevez (keko.gl@gmail.com)
 */
public class MoodlePHPFilter extends AbstractFilter {

    public static final String OPTION_REMOVE_STRINGS_UNTRANSLATED = "unremoveStringsUntranslated";

    protected static final Pattern RE_ENTITY = Pattern.compile("\\$string\\['(.+)'\\] (=) '(.+)(';)$",
            Pattern.DOTALL);

    protected Map<String, String> align;

    /**
     * If true, will remove non-translated segments in the target files
     */
    public boolean removeStringsUntranslated = false;

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.php") };
    }

    @Override
    public String getFileFormatName() {
        return OStrings.getString("MOODLEPHP_FILTER_NAME");
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
    protected BufferedReader createReader(File inFile, String inEncoding)
            throws UnsupportedEncodingException, IOException {
        return new BufferedReader(new InputStreamReader(new FileInputStream(inFile), StandardCharsets.UTF_8));
    }

    @Override
    protected BufferedWriter createWriter(File outFile, String outEncoding)
            throws UnsupportedEncodingException, IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8));
    }

    @Override
    protected void processFile(BufferedReader inFile, BufferedWriter outFile, FilterContext fc) throws IOException,
            TranslationException {

        String removeStringsUntranslatedStr = processOptions.get(OPTION_REMOVE_STRINGS_UNTRANSLATED);
        // If the value is null the default is false
        if ((removeStringsUntranslatedStr != null) && (removeStringsUntranslatedStr.equalsIgnoreCase("true"))) {
            removeStringsUntranslated = true;
        } else {
            removeStringsUntranslated = false;
        }

        StringBuilder block = new StringBuilder();
        boolean isInBlock = false;
        final char quotes = '\'';
        int previousChar = 0;
        int c;
        while ((c = inFile.read()) != -1) {
            if (c == '$' && !isInBlock) {
                isInBlock = true;
            }
            if (isInBlock) {
                block.append((char) c);
            } else {
                outFile.write(c);
            }

            if (c == ';' && isInBlock && previousChar == quotes) {
                isInBlock = false;
                processBlock(block.toString(), outFile);
                block.setLength(0);
            }
            if (c == quotes && previousChar == '\\') {
                previousChar = 0;
            } else {
                previousChar = c;
            }
        }
    }

    protected void processBlock(String block, BufferedWriter out) throws IOException {
        Matcher m = RE_ENTITY.matcher(block);
        if (!m.matches()) {
            out.write(block);
            return;
        }
        String id = m.group(1);
        String text = m.group(3);
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(id, text, null, false, null, null, this, null);
        } else if (entryTranslateCallback != null) {
            // replace translation
            String trans = entryTranslateCallback.getTranslation(id, text, null);
            if (trans != null || !removeStringsUntranslated) {
                out.write(block.substring(0, m.start(3)));
                out.write(trans != null ? trans : text);
                out.write(block.substring(m.end(3)));
            }
        } else if (entryAlignCallback != null && id != null) {
            align.put(id, text);
        }
    }

    @Override
    protected void alignFile(BufferedReader sourceFile, BufferedReader translatedFile, FilterContext fc)
            throws Exception {
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

    @Override
    public String getInEncodingLastParsedFile() {
        return StandardCharsets.UTF_8.name();
    }

    @Override
    public Map<String, String> changeOptions(Window parent, Map<String, String> config) {
        try {
            MoodlePHPOptionsDialog dialog = new MoodlePHPOptionsDialog(parent, config);
            dialog.setVisible(true);
            if (MoodlePHPOptionsDialog.RET_OK == dialog.getReturnStatus()) {
                return dialog.getOptions();
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.log(OStrings.getString("MOODLEPHP_FILTER_EXCEPTION"));
            Log.log(e);
            return null;
        }
    }

    /**
     * Returns true to indicate that Moodle PHP filter has options.
     *
     */
    @Override
    public boolean hasOptions() {
        return true;
    }

}
