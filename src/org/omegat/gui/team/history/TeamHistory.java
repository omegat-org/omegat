package org.omegat.gui.team.history;

import gen.core.project.RepositoryDefinition;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.util.Language;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.File;
import java.util.List;

public class TeamHistory {
    private final Frame parent;

    public TeamHistory(Frame parent) {
        this.parent = parent;
    }

    public void show() {
        if (!Core.getProject().isProjectLoaded()) {
            return;
        }
        JDialog dialog = new JDialog(parent);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane();
        JTextPane textPane = new JTextPane();
        JButton dismissButton = new JButton();
        dialog.setTitle("Team History");
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setModal(true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().add(panel);
        //
        panel.add(scrollPane, BorderLayout.CENTER);
        scrollPane.setViewportView(textPane);
        panel.add(dismissButton, BorderLayout.SOUTH);
        dismissButton.setText("Dismiss");
        //
        loadChanges(textPane);
        //
        dismissButton.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
    }

    private static RepositoryDefinition getRootRepositoryMapping(List<RepositoryDefinition> repos) {
        RepositoryDefinition repositoryDefinition = null;
        for (RepositoryDefinition definition : repos) {
            if (definition.getMapping().get(0).getLocal().equals("/")
                    && definition.getMapping().get(0).getRepository().equals("/")) {
                repositoryDefinition = definition;
                break;
            }
        }
        return repositoryDefinition;
    }

    protected File getRepositoryDir(ProjectProperties props, RepositoryDefinition repo) {
        String path = repo.getUrl().replaceAll("[^A-Za-z0-9.]", "_").replaceAll("__+", "_");
        return props.getProjectRootDir().toPath().resolve(RemoteRepositoryProvider.REPO_SUBDIR).resolve(path)
                .resolve(".git").toFile();
    }

    private void loadChanges(JTextPane textPane) {
        try {
            ProjectProperties prop = Core.getProject().getProjectProperties();
            var repositories = prop.getRepositories();
            if (repositories.isEmpty()) {
                return;
            }
            RepositoryDefinition rootRepo = getRootRepositoryMapping(repositories);
            if (rootRepo == null) {
                return;
            }
            File gitRepoPath = getRepositoryDir(prop, rootRepo);
            TeamProjectHistory viewer = new TeamProjectHistory(gitRepoPath, new Language("en"), new Language("ja"));

            List<TmxCommitChange> changes = viewer.getLatestTmxChanges();

            StringBuilder sb = new StringBuilder();
            for (TmxCommitChange commitChange : changes) {
                sb.append("=".repeat(80));
                sb.append(commitChange);
                sb.append("-".repeat(50));

                for (TuChange tuChange : commitChange.getTuChanges()) {
                    sb.append(tuChange);

                    switch (tuChange.getChangeType()) {
                        case ADDED:
                            sb.append("    New translation:");
                            printTranslationUnit(sb, tuChange.getNewTu());
                            break;

                        case REMOVED:
                            sb.append("    Removed translation:");
                            printTranslationUnit(sb, tuChange.getOldTu());
                            break;
                    }
                    sb.append("\n");
                }
            }
            textPane.setText(sb.toString());
            viewer.close();

        } catch (Exception ignored) {
        }
    }

    private static void printTranslationUnit(StringBuilder sb, TranslationUnit tu) {
        if (tu != null) {
            sb.append("     ").append(tu.getSourceText()).append(":").append(tu.getTargetText()).append(" [")
                    .append(tu.getChanger()).append("]\n");
        }
    }
}
