/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik, Martin Fleurke
               2010 Alex Buloichik, Didier Briel
               2012 Martin Fleurke, Hans-Peter Jacobs
               2015 Aaron Madlon-Kay
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

package org.omegat.gui.editor;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectTMX;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;
import org.omegat.gui.editor.MarkerController.MarkInfo;
import org.omegat.util.*;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIThreadsUtil;

import javax.swing.text.*;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Interface displaying segments in an editor.
 *
 * CLG:  Not sure if we should do this, but thought the old SegmentBuilder will become a CompoundSegmentBuilder
 * (or better name?) since it creates a compound view of both the source and translation(s)
 *
 * but there is some good and well tested code in the SegmentBuilder that I would like to reuse... I think...
 *
 * Refactored from work done by:
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Hans-Peter Jacobs
 * @author Aaron Madlon-Kay
 */
public interface ISegmentBuilder {

    /** Attributes for show text. */
    public static final String SEGMENT_MARK_ATTRIBUTE = "SEGMENT_MARK_ATTRIBUTE";
    public static final String SEGMENT_SPELL_CHECK = "SEGMENT_SPELL_CHECK";

    static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("0000");

    static final String BIDI_LRE = "\u202a";
    static final String BIDI_RLE = "\u202b";
    static final String BIDI_PDF = "\u202c";

    public static final String BIDI_LRM = "\u200e";
    public static final String BIDI_RLM = "\u200f";
    public static final char BIDI_LRM_CHAR = '\u200e';
    public static final char BIDI_RLM_CHAR = '\u200f';


    boolean isDefaultTranslation();

    void setDefaultTranslation(boolean defaultTranslation);

    /**
     * Create element for one segment.
     *
     * @return OmElementSegment
     */
    void createSegmentElement(final boolean isActive, TMXEntry trans);

    void prependSegmentElement(final boolean isActive, TMXEntry trans);


    boolean hasBeenCreated();

    /**
     * Add separator between segments - one empty line.
     */
    public void addSegmentSeparator();


    SourceTextEntry getSourceTextEntry();

    long getDisplayVersion();

    boolean isActive();

    public SourceTextEntry getSource();

    /** Get source text of entry with internal bidi chars, or null if not displayed. */
    String getSourceText();

    /** Get translation text of entry with internal bidi chars, or null if not displayed. */
    String getTranslationText();

    int getStartSourcePosition();

    int getStartTranslationPosition();

    /**
     * Get segment's start position.
     *
     * @return start position
     */
    int getStartPosition();

    /**
     * Get segment's end position.
     *
     * @return end position
     */
    int getEndPosition();



    /**
     * Check if location inside segment.
     */
    boolean isInsideSegment(int location) ;


    void createInputAttributes(Element element, MutableAttributeSet set);



    /**
     * Called on the active entry changed. Required for update translation text.
     */
    void onActiveEntryChanged();

    /**
     * Choose segment part attributes based on rules.
     *
     * @param isSource
     *            is it a source segment or a target segment
     * @param isPlaceholder
     *            is it for a placeholder (OmegaT tag or sprintf-variable etc.) or regular text inside the
     *            segment?
     * @param isRemoveText
     *            is it text that should be removed in the translation?
     * @param isNBSP
     *            is the text a non-breakable space?
     * @return the attributes to format the text
     */
    AttributeSet attrs(boolean isSource, boolean isPlaceholder, boolean isRemoveText, boolean isNBSP) ;


    void resetTextAttributes() ;



}
