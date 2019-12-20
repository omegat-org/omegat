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

import java.util.Collections;
import java.util.List;

import org.omegat.util.Log;

/**
 * A base bridge implementation that handles errors thrown by subclasses.
 * <p>
 * If the subclass throws an exception, the bridge will log the exception and
 * then simply return empty results from then on. This is OK because the bridge
 * is recreated on project load, so in the case of temporary issues the user can
 * just reload the project to restore functionality.
 * <p>
 * This failsafe is required because LanguageTool checks are called
 * automatically and in large batches, so if an exception is thrown it will
 * likely be thrown repeatedly for all invocations. Especially in the case of
 * the network bridge we would otherwise spam the remote server or even DDoS it.
 *
 * @author Aaron Madlon-Kay
 *
 */
public abstract class BaseLanguageToolBridge implements ILanguageToolBridge {

    private boolean hadError = false;

    @Override
    public final List<LanguageToolResult> getCheckResults(String sourceText, String translationText) {
        if (hadError) {
            return Collections.emptyList();
        }
        try {
            return getCheckResultsImpl(sourceText, translationText);
        } catch (Exception e) {
            Log.logErrorRB(e, "LT_ERROR_ON_CHECK");
            hadError = true;
            return Collections.emptyList();
        }
    }

    protected abstract List<LanguageToolResult> getCheckResultsImpl(String sourceText, String translationText)
            throws Exception;
}
