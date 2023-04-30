/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2005-2006 Henry Pijffers
               2006 Martin Wunderlich
               2006-2007 Didier Briel
               2008 Martin Fleurke
               2011 Alex Buloichik
               2012 Wildrich Fourie
               2013 Didier Briel
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

package org.omegat.core.data;

import java.util.ArrayList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.segmentation.Rule;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.util.Language;
import org.omegat.util.PatternConsts;
import org.omegat.util.StringUtil;

/**
 * Process one entry on parse source file.
 *
 * This class caches segments for one file, then flushes they. It required to ability to link prev/next
 * segments.
 *
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public abstract class ParseEntry implements IParseCallback {

    private final ProjectProperties config;

    /** Cached segments. */
    private List<ParseEntryQueueItem> parseQueue = new ArrayList<ParseEntryQueueItem>();

    public ParseEntry(final ProjectProperties conf) {
        this.config = conf;
    }

    protected void setCurrentFile(FileInfo fi) {
    }

    protected void fileFinished() {
        /*
         * Flush queue.
         */
        for (ParseEntryQueueItem item : parseQueue) {
            addSegment(item.id, item.segmentIndex, item.segmentSource, item.protectedParts, item.segmentTranslation,
                    item.segmentTranslationFuzzy, item.props, item.prevSegment, item.nextSegment, item.path);
        }

        /*
         * Clear queue for next file.
         */
        parseQueue.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void linkPrevNextSegments() {
        for (int i = 0; i < parseQueue.size(); i++) {
            ParseEntryQueueItem item = parseQueue.get(i);
            try {
                item.prevSegment = parseQueue.get(i - 1).segmentSource;
            } catch (IndexOutOfBoundsException ex) {
                // first entry - previous will be empty
                item.prevSegment = "";
            }
            try {
                item.nextSegment = parseQueue.get(i + 1).segmentSource;
            } catch (IndexOutOfBoundsException ex) {
                // last entry - next will be empty
                item.nextSegment = "";
            }
        }
    }

    /**
     * This method is called by filters to add new entry in OmegaT after read it from source file.
     *
     * @param id
     *            ID of entry, if format supports it
     * @param source
     *            Translatable source string
     * @param translation
     *            translation of the source string, if format supports it
     * @param isFuzzy
     *            flag for fuzzy translation. If a translation is fuzzy, it is not added to the projects TMX,
     *            but it is added to the generated 'reference' TMX, a special TMX that is used as extra
     *            reference during translation.
     * @param props
     *            a staggered array of non-uniquely-identifying key=value properties (metadata) for the entry.
     *            If property is org.omegat.core.data.SegmentProperties.REFERENCE, the entire segment is added only to
     *            the generated 'reference' TMX, a special TMX that is used as extra reference during translation.
     *            The segment is not added to the list of translatable segments.
     *            Property can also be org.omegat.core.data.SegmentProperties.COMMENT, which shown in the comment panel.
     *            Other properties are possible, but have no special meaning in OmegaT.
     * @param path
     *            path of entry in file
     * @param filter
     *            filter which produces entry
     * @param protectedParts
     *            protected parts
     */
    @Override
    public void addEntryWithProperties(String id, String source, String translation, boolean isFuzzy, String[] props,
            String path, IFilter filter, List<ProtectedPart> protectedParts) {
        if (StringUtil.isEmpty(source)) {
            // empty string - not need to save
            return;
        }

        if (props != null && props.length % 2 != 0) {
            throw new IllegalArgumentException(
                    "Entry properties must be in a key=value array with an even number of items.");
        }

        ParseEntryResult tmp = new ParseEntryResult();

        boolean removeSpaces = Core.getFilterMaster().getConfig().isRemoveSpacesNonseg();
        source = stripSomeChars(source, tmp, config.isRemoveTags(), removeSpaces);
        source = StringUtil.normalizeUnicode(source);
        if (config.isRemoveTags() && protectedParts != null) {
            for (int i = 0; i < protectedParts.size(); i++) {
                ProtectedPart p = protectedParts.get(i);
                String s = p.getTextInSourceSegment();
                s = PatternConsts.OMEGAT_TAG.matcher(s).replaceAll("");
                if (s.isEmpty()) {
                    protectedParts.remove(i);
                    i--;
                } else {
                    p.setTextInSourceSegment(s);
                }
            }
        }
        if (translation != null) {
            translation = stripSomeChars(translation, tmp, config.isRemoveTags(), removeSpaces);
            translation = StringUtil.normalizeUnicode(translation);
        }

        if (config.isSentenceSegmentingEnabled()) {
            List<StringBuilder> spaces = new ArrayList<StringBuilder>();
            List<Rule> brules = new ArrayList<Rule>();
            Language sourceLang = config.getSourceLanguage();
            List<String> segments = Core.getSegmenter().segment(sourceLang, source, spaces, brules);
            if (segments.size() == 1) {
                internalAddSegment(id, (short) 0, segments.get(0), translation, isFuzzy, props, path,
                        protectedParts);
            } else {
                for (short i = 0; i < segments.size(); i++) {
                    String onesrc = segments.get(i);
                    List<ProtectedPart> segmentProtectedParts = ProtectedPart.extractFor(protectedParts,
                            onesrc);
                    internalAddSegment(id, i, onesrc, null, false, props, path, segmentProtectedParts);
                }
            }
        } else {
            internalAddSegment(id, (short) 0, source, translation, isFuzzy, props, path, protectedParts);
        }
    }

    /**
     * This method is called by filters to add new entry in OmegaT after read it from source file.
     * <p>
     * Old call for filters that only support extracting a "comment" property. Kept for compatibility.
     */
    @Override
    public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
            String path, IFilter filter, List<ProtectedPart> protectedParts) {
        String[] props = comment == null ? null : new String[] { SegmentProperties.COMMENT, comment };
        addEntryWithProperties(id, source, translation, isFuzzy, props, path, filter, protectedParts);
    }

    /**
     * This method is called by filters to add new entry in OmegaT after read it from source file.
     * <p>
     * Old call without path, for compatibility. Comment is converted to a property.
     *
     * @param id
     *            ID of entry, if format supports it
     * @param source
     *            Translatable source string
     * @param translation
     *            translation of the source string, if format supports it
     * @param isFuzzy
     *            flag for fuzzy translation. If a translation is fuzzy, it is not added to the projects TMX,
     *            but it is added to the generated 'reference' TMX, a special TMX that is used as extra
     *            reference during translation.
     * @param comment
     *            entry's comment, if format supports it
     * @param filter
     *            filter which produces entry
     */
    @Override
    public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
            IFilter filter) {
        addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
    }

    /**
     * Add segment to queue because we possible need to link prev/next segments.
     */
    private void internalAddSegment(String id, short segmentIndex, String segmentSource, String segmentTranslation,
            boolean segmentTranslationFuzzy, String[] props, String path, List<ProtectedPart> protectedParts) {
        if (segmentSource.trim().isEmpty()) {
            // skip empty segments
            return;
        }
        ParseEntryQueueItem item = new ParseEntryQueueItem();
        item.id = id;
        item.segmentIndex = segmentIndex;
        item.segmentSource = segmentSource;
        item.protectedParts = protectedParts;
        item.segmentTranslation = segmentTranslation;
        item.segmentTranslationFuzzy = segmentTranslationFuzzy;
        item.props = props;
        item.path = path;
        parseQueue.add(item);
    }

    /**
     * Adds a segment to the project. If a translation is given, it it added to
     * the projects TMX.
     *
     * @param id
     *            ID of entry, if format supports it
     * @param segmentIndex
     *            Number of the segment-part of the original source string.
     * @param segmentSource
     *            Translatable source string
     * @param protectedParts
     *            protected parts
     * @param segmentTranslation
     *            translation of the source string, if format supports it
     * @param segmentTranslationFuzzy
     *            fuzzy flag of translation of the source string, if format
     *            supports it
     * @param props
     *            entry's properties, like comments, if format supports it
     * @param prevSegment
     *            previous segment's text
     * @param nextSegment
     *            next segment's text
     * @param path
     *            path of segment
     */
    protected abstract void addSegment(String id, short segmentIndex, String segmentSource,
            List<ProtectedPart> protectedParts, String segmentTranslation, boolean segmentTranslationFuzzy,
            String[] props, String prevSegment, String nextSegment, String path);

    /**
     * Strip some chars for represent string in UI.
     *
     * @param src
     *            source string to strip chars
     * @return result
     */
    public static String stripSomeChars(final String src, final ParseEntryResult per, boolean removeTags,
            boolean removeSpaces) {
        String r = src;

        /*
         * AB: we need to find begin/end spaces first, then replace \r,\n chars.
         * Since \r,\n are spaces, we will not need to store spaces in buffer,
         * but we can just remember spaces count at the begin and at the end,
         * then restore spaces from original string.
         */

        /*
         * Some special space handling: skip leading and trailing whitespace and
         * non-breaking-space
         */
        int len = r.length();
        int b = 0;
        if (removeSpaces) {
            for (int cp; b < len; b += Character.charCount(cp)) {
                cp = r.codePointAt(b);
                if (!Character.isWhitespace(cp) && cp != '\u00A0') {
                    break;
                }
            }
        }
        per.spacesAtBegin = b;

        int e = len;
        if (removeSpaces) {
            for (int cp; e > b; e -= Character.charCount(cp)) {
                cp = r.codePointBefore(e);
                if (!Character.isWhitespace(cp) && cp != '\u00A0') {
                    break;
                }
            }
        }
        per.spacesAtEnd = len - e;

        r = r.substring(b, e);

        /*
         * Replacing all occurrences of single CR (\r) or CRLF (\r\n) by LF
         * (\n). This is reversed on create translation. (fix for bug 1462566)
         * We don't need to remember crlf/cr presents on parse, but only on
         * translate.
         */
        per.crlf = r.indexOf("\r\n") > 0;
        if (per.crlf) {
            r = r.replace("\r\n", "\n");
        }
        per.cr = r.indexOf("\r") > 0;
        if (per.cr) {
            r = r.replace("\r", "\n");
        }
        if (removeTags) {
            r = PatternConsts.OMEGAT_TAG.matcher(r).replaceAll("");
        }

        r = StringUtil.removeXMLInvalidChars(r);

        return r;
    }

    /**
     * Storage for results of entry parsing, i.e. cr/crlf flags, spaces counts
     * on the begin and end.
     */
    public static class ParseEntryResult {
        public boolean crlf, cr;
        int spacesAtBegin, spacesAtEnd;
    }

    /**
     * Storage for collected segments.
     */
    protected static class ParseEntryQueueItem {
        String id;
        short segmentIndex;
        String segmentSource;
        List<ProtectedPart> protectedParts;
        String segmentTranslation;
        boolean segmentTranslationFuzzy;
        String[] props;
        String prevSegment;
        String nextSegment;
        String path;
    }
}
