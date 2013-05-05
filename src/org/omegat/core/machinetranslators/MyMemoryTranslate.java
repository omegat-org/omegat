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

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.Token;
import org.omegat.util.WikiGet;
import org.w3c.dom.Document; // needed for TMX response only 
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource; // needed for TMX response only 

import org.omegat.core.Core;
import org.omegat.core.matching.LevenshteinDistance;

import javax.xml.parsers.DocumentBuilderFactory; // needed for TMX response only 
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

// import org.json.simple.*; // needed for JSON response only
// import org.json.simple.parser.*;  // needed for JSON response only

/**
 * @author Ibai Lakunza Velasco
 * @author Didier Briel
 * @author Martin Wunderlich
 */
public class MyMemoryTranslate extends BaseTranslate {
    protected static String GT_URL = "http://mymemory.translated.net/api/get?q=";
    protected static String GT_URL2 = "&langpair=#sourceLang#|#targetLang#&of=#format#&mt=0"; // Note: Add parameter &mt=0 to suppress MT results from being included in the TMX response; omit this parameter to include MT results
    protected static String MYMEMORYLABEL_TRANSLATION = "translation";
    protected static String MYMEMORYLABEL_MATCHQUALITYPERCENTAGE = "match";
    protected static String XPATH_QUERY = "child::tuv[starts-with(@lang, '#langCode#')]/seg/text()"; // MyMemory always returns a 4-letter locale code, even when the query contains a language code only; to make sure we get the right matches, only the language code is taken into account
    @Override
    protected String getPreferenceName() {
    	return Preferences.ALLOW_MYMEMORY_TRANSLATE;
    }

    @Override
    public String getName() {
        return OStrings.getString("MT_ENGINE_MYMEMORY");
    }

    /**
     * Modify some country codes to fit with MyMemory
     * 
     * @param language
     *            An OmegaT language
     * @return A code modified for MyMemory languages
     */
    private String mymemoryCode(Language language) {

        String lCode = language.getLanguageCode().toLowerCase();

        return lCode;
    }

    @Override
    protected String translate(Language sLang, Language tLang, String text) throws Exception {
        String bestTranslation;
        String tmxResponse;
        	
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
        
        // Set up Xpath stuff
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xpath = xPathFactory.newXPath();
        String allTUsQuery = "//tu"; 
        
        // Get all TUs
        XPathExpression expr = xpath.compile(allTUsQuery);
        Object result = expr.evaluate(document, XPathConstants.NODESET);
        NodeList allTUs = (NodeList) result;     
        
        bestTranslation = getBestTranslation(sLang, tLang, text, xpath, allTUs);

        return bestTranslation; 
    }

	/**
	 * @param sLang
	 * @param tLang
	 * @param text
	 * @param xpath
	 * @param allTUs
	 * @return
	 * @throws XPathExpressionException
	 */
	private String getBestTranslation(Language sLang, Language tLang, String text, XPath xpath, NodeList allTUs) throws XPathExpressionException {
			int lowestEditDistance = 999999; 
            int dist = 0; 
            Node tu = null;
            String sourceSeg = "";
            String targetSeg = "";
            String targetSegQueryString = XPATH_QUERY.replace("#langCode#", tLang.getLanguageCode());
            String sourceSegQueryString = XPATH_QUERY.replace("#langCode#", tLang.getLanguageCode());
            
            String bestTranslation = "";
        
            // Loop over TUs to get best matching source segment and its translation
            for (int i = 0; i < allTUs.getLength(); i++) {
                tu = allTUs.item(i);
  
                sourceSeg = xpath.evaluate(sourceSegQueryString, tu);
                targetSeg = xpath.evaluate(targetSegQueryString, tu);
                
                dist = getLevensteinDistance(text, sourceSeg);

                if( dist < lowestEditDistance && !sourceSeg.isEmpty() && !targetSeg.isEmpty() ) {
                    lowestEditDistance = dist;
                    bestTranslation = targetSeg; 
                }        

                if( dist == 0 ) {
                    break; // Can't find a better match than this one, so let's stop the loop here. 
                } 
            }
            
            bestTranslation = cleanUpText(bestTranslation);
            
            return bestTranslation;
	}

	private String cleanUpText(String str) {
	       str = str.replace("&quot;", "\"");
	       str = str.replace("&nbsp;", "\u00A0");
	       str = str.replace("&amp;", "&");
	       str = str.replace("&apos;", "'");
	       
		return str;
	}

