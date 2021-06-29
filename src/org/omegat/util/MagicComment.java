/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura, Aaron Madlon-Kay
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.input.BOMInputStream;

/**
 * Utility for parsing "magic comments": a leading line in a file that contains
 * metadata or settings like
 *
 * <pre>
 * # -*- coding: utf-8; mode: java; tab-width: 4 -*-
 * </pre>
 *
 * @author Hiroshi Miura
 * @author Aaron Madlon-Kay
 */
public class MagicComment {
    /**
     * Grammar (whitespace omitted)
     *
     * <pre>
     *  magic-comment = "-*-" , commands , "-*-" ;
     *  commands: command , { ";" , command } ;
     *  command: key , ":" value ;
     *  key and value: ? string w/ alphabet, number, underscore, hyphen ? ;
     * </pre>
     */
    private static final Pattern MAGIC_COMMENT_PATTERN = Pattern.compile("(?<key>[\\w-]+)\\s*:\\s*(?<value>[\\w-]+)(?:\\s*;)?");

    /**
     * Extract the first line of the file and parse with {@code #parse(String)}
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static Map<String, String> parse(File file) throws IOException  {
        String line;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new BOMInputStream(new FileInputStream(file)), StandardCharsets.US_ASCII))) {
            line = reader.readLine();
        }
        if (line != null && line.startsWith("#")) {
            return parse(line);
        } else {
            return Collections.emptyMap();
        }
    }

    /**
     * Parse magic comment
     *
     * @param str input string.
     * @return Key-Value map of String.
     */
    public static Map<String, String> parse(final String str) {
        if (str == null || str.length() < 11) {  // minimum = "-*- a:b -*-".length()
            return Collections.emptyMap();
        }
        int startMarker = str.indexOf("-*- ");
        if (startMarker < 0) {
            return Collections.emptyMap();
        }
        int start = startMarker + 4; // add length of "-*- "
        int end = str.indexOf(" -*-", start);
        if (end < 0) {
            return Collections.emptyMap();
        }
        HashMap<String, String> result = new HashMap<>();
        Matcher m = MAGIC_COMMENT_PATTERN.matcher(str);
        int i = start;
        while (m.find(i) && i < end) {
            String key = m.group("key");
            String value = m.group("value");
            result.put(key, value);
            i = m.end();
        }
        return result;
    }
}
