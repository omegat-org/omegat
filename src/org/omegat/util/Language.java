/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: http://www.omegat.org/omegat/omegat.html
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.util;

import java.util.Locale;
import java.util.regex.Matcher;

/**
 * This class is here, because the Locale has hard-coded '_' inside,
 * and we must adhere to ISO standard LL-CC.
 * <p>
 * This class tries to follow 
 * <a href="http://www.lisa.org/standards/tmx/tmx.html#xml:lang">TMX 
 * Specification on languages</a>, which is based on
 * <a href="http://www.ietf.org/rfc/rfc3066.txt">RFC 3066</a>, i.e.
 * <ul>
 * <li>Language is composed from 1-8 alpha (A-Za-z) chars, then "-", 
 *      then 1-8 alpha/digit chars (A-Za-z0-9).
 * <li>Case insensitive
 * <li>Case is not altered by this class, even though
 *     there exist conventions for capitalization 
 *     ([ISO 3166] recommends that country codes are capitalized (MN Mongolia), 
 *       and [ISO 639] recommends that language codes are written 
 *       in lower case (mn Mongolian)).
 * <ul>
 *
 * @author Maxym Mykhalchuk
 */
public class Language
{
    private Locale locale = new Locale("");                                     // NOI18N
    private String languageCode;
    private String countryCode;
    
    /** Creates a new instance of Language, based on Locale */
    public Language(Locale locale)
    {
        if( locale!=null )
            this.locale = locale;
        this.languageCode = this.locale.getLanguage();
        this.countryCode = this.locale.getCountry();
    }
    
    /** 
     * Creates a new instance of Language, based on a string 
     * of a form "XX_YY" or "XX-YY", where 
     * XXX is a language code composed from 1-8 alpha (A-Za-z) chars,
     * and YYY is a country ISO code composed from 1-8 alpha/digit (A-Za-z0-9) 
     * chars.
     */
    public Language(String str)
    {
        this.languageCode = "";                                                 // NOI18N
        this.countryCode = "";                                                  // NOI18N
        this.locale = new Locale("");                                           // NOI18N
        if( str!=null )
        {
            Matcher m = PatternConsts.LANG_AND_COUNTRY.matcher(str);
            if( m.matches() && m.groupCount()>=1 )
            {
                this.languageCode = m.group(1);
                if( m.group(2)!=null )
                    this.countryCode = m.group(2);
                this.locale = new Locale(this.languageCode.toLowerCase(), 
                        this.countryCode.toUpperCase());
            }
        }
    }

    /**
     * Returns a name for the language that is appropriate for display 
     * to the user.
     */
    public String getDisplayName()
    {
        return locale.getDisplayName();
    }
    
    /**
     * Returns a string representation
     * as an ISO language code (XX-YY).
     */
    public String toString()
    {
        return getLanguage();
    }
    
    /**
     * Returns a string representation
     * as an ISO language code (XX-YY).
     */
    public String getLanguage()
    {
        String langstring = getLanguageCode();
        if( langstring.length()>0 && getCountryCode().length()>0 )
            langstring += "-"+getCountryCode();                                 // NOI18N
        return langstring;
    }

    /**
     * Returns a string representation
     * as an Java locale (xx_YY).
     */
    public String getLocale()
    {
        if( locale==null )
            return "";                                                          // NOI18N
        else
            return locale.toString();
    }
    
    /**
     * Returns only a language (XX).
     */
    public String getLanguageCode()
    {
        if( this.languageCode==null )
            return "";                                                          // NOI18N
        else
            return this.languageCode;
    }
    
