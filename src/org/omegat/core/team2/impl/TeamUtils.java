/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Alex Buloichik
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

package org.omegat.core.team2.impl;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.omegat.core.Core;
import org.omegat.core.team2.TeamSettings;
import org.omegat.util.Log;
import org.omegat.util.StringUtil;

/**
 * Some utility methods for team code.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class TeamUtils {

    /**
     * POJO to hold credentials.
     */
    public static class Credentials {
        public String username = null;
        public String password = null;
        public boolean perHost = true;
    }

    private TeamUtils() {
    }

    static final String KEY_USERNAME_SUFFIX = "username";
    static final String KEY_PASSWORD_SUFFIX = "password";

    static final String KEY_FINGERPRINT_SUFFIX = "fingerprint";

    public static Credentials loadCredentials(String url, String scheme, String host, String path, int port) {
        Credentials credentials = new Credentials();
        // we use
        // 1. "schema://server:port/path"
        // 2. "/path/to/.ssh/id_rsa"
        // 3. "schema://server"
        // 4. "schema://server:port",
        // check following order
        credentials.username = TeamSettings.get(url + "!" + KEY_USERNAME_SUFFIX);
        credentials.password = decodePassword(TeamSettings.get(url + "!" + KEY_PASSWORD_SUFFIX));
        if (credentials.password != null) {
            credentials.perHost = false;
            return credentials;
        }

        if (scheme == null) {
            url = path;
            credentials.perHost = false;
        } else if (port != -1) {
            url = scheme + "://" + host + ":" + port;
            credentials.perHost = true;
        } else {
            url = scheme + "://" + host;
            credentials.perHost = true;
        }
        credentials.username = TeamSettings.get(url + "!" + KEY_USERNAME_SUFFIX);
        credentials.password = decodePassword(TeamSettings.get(url + "!" + KEY_PASSWORD_SUFFIX));
        return credentials;
    }

    public static void saveCredentials(String url, String scheme, String host, String path, int port,
                                       Credentials credentials) {
        String key;
        if (scheme == null) {
            key = path;
        } else if (credentials.perHost && host != null) {
            key = scheme + "://" + host + (port != -1 ? ":" + port : "");
        } else {
            key = url;
        }
        try {
            if (!StringUtil.isEmpty(credentials.username)) {
                TeamSettings.set(key + "!" + KEY_USERNAME_SUFFIX, credentials.username);
            }
            TeamSettings.set(key + "!" + KEY_PASSWORD_SUFFIX, TeamUtils.encodePassword(credentials.password));
        } catch (Exception e) {
            Log.logErrorRB(e, "TEAM_ERROR_SAVE_CREDENTIALS");
        }
    }

    public static String loadFingerprint(String url) {
        return TeamSettings.get(url + "!" + KEY_FINGERPRINT_SUFFIX);
    }

    public static void saveFingerprint(String url, String fingerprint) {
        try {
            TeamSettings.set(url + "!" + KEY_FINGERPRINT_SUFFIX, fingerprint);
        } catch (Exception e) {
            Core.getMainWindow().displayErrorRB(e, "TEAM_ERROR_SAVE_CREDENTIALS", null, "TF_ERROR");
        }
    }

    public static String encodePassword(String pass) {
        if (pass == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(pass.getBytes(StandardCharsets.UTF_8));
    }

    public static String decodePassword(String pass) {
        if (pass == null) {
            return null;
        }
        byte[] data = Base64.getDecoder().decode(pass);
        return new String(data, StandardCharsets.UTF_8);
    }
}
