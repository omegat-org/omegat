/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
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

package org.omegat.util.gui;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;

import javax.swing.JTextField;

/**
 * A text field that displays customizable hint text when empty and unfocused.
 * 
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class HintTextField extends JTextField {

    private static final Color HINT_FOREGROUND = Color.GRAY;

    private String hintText;
    private Color originalForeground;
    private boolean isDirty;
    private boolean isShowingHint;

    public HintTextField() {
        addFocusListener(new FocusListener() {

            @Override
            public void focusLost(FocusEvent e) {
                if (!isDirty) {
                    showHint();
                }
            }

            @Override
            public void focusGained(FocusEvent e) {
                if (!isDirty) {
                    hideHint();
                }
            }
        });
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
        if (!isDirty) {
            showHint();
        }
    }

    public void clear() {
        showHint();
    }

    public boolean isEmpty() {
        return isShowingHint || getDocument().getLength() == 0;
    }

    @Override
    protected void processComponentKeyEvent(KeyEvent e) {
        if (!isShowingHint) {
            isDirty = getDocument().getLength() > 0;
        }
    }

    private void showHint() {
        setText(hintText);
        originalForeground = getForeground();
        setForeground(HINT_FOREGROUND);
        isDirty = false;
        isShowingHint = true;
    }

    private void hideHint() {
        if (isShowingHint) {
            setText(null);
            setForeground(originalForeground);
            isDirty = false;
            isShowingHint = false;
        }
    }
}
