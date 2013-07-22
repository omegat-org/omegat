/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko, Aaron Madlon-Kay
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.text.BadLocationException;

import org.omegat.tokenizer.ITokenizer;
import org.omegat.gui.editor.EditorTextArea3;
import org.omegat.gui.editor.TagAutoCompleterView;
import org.omegat.gui.glossary.GlossaryAutoCompleterView;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.TagUtil;
import org.omegat.util.Token;

/**
 * The controller part of the auto-completer
 * 
 * @author Zoltan Bartko <bartkozoltan@bartkozoltan.com>
 * @author Aaron Madlon-Kay
 */
public class AutoCompleter {
    ListModel listModel = new DefaultListModel();
    
    JList list = new JList(); 
    JPopupMenu popup = new JPopupMenu(); 
    EditorTextArea3 editor; 
    
    boolean onMac = StaticUtils.onMacOSX();
    
    private boolean visible = false;
    
    /**
     * insert the selected item from here on.
     */
    int wordChunkStart;
    
    /**
     * a list of the views associated with this auto-completer
     */
    List<AutoCompleterView> views = new ArrayList<AutoCompleterView>();
    
    /**
     * the current view
     */
    int currentView = 0;
    
    JLabel viewLabel;
    
    public AutoCompleter(EditorTextArea3 editor) { 
        // add any views here
        views.add(new GlossaryAutoCompleterView(this));
        views.add(new TagAutoCompleterView(this));
        
        this.editor = editor; 
        
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(null);
 
        list.setFocusable( false ); 
        scroll.getVerticalScrollBar().setFocusable( false ); 
        scroll.getHorizontalScrollBar().setFocusable( false ); 
        
        viewLabel = new JLabel();
        updateViewLabel();
 
        popup.setBorder(BorderFactory.createLineBorder(Color.black)); 
        popup.add(scroll); 
        popup.add(viewLabel);
    } 
    
    /**
     * Process the autocompletion keys
     * @param e the key event to process
     * @return true if a key has been processed, false if otherwise.
     */
    public boolean processKeys(KeyEvent e) {
        
        if (!isVisible() && ((!onMac && StaticUtils.isKey(e, KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK))
                || (onMac && StaticUtils.isKey(e, KeyEvent.VK_ESCAPE, 0)))) {

            if (!editor.isInActiveTranslation(editor.getCaretPosition())) {
                return false;
            }

            setVisible(true);
            
            if (!popup.isVisible()) {
                updatePopup();
            }
            return true;
        }
        
        if (isVisible()) {
            if (StaticUtils.isKey(e, KeyEvent.VK_UP, 0)) {
                // process key UP
                if (popup.isVisible()) 
                    selectPreviousPossibleValue();
                return true;
            } 

            if (StaticUtils.isKey(e, KeyEvent.VK_DOWN, 0)) {
                // process key DOWN
                if (popup.isVisible()) 
                    selectNextPossibleValue(); 
                return true;
            } 

            if ((StaticUtils.isKey(e, KeyEvent.VK_ENTER, 0))) {
                // process key ENTER
                popup.setVisible(false); 
                acceptedListItem((String)list.getSelectedValue()); 
                setVisible(false);
                return true;
            }
            
            if ((StaticUtils.isKey(e, KeyEvent.VK_INSERT, 0))) {
                acceptedListItem((String)list.getSelectedValue()); 
                updatePopup();
                return true;
            }

            if ((StaticUtils.isKey(e, KeyEvent.VK_ESCAPE, 0))) {
                // process key ESCAPE
                hidePopup();
                return true;
            }

            if (StaticUtils.isKey(e,KeyEvent.VK_PAGE_UP, 0)) {
            if (popup.isVisible()) {
                    selectPreviousPossibleValueByPage();
                }
                return true;
            }
    
            if (StaticUtils.isKey(e,KeyEvent.VK_PAGE_DOWN, 0)) {
                if (popup.isVisible()) 
                    selectNextPossibleValueByPage(); 
                return true;
            }
            
            if ((!onMac && StaticUtils.isKey(e, KeyEvent.VK_PAGE_UP, KeyEvent.CTRL_MASK))
                    || (onMac && StaticUtils.isKey(e, KeyEvent.VK_PAGE_UP, KeyEvent.META_MASK))) {
                if (popup.isVisible()) {
                    selectPreviousView();
                }
                return true;
            }
            
            if ((!onMac && StaticUtils.isKey(e, KeyEvent.VK_SPACE, KeyEvent.CTRL_MASK))
                    || (onMac && StaticUtils.isKey(e, KeyEvent.VK_SPACE, KeyEvent.META_MASK))
                    || (!onMac && StaticUtils.isKey(e, KeyEvent.VK_PAGE_DOWN, KeyEvent.CTRL_MASK))
                    || (onMac && StaticUtils.isKey(e, KeyEvent.VK_PAGE_DOWN, KeyEvent.META_MASK))) {
                if (popup.isVisible()) {
                    selectNextView();
                }
                return true;
            }
        }
        
        // otherwise
        return false;
    }

