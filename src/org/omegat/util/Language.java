/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel, Zoltan Bartko
               2010-2011 Didier Briel
               2012 Guido Leenders
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

package org.omegat.util;

import java.util.Locale;
import java.util.regex.Matcher;

/**
 * This class is here, because the Locale has hard-coded '_' inside, and we must
 * adhere to ISO standard LL-CC.
 * <p>
 * This class tries to follow <a
 * href="http://www.lisa.org/standards/tmx/tmx.html#xml:lang">TMX Specification
 * on languages</a>, which is based on <a
 * href="http://www.ietf.org/rfc/rfc3066.txt">RFC 3066</a>, i.e.
 * <ul>
 * <li>Language is composed from 1-8 alpha (A-Za-z) chars, then "-", then 1-8
 * alpha/digit chars (A-Za-z0-9).
 * <li>Case insensitive
 * <li>Case is not altered by this class, even though there exist conventions
 * for capitalization ([ISO 3166] recommends that country codes are capitalized
 * (MN Mongolia), and [ISO 639] recommends that language codes are written in
 * lower case (mn Mongolian)).
 * <ul>
 * 
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Zoltan Bartko bartkozoltan@bartkozoltan.com
 * @author Guido Leenders
 */
public class Language implements Comparable<Object> {
    private Locale locale = new Locale("");
    private String languageCode;
    private String countryCode;

    /** Creates a new instance of Language, based on Locale */
    public Language(Locale locale) {
        if (locale != null)
            this.locale = locale;
        this.languageCode = this.locale.getLanguage();
        this.countryCode = this.locale.getCountry();
    }

    /**
     * Creates a new instance of Language, based on a string of a form "XX_YY"
     * or "XX-YY", where XX is a language code composed from 1-8 alpha (A-Za-z)
     * chars, and YY is a country ISO code composed from 1-8 alpha/digit
     * (A-Za-z0-9) chars.<br>
     * The form xx-xxxx-xx is also accepted, where "xxxx" is a 4 alpha characters script as defined in
     * <a href="http://unicode.org/iso15924/iso15924-codes.html">ISO 15924</a>. E.g., sr-Latn-RS, 
     * which represents Serbian ('sr') written using Latin script ('Latn') as used in Serbia ('RS').
     * This form is described in <a href="http://www.rfc-editor.org/rfc/bcp/bcp47.tx">BCP47</a>.
     */
    public Language(String str) {
        this.languageCode = "";
        this.countryCode = "";
        this.locale = new Locale("");
        if (str != null) {
            Matcher m = PatternConsts.LANG_AND_COUNTRY.matcher(str);
            if (m.matches() && m.groupCount() >= 1) {
                this.languageCode = m.group(1);
                if (m.group(2) != null)
                    this.countryCode = m.group(2);
                this.locale = new Locale(this.languageCode.toLowerCase(Locale.ENGLISH),
                        this.countryCode.toUpperCase(Locale.ENGLISH));
            }
        }
    }

    /**
     * Returns a name for the language that is appropriate for display to the
     * user.
     */
    public String getDisplayName() {
        return locale.getDisplayName();
    }

    /**
     * Returns a string representation as an ISO language code (XX-YY).
     */
    @Override
    public String toString() {
        return getLanguage();
    }

    /**
     * Returns a string representation as an ISO language code (XX-YY).
     */
    public String getLanguage() {
        String langstring = getLanguageCode();
        if (langstring.length() > 0 && getCountryCode().length() > 0)
            langstring += "-" + getCountryCode();
        return langstring;
    }

    /**
     * Returns a string representation as a Java locale (xx_YY).
     */
    public String getLocaleCode() {
        if (locale == null)
            return "";
        else { 
            // Patch Java locale, to return correct locales instead of obsolete codes
            String returnString = locale.toString();
            if (returnString.length()<2)
                return returnString; // We cannot test a locale of less than 2 characters
            if (returnString.substring(0, 2).equalsIgnoreCase("in")) {
                returnString = "id" + returnString.substring(2);
            } else if (returnString.substring(0, 2).equalsIgnoreCase("iw")) {
                returnString = "he" + returnString.substring(2);
            } else if (returnString.substring(0, 2).equalsIgnoreCase("ji")) {
                returnString = "yi" + returnString.substring(2);
            } 
            return returnString;
        }
            
    }

