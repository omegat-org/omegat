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
               2011 Didier Briel
               2012 Wildrich Fourie, Guido Leenders, Martin Fleurke, Didier Briel
               2013 Zoltan Bartko, Didier Briel, Yu Tang
               2014 Aaron Madlon-Kay
               2015 Didier Briel, Yu Tang
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

import java.awt.event.ActionListener;

import javax.swing.event.MenuListener;

/**
 * Class for classic main menu.
 *
 * @author Hiroshi Miura
 */
public final class MainWindowMenu extends BaseMainWindowMenu
        implements ActionListener, MenuListener, IMainMenu {

    public MainWindowMenu(final MainWindow mainWindow, final MainWindowMenuHandler mainWindowMenuHandler) {
        super(mainWindow, mainWindowMenuHandler);
        initComponents();
    }

    @Override
    void createMenuBar() {
        mainMenu.add(projectMenu);
        mainMenu.add(editMenu);
        mainMenu.add(gotoMenu);
        mainMenu.add(viewMenu);
        mainMenu.add(toolsMenu);
        mainMenu.add(optionsMenu);
        mainMenu.add(helpMenu);
    }

}
