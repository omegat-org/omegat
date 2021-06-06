package org.omegat.core.data;

import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class RemotePluginInformation extends PluginInformation {
    private final String PLUGIN_JAR_URL = "Plugin-Download-Url";
    private final String PLUGIN_JAR_FILENAME = "Plugin-Jar-Filename";
    private final String PLUGIN_SHA256SUM = "Plugin-Sha256Sum";

    private final String remoteJarFileUrl;
    private final String jarFilename;
    private final String sha256Sum;

    public RemotePluginInformation(String className, Manifest manifest) {
        super(className, manifest, null, PluginInformation.STATUS.UNINSTALLED);
        Attributes attrs = manifest.getMainAttributes();
        remoteJarFileUrl = attrs.getValue(PLUGIN_JAR_URL);
        jarFilename = attrs.getValue(PLUGIN_JAR_FILENAME);
        sha256Sum = attrs.getValue(PLUGIN_SHA256SUM);
    }

    public String getRemoteJarFileUrl() {
        return remoteJarFileUrl;
    }

    public String getJarFilename() {
        return jarFilename;
    }

    public String getSha256Sum() {
        return sha256Sum;
    }
}
