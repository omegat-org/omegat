/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2014 Didier Briel
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

package org.omegat.filters2.text;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.omegat.core.Core;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Filter to support plain text files (in various encodings).
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class TextFilter extends AbstractFilter {
    public static final String ISO88592 = "ISO-8859-2";

    /**
     * Text filter should segmentOn text into paragraphs on line breaks.
     */
    public static final String SEGMENT_BREAKS = "BREAKS";
    /**
     * Default. Text filter should segmentOn text into paragraphs on empty lines.
     */
    public static final String SEGMENT_EMPTYLINES = "EMPTYLINES";
    /**
     * Text filter should not segmentOn text into paragraphs.
     */
    public static final String SEGMENT_NEVER = "NEVER";

    public static final String OPTION_SEGMENT_ON = "segmentOn";

    /**
     * Length at which a line break should occur in target documents
     */
    public static final String OPTION_LINE_LENGTH = "lineLength";

    /**
     * Maximum line length in target documents
     */
    public static final String OPTION_MAX_LINE_LENGTH = "maxLineLength";

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(TextFilter.class);
    }

    public static void unloadPlugins() {
    }

    @Override
    public String getFileFormatName() {
        return OStrings.getString("TEXTFILTER_FILTER_NAME");
    }

    @Override
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.txt"),
                new Instance("*.txt1", StandardCharsets.ISO_8859_1.name(), StandardCharsets.ISO_8859_1.name()),
                new Instance("*.txt2", ISO88592, ISO88592),
                new Instance("*.utf8", StandardCharsets.UTF_8.name(), StandardCharsets.UTF_8.name()) };
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
    protected boolean requirePrevNextFields() {
        return true;
    }

    @Override
    public void processFile(BufferedReader in, BufferedWriter out, FilterContext fc) throws IOException {
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch != 0xFEFF) {
            in.reset();
        }
        int lineLength, maxLineLength;
        try {
            lineLength = Integer.parseInt(processOptions.get(TextFilter.OPTION_LINE_LENGTH));
        } catch (Exception ex) {
            lineLength = 0;
        }
        try {
            maxLineLength = Integer.parseInt(processOptions.get(TextFilter.OPTION_MAX_LINE_LENGTH));
        } catch (Exception ex) {
            maxLineLength = 0;
        }
        Writer output;
        if (lineLength != 0 && maxLineLength != 0) {
            output = new LineLengthLimitWriter(out, lineLength, maxLineLength, fc.getTargetTokenizer());
        } else {
            output = out;
        }

        String segmentOn = processOptions.get(TextFilter.OPTION_SEGMENT_ON);
        if (SEGMENT_BREAKS.equals(segmentOn)) {
            processSegLineBreaks(in, output);
        } else if (SEGMENT_NEVER.equals(segmentOn)) {
            processNonSeg(in, output);
        } else {
            processSegEmptyLines(in, output);
        }
        output.close();
    }

    /** Process the file without segmenting it. */
    private void processNonSeg(BufferedReader in, Writer out) throws IOException {
        StringBuilder segment = new StringBuilder();
        char[] buf = new char[4096];
        int len;
        while ((len = in.read(buf)) >= 0) {
            segment.append(buf, 0, len);
        }
        out.write(processEntry(segment.toString()));
    }

    /** Processes the file segmenting on line breaks. */
    private void processSegLineBreaks(BufferedReader in, Writer out) throws IOException {
        LinebreakPreservingReader lpin = new LinebreakPreservingReader(in);
        String nontrans = "";
        String s;
        while ((s = lpin.readLine()) != null) {
            if (s.trim().isEmpty()) {
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

        if (!nontrans.isEmpty()) {
            out.write(nontrans);
        }
    }

    /** Processes the file segmenting on empty lines. */
    private void processSegEmptyLines(BufferedReader in, Writer out) throws IOException {
        LinebreakPreservingReader lpin = new LinebreakPreservingReader(in);
        StringBuilder nontrans = new StringBuilder();
        StringBuilder trans = new StringBuilder();
        String s;
        while ((s = lpin.readLine()) != null) {
            if (s.isEmpty()) {
                out.write(nontrans.toString());
                nontrans.setLength(0);

                out.write(processEntry(trans.toString()));
                trans.setLength(0);
                nontrans.append(lpin.getLinebreak());
            } else {
                if (s.trim().isEmpty() && trans.length() == 0) {
                    nontrans.append(s);
                    nontrans.append(lpin.getLinebreak());
                } else {
                    trans.append(s);
                    trans.append(lpin.getLinebreak());
                }
            }
        }
        lpin.close();
        if (nontrans.length() >= 0) {
            out.write(nontrans.toString());
        }
        if (trans.length() >= 0) {
            out.write(processEntry(trans.toString()));
        }
    }

    @Override
    public Map<String, String> changeOptions(Window parent, Map<String, String> config) {
        try {
            TextOptionsDialog dialog = new TextOptionsDialog(parent, config);
            dialog.setVisible(true);
            if (TextOptionsDialog.RET_OK == dialog.getReturnStatus()) {
                return dialog.getOptions();
            } else {
                return null;
            }
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