    /**
     * hide the popup
     */
    public void hidePopup() {
        setVisible(false);
        popup.setVisible(false); 
    }
    
    /** 
     * Selects the next item in the list.
     */ 
    protected void selectNextPossibleValue() { 
        int i = (list.getSelectedIndex() + 1) % list.getModel().getSize();
        list.setSelectedIndex(i);
        list.ensureIndexIsVisible(i);
    }

    /** 
     * Selects the item in the list following the current one by one page or go to the last item. 
     */ 
    protected void selectNextPossibleValueByPage() { 
        int page = list.getLastVisibleIndex() - list.getFirstVisibleIndex();
        int i = Math.min(list.getSelectedIndex() + page, list.getModel().getSize() - 1);
        list.setSelectedIndex(i);
        list.ensureIndexIsVisible(i);
    }

    /** 
     * Selects the previous item in the list.
     */ 
    protected void selectPreviousPossibleValue() {
        int s = list.getModel().getSize();
        int i = (list.getSelectedIndex() - 1 + s) % s;
        list.setSelectedIndex(i);
        list.ensureIndexIsVisible(i);
    } 
    
    /** 
     * Selects the item in the list preceding the current one by one page or go to the first item.
     */ 
    protected void selectPreviousPossibleValueByPage() { 
        int page = list.getLastVisibleIndex() - list.getFirstVisibleIndex();
        int i = Math.max(list.getSelectedIndex() - page, 0);
        list.setSelectedIndex(i);
        list.ensureIndexIsVisible(i);
    }
    
    /**
     * Show the popup list.
     */
    public void updatePopup() { 
        if (!isVisible())
            return;
        
        popup.setVisible(false); 
        
        if (editor.isEnabled() && updateListData() && list.getModel().getSize()!=0) { 
            int size = list.getModel().getSize(); 
            list.setVisibleRowCount(size<10 ? size : 10); 

            int x = 0; 
            int y = editor.getHeight();
            int fontSize = editor.getFont().getSize();
            try { 
                int pos = Math.min(editor.getCaret().getDot(), editor.getCaret().getMark()); 
                x = editor.getUI().modelToView(editor, pos).x; 
                y = editor.getUI().modelToView(editor, editor.getCaret().getDot()).y
                        + fontSize;
            } catch(BadLocationException e) { 
                // this should never happen!!! 
                Log.log(e);
            }
            updateViewLabel();
            popup.show(editor, x, y);
            list.ensureIndexIsVisible(list.getSelectedIndex());
        } else {
            popup.setVisible(false);
        }
        editor.requestFocus(); 
    }
    
