package org.omegat.gui.editor;

import org.junit.Test;
import org.omegat.core.TestCore;
import org.omegat.core.TestCoreInitializer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class EditorControllerTest extends TestCore {

    private EditorController editorController;

    @Test
    public void testEditorController() throws Exception {
        assertNotNull(editorController);
        assertNotNull(editorController.editor);
        assertEquals(0, editorController.displayedFileIndex);
    }

    @Override
    protected void initEditor() {
        editorController = new EditorController(mainWindow);
        TestCoreInitializer.initEditor(editorController);
    }
}
