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

package org.omegat.connectors;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.omegat.connectors.spi.IExternalServiceConnector;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Log;

/**
 * Registry/manager for connectors.
 */
public class ExternalConnectorsManager {
    private final Map<String, IExternalServiceConnector> connectorMap = new LinkedHashMap<>();

    public ExternalConnectorsManager() {
        for (Class<?> clazz : PluginUtils.getExternalServiceConnectorClasses()) {
            try {
                register((IExternalServiceConnector) clazz.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException e) {
                Log.log(e);
            }
        }
    }

    private synchronized void register(@Nullable IExternalServiceConnector connector) {
        if (connector == null) {
            return;
        }
        connectorMap.put(connector.getId(), connector);
    }

    public synchronized IExternalServiceConnector get(String id) {
        return connectorMap.get(id);
    }

    public synchronized List<IExternalServiceConnector> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(connectorMap.values()));
    }
}
