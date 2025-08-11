/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers,
               2000-2006 Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2014 Piotr Kulik
               2015 Aaron Madlon-Kay
               2023 Jean-Christophe Helary
               2025 Hiroshi Miura
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

import org.omegat.core.Core;
import org.omegat.gui.editor.EditorController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.UIThreadsUtil;
import org.openide.awt.Mnemonics;

import javax.swing.Timer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class MainWindowStatusBarController {
    private final MainWindowStatusBar mainWindowStatusBar;

    public MainWindowStatusBarController() {
        mainWindowStatusBar = new MainWindowStatusBar();
        updateProgressText(getProgressMode());
        mainWindowStatusBar.progressLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                MainWindowStatusBar.StatusBarMode[] modes = MainWindowStatusBar.StatusBarMode.values();
                MainWindowStatusBar.StatusBarMode progressMode = modes[(getProgressMode().ordinal() + 1) % modes.length];
                Preferences.setPreference(Preferences.SB_PROGRESS_MODE, progressMode);
                updateProgressText(progressMode);
            }
        });
    }

    private MainWindowStatusBar.StatusBarMode getProgressMode() {
        return Preferences.getPreferenceEnumDefault(Preferences.SB_PROGRESS_MODE, MainWindowStatusBar.StatusBarMode.DEFAULT);
    }

    private void updateProgressText(MainWindowStatusBar.StatusBarMode progressMode) {
        String statusText;
        String tooltipText;
        if (progressMode == MainWindowStatusBar.StatusBarMode.PERCENTAGE) {
            statusText = OStrings.getProgressBarDefaultPrecentageText();
            tooltipText = OStrings.getString("MW_PROGRESS_TOOLTIP_PERCENTAGE");
        } else {
            statusText = OStrings.getString("MW_PROGRESS_DEFAULT");
            tooltipText = OStrings.getString("MW_PROGRESS_TOOLTIP");
        }
        if (Core.getProject().isProjectLoaded()) {
            ((EditorController) Core.getEditor()).showStat();
        } else {
            Mnemonics.setLocalizedText(mainWindowStatusBar.progressLabel, statusText);
        }
        mainWindowStatusBar.setToolTipText(tooltipText);
    }

    public MainWindowStatusBar getUI() {
        return mainWindowStatusBar;
    }

    public void showStatusMessageRB(final String messageKey, final Object... params) {
        final String msg = getLocalizedString(messageKey, params);
        UIThreadsUtil.executeInSwingThread(() -> mainWindowStatusBar.setStatusLabel(msg));
    }

    private String getLocalizedString(String messageKey, Object... params) {
        if (messageKey == null) {
            return " ";
        } else if (params == null) {
            return OStrings.getString(messageKey);
        } else {
            return StringUtil.format(OStrings.getString(messageKey), params);
        }
    }
    public void showTimedStatusMessageRB(String messageKey, Object... params) {
        showStatusMessageRB(messageKey, params);

        if (messageKey == null) {
            return;
        }

        // clear the message after 10 seconds
        String localizedString = getLocalizedString(messageKey, params);
        Timer timer = new Timer(10_000, evt -> {
            String text = mainWindowStatusBar.getStatusLabel();
            if (localizedString.equals(text)) {
                mainWindowStatusBar.setStatusLabel(null);
            }
        });
        timer.setRepeats(false); // one-time only
        timer.start();
    }

    /**
     * Show message in progress bar.
     *
     * @param messageText
     *            message text
     */
    public void showProgressMessage(String messageText) {
        mainWindowStatusBar.setProgressLabel(messageText);
    }

    /**
     * Show message in length label.
     *
     * @param messageText
     *            message text
     */
    public void showLengthMessage(String messageText) {
        mainWindowStatusBar.setLengthLabel(messageText);
    }

    public void showLockInsertMessage(String messageText, String toolTip) {
        mainWindowStatusBar.setLockInsertLabel(messageText);
        mainWindowStatusBar.setLockInsertToolTipText(toolTip);
    }

    public void showStatusMessage(String message) {
        mainWindowStatusBar.setStatusLabel(message);
    }
}
