/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.filters2.text;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;

/**
 * Filter to support plain text files (in various encodings).
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 */
public class TextFilter extends AbstractFilter {

    /**
     * Text filter should segmentOn text into paragraphs on line breaks.
     */
    public static final String SEGMENT_BREAKS = "BREAKS";
    /**
     * Defult. Text filter should segmentOn text into paragraphs on empty lines.
     */
    public static final String SEGMENT_EMPTYLINES = "EMPTYLINES";
    /**
     * Text filter should not segmentOn text into paragraphs.
     */
    public static final String SEGMENT_NEVER = "NEVER";

    public static final String OPTION_SEGMENT_ON = "segmentOn";

    public String getFileFormatName() {
        return OStrings.getString("TEXTFILTER_FILTER_NAME");
    }

    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.txt"), new Instance("*.txt1", OConsts.ISO88591, OConsts.ISO88591),
                new Instance("*.txt2", OConsts.ISO88592, OConsts.ISO88592), new Instance("*.utf8", OConsts.UTF8, OConsts.UTF8) };
    }

    public boolean isSourceEncodingVariable() {
        return true;
    }

    public boolean isTargetEncodingVariable() {
        return true;
    }
    
    @Override
    protected boolean requirePrevNextFields() {
        return true;
    }

    @Override
    public void processFile(BufferedReader in, BufferedWriter out, FilterContext fc) throws IOException {
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch != 0xFEFF)
            in.reset();

        String segmentOn = processOptions.get(TextFilter.OPTION_SEGMENT_ON);
        if (SEGMENT_BREAKS.equals(segmentOn)) {
            processSegLineBreaks(in, out);
        } else if (SEGMENT_NEVER.equals(segmentOn)) {
            processNonSeg(in, out);
        } else {
            processSegEmptyLines(in, out);
        }
    }

    /** Process the file without segmenting it. */
    private void processNonSeg(BufferedReader in, Writer out) throws IOException {
        StringBuffer segment = new StringBuffer();
        char[] buf = new char[4096];
        int len;
        while ((len = in.read(buf)) >= 0)
            segment.append(buf, 0, len);
        out.write(processEntry(segment.toString()));
    }

    /** Processes the file segmenting on line breaks. */
    private void processSegLineBreaks(BufferedReader in, Writer out) throws IOException {
        LinebreakPreservingReader lpin = new LinebreakPreservingReader(in);
        String nontrans = "";
        String s;
        while ((s = lpin.readLine()) != null) {
            if (s.trim().length() == 0) {
                nontrans += s + lpin.getLinebreak();
                continue;
            }
            String srcText = s;

            out.write(nontrans);
            nontrans = "";

            String translation = processEntry(srcText);
            out.write(translation);

            nontrans += lpin.getLinebreak();
        }
        lpin.close();

        if (nontrans.length() != 0)
            out.write(nontrans);
    }

    /** Processes the file segmenting on line breaks. */
    private void processSegEmptyLines(BufferedReader in, Writer out) throws IOException {
        LinebreakPreservingReader lpin = new LinebreakPreservingReader(in);
        StringBuffer nontrans = new StringBuffer();
        StringBuffer trans = new StringBuffer();
        String s;
        while ((s = lpin.readLine()) != null) {
            if (s.length() == 0) {
                out.write(nontrans.toString());
                nontrans.setLength(0);

                out.write(processEntry(trans.toString()));
                trans.setLength(0);
                nontrans.append(lpin.getLinebreak());
            } else {
                if (s.trim().length() == 0 && trans.length() == 0) {
                    nontrans.append(s);
                    nontrans.append(lpin.getLinebreak());
                } else {
                    trans.append(s);
                    trans.append(lpin.getLinebreak());
                }
            }
        }
        lpin.close();
        if (nontrans.length() >= 0)
            out.write(nontrans.toString());
        if (trans.length() >= 0)
            out.write(processEntry(trans.toString()));
    }

    @Override
    public Map<String, String> changeOptions(Dialog parent, Map<String, String> config) {
        try {
            TextOptionsDialog dialog = new TextOptionsDialog(parent, config);
            dialog.setVisible(true);
            if (TextOptionsDialog.RET_OK == dialog.getReturnStatus())
                return dialog.getOptions();
            else
                return null;
        } catch (Exception e) {
            Log.log("Text filter threw an exception:");
            Log.log(e);
            return null;
        }
    }

    /**
     * Returns true to indicate that Text filter has options.
     * 
     * @return True, because Text filter has options.
     */
    @Override
    public boolean hasOptions() {
        return true;
    }
}
