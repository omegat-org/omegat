/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
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

package org.omegat.gui.common;

import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JTextPane;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.util.gui.FontFallbackListener;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * Base class for show information about currently selected entry. It can be
 * used for glossaries, dictionaries and other panes.
 *
 * If you need long search operation, use EntryInfoThreadPane instead.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @param <T>
 *            result type of found data
 */
@SuppressWarnings("serial")
public abstract class EntryInfoPane<T> extends JTextPane implements IProjectEventListener {

    public EntryInfoPane(final boolean useApplicationFont) {
        if (useApplicationFont) {
            setFont(Core.getMainWindow().getApplicationFont());
            CoreEvents.registerFontChangedEventListener(this::setFont);
        }
        CoreEvents.registerProjectChangeListener(this);
        if (!GraphicsEnvironment.isHeadless()) {
            setDragEnabled(true);
        }
        getDocument().addDocumentListener(new FontFallbackListener(this));
        setForeground(Styles.EditorColor.COLOR_FOREGROUND.getColor());
        setCaretColor(Styles.EditorColor.COLOR_FOREGROUND.getColor());
        setBackground(Styles.EditorColor.COLOR_BACKGROUND.getColor());
    }

    @Override
    public void setEditable(boolean isEditable) {
        StaticUIUtils.setCaretUpdateEnabled(this, isEditable);
        super.setEditable(isEditable);
    }

    @Override
    public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
        switch (eventType) {
        case CREATE:
        case LOAD:
            onProjectOpen();
            break;
        case CLOSE:
            onProjectClose();
            break;
        default:
            // Nothing
        }
    }

    public void clear() {
        UIThreadsUtil.mustBeSwingThread();
        setText(null);
        scrollRectToVisible(new Rectangle());
    }

    protected void onProjectOpen() {
    }

    protected void onProjectClose() {
    }
}
