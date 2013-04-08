/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Thomas Cordonnier, Aaron Madlon-Kay
               2013 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/
package org.omegat.gui.matches;

import org.omegat.util.VarExpansion;
import org.omegat.core.matching.DiffDriver;
import org.omegat.core.matching.DiffDriver.TextRun;
import org.omegat.core.matching.DiffDriver.Render;
import org.omegat.core.matching.NearString;

import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.text.DateFormat;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.omegat.core.Core;
import org.omegat.util.OStrings;

/**
 * This class is used to convert a NearString to a text visible in the MatchesTextArea
 * according to the given template containing variables.
 * 
 * @author Thomas CORDONNIER
 * @author Aaron Madlon-Kay
 */
public class MatchesVarExpansion extends VarExpansion<NearString> {
    
    // ------------------------------ definitions -------------------
    
    public static final String VAR_ID = "${id}";
    public static final String VAR_SCORE_BASE = "${score}";
    public static final String VAR_SCORE_NOSTEM = "${noStemScore}";
    public static final String VAR_SCORE_ADJUSTED = "${adjustedScore}";
    public static final String VAR_CREATION_ID = "${creationId}";
    public static final String VAR_CREATION_DATE = "${creationDate}";
    public static final String VAR_FUZZY_FLAG = "${fuzzyFlag}";
    public static final String VAR_DIFF = "${diff}";
    
    
    public static final String[] MATCHES_VARIABLES = {
        VAR_ID, 
        VAR_SOURCE_TEXT,
        VAR_DIFF,
        VAR_TARGET_TEXT, 
        VAR_SCORE_BASE, VAR_SCORE_NOSTEM, VAR_SCORE_ADJUSTED,
        VAR_FILE_NAME_ONLY, VAR_FILE_PATH, VAR_FILE_SHORT_PATH,
        VAR_CREATION_ID, VAR_CREATION_DATE, VAR_FUZZY_FLAG
    };
    
    public static final String DEFAULT_TEMPLATE = VAR_ID + ") " 
			+ VAR_FUZZY_FLAG
            + VAR_SOURCE_TEXT + "\n"
            + VAR_TARGET_TEXT + "\n"
            + "<" + VAR_SCORE_BASE + "/" 
            + VAR_SCORE_NOSTEM + "/"
            + VAR_SCORE_ADJUSTED + "% "
            + VAR_FILE_PATH + ">";
    
    public static final Pattern patternSingleProperty = Pattern.compile("@\\{(.+?)\\}");
    public static final Pattern patternPropertyGroup = Pattern.compile("@\\[(.+?)\\]\\[(.+?)\\]\\[(.+?)\\]"); 
    
    private static Replacer sourceTextReplacer = new Replacer() {
        public void replace(Result R, NearString match) {
            R.sourcePos = R.text.indexOf(VAR_SOURCE_TEXT);
            R.text = R.text.replace(VAR_SOURCE_TEXT, match.source);
        }
    };
    
    private static Replacer diffReplacer = new Replacer() {
        public void replace(Result R, NearString match) {
            R.diffPos = R.text.indexOf(VAR_DIFF);
            if (R.diffPos != -1) {
                Render diffRender = DiffDriver.render(match.source, Core.getEditor().getCurrentEntry().getSrcText());
                R.diffInfo = diffRender.formatting;
                R.text = R.text.replace(VAR_DIFF, diffRender.text);
            }
        }
    };
    
    // ------------------------------ subclasses -------------------
    
    /** Class to store formatted text and indications for other treatments **/
    public static class Result {
        public String text; 
        public int sourcePos;
	public List<TextRun> diffInfo;
	public int diffPos;
    }
    
    /** A simple interface for making anonymous functions that perform string replacements. */
    private interface Replacer {
        public void replace(Result R, NearString match);
    }

    // ------------------------------ non-static part -------------------

    /** A sorted map that ensures styled replacements are performed in the order of appearance. */
    private Map<Integer, Replacer> styledComponents = new TreeMap<Integer, Replacer>();

