/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2012 Thomas Cordonnier
               2015 Aaron Madlon-Kay
               2026 Stephan Pakebusch
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

package org.omegat.gui.stat;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import org.jspecify.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.core.statistics.CalcMatchStatistics;
import org.omegat.core.statistics.CalcPerFileMatchStatistics;
import org.omegat.core.statistics.CalcStandardStatistics;
import org.omegat.core.statistics.ICalcStatistics;
import org.omegat.core.statistics.IStatsConsumer;
import org.omegat.core.threads.LongProcessExecutor;
import org.omegat.core.threads.LongProcessHandle;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Display match statistics window and save data to file.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Thomas Cordonnier
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public class StatisticsWindow extends javax.swing.JDialog {

    private String textData;

    public enum STAT_TYPE {
        STANDARD("CT_STATSSTANDARD_WindowHeader", StatisticsPanel::new, CalcStandardStatistics::new),
        MATCHES("CT_STATSMATCH_WindowHeader", MatchStatisticsPanel::new, CalcMatchStatistics::new),
        MATCHES_PER_FILE("CT_STATSMATCH_PER_FILE_WindowHeader", PerFileMatchStatisticsPanel::new,
                CalcPerFileMatchStatistics::new);

        private final String titleKey;
        private final Function<StatisticsWindow, JComponent> panelCreator;
        private final Function<IStatsConsumer, ICalcStatistics> calcCreator;

        STAT_TYPE(String titleKey, Function<StatisticsWindow, JComponent> panelCreator, Function<IStatsConsumer,
                ICalcStatistics> calcCreator) {
            this.titleKey = titleKey;
            this.panelCreator = panelCreator;
            this.calcCreator = calcCreator;
        }

        public String getTitle() {
            return OStrings.getString(titleKey);
        }

        public JComponent createPanel(StatisticsWindow window) {
            return panelCreator.apply(window);
        }

        public ICalcStatistics createCalculator(IStatsConsumer consumer) {
            return calcCreator.apply(consumer);
        }
    }

    private @Nullable LongProcessHandle<Void> handle;

    private static final Map<STAT_TYPE, StatisticsWindow> OPEN_WINDOWS = new EnumMap<>(STAT_TYPE.class);

    /**
     * Short date/time formatter for the last scan label.
     */
    private static final DateTimeFormatter LAST_SCAN_FORMAT = DateTimeFormatter
            .ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());

    private final STAT_TYPE statType;
    private final JComponent output;
    private final javax.swing.JButton recalculateButton = new javax.swing.JButton();
    private final JLabel lastScanLabel = new JLabel();
    private final IProjectEventListener projectListener = eventType -> {
        if (eventType == IProjectEventListener.PROJECT_CHANGE_TYPE.CLOSE) {
            cancelCalculation();
            dispose();
        }
    };

    /**
     * Show the statistics window of the given type, or bring the already open
     * one to the front. The window is non-modal, so the editor stays usable
     * while it is open. Must be called on the Swing thread.
     */
    public static void showWindow(Frame parent, STAT_TYPE statType) {
        StatisticsWindow existing = OPEN_WINDOWS.get(statType);
        if (existing != null) {
            existing.toFront();
            existing.requestFocus();
            return;
        }
        StatisticsWindow window = new StatisticsWindow(parent, statType);
        OPEN_WINDOWS.put(statType, window);
        window.setVisible(true);
    }

    /**
     * Creates new form StatisticsWindow
     */
    public StatisticsWindow(Frame parent, STAT_TYPE statType) {
        super(parent, false);
        this.statType = statType;
        initComponents();
        copyDataButton.setVisible(false);

        setTitle(statType.getTitle());
        output = statType.createPanel(this);
        displayPanel.add(output);

        // These widgets are added programmatically because initComponents is
        // generated from the form.
        jPanel2.add(lastScanLabel, 0);
        jPanel2.add(Box.createHorizontalStrut(10), 1);
        org.openide.awt.Mnemonics.setLocalizedText(recalculateButton,
                OStrings.getString("BUTTON_STATSMATCH_RECALCULATE"));
        recalculateButton.setVisible(statType == STAT_TYPE.MATCHES);
        recalculateButton.addActionListener(e -> startCalculation());
        jPanel2.add(recalculateButton, jPanel2.getComponentZOrder(copyDataButton));

        StaticUIUtils.setEscapeClosable(this);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cancelCalculation();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                OPEN_WINDOWS.remove(statType, StatisticsWindow.this);
                CoreEvents.unregisterProjectChangeListener(projectListener);
            }
        });
        CoreEvents.registerProjectChangeListener(projectListener);

        boolean restored = statType == STAT_TYPE.MATCHES && output instanceof MatchStatisticsPanel panel
                && panel.restoreFromCache();
        if (restored) {
            finishData();
        } else {
            startCalculation();
        }

        setSize(800, 400);
        StaticUIUtils.persistGeometry(this, Preferences.STATISTICS_WINDOW_GEOMETRY_PREFIX);
        // setLocationRealativeTo called after persistGeometry
        // to make sure the stat window always pops up in the center of the parent window
        setLocationRelativeTo(parent);
    }

    private void startCalculation() {
        recalculateButton.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setString("");
        progressBar.setVisible(true);
        ICalcStatistics calcStat = statType.createCalculator((IStatsConsumer) output);
        LongProcessExecutor executor = Core.getLongProcessExecutor();
        handle = executor.submit(calcStat::run);
    }

    private void cancelCalculation() {
        if (handle != null) {
            handle.cancel();
        }
    }

    private void updateLastScanLabel() {
        if (statType != STAT_TYPE.MATCHES) {
            return;
        }
        MatchStatisticsCache.get().ifPresent(snapshot -> lastScanLabel.setText(StringUtil.format(
                OStrings.getString("CT_STATSMATCH_LastScan"), LAST_SCAN_FORMAT.format(snapshot.getLastScan()))));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        displayPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        progressBar = new javax.swing.JProgressBar();
        copyDataButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        displayPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        displayPanel.setLayout(new java.awt.BorderLayout());
        getContentPane().add(displayPanel, java.awt.BorderLayout.CENTER);

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 10, 10));
        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));
        jPanel2.add(filler1);

        progressBar.setStringPainted(true);
        jPanel2.add(progressBar);

        org.openide.awt.Mnemonics.setLocalizedText(copyDataButton, OStrings.getString("CT_STATS_CopyToClipboard")); // NOI18N
        copyDataButton.setEnabled(false);
        copyDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyDataButtonActionPerformed(evt);
            }
        });
        jPanel2.add(copyDataButton);

        org.openide.awt.Mnemonics.setLocalizedText(closeButton, OStrings.getString("BUTTON_CLOSE")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        jPanel2.add(closeButton);

        getContentPane().add(jPanel2, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void copyDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyDataButtonActionPerformed
        if (textData != null) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(textData), null);
        }
    }//GEN-LAST:event_copyDataButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        // Apparently calling dispose() does not invoke
        // WindowListener.windowClosing() so we have to be sure to end the
        // thread here too.
        // See https://sourceforge.net/p/omegat/bugs/789/
        cancelCalculation();
        dispose();
    }//GEN-LAST:event_closeButtonActionPerformed

    public void setTextData(final String textData) {
        this.textData = textData;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                copyDataButton.setEnabled(textData != null && !textData.isEmpty());
            }
        });
    }

    public void showProgress(final int percent) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(percent);
                progressBar.setString(percent + "%");
            }
        });
    }

    public void finishData() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                progressBar.setValue(100);
                progressBar.setString("");
                progressBar.setVisible(false);
                copyDataButton.setVisible(true);
                recalculateButton.setEnabled(true);
                updateLastScanLabel();
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton copyDataButton;
    javax.swing.JPanel displayPanel;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JPanel jPanel2;
    javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables
}
