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

package org.omegat.gui.issues;

import java.util.List;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;

/**
 * An interface for providing issues (problems with a translation). Implementers
 * should inspect the source text ({@link SourceTextEntry#getSrcText()}) and
 * target text ({@link TMXEntry#translation}) and return one or more
 * {@link IIssue}s if there is a problem, or an empty list otherwise.
 *
 * @author Aaron Madlon-Kay
 */
public interface IIssueProvider {
    List<IIssue> getIssues(SourceTextEntry sourceEntry, TMXEntry tmxEntry);

    String getId();

    String getName();
}
