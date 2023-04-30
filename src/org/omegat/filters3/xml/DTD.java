/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Didier Briel
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

package org.omegat.filters3.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * Document type declaration in XML file. For example,
 * <code>&lt;!DOCTYPE book PUBLIC "-//OASIS//DTD DocBook XML V4.1.2//EN"
 * "http://www.oasis-open.org/docbook/xml/4.1.2/docbookx.dtd"&gt;</code>.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class DTD extends XMLPseudoTag {
    private String name;

    private String publicId;

    /** Returns Public ID of this DTD. */
    public String getPublicId() {
        return publicId;
    }

    private String systemId;

    /** Returns System ID of this DTD. */
    public String getSystemId() {
        return systemId;
    }

    private List<Entity> entities;

    /** Creates a new instance of Doctype */
    public DTD(String name, String publicId, String systemId) {
        this.name = name;
        this.publicId = publicId;
        this.systemId = systemId;
        entities = new ArrayList<Entity>();
    }

    public void addEntity(Entity entity) {
        entities.add(entity);
    }

    /**
     * Returns the DTD in its original form as it was in original document.
     */
    public String toOriginal() {
        StringBuilder res = new StringBuilder();
        res.append("<!DOCTYPE ").append(name);
        if (publicId != null) {
            res.append(" PUBLIC \"").append(publicId).append("\"");
        }
        if (systemId != null) {
            if (publicId == null) {
                res.append(" SYSTEM");
            }
            res.append(" \"").append(systemId).append("\"");
        }

        if (!entities.isEmpty()) {
            res.append("\n[\n");
            for (Entity entity : entities) {
                res.append(entity.toString()).append("\n");
            }
            res.append("]");
        }

        res.append(">\n");
        return res.toString();
    }

}
