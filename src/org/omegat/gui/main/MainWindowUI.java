/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, Henry Pijffers,
                         Benjamin Siband, and Kim Bruning
               2007 Zoltan Bartko
               2008 Andrzej Sawula, Alex Buloichik
               2014 Piotr Kulik
               2015 Aaron Madlon-Kay
               2023 Jean-Christophe Helary
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

package org.omegat.gui.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.core.events.IProjectEventListener;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.filelist.ProjectFilesListController;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIDesignManager;
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
 * @author Piotr Kulik
 * @author Aaron Madlon-Kay
 */
public final class MainWindowUI {

    private MainWindowUI() {
    }

    public static final String UI_LAYOUT_FILE = "uiLayout" + OStrings.getBrandingToken() + ".xml";

    public enum StatusBarMode {
        DEFAULT, PERCENTAGE,
    };

    /**
     * Create main UI panels.
     */
    public static void createMainComponents(final MainWindow mainWindow, final Font font) {
        mainWindow.projWin = new ProjectFilesListController(mainWindow);
    }

    /**
     * Create docking desktop panel.
     */
    public static DockingDesktop initDocking(final MainWindow mainWindow) {
        mainWindow.desktop = new DockingDesktop();
        mainWindow.desktop.addDockableStateWillChangeListener(new DockableStateWillChangeListener() {
            public void dockableStateWillChange(DockableStateWillChangeEvent event) {
                if (event.getFutureState().isClosed()) {
                    event.cancel();
                }
            }
        });

        return mainWindow.desktop;
    }

    /**
     * Installs a {@link IProjectEventListener} that handles loading, storing, and restoring the main window layout when
     * a project-specific layout is present.
     */
    public static void handlePerProjectLayouts(final MainWindow mainWindow) {
        PerProjectLayoutHandler handler = new PerProjectLayoutHandler(mainWindow);
        CoreEvents.registerProjectChangeListener(handler);
        CoreEvents.registerApplicationEventListener(handler);
    }

    private static class PerProjectLayoutHandler implements IProjectEventListener, IApplicationEventListener {

        private final MainWindow mainWindow;
        private boolean didApplyPerProjectLayout = false;

        PerProjectLayoutHandler(MainWindow mainWindow) {
            this.mainWindow = mainWindow;
        }

        @Override
        public void onApplicationStartup() {
        }

        @Override
        public void onApplicationShutdown() {
            // Project is not closed before shutdown, so we need to handle this separately
            // from the onProjectChanged events.
            if (Core.getProject().isProjectLoaded() && didApplyPerProjectLayout) {
                loadScreenLayoutFromPreferences(mainWindow);
                didApplyPerProjectLayout = false;
            }
        }

        @Override
        public void onProjectChanged(PROJECT_CHANGE_TYPE eventType) {
            if (eventType == PROJECT_CHANGE_TYPE.CLOSE && didApplyPerProjectLayout) {
                loadScreenLayoutFromPreferences(mainWindow);
                didApplyPerProjectLayout = false;
                return;
            }
            if (!Core.getProject().isProjectLoaded()) {
                return;
            }
            File perProjLayout = getPerProjectLayout();
            if (!perProjLayout.isFile()) {
                return;
            }
            switch (eventType) {
            case LOAD:
                saveScreenLayout(mainWindow);
                loadScreenLayout(mainWindow, perProjLayout);
                didApplyPerProjectLayout = true;
                break;
            case SAVE:
                saveScreenLayout(mainWindow, perProjLayout);
                break;
            default:
            }
        }

        private File getPerProjectLayout() {
            return new File(Core.getProject().getProjectProperties().getProjectInternal(),
                    MainWindowUI.UI_LAYOUT_FILE);
        }
    }

