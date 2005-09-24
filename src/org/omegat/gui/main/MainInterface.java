/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.gui.main;

import org.omegat.gui.ProjectFrame;

/**
 * The interface specifying the interface of the main window,
 * i.e. the methods that can be used throughout OmegaT.
 *
 * @author Maxym Mykhalchuk
 */
public interface MainInterface
{
    public void doNextEntry();
    public void doPrevEntry();
    public void doRecycleTrans();
    public void activateEntry();
    public void doGotoEntry(String entry);
    public void setMessageText(String message);
    public void displayWarning(String warning, Throwable throwable);
    public void displayError(String error, Throwable throwable);
    public void fatalError(String error, Throwable throwable);
    
    /** Displays fuzzy matching info if it's available. */
    public void updateFuzzyInfo(int nearnum);
    
    public void finishLoadProject();
    
    public boolean isProjectLoaded();
    
    public void toFront();
    
    public void setVisible(boolean show);
    
    /** Returns the window which lists all the source files. */
    public ProjectFrame getProjectFrame();
}
