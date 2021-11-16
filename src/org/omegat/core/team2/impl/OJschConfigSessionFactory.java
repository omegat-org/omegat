/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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

package org.omegat.core.team2.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.StringUtils;

public class OJschConfigSessionFactory extends JschConfigSessionFactory {

    /**
     * Default JSch instance creator.
     * backport of JGit 6.0.
     * When JGit 5.13 releases with the fix, this wrapper can be removed.
     */
    @Override
    protected JSch createDefaultJSch(FS fs) throws JSchException {
        // Note: this is back port of JGit 6.0 master branch.
        JSch jsch = new JSch();
        // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=537790 and
        // https://bugs.eclipse.org/bugs/show_bug.cgi?id=576604
        copyGlobalConfigIfNotSet("signature.rsa", "ssh-rsa");
        copyGlobalConfigIfNotSet("signature.dss", "ssh-dss");
        configureJSch(jsch);
        knownHosts(jsch, fs);
        identities(jsch, fs);
        return jsch;
    }

    private static void copyGlobalConfigIfNotSet(String from, String to) {
        String toValue = JSch.getConfig(to);
        if (StringUtils.isEmptyOrNull(toValue)) {
            String fromValue = JSch.getConfig(from);
            if (!StringUtils.isEmptyOrNull(fromValue)) {
                JSch.setConfig(to, fromValue);
            }
        }
    }

    private static void knownHosts(JSch jsch, FS fs) throws JSchException {
        final File home = fs.userHome();
        if (home == null)
            return;
        final File known_hosts = new File(new File(home, ".ssh"), "known_hosts");
        try (FileInputStream in = new FileInputStream(known_hosts)) {
            jsch.setKnownHosts(in);
        } catch (IOException ignored) {
        }
    }

    private static void identities(final JSch jsch, final FS fs) {
        final File home = fs.userHome();
        if (home == null) {
            return;
        }
        final File sshdir = new File(home, ".ssh");
        if (sshdir.isDirectory()) {
            //  Add more default key names; id_ecdsa and id_ed25519
            loadIdentity(jsch, new File(sshdir, "identity"));
            loadIdentity(jsch, new File(sshdir, "id_rsa"));
            loadIdentity(jsch, new File(sshdir, "id_dsa"));
        }
    }

    private static void loadIdentity(JSch sch, File priv) {
        if (priv.isFile()) {
            try {
                sch.addIdentity(priv.getAbsolutePath());
            } catch (JSchException ignored) {
            }
        }
    }
}
