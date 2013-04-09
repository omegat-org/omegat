/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey
               2006 Henry Pijffers
               2009-2012 Didier Briel
               2013 Aaron Madlon-Kay
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

package org.omegat.gui.search;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import javax.swing.text.StringContent;
import javax.swing.undo.UndoManager;

import org.omegat.util.StaticUtils;

/**
 * "Default text" feature inspired by http://stackoverflow.com/a/1739037/448068
 *  
 *  @author Keith Godfrey
 *  @author Henry Pijffers (henry.pijffers@saxnot.com)
 *  @author Didier Briel
 *  @author Aaron Madlon-Kay
 */
public class MFindField extends JTextField implements FocusListener {
    public MFindField() {
        // Handle undo (CtrlCmd+Z);
        KeyStroke undo = StaticUtils.onMacOSX() ? KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                InputEvent.META_MASK, false) : KeyStroke.getKeyStroke(KeyEvent.VK_Z,
                InputEvent.CTRL_MASK, false);
        Action undoAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                undo();
            }
        };
        getInputMap().put(undo, "UNDO");
        getActionMap().put("UNDO", undoAction);

        // Handle redo (CtrlCmd+Y);
        KeyStroke redo = StaticUtils.onMacOSX() ? KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                InputEvent.META_MASK, false) : KeyStroke.getKeyStroke(KeyEvent.VK_Y,
                InputEvent.CTRL_MASK, false);
        Action redoAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                redo();
            }
        };
        getInputMap().put(redo, "REDO");
        getActionMap().put("REDO", redoAction);
        
        defaultText = null;
        normalFont = getFont();
        defaultTextFont = normalFont.deriveFont(Font.ITALIC);
        normalColor = getForeground();
        isDirty = false;
        addFocusListener(this);
    }

    public void setDefaultText(String text) {
        defaultText = text;
        if (getText().length() == 0) {
            showDefaultText();
        }
    }
    
    private void showDefaultText() {
        setText(defaultText);
        setFont(defaultTextFont);
        setForeground(getDisabledTextColor());
        isDirty = false;
    }

    @Override
    protected Document createDefaultModel() {
        PlainDocument doc = new PlainDocument(new StringContent());
        // doc.addDocumentListener(this);
        undoManager = new UndoManager();
        doc.addUndoableEditListener(undoManager);
        return doc;
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.getID() == KeyEvent.KEY_PRESSED) {
            if (!getText().equals(""))
                enterActionListener.actionPerformed(null); // doSearch()
        } else {
            super.processKeyEvent(e);
        }
        if (e.getID() == KeyEvent.KEY_TYPED) {
            isDirty = true;
        }
    }

    protected void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    protected void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    public void focusGained(FocusEvent e) {
        if (defaultText != null && isEditable() && getText().length() == 0) {
            setText("");
        }
    }

    public void focusLost(FocusEvent e) {
        if (defaultText != null && isEditable() && getText().length() == 0) {
            showDefaultText();
        }
    }

    @Override
    public String getText() {
        String content = super.getText();
        if (!isDirty && defaultText != null && content.equals(defaultText)) {
            return "";
        }
        return content;
    }
    
    @Override
    public void setText(String t) {
        setFont(normalFont);
        setForeground(normalColor);
        if (t.length() > 0) {
            isDirty = true;
        }
        super.setText(t);
    }

    private UndoManager undoManager;
    private Font normalFont;
    private Font defaultTextFont;
    private Color normalColor;
    private String defaultText;
    private boolean isDirty;
    ActionListener enterActionListener;
}
