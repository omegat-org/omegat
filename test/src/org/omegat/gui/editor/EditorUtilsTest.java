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
package org.omegat.gui.editor;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

public class EditorUtilsTest {
    @Test
    public void testGetBoundarySimple() {
        final String lineString = "Hello world of toys!";
        assertEquals(lineString.indexOf('w'), EditorUtils.getWordBoundary(new Locale("en"), lineString, 8,
                false));
        assertEquals(lineString.indexOf('d') + 1, EditorUtils.getWordBoundary(new Locale("en"), lineString, 8,
                true));
        assertEquals(lineString.indexOf('!'), EditorUtils.getWordBoundary(new Locale("en"), lineString, 15,
                true));
        assertEquals(lineString.indexOf('!') + 1, EditorUtils.getWordBoundary(new Locale("en"), lineString,
                lineString.length() + 2, true));
    }

    @Test
    public void testGetWordBoundaryJa() {
        final String lineString = "太平寺の中心的なペン塔";
        //太平寺-の-中心-的-な-ペン-塔
        assertEquals(lineString.indexOf('太'), EditorUtils.getWordBoundary(new Locale("ja"), lineString, 2,
                false));
        assertEquals(lineString.indexOf('寺') + 1, EditorUtils.getWordBoundary(new Locale("ja"), lineString, 2,
                true));
        assertEquals(lineString.indexOf('中'), EditorUtils.getWordBoundary(new Locale("ja"), lineString, 5,
                false));
        assertEquals(lineString.indexOf('心') + 1, EditorUtils.getWordBoundary(new Locale("ja"), lineString, 5,
                true));
    }

    @Test
    public void testGetWordBoundaryCn() {
        final String lineString = "太平寺中的文笔塔";
        // 太平寺-中的-文笔-塔
        assertEquals(lineString.indexOf('太'), EditorUtils.getWordBoundary(new Locale("zh_CN"), lineString, 2,
                false));
        assertEquals(lineString.indexOf('寺') + 1, EditorUtils.getWordBoundary(new Locale("zh_CN"), lineString, 2,
                true));
        assertEquals(lineString.indexOf('中'), EditorUtils.getWordBoundary(new Locale("zh_CN"), lineString, 4,
                false));
        assertEquals(lineString.indexOf('的') + 1, EditorUtils.getWordBoundary(new Locale("zh_CN"), lineString, 4,
                true));
    }
}
