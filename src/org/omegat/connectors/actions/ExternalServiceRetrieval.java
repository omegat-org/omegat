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

package org.omegat.connectors.actions;

import org.omegat.connectors.dto.ServiceTarget;
import org.omegat.connectors.spi.ConnectorException;
import org.omegat.connectors.spi.IExternalServiceConnector;
import org.omegat.core.Core;
import org.omegat.util.Log;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ExternalServiceRetrieval {

    public boolean retrieveResource(IExternalServiceConnector connector, ServiceTarget target, String resourceId, String targetDir) throws Exception {
        Path dir = Paths.get(targetDir);
        String ext = "." + connector.getFileExtension();
        String fileName = (resourceId.isEmpty() ? "external-service-resource" : resourceId) + ext;
        Path out = dir.resolve(fileName);
        Files.createDirectories(dir);
        try (InputStream in = connector.fetchResource(target, resourceId)) {
            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
        } catch (ConnectorException ex) {
            showErrorMessage(ex, resourceId);
            return false;
        }
        return true;
    }

    public boolean retrieveResourceFromUrl(IExternalServiceConnector connector, String url, String targetDir) throws Exception {
        Path dir = Paths.get(targetDir);
        String fileName = extractFileNameFromUrl(connector, url);
        Path out = dir.resolve(fileName);
        Files.createDirectories(dir);
        try (InputStream in = connector.fetchResource(url)) {
            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
        } catch (ConnectorException ex) {
            showErrorMessage(ex, url);
            return false;
        }
        return true;
    }

    private void showErrorMessage(ConnectorException ex, String url) {
        if (ex.getStatus() >= 300) {
            Log.logErrorRB("TF_EXTERNAL_SERVICE_IMPORT_FAILED_HTTP_CODE", url, ex.getStatus());
            Core.getMainWindow().displayErrorRB(ex, "TF_EXTERNAL_SERVICE_IMPORT_FAILED_HTTP_CODE", url,
                    ex.getStatus());
        } else {
            Log.logErrorRB(ex, "TF_EXTERNAL_SERVICE_IMPORT_FAILED");
            Core.getMainWindow().displayErrorRB(ex, "TF_EXTERNAL_SERVICE_IMPORT_FAILED");
        }
    }

    private String extractFileNameFromUrl(IExternalServiceConnector connector, String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        if (!fileName.contains(".")) {
            fileName = "external-service-resource." + connector.getFileExtension();
        }
        return fileName;
    }
}
