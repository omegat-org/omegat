/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.cms.actions;

import org.omegat.cms.spi.CmsConnector;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class CmsRetrieval {

    public void retrieveResource(CmsConnector connector, String projectId, String resourceId, String targetDir) throws Exception {
        InputStream in = connector.fetchResource(projectId, resourceId);
        if (in != null) {
            Path dir = Paths.get(targetDir);
            String fileName = (resourceId == null || resourceId.isEmpty()) ? "cms-resource.txt" : resourceId;
            Path out = dir.resolve(fileName);
            Files.createDirectories(dir);
            try (in) {
                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    public void retrieveResourceFromUrl(CmsConnector connector, String url, String targetDir) throws Exception {
        InputStream in = connector.fetchResource(url);
        if (in != null) {
            Path dir = Paths.get(targetDir);
            String fileName = extractFileNameFromUrl(url);
            Path out = dir.resolve(fileName);
            Files.createDirectories(dir);
            try (in) {
                Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private String extractFileNameFromUrl(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        if (!fileName.contains(".")) {
            fileName = "cms-resource.txt";
        }
        return fileName;
    }
}