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
package org.omegat.core.machinetranslators;

import org.junit.Test;
import org.omegat.gui.exttrans.IMTGlossarySupplier;
import org.omegat.gui.exttrans.IMachineTranslation;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * MachineTranslatorsManagerTest
 * <p>
 * Test class for the MachineTranslatorsManager class. This class focuses on
 * testing the setGlossaryMap method, ensuring correct integration of the
 * IMTGlossarySupplier with the registered IMachineTranslation instances.
 */
public class MachineTranslatorsManagerTest {

    /**
     * Verifies that the setGlossaryMap method sets the supplied
     * IMTGlossarySupplier to all registered machine translation instances.
     */
    @Test
    public void testSetGlossaryMap_ValidGlossarySupplier() {
        // Arrange
        IMachineTranslation mockTranslator1 = mock(IMachineTranslation.class);
        IMachineTranslation mockTranslator2 = mock(IMachineTranslation.class);

        MachineTranslatorsManager manager = new MachineTranslatorsManager() {
            @Override
            public List<IMachineTranslation> getMachineTranslators() {
                return List.of(mockTranslator1, mockTranslator2);
            }
        };

        IMTGlossarySupplier mockGlossarySupplier = mock(IMTGlossarySupplier.class);

        // Act
        manager.setGlossaryMap(mockGlossarySupplier);

        // Assert
        verify(mockTranslator1, times(1)).setGlossarySupplier(mockGlossarySupplier);
        verify(mockTranslator2, times(1)).setGlossarySupplier(mockGlossarySupplier);
    }

    /**
     * Verifies that the setGlossaryMap method does not throw any exceptions
     * when the TRANSLATORS list is empty.
     */
    @Test
    public void testSetGlossaryMap_NoTranslators() {
        // Arrange
        MachineTranslatorsManager manager = new MachineTranslatorsManager() {
            @Override
            public List<IMachineTranslation> getMachineTranslators() {
                return List.of(); // No translators
            }
        };

        IMTGlossarySupplier mockGlossarySupplier = mock(IMTGlossarySupplier.class);

        // Act & Assert does not throw an exception
        manager.setGlossaryMap(mockGlossarySupplier);
    }

    /**
     * Verifies that setGlossaryMap handles a null IMTGlossarySupplier
     * appropriately by setting null for all translators.
     */
    @Test
    public void testSetGlossaryMap_NullGlossarySupplier() {
        // Arrange
        IMachineTranslation mockTranslator1 = mock(IMachineTranslation.class);
        IMachineTranslation mockTranslator2 = mock(IMachineTranslation.class);

        MachineTranslatorsManager manager = new MachineTranslatorsManager() {
            @Override
            public List<IMachineTranslation> getMachineTranslators() {
                return List.of(mockTranslator1, mockTranslator2);
            }
        };

        // Act
        manager.setGlossaryMap(null);

        // Assert
        verify(mockTranslator1, times(1)).setGlossarySupplier(null);
        verify(mockTranslator2, times(1)).setGlossarySupplier(null);
    }
}
