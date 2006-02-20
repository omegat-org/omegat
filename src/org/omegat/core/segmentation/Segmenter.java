/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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
import org.omegat.core.threads.CommandThread;
import org.omegat.gui.ProjectProperties;

import org.omegat.util.Language;
import org.omegat.util.OConsts;
import org.omegat.util.Preferences;

/**
 * The class that sentences the paragraphs into sentences
 * and glues translated sentences together to form a paragraph.
 * 
 * 
 * @author Maxym Mykhalchuk
 */
public final class Segmenter
{
    /** private to disallow creation */
    private Segmenter() { }
    
    /**
     * Segments the paragraph to sentences according to currently setup rules.
     * <p>
     * Bugfix for 
     * <a href="http://sourceforge.net/support/tracker.php?aid=1288742">issue 
     * 1288742</a>: Sentences are returned without spaces in the beginning and
     * at the end of a sentence. 
     * <p>
     * An additional list with space information
     * is returned to be able to glue translation together with the same spaces
     * between them as in original paragraph.
     *
     * @param paragraph the paragraph text
     * @param spaces list to store information about spaces between sentences, may be null
     * @return list of sentences (String objects)
     */
    public static List segment(String paragraph, List spaces)
    {
        List segments = breakParagraph(paragraph);
        List sentences = new ArrayList(segments.size());
        if( spaces!=null )
            spaces.clear();
        for(int i=0; i<segments.size(); i++)
        {
            String one = (String)segments.get(i);
            int len = one.length();
            int b = 0;
            StringBuffer bs = new StringBuffer();
            while( b<len && Character.isWhitespace(one.charAt(b)) )
            {
                bs.append(one.charAt(b));
                b++;
            }

            int e = len-1;
            StringBuffer es = new StringBuffer();
            while( e>=b && Character.isWhitespace(one.charAt(e)) )
            {
                es.append(one.charAt(e));
                e--;
            }
            es.reverse();

            String trimmed = one.substring(b, e+1);
            sentences.add(trimmed);
            if( spaces!=null )
            {
                spaces.add(bs);
                spaces.add(es);
            }
        }
        return sentences;
    }
    
    /**
     * Returns pre-sentences (sentences with spaces between), computed by 
     * breaking paragraph into chunks of text.
     * <p>
     * If glued back together, these strings form the same paragraph text 
     * as this function was fed.
     */
    private static List breakParagraph(String paragraph)
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
            
            // Sometimes the last segment may be empty,
            // it happens for paragraphs like "Rains. "
            // So if it's an empty segment and there's a previous segment
            if( oneseg.trim().length()==0
                    && segments.size()>0 )
            {
                String prev = (String)segments.get(segments.size()-1);
                prev += oneseg;
                segments.set(segments.size()-1, prev);
            }
            else
                segments.add(oneseg);
        }
        catch( IndexOutOfBoundsException iobe ) { }
        
        return segments;
    }
    
    private static Pattern DEFAULT_BEFOREBREAK_PATTERN = Pattern.compile(".", Pattern.DOTALL);  // NOI18N
    
    /**
     * Returns the places of possible breaks between sentences.
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
                if( abs==bbe )
                    res.add( new Integer(bbe) );
            }
        }
        
        return res;
    }
    
    
    /**
     * Glues the sentences back to paragraph.
     * <p>
     * As sentences are returned by {@link #segment(String, List)}
     * without spaces before and after them, this method adds 
     * spaces if needed:
     * <ul>
     * <li>For translation to Japanese does <b>not</b> add any spaces
     * <li>For translation from Japanese adds one space
     * <li>For all other language combinations adds those spaces as were in the
     *     paragraph before.
     * </ul>
     *
     * @param sentences list of translated sentences
     * @param spaces information about spaces in original paragraph, may be null
     * @return glued translated paragraph
     */
    public static String glue(List sentences, List spaces)
    {
        if( sentences.size()<=0 )
            return "";                                                          // NOI18N
	
	ProjectProperties config;
        if(CommandThread.core!=null)
            config=CommandThread.core.getProjectProperties();
        else
            config = new ProjectProperties();
        
        StringBuffer res = new StringBuffer();
        res.append((String)sentences.get(0));
	
        for(int i=1; i<sentences.size(); i++)
        {
            StringBuffer sp = new StringBuffer();
            
            if( !"JA".equals(config.getTargetLanguage().getLanguageCode()) )    // NOI18N
            {
                if( spaces!=null )
                {
                    sp.append((StringBuffer)spaces.get(2*i-1));
                    sp.append((StringBuffer)spaces.get(2*i));
                }
                else
                    sp.append(' ');

                if( "JA".equals(config.getSourceLanguage().getLanguageCode())   // NOI18N
                        && sp.length()==0 )
                    sp.append(' ');
            }
	    
	    res.append(sp);
            res.append((String)sentences.get(i));
        }
        return res.toString();
    }
}
