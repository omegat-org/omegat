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

package org.omegat.filters2.mozdtd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.NullBufferedWriter;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Filter for support Mozilla DTD files.
 * 
 * Format described on
 * http://msdn.microsoft.com/en-us/library/aa380599(VS.85).aspx
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MozillaDTDFilter extends AbstractFilter {
    protected static Pattern RE_ENTITY = Pattern
            .compile("<\\!ENTITY\\s+(\\S+)\\s+\"(.+)\"\\s*>");

    protected Map<String, String> align;

    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.dtd") };
    }

    public String getFileFormatName() {
        return OStrings.getString("MOZDTD_FILTER_NAME");
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
        return new BufferedReader(new InputStreamReader(new FileInputStream(
                inFile), OConsts.UTF8));
    }

    @Override
    protected BufferedWriter createWriter(File outFile, String outEncoding)
            throws UnsupportedEncodingException, IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                outFile), OConsts.UTF8));
    }

    @Override
    protected void processFile(BufferedReader inFile, BufferedWriter outFile)
            throws IOException, TranslationException {
        StringBuilder block = new StringBuilder();
        boolean isInBlock = false;
        int c;
        while ((c = inFile.read()) != -1) {
            if (c == '<' && !isInBlock) {
                isInBlock = true;
            }
            if (isInBlock) {
                block.append((char)c);
            } else {
                outFile.write(c);
            }
            if (c == '>' && isInBlock) {
                isInBlock = false;
                processBlock(block.toString(), outFile);
                block.setLength(0);
            }
        }
    }

    protected void processBlock(String block, BufferedWriter out)
            throws IOException {
        Matcher m = RE_ENTITY.matcher(block);
        if (!m.matches()) {
            // not ENTITY declaration
            out.write(block);
            return;
        }
        String id = m.group(1);
        String text = m.group(2);
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(id, text, null, false, null, this);
        } else if (entryTranslateCallback != null) {
            // replace translation
            String trans = entryTranslateCallback.getTranslation(null, text);
            out.write(block.substring(0, m.start(2)));
            out.write(trans);
            out.write(block.substring(m.end(2)));
        } else if (entryAlignCallback != null && id != null) {
            align.put(id, text);
        }
    }

    @Override
    protected void alignFile(BufferedReader sourceFile,
            BufferedReader translatedFile) throws Exception {
        Map<String, String> source = new HashMap<String, String>();
        Map<String, String> translated = new HashMap<String, String>();

        align = source;
        processFile(sourceFile, new NullBufferedWriter());
        align = translated;
        processFile(translatedFile, new NullBufferedWriter());
        for (Map.Entry<String, String> en : source.entrySet()) {
            String tr = translated.get(en.getKey());
            if (!StringUtil.isEmpty(tr)) {
                entryAlignCallback.addTranslation(en.getKey(), en.getValue(),
                        tr, false, null, this);
            }
        }
    }
}
