/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.omegat.core.machinetranslators;
import java.awt.Window;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader; 
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;  
import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import org.omegat.gui.exttrans.MTConfigDialog;
import org.omegat.util.JsonParser;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OStrings;   
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author prompsit
 */
public class IADAATPATranslate extends BaseTranslate{
    private static final String PROPERTY_API_KEY = "IADAATPA.api.key";

    protected static final String GT_URL = "https://app.iadaatpa.eu/api/translate";

    protected static final int LIMIT_CHARACTER = 1000;
    
    private ArrayList<String> AVAILABLE_LANGUAGE_CODES = new ArrayList<String>();
 
    private void setAvailableLanguageCodes() {
       
       String IADAATPAKey = getCredential(PROPERTY_API_KEY);

       URLConnection connection = null;
       String codesIADAATPA = "";
       try { 
             
            URL iadaatpa = new URL("https://app.iadaatpa.eu/api/describelanguages/"+IADAATPAKey);
            connection = iadaatpa.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                                    connection.getInputStream()));
            String inputLine;
            
            while ((inputLine = in.readLine()) != null) 
                codesIADAATPA += inputLine;
            in.close();
            this.AVAILABLE_LANGUAGE_CODES = getJsonCodes(codesIADAATPA);            
            
        }catch (Exception e){
                System.err.println("IOException: " + e);            
        }
    }

    private ArrayList<String> getAvailableLanguageCodes() {
        return AVAILABLE_LANGUAGE_CODES;
    }    
    
    @Override
    protected String getPreferenceName() {
       return Preferences.ALLOW_IADAATPA_TRANSLATE;
    }
    
    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_IADAATPA");
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
        dialog.panel.valueLabel1.setText(OStrings.getString("MT_ENGINE_IADAATPA_API_KEY_LABEL"));
        dialog.panel.valueField1.setText(getCredential(PROPERTY_API_KEY));
        dialog.panel.valueLabel2.setVisible(false);
        dialog.panel.valueField2.setVisible(false);
        dialog.panel.temporaryCheckBox.setSelected(isCredentialStoredTemporarily(PROPERTY_API_KEY));
        dialog.show();
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        if(this.AVAILABLE_LANGUAGE_CODES.isEmpty())
        {
         this.setAvailableLanguageCodes();
        }        
                
        String IADAATPAKey = getCredential(PROPERTY_API_KEY);
        if (IADAATPAKey == null || IADAATPAKey.isEmpty()) {  
            return OStrings.getString("MT_ENGINE_IADAATPA_KEY_NOTFOUND");
        }
         
        String prev = getFromCache(sLang, tLang, text);
        if (prev != null) {
            return prev;   
        }

        Map<String, Object> config = new HashMap<String, Object>();
        config.put("javax.json.stream.JsonGenerator.prettyPrinting", Boolean.valueOf(true));

        JsonBuilderFactory factory = Json.createBuilderFactory(config);
        JsonObject request = factory.createObjectBuilder()
          .add("token", IADAATPAKey)
          .add("source", getNormaliseCode(sLang))
          .add("target", getNormaliseCode(tLang))          
          //.add("domain", "")
          .add("segments", factory.createArrayBuilder()
                  .add(text.substring(0, Math.min(text.length(), LIMIT_CHARACTER-1)))
          )
         .build();
        // Get the results from IADAATPA
        URLConnection connection = null;
        
        String inputLine = "";
        try { 
              
            String charset = "UTF-8";
            
            connection = (HttpURLConnection) new URL(GT_URL).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Accept-Charset", charset);
            connection.setRequestProperty("Content-Type", "application/json ;charset=" + charset);
            
            OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(),charset);
            writer.write(request.toString());
            writer.flush();
            String line;   
            StringBuilder response = new StringBuilder("");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(),charset));
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            writer.close();
            reader.close();
            String tr = getJsonResults(response.toString());
           
            putToCache(sLang, tLang, text, tr);
            return tr;

        }catch (MalformedURLException e){
            return StringUtil.format(OStrings.getString("IADAATPA_ERROR"), e.getMessage());
        }catch (IOException e){
            System.err.println("IOException: " + e);
         
            InputStream error = ((HttpURLConnection) connection).getErrorStream();

            try {
                int data = error.read();
                while (data != -1) {                   
                    inputLine = inputLine + (char)data;
                    data = error.read();
                }
                error.close();
                
                return this.getJsonResults(inputLine);//dos
                
            } catch (Exception ex) {
                try {
                    if (error != null) {
                        error.close();
                    }
                } catch (Exception e2) {
                    return StringUtil.format(OStrings.getString("IADAATPA_ERROR"), e2.getMessage());
                }
            }return StringUtil.format(OStrings.getString("IADAATPA_ERROR"), e.getMessage());
        }catch (Exception e){
            return StringUtil.format(OStrings.getString("IADAATPA_ERROR"), e.getMessage());
        }   
    }
    
    @SuppressWarnings("unchecked")
    protected String getJsonResults(String json) {
        Map<String, Object> rootNode;
        try {
            rootNode = (Map<String, Object>) JsonParser.parse(json);
        } catch (Exception e) {
            Log.logErrorRB(e, "MT_JSON_ERROR");
            return OStrings.getString("MT_JSON_ERROR");
        }
         
        String tr = "";
        
        if (rootNode.containsKey("success") && (Boolean)rootNode.get("success")==true
            && rootNode.containsKey("error") && rootNode.get("error")==null &&
               rootNode.containsKey("data") 
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
            Map<String, Object> error=(Map<String, Object>) rootNode.get("error");
            switch ((int)error.get("statusCode")) {
            case 401:
                     tr=StringUtil.format(OStrings.getString("IADAATPA_ERROR"), (int)error.get("code"), OStrings.getString("MT_ENGINE_IADAATPA_INVALID_KEY"));
                     break;
            case 400:
                    switch ((int)error.get("code")) {
                        case 4:
                            tr=StringUtil.format(OStrings.getString("IADAATPA_ERROR"), (int)error.get("code"), OStrings.getString("MT_ENGINE_IADAATPA_INVALID_DOMAIN"));
                            break;
                        case 11:
                            tr=StringUtil.format(OStrings.getString("IADAATPA_ERROR"), (int)error.get("code"), OStrings.getString("MT_ENGINE_IADAATPA_MISSING_KEY"));
                            break;
                        case 17:
                            tr=StringUtil.format(OStrings.getString("IADAATPA_ERROR"), (int)error.get("code"), OStrings.getString("MT_ENGINE_IADAATPA_MISSING_SEGMENTS"));
                            break;                         
                        default:
                            tr=StringUtil.format(OStrings.getString("IADAATPA_ERROR"), (int)error.get("code"), OStrings.getString("MT_ENGINE_IADAATPA_MISSING_PARAMETERS"));
                            break;
                    }
                    break;
            case 500:
                switch ((int)error.get("code")) {
                        case 19:
                            tr=StringUtil.format(OStrings.getString("IADAATPA_ERROR"), (int)error.get("code"), OStrings.getString("MT_ENGINE_IADAATPA_INVALID_LANG_CODE"));
                            break;                                             
                        default:
                            tr=StringUtil.format(OStrings.getString("IADAATPA_ERROR"), (int)error.get("code"), OStrings.getString("MT_ENGINE_IADAATPA_MISSING_PARAMETERS"));
                            break;
                    }
                    break;                
            default:
                    tr=StringUtil.format(OStrings.getString("IADAATPA_ERROR"), 1, (String)error.get("message"));
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
            if (rootNode.containsKey("success") && (Boolean)rootNode.get("success")==true
            && rootNode.containsKey("error") && rootNode.get("error")==null &&
               rootNode.containsKey("data") 
           ) { 
                Map<String, Object> dataNode = (Map<String, Object>) rootNode.get("data");
                List<Map<String, String>> languagesList = (List<Map<String, String>>) dataNode.get("languages");

                for (Map<String, String> o : languagesList){
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
     * @return A normalise code for IADAATPA languages (ISO 639-1 Code)
     */
    private String getNormaliseCode(Language language) { 
        
        String lCode = language.getLanguage();
        if(!this.AVAILABLE_LANGUAGE_CODES.isEmpty() && !this.AVAILABLE_LANGUAGE_CODES.contains(language.getLanguage()))
        {
            lCode = language.getLanguageCode();
        }      
                       
        return lCode;
    }
          
}
