/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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

package org.omegat.core.plugins;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.omegat.core.threads.PluginDownloadThread;

public class PluginDowloaderGithubPackages {
    private static final String GITHUB_USER = "miurahr";  /* fixme */
    private static final String GITHUB_TOKEN = "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";  /* fixme */
    private static final String GITHUB_PACKAGES_BASE = "https://maven.pkg.github.com/omegat-org/omegat-plugins/packages/";
    private static final String GROUPID_PREFIX = "org/omegat/plugin/";

    public PluginDowloaderGithubPackages() {
    }

    public void download(final String pluginId, final String version, final File targetDir) throws IOException {
        if (version.endsWith("SNAPSHOT")) {
            /* no support for SNAPSHOT. */
            return;
        }
        String groupIdPath = GROUPID_PREFIX + pluginId.replace(".", "/");
        String baseUrl = GITHUB_PACKAGES_BASE + "/" + groupIdPath + "/" + pluginId + "/" + version + "/";

        String jarFilename = pluginId + "-" + version + ".jar";
        URL jarUrl = new URL(baseUrl + jarFilename);
        PluginDownloadThread downloadThread = new PluginDownloadThread(jarUrl, GITHUB_USER, GITHUB_TOKEN, targetDir, jarFilename);
        downloadThread.checkInterrupted();
    }
}
