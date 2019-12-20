/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2010 Didier Briel
               2012 Alex Buloichick, Didier Briel
               2016 Aaron Madlon-Kay
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

package org.omegat.gui.preferences.view;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.swing.JComponent;

import org.omegat.gui.preferences.BasePreferencesController;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;

/**
 * @author Didier Briel
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class UserPassController extends BasePreferencesController {

    private UserPassPanel panel;

    @Override
    public JComponent getGui() {
        if (panel == null) {
            initGui();
            initFromPrefs();
        }
        return panel;
    }

    @Override
    public String toString() {
        return OStrings.getString("PREFS_TITLE_PROXY_LOGIN");
    }

    private void initGui() {
        panel = new UserPassPanel();
    }

    @Override
    protected void initFromPrefs() {
        String encodedUser = Preferences.getPreference(Preferences.PROXY_USER_NAME);
        String encodedPassword = Preferences.getPreference(Preferences.PROXY_PASSWORD);

        try {
            panel.userText.setText(StringUtil.decodeBase64(encodedUser, StandardCharsets.ISO_8859_1));
            panel.passwordField
                    .setText(StringUtil.decodeBase64(encodedPassword, StandardCharsets.ISO_8859_1));
        } catch (IllegalArgumentException ex) {
            Log.logErrorRB("LOG_DECODING_ERROR");
            Log.log(ex);
        }
    }

    @Override
    public void restoreDefaults() {
        panel.userText.setText("");
        panel.passwordField.setText("");
    }

    @Override
    public void persist() {
        String user = panel.userText.getText().trim();
        if (!user.isEmpty()) {
            String encodedUser = StringUtil.encodeBase64(panel.userText.getText(),
                    StandardCharsets.ISO_8859_1);
            Preferences.setPreference(Preferences.PROXY_USER_NAME, encodedUser);
        }
        char[] password = panel.passwordField.getPassword();
        if (password.length > 0) {
            String encodedPassword = StringUtil.encodeBase64(password, StandardCharsets.ISO_8859_1);
            Preferences.setPreference(Preferences.PROXY_PASSWORD, encodedPassword);
        }
        Arrays.fill(password, '\0');
    }
}
