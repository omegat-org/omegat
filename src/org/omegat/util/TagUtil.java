/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay
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

package org.omegat.util;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.core.data.SourceTextEntry;

/**
 * A collection of tag-related static utilities.
 * 
 * @author Aaron Madlon-Kay
 */
public class TagUtil {
        
    public static List<String> getAllTagsInSource() {
        
        List<String> result = new ArrayList<String>();
        
        // Add tags.
        SourceTextEntry ste = Core.getEditor().getCurrentEntry();
        for(ProtectedPart pp:ste.getProtectedParts()) {
            result.add(pp.getTextInSourceSegment());
        }
        return result;
    }

    public static List<String> getAllTagsMissingFromTarget() {
        List<String> result = new ArrayList<String>();
        
        String target = Core.getEditor().getCurrentTranslation();
        
        for (String tag : getAllTagsInSource()) {
            if (!target.contains(tag)) {
                result.add(tag);
            }
        }
        return result;
    }
    
    public static List<String> getGroupedMissingTagsFromTarget() {
        String sourceText = Core.getEditor().getCurrentEntry().getSrcText();
        List<String> result = new ArrayList<String>();
        
        int index = -1;
        StringBuilder group = null;
        for (String tag : getAllTagsMissingFromTarget()) {
            if (sourceText.startsWith(tag, index)) {
                group.append(tag);
                index += tag.length();
            } else {
                if (group != null) result.add(group.toString());
                group = new StringBuilder(tag);
                index = sourceText.indexOf(tag, index) + tag.length();
            }
        }
        if (group != null && group.length() > 0) result.add(group.toString());
        
        return result;
    }
}
