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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.filters3;

/**
 * Element of the translatable entry. Can be a tag or a piece of text.
 * 
 * @author Maxym Mykhalchuk
 */
public interface Element {
    /**
     * Returns shortcut string representation of the element. E.g. for
     * &lt;strong&gt; tag should return &lt;s3&gt;.
     */
    String toShortcut();

    /**
     * Returns long XML-encoded representation of the element for storing in
     * TMX. E.g. for &lt;strong&gt; tag should return &lt;bpt
     * i="3"&gt;&amp;lt;strong&amp;gt;&lt;/bpt&gt;.
     */
    String toTMX();

    /**
     * Returns the element in its original form as it was in original document.
     * E.g. for &lt;strong&gt; tag should return &lt;bpt
     * i="3"&gt;&amp;lt;strong&amp;gt;&lt;/bpt&gt;.
     */
    String toOriginal();
}
