/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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
package org.omegat.util.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.omegat.core.Core;
import org.omegat.gui.main.MainWindow;

public abstract class ProjectFileDragImporter extends FileDropHandler {
        
    private final MainWindow mw;
    private final boolean doReset;
    
    public ProjectFileDragImporter(MainWindow mw, boolean doReset) {
        this.mw = mw;
        this.doReset = doReset;
    }
    
    @Override
    public boolean canImport(TransferSupport support) {
        return Core.getProject().isProjectLoaded() && super.canImport(support);
    }
    
    @Override
    protected boolean handleFiles(List<File> files) {
        List<File> filtered = new ArrayList<File>(files.size());
        for (File file : files) {
            if (file.exists() && file.canRead() && accept(file)) {
                filtered.add(file);
            }
        }
        if (filtered.isEmpty()) {
            return false;
        }
        mw.importFiles(getDestination(), filtered.toArray(new File[0]), doReset);
        return true;
    }
    
    protected abstract String getDestination();
    
    protected boolean accept(File pathname) {
        return true;
    }
}