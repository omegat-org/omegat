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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;

/**
 * The class that actually segments the text.
 *
 * @author Maxym Mykhalchuk
 */
public final class Segmenter
{
    
    private static Segmenter segmenter = null;
    
    /**
     * Segmenter factory method.
     * <p>
     * For now, just returns the only segmenter object.
     */
    public static Segmenter getSegmenter()
    {
        if( segmenter==null )
            segmenter = new Segmenter();
        return segmenter;
    }
    
    /** private to disallow creation */
    private Segmenter() { }
    
    /**
     * Segments the paragraph to sentences
     * according to currently setup rules.
     *
     * @param paragraph - the paragraph text
     * @returns list of segments
     */
    public static List segment(String paragraph)
    {
        Language srclang = new Language(Preferences.getPreference(Preferences.SOURCE_LOCALE));
        List rules = SRX.getSRX().lookupRulesForLanguage(srclang);

        // determining the applicable break positions
        TreeSet dontbreakpositions = new TreeSet();
        TreeSet breakpositions = new TreeSet();
        for(int i=rules.size()-1; i>=0; i--)
        {
            Rule rule = (Rule)rules.get(i);
            List rulebreaks = getBreaks(paragraph, rule);
            if( rule.isBreakRule() )
            {
                breakpositions.addAll(rulebreaks);
                dontbreakpositions.removeAll(rulebreaks);
            }
            else
            {
                dontbreakpositions.addAll(rulebreaks);
                breakpositions.removeAll(rulebreaks);
            }
        }
        breakpositions.removeAll(dontbreakpositions);
        
        // and now breaking the string according to the positions
        Iterator posIterator = breakpositions.iterator();
        List segments = new ArrayList();
        int prevpos = 0;
        while( posIterator.hasNext() )
        {
            int pos = ((Integer)posIterator.next()).intValue();
            String oneseg = paragraph.substring(prevpos, pos);
            segments.add(oneseg);
            prevpos = pos;
        }
        try
        {
            String oneseg = paragraph.substring(prevpos);
            segments.add(oneseg);
        }
        catch( IndexOutOfBoundsException iobe ) { }
        
        return segments;
    }
    
    private static Pattern DEFAULT_BEFOREBREAK_PATTERN = Pattern.compile(".");  // NOI18N
    
    /**
     * Returns the places of possible breaks between segments.
     */
    private static List getBreaks(String paragraph, Rule rule)
    {
        List res = new ArrayList();
        
        Matcher bbm = null;
        if( rule.getBeforebreak()!=null )
            bbm = rule.getCompiledBeforebreak().matcher(paragraph);
        Matcher abm = null;
        if( rule.getAfterbreak()!=null )
            abm = rule.getCompiledAfterbreak().matcher(paragraph);
        
        if( bbm==null && abm==null )
            return res;

        if( abm!=null )
            if( !abm.find() )
                return res;
        
        if( bbm==null )
            bbm = DEFAULT_BEFOREBREAK_PATTERN.matcher(paragraph);
        
        
        while( bbm.find() )
        {
            int bbe = bbm.end();
            if( abm==null )
                res.add( new Integer(bbe) );
            else
            {
                int abs = abm.start();
                while( abs<bbe )
                {
                    boolean found = abm.find();
                    if( !found )
                        return res;
                    abs = abm.start();
                }
                if( abm.start()==bbe )
                    res.add( new Integer(bbe) );
            }
        }
        
        return res;
    }
}
