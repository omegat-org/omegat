/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2022 Hiroshi Miura
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

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.Timer;

public class SpinIcon implements Icon {
    private final Icon spinIcon;
    private final int width;
    private final int height;
    private double angle = 0;
    private final Timer motionTimer;

    public SpinIcon(final JComponent component) {
        spinIcon = new ImageIcon("src/org/omegat/gui/resources/loading.png");
        width = spinIcon.getIconWidth();
        height = spinIcon.getIconHeight();
        motionTimer = new Timer(100, e -> {
            angle = angle + 10;
            if (angle == 360) {
                angle = 0;
            }
            component.repaint();
        });
        motionTimer.setRepeats(false);
        motionTimer.start();
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        motionTimer.stop();
        Graphics2D g2 = (Graphics2D) g.create();
        int xCenter = width / 2;
        int yCenter = height / 2;
        Rectangle r = new Rectangle(x, y, width, height);
        g2.setClip(r);
        AffineTransform original = g2.getTransform();
        AffineTransform at = new AffineTransform();
        at.concatenate(original);
        at.rotate(Math.toRadians(angle), x + xCenter, y + yCenter);
        g2.setTransform(at);
        spinIcon.paintIcon(c, g2, x, y);
        g2.setTransform(original);
        motionTimer.start();
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
