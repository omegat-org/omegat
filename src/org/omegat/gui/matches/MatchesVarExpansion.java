/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Thomas Cordonnier, Aaron Madlon-Kay
               2013-2014 Aaron Madlon-Kay
               2014 Alex Buloichik
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
package org.omegat.gui.matches;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.core.data.ExternalTMFactory;
import org.omegat.core.data.ProjectProperties;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.matching.DiffDriver;
import org.omegat.core.matching.DiffDriver.Render;
import org.omegat.core.matching.DiffDriver.TextRun;
import org.omegat.core.matching.NearString;
import org.omegat.util.OStrings;
import org.omegat.util.TMXProp;
import org.omegat.util.VarExpansion;

/**
 * This class is used to convert a NearString to a text visible in the
 * MatchesTextArea according to the given template containing variables.
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
    /**
     * For backwards compatibility, this variable is an alias for
     * {@link #VAR_CHANGED_ID}. For the actual creation ID, use
     * {@link #VAR_INITIAL_CREATION_ID}.
     */
    @Deprecated
    public static final String VAR_CREATION_ID = "${creationId}";
    /**
     * For backwards compatibility, this variable is an alias for
     * {@link #VAR_CHANGED_DATE}. For the actual creation date, use
     * {@link #VAR_INITIAL_CREATION_DATE}.
     */
    @Deprecated
    public static final String VAR_CREATION_DATE = "${creationDate}";
    public static final String VAR_INITIAL_CREATION_ID = "${initialCreationId}";
    public static final String VAR_INITIAL_CREATION_DATE = "${initialCreationDate}";
    public static final String VAR_CHANGED_ID = "${changedId}";
    public static final String VAR_CHANGED_DATE = "${changedDate}";
    public static final String VAR_FUZZY_FLAG = "${fuzzyFlag}";
    public static final String VAR_DIFF = "${diff}";
    public static final String VAR_DIFF_REVERSED = "${diffReversed}";
    public static final String VAR_SOURCE_LANGUAGE = "${sourceLanguage}";
    public static final String VAR_TARGET_LANGUAGE = "${targetLanguage}";

    private static final String[] MATCHES_VARIABLES = { VAR_ID, VAR_SOURCE_TEXT, VAR_DIFF, VAR_DIFF_REVERSED,
            VAR_TARGET_TEXT, VAR_SCORE_BASE, VAR_SCORE_NOSTEM, VAR_SCORE_ADJUSTED, VAR_FILE_NAME_ONLY,
            VAR_FILE_PATH, VAR_FILE_SHORT_PATH, VAR_INITIAL_CREATION_ID, VAR_INITIAL_CREATION_DATE,
            VAR_CHANGED_ID, VAR_CHANGED_DATE, VAR_FUZZY_FLAG, VAR_SOURCE_LANGUAGE, VAR_TARGET_LANGUAGE };

    public static List<String> getMatchesVariables() {
        return Collections.unmodifiableList(Arrays.asList(MATCHES_VARIABLES));
    }

    public static final String DEFAULT_TEMPLATE = VAR_ID + ". " + VAR_FUZZY_FLAG + VAR_SOURCE_TEXT + "\n"
            + VAR_TARGET_TEXT + "\n" + "<" + VAR_SCORE_BASE + "/" + VAR_SCORE_NOSTEM + "/"
            + VAR_SCORE_ADJUSTED + "% " + VAR_FILE_PATH + ">";

    public static final Pattern PATTERN_SINGLE_PROPERTY = Pattern.compile("@\\{(.+?)\\}");
    public static final Pattern PATTERN_PROPERTY_GROUP = Pattern
            .compile("@\\[(.+?)\\]\\[(.+?)\\]\\[(.+?)\\]");

    private static final Replacer SOURCE_TEXT_REPLACER = (r, match) -> {
        r.sourcePos = r.text.indexOf(VAR_SOURCE_TEXT);
        r.text = r.text.replace(VAR_SOURCE_TEXT, match.source);
    };

    private static final Replacer DIFF_REPLACER = (r, match) -> {
        int diffPos = r.text.indexOf(VAR_DIFF);
        SourceTextEntry ste = Core.getEditor().getCurrentEntry();
        if (diffPos != -1 && ste != null) {
            Render diffRender = DiffDriver.render(match.source, ste.getSrcText(), true);
            r.diffInfo.put(diffPos, diffRender.formatting);
            if (diffRender.text != null) {
                r.text = r.text.replace(VAR_DIFF, diffRender.text);
            }
        }
    };

    private static final Replacer DIFF_REVERSED_REPLACER = (r, match) -> {
        int diffPos = r.text.indexOf(VAR_DIFF_REVERSED);
        SourceTextEntry ste = Core.getEditor().getCurrentEntry();
        if (diffPos != -1 && ste != null) {
            Render diffRender = DiffDriver.render(ste.getSrcText(), match.source, true);
            r.diffInfo.put(diffPos, diffRender.formatting);
            if (diffRender.text != null) {
                r.text = r.text.replace(VAR_DIFF_REVERSED, diffRender.text);
            }
        }
    };

    // ------------------------------ subclasses -------------------

    /** Class to store formatted text and indications for other treatments **/
    public static class Result {
        public String text = null;
        public int sourcePos = -1;
        public final Map<Integer, List<TextRun>> diffInfo = new HashMap<>();
    }

    /**
     * A simple interface for making anonymous functions that perform string
     * replacements.
     */
    private interface Replacer {
        void replace(Result r, NearString match);
    }

    // ------------------------------ non-static part -------------------

    /**
     * A sorted map that ensures styled replacements are performed in the order
     * of appearance.
     */
    private Map<Integer, Replacer> styledComponents = new TreeMap<>();

    public MatchesVarExpansion(String template) {
        super(template);

    }

    /**
     * Replace property calls by the corresponding value <br>
     * Format : @{PropertyName} in this case, retreive only the property value,
     * name is elsewhere. <br>
     * Format : @[Property name with *][separator 1][separator2] in this case,
     * return all properties matching the 1st pattern, as key=value pairs where
     * = is replaced by separator1 and use separator2 between entries.<br>
     * Expression \n for new line is accepted in separators.
     * 
     * @param localTemplate
     *            Initial template
     * @param props
     *            Map of properties
     * @return Expanded template
     */
    public String expandProperties(String localTemplate, List<TMXProp> props) {
        Matcher matcher;
        while ((matcher = PATTERN_SINGLE_PROPERTY.matcher(localTemplate)).find()) {
            String value = getPropValue(props, matcher.group(1));
            localTemplate = localTemplate.replace(matcher.group(), value == null ? "" : value);
        }
        while ((matcher = PATTERN_PROPERTY_GROUP.matcher(localTemplate)).find()) {
            String patternStr = matcher.group(1);
            String separator1 = matcher.group(2);
            String separator2 = matcher.group(3);
            separator1 = separator1.replace("\\n", "\n");
            separator2 = separator2.replace("\\n", "\n");
            Pattern pattern = Pattern.compile(patternStr.replace("*", "(.*)").replace("?", "(.)"));
            StringBuilder res = new StringBuilder();
            for (TMXProp me : props) {
                if (pattern.matcher(me.getType()).matches()) {
                    res.append(me.getType()).append(separator1).append(me.getValue()).append(separator2);
                }
            }
            if (res.toString().endsWith(separator2)) {
                res.replace(res.toString().lastIndexOf(separator2), res.length(), "");
            }
            localTemplate = localTemplate.replace(matcher.group(), res.toString());
        }
        return localTemplate;
    }

    private String getPropValue(List<TMXProp> props, String type) {
        for (TMXProp entry : props) {
            if (type.equals(entry.getType())) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public String expandVariables(NearString match) {
        // do not modify template directly, so that we can reuse for another
        // change
        String localTemplate = this.template;
        localTemplate = localTemplate.replace(VAR_INITIAL_CREATION_ID,
                match.creator == null ? "" : match.creator);
        // VAR_CREATION_ID is an alias for VAR_CHANGED_ID, for backwards
        // compatibility.
        for (String s : new String[] { VAR_CHANGED_ID, VAR_CREATION_ID }) {
            localTemplate = localTemplate.replace(s, match.changer == null ? "" : match.changer);
        }
        if (match.creationDate > 0) {
            localTemplate = localTemplate.replace(VAR_INITIAL_CREATION_DATE,
                    DateFormat.getInstance().format(new Date(match.creationDate)));
        } else {
            localTemplate = localTemplate.replace(VAR_INITIAL_CREATION_DATE, "");
        }
        // VAR_CREATION_DATE is an alias for VAR_CHANGED_DATE, for backwards
        // compatibility.
        for (String s : new String[] { VAR_CHANGED_DATE, VAR_CREATION_DATE }) {
            if (match.changedDate > 0) {
                localTemplate = localTemplate.replace(s,
                        DateFormat.getInstance().format(new Date(match.changedDate)));
            } else {
                localTemplate = localTemplate.replace(s, "");
            }
        }
        localTemplate = localTemplate.replace(VAR_SCORE_BASE, Integer.toString(match.scores[0].score));
        localTemplate = localTemplate.replace(VAR_SCORE_NOSTEM,
                Integer.toString(match.scores[0].scoreNoStem));
        localTemplate = localTemplate.replace(VAR_SCORE_ADJUSTED,
                Integer.toString(match.scores[0].adjustedScore));
        localTemplate = localTemplate.replace(VAR_TARGET_TEXT, match.translation);
        localTemplate = localTemplate.replace(VAR_FUZZY_FLAG,
                match.fuzzyMark ? (OStrings.getString("MATCHES_FUZZY_MARK") + " ") : "");

        if (match.props != null) {
            for (TMXProp prop : match.props) {
                if (prop.getType().equals(ExternalTMFactory.TMXLoader.PROP_SOURCE_LANGUAGE)) {
                    localTemplate = localTemplate.replace(VAR_SOURCE_LANGUAGE, prop.getValue());
                } else if (prop.getType().equals(ExternalTMFactory.TMXLoader.PROP_TARGET_LANGUAGE)) {
                    localTemplate = localTemplate.replace(VAR_TARGET_LANGUAGE, prop.getValue());
                }
            }
        }
        // If the props were not set, avoid printing the variables.
        localTemplate = localTemplate.replace(VAR_SOURCE_LANGUAGE, "");
        localTemplate = localTemplate.replace(VAR_TARGET_LANGUAGE, "");

        ProjectProperties props = Core.getProject().getProjectProperties();
        if (props != null) {
            localTemplate = expandFileNames(localTemplate, match.projs, props.getTMRoot());
        }
        return localTemplate;
    }

    public Result apply(NearString match, int id) {
        Result result = new Result();
        styledComponents.clear();

        // Variables
        result.text = this.expandVariables(match);
        result.text = result.text.replace(VAR_ID, Integer.toString(id));

        // Properties (<prop type='xxx'>value</prop>)
        if (match.props != null) {
            result.text = expandProperties(result.text, match.props);
        } else {
            result.text = result.text.replaceAll(PATTERN_SINGLE_PROPERTY.pattern(), "");
            result.text = result.text.replaceAll(PATTERN_PROPERTY_GROUP.pattern(), "");
        }

        styledComponents.put(result.text.indexOf(VAR_SOURCE_TEXT), SOURCE_TEXT_REPLACER);
        styledComponents.put(result.text.indexOf(VAR_DIFF), DIFF_REPLACER);
        styledComponents.put(result.text.indexOf(VAR_DIFF_REVERSED), DIFF_REVERSED_REPLACER);

        for (Entry<Integer, Replacer> entry : styledComponents.entrySet()) {
            entry.getValue().replace(result, match);
        }

        return result;
    }
}
