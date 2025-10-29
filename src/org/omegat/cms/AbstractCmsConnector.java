package org.omegat.cms;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.omegat.core.Core;
import org.omegat.cms.dto.CmsProject;
import org.omegat.cms.dto.CmsResource;
import org.omegat.cms.spi.CmsConnector;
import org.omegat.cms.spi.CmsException;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.WikiGet;

import javax.swing.JOptionPane;

/**
 * Base class for CMS connectors with common helpers and defaults.
 */
public abstract class AbstractCmsConnector implements CmsConnector {

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public abstract String getPreferenceName();

    @Override
    public List<CmsProject> listProjects() throws CmsException {
        return Collections.emptyList();
    }

    @Override
    public List<CmsResource> listResources(String projectId) throws CmsException {
        return Collections.emptyList();
    }

    @Override
    public InputStream fetchResource(String projectId, String resourceId) throws CmsException, IOException {
        throw new CmsException("Fetch not implemented");
    }

    @Override
    public void pushTranslation(String projectId, String resourceId, InputStream translated) throws CmsException {
        throw new CmsException("Push not supported");
    }

    /** HTTP helpers, delegating to HttpConnectionUtils */
    protected String httpGet(String url) throws CmsException {
        try {
            return HttpConnectionUtils.getURL(new URL(url));
        } catch (IOException e) {
            throw new CmsException("GET failed: " + url, e);
        }
    }

    protected @Nullable String getCustomRemoteUrl() {
        String remoteUrl = JOptionPane.showInputDialog(Core.getMainWindow().getApplicationFrame(),
                OStrings.getString("TF_WIKI_IMPORT_PROMPT"), OStrings.getString("TF_WIKI_IMPORT_TITLE"),
                JOptionPane.WARNING_MESSAGE);
        if (remoteUrl == null || remoteUrl.trim().isEmpty()) {
            // [1762625] Only try to get MediaWiki page if a string has been
            // entered
            return null;
        }
        return remoteUrl;
    }

    protected void doWikiImport(String remoteUrl) {
        String projectsource = Core.getProject().getProjectProperties().getSourceRoot();
        try {
            if (!HttpConnectionUtils.checkUrl(remoteUrl)) {
                JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                        OStrings.getString("TF_WIKI_IMPORT_URL_ERROR"),
                        OStrings.getString("TF_WIKI_IMPORT_URL_ERROR_TITLE"), JOptionPane.WARNING_MESSAGE);
                return;
            }
            WikiGet.doWikiGet(remoteUrl, projectsource);
            ProjectUICommands.projectReload();
        } catch (Exception ex) {
            Log.log(ex);
            Core.getMainWindow().displayErrorRB(ex, "TF_WIKI_IMPORT_FAILED");
        }
    }

}
