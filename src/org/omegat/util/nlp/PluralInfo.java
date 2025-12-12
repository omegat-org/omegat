package org.omegat.util.nlp;

public class PluralInfo {

    public int plurals;
    public String expression;

    /**
     * Construct from discrete values.
     */
    PluralInfo(int nrOfPlurals, String pluralExpression) {
        plurals = nrOfPlurals;
        expression = pluralExpression;
    }

    /**
     * Construct from a gettext-like plural definition string.
     *
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

    public boolean isPlural(int n) {
        return n != 1;
    }

    public String getPluralForm(int n) {
        return expression.replace("%d", String.valueOf(n));
    }

    public String toString() {
        return expression;
    }
}
