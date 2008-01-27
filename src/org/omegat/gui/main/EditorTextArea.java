/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel and Tiago Saboga
               2007 Zoltan Bartko - bartkozoltan@bartkozoltan.com
               2008 Andrzej Sawula
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.gui.main;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextPane;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;
import javax.swing.undo.UndoManager;
import javax.swing.text.Utilities;
import javax.swing.text.BadLocationException;
import org.omegat.core.matching.SourceTextEntry;
import org.omegat.core.threads.CommandThread;
import org.omegat.core.spellchecker.SpellChecker;
import org.omegat.util.Log;
import org.omegat.util.OConsts;

import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.Token;
import org.omegat.util.gui.ExtendedEditorKit;
import org.omegat.util.gui.Styles;


/**
 * The main panel, where all the translation happens.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Tiago Saboga
 * @author Zoltan Bartko
 * @author Andrzej Sawula
 */
public class EditorTextArea extends JTextPane implements MouseListener, DocumentListener
{
    private MainWindow mw;
    
    /** Creates new form BeanForm */
    public EditorTextArea(MainWindow mainwindow)
    {
        this.mw = mainwindow;
        
        // allow custom editor kit to make custom underlines
        setEditorKit(new ExtendedEditorKit());
        
        DefaultStyledDocument doc = new DefaultStyledDocument(new StyleContext());
        doc.addDocumentListener(this);

        undoManager = new UndoManager();
        doc.addUndoableEditListener(undoManager);
        setDocument(doc);
        setText(OStrings.getString("TF_INTRO_MESSAGE"));

        addMouseListener(this);
    }

    ////////////////////////////////////////////////////////////////////////
    // Managing Undo operations
    ////////////////////////////////////////////////////////////////////////
    
    /** Undo Manager to store edits */
    private UndoManager	undoManager;
    
    /** Orders to cancel all Undoable edits. */
    public synchronized void cancelUndo()
    {
        undoManager.die();
    }
    /** Orders to undo a single edit. */
    public synchronized void undoOneEdit()
    {
        if (undoManager.canUndo())
            undoManager.undo();
    }
    /** Orders to redo a single edit. */
    public synchronized void redoOneEdit()
    {
        if (undoManager.canRedo())
            undoManager.redo();
    }
    
    private static final String IMPOSSIBLE = "Should not have happened, " +     // NOI18N
            "report to http://sf.net/tracker/?group_id=68187&atid=520347";      // NOI18N
    
    ////////////////////////////////////////////////////////////////////////
    // Mouse reaction
    ////////////////////////////////////////////////////////////////////////
    
    /**
     * Reacts to double mouse clicks.
     */
    public synchronized void mouseClicked(MouseEvent e)
    {
        // design-time
        if (mw==null)
            return;
        
        // ignore mouse clicks until document is ready
        if (!mw.m_docReady)
            return;
        
        // the popup menu
        if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
            // any spell checking to be done?
            if (createSpellCheckerPopUp(e.getPoint()))
                        return;
            
            // fall back to go to segment
            if (createGoToSegmentPopUp(e.getPoint()))
                return;
                    }
        
