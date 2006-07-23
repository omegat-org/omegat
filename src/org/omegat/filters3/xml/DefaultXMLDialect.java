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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.xml.sax.InputSource;

import org.omegat.util.MultiMap;

/**
 * Helper class for describing a certain XML dialect.
 *
 * @author Maxym Mykhalchuk
 */
public class DefaultXMLDialect implements XMLDialect
{
    /** The set of defined paragraph tags. */
    private Set paragraphTags = new HashSet();
    
    /** Defines paragraph tag. Allows duplicates. */
    public void defineParagraphTag(String tag)
    {
        paragraphTags.add(tag);
    }
    /** Defines a set of paragraph tags from an array. Allows duplicates. */
    public void defineParagraphTags(String[] tags)
    {
        for (int i=0; i<tags.length; i++)
            defineParagraphTag(tags[i]);
    }

    /** The set of defined tags that surround preformatted text. */
    private Set preformatTags = new HashSet();
    
    /** Defines preformat tag. Allows duplicates. */
    public void definePreformatTag(String tag)
    {
        preformatTags.add(tag);
    }
    /** Defines a set of preformat tags from an array. Allows duplicates. */
    public void definePreformatTags(String[] tags)
    {
        for (int i=0; i<tags.length; i++)
            definePreformatTag(tags[i]);
    }

    /** The set of defined tags that surround intact text. */
    private Set intactTags = new HashSet();
    
    /** Defines intact tag. Allows duplicates. */
    public void defineIntactTag(String tag)
    {
        intactTags.add(tag);
    }
    /** Defines a set of intact tags from an array. Allows duplicates. */
    public void defineIntactTags(String[] tags)
    {
        for (int i=0; i<tags.length; i++)
            defineIntactTag(tags[i]);
    }
    
    /** The set of defined paragraph tags. */
    private MultiMap translatableTagAttributes = new MultiMap();
    
    /** Defines translatable attribute of a tag. */
    public void defineTranslatableTagAttribute(String tag, String attribute)
    {
        translatableTagAttributes.put(tag, attribute);
    }
    /** Defines translatable attributes of a tag. */
    public void defineTranslatableTagAttributes(String tag, String[] attributes)
    {
        for (int i=0; i<attributes.length; i++)
            defineTranslatableTagAttribute(tag, attributes[i]);
    }
    /** Defines translatable attribute of several tags. */
    public void defineTranslatableTagsAttribute(String[] tags, String attribute)
    {
        for (int i=0; i<tags.length; i++)
            defineTranslatableTagAttribute(tags[i], attribute);
    }
    
    /** The set of defined paragraph tags. */
    private Set translatableAttributes = new HashSet();
    
    /** Defines always translatable attribute (no matter what tag it belongs to). */
    public void defineTranslatableAttribute(String attribute)
    {
        translatableAttributes.add(attribute);
    }
    /** Defines always translatable attributes (no matter what tag it belongs to). */
    public void defineTranslatableAttributes(String[] attributes)
    {
        for (int i=0; i<attributes.length; i++)
            defineTranslatableAttribute(attributes[i]);
    }

    /**
     * The set of defined out of turn tags that surround chunks of text 
     * that should be translated separately, not breaking currently 
     * collected text.
     */
    private Set outOfTurnTags = new HashSet();
    
    /**
     * Defines out of turn tag. Such tag surrounds chunk of text 
     * that should be translated separately, not breaking currently 
     * collected text.
     */
    public void defineOutOfTurnTag(String tag)
    {
        outOfTurnTags.add(tag);
    }
    /**
     * Defines out of turn tags. Such tags surround chunks of text 
     * that should be translated separately, not breaking currently 
     * collected text.
     */
    public void defineOutOfTurnTags(String[] tags)
    {
        for (int i=0; i<tags.length; i++)
            defineOutOfTurnTag(tags[i]);
    }

    HashMap constraints = new HashMap();
    
    /** 
     * Defines a constraint to restrict supported subset of XML files.
     * There can be only one constraint of each type.
     *
     * @param constraintType Type of constraint, see CONSTRAINT_... constants.
     * @param template Regular expression for a specified constrained string.
     */
    public void defineConstraint(Integer constraintType, Pattern template)
    {
        constraints.put(constraintType, template);
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // XMLDialect Interface Implementation
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * Returns the set of defined paragraph tags.
     * <p>
     * Each entry in a set should be a String class.
     */
    public Set getParagraphTags()
    {
        return paragraphTags;
    }

    /**
     * Returns the set of tags that surround preformatted text.
     * <p>
     * Each entry in a set should be a String class.
     */
    public Set getPreformatTags()
    {
        return preformatTags;
    }
    

    /**
     * Returns the set of tags that surround intact portions of document,
     * that should not be translated at all.
     * <p>
     * Each entry in a set should be a String class.
     */
    public Set getIntactTags()
    {
        return intactTags;
    }
    
    /**
     * Returns the multimap of translatable attributes of each tag.
     * <p>
     * Each entry should map from a String to a set of Strings.
     */
    public MultiMap getTranslatableTagAttributes()
    {
        return translatableTagAttributes;
    }

    /**
     * Returns the set of translatable attributes (no matter what tag they belong to).
     * <p>
     * Each entry in a set should be a String class.
     */
    public Set getTranslatableAttributes()
    {
        return translatableAttributes;
    }

    /**
     * Returns the set of "out-of-turn" tags.
     * Such tags specify chunks of text that should be translated separately,
     * not breaking currently collected text entry. 
     * For example, footnotes in OpenDocument.
     * <p>
     * Each entry in a set should be a String class.
     */
    public Set getOutOfTurnTags()
    {
        return outOfTurnTags;
    }

    /**
     * Returns defined constraints to restrict supported subset of XML files.
     * There can be only one constraint of each type,
     * see CONSTRAINT_... constants.
     * <p>
     * Each entry should map an {@link Integer} to a {@link Pattern} -- 
     * regular expression for a specified constrained string.
     */
    public Map getConstraints()
    {
        return constraints;
    }

    /**
     * Resolves external entites if child filter needs it.
     * Default implementation returns <code>null</code>.
     */
    public InputSource resolveEntity(String publicId, String systemId)
    {
        return null;
    }
}
