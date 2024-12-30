/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2023 Briac Pilpré
               Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.core.statistics;

import org.omegat.util.OStrings;

public enum StatOutputFormat {
    TEXT(1, ".txt"), XML(2, ".xml"), JSON(4, ".json");

    private String fileExtension;
    private int id;

    StatOutputFormat(int id, String fileExtension) {
        this.id = id;
        this.fileExtension = fileExtension;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public int getId() {
        return id;
    }

    public static int getDefaultFormats() {
        return TEXT.id | JSON.id;
    }

    public boolean isSelected(int outputFormats) {
        return (outputFormats & id) != 0;
    }

    public static StatOutputFormat parse(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }

        for (StatOutputFormat mp : StatOutputFormat.values()) {
            if (code.equalsIgnoreCase(mp.name())) {
                return mp;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return OStrings.getString("STATS_FORMAT_" + name());
    }

}
