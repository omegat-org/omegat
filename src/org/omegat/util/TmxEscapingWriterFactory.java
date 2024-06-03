/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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

package org.omegat.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.codehaus.stax2.io.EscapingWriterFactory;
import org.jetbrains.annotations.NotNull;

public class TmxEscapingWriterFactory implements EscapingWriterFactory {

    @Override
    public Writer createEscapingWriterFor(final Writer writer, final String s)
            throws UnsupportedEncodingException {
        return new EscapeWriter(writer);
    }

    @Override
    public Writer createEscapingWriterFor(final OutputStream outputStream, final String s)
            throws UnsupportedEncodingException {
        return new EscapeWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
    }

    public static class EscapeWriter extends Writer {

        // Copy from woodstox:com.ctc.wstx.sw.BufferingXmlWriter
        private static final int[] QUOTABLE_TEXT_CHARS;
        private static final int HIGH_ENC = 0xFFFE;

        static {
            int[] q = new int[4096];
            Arrays.fill(q, 0, 32, 1);
            Arrays.fill(q, 127, 160, 1);
            q['\t'] = 0;
            q['\n'] = 0;
            q['<'] = 1;
            q['>'] = 1;
            q['&'] = 1;
            QUOTABLE_TEXT_CHARS = q;
        }

        private final Writer delegate;

        public EscapeWriter(@NotNull Writer delegate) {
            this.delegate = delegate;
        }

        /**
         * Wrap Writer and escape characters.
         *
         * @param cbuf Array of characters
         * @param off  Offset from which to start writing characters
         * @param len  Number of characters to write
         * @throws IOException
         */
        @Override
        public void write(@NotNull final char[] cbuf, final int off, final int len) throws IOException {
            StringBuilder escaped = new StringBuilder();
            for (int i = off; i < off + len; i++) {
                char c = cbuf[i];
                if (c < 4096 && QUOTABLE_TEXT_CHARS[c] != 0) {
                    switch (c) {
                    case '<':
                        escaped.append("&lt;");
                        break;
                        case '>':
                            escaped.append("&gt;");
                            break;
                        case '&':
                            escaped.append("&amp;");
                            break;
                        default:
                            escaped.append(String.format("&#x%02x;", (int) c));
                    }
                } else if (c >= HIGH_ENC) {
                    escaped.append(String.format("&#x%04x;", (int) c));
                } else {
                    escaped.append(c);
                }
            }
            delegate.write(escaped.toString());
        }

        @Override
        public void flush() throws IOException {
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }
    }
}
