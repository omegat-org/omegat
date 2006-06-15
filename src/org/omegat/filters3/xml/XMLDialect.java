/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
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

package org.omegat.filters3.xml;

import java.util.Map;
import java.util.Set;
import org.omegat.util.MultiMap;
import org.xml.sax.InputSource;

/**
 * Interface to describe XML dialect.
 *
 * @author Maxym Mykhalchuk
 */
public interface XMLDialect
{
    /**
     * Returns the set of paragraph tags.
     * <p>
     * Each entry in a set should be a String class.
     */
    Set getParagraphTags();

    /**
     * Returns the set of tags that surround preformatted text.
     * <p>
     * Each entry in a set should be a String class.
     */
    Set getPreformatTags();
    
    /**
     * Returns the set of "out-of-turn" tags.
     * Such tags specify chunks of text that should be translated separately,
     * not breaking currently collected text entry. 
     * For example, footnotes in OpenDocument.
     * <p>
     * Each entry in a set should be a String class.
     */
    Set getOutOfTurnTags();
    
    /**
     * Returns the multimap of translatable attributes of each tag.
     * <p>
     * Each entry should map from a String to a set of Strings.
     */
    MultiMap getTranslatableTagAttributes();
    
    /**
     * Returns the set of translatable attributes (no matter what tag they belong to).
     * <p>
     * Each entry in a set should be a String class.
     */
    Set getTranslatableAttributes();
    
    /** Unboxed (of primitive type </code>int</code>) constraint on Doctype name. */
    static final int CONSTRAINT_DOCTYPE_UNBOXED = 1;
    /** Unboxed (of primitive type </code>int</code>) constraint on PUBLIC Doctype declaration. */
    static final int CONSTRAINT_PUBLIC_DOCTYPE_UNBOXED = 2;
    /** Unboxed (of primitive type </code>int</code>) constraint on SYSTEM Doctype declaration. */
    static final int CONSTRAINT_SYSTEM_DOCTYPE_UNBOXED = 3;
    /** Unboxed (of primitive type </code>int</code>) constraint on root tag name. */
    static final int CONSTRAINT_ROOT_UNBOXED = 4;
    
    /** Constraint on Doctype name. */
    static final Integer CONSTRAINT_DOCTYPE = new Integer(CONSTRAINT_DOCTYPE_UNBOXED);
    /** Constraint on PUBLIC Doctype declaration. */
    static final Integer CONSTRAINT_PUBLIC_DOCTYPE = new Integer(CONSTRAINT_PUBLIC_DOCTYPE_UNBOXED);
    /** Constraint on SYSTEM Doctype declaration. */
    static final Integer CONSTRAINT_SYSTEM_DOCTYPE = new Integer(CONSTRAINT_SYSTEM_DOCTYPE_UNBOXED);
    /** Constraint on root tag name. */
    static final Integer CONSTRAINT_ROOT = new Integer(CONSTRAINT_ROOT_UNBOXED);
    
    /**
     * Returns defined constraints to restrict supported subset of XML files.
     * There can be only one constraint of each type,
     * see CONSTRAINT_... constants.
     * <p>
     * Each entry should map an {@link Integer} to a {@link Pattern} -- 
     * regular expression for a specified constrained string.
     */
    Map getConstraints();
}
