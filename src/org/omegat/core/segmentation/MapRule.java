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
import java.util.List;
import java.util.regex.Pattern;

/**
 * A class representing the language rules 
 * and their mapping to the segmentation rules for each particular language.
 *
 * @author Maxym Mykhalchuk
 */
public class MapRule implements Serializable
{
    /** creates a new empty MapRule */
    public MapRule() { }

    /** creates an initialized MapRule */
    public MapRule(String language, String pattern, List rules)
    {
        this.setLanguage(language);
        this.setPattern(pattern);
        this.setRules(rules);
    }                

    /** Language Name */
    private String language;
    /** Returns Language Name */
    public String getLanguage()
    {
        return language;
    }
    /** Sets Language Name */
    public void setLanguage(String language)
    {
        this.language = language;
    }

    /** Pattern for the language/country ISO code (of a form LL-CC) */
    private String pattern;
    /** Returns Pattern for the language/country ISO code (of a form LL-CC) */
    public String getPattern()
    {
        return pattern;
    }
    /** 
     * Returns Pattern object for the 
     * Pattern for the language/country ISO code (of a form LL-CC) 
     */
    public Pattern getCompiledPattern()
    {
        return Pattern.compile(pattern);
    }
    
    /** Sets Pattern for the language/country ISO code (of a form LL-CC) */
    public void setPattern(String pattern)
    {
        this.pattern = pattern;
    }

    /** List of rules (of class {@link Rule}) for the language */
    private List rules;
    /** Returns List of rules (of class {@link Rule}) for the language */
    public List getRules()
    {
        return rules;
    }
    /** Sets List of rules (of class {@link Rule}) for the language */
    public void setRules(List rules)
    {
        this.rules = rules;
    }
}
