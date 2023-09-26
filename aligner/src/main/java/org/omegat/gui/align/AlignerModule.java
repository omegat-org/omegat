/*
 *
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 * Copyright (C) 2023 Hiroshi Miura
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

package org.omegat.gui.align;

import java.awt.Component;
import java.util.Collections;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;

import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.util.Language;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.MenuExtender;

public final class AlignerModule {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.omegat.gui.align.Bundle");
    private static IApplicationEventListener alignerListener;

    private AlignerModule() {
    }

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        alignerListener = new IApplicationEventListener() {
            private final JMenuItem alignerMenu = new JMenuItem();
            @Override
            public void onApplicationStartup() {
                registerMenu();
            }

            @Override
            public void onApplicationShutdown() {
                unregisterMenu();
            }

            private void unregisterMenu() {
                MenuExtender.removeMenuItems(MenuExtender.MenuKey.TOOLS, Collections.singletonList(alignerMenu));
            }

            private void registerMenu() {
                Mnemonics.setLocalizedText(alignerMenu, BUNDLE.getString("TF_MENU_TOOLS_ALIGN_FILES"));
                alignerMenu.addActionListener(actionEvent -> alignerShow());
                MenuExtender.addMenuItem(MenuExtender.MenuKey.TOOLS, alignerMenu);
            }

            public void alignerShow() {
                Component mainWindow = Core.getMainWindow().getApplicationFrame();
                AlignFilePickerController picker = new AlignFilePickerController();
                if (Core.getProject().isProjectLoaded()) {
                    String srcRoot = Core.getProject().getProjectProperties().getSourceRoot();
                    String curFile = Core.getEditor().getCurrentFile();
                    if (curFile != null) {
                        picker.setSourceFile(srcRoot + curFile);
                    }
                    picker.setSourceDefaultDir(srcRoot);
                    picker.setDefaultSaveDir(Core.getProject().getProjectProperties().getTMRoot());
                    picker.setSourceLanguage(Core.getProject().getProjectProperties().getSourceLanguage());
                    picker.setTargetLanguage(Core.getProject().getProjectProperties().getTargetLanguage());
                } else {
                    String srcLang = Preferences.getPreference(Preferences.SOURCE_LOCALE);
                    if (!StringUtil.isEmpty(srcLang)) {
                        picker.setSourceLanguage(new Language(srcLang));
                    }
                    String trgLang = Preferences.getPreference(Preferences.TARGET_LOCALE);
                    if (!StringUtil.isEmpty(trgLang)) {
                        picker.setTargetLanguage(new Language(trgLang));
                    }
                }
                picker.show(mainWindow);
            }
        };
        CoreEvents.registerApplicationEventListener(alignerListener);
    }

    public static void unloadPlugins() {
        if (alignerListener != null) {
            CoreEvents.unregisterApplicationEventListener(alignerListener);
        }
    }
}
