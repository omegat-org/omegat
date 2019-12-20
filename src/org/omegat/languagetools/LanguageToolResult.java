/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.languagetools;

/**
 * An intermediate result container to abstract over native and network result
 * types.
 *
 * @author Aaron Madlon-Kay
 *
 */
public class LanguageToolResult {
    public final String message;
    public final int start;
    public final int end;
    public final String ruleId;
    public final String ruleDescription;

    public LanguageToolResult(String message, int start, int end, String ruleId, String ruleDescription) {
        this.message = message;
        this.start = start;
        this.end = end;
        this.ruleId = ruleId;
        this.ruleDescription = ruleDescription;
    }
}
