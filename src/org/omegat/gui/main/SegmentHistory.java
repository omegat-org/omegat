/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 - Zoltan Bartko - bartkozoltan@bartkozoltan.com
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


package org.omegat.gui.main;

import java.util.ArrayList;
import java.util.List;

/**
 * Segment history class. The model needed to implement functionality as in 
 * web browsers -  go to previous and goto next segment.
 *
 * Since this class is a singleton, use getInstance() to find out the instance.
 * @author bartkoz
 */
public class SegmentHistory {
    /**
     * the pointer
     */
    private int pointer = -1;
    
    private List<Integer> history = new ArrayList<Integer>();
    
    /** Creates a new instance of SegmentHistory */
    protected SegmentHistory() {
    }
    
    /**
     * check if there is any way back
     */
    public boolean hasNext() {
        return (pointer > -1 && pointer < history.size() - 1);
    }
    
    /**
     * check if there is any way forward
     */
    public boolean hasPrev() {
        return (pointer > 0);
    }
    
    /**
     * insert a new segment number after the pointer. If the pointer is not at 
     * the end of the list, the part of the list behind the pointer is discarded
     * before the new segment number is added
     */
    public void insertNew(int segmentNumber) {
        if (pointer > -1) {
            int current = history.get(pointer);
            
            // no need for identical values- just to make sure
            if (current == segmentNumber)
                return;
            
            if (pointer < history.size() - 1) {
                // drop the ones later than the current
                /*List subList = history.subList(pointer + 1, history.size());
                history.removeAll(subList);*/
                for (int i = history.size() - 1; i > pointer; i--) {
                    history.remove(i);
                }
            }
        } 
        history.add(new Integer(segmentNumber));
        pointer++;
    }
    
    /**
     * return the current segment value. -1 if none available;
     */
    public int getValue() {
        if (pointer > -1)
            return get(pointer);
        else
            return -1;
    }
    
    /**
     * return the previous segment value. -1 if not applicable (no previous)
     */
    public int back() {
        if (pointer < 0)
            return -1;
        
        if (pointer > 0)
            pointer--;
        
        int result = get(pointer);
        
        return result;
    }
    
    /**
     * return the next segment value. -1 if not applicable (no next)
     */
    public int forward() {
        if (pointer == -1 || pointer >= history.size() - 1)
            return -1;
        
        return get(++pointer);
    }
    
    /**
     * return the int value of the given item in the history list
     */
    private int get(int index) {
        return history.get(index);
    }
    
    /**
     * clears the history list. It will be empty after the call.
     */
    public void clear() {
        history.clear();
        pointer = -1;
    }
}
