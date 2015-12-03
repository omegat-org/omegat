/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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

/**
 * @author Aaron Madlon-Kay
 */
public interface IEditorSettings {

    boolean isUseTabForAdvance();

    void setUseTabForAdvance(boolean useTabForAdvance);

    boolean isMarkTranslated();

    void setMarkTranslated(boolean markTranslated);

    boolean isMarkUntranslated();

    void setMarkUntranslated(boolean markUntranslated);

    boolean isMarkAutoPopulated();

    void setMarkAutoPopulated(boolean markAutoPopulated);

    boolean isDisplaySegmentSources();

    void setDisplaySegmentSources(boolean displaySegmentSources);

    boolean isMarkNonUniqueSegments();

    void setMarkNonUniqueSegments(boolean markNonUniqueSegments);

    boolean isMarkNotedSegments();

    void setMarkNotedSegments(boolean markNotedSegments);

    boolean isMarkNBSP();

    void setMarkNBSP(boolean markNBSP);

    boolean isMarkWhitespace();

    void setMarkWhitespace(boolean markWhitespace);

    boolean isMarkBidi();

    void setMarkBidi(boolean markBidi);

    boolean isAutoSpellChecking();

    void setAutoSpellChecking(boolean isNeedToSpell);

    boolean isDoFontFallback();

    void setDoFontFallback(boolean doFallback);

    String getDisplayModificationInfo();

    void setDisplayModificationInfo(String displayModificationInfo);

    void updateTagValidationPreferences();

    void updateViewPreferences();
}
