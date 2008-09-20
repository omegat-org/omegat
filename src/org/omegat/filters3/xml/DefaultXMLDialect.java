/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Martin Fleurke
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

package org.omegat.filters3.xml;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.omegat.filters3.Attributes;
import org.omegat.util.MultiMap;
import org.xml.sax.InputSource;

/**
 * Helper class for describing a certain XML dialect.
 *
 * @author Maxym Mykhalchuk
 * @author Martin Fleurke
 */
public class DefaultXMLDialect implements XMLDialect
{
    /** The set of defined paragraph tags. */
    private Set<String> paragraphTags = new HashSet<String>();

    /** Defines paragraph tag. Allows duplicates. */
    public void defineParagraphTag(String tag)
    {
        paragraphTags.add(tag);
    }
    /** Defines a set of paragraph tags from an array. Allows duplicates. */
    public void defineParagraphTags(String[] tags)
    {
        for (String tag : tags)
            defineParagraphTag(tag);
    }

    /** The set of defined tags that surround preformatted text. */
    private Set<String> preformatTags = new HashSet<String>();

    /** Defines preformat tag. Allows duplicates. */
    public void definePreformatTag(String tag)
    {
        preformatTags.add(tag);
    }
    /** Defines a set of preformat tags from an array. Allows duplicates. */
    public void definePreformatTags(String[] tags)
    {
        for (String tag : tags)
            definePreformatTag(tag);
    }

    /** The set of defined tags that surround intact text. */
    private Set<String> intactTags = new HashSet<String>();

    /** Defines intact tag. Allows duplicates. */
    public void defineIntactTag(String tag)
    {
        intactTags.add(tag);
    }
    /** Defines a set of intact tags from an array. Allows duplicates. */
    public void defineIntactTags(String[] tags)
    {
        for (String tag : tags)
            defineIntactTag(tag);
    }

    /** The set of defined paragraph tags. */
    private MultiMap<String,String> translatableTagAttributes = new MultiMap<String, String>();

    /** Defines translatable attribute of a tag. */
    public void defineTranslatableTagAttribute(String tag, String attribute)
    {
        translatableTagAttributes.put(tag, attribute);
    }
    /** Defines translatable attributes of a tag. */
    public void defineTranslatableTagAttributes(String tag, String[] attributes)
    {
        for (String attr : attributes)
            defineTranslatableTagAttribute(tag, attr);
    }
    /** Defines translatable attribute of several tags. */
    public void defineTranslatableTagsAttribute(String[] tags, String attribute)
    {
        for (String tag : tags)
            defineTranslatableTagAttribute(tag, attribute);
    }

    /** The set of defined paragraph tags. */
    private Set<String> translatableAttributes = new HashSet<String>();

    /** Defines always translatable attribute (no matter what tag it belongs to). */
    public void defineTranslatableAttribute(String attribute)
    {
        translatableAttributes.add(attribute);
    }
    /** Defines always translatable attributes (no matter what tag it belongs to). */
    public void defineTranslatableAttributes(String[] attributes)
    {
        for (String attr : attributes)
            defineTranslatableAttribute(attr);
    }

    /**
     * The set of defined out of turn tags that surround chunks of text
     * that should be translated separately, not breaking currently
     * collected text.
     */
    private Set<String> outOfTurnTags = new HashSet<String>();

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
        for (String tag : tags)
            defineOutOfTurnTag(tag);
    }

    Map<Integer, Pattern> constraints = new HashMap<Integer, Pattern>();

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

    Map<String,String> shortcuts = new HashMap<String, String>();

    /**
     * Defines a shortcut for a tag, useful for formatting tags.
     * Shortcut is a short form of a tag visible to translator,
     * and stored in OmegaT's flavor of TMX files.
     *
     * @param tag       Tag name.
     * @param shortcut  The shortcut for a tag.
     */
    public void defineShortcut(String tag, String shortcut)
    {
        shortcuts.put(tag, shortcut);
    }
    /**
     * Defines shortcuts for formatting tags.
     * An alternative to calling {@link #defineShortcut(String,String)}
     * multiple times.
     *
     * @param mappings  Array of strings, where even elements (0th, 2nd, etc) are tags,
     *  and odd elements are their corresponding shortcuts.
     */
    public void defineShortcuts(String[] mappings)
    {
        for (int i = 0; i < mappings.length/2; i++)
            defineShortcut(mappings[2*i], mappings[2*i+1]);
    }

    ///////////////////////////////////////////////////////////////////////////
    // XMLDialect Interface Implementation
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns the set of defined paragraph tags.
     * <p>
     * Each entry in a set should be a String class.
     */
    public Set<String> getParagraphTags()
    {
        return paragraphTags;
    }

    /**
     * Returns the set of tags that surround preformatted text.
     * <p>
     * Each entry in a set should be a String class.
     */
    public Set<String> getPreformatTags()
    {
        return preformatTags;
    }


    /**
     * Returns the set of tags that surround intact portions of document,
     * that should not be translated at all.
     * <p>
     * Each entry in a set should be a String class.
     */
    public Set<String> getIntactTags()
    {
        return intactTags;
    }

    /**
     * Returns the multimap of translatable attributes of each tag.
     * <p>
     * Each entry should map from a String to a set of Strings.
     */
    public MultiMap<String, String> getTranslatableTagAttributes()
    {
        return translatableTagAttributes;
    }

    /**
     * Returns for a given attribute of a given tag if the attribute should 
     * be translated with the given other attributes present.
     * If the tagAttribute is returned by getTranslatable(Tag)Attributes(),
     * this function is called to further test the attribute within its context.
     * This allows for example the XHTML filter to not translate the value
     * attribute of an input-element, except when it is a button or submit or
     * reset.
     */
    public Boolean validateTranslatableTagAttribute(String tag, 
                                                    String attribute, 
                                                    Attributes atts) {
        return true;
    }

    /**
     * Returns the set of translatable attributes (no matter what tag they belong to).
     * <p>
     * Each entry in a set should be a String class.
     */
    public Set<String> getTranslatableAttributes()
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
    public Set<String> getOutOfTurnTags()
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
    public Map<Integer, Pattern> getConstraints()
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

    /**
     * Returns the map of tags to their shortcuts.
     * <p>
     * Each entry should map a {@link String} to a {@link String} --
     * a tag to its shortcut.
     */
    public Map<String,String> getShortcuts()
    {
        return shortcuts;
    }
}
