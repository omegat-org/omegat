package org.omegat.util.nlp;

public class PluralInfo {

    public int plurals;
    public String expression;

    PluralInfo(int nrOfPlurals, String pluralExpression) {
        plurals = nrOfPlurals;
        expression = pluralExpression;
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
