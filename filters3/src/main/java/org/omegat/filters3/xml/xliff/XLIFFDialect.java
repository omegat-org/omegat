/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2010 Didier Briel
               2013 Alex Buloichik, Didier Briel, Piotr Kulik
               2014 Didier Briel, Aaron Madlon-Kay, Piotr Kulik
               2018 Didier Briel
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

import java.util.List;

import org.omegat.core.data.ProtectedPart;
import org.omegat.core.statistics.StatisticsSettings;
import org.omegat.filters3.Attribute;
import org.omegat.filters3.Attributes;
import org.omegat.filters3.Element;
import org.omegat.filters3.Tag;
import org.omegat.filters3.xml.DefaultXMLDialect;
import org.omegat.filters3.xml.XMLContentBasedTag;
import org.omegat.filters3.xml.XMLTag;
import org.omegat.filters3.xml.XMLText;
import org.omegat.filters3.xml.xliff.XLIFFOptions.ID_TYPE;
import org.omegat.util.InlineTagHandler;
import org.omegat.util.StaticUtils;
import org.omegat.util.StringUtil;

/**
 * This class specifies XLIFF XML Dialect.
 *
 * XLIFF 1.2 specification:
 * http://docs.oasis-open.org/xliff/xliff-core/xliff-core.html
 *
 * @author Didier Briel
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Piotr Kulik
 * @author Aaron Madlon-Kay
 */
public class XLIFFDialect extends DefaultXMLDialect {
    private boolean forceShortCutToF;
    private boolean ignoreTypeForPhTags;
    private boolean ignoreTypeForBptTags;
    private boolean changeStateToNeedsReviewTranslation;
    /**
     * Sets whether alternative translations are identified by previous and next
     * paragraphs or by &lt;trans-unit&gt; ID
     */
    protected ID_TYPE altTransIDType;

    public XLIFFDialect() {
    }

    /**
     * Actually defines the dialect. It cannot be done during creation, because
     * options are not known at that step.
     */
    public void defineDialect(XLIFFOptions options) {

        defineParagraphTags(new String[] { "source", "target", });

        defineOutOfTurnTags(new String[] { "sub", });

        if (options.get26Compatibility()) { // Old tag handling compatible with
                                            // 2.6
            defineIntactTags(new String[] { "source", "header", "bin-unit", "prop-group", "count-group",
                    "alt-trans", "note", "ph", "bpt", "ept", "it", "context", "seg-source", "sdl:seg-defs" });

        } else { // New tag handling
            defineIntactTags(new String[] { "source", "header", "bin-unit", "prop-group", "count-group",
                    "alt-trans", "note", "context", "seg-source", "sdl:seg-defs" });

            defineContentBasedTag("bpt", Tag.Type.BEGIN);
            defineContentBasedTag("ept", Tag.Type.END);
            defineContentBasedTag("it", Tag.Type.ALONE);
            defineContentBasedTag("ph", Tag.Type.ALONE);
            // "mrk", only <mrk mtype="protected"> is content-based tag. see
            // validateContentBasedTag

            forceShortCutToF = options.getForceShortcutToF();
            ignoreTypeForPhTags = options.getIgnoreTypeForPhTags();
            ignoreTypeForBptTags = options.getIgnoreTypeForBptTags();
            altTransIDType = options.getAltTransIDType();
            changeStateToNeedsReviewTranslation = options.getChangeStateToNeedsReviewTranslation();
        }

    }

    /**
     * In the XLIFF filter, the tag &lt;mrk&gt; is a preformat tag when the
     * attribute "mtype" contains "seg".
     *
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     * @return <code>true</code> if this tag should be a preformat tag,
     *         <code>false</code> otherwise
     */
    @Override
    public Boolean validatePreformatTag(String tag, Attributes atts) {
        if (!tag.equalsIgnoreCase("mrk")) {
            return false;
        }
        if (atts != null) {
            if ("seg".equalsIgnoreCase(atts.getValueByName("mtype"))) {
                return true;
            }
        }
        return false;
    }

