/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
               2014-2015 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollBar;
import javax.swing.ListModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;

import org.omegat.gui.editor.EditorTextArea3;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;

/**
 * A list based auto-completer view.
 * 
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
public abstract class AutoCompleterListView extends AbstractAutoCompleterView {
    
    private static JList list;

    ListModel listModel = new DefaultListModel();
    
    private static AutoCompleterItem NO_SUGGESTIONS = new AutoCompleterItem(
            OStrings.getString("AC_NO_SUGGESTIONS"), null, 0);
    
    public AutoCompleterListView(String name) {
        super(name);
        getList().setFocusable(false);
    }
    
    public JList getList() {
        if (list == null) {
            list = new JList();
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
    public boolean processKeys(KeyEvent e, boolean visible) {
        if (StaticUtils.isKey(e, KeyEvent.VK_UP, 0)) {
            // process key UP
            if (visible) {
                selectPreviousPossibleValue();
            }
            return true;
        }

        if (StaticUtils.isKey(e, KeyEvent.VK_DOWN, 0)) {
            // process key DOWN
            if (visible) {
                selectNextPossibleValue();
            }
            return true;
        }

        if (StaticUtils.isKey(e, KeyEvent.VK_PAGE_UP, 0)) {
            if (visible) {
                selectPreviousPossibleValueByPage();
            }
            return true;
        }

        if (StaticUtils.isKey(e, KeyEvent.VK_PAGE_DOWN, 0)) {
            if (visible) {
                selectNextPossibleValueByPage();
            }
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
    
    @Override
    public void setData(List<AutoCompleterItem> entryList) {
        getList().setListData(entryList.toArray());
        if (!entryList.isEmpty()) {
            getList().setSelectedIndex(0);
            getList().invalidate();
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
    public boolean updateViewData() {
        EditorTextArea3 editor = completer.getEditor();
        try {
            int offset = editor.getCaretPosition();
            int translationStart = editor.getOmDocument().getTranslationStart();
            
            String prevText = editor.getDocument().getText(translationStart, offset - translationStart);
            
            List<AutoCompleterItem> entryList = computeListData(prevText);
            
            if (entryList.isEmpty()) {
                entryList.add(NO_SUGGESTIONS);
            }
            setData(entryList);
            
            return !entryList.isEmpty();
        } catch (BadLocationException ex) {
            // what now?
            return false;
        }
    }
    
    protected String getLastToken(String text) {
        String token = "";
        ITokenizer tokenizer = getTokenizer();
        Token[] tokens = tokenizer.tokenizeAllExactly(text);
        
        if (tokens.length != 0) {
            Token lastToken = tokens[tokens.length - 1];
            String lastString = text.substring(lastToken.getOffset()).trim();
            if (lastString.length() > 0) {
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
    public abstract List<AutoCompleterItem> computeListData(String prevText);
    
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
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            setBorder(LIST_MARGIN_BORDER);
            if (value == NO_SUGGESTIONS) {
                setText(((AutoCompleterItem)value).payload);
            } else {
                setText(itemToString((AutoCompleterItem)value));
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
