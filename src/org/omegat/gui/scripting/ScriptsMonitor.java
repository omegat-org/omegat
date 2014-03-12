/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Briac Pilpre
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

package org.omegat.gui.scripting;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.List;

import javax.swing.JList;

import org.omegat.util.DirectoryMonitor;

/**
 * Monitor to check changes in the script directory.
 *
 * @author Briac Pilpre
 */
public class ScriptsMonitor implements DirectoryMonitor.Callback {
    protected DirectoryMonitor monitor;

    public ScriptsMonitor(final JList list, List<String> extensions) {
        this.list = list;
        this.extensions = extensions;
    }

    public void start(final File scriptDir) {
    	this.scriptDir = scriptDir;
        monitor = new DirectoryMonitor(scriptDir, this);
        monitor.start();
    }

    public void stop() {
        monitor.fin();
    }

    /**
     * Executed on file changed.
     */
    public void fileChanged(File file) {
        String[] scriptList = new String[]{  };
        
        if (scriptDir.exists() && scriptDir.isDirectory()) {
        	// Only display files with an extension supported by the engines 
        	// currently installed.
            scriptList = scriptDir.list(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					String ext = ScriptingWindow.getFileExtension(name);
					return extensions.contains(ext.toLowerCase());
				}
            });
            Arrays.sort(scriptList);
        }

        list.setListData(scriptList);
    }

    private final JList list;
	private File scriptDir;
	private List<String> extensions;
}
