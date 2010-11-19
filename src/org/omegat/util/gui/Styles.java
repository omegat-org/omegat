/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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

package org.omegat.util.gui;

import java.awt.Color;

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Static attributes for text.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class Styles {

    public static final Color COLOR_GREEN = new Color(192, 255, 192);
    public static final Color COLOR_UNTRANSLATED = new Color(0xCC, 0xCC, 0xFF);
    public static final Color COLOR_TRANSLATED = new Color(255, 255, 153);

    public static final Color COLOR_LIGHT_GRAY = new Color(128, 128, 128);

    /**
     * Construct required attributes set.
     * 
     * Since we need many attributes combinations, it's not good idea to have
     * variable to each attributes set. There is no sence to store created
     * attributes in the cache, because calculate hash for cache require about
     * 2-3 time more than just create attributes set from scratch.
     * 
     * 1000000 attributes creation require about 305 ms - it's enough fast.
     */
    public static AttributeSet createAttributeSet(Color foregroundColor, Color backgroundColor, Boolean bold,
            Boolean italic) {
        MutableAttributeSet r = new SimpleAttributeSet();
        if (foregroundColor != null) {
            StyleConstants.setForeground(r, foregroundColor);
        }
        if (backgroundColor != null) {
            StyleConstants.setBackground(r, backgroundColor);
        }
        if (bold != null) {
            StyleConstants.setBold(r, bold);
        }
        if (italic != null) {
            StyleConstants.setItalic(r, italic);
        }
        return r;
    }
}
