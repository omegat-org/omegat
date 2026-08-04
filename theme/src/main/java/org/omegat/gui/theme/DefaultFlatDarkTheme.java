/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2021-2023 Hiroshi Miura
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

package org.omegat.gui.theme;

import java.awt.Color;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.util.SystemInfo;
import org.jspecify.annotations.NullMarked;
import org.omegat.util.gui.Styles.EditorColor;

/**
 * A default dark theme customized from FlatDarkLaf.
 *
 * @author Hiroshi Miura
 */
@SuppressWarnings("serial")
@NullMarked
public class DefaultFlatDarkTheme extends FlatLaf {
    private static final String NAME = "Flat dark theme";
    private static final String ID = "FlatDarkTheme";
    private static final String DESCRIPTION = "A theme from FlatDarkLaf";
    private final LookAndFeel parent;

    /**
     * Registers the default Flat Dark Theme with the system UIManager.
     */
    public static void loadPlugins() {
        UIManager.installLookAndFeel(NAME, DefaultFlatDarkTheme.class.getName());
    }

    public static void unloadPlugins() {
    }

    public DefaultFlatDarkTheme() {
        parent = new FlatDarkLaf();
    }

    @Override
    public boolean isDark() {
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public UIDefaults getDefaults() {
        UIDefaults original = parent.getDefaults();
        // get omegat defaults
        UIDefaults defaults = DefaultFlatTheme.setDefaults(original, ID);
        UIDefaults custom = setDarkDefaults(defaults);
        UIManager.put("DockViewTitleBar.border", new MatteBorder(1, 1, 1, 1, custom.getColor("border")));
        setupDecoration();
        return custom;
    }

    private static UIDefaults setDarkDefaults(UIDefaults defaults) {

        // GTK+ has bug that TextPane background is fixed white.
        // https://sourceforge.net/p/omegat/bugs/1013/
        Color standardBgColor = defaults.getColor("Panel.background");
        defaults.put("TextPane.background", standardBgColor);

        // Borders
        Color borderColor = defaults.getColor("Component.borderColor");
        defaults.put("OmegaTBorder.color", borderColor);
        defaults.put("borderColor", borderColor);

        // OmegaT-defined Dockables.
        defaults.put("OmegaTDockablePanel.border", new MatteBorder(1, 1, 1, 1, borderColor));
        defaults.put("OmegaTDockablePanel.isProportionalMargins", true);

        // Default application Colors. Keys come from the EditorColor entry
        // they style, so a typo cannot silently disconnect a value. Pen and
        // foreground colors are lightened for the dark background; the dark
        // background fills stay as designed.
        defaults.put(EditorColor.COLOR_ACTIVE_SOURCE.getUIManagerKey(), new Color(0x276d27));
        defaults.put(EditorColor.COLOR_SOURCE.getUIManagerKey(), new Color(0x2b6570));
        defaults.put(EditorColor.COLOR_NOTED.getUIManagerKey(), new Color(0x306030));
        defaults.put(EditorColor.COLOR_UNTRANSLATED.getUIManagerKey(), new Color(0x4d4daa));
        defaults.put(EditorColor.COLOR_TRANSLATED.getUIManagerKey(), new Color(0x57572d));
        defaults.put(EditorColor.COLOR_NON_UNIQUE.getUIManagerKey(), new Color(0xebebeb));
        defaults.put(EditorColor.COLOR_PLACEHOLDER.getUIManagerKey(), new Color(0xadadad));
        defaults.put(EditorColor.COLOR_REMOVETEXT_TARGET.getUIManagerKey(), new Color(0xff9d9d));
        defaults.put(EditorColor.COLOR_NBSP.getUIManagerKey(), new Color(0xc8c8c8));
        defaults.put(EditorColor.COLOR_WHITESPACE.getUIManagerKey(), new Color(0x8d8d8d));
        defaults.put(EditorColor.COLOR_BIDIMARKERS.getUIManagerKey(), new Color(0xff8a80));
        defaults.put(EditorColor.COLOR_PARAGRAPH_START.getUIManagerKey(), new Color(0xaeaeae));
        defaults.put(EditorColor.COLOR_MARK_COMES_FROM_TM_MT.getUIManagerKey(), new Color(0xaa8072));
        defaults.put(EditorColor.COLOR_MARK_COMES_FROM_TM_XICE.getUIManagerKey(), new Color(0x9163B7));
        defaults.put(EditorColor.COLOR_MARK_COMES_FROM_TM_X100PC.getUIManagerKey(), new Color(0x3f5488));
        defaults.put(EditorColor.COLOR_MARK_COMES_FROM_TM_XAUTO.getUIManagerKey(), new Color(0x64456C));
        defaults.put(EditorColor.COLOR_MARK_COMES_FROM_TM_XENFORCED.getUIManagerKey(), new Color(0x108810));
        defaults.put(EditorColor.COLOR_REPLACE.getUIManagerKey(), new Color(0x4fa3ff));
        defaults.put(EditorColor.COLOR_LANGUAGE_TOOLS.getUIManagerKey(), new Color(0x4fa3ff));
        defaults.put(EditorColor.COLOR_TRANSTIPS.getUIManagerKey(), new Color(0x4fa3ff));
        defaults.put(EditorColor.COLOR_SPELLCHECK.getUIManagerKey(), new Color(0xff5252));
        defaults.put(EditorColor.COLOR_TERMINOLOGY.getUIManagerKey(), new Color(0xffa94d));
        defaults.put(EditorColor.COLOR_MATCHES_CHANGED.getUIManagerKey(), new Color(0x69b0ff));
        defaults.put(EditorColor.COLOR_MATCHES_UNCHANGED.getUIManagerKey(), new Color(0x6cd96c));
        defaults.put(EditorColor.COLOR_MATCHES_INS_ACTIVE.getUIManagerKey(), new Color(0x69b0ff));
        defaults.put(EditorColor.COLOR_MATCHES_INS_INACTIVE.getUIManagerKey(), new Color(0x4bbcbc));
        defaults.put(EditorColor.COLOR_HYPERLINK.getUIManagerKey(), new Color(0x69b0ff));
        defaults.put(EditorColor.COLOR_SEARCH_FOUND_MARK.getUIManagerKey(), new Color(0x69b0ff));
        defaults.put(EditorColor.COLOR_SEARCH_REPLACE_MARK.getUIManagerKey(), new Color(0xffb866));
        defaults.put(EditorColor.COLOR_NOTIFICATION_MIN.getUIManagerKey(), new Color(0x332233));
        defaults.put(EditorColor.COLOR_NOTIFICATION_MAX.getUIManagerKey(), new Color(0x647354));
        defaults.put(EditorColor.COLOR_ALIGNER_ACCEPTED.getUIManagerKey(), new Color(0x156b45));
        defaults.put(EditorColor.COLOR_ALIGNER_NEEDSREVIEW.getUIManagerKey(), new Color(0x8f0000));
        defaults.put(EditorColor.COLOR_ALIGNER_HIGHLIGHT.getUIManagerKey(), new Color(0x4f4f00));
        defaults.put(EditorColor.COLOR_ALIGNER_TABLE_ROW_HIGHLIGHT.getUIManagerKey(), new Color(0x787878));
        defaults.put("OmegaT.projectFilesCurrentFileForeground", new Color(0x0));
        defaults.put("OmegaT.projectFilesCurrentFileBackground", new Color(0x788d92));
        defaults.put("OmegaT.searchFieldErrorText", new Color(0xff6b68));
        defaults.put("OmegaT.searchDimmedBackground", new Color(0x80, 0x80, 0x80, 0x80));
        defaults.put("OmegaT.searchResultBorder", new Color(0xEE, 0xD2, 0x00, 0x80));
        defaults.put(EditorColor.COLOR_MACHINETRANSLATE_SELECTED_HIGHLIGHT.getUIManagerKey(),
                new Color(0xaf3900));
        defaults.put(EditorColor.COLOR_PROJECT_FILES_PROGRESS_LOW.getUIManagerKey(), new Color(0xf0b8b4));
        defaults.put(EditorColor.COLOR_PROJECT_FILES_PROGRESS_HIGH.getUIManagerKey(), new Color(0xb7d7b7));
        defaults.put(EditorColor.COLOR_PROJECT_FILES_PROGRESS_COMPLETE.getUIManagerKey(),
                new Color(0xb8ccf0));

        // Text and surface colors that default to the theme's own base
        // colors, so they render exactly like the former "inherit"
        // behavior until a user configures them.
        Color standardFgColor = defaults.getColor("TextPane.foreground");
        defaults.put(EditorColor.COLOR_ACTIVE_SOURCE_FG.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_ACTIVE_TARGET.getUIManagerKey(), standardBgColor);
        defaults.put(EditorColor.COLOR_ACTIVE_TARGET_FG.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_SEGMENT_MARKER_FG.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_SEGMENT_MARKER_BG.getUIManagerKey(), standardBgColor);
        defaults.put(EditorColor.COLOR_SOURCE_FG.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_NOTED_FG.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_UNTRANSLATED_FG.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_TRANSLATED_FG.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_MOD_INFO.getUIManagerKey(), standardBgColor);
        defaults.put(EditorColor.COLOR_MOD_INFO_FG.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_GLOSSARY_SOURCE.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_GLOSSARY_TARGET.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_GLOSSARY_NOTE.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_MATCHES_DEL_ACTIVE.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_MATCHES_DEL_INACTIVE.getUIManagerKey(), standardFgColor);
        defaults.put(EditorColor.COLOR_NON_UNIQUE_BG.getUIManagerKey(), DefaultFlatTheme
                .compositeOver(defaults.getColor("OmegaT.alternatingHilite"), standardBgColor));

        // Panel title bars
        Color activeTitleBgColor = DefaultFlatTheme.adjustRGB(standardBgColor, 0xF6 - 0xEE);
        Color activeTitleText = defaults.getColor("Label.foreground");
        Color inactiveTitleText = new Color(0x767676);

        defaults.put("InternalFrame.activeTitleForeground", activeTitleText);
        defaults.put("InternalFrame.activeTitleBackground", activeTitleBgColor);
        defaults.put("InternalFrame.inactiveTitleForeground", inactiveTitleText);
        defaults.put("InternalFrame.inactiveTitleBackground", standardBgColor);

        // Undocked panel
        defaults.put("activeCaption", Color.GRAY);
        defaults.put("activeCaptionBorder", borderColor);
        defaults.put("inactiveCaption", standardBgColor);
        defaults.put("inactiveCaptionBorder", borderColor);

        defaults.put("TabbedPane.tabSeparatorsFullHeight", true);
        defaults.put("TabbedPane.selectedBackground", Color.GRAY);

        return defaults;
    }

    static void setupDecoration() {
        if (SystemInfo.isLinux) {
            // enable custom window decorations
            JFrame.setDefaultLookAndFeelDecorated(true);
        }
        JDialog.setDefaultLookAndFeelDecorated(false);
    }
}
