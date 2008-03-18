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

package org.omegat.core.matching;

import org.omegat.util.OStrings;
import org.omegat.util.Token;

/**
 * Class to compute Levenshtein Distance.
 *
 * <p/>
 * Levenshtein distance (LD) is a measure of the similarity between two strings,
 * which we will refer to as the source string (s) and the target string (t).
 * The distance is the number of deletions, insertions, or substitutions
 * required to transform s into t.
 *
 * <p/>
 * For example,
 * <ul>
 * <li>If s is "test" and t is "test", then LD(s,t) = 0, because
 * no transformations are needed. The strings are already identical.
 * <li>If s is "test" and t is "tent", then LD(s,t) = 1, because
 * one substitution (change "s" to "n") is sufficient to transform s into t.
 * </ul>
 *
 * <p/>
 * The greater the Levenshtein distance, the more different the strings are.
 * <p/>
 * Levenshtein distance is named after the Russian scientist
 * Vladimir Levenshtein, who devised the algorithm in 1965.
 * If you can't spell or pronounce Levenshtein, the metric is also sometimes
 * called edit distance.
 *
 * @see http://www.merriampark.com/ld.htm
 *
 * @author Vladimir Levenshtein
 * @author Michael Gilleland, Merriam Park Software
 * @author Chas Emerick, Apache Software Foundation
 * @author Maxym Mykhalchuk
 */
public class LevenshteinDistance
{
    
    /**
     * Get minimum of three values
     */
    private static short minimum(int a, int b, int c)
    {
        return (short)Math.min(a, Math.min(b, c));
    }
    
    /** Maximal number of items compared. */
    public static final int MAX_N = 1000;
    
    /** Cost array, horizontally. Here to avoid excessive allocation and garbage collection. */
    private static short[] d = new short[MAX_N+1];
    /** "Previous" cost array, horizontally. Here to avoid excessive allocation and garbage collection. */
    private static short[] p = new short[MAX_N+1];
    
    /*
     * Compute Levenshtein distance between two lists.
     *
     * <p>
     * The difference between this impl. and the canonical one is that,
     * rather than creating and retaining a matrix of size s.length()+1 by t.length()+1,
     * we maintain two single-dimensional arrays of length s.length()+1.
     *
     * <p>
     * The first, d, is the 'current working' distance array that maintains
     * the newest distance cost counts as we iterate through the characters
     * of String s.  Each time we increment the index of String t we are comparing,
     * d is copied to p, the second int[].  Doing so allows us to retain
     * the previous cost counts as required by the algorithm
     * (taking the minimum of the cost count to the left, up one,
     * and diagonally up and to the left of the current cost count
     * being calculated).
     * <p>
     * (Note that the arrays aren't really copied anymore, just switched...
     * this is clearly much better than cloning an array or doing
     * a System.arraycopy() each time through the outer loop.)
     *
     * <p>
     * Effectively, the difference between the two implementations is
     * this one does not cause an out of memory condition when calculating
     * the LD over two very large strings.
     *
     * <p>
     * For perfomance reasons the maximal number of compared items is
     * {@link #MAX_N}.
     */
    public static int compute(Token[] s, Token[] t)
    {
        if( s==null || t==null )
            throw new IllegalArgumentException(OStrings.getString("LD_NULL_ARRAYS_ERROR"));
        
        int n = s.length; // length of s
        int m = t.length; // length of t
        
        if( n==0 )
            return m;
        else if( m==0 )
            return n;

        if( n>MAX_N )
            n = MAX_N;
        if( m>MAX_N )
            m = MAX_N;
        
        short[] swap;               // placeholder to assist in swapping p and d
        
        // indexes into strings s and t
        short i; // iterates through s
        short j; // iterates through t
        
        Token t_j = null; // jth object of t
        
        short cost; // cost
        
        for (i = 0; i<=n; i++)
            p[i] = i;
        
        for (j = 1; j<=m; j++)
        {
            t_j = t[j-1];
            d[0] = j;
            
            Token s_i = null; // ith object of s
            for (i=1; i<=n; i++)
            {
                s_i = s[i-1];
                cost = s_i.equals(t_j) ? (short)0 : (short)1;
                // minimum of cell to the left+1, to the top+1, diagonally left and up +cost
                d[i] = minimum(d[i-1]+1, p[i]+1, p[i-1]+cost);
            }
            
            // copy current distance counts to 'previous row' distance counts
            swap = p;
            p = d;
            d = swap;
        }
        
        // our last action in the above loop was to switch d and p, so p now
        // actually has the most recent cost counts
        return p[n];
    }
}