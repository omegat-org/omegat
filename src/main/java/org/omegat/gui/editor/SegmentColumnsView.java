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

import java.awt.Rectangle;

import javax.swing.SizeRequirements;
import javax.swing.text.BoxView;
import javax.swing.text.Element;
import javax.swing.text.View;

import org.jspecify.annotations.Nullable;

import org.omegat.gui.editor.SegmentMetadataGutter.Column;
import org.omegat.util.Preferences;

/**
 * Section view of the editor document that can lay the source and
 * translation parts of the inactive segments out side by side (SF feature
 * request 1028). While the classic stacked layout is on it behaves like the
 * plain vertical box it replaces, so the default look matches the previous
 * versions; the layout mode is read again on every pass, so switching only
 * needs a relayout, not a rebuilt document.
 *
 * The active segment always keeps the stacked layout: it stays the editing
 * field known from the classic view.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
final class SegmentColumnsView extends BoxView {

    /** The role of a child paragraph in the segment columns layout. */
    private enum Role {
        /** A paragraph spanning the full width, e.g. of the active segment. */
        FULL,
        /** A paragraph of the source part of an inactive segment. */
        SOURCE,
        /** A paragraph of the translation part of an inactive segment. */
        TARGET
    }

    /** The gap between the two text cells; the boundary drags in it. */
    static final int CELL_GAP = 8;

    /** No text cell gets narrower than this, keeping both cells usable. */
    static final int MIN_CELL_WIDTH = 40;

    private Role[] roles = new Role[0];
    private SegmentBuilder @Nullable [] rowBuilders;

    /** The roles stay valid until the document or the layout prefs change. */
    private boolean rolesValid;

    /**
     * The left cell column, cached with the roles: reading it per child and
     * pass would parse the order preference over and over.
     */
    private Column leftCellCache = Column.SOURCE_TEXT;

    /** Geometry of the last columns layout, for the boundary drag. */
    private int leftCellX;
    private int leftCellWidth;
    private int rightCellWidth;
    private boolean columnsLaidOut;

    SegmentColumnsView(Element elem) {
        super(elem, View.Y_AXIS);
    }

    /** The special layouts this view adds to the plain vertical box. */
    private enum Mode {
        /** The classic layout, handled entirely by the box superclass. */
        PLAIN,
        /** Source and translation side by side. */
        COLUMNS,
        /** Stacked, but with the translation above the source. */
        STACKED_SWAPPED
    }

    private Mode mode() {
        if (!(getDocument() instanceof Document3)
                || ((Document3) getDocument()).getController().m_docSegList == null) {
            return Mode.PLAIN;
        }
        if (!Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER)) {
            // The customize toggle is the master switch: off means the pure
            // default rendering, whatever the stored layout says.
            return Mode.PLAIN;
        }
        if (!Preferences.isPreferenceDefault(Preferences.EDITOR_LAYOUT_STACKED, true)) {
            return Mode.COLUMNS;
        }
        return leftCell() == Column.TARGET_TEXT ? Mode.STACKED_SWAPPED : Mode.PLAIN;
    }

    /** The side-by-side layout applies: chosen, and a document to lay out. */
    private boolean columnsActive() {
        return mode() == Mode.COLUMNS;
    }

    /**
     * Invalidates the cached layout, so a changed layout preference (the
     * pair order, the widths, stacked vs. side by side) takes effect on the
     * next paint.
     */
    void relayout() {
        rolesValid = false;
        layoutChanged(X_AXIS);
        layoutChanged(Y_AXIS);
        preferenceChanged(null, true, true);
    }

    @Override
    public void replace(int offset, int length, View[] views) {
        // Covers every structural change, including the document update
        // paths: a plain text edit keeps the child paragraphs and their
        // roles, so it must not trigger a role sweep per keystroke.
        rolesValid = false;
        super.replace(offset, length, views);
    }

    /**
     * The x of the draggable boundary between the two text cells in view
     * coordinates, or -1 while the stacked layout is on.
     */
    int boundaryX() {
        return columnsLaidOut ? leftCellX + leftCellWidth + CELL_GAP / 2 : -1;
    }

    /** The current width of the left text cell, for the drag hint. */
    int currentLeftCellWidth() {
        return leftCellWidth;
    }

    /** The smallest and largest share of a text cell, in percent. */
    static final int MIN_CELL_PERCENT = 25;
    static final int MAX_CELL_PERCENT = 75;

    /**
     * Moves the cell boundary to the given x and persists the new shares of
     * the two coupled percentages. Returns the new width of the left cell,
     * -1 without the columns layout.
     */
    int dragBoundaryTo(int x) {
        if (!columnsLaidOut) {
            return -1;
        }
        int available = leftCellWidth + rightCellWidth;
        int percent = Math.max(MIN_CELL_PERCENT, Math.min(MAX_CELL_PERCENT,
                Math.round((x - leftCellX - CELL_GAP / 2f) * 100f / Math.max(1, available))));
        Column left = leftCell();
        Column right = left == Column.SOURCE_TEXT ? Column.TARGET_TEXT : Column.SOURCE_TEXT;
        Preferences.setPreference(left.getFillWeightKey(), percent);
        Preferences.setPreference(right.getFillWeightKey(), 100 - percent);
        return available * percent / 100;
    }

    /** The text cell shown on the left, the pair order of the settings. */
    static Column leftCell() {
        java.util.List<Column> order = Column.inDisplayOrder();
        return order.indexOf(Column.TARGET_TEXT) < order.indexOf(Column.SOURCE_TEXT)
                ? Column.TARGET_TEXT : Column.SOURCE_TEXT;
    }

    /**
     * The persisted share of the cell, clamped to the percent range. A
     * weight pair not summing to one hundred (e.g. the pixel weights of an
     * earlier build) is normalized first, so the two shares always
     * complement each other.
     */
    static int cellPercent(Column column) {
        Column partner = column == Column.SOURCE_TEXT ? Column.TARGET_TEXT : Column.SOURCE_TEXT;
        int own = Preferences.getPreferenceDefault(column.getFillWeightKey(), 50);
        int other = Preferences.getPreferenceDefault(partner.getFillWeightKey(), 50);
        if (own + other != 100) {
            own = Math.round(own * 100f / Math.max(1, own + other));
        }
        return Math.max(MIN_CELL_PERCENT, Math.min(MAX_CELL_PERCENT, own));
    }

    /** Recomputes the cell geometry for the given total width. */
    private void computeCells(int targetSpan) {
        int available = Math.max(2 * MIN_CELL_WIDTH, targetSpan - CELL_GAP);
        int leftWidth = Math.round(available * cellPercent(leftCellCache) / 100f);
        leftCellX = 0;
        leftCellWidth = Math.max(MIN_CELL_WIDTH,
                Math.min(available - MIN_CELL_WIDTH, leftWidth));
        rightCellWidth = available - leftCellWidth;
    }

    /**
     * Recomputes the role of every child paragraph from the segment list
     * when the cached mapping expired. It works on document offsets, so it
     * follows lazy loading and filtering like the metadata gutter does.
     */
    private void computeRoles() {
        if (rolesValid && roles.length == getViewCount()) {
            return;
        }
        EditorRenderStats.count("roleSweeps", 1);
        rolesValid = true;
        leftCellCache = leftCell();
        int count = getViewCount();
        roles = new Role[count];
        rowBuilders = new SegmentBuilder[count];
        SegmentBuilder[] builders = ((Document3) getDocument()).getController().m_docSegList;
        // One merged sweep: the children and the created segments are both
        // in document order, so a single pair of cursors maps every child.
        // A search per child would degenerate with lazily loaded segments.
        int next = 0;
        for (int i = 0; i < count; i++) {
            roles[i] = Role.FULL;
            int offset = getView(i).getElement().getStartOffset();
            while (next < builders.length && (!builders[next].hasBeenCreated()
                    || builders[next].getEndPosition() <= offset)) {
                next++;
            }
            if (next >= builders.length) {
                continue;
            }
            SegmentBuilder builder = builders[next];
            if (!builder.isInsideSegment(offset) || builder.isActive()) {
                continue;
            }
            // A part can stand alone, e.g. the source of an untranslated
            // segment: it still keeps to its cell, the other cell is empty.
            int sourceStart = builder.getSourceText() == null ? -1
                    : builder.getStartSourcePosition();
            int translationStart = builder.getTranslationText() == null ? -1
                    : builder.getStartTranslationPosition();
            // The direction embedding of a part may sit one position before
            // its recorded start, at the start of its first paragraph.
            if (translationStart >= 0 && offset >= translationStart - 1) {
                roles[i] = Role.TARGET;
                rowBuilders[i] = builder;
            } else if (sourceStart >= 0 && offset >= sourceStart - 1) {
                roles[i] = Role.SOURCE;
                rowBuilders[i] = builder;
            }
        }
    }

    /**
     * Picks the child under the point. The regrouped rows break the
     * monotonic child order the box search of the superclass expects: the
     * two cells of a segment share their y range and only differ in x, and
     * the swapped stack reorders the y ranges. So the pick goes by the
     * nearest child box, vertical distance first, x breaking the tie.
     */
    @Override
    protected View getViewAtPoint(int x, int y, Rectangle alloc) {
        int count = getViewCount();
        if (mode() == Mode.PLAIN || count == 0 || roles.length != count) {
            return super.getViewAtPoint(x, y, alloc);
        }
        int px = x - alloc.x;
        int py = y - alloc.y;
        int best = 0;
        long bestDistance = Long.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            long dy = axisDistance(py, getOffset(Y_AXIS, i), getSpan(Y_AXIS, i));
            long dx = axisDistance(px, getOffset(X_AXIS, i), getSpan(X_AXIS, i));
            long distance = (dy << 16) + Math.min(dx, (1 << 16) - 1);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = i;
            }
        }
        childAllocation(best, alloc);
        return getView(best);
    }

    /** The distance of p to the span starting at offset, zero inside it. */
    private static long axisDistance(int p, int offset, int span) {
        if (p < offset) {
            return offset - p;
        }
        if (p >= offset + span) {
            return p - offset - span + 1;
        }
        return 0;
    }

    /** The x offset and span of a child in the columns layout, or null. */
    private int @Nullable [] cellOf(int index) {
        if (roles.length <= index || roles[index] == Role.FULL) {
            return null;
        }
        boolean leftIsSource = leftCellCache == Column.SOURCE_TEXT;
        boolean isSource = roles[index] == Role.SOURCE;
        if (isSource == leftIsSource) {
            return new int[] { leftCellX, leftCellWidth };
        }
        return new int[] { leftCellX + leftCellWidth + CELL_GAP, rightCellWidth };
    }

    @Override
    protected void layoutMinorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        super.layoutMinorAxis(targetSpan, axis, offsets, spans);
        if (!columnsActive()) {
            columnsLaidOut = false;
            return;
        }
        computeRoles();
        computeCells(targetSpan);
        for (int i = 0; i < offsets.length && i < roles.length; i++) {
            int[] cell = cellOf(i);
            if (cell != null) {
                offsets[i] = cell[0];
                spans[i] = cell[1];
            }
        }
        columnsLaidOut = true;
    }

    @Override
    protected void layoutMajorAxis(int targetSpan, int axis, int[] offsets, int[] spans) {
        super.layoutMajorAxis(targetSpan, axis, offsets, spans);
        Mode mode = mode();
        if (mode == Mode.PLAIN) {
            return;
        }
        if (mode == Mode.STACKED_SWAPPED) {
            computeRoles();
        }
        if (roles.length != getViewCount()) {
            return;
        }
        if (mode == Mode.STACKED_SWAPPED) {
            layoutSwappedMajorAxis(offsets, spans);
            return;
        }
        // Regroup vertically: the source and translation paragraphs of one
        // segment stack in their own cells and share the row, whose height
        // is the taller of the two.
        int y = 0;
        int i = 0;
        int count = getViewCount();
        while (i < count) {
            if (roles[i] == Role.FULL || rowBuilders == null || rowBuilders[i] == null) {
                offsets[i] = y;
                y += spans[i];
                i++;
                continue;
            }
            SegmentBuilder builder = rowBuilders[i];
            int sourceHeight = 0;
            int targetHeight = 0;
            int j = i;
            for (; j < count && rowBuilders[j] == builder && roles[j] != Role.FULL; j++) {
                View child = getView(j);
                int[] cell = cellOf(j);
                if (cell != null) {
                    // The height depends on the wrap at the cell width.
                    child.setSize(cell[1], 0);
                }
                int height = (int) Math.ceil(child.getPreferredSpan(Y_AXIS));
                spans[j] = height;
                if (roles[j] == Role.SOURCE) {
                    offsets[j] = y + sourceHeight;
                    sourceHeight += height;
                } else {
                    offsets[j] = y + targetHeight;
                    targetHeight += height;
                }
            }
            y += Math.max(sourceHeight, targetHeight);
            i = j;
        }
    }

    /**
     * The stacked layout with the swapped pair order: within an inactive
     * segment the translation paragraphs move above the source paragraphs,
     * both at full width. The heights stay as measured, only the vertical
     * order changes, so the total height matches the plain layout.
     */
    private void layoutSwappedMajorAxis(int[] offsets, int[] spans) {
        int y = 0;
        int i = 0;
        int count = getViewCount();
        while (i < count) {
            if (roles[i] == Role.FULL || rowBuilders == null || rowBuilders[i] == null) {
                offsets[i] = y;
                y += spans[i];
                i++;
                continue;
            }
            SegmentBuilder builder = rowBuilders[i];
            int targetHeight = 0;
            int sourceHeight = 0;
            int j = i;
            for (; j < count && rowBuilders[j] == builder && roles[j] != Role.FULL; j++) {
                if (roles[j] == Role.TARGET) {
                    targetHeight += spans[j];
                }
            }
            int targetY = y;
            int sourceY = y + targetHeight;
            for (int k = i; k < j; k++) {
                if (roles[k] == Role.TARGET) {
                    offsets[k] = targetY;
                    targetY += spans[k];
                } else {
                    offsets[k] = sourceY;
                    sourceY += spans[k];
                    sourceHeight += spans[k];
                }
            }
            y += targetHeight + sourceHeight;
            i = j;
        }
    }

    @Override
    protected SizeRequirements calculateMajorAxisRequirements(int axis, SizeRequirements r) {
        SizeRequirements requirements = super.calculateMajorAxisRequirements(axis, r);
        if (!columnsActive()) {
            return requirements;
        }
        computeRoles();
        // The vertical need shrinks against the plain sum: the two cells of
        // a segment share their row.
        long preferred = 0;
        int count = getViewCount();
        int i = 0;
        while (i < count) {
            if (roles[i] == Role.FULL || rowBuilders == null || rowBuilders[i] == null) {
                preferred += (long) getView(i).getPreferredSpan(Y_AXIS);
                i++;
                continue;
            }
            SegmentBuilder builder = rowBuilders[i];
            long sourceHeight = 0;
            long targetHeight = 0;
            for (; i < count && rowBuilders[i] == builder && roles[i] != Role.FULL; i++) {
                long height = (long) getView(i).getPreferredSpan(Y_AXIS);
                if (roles[i] == Role.SOURCE) {
                    sourceHeight += height;
                } else {
                    targetHeight += height;
                }
            }
            preferred += Math.max(sourceHeight, targetHeight);
        }
        requirements.preferred = (int) Math.min(preferred, Integer.MAX_VALUE);
        requirements.minimum = Math.min(requirements.minimum, requirements.preferred);
        return requirements;
    }
}
