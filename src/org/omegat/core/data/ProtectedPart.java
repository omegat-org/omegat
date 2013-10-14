/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

package org.omegat.core.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for store protected part's info.
 * 
 * "Protected part" is common term for :
 * 
 * <m0>Acme</m0> - protected text(Acme) with related tags (<m0>,</m0>)
 * 
 * <i1> - tag
 * 
 * $1 - placeholder
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ProtectedPart {
    /**
     * Text which exist in source segment in editor.
     * 
     * Used for identify protected part.
     */
    protected String textInSourceSegment;

    /**
     * Original full tag text or other information from source file.
     * 
     * Used for display tooltips.
     */
    protected String detailsFromSourceFile;

    /**
     * Replacement for word count calculation.
     */
    protected String replacementWordsCountCalculation;

    /**
     * Replacement for unique and remaining calculation.
     */
    protected String replacementUniquenessCalculation;

    /**
     * Replacement for match calculation.
     */
    protected String replacementMatchCalculation;

    public String getTextInSourceSegment() {
        return textInSourceSegment;
    }

    public void setTextInSourceSegment(String textInSourceSegment) {
        this.textInSourceSegment = textInSourceSegment;
    }

    public String getDetailsFromSourceFile() {
        return detailsFromSourceFile;
    }

    public void setDetailsFromSourceFile(String detailsFromSourceFile) {
        this.detailsFromSourceFile = detailsFromSourceFile;
    }

    public String getReplacementWordsCountCalculation() {
        return replacementWordsCountCalculation;
    }

    public void setReplacementWordsCountCalculation(String replacementWordsCountCalculation) {
        this.replacementWordsCountCalculation = replacementWordsCountCalculation;
    }

    public String getReplacementUniquenessCalculation() {
        return replacementUniquenessCalculation;
    }

    public void setReplacementUniquenessCalculation(String replacementUniquenessCalculation) {
        this.replacementUniquenessCalculation = replacementUniquenessCalculation;
    }

    public String getReplacementMatchCalculation() {
        return replacementMatchCalculation;
    }

    public void setReplacementMatchCalculation(String replacementMatchCalculation) {
        this.replacementMatchCalculation = replacementMatchCalculation;
    }

    public static List<ProtectedPart> extractFor(List<ProtectedPart> original, String text) {
        List<ProtectedPart> result = new ArrayList<ProtectedPart>();
        for (ProtectedPart o : original) {
            if (text.contains(o.textInSourceSegment)) {
                result.add(o);
            }
        }
        return result;
    }
}
