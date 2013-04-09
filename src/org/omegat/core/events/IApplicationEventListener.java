/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.core.events;

/**
 * Listener interface for application event, like startup and shutdown.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public interface IApplicationEventListener {
    /**
     * Called on application startup after all components created and registered
     * in Core.
     */
    void onApplicationStartup();

    /**
     * Called on application shutdown.
     * 
     * CAN BE EXECUTED IN ANY THREAD !
     */
    void onApplicationShutdown();
}
