/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura.
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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.UIManager;

import tokyo.northside.swing.TipOfTheDay;

@SuppressWarnings("serial")
public class TipOfTheDayDialog extends JDialog {

    private static final int BORDER = 10;
    private static final int PREFFERRED_WIDTH = 900;
    private static final int PREFFERRED_HEIGHT = 450;

    JCheckBox showOnCB;
    JButton previousTipButton;
    JButton nextTipButton;
    JButton closeButton;

    public TipOfTheDayDialog(final Frame owner, final String title, final TipOfTheDay totd) {
        super(owner, title, true);
        initComponents(owner, totd, getLocale());
    }

    private void initComponents(Frame owner, TipOfTheDay totd, Locale locale) {
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(BORDER, BORDER, BORDER, BORDER));
        panel.add(totd);
        add(panel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(PREFFERRED_WIDTH, PREFFERRED_HEIGHT));

        JPanel buttonPanel = new JPanel();
        showOnCB = new JCheckBox();
        showOnCB.setText(UIManager.getString("TipOfTheDay.showOnStartupText"));
        buttonPanel.add(showOnCB);
        previousTipButton = new JButton();
        previousTipButton.setText(UIManager.getString("TipOfTheDay.previousTipText", locale));
        buttonPanel.add(previousTipButton);
        nextTipButton = new JButton();
        nextTipButton.setText(UIManager.getString("TipOfTheDay.nextTipText", locale));
        buttonPanel.add(nextTipButton);
        closeButton = new JButton();
        closeButton.setText(UIManager.getString("TipOfTheDay.closeText", locale));
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(closeButton);
        pack();
    }

}
