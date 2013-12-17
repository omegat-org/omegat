/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Aaron Madlon-Kay
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

import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Static attributes for text.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public final class Styles {

    public static final Color COLOR_SOURCE = new Color(192, 255, 192);
    public static final Color COLOR_NOTED = new Color(192, 255, 255);
    public static final Color COLOR_UNTRANSLATED = new Color(0xCC, 0xCC, 0xFF);
    public static final Color COLOR_TRANSLATED = new Color(255, 255, 153);

    public static final Color COLOR_NON_UNIQUE = new Color(128, 128, 128);
    public static final Color COLOR_PLACEHOLDER = new Color(150, 150, 150);
    public static final Color COLOR_REMOVETEXT_TARGET = new Color(255, 0, 0);
    public static final Color COLOR_NBSP = new Color(200, 200, 200);
    public static final Color COLOR_WHITESPACE = new Color(128, 128, 128);
    public static final Color COLOR_BIDIMARKERS = new Color(200, 0, 0);
    public static final Color COLOR_MARK_COMES_FROM_TM = new Color(250,128,114); // Salmon red

    /**
     * Construct required attributes set.
     * 
     * Since we need many attributes combinations, it's not good idea to have
     * variable to each attributes set. There is no sense to store created
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
    
    public static AttributeSet createAttributeSet(Color foregroundColor, Color backgroundColor, Boolean bold,
            Boolean italic, Boolean strikethrough, Boolean underline) {
    	
    	MutableAttributeSet r = (MutableAttributeSet) createAttributeSet(foregroundColor, backgroundColor, bold, italic);
    	
    	if (strikethrough != null) {
    		StyleConstants.setStrikeThrough(r, strikethrough);
    	}
    	if (underline != null) {
    		StyleConstants.setUnderline(r, underline);
    	}
    	
    	return r;
    }
}
