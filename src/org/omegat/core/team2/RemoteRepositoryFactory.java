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

package org.omegat.core.team2;

import org.omegat.core.team2.impl.GITRemoteRepository2;
import org.omegat.core.team2.impl.HTTPRemoteRepository;
import org.omegat.core.team2.impl.SVNRemoteRepository2;

/**
 * Factory for create remote repository provider.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class RemoteRepositoryFactory {

    private RemoteRepositoryFactory() {
    }

    public static IRemoteRepository2 create(String type) {
        if ("svn".equals(type)) {
            return new SVNRemoteRepository2();
        } else if ("git".equals(type)) {
            return new GITRemoteRepository2();
        } else if ("http".equals(type)) {
            return new HTTPRemoteRepository();
        } else {
            throw new RuntimeException("Unknown repository type: " + type);
        }
    }

    /**
     * Tries to detect repository type. Used for migrate old projects only.
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
