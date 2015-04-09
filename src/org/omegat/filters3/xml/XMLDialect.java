/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Martin Fleurke
               2009 Didier Briel
               2010 Antonio Vilei
               2011 Didier Briel
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

package org.omegat.filters3.xml;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.omegat.core.data.ProtectedPart;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.Element;
import org.omegat.filters3.Tag;
import org.omegat.util.MultiMap;
import org.xml.sax.InputSource;

/**
 * Interface to describe XML dialect.
 * 
 * @author Maxym Mykhalchuk
 * @author Martin Fleurke
 * @author Didier Briel
 */
public interface XMLDialect {
    /**
     * Returns the set of paragraph tags.
     * <p>
     * Each entry in a set should be a String class.
     */
    Set<String> getParagraphTags();

    /**
     * Returns the set of tags that surround preformatted text.
     * <p>
     * Each entry in a set should be a String class.
     */
    Set<String> getPreformatTags();

    /**
     * Returns the map of content based tags where key is tag name, value is tag type.
     */
    Map<String, Tag.Type> getContentBasedTags();

    /**
     * Returns the set of tags that surround intact portions of document, that
     * should not be translated at all.
     * <p>
     * Each entry in a set should be a String class.
     */
    Set<String> getIntactTags();

    /**
     * Returns the set of "out-of-turn" tags. Such tags specify chunks of text
     * that should be translated separately, not breaking currently collected
     * text entry. For example, footnotes in OpenDocument.
     * <p>
     * Each entry in a set should be a String class.
     */
    Set<String> getOutOfTurnTags();

    /**
     * Returns shortcut string representation of the entry source. This is what
     * the user translates. E.g. for
     * <code>Here's &lt;b&gt;bold text&lt;/b&gt;</code> should return
     * <code>Here's &lt;b0&gt;bold text&lt;/b0&gt;</code>.
     */
    String constructShortcuts(List<Element> elements, List<ProtectedPart> protectedParts);

    /**
     * Returns the multimap of translatable attributes of each tag.
     * <p>
     * Each entry should map from a String to a set of Strings.
     */
    MultiMap<String, String> getTranslatableTagAttributes();

    /**
     * Returns for a given attribute of a given tag if the attribute should be
     * translated with the given other attributes present. If the tagAttribute
     * is returned by getTranslatable(Tag)Attributes(), this function is called
     * to further test the attribute within its context. This allows for example
     * the XHTML filter to not translate the value attribute of an
     * input-element, except when it is a button or submit or reset.
     */
    Boolean validateTranslatableTagAttribute(String tag, String attribute, Attributes atts);

    /**
     * For a given tag, return wether the content of this tag should be
     * translated, depending on the content of one attribute and the presence or
     * absence of other attributes. For instance, in the ResX filter, tags
     * should not be translated when they contain the attribute "type", or when
     * the attribute "name" starts with "&amp;gt";
     * 
     * @param tag
     *            The tag that could be translated
     * @param atts
     *            The list of the tag attributes
     * @return <code>true</code> or <code>false</code>
     */
    Boolean validateIntactTag(String tag, Attributes atts);

    /**
     * For a given tag, return wether the content of this tag should be
     * translated, depending on the content of one attribute and the presence or
     * absence of other attributes.
     * 
     * @param tag
     *            The tag that could be translated
     * @param atts
     *            The list of the tag attributes
     * @return <code>true</code> or <code>false</code>
     */
    Boolean validateContentBasedTag(String tag, Attributes atts);

    /**
     * For a given tag, return wether the content of this tag should be
     * translated, depending on the content of one attribute and the presence or
     * absence of other attributes. For instance, in the Typo3 filter, tags
     * should be translated when the attribute locazible="1".
     * 
     * @param tag
     *            The tag that could be translated
     * @param atts
     *            The list of the tag attributes
     * @return <code>true</code> or <code>false</code>
     */
    Boolean validateTranslatableTag(String tag, Attributes atts);

    /**
     * For a given tag, return wether the content of this tag is a paragraph
     * tag, depending on the content of one attribute (and/or the presence or
     * absence of other attributes). For instance, in the XLIFF filter, the
     * &lt;mark&gt; tag should start a new paragraph when the attribute "mtype"
     * contains "seg".
     * 
     * @param tag
     *            The tag that could be a paragraph tag
     * @param atts
     *            The list of the tag attributes
     * @return <code>true</code> or <code>false</code>
     */
    Boolean validateParagraphTag(String tag, Attributes atts);

