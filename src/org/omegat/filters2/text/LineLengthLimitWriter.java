/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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

package org.omegat.filters2.text;

import java.io.IOException;
import java.io.Writer;

import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Token;

/**
 * Output filter for limit line length.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class LineLengthLimitWriter extends Writer {
    public static String PLATFORM_LINE_SEPARATOR = System.getProperty("line.separator");

    final Writer out;
    final int lineLength;
    final int maxLineLength;
    final ITokenizer tokenizer;
    final StringBuilder str = new StringBuilder();
    int breakChars;
    char eol1, eol2;

    public LineLengthLimitWriter(Writer out, int lineLength, int maxLineLength, ITokenizer tokenizer) {
        this.out = out;
        this.lineLength = lineLength;
        this.maxLineLength = maxLineLength;
        this.tokenizer = tokenizer;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        for (int cp, i = 0; i < len; i += Character.charCount(cp)) {
            cp = Character.codePointAt(cbuf, off + i);
            if (breakChars > 0 && cp == str.codePointBefore(str.length())) {
                // the same eol char - flush
                outLine();
            }
            if (cp == '\n' || cp == '\r') {
                str.appendCodePoint(cp);
                breakChars++;
                if (breakChars > 1) {
                    // 2 eol chars - flush
                    outLine();
                }
            } else {
                if (breakChars > 0) {
                    // was eol char - flush
                    outLine();
                }
                str.appendCodePoint(cp);
            }
        }
    }

    void outLine() throws IOException {
        if (str.length() == 0) {
            return;
        }
        int cp = str.codePointBefore(str.length());
        if (cp == '\n' || cp == '\r') {
            // get latest eol char
            eol2 = (char) cp;
            str.setLength(str.length() - 1);
        }
        if (str.length() > 0) {
            // get pre-latest eol char
            cp = str.codePointBefore(str.length());
            if (cp == '\n' || cp == '\r') {
                eol1 = (char) cp;
                str.setLength(str.length() - 1);
            }
        }
        if (str.length() == 0) {
            // was empty line
            writeSourceEol();
        } else {
            Token[] tokens = tokenizer.tokenizeAllExactly(str.toString());
            while (str.length() > 0) {
                int p = getBreakPos(tokens);
                breakAt(p, tokens);
            }
        }
        breakChars = 0;
        eol1 = 0;
        eol2 = 0;
    }

    int getBreakPos(Token[] tokens) {
        if (str.codePointCount(0, str.length()) <= maxLineLength) {
            // line no longer than max length - use full line
            return str.length();
        }
        // check if spaces only more than max length
        int latestNonSpacesTokenPos = 0;
        for (int i = tokens.length - 1; i >= 0; i--) {
            Token t = tokens[i];
            if (t == null) {
                // less than begin
                continue;
            }
            if (isSpaces(t)) {
                continue;
            }
            // non-spaces token
            latestNonSpacesTokenPos = t.getOffset() + t.getLength();
            break;
        }
        if (str.codePointCount(0, latestNonSpacesTokenPos) <= maxLineLength) {
            return str.length();
        }
        // try to break on the space ends
        int spacesStart = -1;
        for (int i = 0; i < tokens.length; i++) {
            Token t = tokens[i];
            if (t == null) {
                // less than begin
                continue;
            }
            if (str.codePointCount(0, t.getOffset()) >= lineLength) {
                // spaces can be after max length
                if (spacesStart >= 0 && str.codePointCount(0, spacesStart) < maxLineLength) {
                    return t.getOffset();
                }
            }
            if (isSpaces(t)) {
                if (spacesStart < 0) {
                    spacesStart = t.getOffset();
                }
            } else {
                spacesStart = -1;
            }
        }
        // try to break on the space boundaries
        for (int i = 0; i < tokens.length; i++) {
            Token t = tokens[i];
            if (t == null) {
                // less than begin
                continue;
            }
            int cps = str.codePointCount(0, t.getOffset());
            if (cps >= lineLength && cps < maxLineLength) {
                if (isSpaces(t)) {
                    return t.getOffset();
                }
            }
            cps = str.codePointCount(0, t.getOffset() + t.getLength());
            if (cps >= lineLength && cps < maxLineLength) {
                if (isSpaces(t)) {
                    return t.getOffset() + t.getLength();
                }
            }
        }
        // impossible to break on space boundaries - break at any token, except brackets
        for (int i = 0; i < tokens.length; i++) {
            Token t = tokens[i];
            if (t == null) {
                // less than begin
                continue;
            }
            int cps = str.codePointCount(0, t.getOffset());
            if (cps >= lineLength && cps < maxLineLength) {
                if (isPossibleBreakBefore(t.getOffset())) {
                    return t.getOffset();
                }
            }
            cps = str.codePointCount(0, t.getOffset() + t.getLength());
            if (cps >= lineLength && cps < maxLineLength) {
                if (isPossibleBreakBefore(t.getOffset() + t.getLength())) {
                    return t.getOffset() + t.getLength();
                }
            }
        }
        // use latest token before line length
        for (int i = 0; i < tokens.length; i++) {
            Token t = tokens[i];
            if (t == null) {
                // less than begin
                continue;
            }
            if (str.codePointCount(0, t.getOffset()) >= lineLength) {
                if (i == 0) {
                    return t.getOffset() + t.getLength();
                }
                int j = i - 1;
                while (j >= 0) {
                    Token tp = tokens[j--];
                    if (tp != null && tp.getOffset() > 0) {
                        if (isPossibleBreakBefore(tp.getOffset())) {
                            return tp.getOffset();
                        }
                    }
                }
                return t.getOffset();
            }
        }
        // use full line
        return str.length();
    }

    boolean isSpaces(Token token) {
        for (int cp, i = 0; i < token.getLength(); i += Character.charCount(cp)) {
            cp = str.codePointAt(token.getOffset() + i);
            if (!Character.isWhitespace(cp)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Write part of line to specified position, and change token offsets.
     */
    void breakAt(int pos, Token[] tokens) throws IOException {
        out.write(str.toString(), 0, pos);
        str.delete(0, pos);
        if (str.length() > 0) {
            writeBreakEol();
        } else {
            writeSourceEol();
        }
        for (int i = 0; i < tokens.length; i++) {
            Token t = tokens[i];
            if (t == null || t.getOffset() < pos) {
                tokens[i] = null;
            } else {
                tokens[i] = new Token(null, t.getOffset() - pos, t.getLength());
            }
        }
    }

    /**
     * Write EOL at the line break. Need to write force EOL, even there is no EOL chars in source file at all.
     */
    void writeBreakEol() throws IOException {
        if (eol1 == 0 && eol2 == 0) {
            // there is no known EOL, use platform-dependent
            out.write(PLATFORM_LINE_SEPARATOR);
        } else {
            if (eol1 != 0) {
                out.write(eol1);
            }
            if (eol2 != 0) {
                out.write(eol2);
            }
        }
    }

    /**
     * Write EOL in the source line's end. It can be file without EOL at the end.
     */
    void writeSourceEol() throws IOException {
        if (eol1 != 0) {
            out.write(eol1);
        }
        if (eol2 != 0) {
            out.write(eol2);
        }
    }

    boolean isPossibleBreakBefore(int pos) {
        try {
            // check previous char. Can't split after specified chars.
            int cp = str.codePointBefore(pos);
            if ("([{<«„".indexOf(cp) >= 0) {
                return false;
            }
        } catch (StringIndexOutOfBoundsException ex) {
        }
        try {
            // check next char. Can't split before specified chars.
            int cp = str.codePointAt(pos);
            if (")]}>»“,.".indexOf(cp) >= 0) {
                return false;
            }
        } catch (StringIndexOutOfBoundsException ex) {
        }
        return true;
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
        outLine();
        out.flush();
        out.close();
    }
}
