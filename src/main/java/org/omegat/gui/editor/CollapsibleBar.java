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

import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

/**
 * Base class for the stacked, collapsible control bars shown in the editor's
 * north container (currently the sort bar, and later the filter bar).
 *
 * A collapsible bar has a permanently visible header row - a toggle arrow plus
 * a one-line summary label - and a body that holds the expanded controls. The
 * body is hidden by default, so the bar costs only a single line of vertical
 * space until the user opens it. Clicking anywhere on the header toggles
 * between collapsed and expanded.
 *
 * Subclasses put their expanded controls into {@link #getBody()} and supply the
 * collapsed summary text through {@link #buildSummary()}. The summary is meant
 * to be assembled from the same localized building blocks the expanded controls
 * already use, so that collapsing introduces no new translatable strings.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
public abstract class CollapsibleBar extends JPanel {

    /** Triangle pointing down: the bar is expanded. */
    private static final String ARROW_EXPANDED = "▾";
    /** Triangle pointing right: the bar is collapsed. */
    private static final String ARROW_COLLAPSED = "▸";

    private final JLabel arrow = new JLabel(ARROW_COLLAPSED);
    private final JLabel summary = new JLabel();
    private final JPanel body = new JPanel();
    private boolean expanded;

    protected CollapsibleBar() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        Border border = UIManager.getBorder("OmegaTEditorFilter.border");
        if (border != null) {
            setBorder(border);
        }

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.LINE_AXIS));
        header.add(arrow);
        header.add(Box.createHorizontalStrut(4));
        header.add(summary);
        header.add(Box.createHorizontalGlue());
        header.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        MouseAdapter toggleOnClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggle();
            }
        };
        header.addMouseListener(toggleOnClick);
        arrow.addMouseListener(toggleOnClick);
        summary.addMouseListener(toggleOnClick);

        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));

        add(header);
        add(body);

        // Start collapsed. buildSummary() is intentionally NOT called from this
        // constructor: subclass fields are not initialized yet at this point, so
        // subclasses call refreshSummary() themselves once their state is built.
        applyExpandedState(false);
    }

    /** The container a subclass fills with its expanded controls (rows). */
    protected final JPanel getBody() {
        return body;
    }

    /**
     * The one-line text shown next to the arrow while collapsed, assembled from
     * the subclass's already-localized building blocks. Invoked on every
     * {@link #refreshSummary()} call and never from the constructor.
     */
    protected abstract String buildSummary();

    /**
     * Recompute the summary label from the current model. Subclasses call this
     * after their model changes (criterion/condition added, removed, edited).
     */
    protected final void refreshSummary() {
        summary.setText(buildSummary());
        revalidate();
        repaint();
    }

    public final boolean isExpanded() {
        return expanded;
    }

    public final void setExpanded(boolean expand) {
        applyExpandedState(expand);
    }

    public final void toggle() {
        applyExpandedState(!expanded);
    }

    private void applyExpandedState(boolean expand) {
        expanded = expand;
        body.setVisible(expand);
        arrow.setText(expand ? ARROW_EXPANDED : ARROW_COLLAPSED);
        revalidate();
        repaint();
    }

    /** Test hook: the current collapsed-summary text. */
    String getSummaryText() {
        return summary.getText();
    }
}
