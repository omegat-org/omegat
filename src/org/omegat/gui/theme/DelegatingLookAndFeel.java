/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Aaron Madlon-Kay
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

package org.omegat.gui.theme;

import java.awt.Component;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.LayoutStyle;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.madlonkay.desktopsupport.DesktopSupport;
import org.omegat.util.gui.ResourcesUtil;

/**
 * A LAF class that delegates almost everything to the system LAF.
 *
 * Implementers must provide a distinct identity ({@link #getName()},
 * {@link #getID()}, {@link #getDescription()}). They should also probably
 * supply novel defaults ({@link #initialize()}, {@link #getDefaults()}).
 *
 * @author Aaron Madlon-Kay
 */
public abstract class DelegatingLookAndFeel extends LookAndFeel {
    /**
     * Load icon from classpath.
     *
     * @param iconName
     *            icon file name
     * @return icon instance
     */
    protected static ImageIcon getIcon(String iconName) {
        Image image = ResourcesUtil.getBundledImage(iconName);
        return image == null ? null : new ImageIcon(image);
    }

    protected final LookAndFeel systemLookAndFeel;

    public DelegatingLookAndFeel() throws Exception {
        String systemLafClass = UIManager.getSystemLookAndFeelClassName();
        String systemLafName = null;
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if (info.getClassName().equals(systemLafClass)) {
                systemLafName = info.getName();
            }
        }
        if (systemLafName == null) {
            // Should never happen: system LAF is guaranteed to be installed
            throw new RuntimeException("Could not identify system LAF name");
        }
        systemLookAndFeel = DesktopSupport.getSupport().createLookAndFeel(systemLafName);
    }

    @Override
    public boolean isNativeLookAndFeel() {
        return systemLookAndFeel.isNativeLookAndFeel();
    }

    @Override
    public boolean isSupportedLookAndFeel() {
        return systemLookAndFeel.isSupportedLookAndFeel();
    }

    @Override
    public LayoutStyle getLayoutStyle() {
        return systemLookAndFeel.getLayoutStyle();
    }

    @Override
    public Icon getDisabledIcon(JComponent component, Icon icon) {
        return systemLookAndFeel.getDisabledIcon(component, icon);
    }

    @Override
    public Icon getDisabledSelectedIcon(JComponent component, Icon icon) {
        return systemLookAndFeel.getDisabledSelectedIcon(component, icon);
    }

    @Override
    public boolean getSupportsWindowDecorations() {
        return systemLookAndFeel.getSupportsWindowDecorations();
    }

    @Override
    public void provideErrorFeedback(Component component) {
        systemLookAndFeel.provideErrorFeedback(component);
    }

    @Override
    public void initialize() {
        systemLookAndFeel.initialize();
    }

    @Override
    public void uninitialize() {
        systemLookAndFeel.uninitialize();
    }
}
