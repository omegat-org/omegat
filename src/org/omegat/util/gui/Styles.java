/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
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
 * @author Maxym Mykhalchuk
 */
public final class Styles
{
    private Styles() { }
    
    /** Plain text. */
    public final static AttributeSet PLAIN;
    /** Bold text. */
    public final static MutableAttributeSet BOLD;
    /** Bold text on green background. */
    public final static MutableAttributeSet GREEN;
    /** Disabled, i.e grayed out text. */
    public final static MutableAttributeSet DISABLED;
    
    static
    {
        PLAIN = SimpleAttributeSet.EMPTY;
        BOLD = new SimpleAttributeSet();
        StyleConstants.setBold(BOLD, true);
        GREEN = new SimpleAttributeSet();
        StyleConstants.setBold(GREEN, true);
        StyleConstants.setBackground(GREEN, new Color(192, 255, 192));
        DISABLED = new SimpleAttributeSet();
        StyleConstants.setForeground(DISABLED, javax.swing.UIManager.getDefaults().getColor("Label.disabledForeground"));
    }
    
}
