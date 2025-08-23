/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2020 Briac Pilpre
               2021-2025 Hiroshi Miura
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
public final class PluginInformation {

    public enum Status {
        INSTALLED("installed"),
        BUNDLED("bundled"),
        NEW("new"),
        UPDATABLE("updatable"),
        UNINSTALLED("uninstalled");

        private final String value;

        Status(String value) {
            this.value = value;
        }

        public String getLocalizedValue() {
            switch (this) {
            case UNINSTALLED:
                return OStrings.getString("PLUGIN_STATUS_UNINSTALLED");
            case UPDATABLE:
                return OStrings.getString("PLUGIN_STATUS_UPDATABLE");
            case BUNDLED:
                return OStrings.getString("PLUGIN_STATUS_BUNDLED");
            case NEW:
                return OStrings.getString("PLUGIN_STATUS_NEW");
            case INSTALLED:
                return OStrings.getString("PLUGIN_STATUS_INSTALLED");
            default:
                return "Unknown";
            }

        }
    }

    private String className;
    private String name;
    private String version;
    private String author;
    private String description;
    private PluginUtils.PluginType category;
    private String link;
    private URL url;
    private Status status;

    /* The class is recommended to build from builder. */
    private PluginInformation() {
    }

    private PluginInformation(PluginInformation info, Status status) {
        this.className = info.getClassName();
        this.name = info.getName();
        this.version = info.getVersion();
        this.author = info.getAuthor();
        this.description = info.getDescription();
        this.category = info.getCategory();
        this.link = info.getLink();
        this.url = info.getUrl();
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
    public PluginUtils.PluginType getCategory() {
        return category;
    }

    /**
     * @return link URL of plugin homepage
     */
    public String getLink() {
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
    public boolean isBundled() {
        return status == Status.BUNDLED;
    }

    public Status getStatus() {
        return status;
    }

    /**
     * @return string expression of PluginInformation class.
     */
    @Override
    public String toString() {
        return "PluginInformation [className=" + className + ", name=" + name +
                ", version=" + version + ", author=" + author +
                ", description=" + description + "]";
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
        private static final String PLUGIN_BUNDLED = "Plugin-Bundled";

        /**
         * Disable default constructor.
         */
        private Builder() {
        }

        public static PluginInformation copy(final PluginInformation info, final Status status) {
            return new PluginInformation(info, status);
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
         * @param defaultStatus
         *            Plugin status, bundled or installed
         * @return PluginInformation object.
         */
        public static PluginInformation fromManifest(final String className, final Manifest manifest,
                final URL mu, final Status defaultStatus) {
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
            // bundled modules detection
            Status status;
            if ("true".equalsIgnoreCase(lookupAttribute(targetAttrs, PLUGIN_BUNDLED))) {
                status = Status.BUNDLED;
            } else {
                status = defaultStatus;
            }
            // create the object.
            PluginInformation result = new PluginInformation();
            result.className = className;
            result.name = findName(className, targetAttrs);
            result.version = findVersion(targetAttrs);
            result.author = findAuthor(targetAttrs);
            result.description = lookupAttribute(targetAttrs, PLUGIN_DESCRIPTION);
            result.category = findCategory(targetAttrs);
            result.link = lookupAttribute(targetAttrs, PLUGIN_LINK);
            result.url = mu;
            result.status = status;
            return result;
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
            PluginInformation result = new PluginInformation();
            result.className = className;
            result.name = key;
            result.version = OStrings.getSimpleVersion();
            result.author = AUTHOR;
            result.description = props.getProperty(String.format("plugin.desc.%s", key));
            result.category = PluginUtils.PluginType.getTypeByValue(key);
            result.link = LINK;
            result.url = mu;
            result.status = status;
            return result;
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
            return findName(className);
        }

        private static String findName(String className) {
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
