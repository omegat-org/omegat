/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.editor;

import java.util.Objects;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.jspecify.annotations.Nullable;

import org.omegat.core.Core;
import org.omegat.gui.preferences.PreferencesWindowController;
import org.omegat.gui.preferences.view.EditingBehaviorController;
import org.omegat.util.OStrings;
import org.omegat.util.gui.IPaneMenu;

/**
 * Settings menu of the editor pane. Offers the segment metadata gutter
 * configuration and a shortcut to the editor preferences.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
final class EditorPaneMenu implements IPaneMenu {

    /** Notified after a change of the gutter preferences. */
    private final Runnable displaySettingsListener;

    /** The effective width of a gutter column, presets the width sliders. */
    private final ToIntFunction<SegmentMetadataGutter.Column> columnWidthProvider;

    /** The effective total gutter width, shown in the configuration dialog. */
    private final IntSupplier totalWidthProvider;

    /** The editor font size, the reference for the chosen widths. */
    private final IntSupplier fontSizeProvider;

    /** The dialog opens at most once; a second call brings it to the front. */
    private @Nullable SegmentMetadataConfigDialog configDialog;

    EditorPaneMenu(Runnable displaySettingsListener,
            ToIntFunction<SegmentMetadataGutter.Column> columnWidthProvider,
            IntSupplier totalWidthProvider, IntSupplier fontSizeProvider) {
        this.displaySettingsListener = displaySettingsListener;
        this.columnWidthProvider = columnWidthProvider;
        this.totalWidthProvider = totalWidthProvider;
        this.fontSizeProvider = fontSizeProvider;
    }

    @Override
    public void populatePaneMenu(JPopupMenu menu) {
        JMenuItem configure = new JMenuItem(OStrings.getString("GUI_EDITORWINDOW_GUTTER_CONFIGURE"));
        configure.addActionListener(e -> showConfigDialog());
        menu.add(configure);
        menu.addSeparator();

        JMenuItem prefs = new JMenuItem(OStrings.getString("GUI_EDITORWINDOW_OPEN_PREFS"));
        prefs.addActionListener(e -> new PreferencesWindowController().show(
                Objects.requireNonNull(Core.getMainWindow()).getApplicationFrame(),
                EditingBehaviorController.class));
        menu.add(prefs);
    }

    private void showConfigDialog() {
        if (configDialog != null && configDialog.isDisplayable()) {
            configDialog.setVisible(true);
            configDialog.toFront();
            configDialog.requestFocus();
            return;
        }
        configDialog = new SegmentMetadataConfigDialog(
                Objects.requireNonNull(Core.getMainWindow()).getApplicationFrame(),
                displaySettingsListener, columnWidthProvider, totalWidthProvider,
                fontSizeProvider);
        configDialog.setVisible(true);
    }
}
