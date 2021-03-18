/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2020 Briac Pilpre
               2021 Hiroshi Miura
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

import org.omegat.filters2.master.PluginUtils;

import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class PluginInformation implements Comparable<PluginInformation> {
    private static final String PLUGIN_NAME = "Plugin-Name";
    private static final String PLUGIN_VERSION = "Plugin-Version";
    private static final String PLUGIN_AUTHOR = "Plugin-Author";
    private static final String PLUGIN_DESCRIPTION = "Plugin-Description";
    private static final String PLUGIN_CATEGORY = "Plugin-Category";
    private static final String PLUGIN_LINK = "Plugin-Link";

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
    private final String category;
    private final String link;
    private final String builtBy;
    private boolean installed;

    public PluginInformation(String className, Manifest manifest, boolean installed) {
        this.className = className;
        Attributes attrs = manifest.getMainAttributes();
        name = findName(manifest);
        version = findVersion(manifest);
        author = findAuthor(manifest);
        description = attrs.getValue(PLUGIN_DESCRIPTION);
        link = attrs.getValue(PLUGIN_LINK);
        builtBy = attrs.getValue(BUILT_BY);
        category = categoryName(attrs.getValue(PLUGIN_CATEGORY));
        this.installed = installed;
    }

    public PluginInformation(String className, Properties props, final String key, boolean installed) {
        this.className = className;
        name = className.substring(className.lastIndexOf(".") + 1);
        version = null;
        author = null;
        description = null;
        category = categoryName(key);
        link = null;
        builtBy = null;
        this.installed = installed;
    }

    private String categoryName(final String key) {
        Optional<PluginUtils.PluginType> type = Arrays.stream(PluginUtils.PluginType.values()).filter(v ->
                v.getTypeValue().equals(key)).findFirst();
        if (type.isPresent()) {
            return type.get().getTypeValue();
        }
        return PluginUtils.PluginType.UNKNOWN.getTypeValue();
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
        // fallback to className
        return className.substring(className.lastIndexOf(".") + 1);
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

    public final String getClassName() {
        return className;
    }

    public final String getName() {
        return name;
    }

    public final String getVersion() {
        return version;
    }

    public final String getDescription() {
        return description;
    }

    public final String getAuthor() {
        return author;
    }

    public final String getCategory() {
        return category;
    }

    public final String getLink() {
        return link;
    }

    public final String getBuiltBy() {
        return builtBy;
    }

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PluginInformation [className=").append(className).append(", name=").append(name)
                .append(", version=").append(version).append(", author=").append(author).append(", description=")
                .append(description).append("]");
        return builder.toString();
    }

    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((author == null) ? 0 : author.hashCode());
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        PluginInformation other = (PluginInformation) obj;
        if (author == null) {
            if (other.author != null) {
                return false;
            }
        } else if (!author.equals(other.author)) {
            return false;
        }
        if (className == null) {
            if (other.className != null) {
                return false;
            }
        } else if (!className.equals(other.className)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (version == null) {
            return other.version == null;
        } else {
            return version.equals(other.version);
        }
    }

    @Override
    public final int compareTo(PluginInformation pluginInformation) {
        int score;
        if (this == pluginInformation || className.equals(pluginInformation.getClass().getName())) {
            return version.compareTo(pluginInformation.getVersion());
        }
        if (pluginInformation.category != null) {
            if (category == null) {
                if ("bundle".equals(pluginInformation.category)) {
                    return 1;
                } else {
                    return -1;
                }
            } else {
                score = category.compareTo(pluginInformation.getCategory());
                if (score != 0) {
                    return score;
                }
            }
        }
        if (pluginInformation.getAuthor() != null) {
            if (author == null) {
                return -1;
            } else {
                score = author.compareTo(pluginInformation.getAuthor());
                if (score != 0) {
                    return score;
                }
            }
        }
        score = className.compareTo(pluginInformation.getClassName());
        if (score !=0) {
            return score;
        }
        if (pluginInformation.getName() != null) {
            if (name == null) {
                return -1;
            }
            score = name.compareTo(pluginInformation.getName());
            if (score != 0) {
                return score;
            }
        }
        if (pluginInformation.getVersion() != null) {
            if (version == null) {
                return -1;
            }
            return version.compareTo(pluginInformation.getVersion());
        }
        return 0;
    }
}