        if (e.getClickCount() == 2)
            {
            goToSegmentAtCaretLocation();
                    }
                }
    // not used now
    public void mouseReleased(MouseEvent e) { }
    public void mousePressed(MouseEvent e)  { }
    public void mouseExited(MouseEvent e)   { }
    public void mouseEntered(MouseEvent e)  { }
    
    
    /** Ctrl key mask. On MacOSX it's CMD key. */
    private static final int CTRL_KEY_MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    
    /** 
     * The key mask of the modifier used for Ctrl+Delete / Ctrl+Backspace.
     * It's Ctrl on a PC, Alt on a Mac
     */
    private static final int CTRL_DEL_MASK = 
            StaticUtils.onMacOSX() ? KeyEvent.ALT_MASK : CTRL_KEY_MASK;
    
    ////////////////////////////////////////////////////////////////////////
    // Keyboard handling to protect text areas
    ////////////////////////////////////////////////////////////////////////
    
    /**
     * "Wrapper" method to allow for triggering translation length
     * recalculation after processing key event, i.e. after performing
     * edit operation.
     * All real key event processing is done by processKeyEventBody
     **/
    
    protected synchronized void processKeyEvent(KeyEvent e)
    {
        processKeyEventBody(e);
        mw.setLengthLabel(" " + Integer.toString(mw.m_sourceDisplayLength) + "/" +
            Integer.toString(((getTextLength() - mw.m_segmentEndInset - OStrings.getSegmentEndMarker().length()) -
            (mw.m_segmentStartOffset + mw.m_sourceDisplayLength + OStrings.getSegmentStartMarker().length()))) + " ");
        return;
    }
    
    /**
     * Monitors key events - need to prevent text insertion
     * outside of edit zone while maintaining normal functionality
     * across jvm versions.
     */
    protected synchronized void processKeyEventBody(KeyEvent e)
    {
        // design-time
        if (mw==null)
            return;
        
        if (!mw.m_projectLoaded)
        {
            if( (e.getModifiers()&CTRL_KEY_MASK)==CTRL_KEY_MASK ||
                    (e.getModifiers()&InputEvent.ALT_MASK)==InputEvent.ALT_MASK )
                super.processKeyEvent(e);
            return;
        }
        
        int keyCode = e.getKeyCode();
        char keyChar = e.getKeyChar();
        
        // let released keys go straight to parent - they should
        //	have no effect on UI and so don't need processing
        if (e.getID() == KeyEvent.KEY_RELEASED)
        {
            // let key releases pass through as well
            // no events should be happening here
            super.processKeyEvent(e);
            return;
        }
        
        // let control keypresses pass through unscathed
        if (e.getID() == KeyEvent.KEY_PRESSED	&&
                (keyCode == KeyEvent.VK_CONTROL	||
                keyCode == KeyEvent.VK_ALT		||
                keyCode == KeyEvent.VK_META		||
                keyCode == KeyEvent.VK_SHIFT))
        {
            super.processKeyEvent(e);
            return;
        }
        
        // for now, force all key presses to reset the cursor to
        //	the editing region unless it's a ctrl-c (copy)
        if( e.getID()==KeyEvent.KEY_PRESSED && e.getModifiers()==CTRL_KEY_MASK && keyCode == KeyEvent.VK_C
            || e.getID() == KeyEvent.KEY_TYPED && e.getModifiers() == CTRL_KEY_MASK && keyChar=='\u0003' )
        {
            // control-c pressed or typed
            super.processKeyEvent(e);
            return;
        }

        // if we've made it here, we have a keypressed or
        //	key-typed event of a (presumably) valid key
        //	and we're in an open project
        // it could still be a keyboard shortcut though
        
        // look for delete/backspace events and make sure they're
        //	in an acceptable area
        switch (keyChar)
        {
            case KeyEvent.VK_BACK_SPACE:
                if (mw.checkCaretForDelete(false))
                {
                    // RFE [ 1579488 ] Ctrl+Backspace
                    if (e.getModifiers() == CTRL_DEL_MASK &&
                            e.getKeyCode()== KeyEvent.VK_BACK_SPACE) 
                    {// e.getKeyCode() == KeyEvent.VK_BACK_SPACE has to be 
                     // retested, otherwise the code is triggered twice   
                        try
                        {
                            int offset = getCaretPosition();
                            int prevWord = Utilities.getPreviousWord(this, offset);
                            int endPrevWord = Utilities.getWordEnd(this, prevWord);
                            int wordBefore = Utilities.getPreviousWord(this, prevWord);
                            int wordBeforeEnd = Utilities.getWordEnd(this, wordBefore);
                            setSelectionEnd(offset);
                            if (endPrevWord != offset &&
                                endPrevWord < offset) 
                            // There's space on the left of the cursor              
                                setSelectionStart(prevWord);
                            else
                                setSelectionStart(wordBeforeEnd);
                            // Check selection is within segment
                            mw.checkCaret(); 
                            // Remove selection
                            replaceSelection("");                               // NOI18N
                            // Swallow key event
                            
                            // check the spelling
                            //checkSpelling(false);
                            return; 
                        }
                        catch(BadLocationException ble)
                        {
                            // Nothing
                        }
                    }
                    else
                        super.processKeyEvent(e);
                }
                
                checkSpelling(false);
                return;
            case KeyEvent.VK_DELETE:
                if (mw.checkCaretForDelete(true))
                {
                    // RFE [ 1579488 ] Ctrl+Delete
                    if (e.getModifiers()== CTRL_DEL_MASK &&
                            e.getKeyCode()== KeyEvent.VK_DELETE) 
                    {// e.getKeyCode() == KeyEvent.VK_DELETE has to be retested,
                     // otherwise the code is triggered twice   
                        try
                        {
                            int offset = getCaretPosition();
                            int nextWord = Utilities.getNextWord(this, offset);
                            int wordEnd = Utilities.getWordEnd(this, offset);
                            setSelectionStart(offset);
                            if (nextWord == wordEnd) 
                            // There's space on the right of the cursor              
                                setSelectionEnd(wordEnd);
                            else
                                setSelectionEnd(nextWord);
                            // Check selection is within segment
                            mw.checkCaret(); 
                            // Remove selection
                            replaceSelection("");                               // NOI18N
                            // Swallow key event
                            
                            // spell checking
                            //checkSpelling(false);
                            return; 
                        }
                        catch(BadLocationException ble)
                        {
                            // Nothing
                        }
                    }
                    else
                        super.processKeyEvent(e);
                }
                
                checkSpelling(false);
                return;
        }
        
        // handling Ctrl+Shift+Home / End manually
        // in order to select only to beginning / to end
        // of the current segment
        // Also hadling HOME and END manually
        // BUGFIX FOR: HOME and END key issues
        //             http://sourceforge.net/support/tracker.php?aid=1228296 
        if( keyCode==KeyEvent.VK_HOME || keyCode==KeyEvent.VK_END )
        {
            // letting parent do the handling
            super.processKeyEvent(e);
            
            // and then "refining" the selection
            mw.checkCaret();
            
            return;
        }
        
        // handling Ctrl+A manually
        // BUGFIX FOR: Select all in the editing field shifts focus
        //             http://sourceforge.net/support/tracker.php?aid=1211826
        // BUGFIX FOR: AltGr+A in Irish deletes all the segment text
        //             http://sourceforge.net/support/tracker.php?aid=1256094
        //             changed the condition to work on ONLY! the Ctrl key pressed
        if( e.getModifiers()==CTRL_KEY_MASK &&
                e.getKeyCode()==KeyEvent.VK_A )
        {
            int start = mw.m_segmentStartOffset + mw.m_sourceDisplayLength +
                    OConsts.segmentStartStringFull.length();
            int end = getTextLength() - mw.m_segmentEndInset -
                    OConsts.segmentEndStringFull.length();
            
            // selecting
            setSelectionStart(start);
            setSelectionEnd(end);
            
            return;
        }
        
        // every other key press should be within the editing zone
        //	so make sure the caret is there
        mw.checkCaret();
        
        // if user pressed enter, see what modifiers were pressed
        //	so determine what to do
        // 18may04 KBG - enable user to hit TAB to advance to next
        //	entry.  some asian IMEs don't swallow enter key resulting
        //	in non-usability
        if (keyCode == KeyEvent.VK_ENTER)
        {
            if (e.isShiftDown())
            {
                // convert key event to straight enter key
                KeyEvent ke = new KeyEvent(e.getComponent(), e.getID(),
                        e.getWhen(), 0, KeyEvent.VK_ENTER, '\n');
                super.processKeyEvent(ke);
            }
            else if (mw.m_advancer != keyCode)
            {
                return;	// swallow event - hopefully IME still works
            }
        }
        
        if (keyCode == mw.m_advancer)
        {
            if (mw.m_advancer == KeyEvent.VK_ENTER)
            {
                // Previous segment shortcut should be CMD+Enter on MacOSX
                // http://sourceforge.net/support/tracker.php?aid=1468315
                if( (e.getModifiers() & CTRL_KEY_MASK)==CTRL_KEY_MASK )
                {
                    // go backwards on control return
                    if (e.getID() == KeyEvent.KEY_PRESSED)
                        mw.doPrevEntry();
                }
                else if (!e.isShiftDown())
                {
                    // return w/o modifiers - swallow event and move on to
                    //  next segment
                    if (e.getID() == KeyEvent.KEY_PRESSED)
                        mw.doNextEntry();
                }
            }
            else if (mw.m_advancer == KeyEvent.VK_TAB)
            {
                // ctrl-tab not caught
                if (e.isShiftDown())
                {
                    // go backwards on control return
                    if (e.getID() == KeyEvent.KEY_PRESSED)
                        mw.doPrevEntry();
                }
                else
                {
                    // return w/o modifiers - swallow event and move on to
                    //  next segment
                    if (e.getID() == KeyEvent.KEY_PRESSED)
                        mw.doNextEntry();
                }
            }
            return;
        }
        
        // need to over-ride default text hiliting procedures because
        //	we're managing caret placement manually
        if (e.isShiftDown())
        {
            // if navigation control, make sure things are hilited
            if (keyCode == KeyEvent.VK_UP				||
                    keyCode == KeyEvent.VK_LEFT		||
                    keyCode == KeyEvent.VK_KP_UP		||
                    keyCode == KeyEvent.VK_KP_LEFT)
            {
                super.processKeyEvent(e);
                if (e.getID() == KeyEvent.KEY_PRESSED)
                {
                    int pos = getCaretPosition();
                    int start = mw.m_segmentStartOffset +
                            mw.m_sourceDisplayLength +
                            OConsts.segmentStartStringFull.length();
                    if (pos < start)
                        moveCaretPosition(start);
                }
            }
            else if (keyCode == KeyEvent.VK_DOWN		||
                    keyCode == KeyEvent.VK_RIGHT		||
                    keyCode == KeyEvent.VK_KP_DOWN	||
                    keyCode == KeyEvent.VK_KP_RIGHT)
            {
                super.processKeyEvent(e);
                if (e.getID() == KeyEvent.KEY_PRESSED)
                {
                    int pos = getCaretPosition();
                    // -1 for space before tag, -2 for newlines
                    int end = getTextLength() - mw.m_segmentEndInset -
                            OConsts.segmentEndStringFull.length();
                    if (pos > end)
                        moveCaretPosition(end);
                }
            }
        }
        
        // shift key is not down
        // if arrow key pressed, make sure caret moves to correct side
        //	of hilite (if text hilited)
        if( !e.isShiftDown() )
        {
            if( keyCode == KeyEvent.VK_UP      ||
                    keyCode == KeyEvent.VK_LEFT    ||
                    keyCode == KeyEvent.VK_KP_UP   ||
                    keyCode == KeyEvent.VK_KP_LEFT )
            {
                int end = getSelectionEnd();
                int start = getSelectionStart();
                if (end != start)
                    setCaretPosition(start);
                else
                    super.processKeyEvent(e);
                mw.checkCaret();
                return;
            }
            else if (keyCode == KeyEvent.VK_DOWN		||
                    keyCode == KeyEvent.VK_RIGHT		||
                    keyCode == KeyEvent.VK_KP_DOWN	||
                    keyCode == KeyEvent.VK_KP_RIGHT)
            {
                int end = getSelectionEnd();
                int start = getSelectionStart();
                if (end != start)
                    setCaretPosition(end);
                else
                    super.processKeyEvent(e);
                mw.checkCaret();
                return;
            }
        }
        
        // no more special handling required
        super.processKeyEvent(e);
        
        // check the spelling at last
        if (!e.isActionKey() && keyCode == KeyEvent.VK_UNDEFINED
                && e.getModifiers() != CTRL_KEY_MASK)
            checkSpelling(false);
        
        
    }

    /**
     * Check the spelling of the words around the caret 
     * (the word the caret is in or, if between words, the word before and the
     * word after.
     *
     * Used with keyboard events which modify the text.
     *
     * @param keycode : the keycode, to prevent multiple passes
     * @param full : if true, the whole segment is checked
     */
    public synchronized void checkSpelling(boolean full) {

        if (!mw.autoSpellCheckingOn())
            return;
                
        try {
            // here we are. Assuming that the caret has already been set 
            // to a position within the edited segment
            int offset = getCaretPosition();

            int start = mw.getTranslationStart();
            int end = mw.getTranslationEnd();
            
            int spellcheckStart;
            int spellcheckEnd;
            
            if (full) {
                spellcheckStart = start;
                spellcheckEnd = end;
            } else {
                // the previous word start and end
                int prevWord = Utilities.getPreviousWord(this, offset);
                int endPrevWord = Utilities.getWordEnd(this, prevWord);

                // the previous next word start and end
                int nextWord = Utilities.getNextWord(this, offset);
                int endNextWord = Utilities.getWordEnd(this, nextWord);
                
                spellcheckStart = (prevWord < start ? start : prevWord);
                spellcheckEnd = (endNextWord > end ? end : endNextWord);
            }

            String spellcheckBase = this.getText(spellcheckStart,
                    spellcheckEnd-spellcheckStart);
            
            // find the tokens
            List tokenList = StaticUtils.tokenizeText(spellcheckBase);
            
            SpellChecker spellchecker = CommandThread.core.getSpellchecker();
            
            AttributeSet attributes;
            AttributeSet correctAttributes = mw.getTranslatedAttributeSet();
            AttributeSet wrongAttributes = 
                    Styles.applyStyles(correctAttributes, Styles.MISSPELLED);
            
            AbstractDocument xlDoc = (AbstractDocument)this.getDocument();
            
            // to make the undo framework work
            xlDoc.removeUndoableEditListener(undoManager);
            
            // first, repaint the whole area as if it were correct
            xlDoc.replace(spellcheckStart, spellcheckEnd-spellcheckStart,
                    spellcheckBase, correctAttributes);
            
            // iterate!
            Iterator tokenListIterator = tokenList.iterator();
            while (tokenListIterator.hasNext()) {
                Token token = (Token) tokenListIterator.next();
                String word = token.getTextFromString(spellcheckBase);
                // is it correct?
                if (!spellchecker.isCorrect(word)) {
                    attributes = wrongAttributes;
                } else {
                    attributes = correctAttributes;
                }
                
                xlDoc.replace(spellcheckStart+token.getOffset(),
                        token.getLength(),
                        word,
                        attributes);
            }
            
            // put the caret position where it belongs to
            this.setCaretPosition(offset);
            
            // put the undo manager back where it was
            xlDoc.addUndoableEditListener(undoManager);
            
        } catch (BadLocationException ble) {
            // so now what?
        }
    }

    ////////////////////////////////////////////////////////////////////////
    // getText().length() caching
    ////////////////////////////////////////////////////////////////////////

    /** Holds the length of the text in the underlying document. */
    int textLength = 0;
    
    /** 
     * Returns the length of the text in the underlying document.
     * <p>
     * This should replace all <code>getText().length()</code> calls,
     * because this method does not count the length (costly operation),
     * instead it accounts document length by listening to document updates.
     */
    public synchronized int getTextLength()
    {
        return textLength;
    }
    
    /** Accounting text length. */
    public synchronized void removeUpdate(javax.swing.event.DocumentEvent e)
    {
        textLength -= e.getLength();
    }

    /** Accounting text length. */
    public synchronized void insertUpdate(javax.swing.event.DocumentEvent e)
    {
        textLength += e.getLength();
    }

    /** Attribute changes do not result in document length changes. Doing nothing. */
    public void changedUpdate(javax.swing.event.DocumentEvent e) { }

    // [ 1743100 ] Pasting with middle mouse button allowed anywhere in editor
    /**
     * When replacing selection, first check caret. And check the spelling of 
     * the whole segment, too.
     */
    public void replaceSelection(String string) 
    {
        mw.checkCaret();
        super.replaceSelection(string);
        checkSpelling(true);
    }

    /**
     * create the spell checker popup menu - suggestions for a wrong word, 
     * add and ignore. Works only for the active segment, for the translation
     * @param point : where should the popup be shown
     */
    private synchronized boolean createSpellCheckerPopUp(Point point) {
        // where is the mouse
        int mousepos = this.viewToModel(point);
        
        if (!mw.autoSpellCheckingOn())
            return false;
        
        if (mousepos < mw.getTranslationStart() || mousepos > mw.getTranslationEnd())
            return false;

        try {    
            // find the word boundaries
            final int wordStart = Utilities.getWordStart(this,mousepos);
            final int wordEnd = Utilities.getWordEnd(this, mousepos);

            final String word = this.getText(wordStart, wordEnd - wordStart);

            SpellChecker spellchecker = CommandThread.core.getSpellchecker();

            final AbstractDocument xlDoc = (AbstractDocument) this.getDocument();
            
            if (!spellchecker.isCorrect(word)) {
                // get the suggestions and create a menu
                ArrayList suggestions = spellchecker.suggest(word);
                
                // create the menu
                popup = new JPopupMenu();
                
                // the suggestions
                for (int i = 0; i < suggestions.size(); i++) {
                    final String replacement = (String) suggestions.get(i);
                    JMenuItem item = popup.add(replacement);
                    item.addActionListener(new ActionListener() {
                            // the action: replace the word with the selected
                            // suggestion
                          public synchronized void actionPerformed(ActionEvent e) {
                            try {
                                int pos = getCaretPosition();
                              xlDoc.replace(wordStart,word.length(),
                                      replacement,mw.getTranslatedAttributeSet());
                              setCaretPosition(pos);
}
                            catch (BadLocationException exc) {
                              System.err.println(exc);
                            }
                          }
                    });
                }
                
                // what if no action is done?
                if (suggestions.size() == 0) {
                    JMenuItem item = popup.add(
                            OStrings.getString("SC_NO_SUGGESTIONS"));
                    item.addActionListener(new ActionListener() {
                          public synchronized void actionPerformed(ActionEvent e) {
                            // just hide the menu
                          }
                    });
                }
                
                popup.add(new JSeparator());
                
                // let us ignore it
                JMenuItem item = popup.add(OStrings.getString("SC_IGNORE_ALL"));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addIgnoreWord(word, wordStart, false);
                    }
                });
                
                // or add it to the dictionary
                item = popup.add(OStrings.getString("SC_ADD_TO_DICTIONARY"));
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        addIgnoreWord(word, wordStart, true);
                    }
                });
                
                popup.show(this, (int) point.getX(), (int) point.getY());
                
            }

        } catch (BadLocationException ex) {
            Log.logRB(IMPOSSIBLE);
            Log.log(ex);
        }
        return true;
    }

    /**
     * add a new word to the spell checker or ignore a word
     * @param word : the word in question
     * @param offset : the offset of the word in the editor
     * @param add : true for add, false for ignore
     */
    private synchronized void addIgnoreWord(String word, int offset, boolean add) {
        SpellChecker spellchecker = CommandThread.core.getSpellchecker();
        
        if (add) {
            spellchecker.learnWord(word);
        } else {
            spellchecker.ignoreWord(word);
        }
        
        // redraw document
        
        AbstractDocument xlDoc = (AbstractDocument) getDocument();
        try {
            // redraw the word in question
            xlDoc.replace(offset, word.length(), word, mw.getTranslatedAttributeSet());
            
            // Replace the errors in the rest of the document
            
            // which is the current segment in the document and what is the length?
            int startOffset = mw.m_segmentStartOffset;
            int currentTrLen = mw.getTranslationEnd() - mw.getTranslationStart();
            int totalLen = mw.m_sourceDisplayLength + OConsts.segmentStartStringFull.length() +
                    currentTrLen + OConsts.segmentEndStringFull.length() + 2;
            
            int localCur = mw.m_curEntryNum - mw.m_xlFirstEntry;
            DocumentSegment docSeg = mw.m_docSegList[localCur];
            docSeg.length = totalLen;
            
            // the segment counter - local
            int localCnt = 0;
            
            // the caret offset in the cycle
            int segOffset = 0;
            
            // iterate through the entries in this file
            for (int i = mw.m_xlFirstEntry; i <= mw.m_xlLastEntry; i++) {
                SourceTextEntry ste = CommandThread.core.getSTE(i);
                if (ste.isTranslated() && 
                        localCnt != localCur) {
                    // only translated and inactive made it
                    int translationStartOffset = segOffset;
                    if (mw.displaySegmentSources()) {
                        // don't forget sources if they are displayed
                        translationStartOffset += ste.getSrcText().length() + 1;
                    }
                    
                    String translation = ste.getTranslation();
                    
                    // is the word in the string?
                    if (translation.indexOf(word) != -1) {
                        // split the text into tokens. If there is a match, redraw it
                        List tokenList = StaticUtils.tokenizeText(translation);
                        for (int j = 0; j < tokenList.size(); j++) {
                            Token token = (Token) tokenList.get(j);
                            String tokenText = token.getTextFromString(translation);
                            // redraw?
                            if (tokenText.equals(word)) {
                                xlDoc.replace(
                                        translationStartOffset + token.getOffset(),
                                        word.length(), word, 
                                        mw.getTranslatedAttributeSet());
                            }
                        }
                    }
                }
                
                // next segment
                segOffset += mw.m_docSegList[localCnt++].length;
            }
            
        } catch (BadLocationException ex) {
            Log.logRB(IMPOSSIBLE);
            Log.log(ex);
        }
    }
    
    /**
     * creates a popup menu for inactive segments - with an item allowing to go 
     * to the given segment.
     */
    private synchronized boolean createGoToSegmentPopUp(Point point) {
        final int mousepos = this.viewToModel(point);
        
        if (mousepos >= mw.getTranslationStart() - OConsts.segmentStartStringFull.length()
            && mousepos <= mw.getTranslationEnd() + OConsts.segmentStartStringFull.length())
            return false;

        popup = new JPopupMenu();

        JMenuItem item = popup.add(OStrings.getString("MW_PROMPT_SEG_NR_TITLE"));
        item.addActionListener(new ActionListener() {
              public synchronized void actionPerformed(ActionEvent e) {
                setCaretPosition(mousepos);
                goToSegmentAtCaretLocation();
              }
        });
        
        popup.show(this, (int) point.getX(), (int) point.getY());
        
        return true;
    }

    /**
     * the spellchecker popup menu
     */
    private JPopupMenu popup;
    
    /**
     * go to the segment specified by the caret position in the editor -
     * this code was in the key event handler previously
     */
    private synchronized void goToSegmentAtCaretLocation() {
        // user double clicked on view pane - goto entry
        // that was clicked
        int pos = getCaretPosition();
        DocumentSegment docSeg;
        int i;
        if (pos < mw.m_segmentStartOffset)
        {
            // before current entry
            int offset = 0;
            for (i=mw.m_xlFirstEntry; i<mw.m_curEntryNum; i++)
            {
                docSeg = mw.m_docSegList[i-mw.m_xlFirstEntry];
                offset += docSeg.length;
                if (pos < offset)
                {
                    mw.doGotoEntry(i+1);
                    return;
                }
            }
        }
        else if (pos > getTextLength() - mw.m_segmentEndInset)
        {
            // after current entry
            int inset = getTextLength() - mw.m_segmentEndInset;
            for (i=mw.m_curEntryNum+1; i<=mw.m_xlLastEntry; i++)
            {
                docSeg = mw.m_docSegList[i-mw.m_xlFirstEntry];
                inset += docSeg.length;
                if (pos <= inset)
                {
                    mw.doGotoEntry(i+1);
                    return;
                }
            }
        }
    }
    
    /** lower case */
    public final static int CASE_LOWER = 1;
    /** title case */
    public final static int CASE_TITLE = 2;
    /** upper case */
    public final static int CASE_UPPER = 3;
    
    /** cycle between cases */
    public final static int CASE_CYCLE = 0;
    
    /**
     * change case of the selected text or if none is selected, of the current 
     * word
     * @param toWhat : one of the CASE_* constants
     */
    public synchronized void changeCase(int toWhat) {
        int start = getSelectionStart();
        int end = getSelectionEnd();
        
        int caretPosition = getCaretPosition();
        
        int translationStart = mw.getTranslationStart();
        int translationEnd = mw.getTranslationEnd();
        
        // both should be within the limits
        if (end < translationStart || start > translationEnd)
            return;     // forget it, not worth the effort
        
        // adjust the bound which exceeds the limits
        if (start < translationStart && end <= translationEnd)
            start = translationStart;
        
        if (end > translationEnd && start >= translationStart)
            end = translationEnd;
        
        try {
            // no selection? make it the current word
            if (start == end) {
                start = Utilities.getWordStart(this, start);
                end = Utilities.getWordEnd(this, end);
            }
            
            setSelectionStart(start);
            setSelectionEnd(end);
            
            String selectionText = this.getText(start, end - start);
            // tokenize the selection
            List tokenList = StaticUtils.tokenizeText(selectionText);
            
            StringBuffer buffer = new StringBuffer(selectionText);
            
            if (toWhat == CASE_CYCLE) {
                int lower = 0;
                int upper = 0;
                int title = 0;
                int other = 0;

                for (int i = 0; i < tokenList.size(); i++) {
                    Token token = (Token) tokenList.get(i);
                    String word = token.getTextFromString(selectionText);
                    if (isLowerCase(word)) {
                        lower++;
                        continue;
                    }
                    if (isTitleCase(word)) {
                        title++;
                        continue;
                    }
                    if (isUpperCase(word)) {
                        upper++;
                        continue;
                    }
                    other++;
                }

                if (lower == 0 && title == 0 && upper == 0 && other == 0)
                    return; // nothing to do here

                if (lower != 0 && title == 0 && upper == 0)
                    toWhat = CASE_TITLE;
                
                if (lower == 0 && title != 0 && upper == 0)
                    toWhat = CASE_UPPER;
                
                if (lower == 0 && title == 0 && upper != 0)
                    toWhat = CASE_LOWER;
                
                if (other != 0)
                    toWhat = CASE_UPPER;
            }
           
            int lengthIncrement = 0;
            
            for (int i = 0; i < tokenList.size(); i++) {
                // find out the case and change to the selected 
                Token token = (Token) tokenList.get(i);
                String result = doChangeCase(
                        token.getTextFromString(selectionText), toWhat);
                
                // replace this token
                buffer.replace(token.getOffset() + lengthIncrement, 
                        token.getLength() + token.getOffset() + lengthIncrement,
                        result);
                
                lengthIncrement += result.length() - token.getLength();
            }
            
            // ok, write it back to the editor document
            replaceSelection(buffer.toString());
            
            setCaretPosition(caretPosition);
            
            setSelectionStart(start);
            setSelectionEnd(end);
            
        } catch (BadLocationException ble) {
            // highly improbable
            Log.log("bad location exception when changing case");
            Log.log(ble);
        }
    }
    
    /**
     * perform the case change. Lowercase becomes titlecase, titlecase becomes 
     * uppercase, uppercase becomes lowercase. if the text matches none of these 
     * categories, it is uppercased.
     * @param input : the string to work on
     * @param toWhat: one of the CASE_* values - except for case CASE_CYCLE.
     */
    private String doChangeCase(String input, int toWhat) {
        Locale locale = CommandThread.core
                .getProjectProperties().getTargetLanguage().getLocale();
        
        switch (toWhat) {
            case CASE_LOWER: return input.toLowerCase(locale);
            case CASE_UPPER: return input.toUpperCase(locale);
            // TODO: find out how to get a locale-aware title case
            case CASE_TITLE: return Character.toTitleCase(input.charAt(0)) 
                    + input.substring(1).toLowerCase(locale);
        }
        // if everything fails
        return input.toUpperCase(locale);
    }
    
    /**
     * returns true if the input is lowercase
     */
    private boolean isLowerCase(String input) {
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (Character.isLetter(current) && !Character.isLowerCase(current))
                return false;
        }
        return true;
    }
    
    /**
     * Returns true if the input is upper case
     */
    private boolean isUpperCase(String input) {
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            if (Character.isLetter(current) && !Character.isUpperCase(current))
                return false;
        }
        return true;
    }
    
    /**
     * returns true if the input is title case
     */
    private boolean isTitleCase(String input) {
        if (input.length() > 1)
            return Character.isTitleCase(input.charAt(0)) 
                && isLowerCase(input.substring(1));
        else
            return Character.isTitleCase(input.charAt(0));
    }
}
