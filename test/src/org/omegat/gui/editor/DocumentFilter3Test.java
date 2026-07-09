/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.omegat.core.Core;
import org.omegat.util.Preferences;
import org.omegat.util.TestPreferencesInitializer;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.StyleConstants;
import java.lang.reflect.InvocationTargetException;

public class DocumentFilter3Test {

    @Before
    public final void setUp() throws Exception {
        Core.initializeConsole();
        TestPreferencesInitializer.init();
        Preferences.setPreference(Preferences.ALLOW_TAG_EDITING, true);
    }

    /**
     * Tests for the replace method in the DocumentFilter3 class.
     * <p>
     * The replace method allows replacing text in a document at a given offset
     * and length. It first ensures the action is being performed in the Swing
     * thread and validates the operation via the isPossible method. If valid,
     * it replaces the text in the document.
     */
    @Test
    public void testReplace_AllowsValidReplacement()
            throws BadLocationException, InterruptedException, InvocationTargetException {
        // Arrange
        DocumentFilter3 filter = spy(new DocumentFilter3());
        DocumentFilter.FilterBypass bypassMock = mock(DocumentFilter.FilterBypass.class);
        Document3 documentMock = mock(Document3.class);
        AttributeSet attrsMock = mock(AttributeSet.class);
        EditorController editorControllerMock = mock(EditorController.class);

        when(documentMock.getController()).thenReturn(editorControllerMock);
        when(documentMock.isEditMode()).thenReturn(true);
        when(documentMock.getTranslationEnd()).thenReturn(6);
        when(documentMock.getTranslationStart()).thenReturn(2);
        when(filter.isPossible(documentMock, 2, 3)).thenReturn(true);
        when(bypassMock.getDocument()).thenReturn(documentMock);
        when(documentMock.isEditMode()).thenReturn(true);
        when(documentMock.getTranslationStart()).thenReturn(0);
        when(documentMock.getTranslationEnd()).thenReturn(10);
        when(documentMock.getController()).thenReturn(editorControllerMock);
        when(attrsMock.isDefined(StyleConstants.ComposedTextAttribute)).thenReturn(false);

        // Act
        SwingUtilities.invokeAndWait(() -> {
            try {
                filter.replace(bypassMock, 2, 3, "new text", attrsMock);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });

        // Assert
        verify(bypassMock, times(1)).replace(2, 3, "new text", attrsMock);
    }

    @Test
    public void testReplace_DoesNotAllowReplacement_OutOfBounds()
            throws BadLocationException, InterruptedException, InvocationTargetException {
        // Arrange
        DocumentFilter3 filter = new DocumentFilter3();
        DocumentFilter.FilterBypass bypassMock = mock(DocumentFilter.FilterBypass.class);
        Document3 documentMock = mock(Document3.class);
        EditorController editorControllerMock = mock(EditorController.class);

        when(bypassMock.getDocument()).thenReturn(documentMock);
        when(documentMock.isEditMode()).thenReturn(true);
        when(documentMock.getTranslationStart()).thenReturn(5);
        when(documentMock.getTranslationEnd()).thenReturn(10);
        when(documentMock.getController()).thenReturn(editorControllerMock);

        // Act
        SwingUtilities.invokeAndWait(() -> {
            try {
                filter.replace(bypassMock, 3, 5, "new text", null);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });

        // Assert
        verify(bypassMock, times(0)).replace(anyInt(), anyInt(), anyString(), any());
    }

    @Test
    public void testReplace_TriggeredInTrustedMode()
            throws BadLocationException, InterruptedException, InvocationTargetException {
        // Arrange
        DocumentFilter3 filter = new DocumentFilter3();
        DocumentFilter.FilterBypass bypassMock = mock(DocumentFilter.FilterBypass.class);
        Document3 documentMock = mock(Document3.class);
        EditorController editorControllerMock = mock(EditorController.class);

        when(documentMock.getController()).thenReturn(editorControllerMock);
        when(bypassMock.getDocument()).thenReturn(documentMock);
        when(documentMock.getTrustedChangesInProgress()).thenReturn(true);

        // Act
        SwingUtilities.invokeAndWait(() -> {
            try {
                filter.replace(bypassMock, 0, 1, "trusted text", null);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });

        // Assert
        verify(bypassMock, times(1)).replace(0, 1, "trusted text", null);
    }

    @Test
    public void testReplace_RejectsWhenNotInEditMode()
            throws BadLocationException, InterruptedException, InvocationTargetException {
        // Arrange
        DocumentFilter3 filter = new DocumentFilter3();
        DocumentFilter.FilterBypass bypassMock = mock(DocumentFilter.FilterBypass.class);
        Document3 documentMock = mock(Document3.class);
        EditorController editorControllerMock = mock(EditorController.class);

        when(documentMock.getController()).thenReturn(editorControllerMock);
        when(bypassMock.getDocument()).thenReturn(documentMock);
        when(documentMock.isEditMode()).thenReturn(false);

        // Act
        SwingUtilities.invokeAndWait(() -> {
            try {
                filter.replace(bypassMock, 0, 1, "text", null);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });
        // Assert
        verify(bypassMock, times(0)).replace(anyInt(), anyInt(), anyString(), any());
    }

    @Test
    public void testReplace_SetsTextBeingComposed() throws BadLocationException, InterruptedException, InvocationTargetException {
        // Arrange
        DocumentFilter3 filter = spy(new DocumentFilter3());
        DocumentFilter.FilterBypass bypassMock = mock(DocumentFilter.FilterBypass.class);
        Document3 documentMock = mock(Document3.class);
        AttributeSet attrsMock = mock(AttributeSet.class);
        EditorController editorControllerMock = mock(EditorController.class);

        when(filter.isPossible(documentMock, 0, 1)).thenReturn(true);
        when(documentMock.getController()).thenReturn(editorControllerMock);
        when(bypassMock.getDocument()).thenReturn(documentMock);
        when(documentMock.isEditMode()).thenReturn(true);
        when(documentMock.getTranslationStart()).thenReturn(0);
        when(documentMock.getTranslationEnd()).thenReturn(10);
        when(attrsMock.isDefined(StyleConstants.ComposedTextAttribute)).thenReturn(true);

        // Act
        SwingUtilities.invokeAndWait(() -> {
            try {
                filter.replace(bypassMock, 0, 1, "composed text", attrsMock);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        });
        // Assert
        verify(documentMock, times(1)).setTextBeingComposed(true);
        verify(bypassMock, times(1)).replace(0, 1, "composed text", attrsMock);
    }
}