    /**
     * Returns a string representation as a Microsoft locale (xx-yy).
     */
    public String getLocaleLCID() {
        return getLocaleCode().toLowerCase().replace("_", "-");
    }

    /**
     * returns the Java locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns only a language (XX).
     */
    public String getLanguageCode() {
        if (this.languageCode == null)
            return "";
        else
            return this.languageCode;
    }

    /**
     * Returns only a country (YY).
     */
    public String getCountryCode() {
        if (this.countryCode == null)
            return "";
        else
            return this.countryCode;
    }

    // /////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////
    // /////////////////////////////////////////////////////////////////////////

    /**
     * The array of all the languages.
     * <p>
     * The list contains all the ISO 639-1 codes. When there are country
     * variants (e.g., EN-US, EN-GB), they have been kept only when there are
     * more than one variant (e.g., JA-JP has been left commented).
     */
    public static final Language[] LANGUAGES = new Language[] { new Language("AA"), // AFAR
            // new Language("AA-ET"), // AFAR (ETHIOPIA)
            new Language("AA-DJ"), // AFAR (DJIBOUTI)
            new Language("AA-ER"), // AFAR (ERITREA)
            new Language("AB"), // ABKHAZIAN
            new Language("AE"), // AVESTAN
            new Language("AF"), // AFRIKAANS
            new Language("AF-NA"), // AFRIKAANS (NAMIBIA)
            new Language("AF-ZA"), // AFRIKAANS (SOUTH AFRICA)
            new Language("AF-ZW"), // AFRIKAANS (ZIMBABWAE)
            new Language("AK"), // AKAN
            new Language("AM"), // AMHARIC
            new Language("AN"), // ARAGONESE
            new Language("AR"), // ARABIC
            new Language("AR-AE"), // ARABIC (UNITED ARAB EMIRATES)
            new Language("AR-BH"), // ARABIC (BAHRAIN)
            new Language("AR-DZ"), // ARABIC (ALGERIA)
            new Language("AR-EG"), // ARABIC (EGYPT)
            new Language("AR-IQ"), // ARABIC (IRAQ)
            new Language("AR-JO"), // ARABIC (JORDAN)
            new Language("AR-KW"), // ARABIC (KUWAIT)
            new Language("AR-LB"), // ARABIC (LEBANON)
            new Language("AR-LY"), // ARABIC (LIBYA)
            new Language("AR-MA"), // ARABIC (MOROCCO)
            new Language("AR-OM"), // ARABIC (OMAN)
            new Language("AR-QA"), // ARABIC (QATAR)
            new Language("AR-SA"), // ARABIC (SAUDI ARABIA)
            new Language("AR-SD"), // ARABIC (SUDAN)
            new Language("AR-SY"), // ARABIC (SYRIA)
            new Language("AR-TN"), // ARABIC (TUNISIA)
            new Language("AR-US"), // ARABIC (UNITED STATES)
            new Language("AR-YE"), // ARABIC (YEMEN)

            new Language("AS"), // ASSAMESE
            new Language("AV"), // AVARIC
            new Language("AY"), // AYMARA
            new Language("AZ"), // AZERBAIJANI
            // new Language("AZ-AZ"), // AZERBAIJANI (AZERBAIJAN)
            new Language("BA"), // BASHKIR

            new Language("BE"), // BELARUSIAN
            // new Language("BE-BY"), // BELARUSIAN (BELARUS)
            new Language("BG"), // BULGARIAN
            // new Language("BG-BG"), // BULGARIAN (BULGARIA)

            new Language("BH"), // BIHARI
            new Language("BI"), // BISLAMA
            new Language("BM"), // BAMBARA
            new Language("BN"), // BENGALI
            new Language("BO"), // TIBETAN
            new Language("BR"), // BRETON
            new Language("BS"), // BOSNIAN
            // new Language("BS-BA"), // BOSNIAN (BOSNIA AND HERZEGOVINA)

            new Language("CA"), // CATALAN
            // new Language("CA-ES"), // CATALAN (SPAIN)

            new Language("CE"), // CHECHEN
            new Language("CH"), // CHAMORRO
            new Language("CO"), // CORSICAN
            new Language("CR"), // CREE

            new Language("CS"), // CZECH
            // new Language("CS-CZ"), // CZECH (CZECH REPUBLIC)

            new Language("CU"), // CHURCH SLAVIC; OLD SLAVONIC; CHURCH SLAVONIC;
                                // OLD BULGARIAN; OLD CHURCH SLAVONIC
            new Language("CV"), // CHUVASH
            new Language("CY"), // WELSH

            new Language("DA"), // DANISH
            // new Language("DA-DK"), // DANISH (DENMARK)
            new Language("DE"), // GERMAN
            new Language("DE-AT"), // GERMAN (AUSTRIA)
            new Language("DE-CH"), // GERMAN (SWITZERLAND)
            new Language("DE-DE"), // GERMAN (GERMANY)
            new Language("DE-LU"), // GERMAN (LUXEMBOURG)

            new Language("DV"), // DIVEHI
            new Language("DZ"), // DZONGKHA
            // new Language("DZ-BT"), // DZONGKHA (BHUTAN)
            new Language("EE"), // EWE

            new Language("EL"), // GREEK
            // new Language("EL-GR"), // GREEK (GREECE)
            new Language("EN"), // ENGLISH
            new Language("EN-AU"), // ENGLISH (AUSTRALIA)
            new Language("EN-CA"), // ENGLISH (CANADA)
            new Language("EN-GB"), // ENGLISH (UNITED KINGDOM)
            new Language("EN-IE"), // ENGLISH (IRELAND)
            new Language("EN-IN"), // ENGLISH (INDIA)
            new Language("EN-NZ"), // ENGLISH (NEW ZEALAND)
            new Language("EN-US"), // ENGLISH (UNITED STATES)
            new Language("EN-ZA"), // ENGLISH (SOUTH AFRICA)

            new Language("EO"), // ESPERANTO

            new Language("ES"), // SPANISH
            new Language("ES-AR"), // SPANISH (ARGENTINA)
            new Language("ES-BO"), // SPANISH (BOLIVIA)
            new Language("ES-CL"), // SPANISH (CHILE)
            new Language("ES-CO"), // SPANISH (COLOMBIA)
            new Language("ES-CR"), // SPANISH (COSTA RICA)
            new Language("ES-DO"), // SPANISH (DOMINICAN REPUBLIC)
            new Language("ES-EC"), // SPANISH (ECUADOR)
            new Language("ES-ES"), // SPANISH (SPAIN)
            new Language("ES-GT"), // SPANISH (GUATEMALA)
            new Language("ES-HN"), // SPANISH (HONDURAS)
            new Language("ES-MX"), // SPANISH (MEXICO)
            new Language("ES-NI"), // SPANISH (NICARAGUA)
            new Language("ES-PA"), // SPANISH (PANAMA)
            new Language("ES-PE"), // SPANISH (PERU)
            new Language("ES-PR"), // SPANISH (PUERTO RICO)
            new Language("ES-PY"), // SPANISH (PARAGUAY)
            new Language("ES-SV"), // SPANISH (EL SALVADOR)
            new Language("ES-US"), // SPANISH (UNITED STATES)
            new Language("ES-UY"), // SPANISH (URUGUAY)
            new Language("ES-VE"), // SPANISH (VENEZUELA)

            new Language("ET"), // ESTONIAN
            // new Language("ET-EE"), // ESTONIAN (ESTONIA)

            new Language("EU"), // BASQUE

            new Language("FA"), // PERSIAN; FARSI
            new Language("FA-AF"), // PERSIAN; FARSI (AFGANISTAN)
            new Language("FA-IR"), // PERSIAN; FARSI (IRAN)

            new Language("FF"), // FULAH

            new Language("FI"), // FINNISH
            // new Language("FI-FI"), // FINNISH (FINLAND)

            new Language("FJ"), // FIJIAN
            // new Language("FJ-FJ"), // FIJIAN (FIJI)
            new Language("FO"), // FAROESE

            new Language("FR"), // FRENCH
            new Language("FR-BE"), // FRENCH (BELGIUM)
            new Language("FR-CA"), // FRENCH (CANADA)
            new Language("FR-CH"), // FRENCH (SWITZERLAND)
            new Language("FR-FR"), // FRENCH (FRANCE)
            new Language("FR-LU"), // FRENCH (LUXEMBOURG)

            new Language("FY"), // FRISIAN

            new Language("GA"), // IRISH
            // new Language("GA-IE"), // IRISH (IRELAND)

            new Language("GD"), // GAELIC; SCOTTISH GAELIC

            new Language("GL"), // GALLEGAN
            new Language("GN"), // GUARANI
            new Language("GU"), // GUJARATI
            new Language("GV"), // MANX
            new Language("HA"), // HAUSA

            new Language("HE"), // HEBREW
            // new Language("HE-IL"), // HEBREW (ISRAEL)

            new Language("HI"), // HINDI
            // new Language("HI-IN"), // HINDI (INDIA)

            new Language("HO"), // HIRI MOTU

            new Language("HR"), // CROATIAN
            // new Language("HR-HR"), // CROATIAN (CROATIA)

            new Language("HT"), // HAITIAN; HAITIAN CREOLE
            // new Language("HT-HT"), // HAITIAN; HAITIAN CREOLE (HAITI)

            new Language("HU"), // HUNGARIAN
            // new Language("HU-HU"), // HUNGARIAN (HUNGARY)

            new Language("HY"), // ARMENIAN
            // new Language("HY-AM"), // ARMENIAN (ARMENIA)

            new Language("HZ"), // HERERO
            new Language("IA"), // INTERLINGUA; INTERNATIONAL AUXILIARY
            new Language("ID"), // INDONESIAN
            // new Language("ID-ID"), // INDONESIAN (INDONESIA)
            new Language("IE"), // INTERLINGUE
            new Language("IG"), // IGBO
            new Language("II"), // SICHUAN YI
            new Language("IK"), // INUPIAQ
            new Language("IO"), // IDO

            new Language("IS"), // ICELANDIC
            // new Language("IS-IS"), // ICELANDIC (ICELAND)

            new Language("IT"), // ITALIAN
            new Language("IT-CH"), // ITALIAN (SWITZERLAND)
            new Language("IT-IT"), // ITALIAN (ITALY)

            new Language("IU"), // INUKTITUT

            new Language("JA"), // JAPANESE
            // new Language("JA-JP"), // JAPANESE (JAPAN)

            new Language("JV"), // JAVANESE

            new Language("KA"), // GEORGIAN
            // new Language("KA-GE"), // GEORGIAN (GEORGIA)

            new Language("KG"), // KONGO
            new Language("KI"), // KIKUYU; GIKUYU
            new Language("KJ"), // KUANYAMA; KWANYAMA

            new Language("KK"), // KAZAKH
            // new Language("KK-KZ"), // KAZAKH (KAZAKHSTAN)

            new Language("KL"), // KALAALLISUT; GREENLANDIC
            new Language("KM"), // KHMER
            // new Language("KM-KH"), // KHMER (CAMBODIA)
            new Language("KN"), // KANNADA

            new Language("KO"), // KOREAN
            // new Language("KO-KR"), // KOREAN (SOUTH KOREA)

            new Language("KR"), // KANURI
            new Language("KS"), // KASHMIRI
            new Language("KU"), // KURDISH
            new Language("KV"), // KOMI
            new Language("KW"), // CORNISH

            new Language("KY"), // KIRGHIZ
            // new Language("KY-KG"), // KIRGHIZ (KYRGYZSTAN)

            new Language("LA"), // LATIN
            new Language("LB"), // LUXEMBOURGISH; LETZEBURGESCH
            // new Language("LB-LU"), // LUXEMBOURGISH (LUXEMBOURG)
            new Language("LG"), // GANDA
            new Language("LI"), // LIMBURGAN; LIMBURGER; LIMBURGISH
            new Language("LN"), // LINGALA
            new Language("LO"), // LAO
            // new Language("LO-LA"), // LAO (LAO)

            new Language("LT"), // LITHUANIAN
            // new Language("LT-LT"), // LITHUANIAN (LITHUANIA)

            new Language("LU"), // LUBA-KATANGA

            new Language("LV"), // LATVIAN (LETTISH)
            // new Language("LV-LV"), // LATVIAN (LETTISH) (LATVIA)

            new Language("MG"), // MALAGASY
            // new Language("MG-MG"), // MALAGASY (MADAGASCAR)
            new Language("MH"), // MARSHALLESE
            // new Language("MH-MH"), // MARSHALLESE (MARSHALL ISLANDS)
            new Language("MI"), // MAORI

            new Language("MK"), // MACEDONIAN
            // new Language("MK-MK"), // MACEDONIAN (MACEDONIA)

            new Language("ML"), // MALAYALAM

            new Language("MN"), // MONGOLIAN
            // new Language("MN-MN"), // MONGOLIAN (MONGOLIA)

            new Language("MO"), // MOLDAVIAN
            // new Language("MO-MD"), // MOLDAVIAN (MOLDOVA)

            new Language("MR"), // MARATHI
            new Language("MS"), // MALAY
            // new Language("MS-MY"), // MALAY (MALAYSIA)
            new Language("MT"), // MALTESE
            // new Language("MT-MT"), // MALTESE (MALTA)
            new Language("MY"), // BURMESE
            // new Language("MY-MM"), // BURMESE (MYANMAR)
            new Language("NA"), // NAURU

            new Language("NB"), // NORWEGIAN BOKM?L; BOKM?L, NORWEGIAN
            // new Language("NB-NO"), // BOKM?L (NORWAY)

            new Language("ND"), // NDEBELE, NORTH; NORTH NDEBELE
            new Language("NE"), // NEPALI
            // new Language("NE-NP"), // NEPALI (NEPAL)
            new Language("NG"), // NDONGA

            new Language("NL"), // DUTCH
            new Language("NL-BE"), // DUTCH (BELGIUM)
            new Language("NL-NL"), // DUTCH (NETHERLANDS)

            new Language("NN"), // NORWEGIAN NYNORSK; NYNORSK, NORWEGIAN
            // new Language("NN-NO"), // NYNORSK (NORWAY)

            new Language("NO"), // NORWEGIAN
            // new Language("NO-NO"), // NORWEGIAN (NORWAY)

            new Language("NR"), // NDEBELE, SOUTH; SOUTH NDEBELE
            new Language("NV"), // NAVAJO; NAVAHO
            new Language("NY"), // CHICHEWA; CHEWA; NYANJA
            new Language("OC"), // OCCITAN (POST 1500); PROVEN?AL
            new Language("OJ"), // OJIBWA
            new Language("OM"), // OROMO
            new Language("OR"), // ORIYA
            new Language("OS"), // OSSETIAN; OSSETIC
            new Language("PA"), // PANJABI; PUNJABI
            new Language("PA-IN"), // PANJABI; PUNJABI (INDIA)
            new Language("PA-PK"), // PANJABI; PUNJABI (PAKISTAN)
            new Language("PI"), // PALI

            new Language("PL"), // POLISH
            // new Language("PL-PL"), // POLISH (POLAND)

            new Language("PS"), // PUSHTO

            new Language("PT"), // PORTUGUESE
            new Language("PT-BR"), // PORTUGUESE (BRAZIL)
            new Language("PT-PT"), // PORTUGUESE (PORTUGAL)

            new Language("QU"), // QUECHUA
            new Language("RM"), // RAETO-ROMANCE
            new Language("RN"), // RUNDI

            new Language("RO"), // ROMANIAN
            // new Language("RO-RO"), // ROMANIAN (ROMANIA)

            new Language("RU"), // RUSSIAN
            new Language("RU-BY"), // RUSSIAN (BELORUS)
            new Language("RU-RU"), // RUSSIAN (RUSSIA)

            new Language("RW"), // KINYARWANDA
            new Language("SA"), // SANSKRIT
            new Language("SC"), // SARDINIAN
            new Language("SD"), // SINDHI
            new Language("SD-IN"), // SINDHI (INDIA)
            new Language("SD-PK"), // SINDHI (PAKISTAN)
            new Language("SE"), // NORTHERN SAMI
            new Language("SG"), // SANGO

            new Language("SH"), // SERBO-CROATIAN
            new Language("SI"), // SINHALA; SINHALESE

            new Language("SK"), // SLOVAK
            // new Language("SK-SK"), // SLOVAK (SLOVAKIA)

            new Language("SL"), // SLOVENIAN
            // new Language("SL-SI"), // SLOVENIAN (SLOVENIA)

            new Language("SM"), // SAMOAN
            new Language("SM-AS"), // SAMOAN (AMERICAN SAMOA)
            new Language("SM-WS"), // SAMOAN (SAMOA)

            new Language("SN"), // SHONA
            new Language("SO"), // SOMALI
            // new Language("SO-SO"), // SOMALI (SOMALIA)

            new Language("SQ"), // ALBANIAN
            // new Language("SQ-AL"), // ALBANIAN (ALBANIA)

            new Language("SR"), // SERBIAN
            // new Language("SR-CS"), // SERBIAN (SERBIA AND MONTENEGRO)

            new Language("SS"), // SWATI
            new Language("ST"), // SOTHO, SOUTHERN
            new Language("SU"), // SUNDANESE

            new Language("SV"), // SWEDISH
            // new Language("SV-SE"), // SWEDISH (SWEDEN)

            new Language("SW"), // SWAHILI
            new Language("TA"), // TAMIL
            new Language("TE"), // TELUGU
            new Language("TG"), // TAJIK
            // new Language("TG-TJ"), // TAJIK (TAJIKISTAN)

            new Language("TH"), // THAI
            // new Language("TH-TH"), // THAI (THAILAND)

            new Language("TI"), // TIGRINYA
            new Language("TK"), // TURKMEN
            // new Language("TK-TM"), // TURKMEN (TURKMENISTAN)
            new Language("TL"), // TAGALOG
            new Language("TN"), // TSWANA
            new Language("TO"), // TONGA
            // new Language("TO-TO"), // TONGA (TONGA ISLANDS)

            new Language("TR"), // TURKISH
            // new Language("TR-TR"), // TURKISH (TURKEY)

            new Language("TS"), // TSONGA

            new Language("TT"), // TATAR

            new Language("TW"), // TWI
            new Language("TY"), // TAHITIAN
            new Language("UG"), // UIGHUR; UYGHUR

            new Language("UK"), // UKRAINIAN
            // new Language("UK-UA"), // UKRAINIAN (UKRAINE)

            new Language("UR"), // URDU
            new Language("UR-IN"), // URDU (INDIA)
            new Language("UR-PK"), // URDU (PAKISTAN)

            new Language("UZ"), // UZBEK
            // new Language("UZ-UZ"), // UZBEK (UZBEKISTAN)
            new Language("VE"), // VENDA
            new Language("VI"), // VIETNAMESE
            // new Language("VI-VN"), // VIETNAMESE (VIETNAM)
            new Language("VO"), // VOLAPUK
            new Language("WA"), // WALLOON
            new Language("WO"), // WOLOF
            new Language("XH"), // XHOSA
            new Language("YI"), // YIDDISH
            new Language("YO"), // YORUBA
            new Language("ZA"), // ZHUANG; CHUANG

            new Language("ZH"), // CHINESE
            new Language("ZH-CN"), // CHINESE SIMPLIFIED (CHINA)
            new Language("ZH-HK"), // CHINESE (HONG KONG)
            new Language("ZH-TW"), // CHINESE TRADITIONAL (TAIWAN)

            new Language("ZU"), // ZULU

    };

    /**
     * Indicates whether some other language is "equal to" this one.
     */
    @Override
    public boolean equals(Object lang) {
        if (this == lang)
            return true;
        if (lang == null)
            return false;
        if (!(lang instanceof Language))
            return false;
        Language that = (Language) lang;
        return this.getLocaleCode().equals(that.getLocaleCode());
    }

    /**
     * Returns a hash code value for the language. Basically returns a hashcode
     * of the underlying Locale object.
     */
    @Override
    public int hashCode() {
        return locale.hashCode();
    }

	public int compareTo(Object o) {
		if (o instanceof Language) {
			return this.getLanguage().compareTo(((Language)o).getLanguage());
		}
		return this.getLanguage().compareTo(o.toString());
	}

}
