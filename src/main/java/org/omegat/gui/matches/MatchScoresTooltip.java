/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.matching.NearString;
import org.omegat.util.NumeralValueParser;
import org.omegat.util.OStrings;
import org.omegat.util.StringUtil;

/**
 * Builds the per-match tooltip for the fuzzy matches pane: an optional warning
 * when the numbers in the active segment and in the match differ, the three
 * similarity values with plain-language labels, and the applied penalties when
 * there are any (feature request #465).
 *
 * @author stephan.pakebusch at zollsoft.de
 */
public final class MatchScoresTooltip {

    private MatchScoresTooltip() {
    }

    private static final Pattern DIGIT_RUN = Pattern.compile("\\p{Nd}+");

    /**
     * Renders the tooltip HTML for one match: a title line naming the match
     * number and its source text, then the optional number warning, the score
     * line and the optional penalty lines.
     *
     * @param matchNumber
     *            1-based position of the match in the pane
     */
    public static String render(int matchNumber, NearString match, String activeSource) {
        StringBuilder sb = new StringBuilder("<html>");
        sb.append(escape(StringUtil.format(OStrings.getString("MATCHES_TOOLTIP_TITLE"), matchNumber,
                match.source))).append("<br>");
        if (activeSource != null && numbersDiffer(activeSource, match.source)) {
            sb.append("<b>").append(escape(OStrings.getString("MATCHES_TOOLTIP_NUMBERS_DIFFER")))
                    .append("</b><br>");
        }
        NearString.Scores scores = match.scores[0];
        sb.append(escape(StringUtil.format(OStrings.getString("MATCHES_TOOLTIP_SCORES"), scores.score,
                scores.scoreNoStem, scores.adjustedScore)));
        if (scores.penalty > 0) {
            sb.append("<br>").append(
                    escape(StringUtil.format(OStrings.getString("MATCHES_TOOLTIP_PENALTY"), scores.penalty)));
        }
        if (match.fuzzyMark) {
            sb.append("<br>").append(escape(OStrings.getString("MATCHES_TOOLTIP_FUZZY")));
        }
        return sb.append("</html>").toString();
    }

    /**
     * Whether the numeric values contained in the two texts differ, compared
     * as multisets of values so that the same number written in another script
     * or digit width does not trigger the warning. Only decimal digit runs are
     * considered; letter numerals would produce false warnings on words.
     */
    static boolean numbersDiffer(String a, String b) {
        return !numberValues(a).equals(numberValues(b));
    }

    private static Map<BigInteger, Integer> numberValues(String s) {
        Map<BigInteger, Integer> values = new HashMap<>();
        Matcher m = DIGIT_RUN.matcher(s);
        while (m.find()) {
            NumeralValueParser.parseWhole(m.group()).ifPresent(v -> values.merge(v, 1, Integer::sum));
        }
        return values;
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
