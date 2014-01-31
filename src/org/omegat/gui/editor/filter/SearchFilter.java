/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.gui.editor.filter;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.omegat.core.Core;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.gui.editor.IEditorFilter;

/**
 * Editor filter implementation.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class SearchFilter implements IEditorFilter {
    private final Set<Integer> entriesList = new HashSet<Integer>();
    private FilterBarSearch controlComponent;

    public SearchFilter(List<Integer> entries) {
        entriesList.addAll(entries);
        controlComponent = new FilterBarSearch();
        controlComponent.btnRemoveFilter.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Core.getEditor().removeFilter();
            }
        });
    }

    @Override
    public boolean allowed(SourceTextEntry ste) {
        return entriesList.contains(ste.entryNum());
    }

    @Override
    public Component getControlComponent() {
        return controlComponent;
    }
}
