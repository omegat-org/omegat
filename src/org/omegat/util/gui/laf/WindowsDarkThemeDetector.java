/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2000-2021 JetBrains s.r.o.
 *                2023 Hiroshi Miura.
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

package org.omegat.util.gui.laf;

import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;

import org.omegat.util.JnaLoader;

/**
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by
 * the Apache 2.0 license.
 */
public class WindowsDarkThemeDetector extends SystemDarkThemeDetector {
    private static final String REGISTRY_PATH = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes"
            + "\\Personalize";
    private static final String REGISTRY_VALUE = "AppsUseLightTheme";

    public WindowsDarkThemeDetector() {
    }

    @Override
    public boolean isDark() {
        try {
            return Advapi32Util.registryValueExists(WinReg.HKEY_CURRENT_USER, REGISTRY_PATH, REGISTRY_VALUE)
                    && Advapi32Util.registryGetIntValue(WinReg.HKEY_CURRENT_USER, REGISTRY_PATH,
                            REGISTRY_VALUE) == 0;
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public boolean detectionSupported() {
        return Platform.isWindows() && JnaLoader.isLoaded();
    }
}
