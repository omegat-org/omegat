/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023-2025 Hiroshi Miura.
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.gui.tipoftheday;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.UIManager;

import org.omegat.util.gui.Styles;
import tokyo.northside.tipoftheday.TipOfTheDay;

import org.omegat.core.Core;
import org.omegat.util.Log;
import org.omegat.util.Preferences;
import org.omegat.util.gui.UIDesignManager;

import static org.omegat.gui.tipoftheday.TipOfTheDayModule.ENABLED;

public final class TipOfTheDayController {
    private static final String TIPOFTHEDAY_SHOW_ON_STARTUP = "tipoftheday_show_on_start";
    private static final String TIPOFTHEDAY_CURRENT_TIP = "tipoftheday_current_tip";

    public void start(final boolean force) {
        if (force || Preferences.isPreferenceDefault(TIPOFTHEDAY_SHOW_ON_STARTUP, ENABLED)) {
            showComponent();
        }
    }

    public void showComponent() {
        TipOfTheDay totd = new TipOfTheDay(new OmegaTTipOfTheDayModel());
        totd.setPreferredSize(new Dimension(900, 450));

        // respect theme colors
        totd.clearUserStyleSheets();
        String background = Styles.EditorColor.COLOR_BACKGROUND.toHex();
        String foreground = Styles.EditorColor.COLOR_FOREGROUND.toHex();
        String bodyCss = "body { background-color: " + background + "; color: " + foreground + "; }";
        totd.addUserStyleSheetText(bodyCss);
        // Apply theme-specific CSS to TipOfTheDay content
        boolean dark = UIDesignManager.isDarkTheme(UIManager.getLookAndFeelDefaults());
        String cssPath = dark ? "/tips/dark.css" : "/tips/light.css";
        try {
            URL url = getClass().getResource(cssPath);
            if (url != null) {
                totd.addUserStyleSheetUri(url.toURI());
            }
        } catch (Exception e) {
            Log.log(e);
        }
        //
        String current = Preferences.getPreference(TIPOFTHEDAY_CURRENT_TIP);
        int currentTip = 0;
        if (current != null) {
            try {
                currentTip = (Integer.parseInt(current) + 1) % totd.getModel().getTipCount();
            } catch (NumberFormatException ex) {
                Log.log(ex);
            }
        }
        totd.setCurrentTip(currentTip);

        JFrame mainFrame = Core.getMainWindow().getApplicationFrame();
        TipOfTheDayDialog dialog = new TipOfTheDayDialog(mainFrame,
                UIManager.getString("TipOfTheDay.dialogTitle"), totd);

        dialog.showOnCB.setSelected(Preferences.isPreferenceDefault(TIPOFTHEDAY_SHOW_ON_STARTUP, true));
        dialog.previousTipButton.addActionListener(e -> totd.previousTip());
        dialog.nextTipButton.addActionListener(e -> totd.nextTip());
        dialog.closeButton.addActionListener(actionEvent -> close(dialog));

        totd.getActionMap().put("close", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                close(dialog);
            }
        });

        // set keymap for actions of TipOfTheDay
        totd.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "previousTip");
        totd.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "nextTip");
        totd.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");

        dialog.setVisible(true);
        Preferences.setPreference(TIPOFTHEDAY_CURRENT_TIP, totd.getCurrentTip());
        Preferences.setPreference(TIPOFTHEDAY_SHOW_ON_STARTUP, dialog.showOnCB.isSelected());
        close(dialog);
    }

    public void close(JDialog dialog) {
        if (dialog.isDisplayable()) {
            dialog.dispose();
        }
    }
}
