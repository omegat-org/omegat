/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2008-2016 Alex Buloichik
               2009-2010 Didier Briel
               2012 Guido Leenders, Didier Briel, Martin Fleurke
               2013 Aaron Madlon-Kay, Didier Briel
               2014 Aaron Madlon-Kay, Didier Briel
               2015 Aaron Madlon-Kay
               2017-2018 Didier Briel
               2018 Enrique Estevez Fernandez
               2019 Thomas Cordonnier
               2020 Briac Pilpre
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

package org.omegat.core.team2.fsm.operation;

import java.io.File;

/**
 * Interface for rebase operations that replaces RebaseAndCommit.IRebase
 * with better encapsulation and state management.
 */
public interface IRebaseOperation {
    /**
     * Parse BASE version of file for rebase operation.
     */
    void parseBaseFile(File file) throws Exception;

    /**
     * Parse HEAD version of file for rebase operation.
     */
    void parseHeadFile(File file) throws Exception;

    /**
     * Perform rebase and save result to output file.
     */
    void rebaseAndSave(File out) throws Exception;

    /**
     * Reload project data from the resulted file.
     */
    void reload(File file) throws Exception;

    /**
     * Generate commit comment for this operation.
     */
    String getCommentForCommit();

    /**
     * Get charset for file encoding conversion.
     */
    String getFileCharset(File file) throws Exception;
}
