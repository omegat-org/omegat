/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
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
        StringBuffer res = new StringBuffer();
        res.append("<!DOCTYPE");
        res.append(" ");
        res.append(name);
        if (publicId!=null) {
            res.append(" ");
            res.append("PUBLIC");
            res.append(" ");
            res.append("\"" + publicId + "\"");
        }
        if (systemId!=null) {
            res.append(" ");
            res.append("\"" + systemId + "\"");
        }

        if (entities.size() > 0) {
            res.append("\n[\n");
            for (Entity entity : entities) {
                res.append(entity.toString());
                res.append("\n");
            }
            res.append("]");
        }

        res.append(">");
        res.append("\n");
        return res.toString();
    }

}
