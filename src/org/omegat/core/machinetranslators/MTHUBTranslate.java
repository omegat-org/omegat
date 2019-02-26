/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Encarnita Gómez (Prompsit)
               Home page: http://www.prompsit.com/
               Support center: help@prompsit.com

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
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.io.OutputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * MT-HUB (https://mt-hub.eu/)
 * @author prompsit (help@prompsit.com)
 * @see <a href="https://iadaatpa.docs.apiary.io/">Translation API</a>
 */

/**
 * A class for keep the request translate from MT-Hub.
*/
class MTHUBTranslateRequest {
    public final String token;
    public final String source;
    public final String target;
    public final List<String> segments;

    MTHUBTranslateRequest(String token, String source, String target, List<String> segments) {
        this.token = token;
        this.source = source;
        this.target = target;
        this.segments = new ArrayList<String>(segments);
    }
}

/**
 * A class for keep the response data segments from MT-Hub.
*/
class MTHUBTranslateResponseDataSegments {
    public String segment;
    public String translation;

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    public String getTranslation() {
        return translation;
    }

    public void setTranslation(String translation) {
        this.translation = translation;
    }
}

/**
 * A class for keep the response debug from MT-Hub.
*/
class MTHUBTranslateResponseDataDebug {
    public int supplierId;
    public String engineName;

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }
}

/**
 * A class for keep the response data from MT-Hub.
*/
class MTHUBTranslateResponseData {
    public List<List<MTHUBTranslateResponseDataSegments>> segments;
    public List<MTHUBTranslateResponseDataDebug> debug;

    public List<List<MTHUBTranslateResponseDataSegments>> getSegments() {
        return segments;
    }

    public void setSegments(List<List<MTHUBTranslateResponseDataSegments>>
                            segments) {
        this.segments = segments;
    }

    public List<MTHUBTranslateResponseDataDebug> getDebug() {
        return debug;
    }

    public void setDebug(List<MTHUBTranslateResponseDataDebug> debug) {
        this.debug = debug;
    }
}

/**
 * A class for keep the response error from MT-Hub.
*/
class MTHUBTranslateResponseError {
    public int statusCode;
    public int code;
    public int timestamp;
    public String message;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

/**
 * A class for keep the response from MT-Hub.
*/
class MTHUBTranslateResponse {
    public boolean success;
    public List<String> error;
    public List<MTHUBTranslateResponseData> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<String> getError() {
        return error;
    }

    public void setError(List<String> error) {
        this.error = error;
    }

    public List<MTHUBTranslateResponseData> getData() {
        return data;
    }

    public void setData(List<MTHUBTranslateResponseData> data) {
        this.data = data;
    }
}

public class MTHUBTranslate extends BaseTranslate {
    private static final String PROPERTY_API_KEY = "MTHUB.api.key";
    protected static final String GT_URL = "https://app.mt-hub.eu/api/translate";
    protected static final int LIMIT_CHARACTER = 2000;
    private List<String> availableLanguageCodes = null;
    private static final ObjectMapper OM = new ObjectMapper();

