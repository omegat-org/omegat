/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Ibai Lakunza Velasco, Didier Briel
               2013 Martin Wunderlich
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

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource; 

import javax.xml.parsers.DocumentBuilderFactory; 
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;


/**
 * @author Ibai Lakunza Velasco
 * @author Didier Briel
 * @author Martin Wunderlich
 */
public class MyMemoryMachineTranslate extends AbstractMyMemoryTranslate {
    protected static String GT_URL2 = "&langpair=#sourceLang#|#targetLang#&of=#format#&mt=1"; // Note: Add parameter &mt=0 to suppress MT results from being included in the TMX response; omit this parameter to include MT results
    
    @Override
    protected String getPreferenceName() {
    	return Preferences.ALLOW_MYMEMORY_MACHINE_TRANSLATE;
    }

    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_MYMEMORY_MACHINE");
    }
    
    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String tmxResponse = "";
        String machineTranslationMatch = ""; 
        
        // Get MyMemory response in TMX format
        try {
            tmxResponse = getMyMemoryResponse(sLang, tLang, text, "tmx");
        }
        catch(Exception e)
        {
            return e.getLocalizedMessage();
        }
        
        // Adjust DTD location and bug in entity encoding; the second line should be removed as soon as the bug is 
        // fixed by MyMemory; TODO: Use local DTD
        tmxResponse = tmxResponse.replace("<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">", "");
        tmxResponse = tmxResponse.replace("&", "&amp;");
        
        // Build DOM object from the returned XML string
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        InputSource source = new InputSource(new StringReader(tmxResponse));
        Document document = factory.newDocumentBuilder().parse(source);
        
        // Extract MT response and remove MT TU from XML
        String targetLangCode =  tLang.getLocaleCode().replace('_', '-').toUpperCase(); // MyMemory is using hyphens instead of underscores and uppercase codes in 4-letter locale-codes
        machineTranslationMatch = extractMTresponse(document, targetLangCode);
        
        return machineTranslationMatch; 
    }
    
    private String extractMTresponse(Document document, String targetLang) {
        String MTresponse = "";
        String MTQuery = String.format("/tmx/body/tu[@creationid='MT!']/tuv[@lang='%s']/seg/text()", targetLang); 
        Object result = null; 
        
		XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        
        try {
	        XPathExpression expr = xpath.compile(MTQuery);
	        result = expr.evaluate(document, XPathConstants.NODE);
	        System.out.println(result);
	        
	        Node node = (Node)result;
	        MTresponse = node.getTextContent();
        }
        catch(Exception ex) {       	
        	// silently catch the exception and provide details to user
        	MTresponse = "Error extracting MT result from MyMemory response: " + ex.getLocalizedMessage();
        }
        
		return MTresponse;
	}

	  
	/**
	 * Builds the URL for the XML query
	 */
	protected String buildMyMemoryUrl(Language sLang, Language tLang, String text, String format) throws UnsupportedEncodingException {
    	String sourceLang = mymemoryCode(sLang);
    	String targetLang = mymemoryCode(tLang);
    	String url2 = GT_URL2.replace("#sourceLang#", sourceLang).replace("#targetLang#", targetLang).replace("#format#", format);
    	String url = GT_URL + URLEncoder.encode(text, "UTF-8") + url2;
    	
    	return url;
	}   
}
