/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
               2014-2015 Aaron Madlon-Kay
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
package org.omegat.gui.editor.autocompleter;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.OStrings;
import org.omegat.util.Token;

/**
 * A list based auto-completer view.
 *
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
public abstract class AutoCompleterListView extends AbstractAutoCompleterView {

    private static JList<AutoCompleterItem> list;

    private static final AutoCompleterItem NO_SUGGESTIONS = new AutoCompleterItem(
            OStrings.getString("AC_NO_SUGGESTIONS"), null, 0);

    public AutoCompleterListView(String name) {
        super(name);
        getList().setFocusable(false);
    }

    public JList<AutoCompleterItem> getList() {
        if (list == null) {
            list = new JList<>();
            list.setCellRenderer(new CellRenderer());
            list.addMouseListener(mouseAdapter);
        }
        return list;
    }

    private final MouseAdapter mouseAdapter = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                Point p = e.getPoint();
                int i = list.locationToIndex(p);
                if (list.getSelectedIndex() == i && list.getCellBounds(i, i).contains(p)) {
                    completer.doSelection();
                }
            }
        }
    };

    @Override
    public boolean processKeys(KeyEvent e) {
        int code = e.getKeyCode();
        if ((code == KeyEvent.VK_LEFT || code == KeyEvent.VK_RIGHT) && completer.isVisible()
                && completer.didPopUpAutomatically) {
            // Close autocompleter if user presses left or right (we can't use these anyway since it's a
            // vertical list) and the completer appeared automatically.
            completer.setVisible(false);
            // Don't consume here so that the cursor movement can still take place.
            return false;
        }

        KeyStroke s = KeyStroke.getKeyStrokeForEvent(e);
        AutoCompleterKeys keys = completer.getKeys();

        if (s.equals(keys.listUp) || s.equals(keys.listUpEmacs)) {
            selectPreviousPossibleValue();
            return true;
        }

        if (s.equals(keys.listDown) || s.equals(keys.listDownEmacs)) {
            selectNextPossibleValue();
            return true;
        }

        if (s.equals(keys.listPageUp)) {
            selectPreviousPossibleValueByPage();
            return true;
        }

        if (s.equals(keys.listPageDown)) {
            selectNextPossibleValueByPage();
            return true;
        }

        return false;
    }

    /**
     * Selects the next item in the list.
     */
    protected void selectNextPossibleValue() {
        int i = (getList().getSelectedIndex() + 1) % getList().getModel().getSize();
        getList().setSelectedIndex(i);
        getList().ensureIndexIsVisible(i);
    }

    /**
     * Selects the item in the list following the current one by one page, or the last item if
     * there is less than one page following.
     */
    protected void selectNextPossibleValueByPage() {
        int page = getList().getLastVisibleIndex() - getList().getFirstVisibleIndex();
        int i = Math.min(getList().getSelectedIndex() + page, getList().getModel().getSize() - 1);
        getList().setSelectedIndex(i);
        getList().ensureIndexIsVisible(i);
    }

    /**
     * Selects the previous item in the list.
     */
    protected void selectPreviousPossibleValue() {
        int size = getList().getModel().getSize();
        int i = (getList().getSelectedIndex() - 1 + size) % size;
        getList().setSelectedIndex(i);
        getList().ensureIndexIsVisible(i);
    }

    /**
     * Selects the item in the list preceding the current one by one page, or the first item if
     * there is less than one page preceding.
     */
    protected void selectPreviousPossibleValueByPage() {
        int page = getList().getLastVisibleIndex() - getList().getFirstVisibleIndex();
        int i = Math.max(getList().getSelectedIndex() - page, 0);
        getList().setSelectedIndex(i);
        getList().ensureIndexIsVisible(i);
    }

    @Override
    public int getRowCount() {
        return getList().getModel().getSize();
    }

    @Override
    public int getPreferredHeight() {
        Rectangle bounds = getList().getCellBounds(0, 0);
        return (int) (getModifiedRowCount() * (bounds == null ? getList().getFont().getSize() : bounds.getHeight()));
    }

    @Override
    public int getPreferredWidth() {
        int width = getList().getPreferredSize().width;
        JScrollBar bar = completer.scroll.getVerticalScrollBar();
        if (bar != null) {
            width += bar.getPreferredSize().width;
        }
        return width;
    };

    protected void setData(AutoCompleterItem... entries) {
        getList().setListData(entries);
        if (entries.length > 0) {
            getList().setSelectedIndex(0);
            getList().invalidate();
            getList().scrollRectToVisible(new Rectangle());
        }
    }

    @Override
    public AutoCompleterItem getSelectedValue() {
        Object item = getList().getSelectedValue();
        return item == NO_SUGGESTIONS ? null : (AutoCompleterItem) item;
    }

    @Override
    public Component getViewContent() {
        getList().setVisibleRowCount(getModifiedRowCount());
        return getList();
    }

    @Override
    public void updateViewData() {
        List<AutoCompleterItem> entryList = computeListData(getLeadingText(), false);
        if (entryList.isEmpty()) {
            setData(NO_SUGGESTIONS);
        } else {
            setData(entryList.toArray(new AutoCompleterItem[entryList.size()]));
        }
    }

    @Override
    public boolean shouldPopUp() {
        return !computeListData(getLeadingText(), true).isEmpty();
    }

    protected String getLastToken(String text) {
        String token = "";
        ITokenizer tokenizer = getTokenizer();
        Token[] tokens = tokenizer.tokenizeVerbatim(text);

        if (tokens.length != 0) {
            Token lastToken = tokens[tokens.length - 1];
            String lastString = text.substring(lastToken.getOffset()).trim();
            if (!lastString.isEmpty()) {
                token = lastString;
            }
        }
        return token;
    }

    /**
     * Compute the items visible in the auto-completer list
     * @param prevText the text in the editing field up to the cursor location
     * @return a list of AutoCompleterItems.
     */
    public abstract List<AutoCompleterItem> computeListData(String prevText, boolean contextualOnly);

    /**
     * Each view should determine how to print a view item.
     * @param item The item to print
     * @return A string representation of the view item
     */
    public abstract String itemToString(AutoCompleterItem item);

    private static final Border LIST_MARGIN_BORDER = new EmptyBorder(0, 5, 0, 5);

    @SuppressWarnings("serial")
    private class CellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            setBorder(LIST_MARGIN_BORDER);
            if (value == NO_SUGGESTIONS) {
                setText(((AutoCompleterItem) value).payload);
            } else {
                AutoCompleterListView aclView = (AutoCompleterListView) completer.getCurrentView();
                AutoCompleterItem acItem = (AutoCompleterItem) value;
                setText(aclView.itemToString(acItem));
            }
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }
    }
}
