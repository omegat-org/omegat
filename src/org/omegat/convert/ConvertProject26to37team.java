/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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

package org.omegat.convert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.omegat.core.Core;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.team2.ProjectTeamSettings;
import org.omegat.core.team2.RebaseAndCommit;
import org.omegat.core.team2.RemoteRepositoryProvider;
import org.omegat.util.OStrings;
import org.omegat.util.ProjectFileStorage;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc2.SvnGetInfo;
import org.tmatesoft.svn.core.wc2.SvnInfo;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Check if project is 2.6-style team project, i.e. 'inplace' repository working copy exit. Then convert it
 * into 3.6-style team project.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class ConvertProject26to37team {

    private ConvertProject26to37team() {
    }

    public static void checkTeam(File projectRootFolder) throws Exception {
        if (isSVNDirectory(projectRootFolder) || isGITDirectory(projectRootFolder)) {
            // project is 2.6-style team project
            if (isConsoleMode()) {
                Core.getMainWindow().displayWarningRB("TEAM_26_TO_37_CONSOLE");
                return;
            }

            // ask for convert
            int res = JOptionPane.showConfirmDialog(Core.getMainWindow().getApplicationFrame(),
                    OStrings.getString("TEAM_26_to_37_CONFIRM_MESSAGE"),
                    OStrings.getString("TEAM_26_to_37_CONFIRM_TITLE"), JOptionPane.OK_CANCEL_OPTION);
            if (res != JOptionPane.OK_OPTION) {
                return;
            }

            // convert
            convert(projectRootFolder);
            JOptionPane.showMessageDialog(Core.getMainWindow().getApplicationFrame(),
                    OStrings.getString("TEAM_26_to_37_CONVERTED_MESSAGE"),
                    OStrings.getString("TEAM_26_to_37_CONFIRM_TITLE"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void convert(File projectRootFolder) throws Exception {
        ProjectProperties props = ProjectFileStorage.loadProjectProperties(projectRootFolder);

        String version;
        String url;
        RepositoryDefinition def = new RepositoryDefinition();
        if (isSVNDirectory(projectRootFolder)) {
            url = getSVNUrl(projectRootFolder);
            def.setType("svn");
            version = getSVNTmxVersion(projectRootFolder);
        } else {
            url = getGITUrl(projectRootFolder);
            def.setType("git");
            version = getGITTmxVersion(projectRootFolder);
        }
        if (url == null) {
            throw new Exception("Repository URL not defined");
        }
        def.setUrl(url);
        saveVersion(projectRootFolder, "omegat/project_save.tmx", version);

        // map full project
        RepositoryMapping map = new RepositoryMapping();
        map.setLocal("");
        map.setRepository("");
        def.getMapping().add(map);
        props.setRepositories(new ArrayList<RepositoryDefinition>());
        props.getRepositories().add(def);

        ProjectFileStorage.writeProjectFile(props);

        // all data saved - remove old repository
        FileUtils.deleteDirectory(new File(projectRootFolder, ".svn"));
        FileUtils.deleteDirectory(new File(projectRootFolder, ".git"));
    }

    /**
     * Save version of project_save.tmx to .repositories/versions.properties.
     */
    private static void saveVersion(File projectRootFolder, String file, String version) throws IOException {
        ProjectTeamSettings teamSettings = new ProjectTeamSettings(new File(projectRootFolder,
                RemoteRepositoryProvider.REPO_SUBDIR));
        teamSettings.set(RebaseAndCommit.VERSION_PREFIX + file, version);
    }

    /**
     * Check if project contains 2.6-style SVN directory.
     */
    private static boolean isSVNDirectory(File projectRootFolder) {
        File svnDir = new File(projectRootFolder, ".svn");
        return svnDir.exists() && svnDir.isDirectory();
    }

    /**
     * Check if project contains 2.6-style GIT directory.
     */
    private static boolean isGITDirectory(File projectRootFolder) {
        File gitDir = new File(projectRootFolder, ".git");
        return gitDir.exists() && gitDir.isDirectory();
    }

    /**
     * Get repository URL for SVN.
     */
    private static String getSVNUrl(File wc) throws Exception {
        final SvnOperationFactory of = new SvnOperationFactory();
        final SvnGetInfo infoOp = of.createGetInfo();
        infoOp.setSingleTarget(SvnTarget.fromFile(wc));
        infoOp.setDepth(SVNDepth.EMPTY);
        infoOp.setRevision(SVNRevision.WORKING);
        final SvnInfo info = infoOp.run();
        SVNURL svn = info.getUrl();
        return svn.toString();
    }

    /**
     * Get repository URL for SVN.
     */
    private static String getGITUrl(File wc) throws Exception {
        Repository repository = Git.open(wc).getRepository();
        StoredConfig config = repository.getConfig();
        return config.getString("remote", "origin", "url");
    }

    /**
     * Get version of "omegat/project_save.tmx" in SVN.
     */
    private static String getSVNTmxVersion(File wc) throws Exception {
        final SvnOperationFactory of = new SvnOperationFactory();
        final SvnGetInfo infoOp = of.createGetInfo();
        infoOp.setSingleTarget(SvnTarget.fromFile(new File(wc, "omegat/project_save.tmx")));
        infoOp.setDepth(SVNDepth.EMPTY);
        SvnInfo info = infoOp.run();
        long r = info.getRevision();
        return Long.toString(r);
    }

    /**
     * Get version of "omegat/project_save.tmx" in GIT.
     */
    private static String getGITTmxVersion(File wc) throws Exception {
        Repository repository = Git.open(wc).getRepository();
        try (RevWalk walk = new RevWalk(repository)) {
            Ref localBranch = repository.findRef("HEAD");
            RevCommit headCommit = walk.lookupCommit(localBranch.getObjectId());
            return headCommit.getName();
        }
    }

    /**
     * Check if executed in console mode.
     */
    private static boolean isConsoleMode() {
        try {
            Core.getMainWindow().getApplicationFrame();
            return false;
        } catch (NoSuchMethodError ex) {
            return true;
        }
    }
}
