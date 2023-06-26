/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007-2012 Didier Briel
               2013 Piotr Kulik
               2014 Didier Briel, Aaron Madlon-Kay, Piotr Kulik
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters3.xml.xliff;

import java.util.Map;

import org.omegat.filters2.AbstractOptions;

/**
 * Options for the XLIFF filter. Serializable to allow saving to / reading from
 * configuration file.
 * <p>
 * OpenDoc filter have the following options ([+] means default on).
 * Translatable elements:
 * <ul>
 * <li>[] Compatibility with 2.6
 * <li>[x]Previous and next paragraphs
 * <li>[]&lt;trans-unit&gt;&gt; ID
 * <li>[]Force shortcut to "f" for &lt;it pos="end&gt; tags
 * <li>[]Ignore type and ctype attributes when building &lt;ph&gt; tag shortcuts
 * <li>[]Ignore type and ctype attributes when building &lt;bpt&gt;/&lt;ept&gt;
 * tag shortcuts
 * </ul>
 *
 * @author Didier Briel
 * @author Piotr Kulik
 * @author Aaron Madlon-Kay
 */
public class XLIFFOptions extends AbstractOptions {
    private static final String OPTION_26_COMPATIBILITY = "compatibility26";
    private static final String OPTION_FORCE_SHORTCUT_2_F = "forceshortcut2f";
    private static final String OPTION_IGNORE_TYPE_4_PH_TAGS = "ignoretype4phtags";
    private static final String OPTION_IGNORE_TYPE_4_BPT_TAGS = "ignoretype4bpttags";
    private static final String OPTION_CHANGE_TARGET_STATE_NEEDS_REVIEW_T9N = "changetargetstateneedsreviewtranslation";
    @Deprecated
    private static final String OPTION_ALT_TRANS_ID = "alttransid";
    private static final String OPTION_ALT_TRANS_ID_TYPE = "alttransidtype";

    /**
     * Identify how the ID for alternative translations is decided.
     */
    public enum ID_TYPE {
        /** Take the ID from the prev and next segments. */
        CONTEXT,
        /** Take the ID from the &lt;trans-unit&gt;'s XML ID. */
        ELEMENT_ID,
        /** Take the ID from the &lt;trans-unit&gt;'s resname attribute. */
        RESNAME_ATTR
    }

    public XLIFFOptions(Map<String, String> config) {
        super(config);
    }

    /**
     * Returns whether 2.6 compatibility should be applied
     */
    public boolean get26Compatibility() {
        return getBoolean(OPTION_26_COMPATIBILITY, false);
    }

    /**
     * Sets whether 2.6 compatibility should be applied.
     */
    public void set26Compatibility(boolean compatibility26) {
        setBoolean(OPTION_26_COMPATIBILITY, compatibility26);
    }

    /**
     * Return whether the shortcut should be set to "f" for &lt;it pos="end&gt;
     * tags
     */
    public boolean getForceShortcutToF() {
        return getBoolean(OPTION_FORCE_SHORTCUT_2_F, false);
    }

    /**
     * Set whether the shortcut should be set to "f" for &lt;it pos="end&gt;
     * tags
     */
    public void setForceShortcutToF(boolean forceshortcut2f) {
        setBoolean(OPTION_FORCE_SHORTCUT_2_F, forceshortcut2f);
    }

    /**
     * Return whether the type and ctype attributes of &lt;ph&gt; tag should be
     * ignored when building shortcuts
     */
    public boolean getIgnoreTypeForPhTags() {
        return getBoolean(OPTION_IGNORE_TYPE_4_PH_TAGS, false);
    }

    /**
     * Set whether the type and ctype attributes of &lt;ph&gt; tag should be
     * ignored when building shortcuts
     */
    public void setIgnoreTypeForPhTags(boolean ignoreTypeForPhTags) {
        setBoolean(OPTION_IGNORE_TYPE_4_PH_TAGS, ignoreTypeForPhTags);
    }

    /**
     * Return whether the type and ctype attributes of &lt;bpt&gt;/&lt;ept&gt;
     * tags should be ignored when building shortcuts
     */
    public boolean getIgnoreTypeForBptTags() {
        return getBoolean(OPTION_IGNORE_TYPE_4_BPT_TAGS, false);
    }

    /**
     * Set whether the type and ctype attributes of &lt;bpt&gt;/&lt;ept&gt; tags
     * should be ignored when building shortcuts
     */
    public void setIgnoreTypeForBptTags(boolean ignoreTypeForBptTags) {
        setBoolean(OPTION_IGNORE_TYPE_4_BPT_TAGS, ignoreTypeForBptTags);
    }

    /**
     * Return how the ID for alternative translations should be taken:
     * <ul>
     * <li>previous and next paragraph ({@link ID_TYPE#CONTEXT}, default)</li>
     * <li>the &lt;trans-unit&gt; id ({@link ID_TYPE#ELEMENT_ID})</li>
     * <li>the &lt;trans-unit&gt; resname attribute
     * ({@link ID_TYPE#RESNAME_ATTR})</li>
     * </ul>
     */
    public ID_TYPE getAltTransIDType() {
        ID_TYPE result = getEnum(ID_TYPE.class, OPTION_ALT_TRANS_ID_TYPE, null);
        if (result == null) {
            return getBoolean(OPTION_ALT_TRANS_ID, false) ? ID_TYPE.ELEMENT_ID : ID_TYPE.CONTEXT;
        }
        return result;
    }

    /**
     * Set how the ID for alternative translations should be taken:
     * <ul>
     * <li>previous and next paragraph ({@link ID_TYPE#CONTEXT}, default)</li>
     * <li>the &lt;trans-unit&gt; id ({@link ID_TYPE#ELEMENT_ID})</li>
     * <li>the &lt;trans-unit&gt; resname attribute
     * ({@link ID_TYPE#RESNAME_ATTR})</li>
     * </ul>
     */
    public void setAltTransIDType(ID_TYPE idType) {
        setEnum(OPTION_ALT_TRANS_ID_TYPE, idType);
    }

    /**
     * Return whether transit to state="needs-review-translation" instead of
     * "translated"
     */
    public boolean getChangeStateToNeedsReviewTranslation() {
        return getBoolean(OPTION_CHANGE_TARGET_STATE_NEEDS_REVIEW_T9N, false);
    }

    /**
     * Set behavior flag whether change to state="needs-review-translation"
     * instead of "translated"
     */
    public void setStateToReview(final boolean stateToReview) {
        setBoolean(OPTION_CHANGE_TARGET_STATE_NEEDS_REVIEW_T9N, stateToReview);
    }

}
