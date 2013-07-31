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

package org.omegat.core.tagvalidation;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.tagvalidation.ErrorReport.TagError;
import org.omegat.util.StaticUtils;

/**
 * @author Aaron Madlon-Kay
 */
public class TagRepair {

    public static void fixTag(SourceTextEntry ste, String tag, TagError error, StringBuilder translation, String source) {
        List<String> tags;
        switch (error) {
        case DUPLICATE:
        case ORDER:
        case MALFORMED:
            tags = new ArrayList<String>();
            StaticUtils.buildTagList(source, ste.getProtectedParts(), tags);
            fixMalformed(tags, translation, tag);
            break;
        case MISSING:
            tags = new ArrayList<String>();
            StaticUtils.buildTagList(source, ste.getProtectedParts(), tags);
            fixMissing(tags, translation, tag);
            break;
        case EXTRANEOUS:
            fixExtraneous(translation, tag);
            break;
        case ORPHANED:
            // This is fixed by fixing MISSING.
            break;
        case WHITESPACE:
            fixWhitespace(translation, source);
            break;
        default:
            break;
        }
    }

    protected static void fixWhitespace(StringBuilder translation, String source) {
        if (source.startsWith("\n") && translation.charAt(0) != '\n') {
            translation.insert(0, '\n');
        } else if (!source.startsWith("\n") && translation.charAt(0) == '\n') {
            translation.deleteCharAt(0);
        }
        if (source.endsWith("\n") && translation.charAt(translation.length() - 1) != '\n') {
            translation.append('\n');
        } else if (!source.endsWith("\n") && translation.charAt(translation.length() - 1) == '\n') {
            translation.deleteCharAt(translation.length() - 1);
        }
    }

    protected static void fixMalformed(List<String> tags, StringBuilder text, String tag) {
        fixExtraneous(text, tag);
        fixMissing(tags, text, tag);
    }

    protected static void fixMissing(List<String> tags, StringBuilder text, String tag) {
        // Insert missing tag.
        int index = tags.indexOf(tag);
        String prev = index > 0 ? tags.get(index - 1) : null;
        String next = index + 1 < tags.size() ? tags.get(index + 1) : null;
        if (prev != null && text.indexOf(prev) > -1) {
            // Insert after a preceding tag.
            text.insert(text.indexOf(prev) + prev.length(), tag);
        } else if (next != null && text.indexOf(next) > -1) {
            // Insert before a proceeding tag.
            text.insert(text.indexOf(next), tag);
        } else {
            // Nothing before or after; append to end.
            text.append(tag);
        }
    }

    protected static void fixExtraneous(StringBuilder text, String tag) {
        int i = 0;
        while ((i = text.indexOf(tag, i)) != -1) {
            text.delete(i, i + tag.length());
        }
    }
    
}
