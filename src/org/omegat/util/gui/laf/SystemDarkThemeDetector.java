/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2000-2021 JetBrains s.r.o.
 *                2023 Hiroshi Miura
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

import org.omegat.util.Platform;

/**
 * Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by
 * the Apache 2.0 license.
 */
public abstract class SystemDarkThemeDetector {

    public static SystemDarkThemeDetector createDetector() {
        if (Platform.isMacOSX()) {
            return new MacOSDarkThemeDetector();
        } else if (Platform.isLinux()) {
            return new EmptyDarkThemeDetector();
        } else {
            return new WindowsDarkThemeDetector();
        }
    }

    public abstract boolean isDark();

    public abstract boolean detectionSupported();
}
