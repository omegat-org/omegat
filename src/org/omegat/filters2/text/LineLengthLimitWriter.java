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
        for (int i = 0; i < len; i++) {
            char ch = cbuf[off + i];
            if (breakChars > 0 && ch == str.charAt(str.length() - 1)) {
                // the same eol char - flush
                outLine();
            }
            if (ch == '\n' || ch == '\r') {
                str.append(ch);
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
                str.append(ch);
            }
        }
    }

    void outLine() throws IOException {
        if (str.length() == 0) {
            return;
        }
        char ch = str.charAt(str.length() - 1);
        if (ch == '\n' || ch == '\r') {
            // get latest eol char
            eol2 = ch;
            str.setLength(str.length() - 1);
        }
        if (str.length() > 0) {
            // get pre-latest eol char
            ch = str.charAt(str.length() - 1);
            if (ch == '\n' || ch == '\r') {
                eol1 = ch;
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
        if (str.length() <= maxLineLength) {
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
        if (latestNonSpacesTokenPos <= maxLineLength) {
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
            if (t.getOffset() >= lineLength) {
                // spaces can be after max length
                if (spacesStart >= 0 && spacesStart < maxLineLength) {
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
            if (t.getOffset() >= lineLength && t.getOffset() < maxLineLength) {
                if (isSpaces(t)) {
                    return t.getOffset();
                }
            }
            if (t.getOffset() + t.getLength() >= lineLength && t.getOffset() + t.getLength() < maxLineLength) {
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
            if (t.getOffset() >= lineLength && t.getOffset() < maxLineLength) {
                if (isPossibleBreakBefore(t.getOffset())) {
                    return t.getOffset();
                }
            }
            if (t.getOffset() + t.getLength() >= lineLength && t.getOffset() + t.getLength() < maxLineLength) {
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
            if (t.getOffset() >= lineLength) {
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
        for (int i = 0; i < token.getLength(); i++) {
            if (!Character.isWhitespace(str.charAt(token.getOffset() + i))) {
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
            char c = str.charAt(pos - 1);
            if ("([{<«„".indexOf(c) >= 0) {
                return false;
            }
        } catch (StringIndexOutOfBoundsException ex) {
        }
        try {
            // check next char. Can't split before specified chars.
            char c = str.charAt(pos);
            if (")]}>»“,.".indexOf(c) >= 0) {
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
