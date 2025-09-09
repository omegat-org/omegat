/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.gui.issues;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;

import java.util.List;

public class TestingIssueProvider implements IIssueProvider {
    @Override
    public List<IIssue> getIssues(SourceTextEntry sourceEntry, TMXEntry tmxEntry) {
        return List.of();
    }

    @Override
    public String getId() {
        return TestingIssueProvider.class.getCanonicalName();
    }

    @Override
    public String getName() {
        return "TestingIssueProvider";
    }
}
