/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
 Portions copyright 2007 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
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
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.omegat.gui.editor.ViewLabel;

/**
 * Static attributes for text.
 *
 * @author Maxym Mykhalchuk
 */
public final class Styles
{
    private Styles() { }
    
    /** Plain text. */
    public final static MutableAttributeSet PLAIN;
    /** Bold text. */
    public final static MutableAttributeSet BOLD;
    /** Bold text on light green background. */
    public final static MutableAttributeSet GREEN;
    
    /** translated text */
    public final static MutableAttributeSet TRANSLATED;
    
    /** untranslated text */
    public final static MutableAttributeSet UNTRANSLATED;
    
    /** misspelled text */
    public final static MutableAttributeSet MISSPELLED;
    
    /** Disabled, i.e grayed out text. */
    public final static MutableAttributeSet DISABLED;
    
    /** 
     * Extra text in fuzzy match that is not there in source segment.
     * Blue by default.
     */
    public final static MutableAttributeSet TEXT_EXTRA;
    /** 
     * Borderline text of fuzzy match that is there in source segment, but 
     * on the left or right there's some text that is missing from source segment. 
     * Green by default.
     */
    public final static MutableAttributeSet TEXT_BORDER;
    
    static
    {
        UIDefaults uidefaults = UIManager.getDefaults();
        
        PLAIN = new SimpleAttributeSet();
        StyleConstants.setBackground(PLAIN, 
                uidefaults.getColor("TextPane.background"));                    // NOI18N
        StyleConstants.setForeground(PLAIN,
                uidefaults.getColor("TextPane.foreground"));                    // NOI18N
        BOLD = new SimpleAttributeSet();
        StyleConstants.setBold(BOLD, true);
        GREEN = new SimpleAttributeSet();
        StyleConstants.setBold(GREEN, true);
        StyleConstants.setBackground(GREEN, new Color(192, 255, 192));
        TRANSLATED = new SimpleAttributeSet();
        StyleConstants.setBackground(TRANSLATED, new Color(255, 255, 153));
        
        UNTRANSLATED = new SimpleAttributeSet();
        StyleConstants.setBackground(UNTRANSLATED, new Color(0xCC, 0xCC, 0xFF));
        
        DISABLED = new SimpleAttributeSet();
        StyleConstants.setForeground(DISABLED, 
                uidefaults.getColor("Label.disabledForeground"));               // NOI18N
        TEXT_EXTRA = new SimpleAttributeSet();
        StyleConstants.setForeground(TEXT_EXTRA, Color.blue);
        TEXT_BORDER = new SimpleAttributeSet();
        StyleConstants.setForeground(TEXT_BORDER, Color.green);
        
        // using red custom jagged underline, as seen in fine word processors
        // and IDEs.
        MISSPELLED = new SimpleAttributeSet();
        StyleConstants.setForeground(MISSPELLED, Color.black);
        ViewLabel.setCustomUnderline(MISSPELLED, 
                ViewLabel.RED_JAGGED_UNDERLINE);
        
    }
    
    /**
     * return a new MutableAttributeSet with the background color of the base and
     * all other attributes of toApply (currently: foreground, bold, underline)
     */
    public static MutableAttributeSet applyStyles(AttributeSet base, AttributeSet toApply) {
        MutableAttributeSet result = new SimpleAttributeSet();
        try 
        {
            StyleConstants.setBackground(result, StyleConstants.getBackground(base));
        }
        catch( java.lang.NullPointerException e ) // Hack for [ 1822579 ] 
        {                                         // Check-spelling/styles error
            StyleConstants.setBackground(result, Color.white);
        }
        StyleConstants.setForeground(result, StyleConstants.getForeground(toApply));
        StyleConstants.setBold(result, StyleConstants.isBold(toApply));
        StyleConstants.setUnderline(result, StyleConstants.isUnderline(toApply));
        // make sure the custom underlining is copied, too
        ViewLabel.setCustomUnderline(result, 
                ViewLabel.getCustomUnderline(toApply));
        
        return result;
}
}
