/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.filters2.subtitles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.NullBufferedWriter;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Filter for subtitles files. Format described on
 * http://en.wikipedia.org/wiki/SubRip.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
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
    protected void processFile(BufferedReader inFile, BufferedWriter outFile) throws IOException,
            TranslationException {
        out = outFile;
        READ_STATE state = READ_STATE.WAIT_TIME;
        key = null;
        text.setLength(0);

        String s;
        while ((s = inFile.readLine()) != null) {
            switch (state) {
            case WAIT_TIME:
                if (PATTERN_TIME_INTERVAL.matcher(s).matches()) {
                    state = READ_STATE.WAIT_TEXT;
                }
                key = s;
                text.setLength(0);
                outFile.write(s);
                outFile.write(EOL);
                break;
            case WAIT_TEXT:
                if (s.trim().length() == 0) {
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
            entryParseCallback.addEntry(key, text.toString(), null, false, null, null, this);
        } else {
            String tr = entryTranslateCallback.getTranslation(key, text.toString(), null);
            out.write(tr.replace("\n", EOL));
            out.write(EOL);
        }
        
        key = null;
        text.setLength(0);
    }

    @Override
    protected void alignFile(BufferedReader sourceFile, BufferedReader translatedFile) throws Exception {
        Map<String, String> source = new HashMap<String, String>();
        Map<String, String> translated = new HashMap<String, String>();

        align = source;
        processFile(sourceFile, new NullBufferedWriter());
        align = translated;
        processFile(translatedFile, new NullBufferedWriter());
        for (Map.Entry<String, String> en : source.entrySet()) {
            String tr = translated.get(en.getKey());
            if (!StringUtil.isEmpty(tr)) {
                entryAlignCallback.addTranslation(en.getKey(), en.getValue(), tr, false, null, this);
            }
        }
    }
}
