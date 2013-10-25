/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel
               2010 Antonio Vilei
               2012 Didier Briel
               2013 Alex Buloichik
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

package org.omegat.filters3;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.ProtectedPart;
import org.omegat.filters2.TranslationException;
import org.omegat.filters3.xml.Handler;
import org.omegat.filters3.xml.XMLContentBasedTag;
import org.omegat.filters3.xml.XMLDialect;
import org.omegat.util.PatternConsts;
import org.omegat.util.StringUtil;

/**
 * Translatable entry. Holds a list of source tags and text, translated text and
 * maintains correspondence between tags in source and in target.
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class Entry {
    final XMLDialect xmlDialect;
    final Handler handler;

    public Entry(XMLDialect xmlDialect, Handler handler) {
        this.xmlDialect = xmlDialect;
        this.handler = handler;
    }

    /**
     * Cleans up this entry.
     */
    public void clear() {
        tagsDetected = false;
        elements.clear();
        translation = new String();
        translatedEntry = null;
        textInstance = null;
    }

    // //////////////////////////////////////////////////////////////////////////
    // Dealing with source here
    // //////////////////////////////////////////////////////////////////////////

    private boolean tagsAggregationEnabled = false;

    /**
     * Whether the "first translatable" and "last translatable" tags were
     * detected. They are the first starting tag that has its ending in the
     * paragraph and the last ending tag that has its beginning in the
     * paragraph, respectively.
     */
    private boolean tagsDetected = false;

    private int firstGood;

    /** Returns index of the "first translatable" tag. */
    private int getFirstGood() {
        detectAndEnumerateTags();
        return firstGood;
    }

    private int lastGood;

    /** Returns index of the "last translatable" tag. */
    private int getLastGood() {
        detectAndEnumerateTags();
        return lastGood;
    }

    private Text textInstance = null;

    /** Returns an instance of {@link Text} class used to populate this entry. */
    private Text getTextInstance() {
        detectAndEnumerateTags();
        return textInstance;
    }

    /**
     * Detects the first and the last translatable tags and assigns all tags in
     * translatable region the shortcuts. Basically calls {@link #detectTags()}
     * and {@link #enumerateTags()} if tags were not detected, i.e.
     * {@link #tagsDetected} is false. in the paragraph "last translatable".
     */
    private void detectAndEnumerateTags() {
        if (!tagsDetected) {
            if (tagsAggregationEnabled) {
                aggregateTags();
            }
            detectTags();
            tagsDetected = true;
            enumerateTags(getFirstGood(), getLastGood());
        }
    }

    public void resetTagDetected() {
        tagsDetected = false;
    }

    /**
     * Aggregate tags. The current OpenXML filter finds too many tags, usually
     * causing what users call the "tag soup". Tags aggregation can help
     * alleviate this problem, but can sometimes lead to semantic issues.
     * Aggregation is OK only as a temporary hack, until we improve the OpenXML
     * filter.
     */
    private void aggregateTags() {
        List<Element> newElements = new ArrayList<Element>();
        AggregatedTag aggregated = null;

        for (Element elem : elements) {
            if (elem instanceof Tag) {
                // Add this tag to the aggregated tag
                if (aggregated == null) {
                    aggregated = new AggregatedTag("tag", null, Tag.Type.ALONE, new Attributes());
                }
                aggregated.add((Tag) elem);
            } else {
                /*
                 * This element is not a tag: - add previous aggregated tag (if
                 * any) - add this element
                 */
                if (aggregated != null) {
                    newElements.add(aggregated);
                    aggregated = null;
                }
                newElements.add(elem);
            }
        }
        // Check if there is remaining aggregated tag
        if (aggregated != null) {
            newElements.add(aggregated);
            aggregated = null;
        }

        // Copy everything to elements
        elements.clear();
        for (Element elem : newElements) {
            elements.add(elem);
        }
        newElements.clear();
    }

    /**
     * Detects the first starting tag that has its ending in the paragraph
     * "first translatable" and the last ending tag that has its beginning in
     * the paragraph "last translatable".
     */
    private void detectTags() {
        // first, detecting if we have any text and where we have it
        int textStart = -1;
        for (int i = 0; i < size(); i++) {
            Element elem = get(i);
            if ((elem instanceof Text) && ((Text) elem).isMeaningful()) {
                textStart = i;
                break;
            }
        }
        for (int i = 0; i < size(); i++) {
            Element elem = get(i);
            if ((elem instanceof Text) && ((Text) elem).isMeaningful()) {
                textInstance = (Text) elem;
                break;
            }
        }
        if (textStart < 0) {
            // we have no translatable text in the whole entry
            firstGood = -1;
            lastGood = -2;
            textInstance = null;
            return;
        }

        int textEnd = textStart;
        for (int i = size() - 1; i >= 0; i--) {
            Element elem = get(i);
            if ((elem instanceof Text) && ((Text) elem).isMeaningful()) {
                textEnd = i;
                break;
            }
        }

        // if content-based tag is inside text, then expand text into paired cntent-based tag
        for (int i = textStart; i <= textEnd; i++) {
            Element elem = get(i);
            if (elem instanceof XMLContentBasedTag) {
                XMLContentBasedTag tag = (XMLContentBasedTag) elem;
                if (tag.getTag().equals("bpt") || tag.getTag().equals("ept")) {
                    // find id of paired tag
                    String id = StringUtil.nvl(tag.getAttribute("rid"), tag.getAttribute("id"),
                            tag.getAttribute("i"));
                    if (id == null) {
                        continue;
                    }
                    // find paired tag before
                    for (int j = textStart - 1; j >= 0; j--) {
                        if (get(j) instanceof XMLContentBasedTag) {
                            XMLContentBasedTag tag2 = (XMLContentBasedTag) get(j);
                            if (tag2.getTag().equals("bpt") || tag2.getTag().equals("ept")) {
                                // find id of paired tag
                                String id2 = StringUtil.nvl(tag2.getAttribute("rid"),
                                        tag2.getAttribute("id"), tag2.getAttribute("i"));
                                if (id.equals(id2)) {
                                    textStart = j;
                                }
                            }
                        }
                    }
                    // find paired tag after
                    for (int j = textEnd + 1; j < size(); j++) {
                        if (get(j) instanceof XMLContentBasedTag) {
                            XMLContentBasedTag tag2 = (XMLContentBasedTag) get(j);
                            if (tag2.getTag().equals("bpt") || tag2.getTag().equals("ept")) {
                                // find id of paired tag
                                String id2 = StringUtil.nvl(tag2.getAttribute("rid"),
                                        tag2.getAttribute("id"), tag2.getAttribute("i"));
                                if (id.equals(id2)) {
                                    textEnd = j;
                                }
                            }
                        }
                    }
                }
            }
        }

        // //////////////////////////////////////////////////////////////////////
        // "first good"
        // detecting the first starting tag that has its ending in the paragraph
        boolean found = false;
        for (firstGood = 0; firstGood < textStart; firstGood++) {
            Element goodElem = get(firstGood);
            if (!(goodElem instanceof Tag))
                continue;

            Tag good = (Tag) goodElem;
            if (Tag.Type.BEGIN != good.getType())
                continue;

            // trying to test
            int recursion = 1;
            for (int i = firstGood + 1; i < textEnd; i++) {
                Element candElement = (Element) get(i);
                if (candElement instanceof Tag) {
                    Tag cand = (Tag) candElement;
                    if (cand.getTag().equals(good.getTag())) {
                        if (Tag.Type.BEGIN == cand.getType())
                            recursion++;
                        else if (Tag.Type.END == cand.getType()) {
                            recursion--;
                            if (recursion == 0) {
                                if (i > textStart)
                                    found = true;
                                break;
                            }
                        }
                    }
                }
            }
            // if we could find an ending, this is a "good one"
            if (found)
                break;
        }
        if (!found)
            firstGood = textStart;

        // //////////////////////////////////////////////////////////////////////
        // "last good"
        // detecting the last ending tag that has its starting in the paragraph
        found = false;
        for (lastGood = size() - 1; lastGood > textEnd; lastGood--) {
            Element goodElem = get(lastGood);
            if (!(goodElem instanceof Tag))
                continue;

            Tag good = (Tag) goodElem;
            if (Tag.Type.END != good.getType())
                continue;

            // trying to test
            int recursion = 1;
            for (int i = lastGood - 1; i > textStart; i--) {
                Element candElement = get(i);
                if (candElement instanceof Tag) {
                    Tag cand = (Tag) candElement;
                    if (cand.getTag().equals(good.getTag())) {
                        if (Tag.Type.END == cand.getType())
                            recursion++;
                        else if (Tag.Type.BEGIN == cand.getType()) {
                            recursion--;
                            if (recursion == 0) {
                                if (i < textEnd)
                                    found = true;
                                break;
                            }
                        }
                    }
                }
            }
            // if we coud find a starting, this is a "good one"
            if (found)
                break;
        }
        if (!found)
            lastGood = textEnd;

        boolean removeTags = Core.getFilterMaster().getConfig().isRemoveTags();
        // tags was already removed - restore they if need
        if (!removeTags) {
            for (int i = firstGood - 1; i >= 0; i--) {
                Element elem = get(i);
                if (elem instanceof Tag) {
                    if (handler.isParagraphTag((Tag) elem)) {
                        break;
                    }
                    firstGood = i;
                }
            }
            for (int i = lastGood + 1; i < size(); i++) {
                Element elem = get(i);
                if (elem instanceof Tag) {
                    if (handler.isParagraphTag((Tag) elem)) {
                        break;
                    }
                    lastGood = i;
                }
            }
        }

        boolean removeSpacesAround = Core.getFilterMaster().getConfig().isRemoveSpacesNonseg();
        // spaces was already removed - restore they if need
        if (!removeSpacesAround) {
            for (int i = firstGood - 1; i >= 0; i--) {
                Element elem = get(i);
                if (elem instanceof Tag) {
                    if (handler.isParagraphTag((Tag) elem)) {
                        break;
                    }
                }
                if ((elem instanceof Text) && !((Text) elem).isMeaningful()) {
                    firstGood = i;
                }
            }
            for (int i = lastGood + 1; i < size(); i++) {
                Element elem = get(i);
                if (elem instanceof Tag) {
                    if (handler.isParagraphTag((Tag) elem)) {
                        break;
                    }
                }
                if ((elem instanceof Text) && !((Text) elem).isMeaningful()) {
                    lastGood = i;
                }
            }
        }
    }

    /**
     * Enumerates tags to be properly shortcut.
     */
    private void enumerateTags(int firstGood, int lastGood) {
        int n = 0;
        for (int i = firstGood; i <= lastGood; i++) {
            Element elem = get(i);
            if (elem instanceof Tag) {
                Tag tag = (Tag) elem;
                if (Tag.Type.ALONE == tag.getType() || Tag.Type.BEGIN == tag.getType()) {
                    tag.setIndex(n);
                    n++;
                } else if (Tag.Type.END == tag.getType()) {
                    tag.setIndex(-1); // indication of an error
                    // trying to lookup for appropriate starting tag
                    int recursion = 1;
                    for (int j = i - 1; j >= firstGood; j--) {
                        Element otherElem = get(j);
                        if (otherElem instanceof Tag) {
                            Tag other = (Tag) otherElem;
                            if (other.getTag().equals(tag.getTag())) {
                                if (Tag.Type.END == other.getType())
                                    recursion++;
                                else if (Tag.Type.BEGIN == other.getType()) {
                                    recursion--;
                                    if (recursion == 0) {
                                        tag.setIndex(other.getIndex());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (tag.getIndex() < 0) // ending tag without a starting one
                    {
                        tag.setIndex(n);
                        n++;
                    }
                }
            }
        }
    }

    /**
     * Returns shortcut string representation of the entry source. This is what
     * the user translates. E.g. for
     * <code>Here's &lt;b&gt;bold text&lt;/b&gt;</code> should return
     * <code>Here's &lt;b0&gt;bold text&lt;/b0&gt;</code>.
     * 
     * @param tagsAggregation
     *            Whether tags of this entry can be aggregated.
     * @param xmlDialect
     *            dialect for processing shortcuts
     * @param shortcutDetails
     *            shortcuts details
     */
    public String sourceToShortcut(boolean tagsAggregation, XMLDialect xmlDialect, List<ProtectedPart> protectedParts) {
        if (tagsAggregation != this.tagsAggregationEnabled) {
            this.tagsAggregationEnabled = tagsAggregation;
            // Each change to tags aggregation setting resets detected tags
            tagsDetected = false;
        }

        if (getFirstGood() <= getLastGood()) {
            return xmlDialect.constructShortcuts(elements.subList(getFirstGood(), getLastGood() + 1), protectedParts);
        } else {
            return "";
        }
    }

    private String sourceToShortcut(XMLDialect xmlDialect, List<ProtectedPart> protectedParts) {
        return sourceToShortcut(tagsAggregationEnabled, xmlDialect, protectedParts);
    }

    /**
     * Returns long XML-encoded representation of the source entry for storing
     * in TMX. E.g. for <code>Here's &lt;b&gt;bold text&lt;/b&gt;</code> should
     * return <code>Here's &lt;bpt i="0"&gt;&amp;b0&amp;gt;&lt;/bpt&gt;bold 
     *       text&lt;ept i="0"&gt;&amp;lt;/b0&amp;gt;&lt;/ept&gt;</code>.
     */
    public String sourceToTMX() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < size(); i++)
            buf.append(get(i).toTMX());
        return buf.toString();
    }

    /**
     * Returns the entry source in its original form as it was in original
     * document. E.g. for <code>Here's &lt;b&gt;bold text&lt;/b&gt;</code>
     * should return the same string
     * <code>Here's &lt;b&gt;bold text&lt;/b&gt;</code>.
     */
    public String sourceToOriginal() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < size(); i++)
            buf.append(get(i).toOriginal());
        return buf.toString();
    }

    // //////////////////////////////////////////////////////////////////////////
    // Dealing with translation
    // //////////////////////////////////////////////////////////////////////////

    /** Translation in shortcut form. */
    private String translation = new String();

    Entry translatedEntry = null;

    /**
     * Sets the translation of the shortcut string returned by
     * {@link #toShortcut()}. Before setting translation checks whether the
     * translation contains all the same tags in weakly correct order:
     * <ul>
     * <li>All the tags present in source must be present in translation. For
     * example, <code>It's &lt;b&gt;bold&lt;/b&gt; text</code> should <b>not</b>
     * be translated as <code>Etot tekst poluzhirnyi</code>.
     * <li>End tag goes after corresponding beginning tag. For example,
     * <code>It's &lt;b&gt;bold&lt;/b&gt; text</code> should <b>not</b> be
     * translated as <code>Etot tekst &lt;/b&gt;poluzhirnyi&lt;b&gt;</code>.
     * <li>If standalone tag or tag pair was enclosed in another tag pair in
     * source, it should be enclosed in translation. For example,
     * <code>It's &lt;b&gt;bold and &lt;i&gt;bold italic&lt;/i&gt;&lt;/b&gt; text</code>
     * should <b>not</b> be translated as
     * <code>Etot tekst &lt;b&gt;poluzhirnyi&lt;/b&gt; i &lt;i&gt;naklonnyi&lt;/i&gt;</code>.
     * <li>Independent standalone tags and tag pairs may be reordered within
     * entry. For example,
     * <code>It's &lt;b&gt;bold&lt;/b&gt; and &lt;i&gt;italic&lt;/i&gt; text</code>
     * <b>can</b> be translated as
     * <code>Etot tekst &lt;i&gt;naklonnyi&lt;/i&gt; i &lt;b&gt;poluzhirnyi&lt;/b&gt;</code>.
     * </ul>
     * 
     * @throws TranslationException
     *             -- if any tag is missing or tags are ordered incorrectly.
     */
    public void setTranslation(String translation, XMLDialect xmlDialect, List<ProtectedPart> protectedParts)
            throws TranslationException {
        if (!sourceToShortcut(xmlDialect, protectedParts).equals(translation)) {
            checkAndRecoverTags(translation);
            this.translation = translation;
        }
    }

    /**
     * Before setting translation checks whether the translation contains all
     * the same tags in weakly correct order. See
     * {@link #setTranslation(String)} for details.
     */
    private void checkAndRecoverTags(String translation) throws TranslationException {
        translatedEntry = new Entry(xmlDialect, handler);

        // /////////////////////////////////////////////////////////////////////
        // recovering tags
        List<ShortTag> shortTags = listShortTags(translation);
        int pos = 0;
        for (ShortTag shortTag : shortTags) {
            if (pos < shortTag.pos) {
                translatedEntry.add(getTextInstance()
                        .createInstance(translation.substring(pos, shortTag.pos)));
                pos = shortTag.pos;
            }
            for (int j = getFirstGood(); j <= getLastGood(); j++) {
                Element longElem = get(j);
                if (longElem instanceof Tag) {
                    Tag longTag = (Tag) longElem;
                    if (longTag.toShortcut().equals(shortTag.tag)) {
                        translatedEntry.add(longTag);
                        pos += shortTag.tag.length();
                        break;
                    }
                }
            }
            // P.S. If shortcut tag isn't found, probably we should issue a
            // warning.
        }
        if (pos < translation.length())
            translatedEntry.add(getTextInstance().createInstance(translation.substring(pos)));

        // /////////////////////////////////////////////////////////////////////
        // checking tags
        // TODO: implement checking
    }

    class ShortTag {
        String tag;
        int pos;

        ShortTag(String tag, int pos) {
            this.tag = tag;
            this.pos = pos;
        }
    }

    /**
     * Lists all OmegaT-style tags within the supplied string. Everything that
     * looks like <code>&lt;xx0&gt;</code>, <code>&lt;yy1/&gt;</code> or
     * <code>&lt;/zz2&gt;</code> is considered to probably be a tag.
     * 
     * @return List of {@link #ShortTag} classes.
     */
    private List<ShortTag> listShortTags(String str) {
        // The code is nearly the same as in buildTagList in StaticUtils.java
        final int STATE_NORMAL = 1;
        final int STATE_COLLECT_TAG = 2;

        int state = STATE_NORMAL;

        List<ShortTag> res = new ArrayList<ShortTag>(str.length() / 4);
        StringBuffer tag = new StringBuffer(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '<') // Possible start of a tag
            {
                tag.setLength(0);
                tag.append(c);
                state = STATE_COLLECT_TAG;
            } else if (c == '>') // Possible end of a tag
            {
                // checking if the tag looks like OmegaT tag,
                // not 100% correct, but is the best what I can think of now
                tag.append(c);
                if (PatternConsts.OMEGAT_TAG.matcher(tag).matches()) {
                    res.add(new ShortTag(tag.toString(), 1 + i - tag.length()));
                    tag.setLength(0);
                    state = STATE_NORMAL;
                }
            } else if (state == STATE_COLLECT_TAG)
                tag.append(c);
        }
        return res;
    }

    /**
     * Returns long XML-encoded representation of the entry translation for
     * storing in TMX.
     */
    public String translationToTMX() {
        if (translatedEntry == null)
            return sourceToTMX();

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < getFirstGood(); i++)
            buf.append(get(i).toTMX());

        buf.append(translatedEntry.sourceToTMX());

        for (int i = getLastGood() + 1; i < size(); i++)
            buf.append(get(i).toTMX());

        return buf.toString();
    }

    /**
     * Returns the translated entry as it should be stored in translated
     * document.
     */
    public String translationToOriginal() {
        if (translatedEntry == null)
            return sourceToOriginal();

        StringBuffer buf = new StringBuffer();

        for (int i = 0; i < getFirstGood(); i++)
            buf.append(get(i).toOriginal());

        buf.append(translatedEntry.sourceToOriginal());

        for (int i = getLastGood() + 1; i < size(); i++)
            buf.append(get(i).toOriginal());

        return buf.toString();
    }

    // /////////////////////////////////////////////////////////////////////////
    // List of EntryElement objects.
    // /////////////////////////////////////////////////////////////////////////

    /** Elements (tags and text) of this entry. */
    private List<Element> elements = new ArrayList<Element>();

    /**
     * Adds an element to this entry. Can be either a {@link Text} or a
     * {@link Tag}.
     */
    public void add(Element elem) {
        elements.add(elem);
        tagsDetected = false; // each addition of the new entry resets detected
                              // tags
    }

    /** Removes an element from this entry. */
    public void remove(int index) {
        elements.remove(index);
        tagsDetected = false; // each deletion of the entry resets detected tags
    }

    /** Gets an element. Can be either a {@link Text} or a {@link Tag}. */
    public Element get(int i) {
        return elements.get(i);
    }

    /** Returns the number of source elements. */
    public int size() {
        return elements.size();
    }
}
