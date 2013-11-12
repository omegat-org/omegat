/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2012 Thomas Cordonnier
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.gui.stat;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.omegat.core.Core;
import org.omegat.core.statistics.CalcMatchStatistics;
import org.omegat.core.statistics.CalcStandardStatistics;
import org.omegat.core.threads.LongProcessThread;
import org.omegat.util.OStrings;
import org.omegat.util.gui.DockingUI;

/**
 * Display match statistics window and save data to file.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Thomas Cordonnier
 */
@SuppressWarnings("serial")
public class StatisticsWindow extends JDialog {

    public static enum STAT_TYPE {
        STANDARD, MATCHES, MATCHES_PER_FILE
    };

    private JProgressBar progressBar;
    private JTextArea output;
    private LongProcessThread thread;

    public StatisticsWindow(STAT_TYPE statType) {
        super(Core.getMainWindow().getApplicationFrame(), true);

        progressBar = new JProgressBar();
        output = new JTextArea();

        switch (statType) {
        case STANDARD:
            setTitle(OStrings.getString("CT_STATSSTANDARD_WindowHeader"));
            thread = new CalcStandardStatistics(this);
            break;
        case MATCHES:
            setTitle(OStrings.getString("CT_STATSMATCH_WindowHeader"));
            thread = new CalcMatchStatistics(this, false);
            break;
        case MATCHES_PER_FILE:
            setTitle(OStrings.getString("CT_STATSMATCH_PER_FILE_WindowHeader"));
            thread = new CalcMatchStatistics(this, true);
            break;
        }

        // Run calculation
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        // Prepare UI
        setLayout(new BorderLayout());
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(p);

        progressBar.setStringPainted(true);
        p.add(progressBar, BorderLayout.SOUTH);

        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, Core.getMainWindow().getApplicationFont().getSize()));
        p.add(new JScrollPane(output), BorderLayout.CENTER);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                thread.fin();
                dispose();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                thread.fin();
            }
        });

        setSize(800, 400);
        DockingUI.displayCentered(this);
    }

    public void showProgress(final int percent) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(percent);
                progressBar.setString(percent + "%");
            }
        });
    }

    public void displayData(final String result) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                output.setText(result);
            }
        });
    }

    public void appendData(final String result) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                output.append(result);
            }
        });
    }

    public void finishData() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(100);
                progressBar.setString("");
                progressBar.setVisible(false);
                output.setCaretPosition(0);
            }
        });
    }
}
