/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel and Tiago Saboga
               2007 Zoltan Bartko - bartkozoltan@bartkozoltan.com
               2008 Andrzej Sawula, Alex Buloichik
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

package org.omegat.gui.editor;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyleContext;
import javax.swing.text.Utilities;
import javax.swing.undo.UndoManager;

import org.omegat.core.Core;
import org.omegat.gui.main.MainWindow;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.ExtendedEditorKit;


/**
 * The main panel, where all the translation happens.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Tiago Saboga
 * @author Zoltan Bartko
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class EditorTextArea extends JTextPane implements MouseListener, DocumentListener
{
    private MainWindow mw;
    protected EditorController controller;
    
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
    protected UndoManager undoManager;
    
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
        if (!controller.m_docReady)
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
        String msg = " " + Integer.toString(controller.m_sourceDisplayLength) + "/" +
            Integer.toString(((getTextLength() - controller.m_segmentEndInset - OStrings.getSegmentEndMarker().length()) -
            (controller.m_segmentStartOffset + controller.m_sourceDisplayLength + OStrings.getSegmentStartMarker().length()))) + " ";
        Core.getMainWindow().showLengthMessage(msg);
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
                if (Core.getEditor().checkCaretForDelete(false))
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
                            Core.getEditor().checkCaret(); 
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
                
                controller.checkSpelling(false);
                return;
            case KeyEvent.VK_DELETE:
                if (Core.getEditor().checkCaretForDelete(true))
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
                            Core.getEditor().checkCaret(); 
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
                
                controller.checkSpelling(false);
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
            Core.getEditor().checkCaret();
            
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
            int start = controller.m_segmentStartOffset + controller.m_sourceDisplayLength +
                    OConsts.segmentStartStringFull.length();
            int end = getTextLength() - controller.m_segmentEndInset -
                    OConsts.segmentEndStringFull.length();
            
            // selecting
            setSelectionStart(start);
            setSelectionEnd(end);
            
            return;
        }
        
        // every other key press should be within the editing zone
        //	so make sure the caret is there
        Core.getEditor().checkCaret();
        
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
                        Core.getEditor().prevEntry();
                }
                else if (!e.isShiftDown())
                {
                    // return w/o modifiers - swallow event and move on to
                    //  next segment
                    if (e.getID() == KeyEvent.KEY_PRESSED)
                        Core.getEditor().nextEntry();
                }
            }
            else if (mw.m_advancer == KeyEvent.VK_TAB)
            {
                // ctrl-tab not caught
                if (e.isShiftDown())
                {
                    // go backwards on control return
                    if (e.getID() == KeyEvent.KEY_PRESSED)
                        Core.getEditor().prevEntry();
                }
                else
                {
                    // return w/o modifiers - swallow event and move on to
                    //  next segment
                    if (e.getID() == KeyEvent.KEY_PRESSED)
                        Core.getEditor().nextEntry();
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
                    int start = controller.m_segmentStartOffset +
                    controller.m_sourceDisplayLength +
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
                    int end = getTextLength() - controller.m_segmentEndInset -
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
                Core.getEditor().checkCaret();
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
                Core.getEditor().checkCaret();
                return;
            }
        }
        
        // no more special handling required
        super.processKeyEvent(e);
        
        // check the spelling at last
        if (!e.isActionKey() && keyCode == KeyEvent.VK_UNDEFINED
                && e.getModifiers() != CTRL_KEY_MASK)
            controller.checkSpelling(false);
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
        Core.getEditor().checkCaret();
        super.replaceSelection(string);
        controller.checkSpelling(true);
    }

    /**
     * create the spell checker popup menu - suggestions for a wrong word, 
     * add and ignore. Works only for the active segment, for the translation
     * @param point : where should the popup be shown
     */
    private synchronized boolean createSpellCheckerPopUp(Point point) {
        if (!mw.autoSpellCheckingOn())
            return false;
        
        return EditorSpellChecking.createSpellCheckerPopUp(point, controller, this);
    }

    /**
     * creates a popup menu for inactive segments - with an item allowing to go 
     * to the given segment.
     */
    private synchronized boolean createGoToSegmentPopUp(Point point) {
        final int mousepos = this.viewToModel(point);
        
        if (mousepos >= controller.getTranslationStart() - OConsts.segmentStartStringFull.length()
            && mousepos <= controller.getTranslationEnd() + OConsts.segmentStartStringFull.length())
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
    protected JPopupMenu popup;
    
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
        if (pos < controller.m_segmentStartOffset)
        {
            // before current entry
            int offset = 0;
            for (i=controller.m_xlFirstEntry; i<controller.m_curEntryNum; i++)
            {
                docSeg = controller.m_docSegList[i-controller.m_xlFirstEntry];
                offset += docSeg.length;
                if (pos < offset)
                {
                    Core.getEditor().gotoEntry(i+1);
                    return;
                }
            }
        }
        else if (pos > getTextLength() - controller.m_segmentEndInset)
        {
            // after current entry
            int inset = getTextLength() - controller.m_segmentEndInset;
            for (i=controller.m_curEntryNum+1; i<=controller.m_xlLastEntry; i++)
            {
                docSeg = controller.m_docSegList[i-controller.m_xlFirstEntry];
                inset += docSeg.length;
                if (pos <= inset)
                {
                    Core.getEditor().gotoEntry(i+1);
                    return;
                }
            }
        }
    }
}
