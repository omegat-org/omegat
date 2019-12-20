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

package org.omegat.gui.issues;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;

import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.TMXEntry;

/**
 * A simple issue implementation intended for extensions or scripts.
 *
 * @author Aaron Madlon-Kay
 */
public abstract class SimpleIssue implements IIssue {

    private final SourceTextEntry sourceEntry;
    private final TMXEntry targetEntry;
    private final Icon icon;

    public SimpleIssue(SourceTextEntry sourceEntry, TMXEntry targetEntry) {
        this.sourceEntry = sourceEntry;
        this.targetEntry = targetEntry;
        this.icon = new SimpleColorIcon(getColor());
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    protected abstract String getColor();

    @Override
    public int getSegmentNumber() {
        return sourceEntry.entryNum();
    }

    @Override
    public Component getDetailComponent() {
        IssueDetailSplitPanel panel = new IssueDetailSplitPanel();
        panel.firstTextPane.setText(sourceEntry.getSrcText());
        panel.lastTextPane.setText(targetEntry.translation);
        panel.setMinimumSize(new Dimension(0, panel.firstTextPane.getFont().getSize() * 6));
        return panel;
    }
}
