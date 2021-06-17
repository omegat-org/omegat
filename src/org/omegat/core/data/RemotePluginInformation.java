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

package org.omegat.core.data;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class RemotePluginInformation extends PluginInformation {
    private final String PLUGIN_JAR_URL = "Plugin-Download-Url";
    private final String PLUGIN_JAR_FILENAME = "Plugin-Jar-Filename";
    private final String PLUGIN_SHA256SUM = "Plugin-Sha256Sum";

    private final String remoteJarFileUrl;
    private final String jarFilename;
    private final String sha256Sum;

    public RemotePluginInformation(String className, Manifest manifest) {
        super(className, manifest);
        Attributes attrs = manifest.getMainAttributes();
        remoteJarFileUrl = attrs.getValue(PLUGIN_JAR_URL);
        jarFilename = getJarFilename(attrs);
        sha256Sum = attrs.getValue(PLUGIN_SHA256SUM);
    }

    public String getRemoteJarFileUrl() {
        return remoteJarFileUrl;
    }

    public String getJarFilename() {
        return jarFilename;
    }

    public String getSha256Sum() {
        return sha256Sum;
    }

    private String getJarFilename(Attributes attrs) {
        String attrsName = attrs.getValue(PLUGIN_JAR_FILENAME);
        if (attrsName != null) {
            return attrsName;
        }
        if (attrs.getValue(PLUGIN_JAR_URL) != null) {
            int from = remoteJarFileUrl.lastIndexOf("/");
            int to = remoteJarFileUrl.indexOf("?");
            if (from != -1) {
                if (to == -1) {
                    return remoteJarFileUrl.substring(from + 1);
                } else {
                    return remoteJarFileUrl.substring(from + 1, to);
                }
            }
        }
        String className = getClassName();
        return className.substring(className.lastIndexOf(".") + 1);
    }
}
