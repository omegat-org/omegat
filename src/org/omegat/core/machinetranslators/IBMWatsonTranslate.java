/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Didier Briel
               2011 Briac Pilpre, Alex Buloichik
               2013 Didier Briel
               2016 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.core.machinetranslators;

import java.awt.Window;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.WikiGet;

/**
 * Support of IBM Watson machine translation.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Didier Briel
 * @author Briac Pilpre
 * @author Aaron Madlon-Kay
 *
 * @see <a href="https://www.ibm.com/watson/developercloud/language-translator/api/v2/curl.html?curl#introduction">Translation API</a>
 */
public class IBMWatsonTranslate extends BaseTranslate {
    protected static final String PROPERTY_LOGIN = "ibmwatson.api.login";
    protected static final String PROPERTY_PASSWORD = "ibmwatson.api.password";
    protected static final String WATSON_URL = "https://gateway.watsonplatform.net/language-translator/api/v2/translate";
    protected static final Pattern RE_HTML = Pattern.compile("&#([0-9]+);");

    @Override
    protected String getPreferenceName() {
        return Preferences.ALLOW_IBMWATSON_TRANSLATE;
    }

    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_IBMWATSON");
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String trText = text.length() > 5000 ? text.substring(0, 4997) + "..." : text;
        String prev = getFromCache(sLang, tLang, trText);
        if (prev != null) {
            return prev;
        }

        String apiLogin = getCredential(PROPERTY_LOGIN);
        String apiPassword = getCredential(PROPERTY_PASSWORD);

        if (apiPassword == null || apiPassword.isEmpty() || apiLogin == null || apiLogin.isEmpty()) {
            return OStrings.getString("IBMWATSON_API_KEY_NOTFOUND");
        }

        Map<String, String> params = new TreeMap<>();

        params.put("text", trText);
        params.put("source", sLang.getLanguageCode().toUpperCase());
        params.put("target", tLang.getLanguageCode().toUpperCase());

        Map<String, String> headers = new TreeMap<>();

        // By default, all Watson services log requests and their results.
        // The logged data is not shared or made public. To prevent IBM
        // from accessing your data for general service improvements, set
        // the X-Watson-Learning-Opt-Out request header to true for all requests.

        // Let's opt out then.
        headers.put("X-Watson-Learning-Opt-Out", "true");

        String authentication = "Basic " + Base64.getMimeEncoder().encodeToString((apiLogin + ":" + apiPassword).getBytes(StandardCharsets.ISO_8859_1));
        headers.put("Authorization", authentication);
        // No need to parse JSON
        headers.put("Accept", "text/plain");

        String tr;
        try {
            tr = WikiGet.get(WATSON_URL, params, headers, "UTF-8");
        } catch (IOException e) {
            return e.getLocalizedMessage();
        }

        if (tr == null) {
            return "";
        }

        tr = unescapeHTML(tr);

        tr = cleanSpacesAroundTags(tr, trText);

        putToCache(sLang, tLang, trText, tr);
        return tr;
    }

    /** Convert entities to character. Ex: "&#39;" to "'". */
    private String unescapeHTML(String text) {

        text = text.replace("&quot;", "\"").replace("&gt;", ">").replace("&lt;", "<").replace("&amp;", "&");

        Matcher m = RE_HTML.matcher(text);
        while (m.find()) {
            String g = m.group();
            int codePoint = Integer.parseInt(m.group(1));
            String cpString = String.valueOf(Character.toChars(codePoint));
            text = text.replace(g, cpString);
        }
        return text;
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public void showConfigurationUI(Window parent) {

        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                boolean temporary = panel.temporaryCheckBox.isSelected();

                String login = panel.valueField1.getText().trim();
                setCredential(PROPERTY_LOGIN, login, temporary);

                String password = panel.valueField2.getText().trim();
                setCredential(PROPERTY_PASSWORD, password, temporary);

            }
        };

        dialog.panel.valueLabel1.setText(OStrings.getString("MT_ENGINE_IBMWATSON_LOGIN_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_LOGIN));

        dialog.panel.valueLabel2.setText(OStrings.getString("MT_ENGINE_IBMWATSON_PASSWORD_LABEL"));
        dialog.panel.valueField2.setText(getCredential(PROPERTY_PASSWORD));

        // TODO Apparently, the API URL can change if the user has their own instance.

        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_PASSWORD));

        dialog.show();
    }
}
