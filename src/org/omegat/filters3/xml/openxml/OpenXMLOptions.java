/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007-2010 Didier Briel
               2010 Antonio Vilei
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

package org.omegat.filters3.xml.openxml;

import java.util.Map;

import org.omegat.filters2.AbstractOptions;

/**
 * Options for OpenXML filter. Serializable to allow saving to / reading from
 * configuration file.
 * <p>
 * OpenDoc filter have the following options ([+] means default on).
 * Translatable elements:
 * <ul>
 * <li>[] Hidden text (Word)
 * <li>[+] Comments (Word, Excel)
 * <li>[+] Footnotes (Word)
 * <li>[+] Endnotes (Word)
 * <li>[+] Header (Word)
 * <li>[+] Footer (Word)
 * <li>[] Diagrams (Word)
 * <li>[+] Slide comments (PowerPoint)
 * <li>[] Slide Masters (PowerPoint)
 * <li>[] Slide Layouts (PowerPoint)
 * <li>[] Charts (PowerPoint)
 * </ul>
 * Generic options:
 * <ul>
 * <li>[+] Tags Aggregation
 * </ul>
 *
 * @author Didier Briel, Antonio Vilei
 */
public class OpenXMLOptions extends AbstractOptions {
    private static final String OPTION_TRANSLATE_HIDDEN_TEXT = "translateHiddenText";
    private static final String OPTION_TRANSLATE_COMMENTS = "translateComments";
    private static final String OPTION_TRANSLATE_FOOTNOTES = "translateFootnotes";
    private static final String OPTION_TRANSLATE_ENDNOTES = "translateEndnotes";
    private static final String OPTION_TRANSLATE_HEADERS = "translateHeaders";
    private static final String OPTION_TRANSLATE_FOOTERS = "translateFooters";
    private static final String OPTION_TRANSLATE_DIAGRAMS = "translateDiagrams";
    private static final String OPTION_TRANSLATE_EXCEL_COMMENTS = "translateExcelComments";
    private static final String OPTION_TRANSLATE_SLIDE_COMMENTS = "translateSlideComments";
    private static final String OPTION_TRANSLATE_SLIDE_MASTERS = "translateSlideMasters";
    private static final String OPTION_TRANSLATE_SLIDE_LAYOUTS = "translateSlideLayouts";
    private static final String OPTION_TRANSLATE_CHARTS = "translateCharts";
    private static final String OPTION_AGGREGATE_TAGS = "aggregateTags";

    public OpenXMLOptions(Map<String, String> options) {
        super(options);
    }

    /**
     * Returns whether Hidden Text should be translated.
     */
    public boolean getTranslateHiddenText() {
        return getBoolean(OPTION_TRANSLATE_HIDDEN_TEXT, false);
    }

    /**
     * Sets whether Hidden Text should be translated.
     */
    public void setTranslateHiddenText(boolean translateHiddenText) {
        setBoolean(OPTION_TRANSLATE_HIDDEN_TEXT, translateHiddenText);
    }

    /**
     * Returns whether Commments should be translated.
     */
    public boolean getTranslateComments() {
        return getBoolean(OPTION_TRANSLATE_COMMENTS, true);
    }

    /**
     * Sets whether Comments should be translated.
     */
    public void setTranslateComments(boolean translateComments) {
        setBoolean(OPTION_TRANSLATE_COMMENTS, translateComments);
    }

    /**
     * Returns whether Footnotes should be translated.
     */
    public boolean getTranslateFootnotes() {
        return getBoolean(OPTION_TRANSLATE_FOOTNOTES, true);
    }

    /**
     * Sets whether Footnotes should be translated.
     */
    public void setTranslateFootnotes(boolean translateFootnotes) {
        setBoolean(OPTION_TRANSLATE_FOOTNOTES, translateFootnotes);
    }

    /**
     * Returns whether Endnotes should be translated.
     */
    public boolean getTranslateEndnotes() {
        return getBoolean(OPTION_TRANSLATE_ENDNOTES, true);
    }

