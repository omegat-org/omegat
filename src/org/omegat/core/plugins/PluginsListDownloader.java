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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.Manifest;

import org.omegat.core.data.PluginInformation;
import org.omegat.util.WikiGet;


public final class PluginsListDownloader {
    protected static final String LIST_URL = "https://raw.githubusercontent.com/miurahr/omegat-plugins/main/plugins.MF";

    private PluginsListDownloader() {}

    private static String download() {
        try {
            return WikiGet.getURL(LIST_URL);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Set<PluginInformation> getPluginsList() {
        Set<PluginInformation> pluginInfo = new TreeSet<>();
        String raw_value = download();
        if (raw_value != null) {
            Scanner scanner = new Scanner(raw_value);
            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (!line.equals("")) {
                    sb.append(line).append("\n");
                } else {
                    try {
                        Manifest m = new Manifest(new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8)));
                        String pluginClasses = m.getMainAttributes().getValue("OmegaT-Plugins");
                        for (String clazz : pluginClasses.split("\\s+")) {
                            if (clazz.trim().isEmpty()) {
                                continue;
                            }
                            pluginInfo.add(new PluginInformation(clazz, m, false));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sb = new StringBuilder();
                }
            }
            scanner.close();
        }
        return pluginInfo;
    }
}
