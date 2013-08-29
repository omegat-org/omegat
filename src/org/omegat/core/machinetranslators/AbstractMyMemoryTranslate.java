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
import java.io.UnsupportedEncodingException;

import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.Language;
import org.omegat.util.Token;
import org.omegat.util.WikiGet;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.omegat.core.Core;
import org.omegat.core.matching.LevenshteinDistance;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;


/**
 * @author Ibai Lakunza Velasco
 * @author Didier Briel
 * @author Martin Wunderlich
 */
public abstract class AbstractMyMemoryTranslate extends BaseTranslate {
    protected static String GT_URL = "http://mymemory.translated.net/api/get?q=";
    protected static String MYMEMORYLABEL_TRANSLATION = "translation";
    protected static String MYMEMORYLABEL_MATCHQUALITYPERCENTAGE = "match";
    protected static String XPATH_QUERY = "child::tuv[starts-with(@lang, '#langCode#')]/seg/text()"; // MyMemory always returns a 4-letter locale code, even when the query contains a language code only; to make sure we get the right matches, only the language code is taken into account
    
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

        String lCode = language.getLanguageCode().toLowerCase();

        return lCode;
    }

    @Override
    protected abstract String translate(Language sLang, Language tLang, String text) throws Exception;

	
	/**
	 * @param sLang
	 * @param tLang
	 * @param text
	 * @param xpath
	 * @param allTUs
	 * @return
	 * @throws XPathExpressionException
	 */
	protected String getBestTranslation(Language sLang, Language tLang, String text, XPath xpath, NodeList allTUs) throws XPathExpressionException {
			int lowestEditDistance = 999999; 
            int dist = 0; 
            Node tu = null;
            String sourceSeg = "";
            String targetSeg = "";
            String targetSegQueryString = XPATH_QUERY.replace("#langCode#", tLang.getLanguageCode());
            String sourceSegQueryString = XPATH_QUERY.replace("#langCode#", sLang.getLanguageCode());
            
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

	protected String cleanUpText(String str) {
	       str = str.replace("&quot;", "\"");
	       str = str.replace("&nbsp;", "\u00A0");
	       str = str.replace("&amp;", "&");
	       str = str.replace("&apos;", "'");
	       str = str.replace("&#39;", "'");
	       str = str.replace("&lt;", "<");
	       str = str.replace("&gt;", ">");
	       str = str.trim();
	       
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

            Token[] textTokenArray = srcTokenizer.tokenizeAllExactly(text);
            Token[] sourceSegTokenArray = srcTokenizer.tokenizeAllExactly(sourceSeg);
            
            dist = leven.compute(textTokenArray, sourceSegTokenArray);
            return dist;
	}
    

	protected String getMyMemoryResponse(Language sLang, Language tLang, String text, String format) throws UnsupportedEncodingException, IOException {
        String url = buildMyMemoryUrl(sLang, tLang, text, format);

        // Get the results from MyMemory
        String myMemoryResponse = "";
        try {
            myMemoryResponse = WikiGet.getURL(url);
        } catch (IOException e) {
            throw e;
        }
        
        return myMemoryResponse;
	}

	/**
	 * @param sLang
	 * @param tLang
	 * @param text
	 * @param format
	 * @return
	 * @throws UnsupportedEncodingException
	 * 
	 * This method must be overriden in the concrete implementations to adjust the query to include or exclude MT results
	 */
	protected abstract String buildMyMemoryUrl(Language sLang, Language tLang, String text, String format) throws UnsupportedEncodingException;
    
}
