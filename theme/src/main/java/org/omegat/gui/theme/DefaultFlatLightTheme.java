/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura.
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

import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.jspecify.annotations.NullMarked;
import org.omegat.util.gui.Styles.EditorColor;

/**
 * A default light theme customized from FlatLightLaf.
 *
 * @author Hiroshi Miura
 */
@SuppressWarnings("serial")
@NullMarked
public class DefaultFlatLightTheme extends FlatLaf {
    private static final String NAME = "Flat light";
    private static final String ID = "FlatLightTheme";
    private static final String DESCRIPTION = "A theme customized from FlatLightLaf";

    /**
     * Registers the default Flat Light Theme with the system UIManager.
     */
    public static void loadPlugins() {
        UIManager.installLookAndFeel(NAME, DefaultFlatLightTheme.class.getName());
    }

    public static void unloadPlugins() {
    }

    @Override
    public boolean isDark() {
        return false;
    }

    /**
     * Return default theme configurations.
     */
    @Override
    public UIDefaults getDefaults() {
        UIDefaults origin = new FlatLightLaf().getDefaults();
        // get omegat defaults
        UIDefaults defaults = DefaultFlatTheme.setDefaults(origin, ID);
        UIDefaults custom = setLightDefaults(defaults);
        UIManager.put("DockViewTitleBar.border", new MatteBorder(1, 1, 1, 1, custom.getColor("borderColor")));
        DefaultFlatDarkTheme.setupDecoration();
        return custom;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    private static UIDefaults setLightDefaults(UIDefaults defaults) {
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
        // they style, so a typo cannot silently disconnect a value.
        defaults.put(EditorColor.COLOR_ACTIVE_SOURCE.getUIManagerKey(), new Color(0xc0ffc0));
        defaults.put(EditorColor.COLOR_SOURCE.getUIManagerKey(), new Color(0xc0ffc0));
        defaults.put(EditorColor.COLOR_NOTED.getUIManagerKey(), new Color(0xc0ffff));
        defaults.put(EditorColor.COLOR_UNTRANSLATED.getUIManagerKey(), new Color(0xc0c0ff));
        defaults.put(EditorColor.COLOR_TRANSLATED.getUIManagerKey(), new Color(0xffff99));
        defaults.put(EditorColor.COLOR_NON_UNIQUE.getUIManagerKey(), new Color(0x666666));
        defaults.put(EditorColor.COLOR_PLACEHOLDER.getUIManagerKey(), new Color(0x6b6b6b));
        defaults.put(EditorColor.COLOR_REMOVETEXT_TARGET.getUIManagerKey(), new Color(0xdb0000));
        defaults.put(EditorColor.COLOR_NBSP.getUIManagerKey(), new Color(0x888888));
        defaults.put(EditorColor.COLOR_WHITESPACE.getUIManagerKey(), new Color(0x808080));
        defaults.put(EditorColor.COLOR_BIDIMARKERS.getUIManagerKey(), new Color(0xc80000));
        defaults.put(EditorColor.COLOR_PARAGRAPH_START.getUIManagerKey(), new Color(0x888888));
        defaults.put(EditorColor.COLOR_MARK_COMES_FROM_TM_MT.getUIManagerKey(), new Color(0xfa8072));
        defaults.put(EditorColor.COLOR_MARK_COMES_FROM_TM_XICE.getUIManagerKey(), new Color(0xaf76df));
        defaults.put(EditorColor.COLOR_MARK_COMES_FROM_TM_X100PC.getUIManagerKey(), new Color(0xff9408));
        defaults.put(EditorColor.COLOR_MARK_COMES_FROM_TM_XAUTO.getUIManagerKey(), new Color(0xff9596));
        defaults.put(EditorColor.COLOR_MARK_COMES_FROM_TM_XENFORCED.getUIManagerKey(), new Color(0xffccff));
        defaults.put(EditorColor.COLOR_REPLACE.getUIManagerKey(), new Color(0x0000ff));
        defaults.put(EditorColor.COLOR_LANGUAGE_TOOLS.getUIManagerKey(), new Color(0x0000ff));
        defaults.put(EditorColor.COLOR_TRANSTIPS.getUIManagerKey(), new Color(0x0000ff));
        defaults.put(EditorColor.COLOR_SPELLCHECK.getUIManagerKey(), new Color(0xff0000));
        defaults.put(EditorColor.COLOR_TERMINOLOGY.getUIManagerKey(), new Color(0xb84c00));
        defaults.put(EditorColor.COLOR_MATCHES_CHANGED.getUIManagerKey(), new Color(0x0000ff));
        defaults.put(EditorColor.COLOR_MATCHES_UNCHANGED.getUIManagerKey(), new Color(0x007a00));
        defaults.put(EditorColor.COLOR_MATCHES_INS_ACTIVE.getUIManagerKey(), new Color(0x0000ff));
        defaults.put(EditorColor.COLOR_MATCHES_INS_INACTIVE.getUIManagerKey(), new Color(0x6c6c6c));
        defaults.put(EditorColor.COLOR_HYPERLINK.getUIManagerKey(), new Color(0x0000ff));
        defaults.put(EditorColor.COLOR_SEARCH_FOUND_MARK.getUIManagerKey(), new Color(0x0000ff));
        defaults.put(EditorColor.COLOR_SEARCH_REPLACE_MARK.getUIManagerKey(), new Color(0x995c00));
        defaults.put(EditorColor.COLOR_NOTIFICATION_MIN.getUIManagerKey(), new Color(0xfff2d4));
        defaults.put(EditorColor.COLOR_NOTIFICATION_MAX.getUIManagerKey(), new Color(0xff9900));
        defaults.put(EditorColor.COLOR_ALIGNER_ACCEPTED.getUIManagerKey(), new Color(0x15bb45));
        defaults.put(EditorColor.COLOR_ALIGNER_NEEDSREVIEW.getUIManagerKey(), new Color(0xff0000));
        defaults.put(EditorColor.COLOR_ALIGNER_HIGHLIGHT.getUIManagerKey(), new Color(0xffff00));
        defaults.put(EditorColor.COLOR_ALIGNER_TABLE_ROW_HIGHLIGHT.getUIManagerKey(), new Color(0xc8c8c8));
        defaults.put("OmegaT.projectFilesCurrentFileForeground", new Color(0x0));
        defaults.put("OmegaT.projectFilesCurrentFileBackground", new Color(0xc8ddf2));
        defaults.put("OmegaT.searchFieldErrorText", new Color(0xff0000));
        defaults.put("OmegaT.searchDimmedBackground", new Color(0x80, 0x80, 0x80, 0x80));
        defaults.put("OmegaT.searchResultBorder", new Color(0xEE, 0xD2, 0x00, 0x80));
        defaults.put(EditorColor.COLOR_MACHINETRANSLATE_SELECTED_HIGHLIGHT.getUIManagerKey(),
                new Color(0xffff00));
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
        Color activeTitleText = defaults.getColor("Label.foreground");
        Color activeTitleBgColor = DefaultFlatTheme.adjustRGB(standardBgColor, 0xF6 - 0xEE);
        Color inactiveTitleText = new Color(0x808080);
        defaults.put("InternalFrame.activeTitleForeground", activeTitleText);
        defaults.put("InternalFrame.activeTitleBackground", activeTitleBgColor);
        defaults.put("InternalFrame.inactiveTitleForeground", inactiveTitleText);
        defaults.put("InternalFrame.inactiveTitleBackground", standardBgColor);

        // Undocked panel
        defaults.put("activeCaption", Color.WHITE);
        defaults.put("activeCaptionBorder", borderColor);
        defaults.put("inactiveCaption", standardBgColor);
        defaults.put("inactiveCaptionBorder", borderColor);

        defaults.put("TabbedPane.tabSeparatorsFullHeight", true);
        defaults.put("TabbedPane.selectedBackground", Color.white);
        return defaults;
    }

}
