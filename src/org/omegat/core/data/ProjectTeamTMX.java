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
import java.util.Map;

import org.omegat.core.Core;
import org.omegat.core.KnownException;
import org.omegat.core.team.IRemoteRepository;
import org.omegat.core.team.RepositoryUtils;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
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

    public ProjectTeamTMX(ProjectProperties props, File file, CheckOrphanedCallback callback,
            IRemoteRepository repository) throws Exception {
        super(props.getSourceLanguage(), props.getTargetLanguage(), props.isSentenceSegmentingEnabled(), file, callback);

        this.repository = repository;
    }

    /**
     * Calculates delta between base and changed TMX.
     *
     * @return a tmx with all updated/removed/added entries in changedTMX compared to baseTMX.
     */
    public static ProjectTMX calculateDelta(ProjectTMX baseTMX, ProjectTMX changedTMX) {
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
     * Replaces the current translations with those of the given TMX, and applies the delta on the translations
     *
     * @param newTMX the translation memory of which the translations have to be used
     * @param delta the delta that has to be applied on the new TMX.
     */
    void applyTMXandDelta(ProjectTMX newTMX, ProjectTMX delta) {
        defaults = newTMX.defaults;
        alternatives = newTMX.alternatives;

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
