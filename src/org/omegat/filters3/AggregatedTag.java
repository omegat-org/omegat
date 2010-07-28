/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Antonio Vilei
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

package org.omegat.filters3;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated Tag is a compound tag made up of other tags.
 * The purpose of this class is reducing the number of tags shown in OpenXML
 * documents. The current OpenXML filter finds too many tags, usually causing
 * what users call the "tag soup". Tags aggregation can help alleviate this
 * problem, but can sometimes lead to semantic issues. Aggregation is OK
 * only as a temporary hack, until we improve the OpenXML filter.
 *
 * @author Antonio Vilei
 */
public class AggregatedTag extends Tag {

    /** Creates a new instance of Aggregated Tag */
    public AggregatedTag(String tag, String shortcut, int type, Attributes attributes)
    {
        super(tag, shortcut, type, attributes);
    }


    /** Adds a tag to the aggregated tags list. */
    public void add(Tag tag)
    {
        tags.add(tag);
    }

    /**
     * Returns the tags belonging to this aggregated tag in their original form.
     */
    public String toOriginal()
    {
        StringBuffer buf = new StringBuffer();

        for (Tag tag : tags) {
            buf.append(tag.toOriginal());
        }

        return buf.toString();
    }

    /** List of aggregated tags. */
    private List<Tag> tags = new ArrayList<Tag>();
}