    /**
     * For a given tag, return wether the content of this tag is a preformat
     * tag, depending on the content of one attribute (and/or the presence or
     * absence of other attributes). For instance, in the XLIFF filter, the
     * &lt;mark&gt; tag should be a preformat tag when the attribute "mtype"
     * contains "seg".
     * 
     * @param tag
     *            The tag that could be a preformat tag
     * @param atts
     *            The list of the tag attributes
     * @return <code>true</code> or <code>false</code>
     */
    Boolean validatePreformatTag(String tag, Attributes atts);

    /**
     * Returns the set of translatable attributes (no matter what tag they
     * belong to).
     * <p>
     * Each entry in a set should be a String class.
     */
    Set<String> getTranslatableAttributes();

    /**
     * Returns the map of tags to their shortcuts. Shortcut is a short form of a
     * tag visible to translator, and stored in OmegaT's flavor of TMX files.
     * <p>
     * Each entry should map a {@link String} to a {@link String} -- a tag to
     * its shortcut.
     */
    Map<String, String> getShortcuts();

    /**
     * Unboxed (of primitive type </code>int</code>) constraint on Doctype name.
     */
    static final int CONSTRAINT_DOCTYPE_UNBOXED = 1;
    /**
     * Unboxed (of primitive type </code>int</code>) constraint on PUBLIC
     * Doctype declaration.
     */
    static final int CONSTRAINT_PUBLIC_DOCTYPE_UNBOXED = 2;
    /**
     * Unboxed (of primitive type </code>int</code>) constraint on SYSTEM
     * Doctype declaration.
     */
    static final int CONSTRAINT_SYSTEM_DOCTYPE_UNBOXED = 3;
    /**
     * Unboxed (of primitive type </code>int</code>) constraint on root tag
     * name.
     */
    static final int CONSTRAINT_ROOT_UNBOXED = 4;
    /**
     * Unboxed (of primitive type </code>int</code>) constraint on xmlns
     * declaration.
     */
    static final int CONSTRAINT_XMLNS_UNBOXED = 5;

    /** Constraint on Doctype name. */
    static final int CONSTRAINT_DOCTYPE = CONSTRAINT_DOCTYPE_UNBOXED;
    /** Constraint on PUBLIC Doctype declaration. */
    static final int CONSTRAINT_PUBLIC_DOCTYPE = CONSTRAINT_PUBLIC_DOCTYPE_UNBOXED;
    /** Constraint on SYSTEM Doctype declaration. */
    static final int CONSTRAINT_SYSTEM_DOCTYPE = CONSTRAINT_SYSTEM_DOCTYPE_UNBOXED;
    /** Constraint on root tag name. */
    static final int CONSTRAINT_ROOT = CONSTRAINT_ROOT_UNBOXED;
    /** Constraint on xmlns declaration. */
    static final int CONSTRAINT_XMLNS = CONSTRAINT_XMLNS_UNBOXED;

    /**
     * Returns defined constraints to restrict supported subset of XML files.
     * There can be only one constraint of each type, see CONSTRAINT_...
     * constants.
     * <p>
     * Each entry should map an {@link Integer} to a {@link Pattern} -- regular
     * expression for a specified constrained string.
     */
    Map<Integer, Pattern> getConstraints();

    /**
     * Resolves external entites if child filter needs it. Should return
     * <code>null</code> if it doesn't or cannot.
     */
    InputSource resolveEntity(String publicId, String systemId);

    /**
     * Sets closingTag to <code>true</code> or <code>false</code>
     * 
     * @param onOff
     *            The parameter setting wether closing tags should be used or
     *            not for empty tags.
     */
    void setClosingTagRequired(boolean onOff);

    /**
     * Gives the value of closingTagRequired.
     */
    Boolean getClosingTagRequired();

    /**
     * Sets tagsAggregationEnabled to <code>true</code> or <code>false</code>
     * 
     * @param onOff
     *            The parameter setting whether tags aggregation can be enabled
     *            in order to reduce the number of tags shown to the user.
     */
    void setTagsAggregationEnabled(boolean onOff);

    /**
     * Gives the value of tagsAggregationEnabled.
     */
    Boolean getTagsAggregationEnabled();

    /**
     * Gives the value of forceSpacePreserving;
     * @return whether forceSpacePreserving is set.
     */
    Boolean getForceSpacePreserving();

    /**
     * Set forceSpacePreserving.
     */
    void setForceSpacePreserving(boolean onOff);

}
