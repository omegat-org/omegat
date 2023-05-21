/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
               2023 Hiroshi Miura
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

package org.omegat.core.team2;

import java.util.HashMap;
import java.util.Map;

import org.omegat.core.team2.impl.GITRemoteRepository2;
import org.omegat.core.team2.impl.SVNRemoteRepository2;
import org.omegat.util.Log;

/**
 * Factory for create remote repository provider.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class RemoteRepositoryFactory {

    private static final Map<String, Class<? extends IRemoteRepository2>> repositoryConnectors =
            new HashMap<>();

    /**
     * Register team repository connector.
     * @param type of repository, such as "svn", "git" etc.
     * @param clazz connector class.
     */
    public static void addRepositoryConnector(String type, Class<? extends IRemoteRepository2> clazz) {
        repositoryConnectors.put(type, clazz);
    }

    private RemoteRepositoryFactory() {
    }

    public static IRemoteRepository2 create(String type) {
        if (!repositoryConnectors.containsKey(type)) {
            throw new RuntimeException("Unknown repository type: " + type);
        }
        try {
            return repositoryConnectors.get(type).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            Log.log(e);
            throw new RuntimeException("Failed to instantiate repository connector: " + type);
        }
    }

    /**
     * Tries to detect a repository type. Used for migrate old projects only.
     */
    public static String detectRepositoryType(String url) {
        if (url.startsWith("svn")) {
            return "svn";
        } else if (url.startsWith("git")) {
            return "git";
        } else {
            if (GITRemoteRepository2.isGitRepository(url)) {
                return "git";
            } else if (SVNRemoteRepository2.isSVNRepository(url)) {
                return "svn";
            }
            // unknown
            return null;
        }
    }
}
