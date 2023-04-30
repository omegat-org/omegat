/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
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

package org.omegat.gui.editor;

import java.awt.Component;

import org.omegat.core.data.SourceTextEntry;

/**
 * Interface for editor's filter.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IEditorFilter {
    boolean allowed(SourceTextEntry ste);

    Component getControlComponent();

    /**
     * True if editor should insert source text as empty translation even if it's not allowed by editor
     * behavior settings.
     */
    boolean isSourceAsEmptyTranslation();
}
