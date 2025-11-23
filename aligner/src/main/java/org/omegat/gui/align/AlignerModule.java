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
import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.ResourceBundle;

import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.jetbrains.annotations.Nullable;
import org.openide.awt.Mnemonics;

import org.omegat.core.Core;
import org.omegat.core.CoreEvents;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.events.IApplicationEventListener;
import org.omegat.util.Language;
import org.omegat.util.Preferences;
import org.omegat.util.gui.MenuExtender;

public final class AlignerModule implements IApplicationEventListener {

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("org.omegat.gui.align.Bundle");
    private static @Nullable IApplicationEventListener alignerListener;

    private AlignerModule() {
    }

    /**
     * Register plugins into OmegaT.
     */
    @SuppressWarnings("unused")
    public static void loadPlugins() {
        alignerListener = new AlignerModule();
        CoreEvents.registerApplicationEventListener(alignerListener);
    }

    @SuppressWarnings("unused")
    public static void unloadPlugins() {
        if (alignerListener != null) {
            CoreEvents.unregisterApplicationEventListener(alignerListener);
        }
    }

    private @Nullable JMenuItem alignerMenu;
    private @Nullable Component mainWindow = null;

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
        MenuExtender.removeMenuItems(MenuExtender.MenuKey.TOOLS, Collections.singletonList(alignerMenu));
    }

    private void registerMenu() {
        alignerMenu = new JMenuItem();
        alignerMenu.setName("aligner");
        Mnemonics.setLocalizedText(alignerMenu, BUNDLE.getString("TF_MENU_TOOLS_ALIGN_FILES"));
        alignerMenu.addActionListener(this::showAlignerDialog);
        MenuExtender.addMenuItem(MenuExtender.MenuKey.TOOLS, alignerMenu);
    }

    private void showAlignerDialog(ActionEvent actionEvent) {
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
            alignerShow(sourceLanguage, sourceFile, targetlanguage, null, srcRoot, props.getTMRoot());
        } else {
            String srcLang = Preferences.getPreference(Preferences.SOURCE_LOCALE);
            String trgLang = Preferences.getPreference(Preferences.TARGET_LOCALE);
            alignerShow(srcLang, null, trgLang, null);
        }
    }

    /**
     * Displays the Aligner dialog for the provided source and target language
     * files.
     *
     * @param sourceLanguage
     *            The language code of the source file (e.g., "en").
     * @param sourceFile
     *            The path to the source file to be aligned.
     * @param targetLanguage
     *            The language code of the target file (e.g., "fr").
     * @param targetFile
     *            The path to the target file to be aligned.
     */
    public void alignerShow(@Nullable String sourceLanguage, @Nullable String sourceFile,
                            @Nullable String targetLanguage, @Nullable String targetFile) {
        Language srcLang = null;
        Language trgLang = null;
        if (sourceLanguage != null && !sourceLanguage.isEmpty()) {
            srcLang = new Language(sourceLanguage);
        }
        if (targetLanguage != null && !targetLanguage.isEmpty()) {
            trgLang = new Language(targetLanguage);
        }
        alignerShow(srcLang, sourceFile, trgLang, targetFile, null, null);
    }

    /**
     * Displays the Aligner dialog for the provided source and target language
     * files. Configures default source and save directories if provided.
     *
     * @param sourceLanguage
     *            The source language for the alignment process.
     * @param sourceFile
     *            The path to the source file to be aligned.
     * @param targetLanguage
     *            The target language for the alignment process.
     * @param targetFile
     *            The path to the target file to be aligned.
     * @param defaultDir
     *            The default directory used for selecting source files.
     * @param defaultSaveDir
     *            The default directory used for saving aligned files.
     */
    public void alignerShow(@Nullable Language sourceLanguage, @Nullable String sourceFile,
                            @Nullable Language targetLanguage, @Nullable String targetFile, @Nullable String defaultDir,
                            @Nullable String defaultSaveDir) {
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
        if (sourceFile != null && !sourceFile.isEmpty()) {
            picker.setSourceFile(sourceFile);
        }
        if (targetFile != null && !targetFile.isEmpty()) {
            picker.setTargetFile(targetFile);
        }
        picker.show(mainWindow);
    }

    /**
     * Displays the Aligner dialog by invoking the `alignerShow` method from the
     * `AlignerModule` class with default parameters set to null.
     * <p>
     * The method initializes an instance of `AlignerModule` and calls its
     * `alignerShow` method, which is responsible for configuring and presenting
     * the file alignment interface to the user.
     */
    public static void alignerShow() {
        new AlignerModule().alignerShow(null, null, null, null, null, null);
    }

    /**
     * Displays the Aligner dialog by invoking the `alignerShow` method from the
     * `AlignerModule` class. This method initializes an instance of
     * `AlignerModule` and calls its `alignerShow` method with default
     * parameters, enabling configuration and presentation of the file alignment
     * interface.
     *
     * @param srcRoot
     *            The root directory containing the source files to be aligned.
     */
    public static void alignerShow(String srcRoot) {
        new AlignerModule().alignerShow(null, null, null, null, srcRoot, null);
    }
}
