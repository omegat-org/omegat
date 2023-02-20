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

import java.io.File;

import org.omegat.core.Core;
import org.omegat.core.matching.NearString;
import org.omegat.util.FileUtil;
import org.omegat.util.OConsts;

public final class DataUtils {

    private DataUtils() {
    }

    public static boolean isDuplicate(SourceTextEntry ste, TMXEntry te) {
        return ste.getDuplicate() == SourceTextEntry.DUPLICATE.NEXT && te.defaultTranslation;
    }

    /** Check if a NearString match comes from the tm/mt/ folder. */
    public static boolean isFromMTMemory(NearString near) {
        if (near == null) {
            return false;
        }
        return near.comesFrom == NearString.MATCH_SOURCE.TM && FileUtil.isInPath(
                new File(Core.getProject().getProjectProperties().getTMRoot(), OConsts.MT_TM),
                new File(near.projs[0]));
    }
}
