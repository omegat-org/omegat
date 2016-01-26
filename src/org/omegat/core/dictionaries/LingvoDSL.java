/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.core.dictionaries;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Dictionary implementation for Lingvo DSL format.
 * 
 * Lingvo DSL format described in Lingvo help. See also
 * http://www.dsleditor.narod.ru/art_03.htm(russian).
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class LingvoDSL implements IDictionaryFactory {
    protected static final String CHARSET = "UTF-16";
    protected static final Pattern RE_SKIP = Pattern.compile("\\[.+?\\]");
    protected static final String[] EMPTY_RESULT = new String[0];

    @Override
    public boolean isSupportedFile(File file) {
        return file.getPath().endsWith(".dsl");
    }

    @Override
    public IDictionary loadDict(File file) throws Exception {
        return new LingvoDSLDict(loadData(file));
    }

    private static Map<String, String> loadData(File file) throws Exception {
        BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(file), CHARSET));
        try {
            Map<String, String> result = new HashMap<String, String>();
            String s;
            StringBuilder word = new StringBuilder();
            StringBuilder trans = new StringBuilder();
            while ((s = rd.readLine()) != null) {
                if (s.isEmpty()) {
                    continue;
                }
                if (s.codePointAt(0) == '#') {
                    continue;
                }
                s = RE_SKIP.matcher(s).replaceAll("");
                if (Character.isWhitespace(s.codePointAt(0))) {
                    trans.append(s.trim()).append('\n');
                } else {
                    if (word.length() > 0) {
                        result.put(word.toString(), trans.toString());
                        word.setLength(0);
                        trans.setLength(0);
                    }
                    word.append(s);
                }
            }
            if (word.length() > 0) {
                result.put(word.toString(), trans.toString());
            }
            return result;
        } finally {
            rd.close();
        }
    }

    static class LingvoDSLDict implements IDictionary {
        protected final Map<String, String> data;

        private LingvoDSLDict(Map<String, String> data) throws Exception {
            this.data = data;
        }

        @Override
        public List<DictionaryEntry> readArticles(String word) throws Exception {
            String article = data.get(word);
            if (article == null) {
                return Collections.emptyList();
            }
            DictionaryEntry entry = new DictionaryEntry(word, article);
            List<DictionaryEntry> result = new ArrayList<DictionaryEntry>();
            result.add(entry);
            return result;
        }
    }
}
