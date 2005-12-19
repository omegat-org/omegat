/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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

package org.omegat.core.segmentation;

import java.io.Serializable;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


/**
 * The class representing a single segmentation rule.
 *
 * @author Maxym Mykhalchuk
 */
public class Rule implements Serializable
{
    
    /** Creates a new empty instance of segmentation rule */
    public Rule() { }
    
    /** Creates an initialized instance of segmentation rule */
    public Rule(boolean breakRule, String beforebreak, String afterbreak)
    {
        setBreakRule(breakRule);
        setBeforebreak(beforebreak);
        setAfterbreak(afterbreak);
    }

    /**
     * Holds value of property breakRule.
     * <p>
     * This property corresponds to 'break' attribute of SRX 'rule', 
     * meaning whether this is a rule that determines a break or an exception.
     */
    private boolean breakRule;

    /**
     * Returns whether this is a rule that determines a break or an exception.
     *
     * @return true is this is a break rule.
     */
    public boolean isBreakRule()
    {
        return this.breakRule;
    }

    /**
     * Sets whether this is a rule that determines a break or an exception.
     *
     * @param breakRule New value -- true for a break rule, false for an exception.
     */
    public void setBreakRule(boolean breakRule)
    {
        this.breakRule = breakRule;
    }

    /**
     * A regular expression which represents the text that appears before a segment break.
     */
    private Pattern beforebreak;

    /**
     * Returns a regular expression which represents the text that appears before a segment break.
     * @return regular expression of a text before break.
     */
    public String getBeforebreak()
    {
        if( beforebreak!=null )
            return beforebreak.pattern();
        else
            return null;
    }
    /**
     * Returns a regular expression which represents the text that appears before a segment break.
     * @return regular expression of a text before break.
     */
    public Pattern getCompiledBeforebreak()
    {
        return beforebreak;
    }

    /**
     * Sets a regular expression which represents the text that appears before 
     * a segment break.
     * @param beforebreak Regular expression string of a text before break.
     */
    public void setBeforebreak(String beforebreak) throws PatternSyntaxException
    {
        this.beforebreak=Pattern.compile(beforebreak, Pattern.UNICODE_CASE+Pattern.DOTALL);
        // Pattern.DOTALL is added to fix the bug# 1385202 
    }

    /**
     * A regular expression which represents the text that appears after a segment break.
     */
    private Pattern afterbreak;

    /**
     * Returns a regular expression which represents the text that appears after a segment break.
     * @return regular expression of a text after break.
     */
    public String getAfterbreak()
    {
        if( afterbreak!=null )
            return afterbreak.pattern();
        else
            return null;
    }
    /**
     * Returns a regular expression which represents the text that appears after a segment break.
     * @return regular expression of a text after break.
     */
    public Pattern getCompiledAfterbreak()
    {
        return afterbreak;
    }

    /**
     * Sets a regular expression which represents the text that appears after a segment break.
     * @param afterbreak Regular expression string of a text after break.
     */
    public void setAfterbreak(String afterbreak) throws PatternSyntaxException
    {
        this.afterbreak=Pattern.compile(afterbreak, Pattern.UNICODE_CASE+Pattern.DOTALL);
        // Pattern.DOTALL is added to fix the bug# 1385202 
    }
    
        
}
