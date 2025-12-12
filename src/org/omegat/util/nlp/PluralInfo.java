/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008-2012 Martin Fleurke
               2025 Hiroshi Miura
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
package org.omegat.util.nlp;

public class PluralInfo {

    private final int plurals;
    private final String expression;

    /**
     * Construct from discrete values.
     */
    @SuppressWarnings("unused")
    public PluralInfo(int nrOfPlurals, String pluralExpression) {
        plurals = nrOfPlurals;
        expression = pluralExpression;
    }

    /**
     * Construct from a gettext-like plural definition string.
     * <p>
     * Supported formats:
     * - "nplurals=2; plural=(n != 1)"
     * - "2; (n != 1)"
     * - "2|(n != 1)"
     *
     * @param definition the plural definition string
     * @throws IllegalArgumentException if the definition cannot be parsed
     */
    public PluralInfo(String definition) throws IllegalArgumentException {
        if (definition == null) {
            throw new IllegalArgumentException("definition is null");
        }
        String val = definition.trim();
        if (val.isEmpty()) {
            throw new IllegalArgumentException("definition is empty");
        }

        int count = -1;
        String expr = null;

        String lower = val.toLowerCase();
        if (lower.contains("nplurals=") && lower.contains("plural=")) {
            // gettext-like
            try {
                String[] parts = val.split(";");
                for (String p : parts) {
                    String t = p.trim();
                    String tl = t.toLowerCase();
                    if (tl.startsWith("nplurals=")) {
                        String num = t.substring(t.indexOf('=') + 1).trim();
                        count = Integer.parseInt(num);
                    } else if (tl.startsWith("plural=")) {
                        expr = t.substring(t.indexOf('=') + 1).trim();
                    }
                }
            } catch (Exception ignore) {
                // fallthrough to alternative parsing below
            }
        }

        if (count < 0 || expr == null || expr.isEmpty()) {
            // try split by '|' or ';' into two parts: count and expression
            String[] parts = val.split("[|;]", 2);
            if (parts.length == 2) {
                try {
                    count = Integer.parseInt(parts[0].trim());
                    expr = parts[1].trim();
                } catch (NumberFormatException ignored) {
                    // ignore
                }
            }
        }

        if (count < 0 || expr == null || expr.isEmpty()) {
            throw new IllegalArgumentException("Invalid plural definition: " + definition);
        }

        this.plurals = count;
        this.expression = expr;
    }

    public int getPlurals() {
        return plurals;
    }

    @Override
    public String toString() {
        return expression;
    }

    public String getGettextExpression() {
        return "Plural-Forms: nplurals=" + plurals + "; plural=" + expression + ";";
    }
}
