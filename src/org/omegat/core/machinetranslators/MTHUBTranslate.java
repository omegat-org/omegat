/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.Map;
import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.JsonParser;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.io.OutputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * MT-HUB (https://mt-hub.eu/)
 * @author prompsit (help@prompsit.com)
 * @see <a href="https://iadaatpa.docs.apiary.io/">Translation API</a>
 */

/**
 * A class for keep the request translate from MT-Hub.
*/
class MTHUBTranslateRequest {
    public String token;
    public String source;
    public String target;
    public ArrayList<String> segments = new ArrayList<String>();

    MTHUBTranslateRequest(String token, String source, String target, ArrayList<String> segments) {
        this.token = token;
        this.source = source;
        this.target = target;
        this.segments = segments;
    }
}

public class MTHUBTranslate extends BaseTranslate {
    private static final String PROPERTY_API_KEY = "MTHUB.api.key";

    protected static final String GT_URL = "https://app.mt-hub.eu/api/translate";

    protected static final int LIMIT_CHARACTER = 2000;

    private ArrayList<String> availableLanguageCodes = new ArrayList<String>();

    /**
     * Get from mt-hub the available language codes and set into the arraylist.
    */
    private void setAvailableLanguageCodes() {

       String mthubKey = getCredential(PROPERTY_API_KEY);

       URLConnection connection = null;
       String codesMTHUB = "";
       try {

            URL mthub = new URL("https://app.mt-hub.eu/api/describelanguages/" + mthubKey);
            connection = mthub.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                                    connection.getInputStream(), "UTF-8"));
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                codesMTHUB += inputLine;
            }
            in.close();
            this.availableLanguageCodes = getJsonCodes(codesMTHUB);

        } catch (Exception e) {
                System.err.println("IOException: " + e);
        }
    }

    private ArrayList<String> getAvailableLanguageCodes() {
        return availableLanguageCodes;
    }

    @Override
    protected String getPreferenceName() {
       return Preferences.ALLOW_MTHUB_TRANSLATE;
    }

    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_MTHUB");
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
                String key = panel.valueField1.getText().trim();
                boolean temporary = panel.temporaryCheckBox.isSelected();
                setCredential(PROPERTY_API_KEY, key, temporary);
            }
        };
        dialog.panel.valueLabel1.setText(OStrings.getString("MT_ENGINE_MTHUB_API_KEY_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_API_KEY));
        dialog.panel.valueLabel2.setVisible(false);
        dialog.panel.valueField2.setVisible(false);
        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_API_KEY));
        dialog.show();
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        if (this.availableLanguageCodes.isEmpty()) {
         this.setAvailableLanguageCodes();
        }

        String mthubKey = getCredential(PROPERTY_API_KEY);
        if (mthubKey == null || mthubKey.isEmpty()) {
            return OStrings.getString("MT_ENGINE_MTHUB_KEY_NOTFOUND");
        }

        String prev = getFromCache(sLang, tLang, text);
        if (prev != null) {
            return prev;
        }
        String charset = "UTF-8";

        try {
            URLConnection connection = new URL(GT_URL).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/json ;charset=" + charset);

            OutputStream output = connection.getOutputStream();

            ObjectMapper om = new ObjectMapper();

            ArrayList<String> segments = new ArrayList<String>();
            segments.add(text.substring(0, Math.min(text.length(), LIMIT_CHARACTER - 1)));

            MTHUBTranslateRequest mttr = new MTHUBTranslateRequest(
                    mthubKey, getNormaliseCode(sLang), getNormaliseCode(tLang), segments);

            String json = om.writeValueAsString(mttr);
            output.write(json.getBytes(charset));

            Map<String, Object> map = om.readValue(connection.getInputStream(),
                                      new TypeReference<Map<String, Object>>() { });
            String translation = getJsonResults(map);

            putToCache(sLang, tLang, text, translation);
            return translation;
        } catch (MalformedURLException e) {
            return StringUtil.format(OStrings.getString("MTHUB_ERROR"), e.getMessage());
        } catch (Exception e) {
            if (e.getMessage().contains("401")) {
                return StringUtil.format(OStrings.getString("MTHUB_ERROR"), "401",
                        OStrings.getString("MT_ENGINE_MTHUB_INVALID_KEY"));
            } else if (e.getMessage().contains("400")) {
                return StringUtil.format(OStrings.getString("MTHUB_ERROR"), "400",
                        OStrings.getString("MT_ENGINE_MTHUB_MISSING_PARAMETERS"));
            } else if (e.getMessage().contains("500")) {
                return StringUtil.format(OStrings.getString("MTHUB_ERROR"), "500",
                        OStrings.getString("MT_ENGINE_MTHUB_INVALID_LANG_CODE"));
            } else {
                return StringUtil.format(OStrings.getString("MTHUB_ERROR"), "1", e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected String getJsonResults(Map<String, Object> rootNode) {

        String tr = "";

        if (rootNode.containsKey("success") && (Boolean) rootNode.get("success")
            && rootNode.containsKey("error") && rootNode.get("error") == null
            && rootNode.containsKey("data")
           ) {
            try {
              Map<String, Object> dataNode = (Map<String, Object>) rootNode.get("data");
              List<Object> translationsList = (List<Object>) dataNode.get("segments");
              Map<String, String> translationNode = (Map<String, String>) translationsList.get(0);

              tr = translationNode.get("translation");
            } catch (NullPointerException e) {
              return null;
            }
        }

        if (rootNode.containsKey("error") && rootNode.get("error") != null) {
            Map<String, Object> error = (Map<String, Object>) rootNode.get("error");
            switch ((String) error.get("statusCode")) {
                case "401":
                     tr = StringUtil.format(OStrings.getString("MTHUB_ERROR"),
                                    (int) error.get("code"), OStrings.getString("MT_ENGINE_MTHUB_INVALID_KEY"));
                     break;
                case "400":
                    switch ((String) error.get("code")) {
                        case "4":
                            tr = StringUtil.format(OStrings.getString("MTHUB_ERROR"),
                                    (int) error.get("code"), OStrings.getString("MT_ENGINE_MTHUB_INVALID_DOMAIN"));
                            break;
                        case "11":
                            tr = StringUtil.format(OStrings.getString("MTHUB_ERROR"),
                                    (int) error.get("code"), OStrings.getString("MT_ENGINE_MTHUB_MISSING_KEY"));
                            break;
                        case "17":
                            tr = StringUtil.format(OStrings.getString("MTHUB_ERROR"),
                                    (int) error.get("code"), OStrings.getString("MT_ENGINE_MTHUB_MISSING_SEGMENTS"));
                            break;
                        default:
                            tr = StringUtil.format(OStrings.getString("MTHUB_ERROR"),
                                    (int) error.get("code"), OStrings.getString("MT_ENGINE_MTHUB_MISSING_PARAMETERS"));
                            break;
                    }
                    break;
                case "500":
                    switch ((String) error.get("code")) {
                        case "19":
                            tr = StringUtil.format(OStrings.getString("MTHUB_ERROR"),
                                    (int) error.get("code"), OStrings.getString("MT_ENGINE_MTHUB_INVALID_LANG_CODE"));
                            break;
                        default:
                            tr = StringUtil.format(OStrings.getString("MTHUB_ERROR"),
                                    (int) error.get("code"), OStrings.getString("MT_ENGINE_MTHUB_MISSING_PARAMETERS"));
                            break;
                    }
                    break;
                default:
                    tr = StringUtil.format(OStrings.getString("MTHUB_ERROR"),
                            1, (String) error.get("message"));
                    break;
            }
        }

        return tr;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<String> getJsonCodes(String json) {
        ArrayList<String> codes = new ArrayList<String>();
        Map<String, Object> rootNode = null;
        try {
            rootNode = (Map<String, Object>) JsonParser.parse(json);
            if (rootNode.containsKey("success") && (Boolean) rootNode.get("success")
            && rootNode.containsKey("error") && rootNode.get("error") == null
            && rootNode.containsKey("data")
           ) {
                Map<String, Object> dataNode = (Map<String, Object>) rootNode.get("data");
                List<Map<String, String>> languagesList = (List<Map<String, String>>) dataNode.get("languages");

                for (Map<String, String> o : languagesList) {
                    codes.add(o.get("code").toUpperCase());
                }
            }
        } catch (Exception e) {
            Log.logErrorRB(e, "MT_JSON_ERROR");
            return new ArrayList<String>();
        }
        return codes;
    }

    /**
     * Normalise language codes
     *
     * @param language
     *            An OmegaT language
     * @return A normalise code for MT-HUB languages (ISO 639-1 Code)
     */
    private String getNormaliseCode(Language language) {

        String lCode = language.getLanguage();
        if (!this.availableLanguageCodes.isEmpty() && !this.availableLanguageCodes.contains(language.getLanguage())) {
            lCode = language.getLanguageCode();
        }

        return lCode;
    }
}
