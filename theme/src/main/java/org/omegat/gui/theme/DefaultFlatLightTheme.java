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

@SuppressWarnings("serial")
public class DefaultFlatLightTheme extends FlatLaf {
    private static final String NAME = "Flat light";
    private static final String ID = "FlatLightTheme";
    private static final String DESCRIPTION = "A theme customized from FlatLightLaf";

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
        UIDefaults defaults = DefaultFlatTheme.setDefaults(origin, ID); // get
                                                                        // omegat
                                                                        // defaults
        UIDefaults custom = setLightDefaults(defaults);
        UIManager.put("DockViewTitleBar.border", new MatteBorder(1, 1, 1, 1, custom.getColor("borderColor")));
        DefaultFlatDarkTheme.setupDecoration();
        return custom;
    }

    public String getName() {
        return NAME;
    }

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

        // Default application Colors
        defaults.put("OmegaT.activeSource", new Color(0xc0ffc0));
        defaults.put("OmegaT.source", new Color(0xc0ffc0));
        defaults.put("OmegaT.noted", new Color(0xc0ffff));
        defaults.put("OmegaT.untranslated", new Color(0xc0c0ff));
        defaults.put("OmegaT.translated", new Color(0xffff99));
        defaults.put("OmegaT.nonUnique", new Color(0x808080));
        defaults.put("OmegaT.placeholder", new Color(0x969696));
        defaults.put("OmegaT.removeTextTarget", new Color(0xff0000));
        defaults.put("OmegaT.nbsp", new Color(0xc8c8c8));
        defaults.put("OmegaT.whiteSpace", new Color(0x808080));
        defaults.put("OmegaT.bidiMarkers", new Color(0xc80000));
        defaults.put("OmegaT.paragraphStart", new Color(0xaeaeae));
        defaults.put("OmegaT.markComesFromTm", new Color(0xfa8072));
        defaults.put("OmegaT.markComesFromTxXice", new Color(0xaf76df));
        defaults.put("OmegaT.markComesFromTmX100pc", new Color(0xff9408));
        defaults.put("OmegaT.markComesFromTmXauto", new Color(0xff9596));
        defaults.put("OmegaT.markComesFromTmXendorced", new Color(0xffccff));
        defaults.put("OmegaT.replace", new Color(0x0000ff));
        defaults.put("OmegaT.languageTools", new Color(0x0000ff));
        defaults.put("OmegaT.transTips", new Color(0x0000ff));
        defaults.put("OmegaT.spellCheck", new Color(0xff0000));
        defaults.put("OmegaT.terminology", new Color(0xff6600));
        defaults.put("OmegaT.matchesChanged", new Color(0x0000ff));
        defaults.put("OmegaT.matchesUnchanged", new Color(0x00ff00));
        defaults.put("OmegaT.matchesInsActive", new Color(0x0000ff));
        defaults.put("OmegaT.matchesInsInactive", new Color(0x808080));
        defaults.put("OmegaT.hyperlink", new Color(0x0000ff));
        defaults.put("OmegaT.searchFoundMark", new Color(0x0000ff));
        defaults.put("OmegaT.searchReplaceMark", new Color(0xff9900));
        defaults.put("OmegaT.notificationMin", new Color(0xfff2d4));
        defaults.put("OmegaT.notificationMax", new Color(0xff9900));
        defaults.put("OmegaT.alignerAccepted", new Color(0x15bb45));
        defaults.put("OmegaT.alignerNeedsReview", new Color(0xff0000));
        defaults.put("OmegaT.alignerHighlight", new Color(0xffff00));
        defaults.put("OmegaT.alignerTableRowHighlight", new Color(0xc8c8c8));
        defaults.put("OmegaT.projectFilesCurrentFileForeground", new Color(0x0));
        defaults.put("OmegaT.projectFilesCurrentFileBackground", new Color(0xc8ddf2));
        defaults.put("OmegaT.searchFieldErrorText", new Color(0xff0000));
        defaults.put("OmegaT.searchDimmedBackground", new Color(0x80, 0x80, 0x80, 0x80));
        defaults.put("OmegaT.searchResultBorder", new Color(0xEE, 0xD2, 0x00, 0x80));
        defaults.put("OmegaT.machinetranslateSelectedHighlight", new Color(0xffff00));

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
