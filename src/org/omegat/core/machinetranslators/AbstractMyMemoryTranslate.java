/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Ibai Lakunza Velasco, Didier Briel
               2013 Martin Wunderlich
               2014 Manfred Martin
               2015 Didier Briel
               2017 Briac Pilpre
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

package org.omegat.core.machinetranslators;

import java.awt.Window;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.Language;
import org.omegat.util.OStrings;

/**
 * @author Ibai Lakunza Velasco
 * @author Didier Briel
 * @author Martin Wunderlich
 * @author Manfred Martin
 * @author Briac Pilpre
 */
public abstract class AbstractMyMemoryTranslate extends BaseCachedTranslate {

    protected static final String MYMEMORY_API_EMAIL = "mymemory.api.email";
    protected static final String MYMEMORY_API_KEY = "mymemory.api.key";
    private static final String DEFAULT_GT_URL = "https://mymemory.translated.net/api/get";

    private String gtURL;

    public AbstractMyMemoryTranslate(String url) {
        gtURL = url;
    }

    public AbstractMyMemoryTranslate() {
        gtURL = DEFAULT_GT_URL;
    }

    @Override
    protected abstract String getPreferenceName();

    @Override
    public abstract String getName();

    /**
     * Modify some country codes to fit with MyMemory
     *
     * @param language
     *            An OmegaT language
     * @return A code modified for MyMemory languages
     */
    protected String mymemoryCode(Language language) {
        return language.getLocaleLCID();
    }

    @Override
    protected abstract String translate(Language sLang, Language tLang, String text) throws Exception;

    /**
     * Query MyMemory API and return parsed JsonNode object.
     */
    @SuppressWarnings("unchecked")
    protected JsonNode getMyMemoryResponse(Language sLang, Language tLang, String text) throws IOException {

        String targetLang = tLang.getLocaleLCID();
        String sourceLang = sLang.getLocaleLCID();

        String apiKey = System.getProperty(MYMEMORY_API_KEY, getCredential(MYMEMORY_API_KEY));
        String email = System.getProperty(MYMEMORY_API_EMAIL, getCredential(MYMEMORY_API_EMAIL));

        Map<String, String> params = new TreeMap<>();

        // The sentence you want to translate. Use UTF-8. Max 500 bytes
        params.put("q", text);

        // Source and language pair, separated by the | symbol. Use ISO standard
        // names or RFC3066
        params.put("langpair", sourceLang + "|" + targetLang);

        // Output format - json (default), tmx, serialized php array
        params.put("of", "json");

        // Enables Machine Translation in results. You can turn it off if you
        // want just human segments
        params.put("mt", includeMT() ? "1" : "0");

        // If your request is authenticated, returns only matches from your
        // private TM.
        // params.put("onlyprivate", onlyPrivate() ? "1" : "0");

        // Authenticates the request; matches from your private TM are returned
        // too.
        if (!apiKey.isEmpty()) {
            params.put("key", apiKey);
        }

        // (CAT) The IP of the end user generating the request.
        // params.put("ip", "");

        // (CAT) A valid email where we can reach you in case of troubles.
        if (!email.isEmpty()) {
            params.put("de", email);
        }

        Map<String, String> headers = new TreeMap<>();

        // Get the results from MyMemory
        ObjectMapper mapper = new ObjectMapper();
        String response = HttpConnectionUtils.get(gtURL, params, headers);
        return mapper.readTree(response);
    }

    /**
     * MyMemory driver is configurable.
     */
    @Override
    public boolean isConfigurable() {
        return true;
    }

    /**
     * Default configuration UI.
     */
    @Override
    public void showConfigurationUI(Window parent) {
        MTConfigDialog dialog = new MTConfigDialog(parent, getName()) {
            @Override
            protected void onConfirm() {
                String email = panel.valueField1.getText().trim();
                boolean temporary = panel.temporaryCheckBox.isSelected();
                setCredential(MYMEMORY_API_EMAIL, email, temporary);
            }
        };
        dialog.panel.valueLabel1.setText(OStrings.getString("MT_ENGINE_MYMEMORY_EMAIL_LABEL"));
        dialog.panel.valueField1.setText(getCredential(MYMEMORY_API_EMAIL));
        dialog.panel.valueLabel2.setText(OStrings.getString("MT_ENGINE_MYMEMORY_API_KEY_LABEL"));
        dialog.panel.valueField2.setText(getCredential(MYMEMORY_API_KEY));
        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(MYMEMORY_API_EMAIL));
        dialog.show();
    }

    /** true: Include MT / false: human translate */
    protected abstract boolean includeMT();
}
