/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2020 Briac Pilpre
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

import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class PluginInformation {
    private static final String PLUGIN_NAME = "Plugin-Name";
    private static final String PLUGIN_VERSION = "Plugin-Version";
    private static final String PLUGIN_AUTHOR = "Plugin-Author";
    private static final String PLUGIN_DESCRIPTION = "Plugin-Description";

    private static final String IMPLEMENTATION_VENDOR = "Implementation-Vendor";
    private static final String IMPLEMENTATION_TITLE = "Implementation-Title";
    private static final String IMPLEMENTATION_VERSION = "Implementation-Version";
    private static final String BUNDLE_VERSION = "Bundle-Version";
    private static final String BUNDLE_NAME = "Bundle-Name";
    private static final String BUILT_BY = "Built-By";

    private final String className;
    private final String name;
    private final String version;
    private final String author;
    private final String description;

    public PluginInformation(String className, Manifest manifest) {
        this.className = className;
        Attributes attrs = manifest.getMainAttributes();
        name = findName(manifest);
        version = findVersion(manifest);
        author = findAuthor(manifest);
        description = attrs.getValue(PLUGIN_DESCRIPTION);
    }

    public PluginInformation(String className, Properties props) {
        this.className = className;
        name = null;
        version = null;
        author = null;
        description = null;
    }

    private String findName(Manifest m) {
        Attributes attrs = m.getMainAttributes();
        if (attrs.getValue(PLUGIN_NAME) != null) {
            return attrs.getValue(PLUGIN_NAME);
        } else if (attrs.getValue(BUNDLE_NAME) != null) {
            return attrs.getValue(BUNDLE_NAME);
        } else if (attrs.getValue(IMPLEMENTATION_TITLE) != null) {
            return attrs.getValue(IMPLEMENTATION_TITLE);
        }
        return null;
    }

    private String findVersion(Manifest m) {
        Attributes attrs = m.getMainAttributes();
        if (attrs.getValue(PLUGIN_VERSION) != null) {
            return attrs.getValue(PLUGIN_VERSION);
        } else if (attrs.getValue(BUNDLE_VERSION) != null) {
            return attrs.getValue(BUNDLE_VERSION);
        } else if (attrs.getValue(IMPLEMENTATION_VERSION) != null) {
            return attrs.getValue(IMPLEMENTATION_VERSION);
        }
        return null;
    }

    private String findAuthor(Manifest m) {
        Attributes attrs = m.getMainAttributes();
        if (attrs.getValue(PLUGIN_AUTHOR) != null) {
            return attrs.getValue(PLUGIN_AUTHOR);
        } else if (attrs.getValue(IMPLEMENTATION_VENDOR) != null) {
            return attrs.getValue(IMPLEMENTATION_VENDOR);
        } else if (attrs.getValue(BUILT_BY) != null) {
            return attrs.getValue(BUILT_BY);
        }
        return null;
    }

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthor() {
        return author;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PluginInformation [className=").append(className).append(", name=").append(name)
                .append(", version=").append(version).append(", author=").append(author).append(", description=")
                .append(description).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((author == null) ? 0 : author.hashCode());
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PluginInformation other = (PluginInformation) obj;
        if (author == null) {
            if (other.author != null)
                return false;
        } else if (!author.equals(other.author))
            return false;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

}
