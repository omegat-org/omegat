/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.preferences.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.KeyStroke;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import org.omegat.gui.shortcuts.PropertiesShortcuts;

/**
 * Proves the staged shortcut editing of the preferences table model:
 * enumeration with raw-key fallback labels, modification state, the
 * same-scope/cross-scope conflict rules, restore-default, and the
 * export/import round trip of the documented file format.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class ShortcutsTableModelTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ShortcutsTableModel model;

    @Before
    public void setUp() throws Exception {
        // Ad-hoc shortcut sets: the test fixture of PropertiesShortcuts
        // serves as the menu scope, a synthetic file as the editor scope.
        PropertiesShortcuts menuSet = new PropertiesShortcuts();
        menuSet.loadFromClasspath("/org/omegat/gui/shortcuts/test.properties");
        PropertiesShortcuts editorSet = new PropertiesShortcuts();
        File editorFile = folder.newFile("editor.properties");
        Files.writeString(editorFile.toPath(),
                "editorNextSegment=ctrl N\nautocompleterTrigger=ctrl SPACE\n", StandardCharsets.UTF_8);
        editorSet.loadFromClasspath("/org/omegat/gui/shortcuts/nonexistent.properties");
        editorSet.loadFromFile(editorFile);

        model = new ShortcutsTableModel(menuSet, editorSet,
                Map.of("TEST_SAVE", new ShortcutsTableModel.MenuEntry("Save", "Project")));
    }

    private int rowOf(String key) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getKey(i).equals(key)) {
                return i;
            }
        }
        throw new AssertionError("row not found: " + key);
    }

    @Test
    public void testEnumerationAndLabels() {
        // menu label from the harvest, localized label for known editor
        // keys, raw key for unknown ones
        assertEquals("Save", model.getFunctionLabel(rowOf("TEST_SAVE")));
        assertEquals("TEST_CUT", model.getFunctionLabel(rowOf("TEST_CUT")));
        assertFalse("known editor key must get its localized label",
                "editorNextSegment".equals(model.getFunctionLabel(rowOf("editorNextSegment"))));
        assertEquals(ShortcutsTableModel.Scope.AUTOCOMPLETER.getDisplayName(),
                model.getValueAt(rowOf("autocompleterTrigger"), ShortcutsTableModel.COLUMN_SCOPE));
    }

    @Test
    public void testConflictRules() {
        int save = rowOf("TEST_SAVE");
        int cut = rowOf("TEST_CUT");
        int editorNext = rowOf("editorNextSegment");
        KeyStroke ctrlS = KeyStroke.getKeyStroke("ctrl S");

        assertEquals(ShortcutsTableModel.ConflictKind.NONE, model.getConflictKind(save));

        // same scope: hard conflict, both rows report it
        model.setShortcut(cut, ctrlS);
        assertEquals(ShortcutsTableModel.ConflictKind.CONFLICT, model.getConflictKind(save));
        assertEquals(ShortcutsTableModel.ConflictKind.CONFLICT, model.getConflictKind(cut));
        assertTrue(model.conflictText(cut).contains("Save"));

        // cross scope: warning only - also between editor and autocompleter,
        // which share a file but never the same context
        model.setShortcut(cut, KeyStroke.getKeyStroke("ctrl X"));
        model.setShortcut(editorNext, ctrlS);
        assertEquals(ShortcutsTableModel.ConflictKind.WARNING, model.getConflictKind(save));
        assertEquals(ShortcutsTableModel.ConflictKind.WARNING, model.getConflictKind(editorNext));
        int trigger = rowOf("autocompleterTrigger");
        model.setShortcut(trigger, ctrlS);
        assertEquals("editor vs autocompleter must only warn",
                ShortcutsTableModel.ConflictKind.WARNING, model.getConflictKind(trigger));
        model.setShortcut(trigger, KeyStroke.getKeyStroke("ctrl SPACE"));

        // unbound rows never conflict
        model.setShortcut(editorNext, null);
        assertEquals(ShortcutsTableModel.ConflictKind.NONE, model.getConflictKind(editorNext));
        assertEquals("", model.conflictText(editorNext));
    }

    @Test
    public void testApplyConfirmOnlyNagsAboutEditedConflicts() {
        int save = rowOf("TEST_SAVE");
        int cut = rowOf("TEST_CUT");
        // an untouched model reports nothing, even if defaults shipped with
        // conflicts - here we fabricate one and reset the touch state via a
        // fresh check on unchanged rows
        assertTrue(model.hardConflictsInvolvingEdits().isEmpty());

        model.setShortcut(cut, KeyStroke.getKeyStroke("ctrl S"));
        assertEquals("an edit into a conflict names both sides", 2,
                model.hardConflictsInvolvingEdits().size());

        model.restoreDefault(cut);
        assertTrue("restoring the edit silences the nag",
                model.hardConflictsInvolvingEdits().isEmpty());
        assertEquals(ShortcutsTableModel.ConflictKind.NONE, model.getConflictKind(save));
    }

    @Test
    public void testModificationAndRestore() {
        int save = rowOf("TEST_SAVE");
        assertFalse(model.isModified(save));
        model.setShortcut(save, KeyStroke.getKeyStroke("ctrl P"));
        assertTrue(model.isModified(save));
        model.restoreDefault(save);
        assertFalse(model.isModified(save));
        assertEquals(KeyStroke.getKeyStroke("ctrl S"), model.getStaged(save));

        model.setShortcut(save, null);
        assertTrue(model.isModified(save));
        model.restoreAllDefaults();
        assertFalse(model.isModified(save));
    }

    @Test
    public void testExportImportRoundTrip() throws Exception {
        int save = rowOf("TEST_SAVE");
        model.setShortcut(save, KeyStroke.getKeyStroke("ctrl P"));
        File dir = folder.newFolder("export");
        model.exportTo(dir);

        Properties menuFile = new Properties();
        try (var in = Files.newInputStream(new File(dir, "MainMenuShortcuts.properties").toPath())) {
            menuFile.load(in);
        }
        assertEquals("export must carry the staged value",
                PropertiesShortcuts.toPropertyValue(KeyStroke.getKeyStroke("ctrl P")),
                menuFile.getProperty("TEST_SAVE"));
        assertTrue("export must be complete per scope",
                menuFile.stringPropertyNames().contains("TEST_CUT"));

        // a fresh model imports the exported file and reaches the same state
        model.restoreAllDefaults();
        List<String> rejected = model.importFrom(new File(dir, "MainMenuShortcuts.properties"));
        assertTrue(rejected.isEmpty());
        assertEquals(KeyStroke.getKeyStroke("ctrl P"), model.getStaged(rowOf("TEST_SAVE")));
    }

    @Test
    public void testImportReportsUnknownKeysAndBrokenValues() throws Exception {
        File file = folder.newFile("import.properties");
        Files.writeString(file.toPath(),
                "TEST_SAVE=ctrl P\nnoSuchFunction=ctrl Q\nTEST_CUT=not a shortcut\n",
                StandardCharsets.UTF_8);
        List<String> rejected = model.importFrom(file);
        assertEquals(2, rejected.size());
        assertTrue(rejected.contains("noSuchFunction"));
        assertTrue(rejected.contains("TEST_CUT=not a shortcut"));
        assertEquals("valid entries import despite rejects", KeyStroke.getKeyStroke("ctrl P"),
                model.getStaged(rowOf("TEST_SAVE")));
        assertEquals("broken value must not change the row", KeyStroke.getKeyStroke("ctrl X"),
                model.getStaged(rowOf("TEST_CUT")));
    }
}
