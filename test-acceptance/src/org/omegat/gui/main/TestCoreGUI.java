/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2024 Hiroshi Miura
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
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.HeadlessException;

import javax.swing.JFrame;
import javax.swing.WindowConstants;
import javax.swing.plaf.FontUIResource;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

import org.omegat.core.Core;
import org.omegat.core.TestCoreInitializer;
import org.omegat.core.data.NotLoadedProject;
import org.omegat.gui.editor.EditorController;
import org.omegat.gui.editor.IEditor;
import org.omegat.gui.editor.MarkerController;
import org.omegat.gui.matches.MatchesTextArea;
import org.omegat.util.OStrings;
import org.omegat.util.TestPreferencesInitializer;
import org.omegat.util.gui.FontUtil;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.UIDesignManager;

import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingDesktop;

public abstract class TestCoreGUI extends AssertJSwingJUnitTestCase {

    protected FrameFixture window;
    protected JFrame frame;

    @Override
    protected void onSetUp() throws Exception {
        TestPreferencesInitializer.init();
        Core.setProject(new NotLoadedProject());
        UIDesignManager.initialize();
        frame = GuiActionRunner.execute(() -> {
            TestMainWindow mw = new TestMainWindow(TestMainWindowMenuHandler.class);
            TestCoreInitializer.initMainWindow(mw);
            MarkerController.init();
            IEditor editor = new EditorController(mw);
            TestCoreInitializer.initEditor(editor);
            new MatchesTextArea(mw);
            return mw.getApplicationFrame();
        });

        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @SuppressWarnings("serial")
    static class TestMainWindow implements IMainWindow {
        private final JFrame applicationFrame;
        private final FontUIResource font;
        public final BaseMainWindowMenu menu;
        public final DockingDesktop desktop;

        TestMainWindow(Class<? extends BaseMainWindowMenuHandler> mainWindowMenuHandler) {
            applicationFrame = new JFrame();
            applicationFrame.setPreferredSize(new Dimension(1920, 1040));
            font = FontUtil.getScaledFont();
            try {
                BaseMainWindowMenuHandler handler =
                        mainWindowMenuHandler.getDeclaredConstructor(IMainWindow.class).newInstance(this);
                menu = new TestMainWindowMenu(this, handler);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            applicationFrame.setJMenuBar(menu.mainMenu);
            applicationFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

            desktop = new DockingDesktop();
            desktop.addDockableStateWillChangeListener(event -> {
                if (event.getFutureState().isClosed()) {
                    event.cancel();
                }
            });
            applicationFrame.getContentPane().add(desktop, BorderLayout.CENTER);
            MainWindowStatusBar mainWindowStatusBar = new MainWindowStatusBar();
            applicationFrame.getContentPane().add(mainWindowStatusBar, BorderLayout.SOUTH);

            StaticUIUtils.setWindowIcon(applicationFrame);

            updateTitle();
            applicationFrame.pack();
        }

        /**
         * Sets the title of the main window appropriately
         */
        private void updateTitle() {
            String s = OStrings.getDisplayNameAndVersion();
            applicationFrame.setTitle(s);
        }

        @Override
        public JFrame getApplicationFrame() {
            return applicationFrame;
        }

        @Override
        public void lockUI() {
        }

        @Override
        public void unlockUI() {
        }

        @Override
        public Font getApplicationFont() {
            return font;
        }

        @Override
        public void showStatusMessageRB(final String messageKey, final Object... params) {
        }

        @Override
        public void showTimedStatusMessageRB(final String messageKey, final Object... params) {
        }

        @Override
        public void showProgressMessage(final String messageText) {
        }

        @Override
        public void showLengthMessage(final String messageText) {
        }

        @Override
        public void showLockInsertMessage(final String messageText, final String toolTip) {
        }

        @Override
        public void displayWarningRB(final String warningKey, final Object... params) {
        }

        @Override
        public void displayWarningRB(final String warningKey, final String supercedesKey, final Object... params) {
        }

        @Override
        public void displayErrorRB(final Throwable ex, final String errorKey, final Object... params) {
        }

        @Override
        public void showErrorDialogRB(final String title, final String message, final Object... args) {
        }

        @Override
        public int showConfirmDialog(final Object message, final String title, final int optionType,
                                     final int messageType) throws HeadlessException {
            return 0;
        }

        @Override
        public void showMessageDialog(final String message) {
        }

        /**
         * {@inheritDoc}
         */
        public void addDockable(Dockable pane) {
            desktop.addDockable(pane);
        }

        @Override
        public void setCursor(final Cursor cursor) {

        }

        @Override
        public Cursor getCursor() {
            return null;
        }


        @Override
        public IMainMenu getMainMenu() {
            return menu;
        }

        @Override
        public DockingDesktop getDesktop() {
            return desktop;
        }

        @Override
        public void resetDesktopLayout() {
        }
    }

    static class TestMainWindowMenu extends BaseMainWindowMenu {

        TestMainWindowMenu(IMainWindow mainWindow, BaseMainWindowMenuHandler mainWindowMenuHandler) {
            super(mainWindow, mainWindowMenuHandler);
            initComponents();
        }

        @Override
        void createMenuBar() {
            mainMenu.add(projectMenu);
            mainMenu.add(editMenu);
            mainMenu.add(gotoMenu);
            mainMenu.add(viewMenu);
            mainMenu.add(toolsMenu);
            mainMenu.add(optionsMenu);
            mainMenu.add(helpMenu);
        }
    }
}
