/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2019 Encarnita Gomez (Prompsit)
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
import java.net.MalformedURLException;
import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.OutputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.DeserializationFeature;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import java.sql.Timestamp;

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
class MTHUBResponseError {
    public int statusCode;
    public int code;
    public Timestamp timestamp = null;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
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
 * A class for keep the translate response from MT-Hub.
*/
class MTHUBTranslateResponse {
    public boolean success;
    public List<MTHUBResponseError> error;
    public List<MTHUBTranslateResponseData> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<MTHUBResponseError> getError() {
        return error;
    }

    public void setError(List<MTHUBResponseError> error) {
        this.error = error;
    }

    public List<MTHUBTranslateResponseData> getData() {
        return data;
    }

    public void setData(List<MTHUBTranslateResponseData> data) {
        this.data = data;
    }
}

/**
 * A class for keep the response data languages from MT-Hub.
*/
class MTHUBLanguageResponseDataLanguages {
    public String code;
    public String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

/**
 * A class for keep the response data languages list from MT-Hub.
*/
class MTHUBLanguageResponseData {
    public List<List<MTHUBLanguageResponseDataLanguages>> languages;

    public List<List<MTHUBLanguageResponseDataLanguages>> getLanguages() {
        return languages;
    }

    public void setLanguages(List<List<MTHUBLanguageResponseDataLanguages>> languages) {
        this.languages = languages;
    }
}

/**
 * A class for keep the language response from MT-Hub.
*/
class MTHUBLanguageResponse {
    public boolean success;
    public List<MTHUBResponseError> error;
    public List<MTHUBLanguageResponseData> data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public List<MTHUBResponseError> getError() {
        return error;
    }

    public void setError(List<MTHUBResponseError> error) {
        this.error = error;
    }

    public List<MTHUBLanguageResponseData> getData() {
        return data;
    }

    public void setData(List<MTHUBLanguageResponseData> data) {
        this.data = data;
    }
}
/**
 * MT-HUB (https://mt-hub.eu/)
 * @author prompsit (help@prompsit.com)
 * @see <a href="https://iadaatpa.docs.apiary.io/">Translation API</a>
 */
public class MTHUBTranslate extends BaseTranslate {
    private static final String PROPERTY_API_KEY = "mthub.api.key";
    protected static final String GT_URL = "https://app.mt-hub.eu/api/translate";
    protected static final int LIMIT_CHARACTER = 2000;
    private List<String> availableLanguageCodes = null;
    private static final ObjectMapper OM = new ObjectMapper();
    static {
        OM.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                        true);
    }

    /**
     * Get from mt-hub the available language codes and set into the arraylist.
    */
    private List<String> fetchAvailableLanguageCodes() {

       String mthubKey = getCredential(PROPERTY_API_KEY);
       String encoding = StandardCharsets.UTF_8.name();

       try {
            List<String> codes = new ArrayList<String>();
            URL url = new URL("https://app.mt-hub.eu/api/describelanguages/"
                              + mthubKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestProperty("Accept-Charset", encoding);
            conn.setRequestProperty("Content-Type",
                                        "application/json;charset=" + encoding);
            int statusCode = conn.getResponseCode();

            try (InputStream in = (statusCode >= 200 && statusCode < 400)
                    ? conn.getInputStream() : conn.getErrorStream();) {
                MTHUBLanguageResponse response = OM.readValue(IOUtils.toString(in, StandardCharsets.UTF_8),
                                                      MTHUBLanguageResponse.class);
                if (response.isSuccess()) {
                    for (List<MTHUBLanguageResponseDataLanguages> dl : response.getData().get(0).getLanguages()) {
                        codes.add(dl.get(0).getCode().toUpperCase());
                    }
                    return codes;
                } else {
                    System.err.println("IOException: "
                            + response.getError().get(0).getCode() + " "
                            + response.getError().get(0).getMessage());
                }
            } catch (Exception e) {
                System.err.println("IOException: " + e.getLocalizedMessage());
                return Collections.emptyList();
            }
        } catch (MalformedURLException e) {
            System.err.println("IOException: " + e.getLocalizedMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("IOException: " + e.getLocalizedMessage());
            return Collections.emptyList();
        }
       return Collections.emptyList();
    }

    private synchronized List<String> getAvailableLanguageCodes() {
        if (availableLanguageCodes == null) {
            availableLanguageCodes = fetchAvailableLanguageCodes();
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

        if (getAvailableLanguageCodes() == null) {
            return OStrings.getString("MT_ENGINE_MTHUB_INVALID_KEY");
        }

        String encoding = StandardCharsets.UTF_8.name();
        try {
            URL url = new URL(GT_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestProperty("Accept-Charset", encoding);
            conn.setRequestProperty("Content-Type",
                                        "application/json;charset=" + encoding);

            try (OutputStream output = conn.getOutputStream()) {

                List<String> segments = Collections.singletonList(text.substring(0,
                                 Math.min(text.length(), LIMIT_CHARACTER - 1)));

                MTHUBTranslateRequest mttr = new MTHUBTranslateRequest(
                        mthubKey, normaliseCode(sLang), normaliseCode(tLang),
                        segments);

                String json = OM.writeValueAsString(mttr);
                output.write(json.getBytes(encoding));

                int statusCode = conn.getResponseCode();

                try (InputStream in = (statusCode >= 200 && statusCode < 400)
                        ? conn.getInputStream() : conn.getErrorStream();) {
                    MTHUBTranslateResponse response = OM.readValue(
                            IOUtils.toString(in, StandardCharsets.UTF_8),
                            MTHUBTranslateResponse.class);
                    String translation = "";
                    if (response.isSuccess()) {
                        translation = response.getData().
                                      get(0).getSegments().get(0).get(0).
                                        getTranslation();
                        putToCache(sLang, tLang, text, translation);
                    } else {
                        switch (response.getError().get(0).code) {
                            case 1: return OStrings.getString("MTHUB_ERROR",
                                    response.getError().get(0).code,
                                    OStrings.getString("MT_ENGINE_MTHUB_INVALID_KEY"));
                            case 3: return OStrings.getString("MTHUB_ERROR",
                                    response.getError().get(0).code,
                                    OStrings.getString("MT_ENGINE_MTHUB_INVALID_LANG_CODE"));
                            default: return OStrings.getString(
                                        "MTHUB_ERROR", response.getError().get(0).getCode(),
                                        response.getError().get(0).getMessage());
                        }
                    }
                    return translation;
                } catch (Exception e) {
                    return OStrings.getString("MTHUB_ERROR_MESSAGE", e.getMessage());
                }
            } catch (Exception e) {
                return OStrings.getString("MTHUB_ERROR_MESSAGE", e.getMessage());
            }
        } catch (MalformedURLException e) {
            return OStrings.getString("MTHUB_ERROR_MESSAGE", e.getMessage());
        } catch (Exception e) {
            return OStrings.getString("MTHUB_ERROR_MESSAGE", e.getMessage());
        }
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
