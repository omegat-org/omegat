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

package org.omegat.gui.editor.sort;

import static org.junit.Assert.assertTrue;

import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Layout-cost characterization for the resize behavior with an expanded sort
 * bar. Every width change of the editor pane re-validates the north container,
 * so the expanded bar's layout must stay cheap; a pathological layout (for
 * example a preferred-size computation that grows with the number of rows and
 * resize steps) would show up here as a large expanded/collapsed ratio.
 *
 * The bar is hosted in a never-shown but displayable frame (addNotify creates
 * the peers), because Swing only lays out displayable hierarchies - validate()
 * on a peerless panel is a no-op and would measure nothing. The sweep runs on
 * the EDT like interactive resize layout does.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public class SortBarResizePerformanceTest {

    private static final int WARMUP_PASSES = 2;
    private static final int MEASURED_PASSES = 5;

    @BeforeClass
    public static void setUpBeforeClass() {
        org.junit.Assume.assumeFalse("Skipping test: headless environment",
                GraphicsEnvironment.isHeadless());
        org.junit.Assume.assumeTrue(
                "Skipping performance test: wall-clock timings are unreliable on CI runners",
                System.getenv("CI") == null && System.getenv("TF_BUILD") == null);
    }

    private long sweep(SortBar bar, boolean expanded) {
        JPanel root = new JPanel(new BorderLayout());
        root.add(bar, BorderLayout.NORTH);
        root.add(new JPanel(), BorderLayout.CENTER);
        JFrame frame = new JFrame();
        frame.setUndecorated(true);
        frame.add(root);
        frame.setSize(400, 800);
        frame.addNotify();
        frame.validate();
        bar.setExpanded(expanded);
        frame.validate();
        assertTrue("the bar must actually be laid out", bar.getWidth() > 0);
        try {
            for (int i = 0; i < WARMUP_PASSES; i++) {
                resizePass(frame);
            }
            long t0 = System.nanoTime();
            for (int i = 0; i < MEASURED_PASSES; i++) {
                resizePass(frame);
            }
            return (System.nanoTime() - t0) / 1_000_000;
        } finally {
            frame.dispose();
        }
    }

    /** One pixel-wise resize sweep, as a user dragging the window edge causes. */
    private void resizePass(JFrame frame) {
        for (int w = 400; w <= 1600; w += 4) {
            frame.setSize(w, 800);
            frame.validate();
        }
    }

    @Test
    public void expandedLayoutStaysCheapDuringResize() throws Exception {
        long[] ms = new long[2];
        SwingUtilities.invokeAndWait(() -> {
            SortBar bar = new SortBar();
            bar.selectKey(0, SortKey.SOURCE_ALPHA);
            bar.addRow();
            bar.addRow();

            ms[0] = sweep(bar, false);
            ms[1] = sweep(bar, true);
        });
        long collapsedMs = ms[0];
        long expandedMs = ms[1];

        System.out.println(String.format(Locale.ROOT,
                "SortBar resize sweep (301 widths x %d passes): collapsed=%d ms, expanded=%d ms",
                MEASURED_PASSES, collapsedMs, expandedMs));

        assertTrue("expanded sort bar layout must stay cheap during resize (collapsed=" + collapsedMs
                + " ms, expanded=" + expandedMs + " ms)",
                expandedMs <= Math.max(150, collapsedMs * 5));
    }
}