    /**
     * In the XLKIFF filter, content shouldn't be translated if translate="no"
     * http://docs.oasis-open.org/xliff/v1.2/os/xliff-core.html#translate
     * 
     * @param tag
     *            An XML tag
     * @param atts
     *            The attributes associated with the tag
     * @return <code>false</code> if the content of this tag should be
     *         translated, <code>true</code> otherwise
     */
    @Override
    public Boolean validateIntactTag(String tag, Attributes atts) {
        if (tag.startsWith("str:")) { // All str:* tags should be ignored
            return true;
        }

        if (!tag.equalsIgnoreCase("group") && // Translate can only appear in
                                              // these tags
                !tag.equalsIgnoreCase("trans-unit") && !tag.equalsIgnoreCase("bin-unit")) {
            return false;
        }

        if (atts != null) {
            if ("no".equalsIgnoreCase(atts.getValueByName("translate"))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean validateContentBasedTag(String tag, Attributes atts) {
        return "mrk".equals(tag) && atts != null && "protected".equals(atts.getValueByName("mtype"));
    }

    /**
     * Handle &lt;target state="..."&gt; attribute according to filter settings.
     * 
     * @see <a href=
     *      "https://sourceforge.net/p/omegat/feature-requests/1506/">RFE
     *      #1506</a>
     * @param tag
     *            XML tag to be processed.
     * @param translated
     *            is the value considered translated?
     */
    @Override
    public void handleXMLTag(XMLTag tag, boolean translated) {
        if (!"target".equals(tag.getTag())) {
            return;
        }
        Attribute attr = tag.getAttributeObject("state");
        if (attr == null) {
            return;
        }
        String state = attr.getValue();
        String nextTranslatedState = changeStateToNeedsReviewTranslation ? "needs-review-translation"
                : "translated";
        if (translated && ("needs-translation".equals(state) || "needs-review-translation".equals(state))) {
            attr.setValue(nextTranslatedState);
        } else if ("new".equals(state)) {
            String next = translated ? nextTranslatedState : "needs-translation";
            attr.setValue(next);
        }
    }

    @Override
    public String constructShortcuts(List<Element> elements, List<ProtectedPart> protectedParts) {
        protectedParts.clear();
        // create shortcuts
        InlineTagHandler tagHandler = new InlineTagHandler();

        StringBuilder r = new StringBuilder();
        for (Element el : elements) {
            if (el instanceof XMLContentBasedTag) {
                XMLContentBasedTag tag = (XMLContentBasedTag) el;
                String shortcut = null;
                int shortcutLetter;
                int tagIndex;
                boolean tagProtected;
                if ("bpt".equals(tag.getTag())) {
                    // XLIFF specification requires 'rid' and 'id' attributes,
                    // but some tools uses 'i' attribute like for TMX
                    tagHandler.startBPT(tag.getAttribute("rid"), tag.getAttribute("id"),
                            tag.getAttribute("i"));
                    shortcutLetter = calcTagShortcutLetter(tag, ignoreTypeForBptTags);
                    tagHandler.setTagShortcutLetter(shortcutLetter);
                    tagIndex = tagHandler.endBPT();
                    shortcut = "<"
                            + (shortcutLetter != 0 ? String.valueOf(Character.toChars(shortcutLetter)) : 'f')
                            + tagIndex + '>';
                    tagProtected = false;
                } else if ("ept".equals(tag.getTag())) {
                    tagHandler.startEPT(tag.getAttribute("rid"), tag.getAttribute("id"),
                            tag.getAttribute("i"));
                    tagIndex = tagHandler.endEPT();
                    shortcutLetter = tagHandler.getTagShortcutLetter();
                    shortcut = "</"
                            + (shortcutLetter != 0 ? String.valueOf(Character.toChars(shortcutLetter)) : 'f')
                            + tagIndex + '>';
                    tagProtected = false;
                } else if ("it".equals(tag.getTag())) {
                    tagHandler.startOTHER();
                    tagHandler.setCurrentPos(tag.getAttribute("pos"));
                    tagIndex = tagHandler.endOTHER();
                    // XLIFF specification requires 'open/close' values,
                    // but some tools may use 'begin/end' values like for TMX
                    shortcutLetter = calcTagShortcutLetter(tag);
                    if ("close".equals(tagHandler.getCurrentPos())
                            || "end".equals(tagHandler.getCurrentPos())) {
                        // In some cases, even if we're able to compute a
                        // shortcut, it's better to force to "f"
                        // for better compatibility with corresponding TMX files
                        if (forceShortCutToF) {
                            shortcutLetter = 'f';
                        }
                        shortcut = "</"
                                + (shortcutLetter != 0 ? String.valueOf(Character.toChars(shortcutLetter))
                                        : 'f')
                                + tagIndex + '>';
                    } else {
                        shortcut = "<"
                                + (shortcutLetter != 0 ? String.valueOf(Character.toChars(shortcutLetter))
                                        : 'f')
                                + tagIndex + '>';
                    }
                    tagProtected = false;
                } else if ("ph".equals(tag.getTag())) {
                    tagHandler.startOTHER();
                    tagIndex = tagHandler.endOTHER();
                    shortcutLetter = calcTagShortcutLetter(tag, ignoreTypeForPhTags);
                    shortcut = "<"
                            + (shortcutLetter != 0 ? String.valueOf(Character.toChars(shortcutLetter)) : 'f')
                            + tagIndex + "/>";
                    tagProtected = false;
                } else if ("mrk".equals(tag.getTag())) {
                    tagHandler.startOTHER();
                    tagIndex = tagHandler.endOTHER();
                    shortcutLetter = 'm';
                    shortcut = "<m" + tagIndex + ">" + tag.getIntactContents().sourceToOriginal() + "</m"
                            + tagIndex + ">";
                    tagProtected = true;
                } else {
                    shortcutLetter = 'f';
                    tagIndex = -1;
                    tagProtected = false;
                }
                tag.setShortcutLetter(shortcutLetter);
                tag.setShortcutIndex(tagIndex);
                tag.setShortcut(shortcut);
                r.append(shortcut);
                ProtectedPart pp = new ProtectedPart();
                pp.setTextInSourceSegment(shortcut);
                pp.setDetailsFromSourceFile(tag.toOriginal());
                if (tagProtected) {
                    // protected text with related tags, like <m0>Acme</m0>
                    if (StatisticsSettings.isCountingProtectedText()) {
                        // Protected texts are counted, but related tags are not
                        // counted in the word count
                        pp.setReplacementWordsCountCalculation(StaticUtils.TAG_REPLACEMENT
                                + tag.getIntactContents().sourceToOriginal() + StaticUtils.TAG_REPLACEMENT);
                    } else {
                        // All protected parts are not counted in the word
                        // count(default)
                        pp.setReplacementWordsCountCalculation(StaticUtils.TAG_REPLACEMENT);
                    }
                    pp.setReplacementUniquenessCalculation(StaticUtils.TAG_REPLACEMENT);
                    pp.setReplacementMatchCalculation(tag.getIntactContents().sourceToOriginal());
                } else {
                    // simple tag, like <i0>
                    if (StatisticsSettings.isCountingStandardTags()) {
                        pp.setReplacementWordsCountCalculation(tag.toSafeCalcShortcut());
                    } else {
                        pp.setReplacementWordsCountCalculation(StaticUtils.TAG_REPLACEMENT);
                    }
                    pp.setReplacementUniquenessCalculation(StaticUtils.TAG_REPLACEMENT);
                    pp.setReplacementMatchCalculation(StaticUtils.TAG_REPLACEMENT);
                }
                protectedParts.add(pp);
            } else if (el instanceof Tag) {
                Tag tag = (Tag) el;
                int tagIndex = tagHandler.paired(tag.getTag(), tag.getType());
                tag.setIndex(tagIndex);
                String shortcut = tag.toShortcut();
                r.append(shortcut);
                ProtectedPart pp = new ProtectedPart();
                pp.setTextInSourceSegment(shortcut);
                pp.setDetailsFromSourceFile(tag.toOriginal());
                if (StatisticsSettings.isCountingStandardTags()) {
                    pp.setReplacementWordsCountCalculation(tag.toSafeCalcShortcut());
                } else {
                    pp.setReplacementWordsCountCalculation(StaticUtils.TAG_REPLACEMENT);
                }
                pp.setReplacementUniquenessCalculation(StaticUtils.TAG_REPLACEMENT);
                pp.setReplacementMatchCalculation(StaticUtils.TAG_REPLACEMENT);
                protectedParts.add(pp);
            } else {
                r.append(el.toShortcut());
            }
        }
        return r.toString();
    }

    private int calcTagShortcutLetter(XMLContentBasedTag tag) {
        return calcTagShortcutLetter(tag, false);
    }

    private int calcTagShortcutLetter(XMLContentBasedTag tag, boolean ignoreTypeForPhtags) {
        int s;
        if (!tag.getIntactContents().isEmpty() && (tag.getIntactContents().get(0) instanceof XMLText)) {
            XMLText xmlText = (XMLText) tag.getIntactContents().get(0);
            s = StringUtil.getFirstLetterLowercase(xmlText.getText());
        } else {
            String type = StringUtil.nvl(tag.getAttribute("ctype"), tag.getAttribute("type"));
            if (type != null && !ignoreTypeForPhtags) {
                s = StringUtil.getFirstLetterLowercase(type);
            } else {
                s = 0;
            }
        }
        return s;
    }
}
