/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007-2010 Didier Briel
               2010 Antonio Vilei
               2011-2013 Didier Briel
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
 * <li>[+] Comments (Word)
 * <li>[+] Footnotes (Word)
 * <li>[+] Endnotes (Word)
 * <li>[+] Header (Word)
 * <li>[+] Footer (Word)
 * <li>[+] Comments (Excel)
 * <li>[] Sheet names (Excel)
 * <li>[+] Slide comments (PowerPoint)
 * <li>[] Slide Masters (PowerPoint)
 * <li>[] Slide Layouts (PowerPoint)
 * <li>[] External links (PowerPoint)
 * <li>[] Charts (Global)
 * <li>[] Diagrams (Global)
 * <li>[] Drawings (Global)
 * <li>[] WordArt (Global)
 * </ul>
 * Other options:
 * <ul>
 * <li>[+] Tags Aggregation
 * <li>[+] Preserve space for all tags
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
    private static final String OPTION_TRANSLATE_SHEET_NAMES = "translateSheetNames";
    private static final String OPTION_TRANSLATE_SLIDE_COMMENTS = "translateSlideComments";
    private static final String OPTION_TRANSLATE_SLIDE_MASTERS = "translateSlideMasters";
    private static final String OPTION_TRANSLATE_SLIDE_LAYOUTS = "translateSlideLayouts";
    private static final String OPTION_TRANSLATE_SLIDE_LINKS = "translateSlideLinks";   
    private static final String OPTION_TRANSLATE_CHARTS = "translateCharts";
    private static final String OPTION_TRANSLATE_DRAWINGS = "translateDrawings";
    private static final String OPTION_TRANSLATE_WORDART = "translateWordArt";
    private static final String OPTION_AGGREGATE_TAGS = "aggregateTags";
    private static final String OPTION_PRESERVE_SPACES = "preserveSpaces";

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
     * Returns whether Excel Sheet Names should be translated.
     */
    public boolean getTranslateSheetNames() {
        return getBoolean(OPTION_TRANSLATE_SHEET_NAMES, false);
    }

    /**
     * Sets whether Excel Comments should be translated.
     */
    public void setTranslateSheetNames(boolean translateExcelSheetNames) {
        setBoolean(OPTION_TRANSLATE_SHEET_NAMES, translateExcelSheetNames);
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
     * Returns whether Slide Links should be translated.
     * @return the state of OPTION_TRANSLATE_SLIDE_LINKS
     */
    public boolean getTranslateSlideLinks() {
        return getBoolean(OPTION_TRANSLATE_SLIDE_LINKS, false);
    }

    /**
     * Sets whether Slide Links should be translated.
     * @param translateSlideLinks The option to translate external links
     */
    public void setTranslateSlideLinks(boolean translateSlideLinks) {
        setBoolean(OPTION_TRANSLATE_SLIDE_LINKS, translateSlideLinks);
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
     * Returns whether Drawings should be translated.
     */
    public boolean getTranslateDrawings() {
        return getBoolean(OPTION_TRANSLATE_DRAWINGS, false);
    }

    /**
     * Sets whether Drawings should be translated.
     */
    public void setTranslateDrawings(boolean translateDrawings) {
        setBoolean(OPTION_TRANSLATE_DRAWINGS, translateDrawings);
    }

    /**
     * Returns whether Word art should be translated.
     */
    public boolean getTranslateWordArt() {
        return getBoolean(OPTION_TRANSLATE_WORDART, false);
    }

    /**
     * Sets whether Word art should be translated.
     */
    public void setTranslateWordArt(boolean translateWordArt) {
        setBoolean(OPTION_TRANSLATE_WORDART, translateWordArt);
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

    /**
     * Returns whether spaces should be preserved for all tags
     * @return the state of OPTION_PRESERVE_SPACES
     */
    public boolean getSpacePreserving() {
        return getBoolean(OPTION_PRESERVE_SPACES, true);
    }

    /**
     * Sets whether spaces should be preserved for all tags
     * @param onOff The option for space preserving
     */
    public void setSpacePreserving(boolean onOff) {
        setBoolean(OPTION_PRESERVE_SPACES, onOff);
    }
}
