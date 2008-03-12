/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.util;

import java.text.BreakIterator;
import java.util.LinkedList;

/**
 * BreakIterator for word-breaks with OmegaT heuristics,
 * based on an instance of BreakIterator implementing word breaks.
 *
 * @see java.text.BreakIterator#getWordInstance
 * @author Maxym Mykhalchuk
 */
public class WordIterator extends BreakIterator
{
    BreakIterator breaker;
    String text;
    
    /** Creates a new instance of OmegaT's own word BreakIterator */
    public WordIterator()
    {
        breaker = BreakIterator.getWordInstance();
    }

    /**
     * Set a new text string to be scanned.  The current scan
     * position is reset to first().
     * @param newText new text to scan.
     */
    public void setText(String newText)
    {
        text = newText;
        breaker.setText(newText);
        nextItems.clear();
    }

    /**
     * Return the first boundary. The iterator's current position is set
     * to the first boundary.
     * @return The character index of the first text boundary.
     */
    public int first()
    {
        return breaker.first();
    }

    /**
     * Return character index of the text boundary that was most recently
     * returned by next(), previous(), first(), or last()
     * @return The boundary most recently returned.
     */
    public int current()
    {
        return breaker.current();
    }
    
    LinkedList<Integer> nextItems = new LinkedList<Integer>();
    
    /**
     * Return the boundary of the word following the current boundary.
     * <p>
     * Note: This iterator skips OmegaT-specific tags, and groups 
     * [text-]mnemonics-text into a single token.
     *
     * @return The character index of the next text boundary or DONE if all
     * boundaries have been returned.  Equivalent to next(1).
     */
    public int next()
    {
        if (!nextItems.isEmpty())
            return nextItems.removeFirst();
        
        int curr = current();
        int next = breaker.next();
        if (DONE==next)
            return DONE;

        String str = text.substring(curr, next);

        // grouping OmegaT tags
        if (str.equals("<"))                                                    // NOI18N
        {
            int next2 = breaker.next();
            if (DONE==next2)
                return next;
            
            int next3 = breaker.next();
            if (DONE==next3)
            {
                nextItems.add(next2);
                return next;
            }
            // there're at least two maybe-words after "<"
            String str2 = text.substring(next, next2);
            String str3 = text.substring(next2, next3);
            
            if (str2.equals("/"))                                               // NOI18N
            {
                // maybe closing tag
                if (!PatternConsts.OMEGAT_TAG_ONLY.matcher(str3).matches())
                {
                    // rewind back two times
                    int prev = breaker.previous();
                    prev = breaker.previous();
                    return next;
                }
                
                int next4 = breaker.next();
                if (DONE==next4)
                {
                    nextItems.add(next2);
                    nextItems.add(next3);
                    return next;
                }
                // there're at least three maybe-words after "<"
                String str4 = text.substring(next3, next4);
                if (str4.equals(">"))                                           // NOI18N
                    return next4; // yes, it's a standalone tag
                else
                {
                    // rewind back three times
                    int prev = breaker.previous();
                    prev = breaker.previous();
                    prev = breaker.previous();
                    return next;
                }
            }
            else if (!PatternConsts.OMEGAT_TAG_ONLY.matcher(str2).matches())
            {
                // rewind back two times
                int prev = breaker.previous();
                prev = breaker.previous();
                return next;
            }
            
            if (str3.equals("/"))                                               // NOI18N
            {
                // maybe standalone tag
                int next4 = breaker.next();
                if (DONE==next4)
                {
                    nextItems.add(next2);
                    nextItems.add(next3);
                    return next;
                }
                // there're at least three maybe-words after "<"
                String str4 = text.substring(next3, next4);
                if (str4.equals(">"))                                           // NOI18N
                    return next4; // yes, it's a standalone tag
                else
                {
                    // rewind back three times
                    int prev = breaker.previous();
                    prev = breaker.previous();
                    prev = breaker.previous();
                    return next;
                }
            }
            else if(str3.equals(">"))                                           // NOI18N
                return next3;   // yes, it's an OmegaT tag
            {
                // rewind back two times
                int prev = breaker.previous();
                prev = breaker.previous();
                return next;
            }
        }
        else if (str.equals("&"))                                               // NOI18N
        {
            // trying to see the mnemonic
            int next2 = breaker.next();
            if (DONE==next2)
                return next;
            
            String str2 = text.substring(next, next2);
            if (Character.isLetterOrDigit(str2.charAt(0)))
                return next2;
            else
            {
                // rewind back once
                int prev = breaker.previous();
                return next;
            }
        }
        else if (Character.isLetterOrDigit(str.charAt(0)))
        {
            // trying to see whether the next "word" is a "&"
            int next2 = breaker.next();
            if (DONE==next2)
                return next;
            
            String str2 = text.substring(next, next2);
            if (str2.equals("&"))   // yes, it's there                          // NOI18N
            {
                int next3 = breaker.next();
                if (DONE==next3)
                {
                    // Something&
                    nextItems.add(next2);
                    return next;
                }
                
                String str3 = text.substring(next2, next3);
                // is it followed by a word like Some&thing
                if (Character.isLetterOrDigit(str3.charAt(0)))
                    return next3;                               // oh yes
                else
                {                                               // oh no
                    // rewind back two times
                    int prev = breaker.previous();
                    prev = breaker.previous();
                    return next;
                }
            }
            else
            {
                // rewind back once
                int prev = breaker.previous();
                return next;
            }
        }
        else
            return next;
    }
    
