/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers,
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2009 Didier Briel, Alex Buloichik
               2010 Wildrich Fourie, Didier Briel
               2012 Wildrich Fourie, Guido Leenders, Didier Briel
               2013 Zoltan Bartko, Didier Briel, Yu Tang
               2014 Aaron Madlon-Kay
               2015 Yu Tang, Aaron Madlon-Kay, Didier Briel
               2017 Didier Briel
               2019 Thomas Cordonnier
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
package org.omegat.gui.main;

/**
 * @author Hiroshi Miura
 */
public abstract class BaseMainWindowMenuHandler {

    /**
     * Common base class for menu handler.
     * <p>
     * There should be a mandatory methods for mandatory
     * commands.
     */
    public BaseMainWindowMenuHandler() {
    }

    public void projectExitMenuItemActionPerformed() {
        System.exit(0);
    }

    public void editFindInProjectMenuItemActionPerformed() {
    }

    public void optionsPreferencesMenuItemActionPerformed() {
    }

    public void projectNewMenuItemActionPerformed() {
    }

    public void projectOpenMenuItemActionPerformed() {
    }

    public void projectTeamNewMenuItemActionPerformed() {
    }

    void findInProjectReuseLastWindow() {
    }

    public void helpAboutMenuItemActionPerformed() {
    }

}
