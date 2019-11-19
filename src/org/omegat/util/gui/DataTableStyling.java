/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JViewport;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.omegat.util.Platform;

/**
 *
 * @author Aaron Madlon-Kay
 */
public final class DataTableStyling {

    private DataTableStyling() {
    }

    public static final Color COLOR_STANDARD_FG = Color.BLACK;
    public static final Color COLOR_STANDARD_BG = Color.WHITE;
    public static final Color COLOR_SELECTION_FG = Color.WHITE;
    public static final Color COLOR_SELECTION_BG = new Color(0x2F77DA);
    public static final Color COLOR_ALTERNATING_HILITE = new Color(245, 245, 245);
    public static final Border TABLE_FOCUS_BORDER = new MatteBorder(1, 1, 1, 1, new Color(0x76AFE8));
    public static final Border TABLE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);

    public static final DecimalFormat NUMBER_FORMAT = new DecimalFormat(",##0");

    public static final int FONT_NO_CHANGE = -1;

    public static final int LINE_SPACING = 6;

    public static void applyColors(JTable table) {
        if (!Platform.isMacOSX()) {
            // Windows needs some extra colors set for consistency, but these
            // ruin native LAF on OS X.
            if (table.getParent() instanceof JViewport) {
                table.getParent().setBackground(COLOR_STANDARD_BG);
            }
            if (table.getParent().getParent() instanceof JScrollPane) {
                table.getParent().getParent().setBackground(COLOR_STANDARD_BG);
            }
            if (table.getTableHeader() != null) {
                table.getTableHeader().setBackground(COLOR_STANDARD_BG);
            }
        }
        table.setForeground(COLOR_STANDARD_FG);
        table.setBackground(COLOR_STANDARD_BG);
        table.setSelectionForeground(COLOR_SELECTION_FG);
        table.setSelectionBackground(COLOR_SELECTION_BG);
        table.setGridColor(COLOR_STANDARD_BG);
    }

    public static void applyFont(JTable table, Font font) {
        table.setFont(font);
        table.setRowHeight(font.getSize() + DataTableStyling.LINE_SPACING);
    }

    public static TableCellRenderer getNumberCellRenderer() {
        return new AlternatingHighlightRenderer().setAlignment(SwingConstants.RIGHT).setNumberFormat(NUMBER_FORMAT);
    }

    public static TableCellRenderer getTextCellRenderer() {
        return new AlternatingHighlightRenderer();
    }

    public static TableCellRenderer getHeaderTextCellRenderer() {
        return new AlternatingHighlightRenderer().setFontStyle(Font.BOLD);
    }

    public abstract static class FancyRenderer<T extends JComponent> implements TableCellRenderer {
        private NumberFormat numberFormat = null;
        private boolean doHighlight = true;
        private int fontStyle = FONT_NO_CHANGE;

        public FancyRenderer<T> setNumberFormat(NumberFormat numberFormat) {
            this.numberFormat = numberFormat;
            return this;
        }

        public FancyRenderer<T> setDoHighlight(boolean doHighlight) {
            this.doHighlight = doHighlight;
            return this;
        }

        public FancyRenderer<T> setFontStyle(int fontStyle) {
            this.fontStyle = fontStyle;
            return this;
        }

        protected String transformValue(Object value) {
            if (value == null) {
                return null;
            }
            if (numberFormat != null) {
                if (value instanceof Number) {
                    return numberFormat.format((Number) value);
                }
                if (value instanceof String) {
                    try {
                        long lVal = Long.parseLong((String) value);
                        return numberFormat.format(lVal);
                    } catch (NumberFormatException ignore) {
                    }
                }
            }
            return value.toString();
        }

        protected abstract T getComponent();

        protected abstract void applyValue(String value);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            T result = getComponent();
            Font font = table.getFont();
            if (fontStyle != FONT_NO_CHANGE) {
                font = font.deriveFont(fontStyle);
            }
            result.setFont(font);
            if (isSelected) {
                result.setForeground(table.getSelectionForeground());
                result.setBackground(table.getSelectionBackground());
            } else if (row % 2 != 0 && doHighlight) {
                result.setForeground(table.getForeground());
                result.setBackground(COLOR_ALTERNATING_HILITE);
            } else {
                result.setForeground(table.getForeground());
                result.setBackground(table.getBackground());
            }
            result.setBorder(hasFocus ? TABLE_FOCUS_BORDER : TABLE_NO_FOCUS_BORDER);
            applyValue(transformValue(value));
            return result;
        }
    }

    public static class AlternatingHighlightRenderer extends FancyRenderer<DefaultTableCellRenderer> {
        private final DefaultTableCellRenderer component = new DefaultTableCellRenderer();

        public AlternatingHighlightRenderer setAlignment(int alignment) {
            component.setHorizontalAlignment(alignment);
            return this;
        }

        @Override
        protected DefaultTableCellRenderer getComponent() {
            return component;
        }

        @Override
        protected void applyValue(String value) {
            component.setText(value);
        }
    }

    public static class PatternHighlightRenderer extends FancyRenderer<JTextPane> {
        private static final Color HIGHLIGHT_FG_COLOR = Color.BLACK;
        private static final Color HIGHLIGHT_BG_COLOR = Color.YELLOW;
        private static final AttributeSet EMPTY_ATTR = new SimpleAttributeSet();
        private static final AttributeSet HIGHLIGHT_ATTR;
        static {
            SimpleAttributeSet sas = new SimpleAttributeSet();
            StyleConstants.setBackground(sas, HIGHLIGHT_BG_COLOR);
            StyleConstants.setForeground(sas, HIGHLIGHT_FG_COLOR);
            HIGHLIGHT_ATTR = sas;
        }
        private final JTextPane component;
        private Pattern pattern;

        /**
         * Create a new PatternHighlightRenderer.
         * <p>
         * Disable line wrap when using this as a single-line renderer. This is because this renderer uses a
         * {@link JTextPane} as its rendering component, and the default JTextPane wrapping results in words simply
         * disappearing when used as a single-line renderer.
         *
         * @param lineWrapEnabled
         *            Whether to allow line wrapping
         * @see <a href="https://sourceforge.net/p/omegat/bugs/862/">Bug #862</a>
         */
        public PatternHighlightRenderer(boolean lineWrapEnabled) {
            component = new JTextPane();
            if (!lineWrapEnabled) {
                component.setEditorKit(new NoWrapEditorKit());
            }
        }

        public PatternHighlightRenderer setPattern(Pattern pattern) {
            this.pattern = pattern;
            return this;
        }

        @Override
        protected JTextPane getComponent() {
            return component;
        }

        @Override
        protected void applyValue(String value) {
            component.setText(value);
            if (value != null) {
                doHighlighting(value);
            }
        }

        void doHighlighting(String text) {
            StyledDocument doc = component.getStyledDocument();
            doc.setCharacterAttributes(0, text.length(), EMPTY_ATTR, true);
            if (pattern != null) {
                Matcher m = pattern.matcher(text);
                while (m.find()) {
                    doc.setCharacterAttributes(m.start(), m.end() - m.start(), HIGHLIGHT_ATTR, true);
                }
            }
        }
    }
}