    /**
     * Get from mt-hub the available language codes and set into the arraylist.
    */
    private void setAvailableLanguageCodes() {

       String mthubKey = getCredential(PROPERTY_API_KEY);
       String encoding = StandardCharsets.UTF_8.name();
       try {
            StringBuilder codesMTHUB = new StringBuilder();
            URL mthub = new URL("https://app.mt-hub.eu/api/describelanguages/"
                                + mthubKey);
            URLConnection connection = mthub.openConnection();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), encoding))) {
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    codesMTHUB.append(inputLine);
                }
                this.availableLanguageCodes = getJsonCodes(codesMTHUB);
            } catch (Exception e) {
                System.err.println("IOException: " + e.getLocalizedMessage());
            }
        } catch (Exception e) {
            System.err.println("IOException: " + e.getLocalizedMessage());
        }
    }

    private synchronized List<String> getAvailableLanguageCodes() {
        if (availableLanguageCodes == null) {
            setAvailableLanguageCodes();
        }
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
        dialog.panel.valueLabel1.setText(OStrings.getString(
                                              "MT_ENGINE_MTHUB_API_KEY_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_API_KEY));
        dialog.panel.valueLabel2.setVisible(false);
        dialog.panel.valueField2.setVisible(false);
        dialog.panel.temporaryCheckBox.setSelected(
                isCredentialStoredTemporarily(PROPERTY_API_KEY));
        dialog.show();
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text)
            throws Exception {

        String mthubKey = getCredential(PROPERTY_API_KEY);
        if (mthubKey == null || mthubKey.isEmpty()) {
            return OStrings.getString("MT_ENGINE_MTHUB_KEY_NOTFOUND");
        }

        String prev = getFromCache(sLang, tLang, text);
        if (prev != null) {
            return prev;
        }

        if (this.availableLanguageCodes == null) {
            this.getAvailableLanguageCodes();
            if (this.availableLanguageCodes == null) {
                /*error to obtain the available language,
                it is a error with the user key*/
                return OStrings.getString("MT_ENGINE_MTHUB_INVALID_KEY");
            }
        }

        String encoding = StandardCharsets.UTF_8.name();
        try {
            URLConnection connection = new URL(GT_URL).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept-Charset", encoding);
            connection.setRequestProperty("Content-Type",
                                        "application/json;charset=" + encoding);

            try (OutputStream output = connection.getOutputStream()) {

                OM.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                        true);
                List<String> segments = Collections.singletonList(text.substring(0,
                                 Math.min(text.length(), LIMIT_CHARACTER - 1)));

                MTHUBTranslateRequest mttr = new MTHUBTranslateRequest(
                        mthubKey, normaliseCode(sLang), normaliseCode(tLang),
                        segments);

                String json = OM.writeValueAsString(mttr);
                output.write(json.getBytes(encoding));

                MTHUBTranslateResponse response = OM.readValue(
                                                  connection.getInputStream(),
                                                  MTHUBTranslateResponse.class);

                String translation = "";

                if (response.isSuccess()) {
                    translation = response.getData().
                                get(0).getSegments().get(0).get(0).
                                getTranslation();
                }

                putToCache(sLang, tLang, text, translation);
                return translation;
            } catch (Exception e) {
                if (e.getMessage().contains("401")) {
                    return OStrings.getString("MTHUB_ERROR", "401",
                        OStrings.getString("MT_ENGINE_MTHUB_INVALID_KEY"));
                } else if (e.getMessage().contains("400")) {
                    return OStrings.getString("MTHUB_ERROR", "400",
                        OStrings.getString("MT_ENGINE_MTHUB_MISSING_PARAMETERS"));
                } else if (e.getMessage().contains("500")) {
                    return OStrings.getString("MTHUB_ERROR", "500",
                          OStrings.getString("MT_ENGINE_MTHUB_INVALID_LANG_CODE"));
                } else {
                    return OStrings.getString("MTHUB_ERROR", "1", e.getMessage());
                }
            }
        } catch (MalformedURLException e) {
            return OStrings.getString("MTHUB_ERROR", e.getMessage());
        } catch (Exception e) {
            return OStrings.getString("MTHUB_ERROR", "1", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> getJsonCodes(StringBuilder json) {
        List<String> codes = new ArrayList<String>();
        Map<String, Object> rootNode = null;
        try {
            rootNode = (Map<String, Object>) JsonParser.parse(json.toString());
            if (rootNode.containsKey("success") && (Boolean) rootNode.get("success")
            && rootNode.containsKey("error") && rootNode.get("error") == null
            && rootNode.containsKey("data")) {
                Map<String, Object> dataNode = (Map<String, Object>) rootNode.get("data");
                List<Map<String, String>> languagesList = (List<Map<String, String>>) dataNode.get("languages");

                for (Map<String, String> o : languagesList) {
                    codes.add(o.get("code").toUpperCase());
                }
            }
        } catch (Exception e) {
            Log.logErrorRB(e, "MT_JSON_ERROR");
            return Collections.emptyList();
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
    private String normaliseCode(Language language) {

        String fullCode = language.getLanguage().toUpperCase();

        return availableLanguageCodes.contains(fullCode) ? fullCode : language.getLanguageCode();
    }
}
