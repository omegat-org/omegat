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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.gui.editor.EditorController;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

@SuppressWarnings("serial")
public class MainWindowStatusBar extends JPanel {
    public static final String STATUS_BAR_NAME = "main_window_status_bar";
    public static final String STATUS_LABEL_NAME = "main_window_status_label";
    public static final String STATUS_PROGRESS_LABEL_NAME = "main_window_status_progress_label";
    public static final String LENGTH_LABEL_NAME = "main_window_status_length_label";
    public static final String LOCK_INSERT_LABEL_NAME = "main_window_status_lock_insert_label";
    private final JLabel statusLabel = new JLabel();
    private final JLabel progressLabel = new JLabel();
    private final JLabel lengthLabel = new JLabel();
    private final JLabel lockInsertLabel = new JLabel();

    public MainWindowStatusBar() {
        super();
        setLayout(new BorderLayout());
        setName(STATUS_BAR_NAME);
        statusLabel.setName(STATUS_LABEL_NAME);
        progressLabel.setName(STATUS_PROGRESS_LABEL_NAME);
        lengthLabel.setName(LENGTH_LABEL_NAME);
        lockInsertLabel.setName(LOCK_INSERT_LABEL_NAME);

        // Derive small label point size relative to default size; don't
        // hard-code a point size because it will be wrong for e.g. HiDPI
        // cases. Factor of 0.85 is based on old assumptions of 13pt default
        // and 11pt small.
        Font defaultFont = statusLabel.getFont();
        float smallFontSize = defaultFont.getSize() * 0.85f;
        statusLabel.setFont(defaultFont.deriveFont(smallFontSize));
        Border border = UIManager.getBorder("OmegaTStatusArea.border");

        final StatusBarMode progressMode = Preferences.getPreferenceEnumDefault(Preferences.SB_PROGRESS_MODE,
                StatusBarMode.DEFAULT);

        String statusText = OStrings.getString("MW_PROGRESS_DEFAULT");
        String tooltipText = "MW_PROGRESS_TOOLTIP";
        if (progressMode == StatusBarMode.PERCENTAGE) {
            statusText = OStrings.getProgressBarDefaultPrecentageText();
            tooltipText = "MW_PROGRESS_TOOLTIP_PERCENTAGE";
        }
        Mnemonics.setLocalizedText(progressLabel, statusText);
        progressLabel.setToolTipText(OStrings.getString(tooltipText));

        progressLabel.setBorder(border);
        progressLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                StatusBarMode[] modes = StatusBarMode.values();
                StatusBarMode progressMode = Preferences
                        .getPreferenceEnumDefault(Preferences.SB_PROGRESS_MODE, StatusBarMode.DEFAULT);
                progressMode = modes[(progressMode.ordinal() + 1) % modes.length];

                Preferences.setPreference(Preferences.SB_PROGRESS_MODE, progressMode);

                String statusText = OStrings.getString("MW_PROGRESS_DEFAULT");
                String tooltipText = "MW_PROGRESS_TOOLTIP";
                if (progressMode == StatusBarMode.PERCENTAGE) {
                    statusText = OStrings.getProgressBarDefaultPrecentageText();
                    tooltipText = "MW_PROGRESS_TOOLTIP_PERCENTAGE";
                }

                if (Core.getProject().isProjectLoaded()) {
                    ((EditorController) Core.getEditor()).showStat();
                } else {
                    Core.getMainWindow().showProgressMessage(statusText);
                }
                ((MainWindow) Core.getMainWindow()).setProgressToolTipText(OStrings.getString(tooltipText));
            }
        });

        Mnemonics.setLocalizedText(lengthLabel, OStrings.getString("MW_SEGMENT_LENGTH_DEFAULT"));
        lengthLabel.setToolTipText(OStrings.getString("MW_SEGMENT_LENGTH_TOOLTIP"));
        lengthLabel.setBorder(border);
        lengthLabel.setFocusable(false);

        JPanel statusPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel2.add(lockInsertLabel);
        statusPanel2.add(progressLabel);
        statusPanel2.add(lengthLabel);

        add(statusLabel, BorderLayout.CENTER);
        add(statusPanel2, BorderLayout.EAST);
        setBorder(UIManager.getBorder("OmegaTMainWindowBottomMargin.border"));

        Color bgColor = UIManager.getColor("AutoHideButtonPanel.background");
        if (bgColor != null) {
            setBackground(bgColor);
            statusPanel2.setBackground(bgColor);
        }
    }

    public String getStatusLabel() {
        return statusLabel.getText();
    }

    public void setStatusLabel(String text) {
        statusLabel.setText(text);
    }

    public void setProgressLabel(String text) {
        progressLabel.setText(text);
    }

    public void setProgressToolTip(String text) {
        progressLabel.setToolTipText(text);
    }

    public void setLengthLabel(String text) {
        lengthLabel.setText(text);
    }

    public void setLockInsertLabel(String text) {
        lockInsertLabel.setText(text);
    }

    public void setLockInsertToolTipText(String text) {
        lockInsertLabel.setToolTipText(text);
    }

    public enum StatusBarMode {
        DEFAULT, PERCENTAGE,
    }
}
