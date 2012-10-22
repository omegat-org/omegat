/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/
package org.omegat.core.data;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.Map;

import org.omegat.core.Core;
import org.omegat.core.KnownException;
import org.omegat.core.team.IRemoteRepository;
import org.omegat.core.team.RepositoryUtils;
import org.omegat.util.Log;
import org.omegat.util.Preferences;

/**
 * Instead just usual save like in ProjectTMX, this class saves and synchronizes translation with svn/git
 * repository for ability to team work for one project. Synchronization usually called every 1-5 minutes.
 * 
 * How it works.
 * 
 * On each moment we have 3 versions of translation(project_save.tmx file):
 * 
 * 1. BASE - version which current translator downloaded from remote repository previously(on previous
 * synchronization or startup).
 * 
 * 2. WORKING - current version in translator's OmegaT. It doesn't exist it remote repository yet. It's
 * inherited from BASE version, i.e. BASE + local changes.
 * 
 * 3. HEAD - latest version in repository, which other translators commited. It's also inherited from BASE
 * version, i.e. BASE + remote changes.
 * 
 * In the ideal world, we could just calculate diff between WORKING and BASE - it will be our local changes
 * after latest synchronization, then rebase these changes on the HEAD revision, then commit into remote
 * repository.
 * 
 * But we have some real world limitations: a) computers and networks work slowly, i.e. this synchronization
 * will require some seconds, but translator should be able to edit translation in this time. b) we have to
 * handle network errors, c) other translators can commit own data in the same time.
 * 
 * So, in the real world synchronization works by these steps:
 * 
 * 1. Download HEAD revision from remote repository and load it in memory.
 * 
 * 2. Load BASE revision from local disk.
 * 
 * 3. Calculate diff between WORKING and BASE, then rebase it on the top of HEAD revision. This step
 * synchronized around memory TMX, so, all edits are stopped. Since it's enough fast step, it's okay.
 * 
 * 4. Upload new revision into repository.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class ProjectTeamTMX extends ProjectTMX {
    IRemoteRepository repository;
    boolean isOnlineMode;

    public ProjectTeamTMX(ProjectProperties props, File file, CheckOrphanedCallback callback,
            IRemoteRepository repository) throws Exception {
        super(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(), file, callback);

        this.repository = repository;
        isOnlineMode = true;
    }

    /**
     * Team version of save() for process all steps.
     * 
     * We should restore BASE revision and apply delta even in case translator didn't make any changes,
     * because he will be able to make changes in time of downloading new revision from remote repository.
     */
    @Override
    public void save(ProjectProperties props, String translationFile, boolean translationUpdatedByUser)
            throws Exception {
        final File orig = new File(translationFile);
        final File backup = new File(translationFile + ".bak");

        ProjectTMX baseTMX, headTMX;
        File fileOnBase, fileOnHead;

        String baseRev = repository.getBaseRevisionId(orig);
        // save into ".new" file
        fileOnBase = new File(translationFile + "-based_on_" + baseRev + ".new");
        exportTMX(props, fileOnBase, false, false, true);

        // restore BASE revision and load
        repository.restoreBase(orig);
        baseTMX = new ProjectTMX(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(), orig, null);

        boolean needUpload = false;
        // update to HEAD revision from repository and load
        try {
            repository.download(orig);
            setOnlineMode();
            needUpload = true;
        } catch (SocketException ex) {
            setOfflineMode();
        } catch (Exception ex) {
        }
        String headRev = repository.getBaseRevisionId(orig);

        if (headRev.equals(baseRev)) {
            // don't need rebase
            headTMX = baseTMX;
            fileOnHead = fileOnBase;
            fileOnBase = null;
        } else {
            // need rebase
            headTMX = new ProjectTMX(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(), orig, null);
            synchronized (this) {
                ProjectTMX delta = calculateDelta(baseTMX, this);
                baseTMX.clear();
                applyDelta(headTMX, delta);
            }
            fileOnHead = new File(translationFile + "-based_on_" + headRev + ".new");
            exportTMX(props, fileOnHead, false, false, true);
        }

        if (backup.exists()) {
            if (!backup.delete()) {
                throw new IOException("Error delete backup file");
            }
        }

        // Rename existing project file in case a fatal error
        // is encountered during the write procedure - that way
        // everything won't be lost
        if (orig.exists()) {
            if (!orig.renameTo(backup)) {
                throw new IOException("Error rename old file to backup");
            }
        }

        // Rename new file into TMX file
        if (!fileOnHead.renameTo(orig)) {
            throw new IOException("Error rename new file to tmx");
        }

        if (fileOnBase != null) {
            // Remove temp backup file
            if (!fileOnBase.delete()) {
                throw new IOException("Error remove old file");
            }
        }

        // upload updated
        if (needUpload) {
            final String author = Preferences.getPreferenceDefault(Preferences.TEAM_AUTHOR,
                    System.getProperty("user.name"));
            try {
                new RepositoryUtils.AskCredentials() {
                    public void callRepository() throws Exception {
                        repository.upload(orig, "Translated by " + author);
                    }
                }.execute(repository);
                setOnlineMode();
            } catch (SocketException ex) {
                setOfflineMode();
            } catch (Exception ex) {
                throw new KnownException(ex, "TEAM_SYNCHRONIZATION_ERROR");
            }
        }
    }

    void setOnlineMode() {
        if (!isOnlineMode) {
            Log.logInfoRB("VCS_ONLINE");
            Core.getMainWindow().displayWarningRB("VCS_ONLINE");
        }
        isOnlineMode = true;
    }

    void setOfflineMode() {
        if (isOnlineMode) {
            Log.logInfoRB("VCS_OFFLINE");
            Core.getMainWindow().displayWarningRB("VCS_OFFLINE");
        }
        isOnlineMode = false;
    }

    /**
     * Calculates delta between base and changed TMX.
     */
    ProjectTMX calculateDelta(ProjectTMX baseTMX, ProjectTMX changedTMX) {
        ProjectTMX delta = new ProjectTMX();

        // find updated and removed
        for (Map.Entry<String, TMXEntry> en : baseTMX.defaults.entrySet()) {
            TMXEntry newEntry = changedTMX.defaults.get(en.getKey());

            if (!en.getValue().equalsTranslation(newEntry)) {
                delta.defaults.put(en.getKey(), newEntry);
            }
        }
        for (Map.Entry<EntryKey, TMXEntry> en : baseTMX.alternatives.entrySet()) {
            TMXEntry newEntry = changedTMX.alternatives.get(en.getKey());

            if (!en.getValue().equalsTranslation(newEntry)) {
                delta.alternatives.put(en.getKey(), newEntry);
            }
        }

        // find added
        if (changedTMX.defaults != null) {
            for (Map.Entry<String, TMXEntry> en : changedTMX.defaults.entrySet()) {
                if (!baseTMX.defaults.containsKey(en.getKey())) {
                    delta.defaults.put(en.getKey(), en.getValue());
                }
            }
        }
        for (Map.Entry<EntryKey, TMXEntry> en : changedTMX.alternatives.entrySet()) {
            if (!baseTMX.alternatives.containsKey(en.getKey())) {
                delta.alternatives.put(en.getKey(), en.getValue());
            }
        }

        return delta;
    }

    /**
     * Apply delta on base TMX.
     */
    void applyDelta(ProjectTMX baseTMX, ProjectTMX delta) {
        defaults = baseTMX.defaults;
        alternatives = baseTMX.alternatives;

        for (Map.Entry<String, TMXEntry> en : delta.defaults.entrySet()) {
            if (en.getValue() != null) {
                defaults.put(en.getKey(), en.getValue());
            } else {
                defaults.remove(en.getKey());
            }
        }
        for (Map.Entry<EntryKey, TMXEntry> en : delta.alternatives.entrySet()) {
            if (en.getValue() != null) {
                alternatives.put(en.getKey(), en.getValue());
            } else {
                alternatives.remove(en.getKey());
            }
        }
    }
}
