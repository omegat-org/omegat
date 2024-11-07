package org.omegat.gui.editor;

import org.junit.Test;

import org.omegat.gui.main.TestCoreGUI;
import org.omegat.util.OStrings;

public class EditorTextAreaTest extends TestCoreGUI {

    @Test
    public void testIntroPaneExist() {
        window.panel(OStrings.getString("DOCKING_FIRST_STEPS_TITLE")).requireEnabled();
        window.panel(OStrings.getString("DOCKING_FIRST_STEPS_TITLE")).scrollPane("EditorScrollPane").requireEnabled();
        window.panel(OStrings.getString("DOCKING_FIRST_STEPS_TITLE")).scrollPane("EditorScrollPane")
                .verticalScrollBar().requireVisible();
        window.panel(OStrings.getString("DOCKING_FIRST_STEPS_TITLE")).scrollPane("EditorScrollPane")
                .horizontalScrollBar().requireNotVisible();
    }

}
