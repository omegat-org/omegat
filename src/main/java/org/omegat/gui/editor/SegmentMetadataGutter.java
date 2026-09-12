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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JComponent;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

import org.jspecify.annotations.Nullable;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.gui.editor.mark.BidiPainter;
import org.omegat.gui.editor.mark.SymbolPainter;
import org.omegat.core.events.IEntryEventListener;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.gui.Styles;

/**
 * Row header of the editor scroll pane that shows segment metadata: the
 * segment number, the translation state, the last author and the day of the
 * last change (SF feature request 420). Which columns appear is chosen in the
 * settings menu of the editor pane and persisted in the preferences.
 *
 * The rows are painted at the document position of each created segment, so
 * the gutter follows scrolling, lazy loading and filtering without keeping
 * any state of its own.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
final class SegmentMetadataGutter extends JComponent {

    /** The kind of per-column option shown in the configuration table. */
    enum ColumnOption {
        NONE, REGEX, DATE_FORMAT, LENGTH, STACKED, PAIR_ALIGNMENT
    }

    /** The horizontal alignment of a column, orientation-aware. */
    enum ColumnAlignment {
        LEADING, CENTER, TRAILING;

        ColumnAlignment next() {
            ColumnAlignment[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    /** Columns of the gutter, in display order. */
    enum Column {
        NUMBER(Preferences.EDITOR_METADATA_GUTTER_NUMBER, true, "GUI_EDITORWINDOW_GUTTER_COL_NUMBER",
                "00000", true),
        STATUS(Preferences.EDITOR_METADATA_GUTTER_STATUS, false, "GUI_EDITORWINDOW_GUTTER_COL_STATUS",
                SegmentMetadataFormatter.STATUS_TRANSLATED
                        + SegmentMetadataFormatter.STATUS_NON_UNIQUE_NEXT_SUFFIX
                        + SegmentMetadataFormatter.STATUS_LINKED_ENFORCED
                        + SegmentMetadataFormatter.STATUS_HAS_ORIGIN, true),
        AUTHOR(Preferences.EDITOR_METADATA_GUTTER_AUTHOR, false, "GUI_EDITORWINDOW_GUTTER_COL_AUTHOR",
                "mmmmmmmmmmmm", false, ColumnOption.REGEX,
                Preferences.EDITOR_METADATA_GUTTER_AUTHOR_REGEX),
        CREATION_AUTHOR(Preferences.EDITOR_METADATA_GUTTER_CREATION_AUTHOR, false,
                "GUI_EDITORWINDOW_GUTTER_COL_CREATION_AUTHOR", "mmmmmmmmmmmm", false,
                ColumnOption.REGEX,
                Preferences.EDITOR_METADATA_GUTTER_CREATION_AUTHOR_REGEX),
        DATE(Preferences.EDITOR_METADATA_GUTTER_DATE, false, "GUI_EDITORWINDOW_GUTTER_COL_DATE",
                "2026-08-14", false, ColumnOption.DATE_FORMAT,
                Preferences.EDITOR_METADATA_GUTTER_DATE_FORMAT),
        CREATION_DATE(Preferences.EDITOR_METADATA_GUTTER_CREATION_DATE, false,
                "GUI_EDITORWINDOW_GUTTER_COL_CREATION_DATE", "2026-08-14", false,
                ColumnOption.DATE_FORMAT,
                Preferences.EDITOR_METADATA_GUTTER_CREATION_DATE_FORMAT),
        COLOR(Preferences.EDITOR_METADATA_GUTTER_COLOR, false, "GUI_EDITORWINDOW_GUTTER_COL_COLOR",
                "", false),
        ID(Preferences.EDITOR_METADATA_GUTTER_ID, false, "GUI_EDITORWINDOW_GUTTER_COL_ID",
                "mmmmmmmm", false, ColumnOption.REGEX,
                Preferences.EDITOR_METADATA_GUTTER_ID_REGEX),
        SOURCE_LENGTH(Preferences.EDITOR_METADATA_GUTTER_SOURCE_LENGTH, false,
                "GUI_EDITORWINDOW_GUTTER_COL_SOURCE_LENGTH", "00000", true, ColumnOption.LENGTH,
                null),
        TARGET_LENGTH(Preferences.EDITOR_METADATA_GUTTER_TARGET_LENGTH, false,
                "GUI_EDITORWINDOW_GUTTER_COL_TARGET_LENGTH", "00000", true, ColumnOption.LENGTH,
                null),
        COMMENT_LENGTH(Preferences.EDITOR_METADATA_GUTTER_COMMENT_LENGTH, false,
                "GUI_EDITORWINDOW_GUTTER_COL_COMMENT_LENGTH", "00000", true, ColumnOption.LENGTH,
                null),
        NOTE_LENGTH(Preferences.EDITOR_METADATA_GUTTER_NOTE_LENGTH, false,
                "GUI_EDITORWINDOW_GUTTER_COL_NOTE_LENGTH", "00000", true, ColumnOption.LENGTH,
                null),
        ALTERNATIVE(Preferences.EDITOR_METADATA_GUTTER_ALTERNATIVE, false,
                "GUI_EDITORWINDOW_GUTTER_COL_ALTERNATIVE",
                SegmentMetadataFormatter.ALTERNATIVE_MARK, false),
        // The two pseudo columns of the editor text itself. They are rows of
        // the configuration table like the metadata columns, but the gutter
        // never paints them: they stand for the source and translation texts,
        // which the editor lays out according to their settings.
        SOURCE_TEXT(Preferences.EDITOR_LAYOUT_SOURCE_TEXT, true,
                "GUI_EDITORWINDOW_GUTTER_COL_SOURCE_TEXT", "", false, ColumnOption.STACKED, null),
        TARGET_TEXT(Preferences.EDITOR_LAYOUT_TARGET_TEXT, true,
                "GUI_EDITORWINDOW_GUTTER_COL_TARGET_TEXT", "", false,
                ColumnOption.PAIR_ALIGNMENT, null);

        private final String prefKey;
        private final boolean enabledByDefault;
        private final String labelKey;
        private final String widthSample;
        private final boolean rightAligned;
        private final ColumnOption option;
        private final @Nullable String optionOnKey;
        private final @Nullable String optionValueKey;

        Column(String prefKey, boolean enabledByDefault, String labelKey, String widthSample,
                boolean rightAligned) {
            this(prefKey, enabledByDefault, labelKey, widthSample, rightAligned, ColumnOption.NONE,
                    null);
        }

        Column(String prefKey, boolean enabledByDefault, String labelKey, String widthSample,
                boolean rightAligned, ColumnOption option, @Nullable String optionValueKey) {
            this.prefKey = prefKey;
            this.enabledByDefault = enabledByDefault;
            this.labelKey = labelKey;
            this.widthSample = widthSample;
            this.rightAligned = rightAligned;
            this.option = option;
            // The on/off preference of an option is its value key plus _on.
            this.optionOnKey = optionValueKey == null ? null : optionValueKey + "_on";
            this.optionValueKey = optionValueKey;
        }

        String getPrefKey() {
            return prefKey;
        }

        /** Preference key of the user-chosen display width, 0 = automatic. */
        String getWidthKey() {
            return prefKey + "_width";
        }

        /** Preference key of the font size at which the width was chosen. */
        String getWidthRefKey() {
            return prefKey + "_width_ref";
        }

        /** Preference key of the user-chosen alignment. */
        String getAlignmentKey() {
            return prefKey + "_align";
        }

        /** The alignment of the column values, user-chosen or the default. */
        ColumnAlignment getAlignment() {
            try {
                return ColumnAlignment.valueOf(
                        Preferences.getPreferenceDefault(getAlignmentKey(), defaultAlignment().name()));
            } catch (IllegalArgumentException ignored) {
                return defaultAlignment();
            }
        }

        ColumnAlignment defaultAlignment() {
            if (this == STATUS) {
                return ColumnAlignment.CENTER;
            }
            return rightAligned ? ColumnAlignment.TRAILING : ColumnAlignment.LEADING;
        }

        /** Preference key of the trim option of the length columns. */
        String getTrimKey() {
            return prefKey + "_trim";
        }

        /** Preference key of the non-space option of the length columns. */
        String getNonSpaceKey() {
            return prefKey + "_nonspace";
        }

        String getLabel() {
            return OStrings.getString(labelKey);
        }

        ColumnOption getOption() {
            return option;
        }

        @Nullable String getOptionOnKey() {
            return optionOnKey;
        }

        @Nullable String getOptionValueKey() {
            return optionValueKey;
        }

        /** The active option value of the column, null when switched off. */
        @Nullable String activeOptionValue() {
            if (option == ColumnOption.NONE || optionOnKey == null || optionValueKey == null
                    || !Preferences.isPreference(optionOnKey)) {
                return null;
            }
            String value = Preferences.getPreferenceDefault(optionValueKey, "");
            return value.isEmpty() ? null : value;
        }

        boolean isEnabled() {
            // The text pseudo columns have no visibility of their own.
            return isText() || Preferences.isPreferenceDefault(prefKey, enabledByDefault);
        }

        boolean isEnabledByDefault() {
            return enabledByDefault;
        }

        /** One of the two text pseudo columns, never painted by the gutter. */
        boolean isText() {
            return this == SOURCE_TEXT || this == TARGET_TEXT;
        }

        /** Preference key of the percent share of the text rows. */
        String getFillWeightKey() {
            return prefKey + "_fill_weight";
        }

        /** The parsed display order with the preference it came from. */
        private static final class DisplayOrder {
            private final String spec;
            private final List<Column> columns;

            DisplayOrder(String spec, List<Column> columns) {
                this.spec = spec;
                this.columns = columns;
            }
        }

        /** One volatile holder, so a reader never pairs spec and list from
         * different writes; in practice all access is on the EDT. */
        private static volatile @Nullable DisplayOrder displayOrderCache;

        /**
         * The columns in the user-chosen display order: the persisted order
         * first, columns unknown to the preference appended in declaration
         * order, unknown names ignored. The text pseudo columns are
         * normalized to an adjacent pair at the start or the end of the
         * list, so the metadata columns sit before or after the text, never
         * between its two parts. The parsed order is cached per preference
         * value: the painters and mouse handlers ask on every pass, and the
         * callers mutate the returned list, so each call hands out a copy.
         */
        static List<Column> inDisplayOrder() {
            String spec = Preferences
                    .getPreferenceDefault(Preferences.EDITOR_METADATA_GUTTER_ORDER, "");
            DisplayOrder cached = displayOrderCache;
            if (cached != null && spec.equals(cached.spec)) {
                return new ArrayList<>(cached.columns);
            }
            List<Column> result = buildDisplayOrder(spec);
            displayOrderCache = new DisplayOrder(spec, List.copyOf(result));
            return result;
        }

        private static List<Column> buildDisplayOrder(String spec) {
            List<Column> result = new ArrayList<>();
            for (String name : spec.split(",")) {
                try {
                    Column column = valueOf(name.trim());
                    if (!result.contains(column)) {
                        result.add(column);
                    }
                } catch (IllegalArgumentException ignored) {
                    // an outdated preference names no current column
                }
            }
            for (Column column : values()) {
                if (!result.contains(column)) {
                    result.add(column);
                }
            }
            // The text pair sits at the very start or the very end: at the
            // start when the persisted order began with a text row.
            boolean textFirst = result.get(0).isText();
            boolean targetFirst = result.indexOf(TARGET_TEXT) < result.indexOf(SOURCE_TEXT);
            result.removeIf(Column::isText);
            List<Column> pair = targetFirst ? List.of(TARGET_TEXT, SOURCE_TEXT)
                    : List.of(SOURCE_TEXT, TARGET_TEXT);
            result.addAll(textFirst ? 0 : result.size(), pair);
            return result;
        }

        /** The metadata columns in display order, without the text rows. */
        static List<Column> gutterColumns() {
            List<Column> result = inDisplayOrder();
            result.removeIf(Column::isText);
            return result;
        }

        /** Whether the metadata columns sit after (right of) the text. */
        static boolean metadataAfterText() {
            return inDisplayOrder().get(0).isText();
        }

        static void persistDisplayOrder(List<Column> order) {
            StringBuilder names = new StringBuilder();
            for (Column column : order) {
                if (names.length() > 0) {
                    names.append(",");
                }
                names.append(column.name());
            }
            Preferences.setPreference(Preferences.EDITOR_METADATA_GUTTER_ORDER, names.toString());
        }
    }

    private static final int COLUMN_GAP = 2;
    private static final int LEADING_INSET = 4;
    private static final int MAX_EXTRA_COLORS = 3;

    /**
     * A drag tick lays the text out at most this often (in milliseconds):
     * every width change reflows the whole text, which must not pile up per
     * mouse event.
     */
    private static final int DRAG_LAYOUT_INTERVAL = 60;

    private final transient EditorController controller;
    private final EditorTextArea3 editor;

    /**
     * A document change repaints only the gutter: the metadata of the rows
     * (e.g. the live target length) may change with any edit. The editor
     * needs nothing extra, because an edit within the line moves no
     * decoration, and an edit that rebreaks the text makes the paragraph
     * layout repaint the editor by itself.
     */
    private final transient DocumentListener repaintOnChange = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            EditorRenderStats.documentChanged();
            repaint();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            EditorRenderStats.documentChanged();
            repaint();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            // Attribute changes are not typing: they stay out of the
            // type-to-paint latency probe.
            repaint();
        }
    };

    /** The notes variant: it repaints the note length of the active row,
     * but must not feed the typing latency probe of the editor. */
    private final transient DocumentListener repaintOnNoteChange = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            repaint();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            repaint();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            repaint();
        }
    };

    /**
     * Repaints the gutter and, while grid lines or alternating backgrounds
     * extend into the editor, the editor as well: used for toggles and
     * segment activation, where the decorations move without a matching
     * text repaint.
     */
    private void repaintWithEditorShare() {
        repaint();
        if (Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER)
                && (Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER_GRID)
                        || Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER_ZEBRA))) {
            editor.repaint();
        }
    }

    /**
     * Repaint on a marker highlight change (spell checker, language checker,
     * ...). Only the COLOR column mirrors the marker highlights, so rows
     * without it need no repaint.
     */
    void marksChanged() {
        if (Column.COLOR.isEnabled()) {
            repaint();
        }
    }

    SegmentMetadataGutter(EditorController controller, EditorTextArea3 editor) {
        this.controller = controller;
        this.editor = editor;
        setOpaque(true);
        ToolTipManager.sharedInstance().registerComponent(this);
        editor.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                revalidate();
                repaint();
            }
        });
        // Metadata can change without a height change, e.g. when identical
        // segments turn translated; the document events cover those repaints.
        editor.getDocument().addDocumentListener(repaintOnChange);
        editor.addPropertyChangeListener("document", e -> {
            if (e.getOldValue() instanceof Document) {
                ((Document) e.getOldValue()).removeDocumentListener(repaintOnChange);
            }
            if (e.getNewValue() instanceof Document) {
                ((Document) e.getNewValue()).addDocumentListener(repaintOnChange);
            }
        });
        CoreEvents.registerApplicationEventListener(new IApplicationEventListener() {
            @Override
            public void onApplicationStartup() {
                // Typing a note updates the note length of the active row.
                if (Core.getNotes() instanceof JTextComponent) {
                    ((JTextComponent) Core.getNotes()).getDocument()
                            .addDocumentListener(repaintOnNoteChange);
                }
            }

            @Override
            public void onApplicationShutdown() {
            }
        });
        CoreEvents.registerFontChangedEventListener(newFont -> {
            revalidate();
            repaint();
        });
        CoreEvents.registerColorsChangedEventListener(this::repaint);
        CoreEvents.registerProjectChangeListener(eventType -> {
            revalidate();
            repaint();
        });
        CoreEvents.registerEntryEventListener(new IEntryEventListener() {
            @Override
            public void onNewFile(String activeFileName) {
                revalidate();
                repaint();
            }

            @Override
            public void onEntryActivated(SourceTextEntry newEntry) {
                repaintWithEditorShare();
            }
        });
        installColumnResizer();
    }

    /**
     * Lets the mouse drag the column boundaries: near a boundary the cursor
     * becomes a resize handle, dragging sets the width of the column left of
     * it, live and coupled to the width fields of the configuration dialog.
     * The boundaries work whether or not the grid lines are shown.
     */
    private void installColumnResizer() {
        java.awt.event.MouseAdapter resizer = new java.awt.event.MouseAdapter() {
            private @Nullable Column dragged;
            private int originalWidth;
            private long lastDragLayout;

            @Override
            public void mouseMoved(MouseEvent e) {
                setCursor(java.awt.Cursor.getPredefinedCursor(boundaryAt(e.getX()) != null
                        ? java.awt.Cursor.E_RESIZE_CURSOR : java.awt.Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!javax.swing.SwingUtilities.isLeftMouseButton(e) || e.isPopupTrigger()) {
                    // A right click near a boundary is not a resize.
                    return;
                }
                dragged = boundaryAt(e.getX());
                if (dragged != null) {
                    originalWidth = currentColumnWidth(dragged);
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Column column = dragged;
                if (column != null) {
                    // A changed gutter width reflows the whole text, so the
                    // drag ticks apply it throttled; the release finishes.
                    long now = System.currentTimeMillis();
                    if (now - lastDragLayout >= DRAG_LAYOUT_INTERVAL) {
                        lastDragLayout = now;
                        // The gutter and the editor share their view y.
                        keepLineAnchored(editor, e.getY(),
                                () -> resizeColumnTo(column, e.getX()));
                    }
                    showDragHint(e, dragHintText(column.getLabel(), originalWidth,
                            currentColumnWidth(column)));
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Column column = dragged;
                dragged = null;
                hideDragHint();
                if (column != null) {
                    // The throttle may have skipped the last ticks.
                    keepLineAnchored(editor, e.getY(),
                            () -> resizeColumnTo(column, e.getX()));
                }
            }
        };
        addMouseListener(resizer);
        addMouseMotionListener(resizer);
    }

    /**
     * Runs a width change and keeps the text line at the given view y where
     * it is, so the content jumps less while dragging: the line nearest the
     * cursor stays fixed instead of the last active segment.
     */
    static void keepLineAnchored(EditorTextArea3 editor, int viewY, Runnable change) {
        int anchorOffset = editor.viewToModel2D(new Point(0, viewY));
        double oldY = -1;
        try {
            Rectangle2D rect = editor.modelToView2D(anchorOffset);
            oldY = rect == null ? -1 : rect.getY();
        } catch (BadLocationException ignored) {
            // no anchor without a laid out line
        }
        change.run();
        double anchorY = oldY;
        if (anchorY < 0 || !(editor.getParent() instanceof javax.swing.JViewport)) {
            return;
        }
        javax.swing.JViewport viewport = (javax.swing.JViewport) editor.getParent();
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                javax.swing.RepaintManager.currentManager(editor).validateInvalidComponents();
                Rectangle2D rect = editor.modelToView2D(anchorOffset);
                if (rect == null) {
                    return;
                }
                int delta = (int) Math.round(rect.getY() - anchorY);
                if (delta != 0) {
                    Point position = viewport.getViewPosition();
                    viewport.setViewPosition(
                            new Point(position.x, Math.max(0, position.y + delta)));
                }
            } catch (BadLocationException ignored) {
                // the anchor line disappeared, nothing to fix
            }
        });
    }

    /** The floating hint beside the cursor during a boundary drag. */
    private static javax.swing.@Nullable JWindow dragHint;
    private static javax.swing.@Nullable JLabel dragHintLabel;

    /** Shows the drag hint beside the mouse, tooltip-coloured. */
    static void showDragHint(MouseEvent event, String text) {
        javax.swing.JLabel label = dragHintLabel;
        javax.swing.JWindow hint = dragHint;
        if (hint == null || label == null) {
            label = new javax.swing.JLabel();
            label.setOpaque(true);
            label.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                    javax.swing.BorderFactory.createLineBorder(
                            javax.swing.UIManager.getColor("ToolTip.foreground")),
                    javax.swing.BorderFactory.createEmptyBorder(2, 6, 2, 6)));
            hint = new javax.swing.JWindow();
            hint.setFocusableWindowState(false);
            hint.setType(java.awt.Window.Type.POPUP);
            hint.getContentPane().add(label);
            dragHintLabel = label;
            dragHint = hint;
        }
        label.setBackground(javax.swing.UIManager.getColor("ToolTip.background"));
        label.setForeground(javax.swing.UIManager.getColor("ToolTip.foreground"));
        label.setText(text);
        hint.pack();
        Point screen = event.getLocationOnScreen();
        hint.setLocation(screen.x + 14, screen.y + 18);
        hint.setVisible(true);
    }

    static void hideDragHint() {
        if (dragHint != null) {
            dragHint.setVisible(false);
        }
    }

    /** The drag hint wording: name, original and new width, difference. */
    static String dragHintText(String label, int originalWidth, int newWidth) {
        int difference = newWidth - originalWidth;
        return label + ": " + originalWidth + " px → " + newWidth + " px ("
                + (difference >= 0 ? "+" : "−") + Math.abs(difference) + " px)";
    }

    /** The column whose right boundary lies at the given x, or null. */
    private @Nullable Column boundaryAt(int eventX) {
        SegmentBuilder[] builders = builders();
        if (builders == null) {
            return null;
        }
        FontMetrics fm = getFontMetrics(getRowFont(true));
        int x = LEADING_INSET;
        for (Column column : Column.gutterColumns()) {
            if (!column.isEnabled()) {
                continue;
            }
            x += columnWidth(column, builders, fm) + COLUMN_GAP;
            if (Math.abs(eventX - (x - COLUMN_GAP / 2 - 1)) <= 4) {
                return column;
            }
        }
        return null;
    }

    /** Sets the width of the column so its right boundary follows the drag. */
    private void resizeColumnTo(Column column, int eventX) {
        SegmentBuilder[] builders = builders();
        if (builders == null) {
            return;
        }
        FontMetrics fm = getFontMetrics(getRowFont(true));
        int start = LEADING_INSET;
        for (Column other : Column.gutterColumns()) {
            if (other == column || !other.isEnabled()) {
                if (other == column) {
                    break;
                }
                continue;
            }
            start += columnWidth(other, builders, fm) + COLUMN_GAP;
        }
        // Clamped to the slider range, so the drag and the dialog agree.
        int width = Math.max(SegmentMetadataConfigDialog.WidthSliderPanel.MIN_WIDTH,
                Math.min(SegmentMetadataConfigDialog.WidthSliderPanel.MAX_WIDTH,
                        eventX - start + COLUMN_GAP / 2));
        Preferences.setPreference(column.getWidthKey(), width);
        Preferences.setPreference(column.getWidthRefKey(), editor.getFont().getSize());
        revalidate();
        repaint();
        SegmentMetadataConfigDialog.refreshOpenDialog();
    }

    @Override
    public Dimension getPreferredSize() {
        SegmentBuilder[] builders = builders();
        if (builders == null) {
            return new Dimension(0, 0);
        }
        FontMetrics fm = getFontMetrics(getRowFont(true));
        int width = LEADING_INSET;
        for (Column column : Column.gutterColumns()) {
            if (column.isEnabled()) {
                width += columnWidth(column, builders, fm) + COLUMN_GAP;
            }
        }
        return new Dimension(width, editor.getHeight());
    }

    @Override
    protected void paintComponent(Graphics g) {
        long renderToken = EditorRenderStats.begin();
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(editor.getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        SegmentBuilder[] builders = builders();
        if (builders == null) {
            return;
        }
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        Font rowFont = getRowFont(false);
        Font activeFont = getRowFont(true);
        FontMetrics fm = getFontMetrics(activeFont);
        Color muted = mix(editor.getForeground(), editor.getBackground());
        int clipTop = g.getClipBounds().y;
        int clipBottom = clipTop + g.getClipBounds().height;
        // Compare document offsets to find the visible rows: they are cheap,
        // while modelToView2D is only affordable for the rows actually drawn.
        // The bottom extends by one line so the grid line of a segment just
        // below the repainted strip is not skipped while scrolling.
        int clipTopOffset = editor.viewToModel2D(new Point(0, clipTop));
        int clipBottomOffset = editor.viewToModel2D(
                new Point(0, clipBottom + getFontMetrics(editor.getFont()).getHeight()));

        boolean zebra = Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER_ZEBRA);
        boolean grid = Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER_GRID);
        Color zebraColor = zebraColor(editor.getForeground(), editor.getBackground());
        Color gridColor = gridColor(editor.getForeground(), editor.getBackground());

        // The enabled columns and their widths are loop invariants; resolving
        // them per row multiplies preference lookups into every paint.
        List<Column> enabledColumns = new ArrayList<>();
        for (Column column : Column.gutterColumns()) {
            if (column.isEnabled()) {
                enabledColumns.add(column);
            }
        }
        int[] columnWidths = new int[enabledColumns.size()];
        for (int c = 0; c < columnWidths.length; c++) {
            columnWidths[c] = columnWidth(enabledColumns.get(c), builders, fm);
        }

        for (int i = firstRowIndex(builders, clipTopOffset); i < builders.length; i++) {
            SegmentBuilder builder = builders[i];
            if (!builder.hasBeenCreated()) {
                continue;
            }
            if (builder.getStartPosition() > clipBottomOffset) {
                break;
            }
            Rectangle2D rect;
            try {
                rect = segmentTopRect(editor, builder);
            } catch (BadLocationException ex) {
                continue;
            }
            if (rect == null) {
                continue;
            }
            int rowTop = (int) rect.getY();
            if (zebra && i % 2 != 0) {
                // The band fills the whole cell without padding: from the
                // middle of the separator line above to the middle of the
                // one beneath the segment.
                g2.setColor(zebraColor);
                int lineHeight = getFontMetrics(editor.getFont()).getHeight();
                int bandTop = rowTop - lineHeight / 2;
                int bandBottom = zebraBandBottom(editor, builders, i,
                        segmentTextBottom(editor, builder, rowTop), lineHeight);
                g2.fillRect(0, bandTop, getWidth(), bandBottom - bandTop);
            }
            if (grid) {
                // Centred in the empty separator line between the segments.
                g2.setColor(gridColor);
                int gapCentre = rowTop - getFontMetrics(editor.getFont()).getHeight() / 2;
                g2.drawLine(0, gapCentre, getWidth(), gapCentre);
            }
            g2.setFont(builder.isActive() ? activeFont : rowFont);
            g2.setColor(muted);
            // Alignment must measure with the font this row is drawn in: the
            // active row is bold, so the shared (bold) column metrics would
            // overestimate plain rows and push trailing text off the edge.
            FontMetrics rowFm = g2.getFontMetrics();
            // The texts start at the first displayed text line, so the
            // modification info line of the segment keeps the full width.
            int textTop = textLineTop(builder, rowTop);
            // The text centre sits on the centre line of the source text:
            // its glyph centre approximated from the editor font metrics.
            FontMetrics editorFm = getFontMetrics(editor.getFont());
            int baseline = textTop + (editorFm.getAscent() + editorFm.getDescent()) / 2
                    + (fm.getAscent() - fm.getDescent()) / 2;
            int x = LEADING_INSET;
            TMXEntry trans = Core.getProject().getTranslationInfo(builder.getSourceTextEntry());
            for (int c = 0; c < columnWidths.length; c++) {
                Column column = enabledColumns.get(c);
                int columnWidth = columnWidths[c];
                Graphics2D cell = (Graphics2D) g2.create();
                try {
                    if (column == Column.COLOR) {
                        // The target pair sits at the height of the first
                        // translation line, so the clip spans the segment.
                        cell.clipRect(x, rowTop, columnWidth, getHeight() - rowTop);
                        paintColorSwatch(cell, x, columnWidth, rowTop, fm, builder, muted);
                    } else {
                        cell.clipRect(x, textTop, columnWidth, fm.getHeight());
                        String value = value(column, builder, trans);
                        int textX;
                        switch (column.getAlignment()) {
                        case CENTER:
                            textX = x + (columnWidth - rowFm.stringWidth(value)) / 2;
                            break;
                        case TRAILING:
                            textX = x + columnWidth - rowFm.stringWidth(value);
                            break;
                        default:
                            textX = x;
                            break;
                        }
                        cell.drawString(value, textX, baseline);
                    }
                } finally {
                    cell.dispose();
                }
                x += columnWidth + COLUMN_GAP;
            }
        }

        if (grid) {
            // Vertical grid lines on the column boundaries.
            g2.setColor(gridColor);
            if (Column.metadataAfterText()) {
                // The boundary between the text and the trailing gutter.
                g2.drawLine(0, clipTop, 0, clipBottom);
            }
            int x = LEADING_INSET;
            for (int columnWidth : columnWidths) {
                x += columnWidth + COLUMN_GAP;
                g2.drawLine(x - COLUMN_GAP / 2 - 1, clipTop, x - COLUMN_GAP / 2 - 1, clipBottom);
            }
        }
        EditorRenderStats.end("gutter.paint", renderToken);
    }

    /**
     * Paints the alternating backgrounds of the segments across the editor.
     * Called from the editor's highlighter, between the background fill of
     * the UI delegate and the text, so the stripes lie under everything the
     * text paints; the gutter draws its own share.
     */
    static void paintZebraStripes(EditorTextArea3 editor, Graphics g) {
        if (!Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER)
                || !Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER_ZEBRA)
                || !Core.getProject().isProjectLoaded()) {
            return;
        }
        SegmentBuilder[] builders = editor.controller.m_docSegList;
        if (builders == null) {
            return;
        }
        long renderToken = EditorRenderStats.begin();
        g.setColor(zebraColor(editor.getForeground(), editor.getBackground()));
        int clipTop = g.getClipBounds().y;
        int clipBottom = clipTop + g.getClipBounds().height;
        int lineHeight = editor.getFontMetrics(editor.getFont()).getHeight();
        int clipTopOffset = editor.viewToModel2D(new Point(0, clipTop));
        int clipBottomOffset = editor.viewToModel2D(new Point(0, clipBottom + lineHeight));
        // Starts at the row containing the clip top: a stripe can begin far
        // above the repainted strip and still reach into it.
        for (int i = firstRowIndex(builders, clipTopOffset); i < builders.length; i++) {
            SegmentBuilder builder = builders[i];
            if (!builder.hasBeenCreated()) {
                continue;
            }
            if (builder.getStartPosition() > clipBottomOffset) {
                break;
            }
            if (i % 2 == 0) {
                continue;
            }
            try {
                Rectangle2D rect = segmentTopRect(editor, builder);
                if (rect == null) {
                    continue;
                }
                int rowTop = (int) rect.getY();
                // The band fills the whole cell without padding, from
                // separator middle to separator middle.
                int bandTop = rowTop - lineHeight / 2;
                int bandBottom = zebraBandBottom(editor, builders, i,
                        segmentTextBottom(editor, builder, rowTop), lineHeight);
                g.fillRect(0, bandTop, editor.getWidth(), bandBottom - bandTop);
            } catch (BadLocationException ignored) {
                // a not yet laid out segment has no stripe
            }
        }
        EditorRenderStats.end("zebra.paint", renderToken);
    }

    /**
     * Paints the horizontal grid lines across the editor, centred in the
     * separator line between the segments. Called from the editor's paint so
     * the lines span the full editor width; the gutter draws its own share.
     */
    static void paintSegmentSeparators(EditorTextArea3 editor, Graphics g) {
        if (!Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER)
                || !Preferences.isPreference(Preferences.EDITOR_METADATA_GUTTER_GRID)
                || !Core.getProject().isProjectLoaded()) {
            return;
        }
        SegmentBuilder[] builders = editor.controller.m_docSegList;
        if (builders == null) {
            return;
        }
        long renderToken = EditorRenderStats.begin();
        g.setColor(gridColor(editor.getForeground(), editor.getBackground()));
        int clipTop = g.getClipBounds().y;
        int clipBottom = clipTop + g.getClipBounds().height;
        int lineHeight = editor.getFontMetrics(editor.getFont()).getHeight();
        // The vertical boundary between the two text cells of the side by
        // side layout; it is also the drag handle for the cell widths. It
        // skips the active segment, which keeps the stacked layout.
        SegmentColumnsView columnsView = editor.columnsView();
        int boundary = columnsView == null ? -1 : columnsView.boundaryX();
        if (boundary >= 0) {
            // The view x and the component x differ by the text margin.
            boundary += editor.getInsets().left;
            int skipTop = Integer.MAX_VALUE;
            int skipBottom = Integer.MIN_VALUE;
            int activeIndex = editor.controller.displayedEntryIndex;
            SegmentBuilder activeBuilder = activeIndex >= 0 && activeIndex < builders.length
                    ? builders[activeIndex] : null;
            if (activeBuilder != null && activeBuilder.isActive()
                    && activeBuilder.hasBeenCreated()) {
                try {
                    Rectangle2D active = segmentTopRect(editor, activeBuilder);
                    if (active != null) {
                        int activeTop = (int) active.getY();
                        skipTop = activeTop - lineHeight / 2;
                        skipBottom = segmentTextBottom(editor, activeBuilder, activeTop)
                                + lineHeight - lineHeight / 2;
                    }
                } catch (BadLocationException ignored) {
                    // no active row laid out, nothing to skip
                }
            }
            if (skipTop == Integer.MAX_VALUE) {
                g.drawLine(boundary, clipTop, boundary, clipBottom);
            } else {
                if (skipTop > clipTop) {
                    g.drawLine(boundary, clipTop, boundary, Math.min(clipBottom, skipTop));
                }
                if (skipBottom < clipBottom) {
                    g.drawLine(boundary, Math.max(clipTop, skipBottom), boundary, clipBottom);
                }
            }
        }
        int clipTopOffset = editor.viewToModel2D(new Point(0, clipTop));
        // One line further down: the separator line of a segment sits above
        // its start, so a segment just below the repainted strip still owns a
        // line within it (scrolling repaints only the newly exposed strip).
        int clipBottomOffset = editor.viewToModel2D(new Point(0, clipBottom + lineHeight));
        for (int i = firstRowIndex(builders, clipTopOffset); i < builders.length; i++) {
            SegmentBuilder builder = builders[i];
            if (!builder.hasBeenCreated() || builder.getStartPosition() < clipTopOffset) {
                continue;
            }
            if (builder.getStartPosition() > clipBottomOffset) {
                break;
            }
            try {
                Rectangle2D rect = segmentTopRect(editor, builder);
                if (rect == null) {
                    continue;
                }
                int gapCentre = (int) rect.getY() - lineHeight / 2;
                g.drawLine(0, gapCentre, editor.getWidth(), gapCentre);
            } catch (BadLocationException ignored) {
                // a not yet laid out segment has no line
            }
        }
        EditorRenderStats.end("grid.paint", renderToken);
    }

    /**
     * The bottom of the zebra band of the segment: the band reaches the top
     * of the next created segment's band, so the bands tile without gaps.
     */
    private static int zebraBandBottom(EditorTextArea3 editor, SegmentBuilder[] builders,
            int index, int textBottom, int lineHeight) {
        for (int j = index + 1; j < builders.length; j++) {
            if (!builders[j].hasBeenCreated()) {
                continue;
            }
            try {
                Rectangle2D rect = segmentTopRect(editor, builders[j]);
                if (rect != null) {
                    return (int) rect.getY() - lineHeight / 2;
                }
            } catch (BadLocationException ignored) {
                break;
            }
        }
        return textBottom + lineHeight - lineHeight / 2;
    }

    /**
     * The view rectangle of the visual top of the segment: with the swapped
     * stacked order the translation block sits above the segment's first
     * document offset.
     */
    private static @Nullable Rectangle2D segmentTopRect(EditorTextArea3 editor,
            SegmentBuilder builder) throws BadLocationException {
        EditorRenderStats.count("topRect", 1);
        Rectangle2D rect = editor.modelToView2D(builder.getStartPosition());
        int translationStart = builder.getStartTranslationPosition();
        if (translationStart >= 0) {
            Rectangle2D translation = editor.modelToView2D(translationStart);
            if (translation != null && (rect == null || translation.getY() < rect.getY())) {
                rect = translation;
            }
        }
        return rect;
    }

    /** The view bottom of the last text line of the segment. */
    private static int segmentTextBottom(EditorTextArea3 editor, SegmentBuilder builder,
            int rowTop) {
        int bottom = -1;
        try {
            Rectangle2D rect = builder.endPosM1 == null ? null
                    : editor.modelToView2D(builder.endPosM1.getOffset());
            if (rect != null) {
                bottom = (int) (rect.getY() + rect.getHeight());
            }
            // With the swapped stacked order the source block sits below
            // the segment's last document offset.
            String sourceText = builder.getSourceText();
            if (sourceText != null && builder.getStartSourcePosition() >= 0) {
                Rectangle2D source = editor.modelToView2D(
                        builder.getStartSourcePosition() + sourceText.length());
                if (source != null) {
                    bottom = Math.max(bottom, (int) (source.getY() + source.getHeight()));
                }
            }
        } catch (BadLocationException ignored) {
            // fall through to the fallback
        }
        return bottom >= 0 ? bottom
                : rowTop + editor.getFontMetrics(editor.getFont()).getHeight();
    }

    @Override
    public @Nullable String getToolTipText(MouseEvent event) {
        SegmentBuilder row = rowAt(event.getY());
        Column column = columnAt(event.getX());
        if (row == null || column == null) {
            return null;
        }
        TMXEntry trans = Core.getProject().getTranslationInfo(row.getSourceTextEntry());
        if (column == Column.COLOR) {
            // One line per text part keeps the colour names readable.
            String lines = colorNames(row);
            return lines.isEmpty() ? null
                    : "<html>" + column.getLabel() + ":<br>" + lines + "</html>";
        }
        if (column == Column.STATUS) {
            return "<html>" + column.getLabel() + ":<br>"
                    + String.join("<br>", statusDescription(row, trans)) + "</html>";
        }
        String value = value(column, row, trans);
        return value.isEmpty() ? null : column.getLabel() + ": " + value;
    }

    /** The status shorthands of the row spelled out, one line each. */
    private List<String> statusDescription(SegmentBuilder builder, TMXEntry trans) {
        List<String> lines = new ArrayList<>();
        lines.add(OStrings.getString(trans.isTranslated() ? "GUI_EDITORWINDOW_GUTTER_STATUS_TRANSLATED"
                : "GUI_EDITORWINDOW_GUTTER_STATUS_UNTRANSLATED"));
        switch (builder.getSourceTextEntry().getDuplicate()) {
        case FIRST:
            lines.add(OStrings.getString("GUI_EDITORWINDOW_GUTTER_STATUS_DUP_FIRST"));
            break;
        case NEXT:
            lines.add(OStrings.getString("GUI_EDITORWINDOW_GUTTER_STATUS_DUP_NEXT"));
            break;
        default:
            lines.add(OStrings.getString("GUI_EDITORWINDOW_GUTTER_STATUS_UNIQUE"));
            break;
        }
        if (trans.linked != null) {
            switch (trans.linked) {
            case xICE:
                lines.add(OStrings.getString("GUI_EDITORWINDOW_GUTTER_STATUS_ICE"));
                break;
            case x100PC:
                lines.add(OStrings.getString("GUI_EDITORWINDOW_GUTTER_STATUS_100PC"));
                break;
            case xAUTO:
                lines.add(OStrings.getString("GUI_EDITORWINDOW_GUTTER_STATUS_AUTO"));
                break;
            case xENFORCED:
                lines.add(OStrings.getString("GUI_EDITORWINDOW_GUTTER_STATUS_ENFORCED"));
                break;
            default:
                break;
            }
        }
        if (trans.origin != null && !trans.origin.isEmpty()) {
            lines.add(OStrings.getString("GUI_EDITORWINDOW_GUTTER_STATUS_ORIGIN") + ": "
                    + trans.origin);
        }
        return lines;
    }

    /** The enabled column under the given x coordinate. */
    private @Nullable Column columnAt(int eventX) {
        SegmentBuilder[] builders = builders();
        if (builders == null) {
            return null;
        }
        FontMetrics fm = getFontMetrics(getRowFont(true));
        int x = LEADING_INSET;
        for (Column column : Column.gutterColumns()) {
            if (!column.isEnabled()) {
                continue;
            }
            int width = columnWidth(column, builders, fm);
            if (eventX >= x && eventX < x + width) {
                return column;
            }
            x += width + COLUMN_GAP;
        }
        return null;
    }

    /**
     * The index of the last created row starting above the clip, so that a
     * row whose segment starts above the visible area is still painted.
     */
    private static int firstRowIndex(SegmentBuilder[] builders, int clipTopOffset) {
        // A binary search over the created rows, which are in document
        // order; an uncreated row defers to its nearest created neighbour
        // on the left. A linear scan would run over every row per paint.
        int lo = 0;
        int hi = builders.length - 1;
        int result = 0;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int probe = mid;
            while (probe >= lo && !builders[probe].hasBeenCreated()) {
                probe--;
            }
            if (probe < lo) {
                lo = mid + 1;
                continue;
            }
            if (builders[probe].getStartPosition() > clipTopOffset) {
                hi = probe - 1;
            } else {
                result = probe;
                lo = mid + 1;
            }
        }
        return result;
    }

    private String value(Column column, SegmentBuilder builder, TMXEntry trans) {
        SourceTextEntry ste = builder.getSourceTextEntry();
        switch (column) {
        case NUMBER:
            return SegmentMetadataFormatter.number(builder.segmentNumberInProject,
                    Locale.getDefault());
        case STATUS:
            return SegmentMetadataFormatter.status(trans.isTranslated(), ste.getDuplicate(),
                    trans.linked, trans.origin);
        case AUTHOR:
            return SegmentMetadataFormatter.regexMatch(
                    SegmentMetadataFormatter.author(trans.changer, trans.creator),
                    columnPattern(Column.AUTHOR));
        case CREATION_AUTHOR:
            return SegmentMetadataFormatter.regexMatch(SegmentMetadataFormatter.id(trans.creator),
                    columnPattern(Column.CREATION_AUTHOR));
        case DATE:
            return SegmentMetadataFormatter.date(
                    trans.changeDate > 0 ? trans.changeDate : trans.creationDate,
                    ZoneId.systemDefault(), columnDateFormat(Column.DATE));
        case CREATION_DATE:
            return SegmentMetadataFormatter.date(trans.creationDate, ZoneId.systemDefault(),
                    columnDateFormat(Column.CREATION_DATE));
        case ID:
            return SegmentMetadataFormatter.regexMatch(
                    SegmentMetadataFormatter.id(ste.getKey().id), columnPattern(Column.ID));
        case SOURCE_LENGTH:
            return columnLength(column, ste.getSrcText());
        case TARGET_LENGTH:
            return columnLength(column, liveTranslation(builder, trans));
        case COMMENT_LENGTH:
            return columnLength(column, ste.getComment());
        case NOTE_LENGTH:
            return columnLength(column, liveNote(builder, trans));
        case ALTERNATIVE:
            return SegmentMetadataFormatter.alternative(builder.isDefaultTranslation());
        default:
            return "";
        }
    }

    /**
     * The effective total width of the gutter right now: the widths of the
     * enabled columns plus the effective padding. Shown in the configuration
     * dialog.
     */
    int currentTotalWidth() {
        return getPreferredSize().width;
    }

    /** The effective width of the column right now, for the width slider. */
    int currentColumnWidth(Column column) {
        SegmentBuilder[] builders = builders();
        return columnWidth(column, builders == null ? new SegmentBuilder[0] : builders,
                getFontMetrics(getRowFont(true)));
    }

    private int columnWidth(Column column, SegmentBuilder[] builders, FontMetrics fm) {
        // The user width is stored with the font size it was chosen at and
        // scales with the current editor font size; a future smooth zoom
        // inherits that scaling for free.
        int userWidth = Preferences.getPreferenceDefault(column.getWidthKey(), 0);
        if (userWidth > 0) {
            int referenceSize = Preferences.getPreferenceDefault(column.getWidthRefKey(), 0);
            int currentSize = editor.getFont().getSize();
            if (referenceSize > 0 && referenceSize != currentSize) {
                return Math.max(4, Math.round(userWidth * (float) currentSize / referenceSize));
            }
            return userWidth;
        }
        if (column == Column.COLOR) {
            // The base pair plus room for boxes of further marker colours
            // and for the underline miniatures.
            int single = colorPairWidth(fm) + 2 * MAX_EXTRA_COLORS * (foregroundBoxSize(fm) + 2);
            // Side by side, source and target sit level: the colour column
            // doubles into a source half and a target half beside it.
            return stackedTexts() ? single : 2 * single + COLUMN_GAP;
        }
        return fm.stringWidth(widthSample(column, builders, fm));
    }

    /** The classic layout with the source text above the translation. */
    private static boolean stackedTexts() {
        return Preferences.isPreferenceDefault(Preferences.EDITOR_LAYOUT_STACKED, true);
    }

    /** The background box, a gap, the smaller foreground box. */
    private static int colorPairWidth(FontMetrics fm) {
        return fm.getAscent() + 2 + foregroundBoxSize(fm);
    }

    private static int foregroundBoxSize(FontMetrics fm) {
        return Math.max(4, fm.getAscent() * 7 / 10);
    }

    private String widthSample(Column column, SegmentBuilder[] builders, FontMetrics fm) {
        if (column == Column.NUMBER && builders.length > 0) {
            String largest = SegmentMetadataFormatter.number(
                    builders[builders.length - 1].segmentNumberInProject, Locale.getDefault());
            if (fm.stringWidth(largest) > fm.stringWidth(column.widthSample)) {
                return largest;
            }
        }
        return column.widthSample;
    }

    /**
     * The bound marker colours: per displayed text part a pair of boxes, the
     * background box first, the smaller foreground box beside it, and a strip
     * with miniatures of the underline markers of that part beneath them.
     * Each pair sits at the height of the first line of its text part, so
     * stacked source and target texts get stacked pairs. The bound palette
     * entries are resolved when painting, so the boxes follow colour changes
     * like the editor does.
     */
    private void paintColorSwatch(Graphics2D g2, int x, int columnWidth, int rowTop,
            FontMetrics fm, SegmentBuilder builder, Color outline) {
        // Side by side, both parts start level, so the target pair moves
        // into the second half of the doubled column.
        int targetX = stackedTexts() ? x : x + columnWidth / 2 + 1;
        if (builder.getSourceText() != null && builder.posSourceBeg != null) {
            paintColorPair(g2, x, lineTop(builder.posSourceBeg.getOffset(), rowTop), fm, outline,
                    builder.posSourceBeg.getOffset(), sourceRange(builder));
        }
        int translationStart = translationStartOffset(builder);
        if (translationStart >= 0) {
            paintColorPair(g2, targetX, lineTop(translationStart, rowTop), fm, outline,
                    translationStart, targetRange(builder));
        }
    }

    /** The length of the text with the trim and non-space options applied. */
    private String columnLength(Column column, @Nullable String text) {
        return SegmentMetadataFormatter.length(text, Locale.getDefault(),
                Preferences.isPreference(column.getTrimKey()),
                Preferences.isPreference(column.getNonSpaceKey()));
    }

    /**
     * The translation being typed for the active segment, the stored
     * translation otherwise. Keeps the target length live while editing.
     */
    private @Nullable String liveTranslation(SegmentBuilder builder, TMXEntry trans) {
        if (builder.isActive()) {
            Document3 doc = editor.getOmDocument();
            if (doc != null) {
                String current = doc.extractTranslation();
                if (current != null) {
                    return current;
                }
            }
        }
        return trans.translation;
    }

    /**
     * The note being typed for the active segment, the stored note otherwise.
     */
    private @Nullable String liveNote(SegmentBuilder builder, TMXEntry trans) {
        if (builder.isActive()) {
            String current = Core.getNotes().getNoteText();
            return current == null || current.isEmpty() ? null : current;
        }
        return trans.note;
    }

    /**
     * The document offset of the displayed translation. The active segment
     * has no translation position in its builder; the document knows it.
     */
    private int translationStartOffset(SegmentBuilder builder) {
        if (builder.isActive()) {
            Document3 doc = editor.getOmDocument();
            return doc == null ? -1 : doc.getTranslationStart();
        }
        return builder.getStartTranslationPosition();
    }

    /** The view top of the first displayed text line of the segment. */
    private int textLineTop(SegmentBuilder builder, int fallback) {
        // Never above the fallback (the row top): segments loaded upward are
        // inserted at document offset 0 and a Position created there sticks
        // at 0 forever, so their source anchor ends up pointing at whatever
        // is the top of the document once more segments load above them.
        // The smaller of the two part tops counts: with the swapped stacked
        // order the translation block sits above the source block.
        int top = Integer.MAX_VALUE;
        if (builder.getSourceText() != null && builder.posSourceBeg != null) {
            top = lineTop(builder.posSourceBeg.getOffset(), fallback);
        }
        int translationStart = translationStartOffset(builder);
        if (translationStart >= 0) {
            top = Math.min(top, lineTop(translationStart, fallback));
        }
        return top == Integer.MAX_VALUE ? fallback : Math.max(fallback, top);
    }

    /** The view top of the line at the offset, the row top as fallback. */
    private int lineTop(int offset, int fallback) {
        try {
            Rectangle2D rect = editor.modelToView2D(offset);
            return rect == null ? fallback : (int) rect.getY();
        } catch (BadLocationException ex) {
            return fallback;
        }
    }

    private void paintColorPair(Graphics2D g2, int x, int rowTop, FontMetrics fm, Color outline,
            int attributeOffset, int[] range) {
        int bgBox = fm.getAscent();
        int fgBox = foregroundBoxSize(fm);
        Styles.EditorColor fg = boundColorAt(attributeOffset, Styles.EDITOR_COLOR_FOREGROUND);
        Styles.EditorColor bg = boundColorAt(attributeOffset, Styles.EDITOR_COLOR_BACKGROUND);
        paintBox(g2, x, rowTop, bgBox, bg == null ? null : bg.getColor(), outline);
        paintBox(g2, x + bgBox + 2, rowTop + (bgBox - fgBox) / 2, fgBox,
                fg == null ? null : fg.getColor(), outline);

        // Further marker colours within the text get boxes of their own,
        // e.g. the tag colour of protected parts.
        int extraX = x + colorPairWidth(fm) + 2;
        for (Color extra : extraBoundColors(range, fg, bg)) {
            paintBox(g2, extraX, rowTop + (bgBox - fgBox) / 2, fgBox, extra, outline);
            extraX += fgBox + 2;
        }

        // Each underline painter renders its own miniature beside the boxes,
        // centred on their centre line, so the row shows the real underline
        // style and follows future style additions. The painters anchor
        // their line near the bottom of the given rectangle.
        // Metric-derived so the geometry scales with a future editor zoom.
        // Each painter anchors its mark differently within the rectangle, so
        // the rectangle is placed per painter to land on the centre line of
        // the boxes: waves draw near the bottom edge, line painters at the
        // font baseline above the descent, and the glyph painters at the
        // ascent below the rectangle top.
        FontMetrics editorFm = getFontMetrics(editor.getFont());
        int editorDescent = editorFm.getDescent();
        int miniatureHeight = editorDescent + 3;
        int midline = rowTop + bgBox / 2;
        int painted = 0;
        for (UnderlineFactory.Underliner underline : underlinePainters(range)) {
            if (painted++ >= MAX_EXTRA_COLORS) {
                break;
            }
            int miniatureTop;
            if (underline instanceof SymbolPainter || underline instanceof BidiPainter) {
                miniatureTop = midline - 2 * editorFm.getAscent() / 3;
            } else if (underline instanceof UnderlineFactory.WaveUnderline) {
                miniatureTop = midline + 3 - miniatureHeight;
            } else {
                miniatureTop = midline + Math.max(1, editorDescent - 1) - miniatureHeight;
            }
            underline.paint(g2, new Rectangle(extraX, miniatureTop, fgBox, miniatureHeight),
                    editor);
            extraX += fgBox + 2;
        }
    }

    /** The document range of the displayed source text of the segment. */
    private int[] sourceRange(SegmentBuilder builder) {
        int start = builder.getStartSourcePosition();
        int translationStart = translationStartOffset(builder);
        return new int[] { start, translationStart > start ? translationStart - 1
                : segmentEnd(builder) };
    }

    /** The document range of the displayed translation text of the segment. */
    private int[] targetRange(SegmentBuilder builder) {
        int translationStart = translationStartOffset(builder);
        return new int[] { translationStart >= 0 ? translationStart : builder.getStartPosition(),
                segmentEnd(builder) };
    }

    /**
     * The distinct further bound colours within the range, beyond the two of
     * the base pair.
     */
    private List<Color> extraBoundColors(int[] range, Styles.@Nullable EditorColor baseFg,
            Styles.@Nullable EditorColor baseBg) {
        List<Color> extras = new ArrayList<>();
        Document3 doc = editor.getOmDocument();
        if (doc == null) {
            return extras;
        }
        int offset = range[0];
        while (offset <= range[1] && extras.size() < MAX_EXTRA_COLORS) {
            Element element = doc.getCharacterElement(offset);
            for (Object bindingKey : new Object[] { Styles.EDITOR_COLOR_FOREGROUND,
                    Styles.EDITOR_COLOR_BACKGROUND }) {
                Object bound = element.getAttributes().getAttribute(bindingKey);
                if (bound instanceof Styles.EditorColor && bound != baseFg && bound != baseBg) {
                    Color color = ((Styles.EditorColor) bound).getColor();
                    if (color != null && !extras.contains(color)) {
                        extras.add(color);
                    }
                }
            }
            offset = Math.max(element.getEndOffset(), offset + 1);
        }
        return extras;
    }

    /**
     * The underline marker painters within the range, deduplicated by style
     * and colour.
     */
    private List<UnderlineFactory.Underliner> underlinePainters(int[] range) {
        List<UnderlineFactory.Underliner> result = new ArrayList<>();
        for (Highlighter.Highlight highlight : editor.getHighlighter().getHighlights()) {
            if (highlight.getEndOffset() < range[0] || highlight.getStartOffset() > range[1]) {
                continue;
            }
            if (!(highlight.getPainter() instanceof UnderlineFactory.Underliner)) {
                continue;
            }
            UnderlineFactory.Underliner underliner = (UnderlineFactory.Underliner) highlight
                    .getPainter();
            boolean known = result.stream()
                    .anyMatch(seen -> seen.getClass() == underliner.getClass() && Objects
                            .equals(seen.getUnderlineColor(), underliner.getUnderlineColor()));
            if (!known) {
                result.add(underliner);
            }
        }
        return result;
    }

    private int segmentEnd(SegmentBuilder builder) {
        return builder.endPosM1 == null ? builder.getStartPosition()
                : builder.endPosM1.getOffset() + 1;
    }

    private void paintBox(Graphics2D g2, int x, int y, int size, @Nullable Color fill,
            Color outline) {
        if (fill != null) {
            g2.setColor(fill);
            g2.fillRect(x, y, size - 1, size - 1);
        }
        g2.setColor(outline);
        g2.drawRect(x, y, size - 2, size - 2);
    }

    private Styles.@Nullable EditorColor boundColorAt(int offset, Object bindingKey) {
        Document3 doc = editor.getOmDocument();
        if (doc == null) {
            return null;
        }
        Object bound = doc.getCharacterElement(offset).getAttributes().getAttribute(bindingKey);
        return bound instanceof Styles.EditorColor ? (Styles.EditorColor) bound : null;
    }

    /**
     * The names of the palette entries that determine the row colours, one
     * line per displayed text part.
     */
    private String colorNames(SegmentBuilder builder) {
        List<String> lines = new ArrayList<>();
        if (builder.getSourceText() != null && builder.posSourceBeg != null) {
            String names = partColorNames(builder.posSourceBeg.getOffset(), sourceRange(builder));
            if (!names.isEmpty()) {
                lines.add(OStrings.getString("GUI_EDITORWINDOW_GUTTER_TOOLTIP_SOURCE") + ": "
                        + names);
            }
        }
        int translationStart = translationStartOffset(builder);
        if (translationStart >= 0) {
            String names = partColorNames(translationStart, targetRange(builder));
            if (!names.isEmpty()) {
                lines.add(OStrings.getString("GUI_EDITORWINDOW_GUTTER_TOOLTIP_TARGET") + ": "
                        + names);
            }
        }
        return String.join("<br>", lines);
    }

    private String partColorNames(int attributeOffset, int[] range) {
        Styles.EditorColor fg = boundColorAt(attributeOffset, Styles.EDITOR_COLOR_FOREGROUND);
        Styles.EditorColor bg = boundColorAt(attributeOffset, Styles.EDITOR_COLOR_BACKGROUND);
        StringBuilder names = new StringBuilder();
        if (bg != null) {
            names.append(bg.getDisplayName());
        }
        if (fg != null && fg != bg) {
            if (names.length() > 0) {
                names.append(" / ");
            }
            names.append(fg.getDisplayName());
        }
        for (Color extra : extraBoundColors(range, fg, bg)) {
            if (names.length() > 0) {
                names.append(" / ");
            }
            names.append(colorName(extra));
        }
        for (UnderlineFactory.Underliner underline : underlinePainters(range)) {
            if (names.length() > 0) {
                names.append(" · ");
            }
            Color color = underline.getUnderlineColor();
            names.append(color == null ? underline.getClass().getSimpleName() : colorName(color));
        }
        return names.toString();
    }

    /** The palette name of the colour, its hex value when unknown. */
    private static String colorName(Color color) {
        for (Styles.EditorColor entry : Styles.EditorColor.values()) {
            if (color.equals(entry.getColor())) {
                return entry.getDisplayName();
            }
        }
        return String.format("#%06X", color.getRGB() & 0xFFFFFF);
    }

    private final Map<Column, String> optionSpecCache = new HashMap<>();
    private final Map<Column, Object> optionCompiledCache = new HashMap<>();

    /** The compiled, validated regex of the column; null when off or broken. */
    private @Nullable Pattern columnPattern(Column column) {
        Object compiled = compiledOption(column,
                spec -> {
                    try {
                        return Pattern.compile(spec);
                    } catch (PatternSyntaxException ex) {
                        return null;
                    }
                });
        return compiled instanceof Pattern ? (Pattern) compiled : null;
    }

    /** The compiled, validated date format of the column; null when off or broken. */
    private @Nullable DateTimeFormatter columnDateFormat(Column column) {
        Object compiled = compiledOption(column,
                spec -> {
                    try {
                        return DateTimeFormatter.ofPattern(spec);
                    } catch (IllegalArgumentException ex) {
                        return null;
                    }
                });
        return compiled instanceof DateTimeFormatter ? (DateTimeFormatter) compiled : null;
    }

    private @Nullable Object compiledOption(Column column,
            Function<String, @Nullable Object> compiler) {
        String spec = column.activeOptionValue();
        if (spec == null) {
            optionSpecCache.remove(column);
            optionCompiledCache.remove(column);
            return null;
        }
        if (!spec.equals(optionSpecCache.get(column))) {
            optionSpecCache.put(column, spec);
            optionCompiledCache.put(column, compiler.apply(spec));
        }
        return optionCompiledCache.get(column);
    }

    private SegmentBuilder @Nullable [] builders() {
        if (!Core.getProject().isProjectLoaded()) {
            return null;
        }
        return controller.m_docSegList;
    }

    private @Nullable SegmentBuilder rowAt(int y) {
        SegmentBuilder[] builders = builders();
        if (builders == null) {
            return null;
        }
        int offset = editor.viewToModel2D(new Point(0, y));
        SegmentBuilder result = null;
        for (SegmentBuilder builder : builders) {
            if (!builder.hasBeenCreated()) {
                continue;
            }
            if (builder.getStartPosition() > offset) {
                break;
            }
            result = builder;
        }
        return result;
    }

    private Font getRowFont(boolean active) {
        // A bit smaller than the segment text so the gutter stays discreet.
        Font font = editor.getFont();
        font = font.deriveFont(Math.max(8f, font.getSize() - 2f));
        return active ? font.deriveFont(Font.BOLD) : font;
    }

    private static Color mix(Color fg, Color bg) {
        return new Color((fg.getRed() + bg.getRed()) / 2, (fg.getGreen() + bg.getGreen()) / 2,
                (fg.getBlue() + bg.getBlue()) / 2);
    }

    /** The translucent wash of the alternating segment backgrounds. */
    private static Color zebraColor(Color fg, Color bg) {
        Color muted = mix(fg, bg);
        return new Color(muted.getRed(), muted.getGreen(), muted.getBlue(), 55);
    }

    /** The translucent colour of the grid lines. */
    private static Color gridColor(Color fg, Color bg) {
        Color muted = mix(fg, bg);
        return new Color(muted.getRed(), muted.getGreen(), muted.getBlue(), 70);
    }
}
