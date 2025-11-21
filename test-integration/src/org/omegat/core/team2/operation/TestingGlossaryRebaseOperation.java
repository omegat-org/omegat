/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008-2016 Alex Buloichik
               2012 Martin Fleurke
               2013-2017 Aaron Madlon-Kay
               2023 Briac Pilpre
               2025 Hiroshi Miura
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

package org.omegat.core.team2.operation;

import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.TestCoreState;
import org.omegat.util.Log;

import java.io.File;

public class TestingGlossaryRebaseOperation extends GlossaryRebaseOperation implements IRebaseOperation {

    public TestingGlossaryRebaseOperation(ProjectProperties config) {
        super(config);
    }

    @Override
    public void reload(final File file) {
        Log.logDebug("Reloading glossary file {0}", file);
        TestCoreState.getInstance().getGlossaryManager().fileChanged(file);
    }
}
