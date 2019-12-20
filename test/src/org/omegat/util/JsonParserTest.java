/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
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

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import javax.script.ScriptException;

import org.junit.Test;

public class JsonParserTest {

    @Test
    public void testParse() throws Exception {
        {
            Object result = JsonParser.parse("{}");
            assertTrue(result instanceof Map);
        }
        {
            Object result = JsonParser.parse("{\"item\": []}");
            assertTrue(result instanceof Map);
            Map<?, ?> map = (Map<?, ?>) result;
            Object item = map.get("item");
            assertTrue(item instanceof List);
            List<?> list = (List<?>) item;
            assertTrue(list.isEmpty());
        }
    }

    @Test(expected = ScriptException.class)
    public void testParseEmpty() throws Exception {
        JsonParser.parse("");
    }

    @Test(expected = ScriptException.class)
    public void testParseInvalid() throws Exception {
        // Trailing comma is invalid
        JsonParser.parse("{\"item\": [],}");
    }
}
