/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

package org.omegat.gui.properties;

import java.awt.Graphics;

import javax.swing.JTextPane;

/**
 * A text pane that can flash its background.
 *
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class FlashingTextArea extends JTextPane {
    private FlashColorInterpolator flasher;

    public void flash() {
        flasher = new FlashColorInterpolator();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (flasher != null && flasher.isFlashing()) {
            flasher.mark();
            setBackground(flasher.getColor());
            repaint();
        }
    }
}
