/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2020 Briac Pilpre
               2021,2022 Hiroshi Miura
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

package org.omegat.core.data;

import java.net.URL;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.OStrings;

/**
 * Plugin information POJO data class.
 * 
 * @author Briac Pilpre
 * @author Hiroshi Miura
 */
public class PluginInformation {

    public enum Status {
        INSTALLED, BUNDLED, NEW,
    }

    private final String className;
    private final String name;
    private final String version;
    private final String author;
    private final String description;
    private final PluginUtils.PluginType category;
    private final String link;
    private final URL url;
    private final Status status;

    /* The class is recommend to build from builder. */
    private PluginInformation(String className, String name, String version, String author,
            String description, PluginUtils.PluginType category, String link, URL url, Status status) {
        this.className = className;
        this.name = name;
        this.version = version;
        this.author = author;
        this.description = description;
        this.category = category;
        this.link = link;
        this.url = url;
        this.status = status;
    }

    /**
     * @return className of plugin entry point
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return name of plugin
     */
    public String getName() {
        return name;
    }

    /**
     * @return version of plugin
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return description of plugin features
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return author(s) of plugin
     */
    public String getAuthor() {
        return author;
    }

    /**
     * @return category type of plugin as PluginType enum
     */
    public final PluginUtils.PluginType getCategory() {
        return category;
    }

    /**
     * @return link URL of plugin homepage
     */
    public final String getLink() {
        return link;
    }

    /**
     * @return manifest URL of plugin jar
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @return true if plugin is bundled with OmegaT distribution, otherwise
     *         false when 3rd party plugin
     */
    public final boolean isBundled() {
        return status == Status.BUNDLED;
    }

    /**
     * @return string expression of PluginInformation class.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PluginInformation [className=").append(className).append(", name=").append(name)
                .append(", version=").append(version).append(", author=").append(author)
                .append(", description=").append(description).append("]");
        return builder.toString();
    }

    /**
     * It is identical if status is differed.
     * 
     * @return hashCode of plugin
     */
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

    /**
     * Builder class.
     */
    public static final class Builder {
        private static final String PLUGIN_NAME = "Plugin-Name";
        private static final String PLUGIN_VERSION = "Plugin-Version";
        private static final String PLUGIN_AUTHOR = "Plugin-Author";
        private static final String PLUGIN_DESCRIPTION = "Plugin-Description";
        private static final String PLUGIN_CATEGORY = "Plugin-Category";
        private static final String PLUGIN_LINK = "Plugin-Link";
        private static final String PLUGIN_TYPE = "OmegaT-Plugin";
        private static final String IMPLEMENTATION_VENDOR = "Implementation-Vendor";
        private static final String IMPLEMENTATION_TITLE = "Implementation-Title";
        private static final String IMPLEMENTATION_VERSION = "Implementation-Version";
        private static final String BUNDLE_VERSION = "Bundle-Version";
        private static final String BUNDLE_NAME = "Bundle-Name";
        private static final String BUILT_BY = "Built-By";

        /**
         * Disable default constructor.
         */
        private Builder() {
        }

        /**
         * Build PluginInformation from Manifest attributes.
         * 
         * @param className
         *            Plugin class name.
         * @param manifest
         *            metadata of plugin.
         * @param mu
         *            URL of manifest
         * @param status
         *            Plugin status, bundled or installed
         * @return PluginInformation object.
         */
        public static PluginInformation fromManifest(final String className, final Manifest manifest,
                final URL mu, final Status status) {
            Attributes targetAttrs = new Attributes(manifest.getMainAttributes());
            String packageName = className == null ? ""
                    : className.substring(0, className.lastIndexOf(".") + 1).replace(".", "/");

            int i = 0;
            while (i < packageName.length()) {
                i = packageName.indexOf("/", i) + 1;
                Attributes attrs = manifest.getEntries().get(packageName.substring(0, i));
                if (attrs != null) {
                    targetAttrs.putAll(attrs);
                }
            }
            // Specific class section
            Attributes attrs = manifest.getEntries().get(className);
            if (attrs != null) {
                targetAttrs.putAll(attrs);
            }
            return new PluginInformation(className, findName(className, targetAttrs),
                    findVersion(targetAttrs), findAuthor(targetAttrs),
                    lookupAttribute(targetAttrs, PLUGIN_DESCRIPTION), findCategory(targetAttrs),
                    lookupAttribute(targetAttrs, PLUGIN_LINK), mu, status);
        }

        private static final String AUTHOR = "OmegaT team";
        private static final String LINK = "https://www.omegat.org";

        /**
         * Build PluginInformation from properties.
         * <p>
         * This builder is useful when OmegaT run from Gradle build system.
         * </p>
         * 
         * @param className
         *            Plugin class name.
         * @param props
         *            plugin properties bundled with OmegaT.
         * @param key
         *            plugin type string.
         * @param mu
         *            URL of property
         * @param status
         *            Plugin status, bundled or installed
         * @return PluginInformation object.
         */
        public static PluginInformation fromProperties(String className, Properties props, final String key,
                final URL mu, final Status status) {
            return new PluginInformation(className, key, OStrings.getSimpleVersion(), AUTHOR,
                    props.getProperty(String.format("plugin.desc.%s", key)),
                    PluginUtils.PluginType.getTypeByValue(key), LINK, mu, status);
        }

        private static PluginUtils.PluginType findCategory(Attributes attrs) {
            String categoryKey;
            categoryKey = lookupAttribute(attrs, PLUGIN_CATEGORY, PLUGIN_TYPE);
            if (categoryKey != null) {
                return PluginUtils.PluginType.getTypeByValue(categoryKey);
            }
            return PluginUtils.PluginType.MISCELLANEOUS;
        }

        private static String findName(String className, Attributes attrs) {
            String name = lookupAttribute(attrs, PLUGIN_NAME, BUNDLE_NAME, IMPLEMENTATION_TITLE);
            if (name != null) {
                return name;
            }

            return className == null ? "" : className.substring(className.lastIndexOf(".") + 1);
        }

        private static String findVersion(Attributes attrs) {
            String version = lookupAttribute(attrs, PLUGIN_VERSION, BUNDLE_VERSION, IMPLEMENTATION_VERSION);
            if (version != null) {
                return version;
            }
            return "";
        }

        private static String findAuthor(Attributes attrs) {
            String author = lookupAttribute(attrs, PLUGIN_AUTHOR, IMPLEMENTATION_VENDOR, BUILT_BY);
            if (author != null) {
                return author;
            }
            if ("org.omegat.Main".equals(attrs.getValue("Main-Class"))) {
                return "OmegaT team";
            }
            return "Unknown";
        }

        private static String lookupAttribute(Attributes attrs, String... candidates) {
            for (String key : candidates) {
                if (attrs.getValue(key) != null) {
                    return attrs.getValue(key);
                }
            }
            return null;
        }
    }
}
