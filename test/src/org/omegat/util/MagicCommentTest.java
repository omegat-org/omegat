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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class MagicCommentTest {
    @SuppressWarnings("serial")
    @Test
    public void testParseString() {
        // TODO: junit5 parameterized test.
        assertEquals(new HashMap<String, String>() {{ put("coding", "UTF-8"); }},
                MagicComment.parse("# -*- coding: UTF-8 -*-"));
        assertEquals(new HashMap<String, String>() {{ put("coding", "UTF-8"); }},
                MagicComment.parse("# comment -*- coding: UTF-8 -*-"));
        assertEquals(new HashMap<String, String>() {
            {
                put("coding", "UTF-8");
                put("foo", "bar");
            }
        }, MagicComment.parse("# comment -*- coding: UTF-8; foo: bar -*-"));
        assertEquals(new HashMap<String, String>() {
            {
                put("coding", "UTF-8");
                put("foo", "bar");
            }
        }, MagicComment.parse("# comment -*- foo: bar; coding: UTF-8; -*-"));
        assertEquals(new HashMap<String, String>() {{ put("foo", "bar"); }},
                MagicComment.parse("# comment -*- foo: bar; -*- coding: UTF-8"));
        assertEquals(Collections.emptyMap(), MagicComment.parse("# comment -*- foo: bar; coding: UTF-8"));
        assertEquals(Collections.emptyMap(), MagicComment.parse("# comment foo: bar; coding: UTF-8 -*-"));
        assertEquals(Collections.emptyMap(), MagicComment.parse((String) null));
    }

    @SuppressWarnings("serial")
    @Test
    public void testParseFile() throws IOException {
        Map<String, String> result = MagicComment.parse(new File("test/data/glossaries/test-magiccomment.tab"));
        assertEquals(new HashMap<String, String>() {{ put("coding", "utf-8"); }}, result);
    }

    @SuppressWarnings("serial")
    @Test
    public void testParseFileBom() throws IOException {
        Map<String, String> result = MagicComment.parse(new File("test/data/glossaries/test-magiccomment-bom.tab"));
        assertEquals(new HashMap<String, String>() {{ put("coding", "utf-8"); }}, result);
    }

    @SuppressWarnings("serial")
    @Test
    public void testParseEmpty() throws IOException {
        Map<String, String> result = MagicComment.parse(new File("test/data/glossaries/empty.txt"));
        assertEquals(Collections.emptyMap(), result);
    }

    @SuppressWarnings("serial")
    @Test
    public void testParseFileTab() throws IOException {
        Map<String, String> result = MagicComment.parse(new File("test/data/glossaries/test.tab"));
        assertEquals(Collections.emptyMap(), result);
    }

    @SuppressWarnings("serial")
    @Test
    public void testParseFileUTF16() throws IOException {
        Map<String, String> result = MagicComment.parse(new File("test/data/glossaries/testUTF16LE.txt"));
        assertEquals(Collections.emptyMap(), result);
    }}
