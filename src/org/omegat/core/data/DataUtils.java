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

package org.omegat.core.data;

import org.omegat.core.Core;

public final class DataUtils {

    private DataUtils() {
    }

    public static boolean isDuplicate(SourceTextEntry ste, TMXEntry te) {
        //return ste.getDuplicate() == SourceTextEntry.DUPLICATE.NEXT && te.defaultTranslation;
        if (! te.defaultTranslation) {
            return false;
        }
        if (ste.getDuplicate() != SourceTextEntry.DUPLICATE.NEXT) {
            return false;
        }
        // Must not be first occurence of defaultTranslation
        SourceTextEntry first = ste.firstInstance;
        te = Core.getProject().getTranslationInfo(first);
        boolean foundDefault = te.defaultTranslation;
        for (SourceTextEntry next: first.duplicates) {
            if (next == first) {
                continue;
            }
            if (next != ste) {
                te = Core.getProject().getTranslationInfo(first);
                foundDefault = foundDefault || te.defaultTranslation;
            } else {
                return foundDefault;
            }
        }
        return foundDefault;
    }
}
