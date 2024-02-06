/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.filters2.subtitles;

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
import org.omegat.filters2.TranslationException;
import org.omegat.util.MixedEolHandlingReader;
import org.omegat.util.NullBufferedWriter;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Filter for subtitles files.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @see <a href="http://en.wikipedia.org/wiki/SubRip">Format description</a>
 */
public class SrtFilter extends AbstractFilter {
    protected static final Pattern PATTERN_TIME_INTERVAL = Pattern
            .compile("([0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3})\\s+-->\\s+([0-9]{2}:[0-9]{2}:[0-9]{2},[0-9]{3})");
    protected static final String EOL = "\r\n";

    enum READ_STATE {
        WAIT_TIME, WAIT_TEXT
    };

    protected Map<String, String> align;

    protected String key;
    protected StringBuilder text = new StringBuilder();
    protected BufferedWriter out;

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(SrtFilter.class);
    }

    public static void unloadPlugins() {
    }

    protected Pattern getPattern() {
        return PATTERN_TIME_INTERVAL;
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.srt") };
    }

    @Override
    public String getFileFormatName() {
        return OStrings.getString("SRTFILTER_FILTER_NAME");
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
    protected void processFile(BufferedReader inFile, BufferedWriter outFile, FilterContext fc) throws IOException,
            TranslationException {
        out = outFile;
        READ_STATE state = READ_STATE.WAIT_TIME;
        key = null;
        text.setLength(0);
        Pattern pattern = getPattern();

        try (MixedEolHandlingReader reader = new MixedEolHandlingReader(inFile)) {
            String s;
            while ((s = reader.readLine()) != null) {
                String trimmed = s.trim();
                switch (state) {
                case WAIT_TIME:
                    if (pattern.matcher(trimmed).matches()) {
                        state = READ_STATE.WAIT_TEXT;
                    }
                    key = trimmed;
                    text.setLength(0);
                    outFile.write(s);
                    outFile.write(EOL);
                    break;
                case WAIT_TEXT:
                    if (trimmed.isEmpty()) {
                        flush();
                        outFile.write(EOL);
                        state = READ_STATE.WAIT_TIME;
                    }
                    if (text.length() > 0) {
                        text.append('\n');
                    }
                    text.append(s);
                    break;
                }
            }
        }
        flush();
    }

    private void flush() throws IOException {
        if (text.length() == 0) {
            return;
        }

        if (align != null) {
            align.put(key, text.toString());
        }

        if (entryParseCallback != null) {
            entryParseCallback.addEntry(key, text.toString(), null, false, null, null, this, null);
        } else if (entryTranslateCallback != null) {
            String tr = entryTranslateCallback.getTranslation(key, text.toString(), null);
            if (tr == null) {
                tr = text.toString();
            }
            out.write(tr.replace("\n", EOL));
            out.write(EOL);
        }

        key = null;
        text.setLength(0);
    }

    @Override
    protected void alignFile(BufferedReader sourceFile, BufferedReader translatedFile, FilterContext fc) throws Exception {
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
}