    //////////////////////////////////////////////////////////////////////////
    // Not yet implemented
    //////////////////////////////////////////////////////////////////////////
    
    /**
     * <b>Not yet implemented! Throws a RuntimeException if you try to call it.</b>
     *
     * Return the nth boundary from the current boundary
     * @param n which boundary to return.  A value of 0
     * does nothing.  Negative values move to previous boundaries
     * and positive values move to later boundaries.
     * @return The index of the nth boundary from the current position.
     */
    public int next(int n)
    {
        throw new RuntimeException("Not Implemented");                          // NOI18N
    }

    /**
     * <b>Not yet implemented! Throws a RuntimeException if you try to call it.</b>
     *
     * Return the first boundary following the specified offset.
     * The value returned is always greater than the offset or
     * the value BreakIterator.DONE
     * @param offset the offset to begin scanning. Valid values
     * are determined by the CharacterIterator passed to
     * setText().  Invalid values cause
     * an IllegalArgumentException to be thrown.
     * @return The first boundary after the specified offset.
     */
    public int following(int offset)
    {
        throw new RuntimeException("Not Implemented");                          // NOI18N
    }

    /**
     * <b>Not yet implemented! Throws a RuntimeException if you try to call it.</b>
     *
     * Set a new text for scanning.  The current scan
     * position is reset to first().
     * @param newText new text to scan.
     */
    public void setText(java.text.CharacterIterator newText)
    {
        throw new RuntimeException("Not Implemented");                          // NOI18N
    }
    
    /**
     * <b>Not yet implemented! Throws a RuntimeException if you try to call it.</b>
     *
     * Get the text being scanned
     * @return the text being scanned
     */
    public java.text.CharacterIterator getText()
    {
        throw new RuntimeException("Not Implemented");                          // NOI18N
    }

    /**
     * <b>Not yet implemented! Throws a RuntimeException if you try to call it.</b>
     *
     * Return the boundary preceding the current boundary.
     * @return The character index of the previous text boundary or DONE if all
     * boundaries have been returned.
     */
    public int previous()
    {
        throw new RuntimeException("Not Implemented");                          // NOI18N
    }

    /**
     * <b>Not yet implemented! Throws a RuntimeException if you try to call it.</b>
     *
     * Return the last boundary. The iterator's current position is set
     * to the last boundary.
     * @return The character index of the last text boundary.
     */
    public int last()
    {
        throw new RuntimeException("Not Implemented");                          // NOI18N
    }

}
