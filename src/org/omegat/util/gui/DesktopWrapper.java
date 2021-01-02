/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Lev Abashkin
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

package org.omegat.util.gui;


import org.omegat.util.Platform;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;

/**
 * Wrapper class for java.awt.Desktop with `xdg-open` path on Linux platform.
 */
public class DesktopWrapper {

    private final static Desktop awtDesktop = Desktop.getDesktop();
    private final static boolean useXDGOpen;

    static {
        boolean hasXDGOpen = false;
        if (Platform.isLinux()) {
            try {
                hasXDGOpen = xdgOpen("--help");
            } catch (IOException ex) {
                // Do nothing
            }
        }
        useXDGOpen = hasXDGOpen;
    }

    public static void browse(URI uri) throws IOException {
        if (useXDGOpen) {
            xdgOpen(uri.toString());
        } else {
            awtDesktop.browse(uri);
        }
    }

    public static void open(File file) throws IOException {
        if (useXDGOpen) {
            xdgOpen(file.getPath());
        } else {
            awtDesktop.open(file);
        }
    }

    private static boolean xdgOpen(String s) throws IOException {
        File devNull = new File("/dev/null");
        ProcessBuilder pb = new ProcessBuilder("xdg-open", s);
        pb.redirectOutput(devNull);
        pb.redirectError(devNull);
        Process p = pb.start();

        try {
            return p.waitFor() == 0;
        } catch (InterruptedException ex) {
            return false;
        }
    }

    private DesktopWrapper() {
    }
}
