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

package org.omegat.gui.editor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.omegat.core.data.ProtectedPartsFixtures.entryWithProtectedParts;
import static org.omegat.core.data.ProtectedPartsFixtures.tokenAt;

import org.junit.Test;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.util.Token;

/**
 * The spell checker popup must stay quiet inside protected placeholders:
 * where the marker draws no wave, the right click offers no suggestions and
 * especially no learn/ignore that would put placeholder fragments into the
 * learned words.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class EditorPopupsProtectedPartsTest {

    @Test
    public void testWordInsidePlaceholderIsProtected() {
        String translation = "Server error %1$ld: %2$@";
        SourceTextEntry ste = entryWithProtectedParts("Serverfehler %1$ld: %2$@", "%1$ld", "%2$@");
        assertTrue("the ld inside %1$ld must get no popup items",
                EditorPopups.SpellCheckerPopup.isInsideProtectedPart(tokenAt(translation, "ld"), ste,
                        translation));
    }

    @Test
    public void testWordOutsidePlaceholderIsNotProtected() {
        String translation = "Server errorr %1$ld";
        SourceTextEntry ste = entryWithProtectedParts("Serverfehler %1$ld", "%1$ld");
        assertFalse("a real typo next to the placeholder must keep its popup",
                EditorPopups.SpellCheckerPopup.isInsideProtectedPart(tokenAt(translation, "errorr"), ste,
                        translation));
    }

    @Test
    public void testWordOverlappingPlaceholderBoundaryIsNotProtected() {
        String translation = "errld%1$ld";
        SourceTextEntry ste = entryWithProtectedParts("%1$ld", "%1$ld");
        assertFalse("a token reaching outside the placeholder must keep its popup",
                EditorPopups.SpellCheckerPopup.isInsideProtectedPart(new Token("errld%1$", 0, 8), ste,
                        translation));
    }

    @Test
    public void testEntryWithoutProtectedPartsProtectsNothing() {
        String translation = "Server errorr";
        SourceTextEntry ste = entryWithProtectedParts("Serverfehler");
        assertFalse(EditorPopups.SpellCheckerPopup.isInsideProtectedPart(tokenAt(translation, "errorr"),
                ste, translation));
    }
}