    /**
     * Sets whether Footnotes should be translated.
     */
    public void setTranslateEndnotes(boolean translateEndnotes) {
        setBoolean(OPTION_TRANSLATE_ENDNOTES, translateEndnotes);
    }

    /**
     * Returns whether Headers should be translated.
     */
    public boolean getTranslateHeaders() {
        return getBoolean(OPTION_TRANSLATE_HEADERS, true);
    }

    /**
     * Sets whether Headers should be translated.
     */
    public void setTranslateHeaders(boolean translateHeaders) {
        setBoolean(OPTION_TRANSLATE_HEADERS, translateHeaders);
    }

    /**
     * Returns whether Footers should be translated.
     */
    public boolean getTranslateFooters() {
        return getBoolean(OPTION_TRANSLATE_FOOTERS, true);
    }

    /**
     * Sets whether Footers should be translated.
     */
    public void setTranslateFooters(boolean translateFooters) {
        setBoolean(OPTION_TRANSLATE_FOOTERS, translateFooters);
    }
    /**
     * Returns whether Diagrams should be translated.
     */
    public boolean getTranslateDiagrams() {
        return getBoolean(OPTION_TRANSLATE_DIAGRAMS, false);
    }

    /**
     * Sets whether Diagrams should be translated.
     */
    public void setTranslateDiagrams(boolean translateDiagrams) {
        setBoolean(OPTION_TRANSLATE_DIAGRAMS, translateDiagrams);
    }

    /**
     * Returns whether Excel Comments should be translated.
     */
    public boolean getTranslateExcelComments() {
        return getBoolean(OPTION_TRANSLATE_EXCEL_COMMENTS, true);
    }

    /**
     * Sets whether Excel Comments should be translated.
     */
    public void setTranslateExcelComments(boolean translateExcelComments) {
        setBoolean(OPTION_TRANSLATE_EXCEL_COMMENTS, translateExcelComments);
    }

    /**
     * Returns whether Slide Comments should be translated.
     */
    public boolean getTranslateSlideComments() {
        return getBoolean(OPTION_TRANSLATE_SLIDE_COMMENTS, true);
    }

    /**
     * Sets whether Slide Comments should be translated.
     */
    public void setTranslateSlideComments(boolean translateSlideComments) {
        setBoolean(OPTION_TRANSLATE_SLIDE_COMMENTS, translateSlideComments);
    }

    /**
     * Returns whether Slide Masters should be translated.
     */
    public boolean getTranslateSlideMasters() {
        return getBoolean(OPTION_TRANSLATE_SLIDE_MASTERS, false);
    }

    /**
     * Sets whether Slide Masters should be translated.
     */
    public void setTranslateSlideMasters(boolean translateSlideMasters) {
        setBoolean(OPTION_TRANSLATE_SLIDE_MASTERS, translateSlideMasters);
    }

    /**
     * Returns whether Slide Layouts should be translated.
     */
    public boolean getTranslateSlideLayouts() {
        return getBoolean(OPTION_TRANSLATE_SLIDE_LAYOUTS, false);
    }

    /**
     * Sets whether Slide Layouts should be translated.
     */
    public void setTranslateSlideLayouts(boolean translateSlideLayouts) {
        setBoolean(OPTION_TRANSLATE_SLIDE_LAYOUTS, translateSlideLayouts);
    }
    /**
     * Returns whether Charts should be translated.
     */
    public boolean getTranslateCharts() {
        return getBoolean(OPTION_TRANSLATE_CHARTS, false);
    }

    /**
     * Sets whether Charts should be translated.
     */
    public void setTranslateCharts(boolean translateCharts) {
        setBoolean(OPTION_TRANSLATE_CHARTS, translateCharts);
    }

    /**
     * Returns whether OpenXML tags should be aggregated.
     */
    public boolean getAggregateTags() {
        return getBoolean(OPTION_AGGREGATE_TAGS, true);
    }

    /**
     * Sets whether OpenXML tags should be aggregated.
     */
    public void setAggregateTags(boolean aggregateTags) {
        setBoolean(OPTION_AGGREGATE_TAGS, aggregateTags);
    }

}
