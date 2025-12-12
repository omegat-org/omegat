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

import org.jspecify.annotations.Nullable;

import java.util.HashMap;

public class PluralData {

    private static final PluralData INSTANCE = new PluralData();

    private HashMap<String, PluralInfo> info = new HashMap<>();

    private PluralData() {
        initalize();
    }

    public static PluralData getInstance() {
        return INSTANCE;
    }

    public @Nullable PluralInfo getPlural(String lang) {
        return info.get(lang);
    }

    // CHECKSTYLE.OFF: LineLength
    private void initalize() {
        // list taken from http://translate.sourceforge.net/wiki/l10n/pluralforms d.d. 14-09-2012
        // See also http://unicode.org/repos/cldr-tmp/trunk/diff/supplemental/language_plural_rules.html
        info.put("ach", new PluralInfo(2, "(n > 1)"));
        info.put("af", new PluralInfo(2, "(n != 1)"));
        info.put("ak", new PluralInfo(2, "(n > 1)"));
        info.put("am", new PluralInfo(2, "(n > 1)"));
        info.put("an", new PluralInfo(2, "(n != 1)"));
        info.put("ar", new PluralInfo(6,
                " n==0 ? 0 : n==1 ? 1 : n==2 ? 2 : n%100>=3 && n%100<=10 ? 3 : n%100>=11 ? 4 : 5"));
        info.put("arn", new PluralInfo(2, "(n > 1)"));
        info.put("ast", new PluralInfo(2, "(n != 1)"));
        info.put("ay", new PluralInfo(1, "0"));
        info.put("az", new PluralInfo(2, "(n != 1) "));
        info.put("be", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("bg", new PluralInfo(2, "(n != 1)"));
        info.put("bn", new PluralInfo(2, "(n != 1)"));
        info.put("bo", new PluralInfo(1, "0"));
        info.put("br", new PluralInfo(2, "(n > 1)"));
        info.put("brx", new PluralInfo(2, "(n != 1)"));
        info.put("bs", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2) "));
        info.put("ca", new PluralInfo(2, "(n != 1)"));
        info.put("cgg", new PluralInfo(1, "0"));
        info.put("cs", new PluralInfo(3, "(n==1) ? 0 : (n>=2 && n<=4) ? 1 : 2"));
        info.put("csb",
                new PluralInfo(3, "n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2"));
        info.put("cy", new PluralInfo(4, " (n==1) ? 0 : (n==2) ? 1 : (n != 8 && n != 11) ? 2 : 3"));
        info.put("da", new PluralInfo(2, "(n != 1)"));
        info.put("de", new PluralInfo(2, "(n != 1)"));
        info.put("doi", new PluralInfo(2, "(n != 1)"));
        info.put("dz", new PluralInfo(1, "0"));
        info.put("el", new PluralInfo(2, "(n != 1)"));
        info.put("en", new PluralInfo(2, "(n != 1)"));
        info.put("eo", new PluralInfo(2, "(n != 1)"));
        info.put("es", new PluralInfo(2, "(n != 1)"));
        info.put("et", new PluralInfo(2, "(n != 1)"));
        info.put("eu", new PluralInfo(2, "(n != 1)"));
        info.put("fa", new PluralInfo(1, "0"));
        info.put("ff", new PluralInfo(2, "(n != 1)"));
        info.put("fi", new PluralInfo(2, "(n != 1)"));
        info.put("fil", new PluralInfo(2, "n > 1"));
        info.put("fo", new PluralInfo(2, "(n != 1)"));
        info.put("fr", new PluralInfo(2, "(n > 1)"));
        info.put("fur", new PluralInfo(2, "(n != 1)"));
        info.put("fy", new PluralInfo(2, "(n != 1)"));
        info.put("ga", new PluralInfo(5, "n==1 ? 0 : n==2 ? 1 : n<7 ? 2 : n<11 ? 3 : 4"));
        info.put("gd",
                new PluralInfo(4, "(n==1 || n==11) ? 0 : (n==2 || n==12) ? 1 : (n > 2 && n < 20) ? 2 : 3"));
        info.put("gl", new PluralInfo(2, "(n != 1)"));
        info.put("gu", new PluralInfo(2, "(n != 1)"));
        info.put("gun", new PluralInfo(2, "(n > 1)"));
        info.put("ha", new PluralInfo(2, "(n != 1)"));
        info.put("he", new PluralInfo(2, "(n != 1)"));
        info.put("hi", new PluralInfo(2, "(n != 1)"));
        info.put("hne", new PluralInfo(2, "(n != 1)"));
        info.put("hy", new PluralInfo(2, "(n != 1)"));
        info.put("hr", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("hu", new PluralInfo(2, "(n != 1)"));
        info.put("ia", new PluralInfo(2, "(n != 1)"));
        info.put("id", new PluralInfo(1, "0"));
        info.put("is", new PluralInfo(2, "(n%10!=1 || n%100==11)"));
        info.put("it", new PluralInfo(2, "(n != 1)"));
        info.put("ja", new PluralInfo(1, "0"));
        info.put("jbo", new PluralInfo(1, "0"));
        info.put("jv", new PluralInfo(2, "n!=0"));
        info.put("ka", new PluralInfo(1, "0"));
        info.put("kk", new PluralInfo(1, "0"));
        info.put("km", new PluralInfo(1, "0"));
        info.put("kn", new PluralInfo(2, "(n!=1)"));
        info.put("ko", new PluralInfo(1, "0"));
        info.put("ku", new PluralInfo(2, "(n!= 1)"));
        info.put("kw", new PluralInfo(4, " (n==1) ? 0 : (n==2) ? 1 : (n == 3) ? 2 : 3"));
        info.put("ky", new PluralInfo(1, "0"));
        info.put("lb", new PluralInfo(2, "(n != 1)"));
        info.put("ln", new PluralInfo(2, "n>1"));
        info.put("lo", new PluralInfo(1, "0"));
        info.put("lt",
                new PluralInfo(3, "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && (n%100<10 or n%100>=20) ? 1 : 2)"));
        info.put("lv", new PluralInfo(3, "(n%10==1 && n%100!=11 ? 0 : n != 0 ? 1 : 2)"));
        info.put("mai", new PluralInfo(2, "(n != 1)"));
        info.put("mfe", new PluralInfo(2, "(n > 1)"));
        info.put("mg", new PluralInfo(2, "(n > 1)"));
        info.put("mi", new PluralInfo(2, "(n > 1)"));
        info.put("mk", new PluralInfo(2, " n==1 || n%10==1 ? 0 : 1"));
        info.put("ml", new PluralInfo(2, "(n != 1)"));
        info.put("mn", new PluralInfo(2, "(n != 1)"));
        info.put("mni", new PluralInfo(2, "(n != 1)"));
        info.put("mnk", new PluralInfo(3, "(n==0 ? 0 : n==1 ? 1 : 2"));
        info.put("mr", new PluralInfo(2, "(n != 1)"));
        info.put("ms", new PluralInfo(1, "0"));
        info.put("mt", new PluralInfo(4,
                "(n==1 ? 0 : n==0 || ( n%100>1 && n%100<11) ? 1 : (n%100>10 && n%100<20 ) ? 2 : 3)"));
        info.put("my", new PluralInfo(1, "0"));
        info.put("nah", new PluralInfo(2, "(n != 1)"));
        info.put("nap", new PluralInfo(2, "(n != 1)"));
        info.put("nb", new PluralInfo(2, "(n != 1)"));
        info.put("ne", new PluralInfo(2, "(n != 1)"));
        info.put("nl", new PluralInfo(2, "(n != 1)"));
        info.put("se", new PluralInfo(2, "(n != 1)"));
        info.put("nn", new PluralInfo(2, "(n != 1)"));
        info.put("no", new PluralInfo(2, "(n != 1)"));
        info.put("nso", new PluralInfo(2, "(n != 1)"));
        info.put("oc", new PluralInfo(2, "(n > 1)"));
        info.put("or", new PluralInfo(2, "(n != 1)"));
        info.put("ps", new PluralInfo(2, "(n != 1)"));
        info.put("pa", new PluralInfo(2, "(n != 1)"));
        info.put("pap", new PluralInfo(2, "(n != 1)"));
        info.put("pl",
                new PluralInfo(3, "(n==1 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("pms", new PluralInfo(2, "(n != 1)"));
        info.put("pt", new PluralInfo(2, "(n != 1)"));
        info.put("rm", new PluralInfo(2, "(n!=1)"));
        info.put("ro", new PluralInfo(3, "(n==1 ? 0 : (n==0 || (n%100 > 0 && n%100 < 20)) ? 1 : 2)"));
        info.put("ru", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("rw", new PluralInfo(2, "(n != 1)"));
        info.put("sah", new PluralInfo(1, "0"));
        info.put("sat", new PluralInfo(2, "(n != 1)"));
        info.put("sco", new PluralInfo(2, "(n != 1)"));
        info.put("sd", new PluralInfo(2, "(n != 1)"));
        info.put("si", new PluralInfo(2, "(n != 1)"));
        info.put("sk", new PluralInfo(3, "(n==1) ? 0 : (n>=2 && n<=4) ? 1 : 2"));
        info.put("sl", new PluralInfo(4, "(n%100==1 ? 1 : n%100==2 ? 2 : n%100==3 || n%100==4 ? 3 : 0)"));
        info.put("so", new PluralInfo(2, "n != 1"));
        info.put("son", new PluralInfo(2, "(n != 1)"));
        info.put("sq", new PluralInfo(2, "(n != 1)"));
        info.put("sr", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("su", new PluralInfo(1, "0"));
        info.put("sw", new PluralInfo(2, "(n != 1)"));
        info.put("sv", new PluralInfo(2, "(n != 1)"));
        info.put("ta", new PluralInfo(2, "(n != 1)"));
        info.put("te", new PluralInfo(2, "(n != 1)"));
        info.put("tg", new PluralInfo(2, "(n > 1)"));
        info.put("ti", new PluralInfo(2, "n > 1"));
        info.put("th", new PluralInfo(1, "0"));
        info.put("tk", new PluralInfo(2, "(n != 1)"));
        info.put("tr", new PluralInfo(2, "(n>1)"));
        info.put("tt", new PluralInfo(1, "0"));
        info.put("ug", new PluralInfo(1, "0"));
        info.put("uk", new PluralInfo(3,
                "(n%10==1 && n%100!=11 ? 0 : n%10>=2 && n%10<=4 && (n%100<10 || n%100>=20) ? 1 : 2)"));
        info.put("ur", new PluralInfo(2, "(n != 1)"));
        info.put("uz", new PluralInfo(2, "(n > 1)"));
        info.put("vi", new PluralInfo(1, "0"));
        info.put("wa", new PluralInfo(2, "(n > 1)"));
        info.put("wo", new PluralInfo(1, "0"));
        info.put("yo", new PluralInfo(2, "(n != 1)"));
        info.put("zh", new PluralInfo(1, "0 "));
    }
    // CHECKSTYLE.ON: LineLength
}
