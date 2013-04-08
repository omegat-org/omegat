/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Volker Berlin
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters2.text.dokuwiki;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.Instance;
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.OStrings;

/**
 * Filter to support Files with the DokuWiki syntax
 * http://www.dokuwiki.org/syntax. The DokuWiki save it content in *.txt files
 * 
 * @author Volker Berlin
 */
public class DokuWikiFilter extends AbstractFilter {
    private Pattern codeTag = Pattern.compile("\\<code|\\<file|\\<html|\\<php|\\/\\*");

    @Override
    public String getFileFormatName() {
        return OStrings.getString("DWFILTER_FILTER_NAME");
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
    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.txt", "UTF-8", "UTF-8"), };
    }
    
    protected boolean requirePrevNextFields() {
        return true;
    }

    @Override
    protected boolean isFileSupported(BufferedReader reader) {
        LinebreakPreservingReader lbpr = new LinebreakPreservingReader(reader);

        try {
            String line;
            while ((line = lbpr.readLine()) != null) {
                String trimmed = line.trim();
                if (getHeadingLevel(trimmed) > 0) {
                    lbpr.close();
                    return true;
                }
            }
            lbpr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * {@inheritDoc} Syntax see at http://www.dokuwiki.org/syntax
     */
    @Override
    public void processFile(BufferedReader reader, BufferedWriter outfile, FilterContext fc) throws IOException {
        LinebreakPreservingReader lbpr = new LinebreakPreservingReader(reader); // fix
                                                                                // for
                                                                                // bug
                                                                                // 1462566
        String line;
        StringBuilder text = new StringBuilder();

        while ((line = lbpr.readLine()) != null) {
            String trimmed = line.trim();

            // skipping empty strings
            if (trimmed.length() == 0) {
                writeTranslate(outfile, text, lbpr);
                outfile.write(line + lbpr.getLinebreak());
                continue;
            }

            // heading like "=== Abc ==="
            int headingLevel = getHeadingLevel(trimmed);
            if (headingLevel > 0) {
                writeTranslate(outfile, text, lbpr);
                String header = trimmed.substring(headingLevel, trimmed.length() - headingLevel).trim();
                if (header.length() > 0) {
                    String trans = processEntry(header);
                    line = line.replace(header, trans);
                }
                outfile.write(line + lbpr.getLinebreak());
                continue;
            }

            // list like "  * Abc" or "  - Abc"
            if (line.startsWith("  *") || line.startsWith("  -")) {
                writeTranslate(outfile, text, lbpr);
                outfile.write(line.substring(0, 3));
                outfile.write(' ');
                writeTranslate(outfile, line.substring(3), lbpr);
                continue;
            }

            // image alone like "{{any}}" or macros alone like "~~any~~"
            if ((trimmed.startsWith("{{") && trimmed.endsWith("}}"))
                    || (trimmed.startsWith("~~") && trimmed.endsWith("~~") && trimmed.length() > 5)) {
                writeTranslate(outfile, text, lbpr);
                outfile.write(line + lbpr.getLinebreak());
                continue;
            }

            // tables
            if (line.startsWith("|") || line.startsWith("^")) {
                writeTranslate(outfile, text, lbpr);
                int start = 0;
                int braceCount = 0;
                for (int i = 0; i < line.length(); i++) {
                    char ch = line.charAt(i);
                    switch (ch) {
                    case '|':
                    case '^':
                        if (braceCount == 0) {
                            String value = line.substring(start, i);
                            if (start > 0) {
                                outfile.write(' ');
                                writeTranslate(outfile, value, null);
                                outfile.write(' ');
                            }
                            outfile.write(ch);
                            start = i + 1;
                        }
                        break;
                    case '{':
                        braceCount++;
                        break;
                    case '}':
                        braceCount--;
                        break;
                    }
                }
                outfile.write(lbpr.getLinebreak());
                continue;
            }

            // skip code fragments
            trimmed = skipCode(outfile, text, lbpr, line);
            if (trimmed == null) {
                return;
            }

            text.append(' ');
            text.append(trimmed);
        }
        writeTranslate(outfile, text, lbpr);
    }

    /**
     * Check if the line is a heading and which level of heading
     * 
     * @param line
     *            the lien to check
     * @return the level, 0 means no heading
     */
    private int getHeadingLevel(String line) {
        int level = 0;
        int length = line.length() - 1;
        while (level < length && line.charAt(level) == '=' && line.charAt(length) == '=') {
            level++;
            length--;
        }
        if (level < length) {
            return level;
        } else {
            return 0;
        }
    }

    /**
     * Check if there are data to translate in the StringBuilder. If yes then it
     * translate it and reset the StringBuilder.
     * 
     * @param outfile
     *            Writer of the target file on compilation
     * @param text
     *            The possible to translate text
     * @param lbpr
     *            the line breaker
     * @throws IOException
     *             If an I/O error occurs
     */
    private void writeTranslate(BufferedWriter outfile, StringBuilder text, LinebreakPreservingReader lbpr)
            throws IOException {
        if (text.length() > 0) {
            String value = text.toString();
            text.setLength(0);
            writeTranslate(outfile, value, lbpr);
        }
    }

    /**
     * Check if there are data to translate. If yes then it translate it.
     * 
     * @param outfile
     *            Writer of the target file on compilation
     * @param value
     *            The possible to translate text
     * @param lbpr
     *            the line breaker or null if no line break should be added
     * @throws IOException
     *             If an I/O error occurs
     */
    private void writeTranslate(BufferedWriter outfile, String value, LinebreakPreservingReader lbpr)
            throws IOException {
        value = value.trim();
        if (value.length() > 0) {
            while (true) {
                // reduce all spaces to a single space
                String newValue = value.replace("  ", " ");
                if (newValue.equals(value)) {
                    break;
                }
                value = newValue;
            }
            String trans = processEntry(value);
            outfile.write(trans);
            if (lbpr != null) {
                outfile.write(lbpr.getLinebreak());
            }
        }
    }

    /**
     * Skip comments and code blocks.
     * 
     * @param outfile
     *            Writer of the target file on compilation
     * @param text
     *            The possible to translate text
     * @param lbpr
     *            the line breaker
     * @param trimmed
     *            the current trimmed line
     * @return the new trimmed line after skip
     * @throws IOException
     *             If an I/O error occurs
     */
    private String skipCode(BufferedWriter outfile, StringBuilder text, LinebreakPreservingReader lbpr,
            String line) throws IOException {
        while (true) {
            Matcher matcher = codeTag.matcher(line);
            if (matcher.find()) {
                int start = matcher.start();
                String tagName = line.substring(start + 1, matcher.end());
                boolean isAsterisk = tagName.equals("*");
                text.append(' ');
                text.append(line.substring(0, start));
                if (!isAsterisk) {
                    writeTranslate(outfile, text, lbpr);
                }
                String endTagPattern = isAsterisk ? "\\*\\/" : "\\</" + tagName + "\\>";
                Pattern endTag = Pattern.compile(endTagPattern);
                line = line.substring(start);
                matcher = endTag.matcher(line);
                while (!matcher.find()) {
                    outfile.write(line + lbpr.getLinebreak());
                    line = lbpr.readLine();
                    if (line == null) {
                        return null;
                    }
                    matcher = endTag.matcher(line);
                }
                int end = matcher.end();
                outfile.write(line.substring(0, end) + lbpr.getLinebreak());
                line = line.substring(end);
            } else {
                return line;
            }
        }
    }
}