    /**
     * Update the data of the list based on the text at/before the caret position
     * @return 
     */
    private boolean updateListData() {
        try {
            AutoCompleterView currentACView = views.get(currentView);
            int offset = editor.getCaretPosition();
            int translationStart = editor.getOmDocument().getTranslationStart();
            
            // init - these are going to be overwritten, if something better is found.
            String wordChunk = "";
            wordChunkStart = offset;
            
            String prevText = editor.getDocument().getText(translationStart, offset - translationStart);
            
            if (prevText.length() != 0) {
                ITokenizer tokenizer = currentACView.getTokenizer();
                Token[] tokens = tokenizer.tokenizeAllExactly(prevText);
                
                if (tokens.length != 0) {
                    Token lastToken = tokens[tokens.length - 1];
                    String lastString = prevText.substring(lastToken.getOffset()).trim();
                    if (lastString.length() > 0 && !TagUtil.getAllTagsInSource().contains(lastString)) {
                        wordChunk = lastString;
                        wordChunkStart = translationStart + lastToken.getOffset();
                    }
                }
            }
            
            List<String> entryList = currentACView.computeListData(wordChunk);
            
            if (entryList.isEmpty()) {
                entryList.add(OStrings.getString("AC_NO_SUGGESTIONS"));
            }
            list.setListData(entryList.toArray());
            if (!entryList.isEmpty())
                list.setSelectedIndex(0);
            
            return !entryList.isEmpty();
        } catch (BadLocationException ex) {
            // what now?
            return false;
        }
    }

    /**
     * Replace the text in the editor with the accepted item.
     * @param selected 
     */
    protected void acceptedListItem(String selected) { 
        if (selected==null || selected.equals(OStrings.getString("AC_NO_SUGGESTIONS"))) 
            return; 
        
        if (editor.getSelectionStart() == editor.getSelectionEnd()) {
            editor.setSelectionStart(wordChunkStart);
            editor.setSelectionEnd(editor.getCaretPosition());
        }
        editor.replaceSelection(selected);
    }

    private int nextViewNumber() {
        return (currentView + 1) % views.size();
    }
    
    private int prevViewNumber() {
        return (currentView - 1 + views.size()) % views.size();
    }
    
    private void updateViewLabel() {
        StringBuilder sb = new StringBuilder(OStrings.getString("AC_LABEL_START"));
        sb.append(StaticUtils.format(OStrings.getString("AC_THIS_VIEW"),
                views.get(currentView).getName()));
        
        if (views.size() != 1) {
            int modifier = onMac ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK;
            String nextKeyString = keyText(KeyEvent.VK_PAGE_DOWN, modifier);
            String prevKeyString = keyText(KeyEvent.VK_PAGE_UP, modifier);
            
            if (views.size() >= 2) {
            sb.append(StaticUtils.format(OStrings.getString("AC_NEXT_VIEW"),
                    nextKeyString,
                    views.get(nextViewNumber()).getName()));
            }
            
            if (views.size() > 2) {
            sb.append(StaticUtils.format(OStrings.getString("AC_PREV_VIEW"),
                    prevKeyString,
                    views.get(prevViewNumber()).getName()));
            }
        }
        sb.append(OStrings.getString("AC_LABEL_END"));
        
        viewLabel.setText(sb.toString());
        viewLabel.setPreferredSize(new Dimension(300,50));
    }

    private void selectNextView() {
        currentView = nextViewNumber();
        updatePopup();
    }

    private void selectPreviousView() {
        currentView = prevViewNumber();
        
        updatePopup();
    }

    /**
     * @return the autoCompleterVisible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param autoCompleterVisible the autoCompleterVisible to set
     */
    public void setVisible(boolean autoCompleterVisible) {
         this.visible = autoCompleterVisible;
    }
    
    public String keyText(int base, int modifier) {
         return KeyEvent.getKeyModifiersText(modifier) + "+" + KeyEvent.getKeyText(base);
    }

    /**
     * Allow outside actors ({@link AutoCompleteView}s) to adjust the item
     * insertion point according to their needs.
     * @param adjustment An integer added to the current insertion point
     */
    public void adjustInsertionPoint(int adjustment) {
        if (editor.isInActiveTranslation(wordChunkStart + adjustment)) {
            wordChunkStart += adjustment;
        } else {
            throw new InvalidParameterException("Cannot move the insertion point "
                    + "outside of the active translation area.");
        }
    }
}
