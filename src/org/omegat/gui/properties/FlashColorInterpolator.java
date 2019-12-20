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

import java.awt.Color;

import org.omegat.util.gui.Styles;

/**
 * A class that interpolates between a given min and max color, according to the formula
 * <code>-4x^2 + 4x</code>.
 *
 * @author Aaron Madlon-Kay
 */
public class FlashColorInterpolator {
    private static final double DEFAULT_DURATION = 300d;

    private final double flashDuration;
    private final long startTime;
    private volatile long mark;

    private final Color colorMin;
    private final Color colorMax;

    public FlashColorInterpolator() {
        this(DEFAULT_DURATION, Styles.EditorColor.COLOR_NOTIFICATION_MIN.getColor(),
                Styles.EditorColor.COLOR_NOTIFICATION_MAX.getColor());
    }

    public FlashColorInterpolator(double duration, Color colorMin, Color colorMax) {
        this.flashDuration = duration;
        this.startTime = System.currentTimeMillis();
        this.colorMin = colorMin;
        this.colorMax = colorMax;
    }

    public void mark() {
        mark = System.currentTimeMillis();
    }

    public boolean isFlashing() {
        return mark - startTime <= flashDuration;
    }

    private double getIntensity(long elapsed) {
        double x = elapsed / flashDuration;
        return -4 * x * x + 4 * x;
    }

    public Color getColor() {
        long elapsed = mark - startTime;
        if (elapsed >= flashDuration) {
            return colorMin;
        }
        double intensity = getIntensity(elapsed);
        double r = colorMin.getRed() + (colorMax.getRed() - colorMin.getRed()) * intensity;
        double g = colorMin.getGreen() + (colorMax.getGreen() - colorMin.getGreen()) * intensity;
        double b = colorMin.getBlue() + (colorMax.getBlue() - colorMin.getBlue()) * intensity;
        return new Color((int) r, (int) g, (int) b);
    }
}