    public MatchesVarExpansion (String template) {
        super(template);
        
    }
        
    /**
     * Replace property calls by the corresponding value <br>
     * Format : @{PropertyName} 
     *      in this case, retreive only the property value, name is elsewhere. <br>
     * Format : @[Property name with *][separator 1][separator2] 
     *      in this case, return all properties matching the 1st pattern,
     *      as key=value pairs where = is replaced by separator1 and use separator2 between entries.<br>
     * Expression \n for new line is accepted in separators. 
     * @param localTemplate  Initial template
     * @param props Map of properties
     * @return Expanded template
     */
    public String expandProperties (String localTemplate, Map<String,String> props) {
        Matcher matcher;
        while ((matcher = patternSingleProperty.matcher(localTemplate)).find()) {
            String value = props.get(matcher.group(1));
            localTemplate = localTemplate.replace (matcher.group(), value == null ? "" : value);
        }
        while ((matcher = patternPropertyGroup.matcher(localTemplate)).find()) {
            String patternStr = matcher.group(1), separator1 = matcher.group(2), separator2 = matcher.group(3);
            separator1 = separator1.replace("\\n","\n"); 
            separator2 = separator2.replace("\\n","\n");
            Pattern pattern = Pattern.compile(patternStr.replace("*","(.*)").replace("?","(.)"));
            StringBuilder res = new StringBuilder();
            for (Map.Entry<String, String> me: props.entrySet()) {
                if (pattern.matcher(me.getKey().toString()).matches())
                    res.append(me.getKey()).append(separator1).append(me.getValue()).append(separator2);
            }
            if (res.toString().endsWith(separator2))
                res.replace(res.toString().lastIndexOf(separator2), res.length(), "");
            localTemplate = localTemplate.replace(matcher.group(), res.toString());
        }        
        return localTemplate;        
    }
    
    @Override
    public String expandVariables (NearString match) {
        String localTemplate = this.template; // do not modify template directly, so that we can reuse for another change
        localTemplate = localTemplate.replace(VAR_CREATION_ID, match.creator == null ? "" : match.creator);
        if (match.creationDate > 0)
            localTemplate = localTemplate.replace(VAR_CREATION_DATE, DateFormat.getInstance().format(new Date (match.creationDate)));
        else
            localTemplate = localTemplate.replace(VAR_CREATION_DATE, "");
        localTemplate = localTemplate.replace(VAR_SCORE_BASE, Integer.toString(match.score));
        localTemplate = localTemplate.replace(VAR_SCORE_NOSTEM, Integer.toString(match.scoreNoStem));
        localTemplate = localTemplate.replace(VAR_SCORE_ADJUSTED, Integer.toString(match.adjustedScore));
        localTemplate = localTemplate.replace(VAR_TARGET_TEXT, match.translation);
		localTemplate = localTemplate.replace(VAR_FUZZY_FLAG, match.fuzzyMark ? (OStrings.getString("MATCHES_FUZZY_MARK") + " ") : "");
        localTemplate = expandFileName(localTemplate, match.proj, Core.getProject().getProjectProperties().getTMRoot());
        
        return localTemplate;
    }
    
    public Result apply(NearString match, int id) {
        Result R = new Result();
        styledComponents.clear();
        
        // Variables
        R.text = this.expandVariables(match);
        R.text = R.text.replace(VAR_ID, Integer.toString(id));

        // Properties (<prop type='xxx'>value</prop>)
        if (match.props != null) {
            R.text = expandProperties(R.text, match.props);
        } else {
            R.text = R.text.replaceAll(patternSingleProperty.pattern(), "");
            R.text = R.text.replaceAll(patternPropertyGroup.pattern(), "");            
        }

        styledComponents.put(R.text.indexOf(VAR_SOURCE_TEXT), sourceTextReplacer);
        styledComponents.put(R.text.indexOf(VAR_DIFF), diffReplacer);

        for (Entry<Integer, Replacer> e : styledComponents.entrySet()) {
            e.getValue().replace(R, match);
        }
        
        return R;
    }
}
