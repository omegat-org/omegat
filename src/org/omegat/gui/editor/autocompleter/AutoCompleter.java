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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;

import org.omegat.gui.editor.EditorTextArea3;
import org.omegat.gui.editor.TagAutoCompleterView;
import org.omegat.gui.editor.autotext.AutotextAutoCompleterView;
import org.omegat.gui.editor.chartable.CharTableAutoCompleterView;
import org.omegat.gui.glossary.GlossaryAutoCompleterView;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;

/**
 * The controller part of the auto-completer
 * 
 * @author Zoltan Bartko <bartkozoltan@bartkozoltan.com>
 * @author Aaron Madlon-Kay
 */
public class AutoCompleter {    
    
    JPopupMenu popup = new JPopupMenu(); 
    private EditorTextArea3 editor; 
    
    boolean onMac = StaticUtils.onMacOSX();
    
    private boolean visible = false;
    
    /**
     * insert the selected item from here on.
     */
    private int wordChunkStart;
    
    public final static int pageRowCount = 10;
    
    /**
     * a list of the views associated with this auto-completer
     */
    List<AbstractAutoCompleterView> views = new ArrayList<AbstractAutoCompleterView>();
    
    /**
     * the current view
     */
    int currentView = -1;
    
    JScrollPane scroll;
    JLabel viewLabel;
    
    public AutoCompleter(EditorTextArea3 editor) { 
        this.editor = editor; 
        
        scroll = new JScrollPane();
        scroll.setBorder(null);
        scroll.setPreferredSize(new Dimension(200,200));
        scroll.setColumnHeaderView(null);
        scroll.setFocusable(false);
 
        scroll.getVerticalScrollBar().setFocusable( false ); 
        scroll.getHorizontalScrollBar().setFocusable( false ); 
        
        // add any views here
        views.add(new GlossaryAutoCompleterView(this));
        views.add(new AutotextAutoCompleterView(this));
        views.add(new TagAutoCompleterView(this));
        views.add(new CharTableAutoCompleterView(this));

        viewLabel = new JLabel();
        viewLabel.setBorder(new EmptyBorder(4, 4, 4, 4));;
        popup.setBorder(BorderFactory.createLineBorder(Color.black));
        popup.setLayout(new BorderLayout());
        popup.add(scroll, BorderLayout.CENTER); 
        popup.add(viewLabel, BorderLayout.SOUTH);
        selectNextView();
    } 

    public EditorTextArea3 getEditor() {
        return editor;
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
            if (views.get(currentView).processKeys(e, popup.isVisible()))
                return true;
            
            if ((StaticUtils.isKey(e, KeyEvent.VK_ENTER, 0))) {
                popup.setVisible(false); 
                acceptedListItem(getSelectedValue()); 
                setVisible(false);
                return true;
            }
            
            if ((StaticUtils.isKey(e, KeyEvent.VK_INSERT, 0))) {
                acceptedListItem(getSelectedValue()); 
                updatePopup();
                return true;
            }

            if ((StaticUtils.isKey(e, KeyEvent.VK_ESCAPE, 0))) {
                hidePopup();
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
     * Returns the currently selected value.
     * @return 
     */
    private String getSelectedValue() {
        return views.get(currentView).getSelectedValue();
    }
       
    /**
     * Show the popup list.
     */
    public void updatePopup() { 
        if (!isVisible())
            return;
        
        if (editor.isEnabled() && updateViewData() && views.get(currentView).getRowCount()!=0) { 
            scroll.setPreferredSize(new Dimension(
                    scroll.getPreferredSize().width,
                    views.get(currentView).getPreferredHeight()));
            popup.validate();
            popup.pack();
            Point p = getDisplayPoint();
            popup.show(editor, p.x, p.y);
        } else {
            popup.setVisible(false);
        }
        editor.requestFocus(); 
    }
    
    /**
     * Determine the x,y coordinate at which to place the popup.
     */
    private Point getDisplayPoint() {
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
        return new Point(x, y);
    }
    
    /**
     * Update the data of the list based on the text at/before the caret position
     * @return 
     */
    private boolean updateViewData() {
        AbstractAutoCompleterView currentACView = views.get(currentView);
        return currentACView.updateViewData();
    }

    /**
     * Replace the text in the editor with the accepted item.
     * @param selected 
     */
    protected void acceptedListItem(String selected) { 
        if (selected == null || selected.equals(OStrings.getString("AC_NO_SUGGESTIONS"))) {
            return;
        }

        int offset = editor.getCaretPosition();

        if (editor.getSelectionStart() == editor.getSelectionEnd()) {
            editor.setSelectionStart(getWordChunkStart());
            editor.setSelectionEnd(offset);
        }
        editor.replaceSelection(selected);
    }

    /**
     * get the view number of the next view
     * @return the number
     */
    private int nextViewNumber() {
        return (currentView + 1) % views.size();
    }
    
    /**
     * Get the view number of the previous view.
     * @return 
     */
    private int prevViewNumber() {
        return (currentView + views.size() - 1) % views.size();
    }
    
    /**
     * Update the view label
     */
    private void updateViewLabel() {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("<b>");
        sb.append(views.get(currentView).getName());
        sb.append("</b>");
        
        if (views.size() != 1) {
            int modifier = onMac ? KeyEvent.META_MASK : KeyEvent.CTRL_MASK;
            String nextKeyString = keyText(KeyEvent.VK_PAGE_DOWN, modifier);
            String prevKeyString = keyText(KeyEvent.VK_PAGE_UP, modifier);
            
            if (views.size() >= 2) {
                sb.append("<br>");
                sb.append(StaticUtils.format(OStrings.getString("AC_NEXT_VIEW"),
                        nextKeyString,
                        views.get(nextViewNumber()).getName()));
            }
            
            if (views.size() > 2) {
                sb.append("<br>");
                sb.append(StaticUtils.format(OStrings.getString("AC_PREV_VIEW"),
                        prevKeyString,
                        views.get(prevViewNumber()).getName()));
            }
        }
        sb.append("</html>");
        
        viewLabel.setText(sb.toString());
    }

    /** go to the next view */
    private void selectNextView() {
        currentView = nextViewNumber();
        activateView();
    }

    /** activate the current view */
    private void activateView() {
        scroll.setViewportView(views.get(currentView).getViewContent());
        updateViewLabel();
        updatePopup();
    }
    
    /** select the previous view */
    private void selectPreviousView() {
        currentView = prevViewNumber();
        activateView();
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
    
    /** 
     * get the key text
     * @param base
     * @param modifier
     * @return 
     */
    public String keyText(int base, int modifier) {
         return KeyEvent.getKeyModifiersText(modifier) + "+" + KeyEvent.getKeyText(base);
    }

    /**
     * Allow outside actors ({@link AutoCompleteView}s) to adjust the item
     * insertion point according to their needs.
     * @param adjustment An integer added to the current insertion point
     */
    public void adjustInsertionPoint(int adjustment) {
        if (editor.isInActiveTranslation(getWordChunkStart() + adjustment)) {
            setWordChunkStart(getWordChunkStart() + adjustment);
        } else {
            throw new InvalidParameterException("Cannot move the insertion point "
                    + "outside of the active translation area.");
        }
    }

    /**
     * @return the wordChunkStart
     */
    public int getWordChunkStart() {
        return wordChunkStart;
    }

    /**
     * @param wordChunkStart the wordChunkStart to set
     */
    public void setWordChunkStart(int wordChunkStart) {
        this.wordChunkStart = wordChunkStart;
    }
}
