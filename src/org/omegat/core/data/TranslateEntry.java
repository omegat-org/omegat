/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
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

package org.omegat.core.data;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.segmentation.Rule;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.util.Language;
import org.omegat.util.StaticUtils;

/**
 * Base class for entry translation.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public abstract class TranslateEntry implements ITranslateCallback {

    private final ProjectProperties m_config;

    public TranslateEntry(final ProjectProperties m_config) {
        this.m_config = m_config;
    }

    /**
     * Get translation for specified entry to write output file.
     * 
     * @param entry
     *            entry ID
     * @param source
     *            source text
     */
    public String getTranslation(String id, String source) {
        // replacing all occurrences of single CR (\r) or CRLF (\r\n) by LF (\n)
        // this is reversed at the end of the method
        // fix for bug 1462566
        boolean crlf = source.indexOf("\r\n") > 0;
        if (crlf)
            source = source.replace("\\r\\n", "\n");
        boolean cr = source.indexOf("\r") > 0;
        if (cr)
            source = source.replace("\\r", "\n");

        // Some special space handling: skip leading and trailing whitespace
        // and non-breaking-space
        int len = source.length();
        int b = 0;
        StringBuffer bs = new StringBuffer();
        while (b < len
                && (Character.isWhitespace(source.charAt(b)) || source
                        .charAt(b) == '\u00A0')) {
            bs.append(source.charAt(b));
            b++;
        }

        int e = len - 1;
        StringBuffer es = new StringBuffer();
        while (e >= b
                && (Character.isWhitespace(source.charAt(e)) || source
                        .charAt(e) == '\u00A0')) {
            es.append(source.charAt(e));
            e--;
        }
        es.reverse();

        source = StaticUtils.fixChars(source.substring(b, e + 1));

        StringBuffer res = new StringBuffer();
        res.append(bs);

        if (m_config.isSentenceSegmentingEnabled()) {
            List<StringBuffer> spaces = new ArrayList<StringBuffer>();
            List<Rule> brules = new ArrayList<Rule>();
            Language sourceLang = m_config.getSourceLanguage();
            Language targetLang = m_config.getTargetLanguage();
            List<String> segments = Segmenter.segment(sourceLang, source,
                    spaces, brules);
            for (int i = 0; i < segments.size(); i++) {
                String onesrc = segments.get(i);
                segments.set(i, getSegmentTranslation(id, i, onesrc));
            }
            res.append(Segmenter.glue(sourceLang, targetLang, segments, spaces,
                    brules));
        } else
            res.append(getSegmentTranslation(id, 0, source));

        res.append(es);

        // replacing all occurrences of LF (\n) by either single CR (\r) or CRLF
        // (\r\n)
        // this is a reversal of the process at the beginning of this method
        // fix for bug 1462566
        String result = res.toString();
        if (crlf)
            result = result.replace("\\n", "\r\n");
        else if (cr)
            result = result.replace("\\n", "\r");

        return result;
    }

    protected abstract String getSegmentTranslation(String id,
            int segmentIndex, String segmentSource);
}
