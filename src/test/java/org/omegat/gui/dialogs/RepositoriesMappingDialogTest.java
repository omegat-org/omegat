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

package org.omegat.gui.dialogs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.awt.GraphicsEnvironment;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Leaving the repositories mapping dialog through any path other than a
 * successful OK must return null, so the caller keeps the previous mapping.
 * A team project whose repository row was removed before cancelling used to
 * receive the emptied table state anyway, and the next project save then
 * silently dropped the repositories element from omegat.project.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class RepositoriesMappingDialogTest {

    @Before
    public final void setUp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
    }

    private static List<RepositoryDefinition> teamRepositories() {
        RepositoryDefinition repo = new RepositoryDefinition();
        repo.setType("git");
        repo.setUrl("https://example.com/team-project.git");
        RepositoryMapping mapping = new RepositoryMapping();
        mapping.setLocal("");
        mapping.setRepository("");
        repo.getMapping().add(mapping);
        List<RepositoryDefinition> repositories = new ArrayList<>();
        repositories.add(repo);
        return repositories;
    }

    @Test
    public void testOkReturnsTableState() {
        RepositoriesMappingDialog dialog = new RepositoriesMappingDialog(null, false);
        dialog.init(teamRepositories());
        dialog.okButton.doClick();
        assertNotNull("OK must return the table state", dialog.result);
        assertEquals(1, dialog.result.size());
        assertEquals("https://example.com/team-project.git", dialog.result.get(0).getUrl());
        dialog.dispose();
    }

    @Test
    public void testCancelReturnsNull() {
        RepositoriesMappingDialog dialog = new RepositoriesMappingDialog(null, false);
        dialog.init(teamRepositories());
        dialog.cancelButton.doClick();
        assertNull("Cancel must not return the table state", dialog.result);
        dialog.dispose();
    }

    @Test
    public void testClosingWithoutOkReturnsNull() {
        // Escape and the window close button leave the dialog without
        // passing through either button; show() must then return null.
        RepositoriesMappingDialog dialog = new RepositoriesMappingDialog(null, false);
        List<RepositoryDefinition> result = dialog.show(teamRepositories());
        assertNull("Leaving the dialog without OK must return null", result);
    }

    @Test
    public void testWindowClosingReturnsNull() {
        // Escape is translated into a WINDOW_CLOSING event, so this covers
        // both the Escape key and the window close button.
        RepositoriesMappingDialog dialog = new RepositoriesMappingDialog(null, false);
        dialog.init(teamRepositories());
        dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
        assertNull("Closing the window must not return the table state", dialog.result);
    }
}