    /**
     * Returns only a country (YY).
     */
    public String getCountryCode()
    {
        if( this.countryCode==null )
            return "";                                                          // NOI18N
        else
            return this.countryCode;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * The array of all the languages.
     * <p>
     * I've commented out a lot of rarely used ones to make a combobox list shorter.
     */
    public static final Language[] LANGUAGES = new Language[]
    {
//        new Language("AA"),       // NOI18N  // AFAR
//        new Language("AA-ET"),       // NOI18N  // AFAR (ETHIOPIA)
//        new Language("AA-DJ"),       // NOI18N  // AFAR (DJIBOUTI)
//        new Language("AA-ER"),       // NOI18N  // AFAR (ERITREA)
//        new Language("AB"),       // NOI18N  // ABKHAZIAN
//        new Language("AE"),       // NOI18N  // AVESTAN
        new Language("AF"),       // NOI18N  // AFRIKAANS
//        new Language("AF-NA"),       // NOI18N  // AFRIKAANS (NAMIBIA)
//        new Language("AF-ZA"),       // NOI18N  // AFRIKAANS (SOUTH AFRICA)
//        new Language("AF-ZW"),       // NOI18N  // AFRIKAANS (ZIMBABWAE)
//        new Language("AK"),       // NOI18N  // AKAN
//        new Language("AM"),       // NOI18N  // AMHARIC
//        new Language("AN"),       // NOI18N  // ARAGONESE
        new Language("AR"),       // NOI18N  // ARABIC
//        new Language("AR-AE"),       // NOI18N  // ARABIC (UNITED ARAB EMIRATES)
//        new Language("AR-BH"),       // NOI18N  // ARABIC (BAHRAIN)
//        new Language("AR-DZ"),       // NOI18N  // ARABIC (ALGERIA)
//        new Language("AR-EG"),       // NOI18N  // ARABIC (EGYPT)
//        new Language("AR-IQ"),       // NOI18N  // ARABIC (IRAQ)
//        new Language("AR-JO"),       // NOI18N  // ARABIC (JORDAN)
//        new Language("AR-KW"),       // NOI18N  // ARABIC (KUWAIT)
//        new Language("AR-LB"),       // NOI18N  // ARABIC (LEBANON)
//        new Language("AR-LY"),       // NOI18N  // ARABIC (LIBYA)
//        new Language("AR-MA"),       // NOI18N  // ARABIC (MOROCCO)
//        new Language("AR-OM"),       // NOI18N  // ARABIC (OMAN)
//        new Language("AR-QA"),       // NOI18N  // ARABIC (QATAR)
//        new Language("AR-SA"),       // NOI18N  // ARABIC (SAUDI ARABIA)
//        new Language("AR-SD"),       // NOI18N  // ARABIC (SUDAN)
//        new Language("AR-SY"),       // NOI18N  // ARABIC (SYRIA)
//        new Language("AR-TN"),       // NOI18N  // ARABIC (TUNISIA)
//        new Language("AR-US"),       // NOI18N  // ARABIC (UNITED STATES)
//        new Language("AR-YE"),       // NOI18N  // ARABIC (YEMEN)
                
//        new Language("AS"),       // NOI18N  // ASSAMESE
//        new Language("AV"),       // NOI18N  // AVARIC
//        new Language("AY"),       // NOI18N  // AYMARA
//        new Language("AZ"),       // NOI18N  // AZERBAIJANI
//        new Language("AZ-AZ"),       // NOI18N  // AZERBAIJANI (AZERBAIJAN)
//        new Language("BA"),       // NOI18N  // BASHKIR
                
        new Language("BE"),       // NOI18N  // BYELORUSSIAN
//        new Language("BE-BY"),       // NOI18N  // BYELORUSSIAN (BELARUS)
        new Language("BG"),       // NOI18N  // BULGARIAN
//        new Language("BG-BG"),       // NOI18N  // BULGARIAN (BULGARIA)
        
//        new Language("BH"),       // NOI18N  // BIHARI
//        new Language("BI"),       // NOI18N  // BISLAMA
//        new Language("BM"),       // NOI18N  // BAMBARA
//        new Language("BN"),       // NOI18N  // BENGALI
//        new Language("BO"),       // NOI18N  // TIBETAN
//        new Language("BR"),       // NOI18N  // BRETON
        new Language("BS"),       // NOI18N  // BOSNIAN
//        new Language("BS-BA"),       // NOI18N  // BOSNIAN (BOSNIA AND HERZEGOVINA)
        
        new Language("CA"),       // NOI18N  // CATALAN
//        new Language("CA-ES"),       // NOI18N  // CATALAN (SPAIN)
        
//        new Language("CE"),       // NOI18N  // CHECHEN 
//        new Language("CH"),       // NOI18N  // CHAMORRO
        new Language("CO"),       // NOI18N  // CORSICAN
//        new Language("CR"),       // NOI18N  // CREE
        
        new Language("CS"),       // NOI18N  // CZECH
//        new Language("CS-CZ"),       // NOI18N  // CZECH (CZECH REPUBLIC)
        
//        new Language("CU"),       // NOI18N  // CHURCH SLAVIC; OLD SLAVONIC; CHURCH SLAVONIC; OLD BULGARIAN; OLD CHURCH SLAVONIC
//        new Language("CV"),       // NOI18N  // CHUVASH
        new Language("CY"),       // NOI18N  // WELSH
        
        new Language("DA"),       // NOI18N  // DANISH
//        new Language("DA-DK"),       // NOI18N  // DANISH (DENMARK)
        new Language("DE"),       // NOI18N  // GERMAN
//        new Language("DE-AT"),       // NOI18N  // GERMAN (AUSTRIA)
//        new Language("DE-CH"),       // NOI18N  // GERMAN (SWITZERLAND)
//        new Language("DE-DE"),       // NOI18N  // GERMAN (GERMANY)
//        new Language("DE-LU"),       // NOI18N  // GERMAN (LUXEMBOURG)
        
//        new Language("DV"),       // NOI18N  // DIVEHI
//        new Language("DZ"),       // NOI18N  // DZONGKHA
//        new Language("DZ-BT"),       // NOI18N  // DZONGKHA (BHUTAN)
//        new Language("EE"),       // NOI18N  // EWE
        
        new Language("EL"),       // NOI18N  // GREEK
//        new Language("EL-GR"),       // NOI18N  // GREEK (GREECE)
        new Language("EN"),       // NOI18N  // ENGLISH
//        new Language("EN-AU"),       // NOI18N  // ENGLISH (AUSTRALIA)
//        new Language("EN-CA"),       // NOI18N  // ENGLISH (CANADA)
        new Language("EN-GB"),       // NOI18N  // ENGLISH (UNITED KINGDOM)
//        new Language("EN-IE"),       // NOI18N  // ENGLISH (IRELAND)
//        new Language("EN-IN"),       // NOI18N  // ENGLISH (INDIA)
//        new Language("EN-NZ"),       // NOI18N  // ENGLISH (NEW ZEALAND)
        new Language("EN-US"),       // NOI18N  // ENGLISH (UNITED STATES)
//        new Language("EN-ZA"),       // NOI18N  // ENGLISH (SOUTH AFRICA)
        
        new Language("EO"),       // NOI18N  // ESPERANTO
        
        new Language("ES"),       // NOI18N  // SPANISH
        new Language("ES-AR"),       // NOI18N  // SPANISH (ARGENTINA)
//        new Language("ES-BO"),       // NOI18N  // SPANISH (BOLIVIA)
//        new Language("ES-CL"),       // NOI18N  // SPANISH (CHILE)
//        new Language("ES-CO"),       // NOI18N  // SPANISH (COLOMBIA)
//        new Language("ES-CR"),       // NOI18N  // SPANISH (COSTA RICA)
//        new Language("ES-DO"),       // NOI18N  // SPANISH (DOMINICAN REPUBLIC)
//        new Language("ES-EC"),       // NOI18N  // SPANISH (ECUADOR)
        new Language("ES-ES"),       // NOI18N  // SPANISH (SPAIN)
//        new Language("ES-GT"),       // NOI18N  // SPANISH (GUATEMALA)
//        new Language("ES-HN"),       // NOI18N  // SPANISH (HONDURAS)
        new Language("ES-MX"),       // NOI18N  // SPANISH (MEXICO)
//        new Language("ES-NI"),       // NOI18N  // SPANISH (NICARAGUA)
//        new Language("ES-PA"),       // NOI18N  // SPANISH (PANAMA)
//        new Language("ES-PE"),       // NOI18N  // SPANISH (PERU)
//        new Language("ES-PR"),       // NOI18N  // SPANISH (PUERTO RICO)
//        new Language("ES-PY"),       // NOI18N  // SPANISH (PARAGUAY)
//        new Language("ES-SV"),       // NOI18N  // SPANISH (EL SALVADOR)
//        new Language("ES-US"),       // NOI18N  // SPANISH (UNITED STATES)
//        new Language("ES-UY"),       // NOI18N  // SPANISH (URUGUAY)
//        new Language("ES-VE"),       // NOI18N  // SPANISH (VENEZUELA)

        new Language("ET"),       // NOI18N  // ESTONIAN
//        new Language("ET-EE"),       // NOI18N  // ESTONIAN (ESTONIA)
        
//        new Language("EU"),       // NOI18N  // BASQUE
                
//        new Language("FA"),       // NOI18N  // PERSIAN; FARSI
//        new Language("FA-AF"),       // NOI18N  // PERSIAN; FARSI (AFGANISTAN)
//        new Language("FA-IR"),       // NOI18N  // PERSIAN; FARSI (IRAN)
                
//        new Language("FF"),       // NOI18N  // FULAH
        
        new Language("FI"),       // NOI18N  // FINNISH
//        new Language("FI-FI"),       // NOI18N  // FINNISH (FINLAND)
        
//        new Language("FJ"),       // NOI18N  // FIJIAN
//        new Language("FJ-FJ"),       // NOI18N  // FIJIAN (FIJI)
//        new Language("FO"),       // NOI18N  // FAROESE
        
        new Language("FR"),       // NOI18N  // FRENCH
//        new Language("FR-BE"),       // NOI18N  // FRENCH (BELGIUM)
        new Language("FR-CA"),       // NOI18N  // FRENCH (CANADA)
//        new Language("FR-CH"),       // NOI18N  // FRENCH (SWITZERLAND)
        new Language("FR-FR"),       // NOI18N  // FRENCH (FRANCE)
//        new Language("FR-LU"),       // NOI18N  // FRENCH (LUXEMBOURG)
        
//        new Language("FY"),       // NOI18N  // FRISIAN
                
        new Language("GA"),       // NOI18N  // IRISH
//        new Language("GA-IE"),       // NOI18N  // IRISH (IRELAND)
                
        new Language("GD"),       // NOI18N  // GAELIC; SCOTTISH GAELIC
                
//        new Language("GL"),       // NOI18N  // GALLEGAN
//        new Language("GN"),       // NOI18N  // GUARANI
//        new Language("GU"),       // NOI18N  // GUJARATI
//        new Language("GV"),       // NOI18N  // MANX
//        new Language("HA"),       // NOI18N  // HAUSA
        
        new Language("HE"),       // NOI18N  // HEBREW
//        new Language("HE-IL"),       // NOI18N  // HEBREW (ISRAEL)
                
        new Language("HI"),       // NOI18N  // HINDI
//        new Language("HI-IN"),       // NOI18N  // HINDI (INDIA)
        
//        new Language("HO"),       // NOI18N  // HIRI MOTU
        
        new Language("HR"),       // NOI18N  // CROATIAN
//        new Language("HR-HR"),       // NOI18N  // CROATIAN (CROATIA)
        
//        new Language("HT"),       // NOI18N  // HAITIAN; HAITIAN CREOLE
//        new Language("HT-HT"),       // NOI18N  // HAITIAN; HAITIAN CREOLE (HAITI)
        
        new Language("HU"),       // NOI18N  // HUNGARIAN
//        new Language("HU-HU"),       // NOI18N  // HUNGARIAN (HUNGARY)
        
        new Language("HY"),       // NOI18N  // ARMENIAN
//        new Language("HY-AM"),       // NOI18N  // ARMENIAN (ARMENIA)

//        new Language("HZ"),       // NOI18N  // HERERO
//        new Language("IA"),       // NOI18N  // INTERLINGUA; INTERNATIONAL AUXILIARY
//        new Language("ID"),       // NOI18N  // INDONESIAN
//        new Language("ID-ID"),       // NOI18N  // INDONESIAN (INDONESIA)
//        new Language("IG"),       // NOI18N  // IGBO
//        new Language("II"),       // NOI18N  // SICHUAN YI
//        new Language("IK"),       // NOI18N  // INUPIAQ
//        new Language("IO"),       // NOI18N  // IDO
        
        new Language("IS"),       // NOI18N  // ICELANDIC
//        new Language("IS-IS"),       // NOI18N  // ICELANDIC (ICELAND)
                
        new Language("IT"),       // NOI18N  // ITALIAN
//        new Language("IT-CH"),       // NOI18N  // ITALIAN (SWITZERLAND)
//        new Language("IT-IT"),       // NOI18N  // ITALIAN (ITALY)
        
//        new Language("IU"),       // NOI18N  // INUKTITUT
        
        new Language("JA"),       // NOI18N  // JAPANESE
//        new Language("JA-JP"),       // NOI18N  // JAPANESE (JAPAN)

//        new Language("JI"),       // NOI18N  // YIDDISH
        
//        new Language("JV"),       // NOI18N  // JAVANESE
                
        new Language("KA"),       // NOI18N  // GEORGIAN
//        new Language("KA-GE"),       // NOI18N  // GEORGIAN (GEORGIA)
                
//        new Language("KG"),       // NOI18N  // KONGO
//        new Language("KI"),       // NOI18N  // KIKUYU; GIKUYU
//        new Language("KJ"),       // NOI18N  // KUANYAMA; KWANYAMA
                
        new Language("KK"),       // NOI18N  // KAZAKH
//        new Language("KK-KZ"),       // NOI18N  // KAZAKH (KAZAKHSTAN)
                
//        new Language("KL"),       // NOI18N  // KALAALLISUT; GREENLANDIC
//        new Language("KM"),       // NOI18N  // KHMER
//        new Language("KM-KH"),       // NOI18N  // KHMER (CAMBODIA)
//        new Language("KN"),       // NOI18N  // KANNADA
        
        new Language("KO"),       // NOI18N  // KOREAN
//        new Language("KO-KR"),       // NOI18N  // KOREAN (SOUTH KOREA)
        
//        new Language("KR"),       // NOI18N  // KANURI
//        new Language("KS"),       // NOI18N  // KASHMIRI
//        new Language("KU"),       // NOI18N  // KURDISH
//        new Language("KV"),       // NOI18N  // KOMI
//        new Language("KW"),       // NOI18N  // CORNISH
                
//        new Language("KY"),       // NOI18N  // KIRGHIZ
//        new Language("KY-KG"),       // NOI18N  // KIRGHIZ (KYRGYZSTAN)
                
        new Language("LA"),       // NOI18N  // LATIN
        new Language("LB"),       // NOI18N  // LUXEMBOURGISH; LETZEBURGESCH
//        new Language("LB-LU"),       // NOI18N  // LUXEMBOURGISH (LUXEMBOURG)
//        new Language("LG"),       // NOI18N  // GANDA
//        new Language("LI"),       // NOI18N  // LIMBURGAN; LIMBURGER; LIMBURGISH
//        new Language("LN"),       // NOI18N  // LINGALA
//        new Language("LO"),       // NOI18N  // LAO
//        new Language("LO-LA"),       // NOI18N  // LAO (LAO)
        
        new Language("LT"),       // NOI18N  // LITHUANIAN
//        new Language("LT-LT"),       // NOI18N  // LITHUANIAN (LITHUANIA)
        
//        new Language("LU"),       // NOI18N  // LUBA-KATANGA
        
        new Language("LV"),       // NOI18N  // LATVIAN (LETTISH)
//        new Language("LV-LV"),       // NOI18N  // LATVIAN (LETTISH) (LATVIA)
        
//        new Language("MG"),       // NOI18N  // MALAGASY
//        new Language("MG-MG"),       // NOI18N  // MALAGASY (MADAGASCAR)
//        new Language("MH"),       // NOI18N  // MARSHALLESE
//        new Language("MH-MH"),       // NOI18N  // MARSHALLESE (MARSHALL ISLANDS)
//        new Language("MI"),       // NOI18N  // MAORI
        
//        new Language("MK"),       // NOI18N  // MACEDONIAN
//        new Language("MK-MK"),       // NOI18N  // MACEDONIAN (MACEDONIA)
        
//        new Language("ML"),       // NOI18N  // MALAYALAM
                
        new Language("MN"),       // NOI18N  // MONGOLIAN
//        new Language("MN-MN"),       // NOI18N  // MONGOLIAN (MONGOLIA)
                
        new Language("MO"),       // NOI18N  // MOLDAVIAN
//        new Language("MO-MD"),       // NOI18N  // MOLDAVIAN (MOLDOVA)
                
//        new Language("MR"),       // NOI18N  // MARATHI
//        new Language("MS"),       // NOI18N  // MALAY
//        new Language("MS-MY"),       // NOI18N  // MALAY (MALAYSIA)
//        new Language("MT"),       // NOI18N  // MALTESE
//        new Language("MT-MT"),       // NOI18N  // MALTESE (MALTA)
//        new Language("MY"),       // NOI18N  // BURMESE
//        new Language("MY-MM"),       // NOI18N  // BURMESE (MYANMAR)
//        new Language("NA"),       // NOI18N  // NAURU
                
        new Language("NB"),       // NOI18N  // NORWEGIAN BOKM?L; BOKM?L, NORWEGIAN
//        new Language("NB-NO"),       // NOI18N  // BOKM?L (NORWAY)
                
//        new Language("ND"),       // NOI18N  // NDEBELE, NORTH; NORTH NDEBELE
//        new Language("NE"),       // NOI18N  // NEPALI
//        new Language("NE-NP"),       // NOI18N  // NEPALI (NEPAL)
//        new Language("NG"),       // NOI18N  // NDONGA
        
        new Language("NL"),       // NOI18N  // DUTCH
//        new Language("NL-BE"),       // NOI18N  // DUTCH (BELGIUM)
//        new Language("NL-NL"),       // NOI18N  // DUTCH (NETHERLANDS)
        
        new Language("NN"),       // NOI18N  // NORWEGIAN NYNORSK; NYNORSK, NORWEGIAN
//        new Language("NN-NO"),       // NOI18N  // NYNORSK (NORWAY)
        
        new Language("NO"),       // NOI18N  // NORWEGIAN
//        new Language("NO-NO"),       // NOI18N  // NORWEGIAN (NORWAY)
        
//        new Language("NR"),       // NOI18N  // NDEBELE, SOUTH; SOUTH NDEBELE
//        new Language("NV"),       // NOI18N  // NAVAJO; NAVAHO
//        new Language("NY"),       // NOI18N  // CHICHEWA; CHEWA; NYANJA
//        new Language("OC"),       // NOI18N  // OCCITAN (POST 1500); PROVEN?AL
//        new Language("OJ"),       // NOI18N  // OJIBWA
//        new Language("OM"),       // NOI18N  // OROMO
//        new Language("OR"),       // NOI18N  // ORIYA
//        new Language("OS"),       // NOI18N  // OSSETIAN; OSSETIC
//        new Language("PA"),       // NOI18N  // PANJABI; PUNJABI
//        new Language("PA-IN"),       // NOI18N  // PANJABI; PUNJABI (INDIA)
//        new Language("PA-PK"),       // NOI18N  // PANJABI; PUNJABI (PAKISTAN)
//        new Language("PI"),       // NOI18N  // PALI
        
        new Language("PL"),       // NOI18N  // POLISH
//        new Language("PL-PL"),       // NOI18N  // POLISH (POLAND)
        
//        new Language("PS"),       // NOI18N  // PUSHTO
        
        new Language("PT"),       // NOI18N  // PORTUGUESE
        new Language("PT-BR"),       // NOI18N  // PORTUGUESE (BRAZIL)
        new Language("PT-PT"),       // NOI18N  // PORTUGUESE (PORTUGAL)
        
//        new Language("QU"),       // NOI18N  // QUECHUA
//        new Language("RM"),       // NOI18N  // RAETO-ROMANCE
//        new Language("RN"),       // NOI18N  // RUNDI
        
        new Language("RO"),       // NOI18N  // ROMANIAN
//        new Language("RO-RO"),       // NOI18N  // ROMANIAN (ROMANIA)
                
        new Language("RU"),       // NOI18N  // RUSSIAN
//        new Language("RU-BY"),       // NOI18N  // RUSSIAN (BELORUS)
//        new Language("RU-RU"),       // NOI18N  // RUSSIAN (RUSSIA)
        
//        new Language("RW"),       // NOI18N  // KINYARWANDA
//        new Language("SA"),       // NOI18N  // SANSKRIT
        new Language("SC"),       // NOI18N  // SARDINIAN
//        new Language("SD"),       // NOI18N  // SINDHI
//        new Language("SD-IN"),       // NOI18N  // SINDHI (INDIA)
//        new Language("SD-PK"),       // NOI18N  // SINDHI (PAKISTAN)
//        new Language("SE"),       // NOI18N  // NORTHERN SAMI
//        new Language("SG"),       // NOI18N  // SANGO
        
        new Language("SH"),       // NOI18N  // SERBO-CROATIAN
//        new Language("SI"),       // NOI18N  // SINHALA; SINHALESE
        
        new Language("SK"),       // NOI18N  // SLOVAK
//        new Language("SK-SK"),       // NOI18N  // SLOVAK (SLOVAKIA)
                
        new Language("SL"),       // NOI18N  // SLOVENIAN
//        new Language("SL-SI"),       // NOI18N  // SLOVENIAN (SLOVENIA)
        
//        new Language("SM"),       // NOI18N  // SAMOAN 
//        new Language("SM-AS"),       // NOI18N  // SAMOAN (AMERICAN SAMOA)
//        new Language("SM-WS"),       // NOI18N  // SAMOAN (SAMOA)
                
//        new Language("SN"),       // NOI18N  // SHONA
//        new Language("SO"),       // NOI18N  // SOMALI
//        new Language("SO-SO"),       // NOI18N  // SOMALI (SOMALIA)
                
        new Language("SQ"),       // NOI18N  // ALBANIAN
//        new Language("SQ-AL"),       // NOI18N  // ALBANIAN (ALBANIA)
                
        new Language("SR"),       // NOI18N  // SERBIAN
//        new Language("SR-CS"),       // NOI18N  // SERBIAN (SERBIA AND MONTENEGRO)
                
//        new Language("SS"),       // NOI18N  // SWATI
//        new Language("ST"),       // NOI18N  // SOTHO, SOUTHERN 
//        new Language("SU"),       // NOI18N  // SUNDANESE 
        
        new Language("SV"),       // NOI18N  // SWEDISH
//        new Language("SV-SE"),       // NOI18N  // SWEDISH (SWEDEN)
        
//        new Language("SW"),       // NOI18N  // SWAHILI
//        new Language("TA"),       // NOI18N  // TAMIL
//        new Language("TE"),       // NOI18N  // TELUGU
//        new Language("TG"),       // NOI18N  // TAJIK
//        new Language("TG-TJ"),       // NOI18N  // TAJIK (TAJIKISTAN)
        
        new Language("TH"),       // NOI18N  // THAI
//        new Language("TH-TH"),       // NOI18N  // THAI (THAILAND)
        
//        new Language("TI"),       // NOI18N  // TIGRINYA
//        new Language("TK"),       // NOI18N  // TURKMEN
//        new Language("TK-TM"),       // NOI18N  // TURKMEN (TURKMENISTAN)
//        new Language("TL"),       // NOI18N  // TAGALOG
//        new Language("TN"),       // NOI18N  // TSWANA
//        new Language("TO"),       // NOI18N  // TONGA
//        new Language("TO-TO"),       // NOI18N  // TONGA (TONGA ISLANDS)

        new Language("TR"),       // NOI18N  // TURKISH
//        new Language("TR-TR"),       // NOI18N  // TURKISH (TURKEY)
        
//        new Language("TS"),       // NOI18N  // TSONGA
                
        new Language("TT"),       // NOI18N  // TATAR 
                
//        new Language("TW"),       // NOI18N  // TWI
//        new Language("TY"),       // NOI18N  // TAHITIAN
//        new Language("UG"),       // NOI18N  // UIGHUR; UYGHUR
        
        new Language("UK"),       // NOI18N  // UKRAINIAN
//        new Language("UK-UA"),       // NOI18N  // UKRAINIAN (UKRAINE)
        
//        new Language("UR"),       // NOI18N  // URDU
//        new Language("UR-IN"),       // NOI18N  // URDU (INDIA)
//        new Language("UR-PK"),       // NOI18N  // URDU (PAKISTAN)
                
//        new Language("UZ"),       // NOI18N  // UZBEK
//        new Language("UZ-UZ"),       // NOI18N  // UZBEK (UZBEKISTAN)
//        new Language("VE"),       // NOI18N  // VENDA 
//        new Language("VI"),       // NOI18N  // VIETNAMESE
//        new Language("VI-VN"),       // NOI18N  // VIETNAMESE (VIETNAM)
//        new Language("VO"),       // NOI18N  // VOLAP?K
//        new Language("WA"),       // NOI18N  // WALLOON
//        new Language("WO"),       // NOI18N  // WOLOF
//        new Language("XH"),       // NOI18N  // XHOSA
//        new Language("YO"),       // NOI18N  // YORUBA
//        new Language("ZA"),       // NOI18N  // ZHUANG; CHUANG
        
//        new Language("ZH"),       // NOI18N  // CHINESE
        new Language("ZH-CN"),       // NOI18N  // CHINESE SIMPLIFIED (CHINA)
//        new Language("ZH-HK"),       // NOI18N  // CHINESE (HONG KONG)
        new Language("ZH-TW"),       // NOI18N  // CHINESE TRADITIONAL (TAIWAN)
        
//        new Language("ZU"),       // NOI18N  // ZULU
        
    };

    /**
     * Indicates whether some other language is "equal to" this one.
     */
    public boolean equals(Object lang) 
    {
        if( this==lang )
            return true;
        if( lang==null )
            return false;
        if( !(lang instanceof Language) )
            return false;
        Language that = (Language) lang;
        return this.getLocale().equals(that.getLocale());
    }

    /**
     * Returns a hash code value for the language.
     * Basically returns a hashcode of the underlying Locale object.
     */
    public int hashCode() 
    {
        return locale.hashCode();
    }

}
