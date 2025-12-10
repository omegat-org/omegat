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

import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.jspecify.annotations.Nullable;

import java.net.URL;
import java.util.List;

public class TracWikiRpc {
    private final XmlRpcClient client;

    public TracWikiRpc(String endpoint, String username, String password) throws Exception {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(endpoint));
        config.setBasicUserName(username);
        config.setBasicPassword(password);

        client = new XmlRpcClient();
        client.setConfig(config);
    }

    public String getPage(String pageName) throws Exception {
        return (String) client.execute("wiki.getPage", new Object[]{pageName});
    }

    public List<String> getAllPages() throws Exception {
        Object result = client.execute("wiki.getAllPages", new Object[]{});
        if (result == null) {
            return java.util.Collections.emptyList();
        }
        if (result instanceof Object[]) {
            Object[] arr = (Object[]) result;
            java.util.ArrayList<String> list = new java.util.ArrayList<>(arr.length);
            for (Object o : arr) {
                list.add(o != null ? o.toString() : null);
            }
            return list;
        }
        if (result instanceof List) {
            // Some implementations may return a List
            @SuppressWarnings("unchecked")
            List<Object> objList = (List<Object>) result;
            java.util.ArrayList<String> list = new java.util.ArrayList<>(objList.size());
            for (Object o : objList) {
                list.add(o != null ? o.toString() : null);
            }
            return list;
        }
        // Fallback to single value
        return java.util.List.of(result.toString());
    }
}