	/**
	 * @param text
	 * @param sourceSeg
	 * @return
	 */
	private int getLevensteinDistance(String text, String sourceSeg) {
            int dist;
            LevenshteinDistance leven = new LevenshteinDistance(); 
            ITokenizer srcTokenizer = Core.getProject().getSourceTokenizer();
            
//            String[] txtWords = text.split(" "); // TODO: Decide which tokenization method to use; the commented-out lines return different results. 
//            String[] srcWords = sourceSeg.split(" "); 
//            Token[] textTokenArray = convertStringArrayToTokenArray(txtWords);
//            Token[] sourceSegTokenArray = convertStringArrayToTokenArray(srcWords);
            Token[] textTokenArray = srcTokenizer.tokenizeAllExactly(text);
            Token[] sourceSegTokenArray = srcTokenizer.tokenizeAllExactly(sourceSeg);
            
            dist = leven.compute(textTokenArray, sourceSegTokenArray);
            return dist;
	}
    
//	private Token[] convertStringArrayToTokenArray(String[] words) {
//		Token[] emptyArrayForConversion = {}; 				// a slightly weird work-around due to Java limitations when converting a typed array
//		List <Token> tokenList = new ArrayList<Token>();
//		int i = 0; 
//		
//		for( String w : words ) 
//		{
//			Token myToken = new Token(w, i);
//			tokenList.add(myToken);
//			i++;
//		}
//		
//		return (Token[]) tokenList.toArray(emptyArrayForConversion);
//	}

	private String getMyMemoryResponse(Language sLang, Language tLang, String text, String format)
	throws UnsupportedEncodingException, IOException {
            // Build URL for the JSON query
            String sourceLang = mymemoryCode(sLang);
            String targetLang = mymemoryCode(tLang);
            String url2 = GT_URL2.replace("#sourceLang#", sourceLang).replace("#targetLang#", targetLang).replace("#format#", format);
            String url = GT_URL + URLEncoder.encode(text, "UTF-8") + url2;

            // Get the results from MyMemory
            String myMemoryResponse = "";
            try {
                myMemoryResponse = WikiGet.getURL(url);
            } catch (IOException e) {
                throw e;
            }
            
            // System.out.println(myMemoryResponse);
            
            return myMemoryResponse;
	}
    
    // Note: This method is not used, because it requires the JSON Simple library, which is under Apache license (http://code.google.com/p/json-simple/). 
    // Apache license is not compatible with GPLv2 strict, which OmegaT has to use, because it is linked with JAXB which is GPLv2 strict.
	// The method is kep here in case the license compatibility changes at some point in the future. 
    /*
    protected String translateUsingJSON(Language sLang, Language tLang, String text) throws Exception {
    	// Get JSON response
        String jsonRepsonse = "";
        
        try {
        	jsonRepsonse = getMyMemoryResponse(sLang, tLang, text, "json");
        }
        catch(Exception e)
        {
        	return e.getLocalizedMessage();
        }
        
        // Parse the raw response string to JSON-Simple objects
        JSONParser parser=new JSONParser();
        Object obj=parser.parse(jsonRepsonse);
        JSONObject jo = (JSONObject)obj; 
        JSONObject match = new JSONObject();        
        JSONArray matches = (JSONArray) jo.get("matches");
        
        // Loop over the results and determine the best match
        
        // Note: The "match" value of the JSON response is a percentage of how well the query string matches the returned source string. The response also contains a 
        // "quality" value, which is a rating of the translation that some user assigned via the website interface. A lot of segments are not rated, but this 
        // doesn't mean they are of low quality. The "quality" value is not used here. The target segment is merely selected based on highest match percentage. 
        
        String bestTranslation = ""; 
        Object matchP = null; 
        double matchPercentage = 0.0; 
        double bestMatchQuality = 0.0; 
        String translation = "";
        @SuppressWarnings("unchecked")
		Iterator<JSONObject> it = matches.iterator();
        
        while(it.hasNext())
        {
        	match = it.next();
        	matchP = match.get(MYMEMORYLABEL_MATCHQUALITYPERCENTAGE);
        	
        	// Get match percentage value (see note above)
        	if(matchP.toString().equalsIgnoreCase("1")) // special case where the object type is Long instead of Double
        		matchPercentage = 1.0;
        	else if(matchP.toString().equalsIgnoreCase("0")) // special case where the object type is Long instead of Double
        		matchPercentage = 0.0;
        	else
        		matchPercentage = (Double) matchP;
        	
        	// If this match is better than what we've had before, get the translation. 
       		translation = (String) match.get(MYMEMORYLABEL_TRANSLATION);
       		if( matchPercentage > bestMatchQuality)
        	{
        		bestMatchQuality = matchPercentage;
            	bestTranslation = translation; 
        	}
        }
        	
        return bestTranslation;
    }
	
	*/
}