    /**
     * Create swing UI components for status panel.
     */
    public static JPanel createStatusBar(final MainWindow mainWindow) {
        mainWindow.statusLabel = new JLabel();
        mainWindow.progressLabel = new JLabel();
        mainWindow.lengthLabel = new JLabel();
        mainWindow.lockInsertLabel = new JLabel();

        // Derive small label point size relative to default size; don't hard-code a
        // point size because it will be wrong for e.g. HiDPI cases.
        // Factor of 0.85 is based on old assumptions of 13pt default and 11pt small.
        Font defaultFont = mainWindow.statusLabel.getFont();
        float smallFontSize = defaultFont.getSize() * 0.85f;
        mainWindow.statusLabel.setFont(defaultFont.deriveFont(smallFontSize));

        Border border = UIManager.getBorder("OmegaTStatusArea.border");

        final StatusBarMode progressMode = Preferences.getPreferenceEnumDefault(Preferences.SB_PROGRESS_MODE,
                StatusBarMode.DEFAULT);

        String statusText = OStrings.getString("MW_PROGRESS_DEFAULT");
        String tooltipText = "MW_PROGRESS_TOOLTIP";
        if (progressMode == StatusBarMode.PERCENTAGE) {
            statusText = OStrings.getProgressBarDefaultPrecentageText();
            tooltipText = "MW_PROGRESS_TOOLTIP_PERCENTAGE";
        }
        Mnemonics.setLocalizedText(mainWindow.progressLabel, statusText);
        mainWindow.progressLabel.setToolTipText(OStrings.getString(tooltipText));

        mainWindow.progressLabel.setBorder(border);
        mainWindow.progressLabel.addMouseListener(new MouseAdapter() {
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

        Mnemonics.setLocalizedText(mainWindow.lengthLabel, OStrings.getString("MW_SEGMENT_LENGTH_DEFAULT"));
        mainWindow.lengthLabel.setToolTipText(OStrings.getString("MW_SEGMENT_LENGTH_TOOLTIP"));
        mainWindow.lengthLabel.setBorder(border);
        mainWindow.lengthLabel.setFocusable(false);

        JPanel statusPanel2 = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        statusPanel2.add(mainWindow.lockInsertLabel);
        statusPanel2.add(mainWindow.progressLabel);
        statusPanel2.add(mainWindow.lengthLabel);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(mainWindow.statusLabel, BorderLayout.CENTER);
        statusPanel.add(statusPanel2, BorderLayout.EAST);
        statusPanel.setBorder(UIManager.getBorder("OmegaTMainWindowBottomMargin.border"));

        Color bgColor = UIManager.getColor("AutoHideButtonPanel.background");
        if (bgColor != null) {
            statusPanel.setBackground(bgColor);
            statusPanel2.setBackground(bgColor);
        }

        return statusPanel;
    }

    /**
     * Initialize the size of OmegaT window, then load the layout prefs.
     */
    public static void initializeScreenLayout(MainWindow mainWindow) {
        /**
         * (23dec22) Set a reasonable default window size assuming a standard"pro" laptop resolution of 1920x1080.
         * Smaller screens do not need to be considered since OmegaT will just use the whole window size in such cases.
         */

        // Check the real available space accounting for macOS DOCK, Windows Toolbar, etc.
        Rectangle localAvailableSpace = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        int screenWidth = localAvailableSpace.width;
        int screenHeight = localAvailableSpace.height;
        int omegatWidth = OConsts.OMEGAT_WINDOW_WIDTH;
        int omegatHeight = OConsts.OMEGAT_WINDOW_HEIGHT;

        if (omegatWidth > screenWidth) {
            omegatWidth = screenWidth;
        }

        if (omegatHeight > screenHeight) {
            omegatHeight = screenHeight;
        }

        // Attempt to center the OmegaT main window on the screen
        int omegatLeftPosition = (screenWidth - omegatWidth) / 2;

        Rectangle defaultWindowSize = new Rectangle(omegatLeftPosition, 0, omegatWidth, omegatHeight);

        mainWindow.setBounds(defaultWindowSize);
        StaticUIUtils.persistGeometry(mainWindow, Preferences.MAINWINDOW_GEOMETRY_PREFIX);

        loadScreenLayoutFromPreferences(mainWindow);

        // Ensure any "closed" Dockables are visible. These can be newly added
        // panes not included in an older layout file, or e.g. panes installed by
        // plugins.
        UIDesignManager.ensureDockablesVisible(mainWindow.desktop);
    }

    /**
     * Load the main window layout from the global preferences file. Will reset to defaults if global preferences are
     * not present or if an error occurs.
     */
    private static void loadScreenLayoutFromPreferences(MainWindow mainWindow) {
        File uiLayoutFile = new File(StaticUtils.getConfigDir(), MainWindowUI.UI_LAYOUT_FILE);
        if (uiLayoutFile.exists()) {
            loadScreenLayout(mainWindow, uiLayoutFile);
        } else {
            resetDesktopLayout(mainWindow);
        }
    }

    /**
     * Load the main window layout from the specified file. Will reset to defaults if an error occurs.
     */
    private static void loadScreenLayout(MainWindow mainWindow, File uiLayoutFile) {
        try (InputStream in = new FileInputStream(uiLayoutFile)) {
            mainWindow.desktop.readXML(in);
        } catch (Exception ex) {
            Log.log(ex);
            resetDesktopLayout(mainWindow);
        }
    }

    /**
     * Stores main window docking layout to disk.
     */
    public static void saveScreenLayout(MainWindow mainWindow) {
        File uiLayoutFile = new File(StaticUtils.getConfigDir(), MainWindowUI.UI_LAYOUT_FILE);
        saveScreenLayout(mainWindow, uiLayoutFile);
    }

    /**
     * Stores main window layout to the specified output file.
     */
    private static void saveScreenLayout(MainWindow mainWindow, File uiLayoutFile) {
        try (OutputStream out = new FileOutputStream(uiLayoutFile)) {
            mainWindow.desktop.writeXML(out);
        } catch (Exception ex) {
            Log.log(ex);
        }
    }

    /**
     * Restores main window layout to the default values (distinct from global preferences).
     */
    public static void resetDesktopLayout(MainWindow mainWindow) {
        try (InputStream in = MainWindowUI.class.getResourceAsStream("DockingDefaults.xml")) {
            mainWindow.desktop.readXML(in);
        } catch (Exception e) {
            Log.log(e);
        }
    }
}
