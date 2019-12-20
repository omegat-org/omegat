/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Aaron Madlon-Kay
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

    protected String hintText;
    protected Color originalForeground;
    protected boolean isDirty;
    protected boolean isShowingHint;

    public HintTextField() {
        originalForeground = getForeground();
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
        super.setText(hintText);
        applyHintStyle();
        isDirty = false;
        isShowingHint = true;
    }

    protected void applyHintStyle() {
        setForeground(getDisabledTextColor());
    }

    private void hideHint() {
        super.setText(null);
        restoreNormalStyle();
        isDirty = false;
        isShowingHint = false;
    }

    protected void restoreNormalStyle() {
        setForeground(originalForeground);
    }

    @Override
    public String getText() {
        return isShowingHint ? "" : super.getText();
    }

    @Override
    public void setText(String t) {
        if (t.isEmpty() && !hasFocus()) {
            showHint();
        } else {
            hideHint();
            super.setText(t);
            isDirty = true;
        }
    }
}
