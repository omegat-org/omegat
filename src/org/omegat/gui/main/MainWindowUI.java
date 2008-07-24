/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers, 
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.omegat.gui.filelist.ProjectFrame;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.DockingUI;
import org.openide.awt.Mnemonics;

import com.vlsolutions.swing.docking.DockingDesktop;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeEvent;
import com.vlsolutions.swing.docking.event.DockableStateWillChangeListener;

/**
 * Class for initialize, load/save, etc. for main window UI components.
 * 
 * @author Keith Godfrey
 * @author Benjamin Siband
 * @author Maxym Mykhalchuk
 * @author Kim Bruning
 * @author Henry Pijffers (henry.pijffers@saxnot.com)
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Andrzej Sawula
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class MainWindowUI {
    /**
     * Create main UI panels.
     */
    public static void createMainComponents(final MainWindow mainWindow, final Font font) {
        mainWindow.m_projWin = new ProjectFrame(mainWindow);
    }

    /**
     * Create docking desktop panel.
     */
    public static DockingDesktop initDocking(final MainWindow mainWindow) {
        DockingUI.initialize();

        mainWindow.desktop = new DockingDesktop();
        mainWindow.desktop.addDockableStateWillChangeListener(new DockableStateWillChangeListener() {
            public void dockableStateWillChange(DockableStateWillChangeEvent event) {
                if (event.getFutureState().isClosed())
                    event.cancel();
            }
        });

        return mainWindow.desktop;
    }

    /**
     * Create swing UI components for status panel.
     */
    public static JPanel createStatusBar(final MainWindow mainWindow) {
        mainWindow.statusLabel = new JLabel();
        mainWindow.progressLabel = new JLabel();
        mainWindow.lengthLabel = new JLabel();

        mainWindow.statusLabel.setFont(new Font("MS Sans Serif", 0, 11));

        Mnemonics.setLocalizedText(mainWindow.progressLabel, OStrings.getString("MW_PROGRESS_DEFAULT"));
        mainWindow.progressLabel.setToolTipText(OStrings.getString("MW_PROGRESS_TOOLTIP"));
        mainWindow.progressLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        mainWindow.progressLabel.setHorizontalTextPosition(SwingConstants.CENTER);

        Mnemonics.setLocalizedText(mainWindow.lengthLabel, OStrings.getString("MW_SEGMENT_LENGTH_DEFAULT"));
        mainWindow.lengthLabel.setToolTipText(OStrings.getString("MW_SEGMENT_LENGTH_TOOLTIP"));
        mainWindow.lengthLabel.setAlignmentX(1.0F);
        mainWindow.lengthLabel.setBorder(BorderFactory.createLineBorder(Color.black));
        mainWindow.lengthLabel.setFocusable(false);

        JPanel statusPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel2.add(mainWindow.progressLabel);
        statusPanel2.add(mainWindow.lengthLabel);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(mainWindow.statusLabel, BorderLayout.CENTER);
        statusPanel.add(statusPanel2, BorderLayout.EAST);

        return statusPanel;
    }

    /**
     * Initialized the sizes of OmegaT window.
     * <p>
     * Assume screen size is 800x600 if width less than 900, and 1024x768 if
     * larger. Assume task bar at bottom of screen. If screen size saved,
     * recover that and use instead (18may04).
     */
    public static void loadScreenLayout(final MainWindow mainWindow) {
        int x, y, w, h;
        // main window
        try {
            x = Integer.parseInt(Preferences.getPreference(Preferences.MAINWINDOW_X));
            y = Integer.parseInt(Preferences.getPreference(Preferences.MAINWINDOW_Y));
            w = Integer.parseInt(Preferences.getPreference(Preferences.MAINWINDOW_WIDTH));
            h = Integer.parseInt(Preferences.getPreference(Preferences.MAINWINDOW_HEIGHT));
        } catch (NumberFormatException nfe) {
            // size info missing - put window in default position
            GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle scrSize = env.getMaximumWindowBounds();
            if (scrSize.width < 900) {
                // assume 800x600
                x = 0;
                y = 0;
                w = 580;
                h = 536;
            } else {
                // assume 1024x768 or larger
                x = 0;
                y = 0;
                w = 690;
                h = 700;
            }
        }
        mainWindow.setBounds(x, y, w, h);

        String layout = Preferences.getPreference(Preferences.MAINWINDOW_LAYOUT);
        if (layout.length() > 0) {
            byte[] bytes = StaticUtils.uudecode(layout);
            try {
                Log.log("load desktop layout:" + new String(bytes, "UTF-8"));
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                mainWindow.desktop.readXML(in);
                in.close();
            } catch (Exception e) {
                Log.log(e);
            }
        }
    }

    /**
     * Stores screen layout (width, height, position, etc).
     */
    public static void saveScreenLayout(final MainWindow mainWindow) {
        Preferences.setPreference(Preferences.MAINWINDOW_X, mainWindow.getX());
        Preferences.setPreference(Preferences.MAINWINDOW_Y, mainWindow.getY());
        Preferences.setPreference(Preferences.MAINWINDOW_WIDTH, mainWindow.getWidth());
        Preferences.setPreference(Preferences.MAINWINDOW_HEIGHT, mainWindow.getHeight());

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            mainWindow.desktop.writeXML(out);
            out.close();
            byte[] buf = out.toByteArray();
            Log.log("save desktop layout:" + new String(buf, "UTF-8"));
            String layout = StaticUtils.uuencode(buf);
            Preferences.setPreference(Preferences.MAINWINDOW_LAYOUT, layout);
        } catch (Exception e) {
            Preferences.setPreference(Preferences.MAINWINDOW_LAYOUT, new String());
        }
    }
    

    /**
     * Restores defaults for all dockable parts. May be expanded in the future
     * to reset the entire GUI to its defaults.
     * 
     * Note: The current implementation is just a quick hack, due to
     * insufficient knowledge of the docking framework library.
     * 
     * @author Henry Pijffers (henry.pijffers@saxnot.com)
     */
    public static void resetDesktopLayout(final MainWindow mainWindow) {
        try {
            Log.log("reset desktop layout");
            InputStream in = MainWindowUI.class.getResourceAsStream(
                    "DockingDefaults.xml");
            try {
                mainWindow.desktop.readXML(in);
            } finally {
                in.close();
            }
        } catch (Exception exception) {
            Log.log(exception);
        }
    }
}
