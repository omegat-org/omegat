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
import java.util.logging.Logger;

import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.omegat.util.Log;
import org.omegat.util.Preferences;

/**
 * Static attributes for text.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public final class Styles {
    private static final Logger LOGGER = Logger.getLogger(EditorColor.class.getName());

    public enum EditorColor {
    	COLOR_BACKGROUND(UIManager.getColor("TextPane.background")), // Also used for EditorPane.background
    	COLOR_FOREGROUND(UIManager.getColor("TextPane.foreground")),

    	COLOR_SOURCE("#c0ffc0"),
    	COLOR_NOTED("#c0ffff"),
    	COLOR_UNTRANSLATED("#c0c0ff"),
    	COLOR_TRANSLATED("#ffff99"),
    	COLOR_NON_UNIQUE("#808080"),
    	COLOR_PLACEHOLDER("#969696"),
    	COLOR_REMOVETEXT_TARGET("#ff0000"),
    	COLOR_NBSP("#c8c8c8"),
    	COLOR_WHITESPACE("#808080"),
    	COLOR_BIDIMARKERS("#c80000"),
    	COLOR_MARK_COMES_FROM_TM("#fa8072"), // Salmon red
    	COLOR_MARK_COMES_FROM_TM_XICE("#af76df"), // Purple 
    	COLOR_MARK_COMES_FROM_TM_X100PC("#ff948"), // Dark Orange 
    	COLOR_MARK_COMES_FROM_TM_XAUTO("#ffd596"), // Orange
    	COLOR_REPLACE("#0000ff"), // Blue
    	COLOR_LANGUAGE_TOOLS("#0000ff"),
    	COLOR_TRANSTIPS("#0000ff"),
    	COLOR_SPELLCHECK("#ff0000");

    	private Color color;

    	private EditorColor(Color defaultColor) {
    		if (Preferences.existsPreference(this.name()))
    		{
				String prefColor = Preferences.getPreference(this.name());
				
				if (prefColor.equals("__DEFAULT__"))
				{
					color = defaultColor;
				}

    			try {
					color = Color.decode(prefColor);
    			} catch (NumberFormatException e) {
        			Log.logDebug(LOGGER, "Cannot set custom color for {0}, default to {1}.", this.name(), prefColor);
        			color = defaultColor;
        		}
    		}
    		else
    		{
        		color = defaultColor;
    		}
    	}

    	private EditorColor(String defaultColor) {
    		Color color = null;
    		try {
    			color = Color.decode(Preferences.getPreferenceDefault(this.name(), defaultColor));
    		} catch (NumberFormatException e) {
    			Log.logDebug(LOGGER, "Cannot set custom color for {0}, default to {1}.", this.name(), defaultColor);
    			color = Color.decode(defaultColor);
    		}
    		this.color = color;
    	}

    	public String toHex()
    	{
    		return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    	}

    	public Color getColor()
    	{
    		return color;
    	}
    }

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
        else
        {
        	StyleConstants.setForeground(r, EditorColor.COLOR_FOREGROUND.getColor());
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
