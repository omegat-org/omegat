package org.omegat.cms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.omegat.cms.spi.CmsConnector;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.Log;

/**
 * Registry/manager for CMS connectors.
 */
public class CmsConnectors {
    private final Map<String, CmsConnector> connectorMap = new LinkedHashMap<>();

    public CmsConnectors() {
        for (Class<? extends CmsConnector> clazz : PluginUtils.getCMSConnectorClasses() ) {
            try {
                register(clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                Log.log(e);
            }
        }
    }

    private synchronized void register(CmsConnector connector) {
        if (connector == null) return;
        connectorMap.put(connector.getId(), connector);
    }

    public synchronized CmsConnector get(String id) {
        return connectorMap.get(id);
    }

    public synchronized List<CmsConnector> getAll() {
        return Collections.unmodifiableList(new ArrayList<>(connectorMap.values()));
    }
}
