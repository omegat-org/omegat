/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2008      Alex Buloichik
 Portions copyright 2007 Zoltan Bartko - bartkozoltan@bartkozoltan.com
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.core.data;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.team.IRemoteRepository;

/**
 * Factory for load project, create project, and create "not-loaded" project.
 * 
 * TODO: change exception handling
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ProjectFactory {
    public static void createNotLoadedProject() {
        Core.setProject(new NotLoadedProject());
    }

    /**
     * Create new project.
     */
    public static void createProject(ProjectProperties newProps) {
        RealProject p = new RealProject(newProps, null);
        p.createProject();
        Core.setProject(p);
        Core.getAutoSave().enable();
        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.CREATE);
    }

    /**
     * Loads project in a "big" sense -- loads project's properties, glossaryes,
     * tms, source files etc.
     * 
     * @param props
     *            properties for new project
     */
    public static void loadProject(ProjectProperties props, IRemoteRepository repository) {
        Core.getAutoSave().disable();
        RealProject p = new RealProject(props, repository);
        p.loadProject();
        Core.setProject(p);
        Core.getAutoSave().enable();
        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.LOAD);
    }

    /**
     * Close current project.
     */
    public static void closeProject() {
        Core.getAutoSave().disable();
        Core.getProject().closeProject();
        Core.setProject(new NotLoadedProject());
        CoreEvents.fireProjectChange(IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE);
    }
}
