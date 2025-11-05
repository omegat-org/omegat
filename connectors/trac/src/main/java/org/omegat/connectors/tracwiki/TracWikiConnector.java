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

package org.omegat.connectors.tracwiki;

import org.omegat.connectors.AbstractExternalServiceConnector;
import org.omegat.connectors.spi.ConnectorCapability;
import org.omegat.connectors.spi.ConnectorException;
import org.omegat.core.Core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TracWikiConnector extends AbstractExternalServiceConnector {

    public static void loadPlugins() {
        Core.registerCmsConnectorClass(TracWikiConnector.class);
    }

    public static void unloadPlugins() {
    }

    private static final Map<String, String> CONFIG = new HashMap<>();

    static {
        CONFIG.put("tracwiki", "https://trac.edgewall.org/wiki");
    }

    @Override
    public String getId() {
        return "trac";
    }

    @Override
    public String getName() {
        return "Trac connector";
    }

    @Override
    public Set<ConnectorCapability> getCapabilities() {
        return Set.of(ConnectorCapability.READ);
    }

    @Override
    public String getPreferenceName() {
        return "trac";
    }

    public InputStream fetchResource(String projectId, String resourceId) throws ConnectorException {
        String page = httpGet(CONFIG.get(projectId) + "/" + resourceId);
        return new ByteArrayInputStream(page.getBytes(StandardCharsets.UTF_8));
    }
}
