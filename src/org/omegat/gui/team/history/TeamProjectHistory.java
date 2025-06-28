package org.omegat.gui.team.history;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.omegat.util.Language;
import org.omegat.util.StringUtil;
import org.omegat.util.TMXReader2;

import javax.xml.bind.JAXBException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TeamProjectHistory {

    private static final String TMX_FILE_PATH = "omegat/project_save.tmx";

    private final Repository repository;
    private final Git git;

    private final Language sourceLang;
    private final Language targetLang;

    public TeamProjectHistory(File gitRepoPath, Language sourceLang, Language targetlang) throws IOException, JAXBException {
        this.sourceLang = sourceLang;
        this.targetLang = targetlang;
        this.repository = new FileRepositoryBuilder()
                .setGitDir(gitRepoPath)
                .readEnvironment()
                .findGitDir()
                .build();
        this.git = new Git(repository);
    }

    public List<TmxCommitChange> getLatestTmxChanges() throws Exception {
        List<TmxCommitChange> changes = new ArrayList<>();

        // Get latest commit that affected the TMX file
        Iterable<RevCommit> commits = git.log()
                .addPath(TMX_FILE_PATH)
                .setMaxCount(5)
                .call();

        List<RevCommit> commitList = new ArrayList<>();
        commits.forEach(commitList::add);

        for (RevCommit currentCommit : commitList) {
            if (currentCommit.getParentCount() == 0) {
                continue;
            }

            RevCommit parentCommit = currentCommit.getParent(0);

            TmxCommitChange change = new TmxCommitChange(
                    currentCommit.getId().name(),
                    currentCommit.getShortMessage(),
                    currentCommit.getAuthorIdent().getName(),
                    new Date(currentCommit.getCommitTime() * 1000L)
            );

            // Get TMX content from both commits
            String oldTmxContent = getTmxContent(parentCommit);
            String newTmxContent = getTmxContent(currentCommit);

            if (oldTmxContent != null || newTmxContent != null) {
                List<TuChange> tuChanges = analyzeTuChanges(oldTmxContent, newTmxContent);
                change.setTuChanges(tuChanges);
                changes.add(change);
            }
        }

        return changes;
    }

    private String getTmxContent(RevCommit commit) throws IOException {
        try (RevWalk walk = new RevWalk(repository)) {
            var treeWalk = org.eclipse.jgit.treewalk.TreeWalk.forPath(
                    repository, TMX_FILE_PATH, walk.parseTree(commit.getTree())
            );

            if (treeWalk == null) {
                return null;
            }

            ObjectLoader loader = repository.open(treeWalk.getObjectId(0));
            return new String(loader.getBytes(), StandardCharsets.UTF_8);
        }
    }

    private List<TuChange> analyzeTuChanges(String oldTmxContent, String newTmxContent) {
        List<TuChange> changes = new ArrayList<>();

        try {
            Set<TranslationUnit> oldTus = parseTmxToTuSet(oldTmxContent);
            Set<TranslationUnit> newTus = parseTmxToTuSet(newTmxContent);

            // Find added TUs
            for (TranslationUnit tu : newTus) {
                if (!oldTus.contains(tu)) {
                    changes.add(new TuChange(TuChangeType.ADDED, null, tu));
                }
            }

            // Find removed TUs
            for (TranslationUnit tu : oldTus) {
                if (!newTus.contains(tu)) {
                    changes.add(new TuChange(TuChangeType.REMOVED, tu, null));
                }
            }

        } catch (Exception e) {
            System.err.println("Error analyzing TU changes: " + e.getMessage());
        }

        return changes;
    }

    private Set<TranslationUnit> parseTmxToTuSet(String tmxContent) throws Exception {
        Set<TranslationUnit> tuSet = new HashSet<>();

        if (tmxContent == null || tmxContent.trim().isEmpty()) {
            return tuSet;
        }

        try (InputStream is = new ByteArrayInputStream(tmxContent.getBytes(StandardCharsets.UTF_8))) {
            new TMXReader2().readTMX(is, sourceLang, targetLang, new Loader(tuSet));
        }
        return tuSet;
    }


    static class Loader implements TMXReader2.LoadCallback {
        private final Set<TranslationUnit> tuSet;

        public Loader(Set<TranslationUnit> tuSet) {
            this.tuSet = tuSet;
        }

        @Override
        public boolean onEntry(TMXReader2.ParsedTu tu, TMXReader2.ParsedTuv tuvSource, TMXReader2.ParsedTuv tuvTarget,
                               boolean isParagraphSegtype) {
            Map<String, String> segments = new HashMap<>();
            if (tuvSource == null) {
                // source Tuv not found
                return false;
            }
            String changer = null;
            long changed = 0;
            String sourceText = null;
            String translation = null;

            if (tuvTarget != null) {
                changer = StringUtil.nvl(tuvTarget.changeid, tuvTarget.creationid, tu.changeid,
                        tu.creationid);
                changed = StringUtil.nvlLong(tuvTarget.changedate, tuvTarget.creationdate, tu.changedate,
                        tu.creationdate);
                sourceText = tuvSource.text;
                translation = tuvTarget.text;
            }

            tuSet.add(new TranslationUnit(changer, changed, sourceText, translation));

            return true;
        }
    }

    public void close() {
        if (git != null) {
            git.close();
        }
        if (repository != null) {
            repository.close();
        }
    }

}
