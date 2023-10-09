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

/**
 * @author Hiroshi Miura
 */
@SuppressWarnings("serial")
public class DefaultFlatDarkTheme extends FlatLaf {
    private static final String NAME = "Flat dark theme";
    private static final String ID = "FlatDarkTheme";
    private static final String DESCRIPTION = "A theme from FlatDarkLaf";
    private final LookAndFeel parent;

    public static void loadPlugins() {
        UIManager.installLookAndFeel(NAME, DefaultFlatDarkTheme.class.getName());
    }

    public static void unloadPlugins() {
    }

    /**
     * Constructor.
     */
    public DefaultFlatDarkTheme() {
        parent = new FlatDarkLaf();
    }

    @Override
    public boolean isDark() {
        return true;
    }

    /**
     * Return human-readable name of theme.
     *
     * @return name.
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Return description of theme.
     *
     * @return description.
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Return default theme configurations.
     */
    @Override
    public UIDefaults getDefaults() {
        UIDefaults original = parent.getDefaults();
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

        // Default application Colors
        defaults.put("OmegaT.activeSource", new Color(0x287128));
        defaults.put("OmegaT.source", new Color(0x327682));
        defaults.put("OmegaT.noted", new Color(0x306030));
        defaults.put("OmegaT.untranslated", new Color(0x4d4daa));
        defaults.put("OmegaT.translated", new Color(0x57572d));
        defaults.put("OmegaT.nonUnique", new Color(0x808080));
        defaults.put("OmegaT.placeholder", new Color(0x969696));
        defaults.put("OmegaT.removeTextTarget", new Color(0x8f0000));
        defaults.put("OmegaT.nbsp", new Color(0xc8c8c8));
        defaults.put("OmegaT.whiteSpace", new Color(0x808080));
        defaults.put("OmegaT.bidiMarkers", new Color(0x480000));
        defaults.put("OmegaT.paragraphStart", new Color(0xaeaeae));
        defaults.put("OmegaT.markComesFromTm", new Color(0xaa8072));
        defaults.put("OmegaT.markComesFromTxXice", new Color(0x9163B7));
        defaults.put("OmegaT.markComesFromTmX100pc", new Color(0x3f5488));
        defaults.put("OmegaT.markComesFromTmXauto", new Color(0x64456C));
        defaults.put("OmegaT.markComesFromTmXendorced", new Color(0x108810));
        defaults.put("OmegaT.replace", new Color(0x00008f));
        defaults.put("OmegaT.languageTools", new Color(0x00008f));
        defaults.put("OmegaT.transTips", new Color(0x00008f));
        defaults.put("OmegaT.spellCheck", new Color(0x8f0000));
        defaults.put("OmegaT.terminology", new Color(0x8f5500));
        defaults.put("OmegaT.matchesChanged", new Color(0x00008f));
        defaults.put("OmegaT.matchesUnchanged", new Color(0x008f00));
        defaults.put("OmegaT.matchesInsActive", new Color(0x00008f));
        defaults.put("OmegaT.matchesInsInactive", new Color(0x308080));
        defaults.put("OmegaT.hyperlink", new Color(0x00008f));
        defaults.put("OmegaT.searchFoundMark", new Color(0x00008f));
        defaults.put("OmegaT.searchReplaceMark", new Color(0x8f4900));
        defaults.put("OmegaT.notificationMin", new Color(0x332233));
        defaults.put("OmegaT.notificationMax", new Color(0x647354));
        defaults.put("OmegaT.alignerAccepted", new Color(0x156b45));
        defaults.put("OmegaT.alignerNeedsReview", new Color(0x8f0000));
        defaults.put("OmegaT.alignerHighlight", new Color(0x4f4f00));
        defaults.put("OmegaT.alignerTableRowHighlight", new Color(0x787878));
        defaults.put("OmegaT.projectFilesCurrentFileForeground", new Color(0x0));
        defaults.put("OmegaT.projectFilesCurrentFileBackground", new Color(0x788d92));
        defaults.put("OmegaT.searchFieldErrorText", new Color(0x7f0000));
        defaults.put("OmegaT.searchDimmedBackground", new Color(0x80, 0x80, 0x80, 0x80));
        defaults.put("OmegaT.searchResultBorder", new Color(0xEE, 0xD2, 0x00, 0x80));
        defaults.put("OmegaT.machinetranslateSelectedHighlight", new Color(0xaf3900));

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
