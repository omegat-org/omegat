/*
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
import java.io.IOException;
import java.util.Collections;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.gui.UIDesignManager;
import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.util.Language;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.gui.MenuExtender;

public final class AlignerModule implements IApplicationEventListener {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.omegat.gui.align.Bundle");
    private static IApplicationEventListener alignerListener;

    private AlignerModule() {
    }

    /**
     * Register plugins into OmegaT.
     */
    public static void loadPlugins() {
        alignerListener = new AlignerModule();
        CoreEvents.registerApplicationEventListener(alignerListener);
    }

    public static void unloadPlugins() {
        if (alignerListener != null) {
            CoreEvents.unregisterApplicationEventListener(alignerListener);
        }
    }

    private JMenuItem alignerMenu;
    private Component mainWindow = null;

    @Override
    public void onApplicationStartup() {
        mainWindow = Core.getMainWindow().getApplicationFrame();
        SwingUtilities.invokeLater(this::registerMenu);
    }

    @Override
    public void onApplicationShutdown() {
        unregisterMenu();
        mainWindow = null;
    }

    private void unregisterMenu() {
        MenuExtender.removeMenuItems(MenuExtender.MenuKey.TOOLS,
                Collections.singletonList(alignerMenu));
    }

    private void registerMenu() {
        alignerMenu = new JMenuItem();
        alignerMenu.setName("aligner");
        Mnemonics.setLocalizedText(alignerMenu, BUNDLE.getString("TF_MENU_TOOLS_ALIGN_FILES"));
        alignerMenu.addActionListener(actionEvent -> alignerShow());
        MenuExtender.addMenuItem(MenuExtender.MenuKey.TOOLS, alignerMenu);
    }

    public void alignerShow() {
        if (Core.getProject().isProjectLoaded()) {
            ProjectProperties props = Core.getProject().getProjectProperties();
            String srcRoot = props.getSourceRoot();
            String curFile = Core.getEditor().getCurrentFile();
            Language sourceLanguage = props.getSourceLanguage();
            Language targetlanguage = props.getTargetLanguage();
            String sourceFile = null;
            if (curFile != null) {
                sourceFile = srcRoot + curFile;
            }
            showAligner(sourceLanguage, sourceFile, targetlanguage, null, srcRoot, props.getTMRoot());
        } else {
            String srcLang = Preferences.getPreference(Preferences.SOURCE_LOCALE);
            String trgLang = Preferences.getPreference(Preferences.TARGET_LOCALE);
            showAligner(srcLang, null, trgLang, null);
        }
    }

    public void showAligner(String sourceLanguage, String sourceFile, String targetLanguage, String targetFile) {
        Language srcLang = null;
        Language trgLang = null;
        if (sourceLanguage != null && !sourceLanguage.isEmpty()) {
            srcLang = new Language(sourceLanguage);
        }
        if (targetLanguage != null && !targetLanguage.isEmpty()) {
            trgLang = new Language(targetLanguage);
        }
        showAligner(srcLang, sourceFile, trgLang, targetFile, null, null);
    }

    public void showAligner(Language sourceLanguage, String sourceFile, Language targetLanguage, String targetFile, String defaultDir, String defaultSaveDir) {
        AlignFilePickerController picker = new AlignFilePickerController();
        if (sourceLanguage != null) {
            picker.setSourceLanguage(sourceLanguage);
        }
        if (targetLanguage != null) {
            picker.setTargetLanguage(targetLanguage);
        }
        if (defaultDir != null && !defaultDir.isEmpty()) {
            picker.setSourceDefaultDir(defaultDir);
        }
        if (defaultSaveDir != null && !defaultSaveDir.isEmpty()) {
            picker.setDefaultSaveDir(defaultSaveDir);
        }
        picker.setSourceFile(sourceFile);
        picker.setTargetFile(targetFile);
        picker.show(mainWindow);
    }
}
