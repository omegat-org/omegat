/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Lev Abashkin
 Home page: https://www.omegat.org/
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
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.gui;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.omegat.util.Platform;

/**
 * Wrapper class for java.awt.Desktop with `xdg-open` path on Linux platform.
 */
public final class DesktopWrapper {

    private static final boolean USE_XDG_OPEN;

    static {
        boolean hasXDGOpen = false;
        if (Platform.isLinux()) {
            try {
                hasXDGOpen = xdgOpen("--help", true);
            } catch (IOException ex) {
                // Do nothing
            }
        }
        USE_XDG_OPEN = hasXDGOpen;
    }

    public static void browse(URI uri) throws IOException {
        if (USE_XDG_OPEN) {
            xdgOpen(uri.toString(), false);
        } else {
            Desktop.getDesktop().browse(uri);
        }
    }

    public static void open(File file) throws IOException {
        if (USE_XDG_OPEN) {
            xdgOpen(file.getPath(), false);
        } else {
            Desktop.getDesktop().open(file);
        }
    }

    private static boolean xdgOpen(String s, boolean doWait) throws IOException {
        File devNull = new File("/dev/null");
        ProcessBuilder pb = new ProcessBuilder("xdg-open", s);
        pb.redirectOutput(devNull);
        pb.redirectError(devNull);
        Process p = pb.start();

        if (doWait) {
            try {
                return p.waitFor() == 0;
            } catch (InterruptedException ex) {
                return false;
            }
        }
        return false; // Not really used
    }

    private DesktopWrapper() {
    }
}
