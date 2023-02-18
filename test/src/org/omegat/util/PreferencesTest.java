/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015-2016 Aaron Madlon-Kay
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.omegat.filters.TestFilterBase;
import org.omegat.util.Preferences.IPreferences;

/**
 * @author Aaron Madlon-Kay
 */
public class PreferencesTest {

    private File tmpDir;

    @Before
    public final void setUp() throws Exception {
        tmpDir = Files.createTempDirectory("omegat").toFile();
        assertTrue(tmpDir.isDirectory());
    }

    @After
    public final void tearDown() throws Exception {
        FileUtils.deleteDirectory(tmpDir);
    }

    /**
     * Test that if an error is encountered when loading the
     * preferences file, the original file is backed up.
     */
    @Test
    public void testPreferencesBackup() throws Exception {
        File prefsFile = new File(tmpDir, Preferences.FILE_PREFERENCES);

        // Write anything that is malformed XML, to force a parsing error.
        PrintWriter out = new PrintWriter(prefsFile, "UTF-8");
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        out.println("<omegat>");
        out.println("<preference version=\"1.0\">");
        out.close();
        assertFalse(out.checkError());

        // Load bad prefs file.
        Log.log("---------It will show a parser error. This is intended----------------");
        new PreferencesImpl(new PreferencesXML(prefsFile, null));
        Log.log("-----------------------------------------------------------------Done.");

        // The actual backup file will have a timestamp in the filename,
        // so we have to loop through looking for it.
        File backup = null;
        for (File f : tmpDir.listFiles()) {
            String name = f.getName();
            if (name.startsWith(prefsFile.getName()) && name.endsWith(".bak")) {
                backup = f;
                break;
            }
        }

        assertNotNull(backup);
        assertTrue(backup.isFile());

        TestFilterBase.compareBinary(prefsFile, backup);
    }

    enum TestEnum {
        BAR, BAZ, BAZINGA;

        public String toString() {
            if (this == BAZINGA) {
                return "!";
            } else {
                return super.toString();
            }
        };
    }

    @Test
    public void testPreferencesLoadStore() throws Exception {
        File prefsFile = new File(tmpDir, Preferences.FILE_PREFERENCES);

        IPreferences prefs = new PreferencesImpl(new PreferencesXML(null, prefsFile));

        // Set values normally
        assertEquals(null, prefs.setPreference("MyString", "foo"));
        assertEquals(null, prefs.setPreference("MyBoolean", true));
        assertEquals(null, prefs.setPreference("MyInt", 5));
        assertEquals(null, prefs.setPreference("MyEnum", TestEnum.BAR));
        assertEquals(null, prefs.setPreference("MyEmptyString", ""));

        // Check values normally
        assertEquals("foo", prefs.getPreference("MyString"));
        assertEquals(true, prefs.isPreference("MyBoolean"));
        assertEquals(TestEnum.BAR, TestEnum.valueOf(prefs.getPreference("MyEnum")));
        assertEquals(5, Integer.parseInt(prefs.getPreference("MyInt")));
        // Check empty string behavior
        assertEquals("", prefs.getPreference("MyEmptyString"));
        assertTrue(prefs.existsPreference("MyEmptyString"));
        assertEquals("", prefs.getPreference("MyNonexistentString"));
        assertFalse(prefs.existsPreference("MyNonexistentString"));
        // Check that defaults are not used for already-set values
        assertEquals("foo", prefs.getPreferenceDefault("MyString", "bar"));
        assertEquals(true, prefs.isPreferenceDefault("MyBoolean", false));
        assertEquals(5, prefs.getPreferenceDefault("MyInt", 0));
        assertEquals(TestEnum.BAR, prefs.getPreferenceEnumDefault("MyEnum", TestEnum.BAZ));

        // Persist prefs
        prefs.save();

        // Load prefs from persisted version
        prefs = new PreferencesImpl(new PreferencesXML(prefsFile, null));

        // Check values
        assertEquals("foo", prefs.getPreference("MyString"));
        assertEquals(true, prefs.isPreference("MyBoolean"));
        assertEquals(5, Integer.parseInt(prefs.getPreference("MyInt")));
        assertEquals(TestEnum.BAR, TestEnum.valueOf(prefs.getPreference("MyEnum")));
        assertEquals("", prefs.getPreference("MyEmptyString"));
        assertTrue(prefs.existsPreference("MyEmptyString"));
        assertEquals("", prefs.getPreference("MyNonexistentString"));
        assertFalse(prefs.existsPreference("MyNonexistentString"));
        assertEquals("foo", prefs.getPreferenceDefault("MyString", "bar"));
        assertEquals(true, prefs.isPreferenceDefault("MyBoolean", false));
        assertEquals(5, prefs.getPreferenceDefault("MyInt", 0));
        assertEquals(TestEnum.BAR, prefs.getPreferenceEnumDefault("MyEnum", TestEnum.BAZ));

        // Check setting values via *Default methods
        assertEquals("bar", prefs.getPreferenceDefault("MyStringDefault", "bar"));
        assertEquals("bar", prefs.getPreference("MyStringDefault"));
        assertEquals(false, prefs.isPreferenceDefault("MyBoolDefault", false));
        assertEquals(false, prefs.isPreference("MyBoolDefault"));
        assertEquals(77, prefs.getPreferenceDefault("MyIntDefault", 77));
        assertEquals(77, Integer.parseInt(prefs.getPreference("MyIntDefault")));
        assertEquals(TestEnum.BAZ, prefs.getPreferenceEnumDefault("MyEnumDefault", TestEnum.BAZ));
        assertEquals(TestEnum.BAZ, TestEnum.valueOf(prefs.getPreference("MyEnumDefault")));

        // Check writing new value
        assertEquals("bar", prefs.setPreference("MyStringDefault", "baz"));

        // Check bad enum
        try {
            prefs.setPreference("Bad!", TestEnum.BAZINGA);
            fail();
        } catch (IllegalArgumentException ex) {
            // Can't store enum where toString() gives a different result than
            // name()
        }

        // Check null behvaior
        assertEquals(null, prefs.setPreference(null, null));
        assertEquals(null, prefs.setPreference("", null));
        assertEquals(null, prefs.setPreference("blah", null));
        assertEquals("", prefs.getPreference(null));
        assertEquals("", prefs.getPreference(""));
    }

    @Test
    public void testLoadingUserPreferencesXML() {
        File loadFile = new File("test/data/preferences/omegat.prefs.xml");
        File saveFile = new File(tmpDir, "omegat-save.prefs");
        Preferences.IPreferences preferences = new PreferencesImpl(new PreferencesXML(loadFile, saveFile));
        Assert.assertEquals("14", preferences.getPreference(Preferences.TF_SRC_FONT_SIZE));
        preferences.setPreference(Preferences.TF_SRC_FONT_SIZE, "12");
        preferences.save();
        Preferences.IPreferences savedPreferences = new PreferencesImpl(new PreferencesXML(saveFile,
                null));
        Assert.assertEquals("12", savedPreferences.getPreference(Preferences.TF_SRC_FONT_SIZE));
    }

}
