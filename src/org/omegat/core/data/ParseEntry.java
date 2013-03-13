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

package org.omegat.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.omegat.core.data.IProject.FileInfo;
import org.omegat.core.segmentation.Rule;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.util.Language;
import org.omegat.util.PatternConsts;
import org.omegat.util.StaticUtils;
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

    private final ProjectProperties m_config;
    
    /** Cached segments. */
    private List<ParseEntryQueueItem> parseQueue = new ArrayList<ParseEntryQueueItem>();

    public ParseEntry(final ProjectProperties m_config) {
        this.m_config = m_config;
    }

    protected void setCurrentFile(FileInfo fi) {
    }

    protected void fileFinished() {
        /**
         * Flush queue.
         */
        for (ParseEntryQueueItem item : parseQueue) {
            addSegment(item.id, item.segmentIndex, item.segmentSource, item.segmentTranslation,
                    item.segmentTranslationFuzzy, item.comment, item.prevSegment, item.nextSegment, item.path);
        }

        /**
         * Clear queue for next file.
         */
        parseQueue.clear();
    }
    
    /**
     * {@inheritDoc}
     */
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
     * This method is called by filters to add new entry in OmegaT after read it
     * from source file.
     * 
     * @param id
     *            ID of entry, if format supports it
     * @param source
     *            Translatable source string
     * @param translation
     *            translation of the source string, if format supports it
     * @param isFuzzy
     *            flag for fuzzy translation. If a translation is fuzzy, it is
     *            not added to the projects TMX, but it is added to the
     *            generated 'reference' TMX, a special TMX that is used as extra
     *            reference during translation.
     * @param comment
     *            entry's comment, if format supports it
     * @param path
     *            path of entry infile
     * @param filter
     *            filter which produces entry
     * @param shortcutDetails
     *            shortcuts details
     */
    public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment, String path,
            IFilter filter, Map<String, String> shortcutDetails) {
        if (StringUtil.isEmpty(source)) {
            // empty string - not need to save
            return;
        }

        ParseEntryResult tmp = new ParseEntryResult();

        source = stripSomeChars(source, tmp, m_config.isRemoveTags());
        if (translation != null) {
            translation = stripSomeChars(translation, tmp, m_config.isRemoveTags());
        }

        if (m_config.isSentenceSegmentingEnabled()) {
            List<StringBuffer> spaces = new ArrayList<StringBuffer>();
            List<Rule> brules = new ArrayList<Rule>();
            Language sourceLang = m_config.getSourceLanguage();
            List<String> segments = Segmenter.segment(sourceLang, source, spaces, brules);
            if (segments.size() == 1) {
                internalAddSegment(id, (short) 0, segments.get(0), translation, isFuzzy, comment, path);
            } else {
                for (short i = 0; i < segments.size(); i++) {
                    String onesrc = segments.get(i);
                    internalAddSegment(id, i, onesrc, null, false, comment, path);
                }
            }
        } else {
            internalAddSegment(id, (short) 0, source, translation, isFuzzy, comment, path);
        }
    }

     /**
     * This method is called by filters to add new entry in OmegaT after read it
     * from source file.
     * Old call without path, for compatibility
     * @param id
     *            ID of entry, if format supports it
     * @param source
     *            Translatable source string
     * @param translation
     *            translation of the source string, if format supports it
     * @param isFuzzy
     *            flag for fuzzy translation. If a translation is fuzzy, it is
     *            not added to the projects TMX, but it is added to the
     *            generated 'reference' TMX, a special TMX that is used as extra
     *            reference during translation.
     * @param comment
     *            entry's comment, if format supports it
     * @param filter
     *            filter which produces entry
     */
    public void addEntry(String id, String source, String translation, boolean isFuzzy, String comment,
            IFilter filter) {
        addEntry(id, source, translation, isFuzzy, comment, null, filter, null);
    }

    /**
     * Add segment to queue because we possible need to link prev/next segments.
     */
    private void internalAddSegment(String id, short segmentIndex, String segmentSource, String segmentTranslation,
            boolean segmentTranslationFuzzy, String comment, String path) {
        if (segmentSource.length() == 0 || segmentSource.trim().length() == 0) {
            // skip empty segments
            return;
        }
        ParseEntryQueueItem item = new ParseEntryQueueItem();
        item.id = id;
        item.segmentIndex = segmentIndex;
        item.segmentSource = segmentSource;
        item.segmentTranslation = segmentTranslation;
        item.segmentTranslationFuzzy = segmentTranslationFuzzy;
        item.comment = comment;
        item.path = path;
        parseQueue.add(item);
    }

    /**
     * Adds a segment to the project. If a translation is given, it it added to the projects TMX.
     * 
     * @param id
     *            ID of entry, if format supports it
     * @param segmentIndex
     *            Number of the segment-part of the original source string.
     * @param segmentSource
     *            Translatable source string
     * @param segmentTranslation
     *            translation of the source string, if format supports it
     * @param segmentTranslationFuzzy
     *            fuzzy flag of translation of the source string, if format supports it
     * @param comment
     *            entry's comment, if format supports it
     * @param prevSegment
     *            previous segment's text
     * @param nextSegment
     *            next segment's text
     * @param path
     *            path of segment
     */
    protected abstract void addSegment(String id, short segmentIndex, String segmentSource, String segmentTranslation,
            boolean segmentTranslationFuzzy, String comment, String prevSegment, String nextSegment, String path);

    /**
     * Strip some chars for represent string in UI.
     * 
     * @param src
     *            source string to strip chars
     * @return result
     */
    static String stripSomeChars(final String src, final ParseEntryResult per, boolean removeTags) {
        String r = src;

        /**
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
        while (b < len && (Character.isWhitespace(r.charAt(b)) || r.charAt(b) == '\u00A0')) {
            b++;
        }
        per.spacesAtBegin = b;

        int pos = len - 1;
        int e = 0;
        while (pos >= b && (Character.isWhitespace(r.charAt(pos)) || r.charAt(pos) == '\u00A0')) {
            pos--;
            e++;
        }
        per.spacesAtEnd = e;

        r = r.substring(b, pos + 1);

        /*
         * Replacing all occurrences of single CR (\r) or CRLF (\r\n) by LF
         * (\n). This is reversed on create translation. (fix for bug 1462566)
         * We don't need to remember crlf/cr presents on parse, but only on
         * translate.
         */
        per.crlf = r.indexOf("\r\n") > 0;
        if (per.crlf)
            r = r.replace("\r\n", "\n");
        per.cr = r.indexOf("\r") > 0;
        if (per.cr)
            r = r.replace("\r", "\n");

        if(removeTags) {
            r = PatternConsts.OMEGAT_TAG.matcher(r).replaceAll("");
        }

        r = StaticUtils.fixChars(r);

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
        String segmentTranslation;
        boolean segmentTranslationFuzzy;
        String comment;
        String prevSegment;
        String nextSegment;
        String path;
    }
}
