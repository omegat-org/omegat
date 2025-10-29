package org.omegat.cms.gui;

import org.omegat.cms.spi.CmsConnector;
import org.omegat.core.Core;
import org.omegat.gui.main.ProjectUICommands;
import org.omegat.util.Log;

import javax.swing.JDialog;
import java.awt.Frame;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class CmsPanelController {

    public void show() {
        Frame owner = Core.getMainWindow().getApplicationFrame();
        CmsPanel panel = new CmsPanel();
        JDialog dialog = new JDialog(owner, "External CMS import", true);
        dialog.getContentPane().add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);

        panel.getLaunchButton().addActionListener(e -> {
            String url = panel.getCustomUrl();
            try {
                if (url != null && !url.trim().isEmpty()) {
                    String srcRoot = Core.getProject().getProjectProperties().getSourceRoot();
                    org.omegat.util.WikiGet.doWikiGet(url.trim(), srcRoot);
                    ProjectUICommands.projectReload();
                } else {
                    CmsConnector connector = panel.getSelectedConnector();
                    if (connector == null) {
                        return;
                    }
                    String projectId = panel.getProjectId();
                    String resourceId = panel.getResourceId();
                    java.io.InputStream in = connector.fetchResource(projectId, resourceId);
                    if (in != null) {
                        Path dir = java.nio.file.Paths.get(Core.getProject().getProjectProperties().getSourceRoot());
                        String fileName = (resourceId == null || resourceId.isEmpty()) ? "cms-resource.txt" : resourceId;
                        Path out = dir.resolve(fileName);
                        Files.createDirectories(dir);
                        try (in) {
                            Files.copy(in, out, StandardCopyOption.REPLACE_EXISTING);
                        }
                        ProjectUICommands.projectReload();
                    }
                }
            } catch (Exception ex) {
                Log.log(ex);
                Core.getMainWindow().displayErrorRB(ex, "TF_WIKI_IMPORT_FAILED");
            } finally {
                dialog.dispose();
            }
        });

        dialog.setVisible(true);
    }
}
