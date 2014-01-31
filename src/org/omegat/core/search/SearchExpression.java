/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Henry Pijffers
               2009 Didier Briel
               2010 Martin Fleurke, Antonio Vilei, Didier Briel
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

package org.omegat.core.search;

import org.omegat.util.OConsts;

/**
 * Storage for what to search for (search text and options).
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Antonio Vilei
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SearchExpression {
    public enum SearchExpressionType {
        EXACT, KEYWORD, REGEXP
    };

    public enum SearchPlace {
        SOURCE_TRANSLATION, SOURCE_ONLY, TRANSLATION_ONLY
    };

    public enum SearchState {
        TRANSLATED_UNTRANSLATED, TRANSLATED, UNTRANSLATED
    };

    public SearchMode mode;
    public String text;
    public String rootDir;
    public boolean recursive = true;
    public SearchExpressionType searchExpressionType;
    public boolean caseSensitive = false;
    public boolean glossary = true;
    public boolean memory = true;
    public boolean tm = true;
    public boolean allResults = false;
    public boolean searchSource = true;
    public boolean searchTarget = true;
    public boolean searchTranslated;
    public boolean replaceTranslated;
    public boolean replaceUntranslated;
    public boolean searchUntranslated;
    public boolean searchNotes = true;
    public boolean searchComments = true;
    public boolean searchAuthor = false;
    public String author = "";
    public boolean searchDateAfter = false;
    public long dateAfter;
    public boolean searchDateBefore = false;
    public long dateBefore;
    public int numberOfResults = OConsts.ST_MAX_SEARCH_RESULTS;
}
