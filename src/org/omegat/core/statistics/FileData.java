/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2010 Didier Briel
               2020 Aaron Madlon-Kay
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

import com.fasterxml.jackson.annotation.JsonProperty;

public class FileData {
    @JsonProperty("filename")
    public String filename;
    @JsonProperty("total")
    public StatCount total;
    @JsonProperty("unique")
    public StatCount unique;
    @JsonProperty("remaining")
    public StatCount remaining;
    @JsonProperty("unique-remaining")
    public StatCount remainingUnique;

    public FileData() {
        total = new StatCount();
        unique = new StatCount();
        remaining = new StatCount();
        remainingUnique = new StatCount();
    }
}
