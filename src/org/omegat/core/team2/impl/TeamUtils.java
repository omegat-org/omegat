/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Alex Buloichik
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

package org.omegat.core.team2.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Some utility methods for team code.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class TeamUtils {

    private TeamUtils() {
    }

    public static String encodePassword(String pass) {
        if (pass == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(pass.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodePassword(String pass) {
        if (pass == null) {
            return null;
        }
        byte[] data = Base64.getDecoder().decode(pass);
        return new String(data, StandardCharsets.UTF_8);
    }
}
