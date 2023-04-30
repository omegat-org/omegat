/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2011 Alex Buloichik
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
import java.util.Locale;
import java.util.Objects;

import org.omegat.core.Core;
import org.omegat.core.segmentation.Rule;
import org.omegat.filters2.ITranslateCallback;
import org.omegat.util.Language;
import org.omegat.util.StringUtil;
import org.omegat.util.TagUtil;

/**
 * Base class for entry translation.
 *
 * This class collects all segments which should be translated, for ability to link prev/next segments for the
 * seconds pass.
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Wildrich Fourie
 * @author Didier Briel
 */
public abstract class TranslateEntry implements ITranslateCallback {

    private final ProjectProperties config;

    private int pass;

    /** Collected segments. */
    private List<TranslateEntryQueueItem> translateQueue = new ArrayList<TranslateEntryQueueItem>();

    /**
     * Index of currently processed segment. It required for multiple translation for use right segment.
     */
    private int currentlyProcessedSegment;

    public TranslateEntry(final ProjectProperties config) {
        this.config = config;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPass(int pass) {
        this.pass = pass;
        currentlyProcessedSegment = 0;
    }

    protected void fileStarted() {
        currentlyProcessedSegment = 0;
    }

    abstract String getCurrentFile();

    protected void fileFinished() {
        if (currentlyProcessedSegment != translateQueue.size()) {
            throw new RuntimeException("Invalid two-pass processing: number of segments are not equals");
        }
        translateQueue.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTranslation(final String id, final String origSource, final String path) {
        ParseEntry.ParseEntryResult spr = new ParseEntry.ParseEntryResult();

        // fix for bug 3487497;
        // Fetch removed tags if the options
        // has been enabled.
        String tags = null;
        if (config.isRemoveTags()) {
            tags = TagUtil.buildTagListForRemove(origSource);
        }

        boolean removeSpaces = Core.getFilterMaster().getConfig().isRemoveSpacesNonseg();
        final String source = StringUtil.normalizeUnicode(ParseEntry.stripSomeChars(
                origSource, spr, config.isRemoveTags(), removeSpaces));

        StringBuilder res = new StringBuilder();

        if (config.isSentenceSegmentingEnabled()) {
            boolean translated = false;
            List<StringBuilder> spaces = new ArrayList<StringBuilder>();
            List<Rule> brules = new ArrayList<Rule>();
            Language sourceLang = config.getSourceLanguage();
            Language targetLang = config.getTargetLanguage();
            List<String> segments = Core.getSegmenter().segment(sourceLang, source, spaces, brules);
            for (int i = 0; i < segments.size(); i++) {
                String onesrc = segments.get(i);
                String tr = internalGetSegmentTranslation(id, i, onesrc, path);
                if (tr == null) {
                    tr = onesrc;
                } else {
                    translated = true;
                }
                segments.set(i, tr);
            }
            if (!translated) {
                return null; // there is no even one translated segment
            }
            res.append(Core.getSegmenter().glue(sourceLang, targetLang, segments, spaces, brules));
        } else {
            String tr = internalGetSegmentTranslation(id, 0, source, path);
            if (tr == null) {
                return null; // non-translated
            }
            res.append(tr);
        }

        // replacing all occurrences of LF (\n) by either single CR (\r) or CRLF
        // (\r\n)
        // this is a reversal of the process at the beginning of this method
        // fix for bug 1462566
        String r = res.toString();

        //- Word: anything placed before the leading tag is omitted in translated document
        // https://sourceforge.net/p/omegat/bugs/634/
        // This is a Word document, Remove Tags (from Project Properties) is not checked and Remove leading and
        // trailing tags (from File Filters) is not checked
        String fileName = getCurrentFile().toLowerCase(Locale.ENGLISH);
        if ((fileName.endsWith(".docx") || fileName.endsWith(".docm")) && !config.isRemoveTags()
                && !Core.getFilterMaster().getConfig().isRemoveTags()) {
            // Locate the location of the first tag
            String firstTag = TagUtil.getFirstTag(r);
            if (firstTag != null) {
                int locFirstTag = r.indexOf(firstTag);
                // Is there text before that first tag?
                if (locFirstTag > 0) {
                    // Was the first tag between two words without any spaces around?
                    String addSpace = "";
                    if (!Character.isWhitespace(r.codePointBefore(locFirstTag))
                            && !Character.isWhitespace(r.codePointAt(locFirstTag + firstTag.length()))
                            && Core.getProject().getProjectProperties().getTargetLanguage().isSpaceDelimited()) {
                        addSpace = " ";
                    }
                    // Move that first tag before the text, adding a space if needed.
                    r = firstTag + r.substring(0, locFirstTag) + addSpace
                            + r.substring(locFirstTag + firstTag.length());
                }
            }
        }

        // fix for bug 3487497;
        // explicitly add the removed tags at
        // the end of the translated string.
        if (config.isRemoveTags()) {
            r += tags;
        }

        if (spr.crlf) {
            r = r.replace("\n", "\r\n");
        } else if (spr.cr) {
            r = r.replace("\n", "\r");
        }

        if (spr.spacesAtBegin > 0) {
            r = origSource.substring(0, spr.spacesAtBegin) + r;
        }

        if (spr.spacesAtEnd > 0) {
            r = r + origSource.substring(origSource.length() - spr.spacesAtEnd);
        }

        return r;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTranslation(final String id, final String origSource) {
        return getTranslation(id, origSource, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void linkPrevNextSegments() {
        for (int i = 0; i < translateQueue.size(); i++) {
            TranslateEntryQueueItem item = translateQueue.get(i);
            try {
                item.prevSegment = translateQueue.get(i - 1).segmentSource;
            } catch (IndexOutOfBoundsException ex) {
                // first entry - previous will be empty
                item.prevSegment = "";
            }
            try {
                item.nextSegment = translateQueue.get(i + 1).segmentSource;
            } catch (IndexOutOfBoundsException ex) {
                // last entry - next will be empty
                item.nextSegment = "";
            }
        }
    }

    /**
     * This method calls real method for empty prev/next on the first pass, then with real prev/next on the
     * second pass.
     *
     * @param id
     * @param segmentIndex
     * @param segmentSource
     * @return
     */
    private String internalGetSegmentTranslation(String id, int segmentIndex, String segmentSource, String path) {
        if (segmentSource.trim().isEmpty()) {
            // empty segment
            return segmentSource;
        }

        TranslateEntryQueueItem item;
        switch (pass) {
        case 1:
            item = new TranslateEntryQueueItem();
            item.id = id;
            item.segmentIndex = segmentIndex;
            item.segmentSource = segmentSource;
            translateQueue.add(item);
            currentlyProcessedSegment++;
            return getSegmentTranslation(id, segmentIndex, segmentSource, null, null, path);
        case 2:
            item = translateQueue.get(currentlyProcessedSegment);
            if (!Objects.equals(id, item.id) || segmentIndex != item.segmentIndex
                    || !Objects.equals(segmentSource, item.segmentSource)) {
                throw new RuntimeException("Invalid two-pass processing: not equals fields");
            }
            currentlyProcessedSegment++;
            return getSegmentTranslation(id, segmentIndex, segmentSource, item.prevSegment, item.nextSegment, path);
        default:
            throw new RuntimeException("Invalid pass number: " + pass);
        }
    }

    protected abstract String getSegmentTranslation(String id, int segmentIndex, String segmentSource,
            String prevSegment, String nextSegment, String path);

    /**
     * Storage for cached segments.
     */
    protected static class TranslateEntryQueueItem {
        String id;
        int segmentIndex;
        String segmentSource;
        String prevSegment;
        String nextSegment;
    }
}
