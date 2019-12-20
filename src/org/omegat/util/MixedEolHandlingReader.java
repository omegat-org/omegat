/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.stream.IntStream;

/**
 * This reader tries to detect the correct EOL type for the input stream based
 * on the frequency of EOL chars encountered within a lookahead range. Calling
 * {@link #readLine()} will return lines that include "bad" EOL chars.
 * <p>
 * For example an input that is detected to be CRLF that contains a line
 * "foo\r\r\n" will return "foo\r" for that line. This differs from
 * {@link BufferedReader} in that the latter will treat all EOL chars as
 * starting new lines, so the above example would give "foo" and then "".
 *
 * @author Aaron Madlon-Kay
 *
 */
public class MixedEolHandlingReader extends Reader {

    private final BufferedReader in;
    private String detectedEol;
    private boolean hasMixedEol;

    public MixedEolHandlingReader(Reader in) throws IOException {
        if (in instanceof BufferedReader) {
            this.in = (BufferedReader) in;
        } else {
            this.in = new BufferedReader(in);
        }
        init();
    }

    private void init() throws IOException {
        in.mark(OConsts.READ_AHEAD_LIMIT);
        char[] buf = new char[OConsts.READ_AHEAD_LIMIT];
        int read = in.read(buf);
        int[] counts = countEols(buf, read);
        hasMixedEol = IntStream.of(counts).filter(i -> i > 0).count() > 1;
        detectedEol = decideRepresentativeEol(counts[0], counts[1], counts[2]);
        in.reset();
    }

    static int[] countEols(char[] buf, int len) {
        int cr = 0;
        int lf = 0;
        int crlf = 0;
        for (int i = 0; i < len; i++) {
            char c = buf[i];
            if (c == '\r') {
                if (i < len - 1 && buf[i + 1] == '\n') {
                    crlf++;
                    i++;
                } else {
                    cr++;
                }
            } else if (c == '\n') {
                lf++;
            }
        }
        return new int[] { cr, lf, crlf };
    }

    static String decideRepresentativeEol(int cr, int lf, int crlf) {
        if (cr > lf && cr > crlf) {
            return "\r";
        }
        if (lf > cr && lf > crlf) {
            return "\n";
        }
        if (crlf > lf && crlf > cr) {
            return "\r\n";
        }
        if (crlf > 0) {
            return "\r\n";
        }
        return System.lineSeparator();
    }

    public String getDetectedEol() {
        return detectedEol;
    }

    public boolean hasMixedEol() {
        return hasMixedEol;
    }

    @Override
    public int read(char[] cbuf, int off, int len) throws IOException {
        return in.read(cbuf, off, len);
    }

    public String readLine() throws IOException {
        int c = in.read();
        if (c == -1) {
            return null;
        }

        StringBuilder line = new StringBuilder(1024);

        while (true) {
            line.append((char) c);
            if (encounteredEol(line)) {
                return line.substring(0, line.length() - detectedEol.length());
            }
            c = in.read();
            if (c == -1) {
                return line.toString();
            }
        }
    }

    private boolean encounteredEol(StringBuilder sb) {
        if (sb.length() < detectedEol.length()) {
            return false;
        }
        for (int i = 0; i < detectedEol.length(); i++) {
            if (sb.charAt(sb.length() - detectedEol.length() + i) != detectedEol.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

}
