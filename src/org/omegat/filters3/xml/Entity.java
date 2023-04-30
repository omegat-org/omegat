/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2013 Alex Buloichik
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

/**
 * Entity declaration in XML file's Document type declaration. For example,
 * <code>&lt;!ENTITY gloss SYSTEM "gloss.xml"&gt;</code> is external entity and
 * <code>&lt;!ENTITY % dbnotn.module "INCLUDE"&gt;</code> is internal one.
 *
 * @author Maxym Mykhalchuk
 * @author Alex Buloichik
 */
public class Entity {
    enum Type {
        INTERNAL,
        EXTERNAL,
        REFERENCE
    }

    private Type entityType;

    /** Whether entity is internal. If false, it's an external entity. */
    public Type getType() {
        return entityType;
    }

    private boolean parameter;

    /** Whether entity is a parameter entity. */
    public boolean isParameter() {
        return parameter;
    }

    private String name, originalName;

    /** Returns entity's name. */
    public String getName() {
        return name;
    }

    /** Returns entity's original name, with '%' for parameter entities. */
    public String getOriginalName() {
        return originalName;
    }

    private String value;

    /** Returns entity's value. */
    public String getValue() {
        return value;
    }

    private String publicId;

    /** Returns entity's publicId. */
    public String getPublicId() {
        return publicId;
    }

    private String systemId;

    /** Returns entity's systemId. */
    public String getSystemId() {
        return systemId;
    }

    private void setName(String name) {
        originalName = name;
        if (name.codePointAt(0) == '%') {
            parameter = true;
            this.name = name.substring(name.offsetByCodePoints(0, 1));
        } else {
            parameter = false;
            this.name = name;
        }
    }

    /** Creates internal entity. */
    public Entity(String name, String value) {
        entityType = Type.INTERNAL;
        setName(name);
        this.value = value;
    }

    /** Creates external entity. */
    public Entity(String name, String publicId, String systemId) {
        entityType = Type.EXTERNAL;
        setName(name);
        this.publicId = publicId;
        this.systemId = systemId;
    }

    /** Creates entity reference. */
    public Entity(String name) {
        entityType = Type.REFERENCE;
        setName(name);
    }

    /**
     * Returns the entity as string.
     */
    public String toString() {
        StringBuilder res = new StringBuilder();
        if (entityType == Type.REFERENCE) {
            if (parameter) {
                res.append('%').append(name).append(';');
            } else {
                res.append('&').append(name).append(';');
            }
        } else {
            res.append("<!ENTITY");
            if (parameter) {
                res.append(" %");
            }
            res.append(' ');
            res.append(name);
            if (entityType == Type.INTERNAL) {
                // <!ENTITY % name "value">
                res.append(" \"").append(value).append('"');
            } else if (entityType == Type.EXTERNAL) {
                // <!ENTITY gloss SYSTEM "gloss.xml">
                if (publicId != null) {
                    res.append(" PUBLIC \"").append(publicId).append('"');
                }
                res.append(" SYSTEM \"").append(systemId).append('"');
            }
            res.append('>');
        }
        return res.toString();
    }

}
