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

package org.omegat.util.gui;

import static org.junit.Assert.assertEquals;

import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.After;
import org.junit.Test;
import org.omegat.core.CoreEvents;

/**
 * Lifecycle tests for {@link Styles#bindColors}: the binding applies once at
 * bind time, listens to color changes only while the component is
 * displayable, and re-applies when the component becomes displayable again.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class StylesColorBindingTest {

    /** Test component whose displayability is controlled by the test. */
    @SuppressWarnings("serial")
    private static class FakePane extends JPanel {
        private boolean displayable;

        @Override
        public boolean isDisplayable() {
            return displayable;
        }
    }

    private final List<FakePane> boundPanes = new ArrayList<>();

    @After
    public void tearDown() {
        // Unbind even when an assertion failed mid-test, so no listener stays
        // registered with the global CoreEvents list for later tests.
        for (FakePane pane : boundPanes) {
            pane.displayable = false;
            dispatchDisplayabilityChange(pane);
        }
    }

    private FakePane bindNewPane(AtomicInteger applied, boolean displayable) {
        FakePane pane = new FakePane();
        pane.displayable = displayable;
        boundPanes.add(pane);
        Styles.bindColors(pane, applied::incrementAndGet);
        return pane;
    }

    @Test
    public void testBindingFollowsDisplayability() throws Exception {
        AtomicInteger applied = new AtomicInteger();
        FakePane pane = bindNewPane(applied, false);
        // Applied once at bind time even while not displayable.
        assertEquals(1, applied.get());

        fireColorsChangedAndWait();
        // Not displayable: the binding must not be registered.
        assertEquals(1, applied.get());

        pane.displayable = true;
        dispatchDisplayabilityChange(pane);
        // Re-applied on registration because the palette may have changed
        // while unbound.
        assertEquals(2, applied.get());

        fireColorsChangedAndWait();
        assertEquals(3, applied.get());

        pane.displayable = false;
        dispatchDisplayabilityChange(pane);
        fireColorsChangedAndWait();
        // Unregistered again: the color change must not reach the applier.
        assertEquals(3, applied.get());
    }

    @Test
    public void testBindWhileDisplayableAppliesOnce() throws Exception {
        AtomicInteger applied = new AtomicInteger();
        FakePane pane = bindNewPane(applied, true);
        assertEquals(1, applied.get());

        fireColorsChangedAndWait();
        assertEquals(2, applied.get());

        // Unbinding must unregister: a later color change stays unseen.
        pane.displayable = false;
        dispatchDisplayabilityChange(pane);
        fireColorsChangedAndWait();
        assertEquals(2, applied.get());
    }

    private static void dispatchDisplayabilityChange(FakePane pane) {
        HierarchyEvent event = new HierarchyEvent(pane, HierarchyEvent.HIERARCHY_CHANGED, pane, null,
                HierarchyEvent.DISPLAYABILITY_CHANGED);
        for (HierarchyListener listener : pane.getHierarchyListeners()) {
            listener.hierarchyChanged(event);
        }
    }

    private static void fireColorsChangedAndWait() throws Exception {
        CoreEvents.fireColorsChanged();
        // The event is delivered via invokeLater; flush the EDT queue.
        SwingUtilities.invokeAndWait(() -> {
        });
    }
}
