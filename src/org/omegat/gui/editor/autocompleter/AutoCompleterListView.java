/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
               2014 Aaron Madlon-Kay
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
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;
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
    private static CellRenderer renderer;

    ListModel listModel = new DefaultListModel();
    
    private static AutoCompleterItem NO_SUGGESTIONS = new AutoCompleterItem(
            OStrings.getString("AC_NO_SUGGESTIONS"), null);
    
    public AutoCompleterListView(String name, AutoCompleter completer) {
        super(name,completer);
        getList().setFocusable(false);
    }
    
    public JList getList() {
        if (list == null) {
            list = new JList();
            list.setFixedCellHeight(12);
            renderer = new CellRenderer(this);
            list.setCellRenderer(renderer);
        }
        return list;
    }
    
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
        int height = getModifiedRowCount() * getList().getFont().getSize();
        return Math.max(height, 50);
    }
    
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
        renderer.view = this;
        getList().setVisibleRowCount(getModifiedRowCount());
        return getList();
    }
    
    @Override
    public boolean updateViewData() {
        EditorTextArea3 editor = completer.getEditor();
        try {
            int offset = editor.getCaretPosition();
            int translationStart = editor.getOmDocument().getTranslationStart();
            
            // init - these are going to be overwritten, if something better is found.
            String wordChunk = "";
            completer.setWordChunkStart(offset);
            
            String prevText = editor.getDocument().getText(translationStart, offset - translationStart);
            
            if (prevText.length() != 0) {
                ITokenizer tokenizer = getTokenizer();
                Token[] tokens = tokenizer.tokenizeAllExactly(prevText);
                
                if (tokens.length != 0) {
                    Token lastToken = tokens[tokens.length - 1];
                    String lastString = prevText.substring(lastToken.getOffset()).trim();
                    if (lastString.length() > 0) {
                        wordChunk = lastString;
                        completer.setWordChunkStart(translationStart + lastToken.getOffset());
                    }
                }
            }
            
            List<AutoCompleterItem> entryList = computeListData(wordChunk);
            
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
    
    /**
     * Compute the items visible in the auto-completer list
     * @param wordChunk the string to start with
     * @return a list of strings.
     */
    public abstract List<AutoCompleterItem> computeListData(String wordChunk);
    
    /**
     * Each view should determine how to print a view item.
     * @param item The item to print
     * @return A string representation of the view item
     */
    public abstract String itemToString(AutoCompleterItem item);
    
    
    @SuppressWarnings("serial")
    class CellRenderer extends DefaultListCellRenderer {
        private AutoCompleterListView view;
        
        public CellRenderer(AutoCompleterListView view) {
            this.view = view;
        }
        
        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            if (value == NO_SUGGESTIONS) {
                setText(((AutoCompleterItem)value).payload);
            } else {
                setText(view.itemToString((AutoCompleterItem)value));
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
