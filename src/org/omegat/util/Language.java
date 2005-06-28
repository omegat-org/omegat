/**************************************************************************
 OmegaT - Java based Computer Assisted Translation (CAT) tool
 Copyright (C) 2002-2005  Keith Godfrey et al
                          keithgodfrey@users.sourceforge.net
                          907.223.2039

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

/**
 * This class is here, because the Locale
 * has hard-coded '_' inside,
 * and we must adhere to ISO standard LL-CC.
 *
 * @author Maxym Mykhalchuk
 */
public class Language
{
    private Locale locale;
    
    /** Creates a new instance of Language, based on Locale */
    public Language(Locale locale)
    {
        this.locale = locale;
    }
    /** 
     * Creates a new instance of Language, based on a string 
     * of a form "xx_YY" or "XX-YY", where xx/XX is a language ISO code,
     * and YY is a country ISO code.
     */
    public Language(String str)
    {
        if( str==null || str.length()==0 )
            this.locale = null;
        else
        {
            String lang = str.substring(0, 2).toLowerCase();
            if( str.length()<=2 )
                this.locale = new Locale(lang);
            else
            {
                String country = str.substring(3, 5).toUpperCase();
                this.locale = new Locale(lang, country);
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
     * as an ISO language code (xx-YY).
     */
    public String toString()
    {
        return getISOLanguage();
    }
    
    /**
     * Returns a string representation
     * as an ISO language code (xx-YY).
     */
    public String getISOLanguage()
    {
        if( locale==null || locale.toString().length()==0 )
            return "";                                                          // NOI18N
        else
        {
            String localestring = locale.getLanguage().toLowerCase();
            if( !"".equals(locale.getCountry()) )                               // NOI18N
                localestring += "-"+locale.getCountry().toUpperCase();          // NOI18N
            return localestring;
        }
    }

    /**
     * Returns a string representation
     * as an Java locale (xx_YY).
     */
    public String getLocale()
    {
        if( locale==null || locale.toString().length()==0 )
            return "";                                                          // NOI18N
        else
        {
            String localestring = locale.getLanguage().toLowerCase();
            if( !"".equals(locale.getCountry()) )                               // NOI18N
                localestring += "_"+locale.getCountry().toUpperCase();          // NOI18N
            return localestring;
        }
    }
    
    /**
     * Returns only a language (xx).
     */
    public String getLanguageCode()
    {
        if( locale==null || locale.toString().length()==0 )
            return "";                                                          // NOI18N
        else
        {
            return locale.getLanguage().toLowerCase();
        }
    }
    
    /**
     * Returns only a country (YY).
     */
    public String getCountryCode()
    {
        if( locale==null || locale.toString().length()==0 || 
                "".equals(locale.getCountry()) )                                // NOI18N
            return "";                                                          // NOI18N
        else
        {
            return locale.getCountry().toUpperCase();
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////
    
    /**
     * The array of all the languages.
     */
    public static final Language[] LANGUAGES = new Language[]
    {
        new Language("aa"),       // NOI18N  // Afar
        new Language("aa-ET"),       // NOI18N  // Afar (Ethiopia)
        new Language("aa-DJ"),       // NOI18N  // Afar (Djibouti)
        new Language("aa-ER"),       // NOI18N  // Afar (Eritrea)
        new Language("ab"),       // NOI18N  // Abkhazian
        new Language("ae"),       // NOI18N  // Avestan
        new Language("af"),       // NOI18N  // Afrikaans
        new Language("af-NA"),       // NOI18N  // Afrikaans (Namibia)
        new Language("af-ZA"),       // NOI18N  // Afrikaans (South Africa)
        new Language("af-ZW"),       // NOI18N  // Afrikaans (Zimbabwae)
        new Language("ak"),       // NOI18N  // Akan
        new Language("am"),       // NOI18N  // Amharic
        new Language("an"),       // NOI18N  // Aragonese
        new Language("ar"),       // NOI18N  // Arabic
        new Language("ar-AE"),       // NOI18N  // Arabic (United Arab Emirates)
        new Language("ar-BH"),       // NOI18N  // Arabic (Bahrain)
        new Language("ar-DZ"),       // NOI18N  // Arabic (Algeria)
        new Language("ar-EG"),       // NOI18N  // Arabic (Egypt)
        new Language("ar-IQ"),       // NOI18N  // Arabic (Iraq)
        new Language("ar-JO"),       // NOI18N  // Arabic (Jordan)
        new Language("ar-KW"),       // NOI18N  // Arabic (Kuwait)
        new Language("ar-LB"),       // NOI18N  // Arabic (Lebanon)
        new Language("ar-LY"),       // NOI18N  // Arabic (Libya)
        new Language("ar-MA"),       // NOI18N  // Arabic (Morocco)
        new Language("ar-OM"),       // NOI18N  // Arabic (Oman)
        new Language("ar-QA"),       // NOI18N  // Arabic (Qatar)
        new Language("ar-SA"),       // NOI18N  // Arabic (Saudi Arabia)
        new Language("ar-SD"),       // NOI18N  // Arabic (Sudan)
        new Language("ar-SY"),       // NOI18N  // Arabic (Syria)
        new Language("ar-TN"),       // NOI18N  // Arabic (Tunisia)
        new Language("ar-US"),       // NOI18N  // Arabic (United States)
        new Language("ar-YE"),       // NOI18N  // Arabic (Yemen)
                
        new Language("as"),       // NOI18N  // Assamese
        new Language("av"),       // NOI18N  // Avaric
        new Language("ay"),       // NOI18N  // Aymara
        new Language("az"),       // NOI18N  // Azerbaijani
        new Language("az-AZ"),       // NOI18N  // Azerbaijani (Azerbaijan)
        new Language("ba"),       // NOI18N  // Bashkir
                
        new Language("be"),       // NOI18N  // Byelorussian
        new Language("be-BY"),       // NOI18N  // Byelorussian (Belarus)
        new Language("bg"),       // NOI18N  // Bulgarian
        new Language("bg-BG"),       // NOI18N  // Bulgarian (Bulgaria)
        
        new Language("bh"),       // NOI18N  // Bihari
        new Language("bi"),       // NOI18N  // Bislama
        new Language("bm"),       // NOI18N  // Bambara
        new Language("bn"),       // NOI18N  // Bengali
        new Language("bo"),       // NOI18N  // Tibetan
        new Language("br"),       // NOI18N  // Breton
        new Language("bs"),       // NOI18N  // Bosnian
        new Language("bs-BA"),       // NOI18N  // Bosnian (Bosnia and Herzegovina)
        
        new Language("ca"),       // NOI18N  // Catalan
        new Language("ca-ES"),       // NOI18N  // Catalan (Spain)
        
        new Language("ce"),       // NOI18N  // Chechen 
        new Language("ch"),       // NOI18N  // Chamorro
        new Language("co"),       // NOI18N  // Corsican
        new Language("cr"),       // NOI18N  // Cree
        
        new Language("cs"),       // NOI18N  // Czech
        new Language("cs-CZ"),       // NOI18N  // Czech (Czech Republic)
        
        new Language("cu"),       // NOI18N  // Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic
        new Language("cv"),       // NOI18N  // Chuvash
        new Language("cy"),       // NOI18N  // Welsh
        
        new Language("da"),       // NOI18N  // Danish
        new Language("da-DK"),       // NOI18N  // Danish (Denmark)
        new Language("de"),       // NOI18N  // German
        new Language("de-AT"),       // NOI18N  // German (Austria)
        new Language("de-CH"),       // NOI18N  // German (Switzerland)
        new Language("de-DE"),       // NOI18N  // German (Germany)
        new Language("de-LU"),       // NOI18N  // German (Luxembourg)
        
        new Language("dv"),       // NOI18N  // Divehi
        new Language("dz"),       // NOI18N  // Dzongkha
        new Language("dz-BT"),       // NOI18N  // Dzongkha (Bhutan)
        new Language("ee"),       // NOI18N  // Ewe
        
        new Language("el"),       // NOI18N  // Greek
        new Language("el-GR"),       // NOI18N  // Greek (Greece)
        new Language("en"),       // NOI18N  // English
        new Language("en-AU"),       // NOI18N  // English (Australia)
        new Language("en-CA"),       // NOI18N  // English (Canada)
        new Language("en-GB"),       // NOI18N  // English (United Kingdom)
        new Language("en-IE"),       // NOI18N  // English (Ireland)
        new Language("en-IN"),       // NOI18N  // English (India)
        new Language("en-NZ"),       // NOI18N  // English (New Zealand)
        new Language("en-US"),       // NOI18N  // English (United States)
        new Language("en-ZA"),       // NOI18N  // English (South Africa)
        
        new Language("eo"),       // NOI18N  // Esperanto
        
        new Language("es"),       // NOI18N  // Spanish
        new Language("es-AR"),       // NOI18N  // Spanish (Argentina)
        new Language("es-BO"),       // NOI18N  // Spanish (Bolivia)
        new Language("es-CL"),       // NOI18N  // Spanish (Chile)
        new Language("es-CO"),       // NOI18N  // Spanish (Colombia)
        new Language("es-CR"),       // NOI18N  // Spanish (Costa Rica)
        new Language("es-DO"),       // NOI18N  // Spanish (Dominican Republic)
        new Language("es-EC"),       // NOI18N  // Spanish (Ecuador)
        new Language("es-ES"),       // NOI18N  // Spanish (Spain)
        new Language("es-GT"),       // NOI18N  // Spanish (Guatemala)
        new Language("es-HN"),       // NOI18N  // Spanish (Honduras)
        new Language("es-MX"),       // NOI18N  // Spanish (Mexico)
        new Language("es-NI"),       // NOI18N  // Spanish (Nicaragua)
        new Language("es-PA"),       // NOI18N  // Spanish (Panama)
        new Language("es-PE"),       // NOI18N  // Spanish (Peru)
        new Language("es-PR"),       // NOI18N  // Spanish (Puerto Rico)
        new Language("es-PY"),       // NOI18N  // Spanish (Paraguay)
        new Language("es-SV"),       // NOI18N  // Spanish (El Salvador)
        new Language("es-US"),       // NOI18N  // Spanish (United States)
        new Language("es-UY"),       // NOI18N  // Spanish (Uruguay)
        new Language("es-VE"),       // NOI18N  // Spanish (Venezuela)
        new Language("et"),       // NOI18N  // Estonian
        new Language("et-EE"),       // NOI18N  // Estonian (Estonia)
        
        new Language("eu"),       // NOI18N  // Basque
                
        new Language("fa"),       // NOI18N  // Persian; Farsi
        new Language("fa-AF"),       // NOI18N  // Persian; Farsi (Afganistan)
        new Language("fa-IR"),       // NOI18N  // Persian; Farsi (Iran)
                
        new Language("ff"),       // NOI18N  // Fulah
        
        new Language("fi"),       // NOI18N  // Finnish
        new Language("fi-FI"),       // NOI18N  // Finnish (Finland)
        
        new Language("fj"),       // NOI18N  // Fijian
        new Language("fj-FJ"),       // NOI18N  // Fijian (Fiji)
        new Language("fo"),       // NOI18N  // Faroese
        
        new Language("fr"),       // NOI18N  // French
        new Language("fr-BE"),       // NOI18N  // French (Belgium)
        new Language("fr-CA"),       // NOI18N  // French (Canada)
        new Language("fr-CH"),       // NOI18N  // French (Switzerland)
        new Language("fr-FR"),       // NOI18N  // French (France)
        new Language("fr-LU"),       // NOI18N  // French (Luxembourg)
        
        new Language("fy"),       // NOI18N  // Frisian
        new Language("ga"),       // NOI18N  // Irish
        new Language("ga-IE"),       // NOI18N  // Irish (Ireland)
        new Language("gd"),       // NOI18N  // Gaelic; Scottish Gaelic
        new Language("gl"),       // NOI18N  // Gallegan
        new Language("gn"),       // NOI18N  // Guarani
        new Language("gu"),       // NOI18N  // Gujarati
        new Language("gv"),       // NOI18N  // Manx
        new Language("ha"),       // NOI18N  // Hausa
        
        new Language("hi"),       // NOI18N  // Hindi
        new Language("hi-IN"),       // NOI18N  // Hindi (India)
        
        new Language("ho"),       // NOI18N  // Hiri Motu
        
        new Language("hr"),       // NOI18N  // Croatian
        new Language("hr-HR"),       // NOI18N  // Croatian (Croatia)
        
        new Language("ht"),       // NOI18N  // Haitian; Haitian Creole
        new Language("ht-HT"),       // NOI18N  // Haitian; Haitian Creole (Haiti)
        
        new Language("hu"),       // NOI18N  // Hungarian
        new Language("hu-HU"),       // NOI18N  // Hungarian (Hungary)
        
        new Language("hy"),       // NOI18N  // Armenian
        new Language("hy-AM"),       // NOI18N  // Armenian (Armenia)
        new Language("hz"),       // NOI18N  // Herero
        new Language("ia"),       // NOI18N  // Interlingua; International Auxiliary
        new Language("id"),       // NOI18N  // Indonesian
        new Language("id-ID"),       // NOI18N  // Indonesian (Indonesia)
        new Language("ig"),       // NOI18N  // Igbo
        new Language("ii"),       // NOI18N  // Sichuan Yi
        new Language("ik"),       // NOI18N  // Inupiaq
        new Language("io"),       // NOI18N  // Ido
        
        new Language("is"),       // NOI18N  // Icelandic
        new Language("is-IS"),       // NOI18N  // Icelandic (Iceland)
        new Language("it"),       // NOI18N  // Italian
        new Language("it-CH"),       // NOI18N  // Italian (Switzerland)
        new Language("it-IT"),       // NOI18N  // Italian (Italy)
        
        new Language("iu"),       // NOI18N  // Inuktitut
        
        new Language("he"),       // NOI18N  // Hebrew
        new Language("he-IL"),       // NOI18N  // Hebrew (Israel)
        new Language("ja"),       // NOI18N  // Japanese
        new Language("ja-JP"),       // NOI18N  // Japanese (Japan)

        new Language("ji"),       // NOI18N  // Yiddish
        
        new Language("jv"),       // NOI18N  // Javanese
        new Language("ka"),       // NOI18N  // Georgian
        new Language("ka-GE"),       // NOI18N  // Georgian (Georgia)
        new Language("kg"),       // NOI18N  // Kongo
        new Language("ki"),       // NOI18N  // Kikuyu; Gikuyu
        new Language("kj"),       // NOI18N  // Kuanyama; Kwanyama
        new Language("kk"),       // NOI18N  // Kazakh
        new Language("kk-KZ"),       // NOI18N  // Kazakh (Kazakhstan)
        new Language("kl"),       // NOI18N  // Kalaallisut; Greenlandic
        new Language("km"),       // NOI18N  // Khmer
        new Language("km-KH"),       // NOI18N  // Khmer (Cambodia)
        new Language("kn"),       // NOI18N  // Kannada
        
        new Language("ko"),       // NOI18N  // Korean
        new Language("ko-KR"),       // NOI18N  // Korean (South Korea)
        
        new Language("kr"),       // NOI18N  // Kanuri
        new Language("ks"),       // NOI18N  // Kashmiri
        new Language("ku"),       // NOI18N  // Kurdish
        new Language("kv"),       // NOI18N  // Komi
        new Language("kw"),       // NOI18N  // Cornish
        new Language("ky"),       // NOI18N  // Kirghiz
        new Language("ky-KG"),       // NOI18N  // Kirghiz (Kyrgyzstan)
        new Language("la"),       // NOI18N  // Latin
        new Language("lb"),       // NOI18N  // Luxembourgish; Letzeburgesch
        new Language("lb-LU"),       // NOI18N  // Luxembourgish (Luxembourg)
        new Language("lg"),       // NOI18N  // Ganda
        new Language("li"),       // NOI18N  // Limburgan; Limburger; Limburgish
        new Language("ln"),       // NOI18N  // Lingala
        new Language("lo"),       // NOI18N  // Lao
        new Language("lo-LA"),       // NOI18N  // Lao (Lao)
        
        new Language("lt"),       // NOI18N  // Lithuanian
        new Language("lt-LT"),       // NOI18N  // Lithuanian (Lithuania)
        
        new Language("lu"),       // NOI18N  // Luba-Katanga
        
        new Language("lv"),       // NOI18N  // Latvian (Lettish)
        new Language("lv-LV"),       // NOI18N  // Latvian (Lettish) (Latvia)
        
        new Language("mg"),       // NOI18N  // Malagasy
        new Language("mg-MG"),       // NOI18N  // Malagasy (Madagascar)
        new Language("mh"),       // NOI18N  // Marshallese
        new Language("mh-MH"),       // NOI18N  // Marshallese (Marshall Islands)
        new Language("mi"),       // NOI18N  // Maori
        
        new Language("mk"),       // NOI18N  // Macedonian
        new Language("mk-MK"),       // NOI18N  // Macedonian (Macedonia)
        
        new Language("ml"),       // NOI18N  // Malayalam
        new Language("mn"),       // NOI18N  // Mongolian
        new Language("mn-MN"),       // NOI18N  // Mongolian (Mongolia)
        new Language("mo"),       // NOI18N  // Moldavian
        new Language("mo-MD"),       // NOI18N  // Moldavian (Moldova)
        new Language("mr"),       // NOI18N  // Marathi
        new Language("ms"),       // NOI18N  // Malay
        new Language("ms-MY"),       // NOI18N  // Malay (Malaysia)
        new Language("mt"),       // NOI18N  // Maltese
        new Language("mt-MT"),       // NOI18N  // Maltese (Malta)
        new Language("my"),       // NOI18N  // Burmese
        new Language("my-MM"),       // NOI18N  // Burmese (Myanmar)
        new Language("na"),       // NOI18N  // Nauru
        new Language("nb"),       // NOI18N  // Norwegian Bokm?l; Bokm?l, Norwegian
        new Language("nb-NO"),       // NOI18N  // Bokm?l (Norway)
        new Language("nd"),       // NOI18N  // Ndebele, North; North Ndebele
        new Language("ne"),       // NOI18N  // Nepali
        new Language("ne-NP"),       // NOI18N  // Nepali (Nepal)
        new Language("ng"),       // NOI18N  // Ndonga
        
        new Language("nl"),       // NOI18N  // Dutch
        new Language("nl-BE"),       // NOI18N  // Dutch (Belgium)
        new Language("nl-NL"),       // NOI18N  // Dutch (Netherlands)
        
        new Language("nn"),       // NOI18N  // Norwegian Nynorsk; Nynorsk, Norwegian
        new Language("nn-NO"),       // NOI18N  // Nynorsk (Norway)
        
        new Language("no"),       // NOI18N  // Norwegian
        new Language("no-NO"),       // NOI18N  // Norwegian (Norway)
        
        new Language("nr"),       // NOI18N  // Ndebele, South; South Ndebele
        new Language("nv"),       // NOI18N  // Navajo; Navaho
        new Language("ny"),       // NOI18N  // Chichewa; Chewa; Nyanja
        new Language("oc"),       // NOI18N  // Occitan (post 1500); Proven?al
        new Language("oj"),       // NOI18N  // Ojibwa
        new Language("om"),       // NOI18N  // Oromo
        new Language("or"),       // NOI18N  // Oriya
        new Language("os"),       // NOI18N  // Ossetian; Ossetic
        new Language("pa"),       // NOI18N  // Panjabi; Punjabi
        new Language("pa-IN"),       // NOI18N  // Panjabi; Punjabi (India)
        new Language("pa-PK"),       // NOI18N  // Panjabi; Punjabi (Pakistan)
        new Language("pi"),       // NOI18N  // Pali
        
        new Language("pl"),       // NOI18N  // Polish
        new Language("pl-PL"),       // NOI18N  // Polish (Poland)
        
        new Language("ps"),       // NOI18N  // Pushto
        
        new Language("pt"),       // NOI18N  // Portuguese
        new Language("pt-BR"),       // NOI18N  // Portuguese (Brazil)
        new Language("pt-PT"),       // NOI18N  // Portuguese (Portugal)
        
        new Language("qu"),       // NOI18N  // Quechua
        new Language("rm"),       // NOI18N  // Raeto-Romance
        new Language("rn"),       // NOI18N  // Rundi
        
        new Language("ro"),       // NOI18N  // Romanian
        new Language("ro-RO"),       // NOI18N  // Romanian (Romania)
        new Language("ru"),       // NOI18N  // Russian
        new Language("ru-BY"),       // NOI18N  // Russian (Belorus)
        new Language("ru-MD"),       // NOI18N  // Russian (Moldova)
        new Language("ru-RU"),       // NOI18N  // Russian (Russia)
        new Language("ru-UA"),       // NOI18N  // Russian (Ukraine)
        
        new Language("rw"),       // NOI18N  // Kinyarwanda
        new Language("sa"),       // NOI18N  // Sanskrit
        new Language("sc"),       // NOI18N  // Sardinian
        new Language("sd"),       // NOI18N  // Sindhi
        new Language("sd-IN"),       // NOI18N  // Sindhi (India)
        new Language("sd-PK"),       // NOI18N  // Sindhi (Pakistan)
        new Language("se"),       // NOI18N  // Northern Sami
        new Language("sg"),       // NOI18N  // Sango
        
        new Language("sr"),       // NOI18N  // Serbo-Croatian
        new Language("sh-YU"),       // NOI18N  // Serbo-Croatian (Yugoslavia)
        
        new Language("si"),       // NOI18N  // Sinhala; Sinhalese
        
        new Language("sk"),       // NOI18N  // Slovak
        new Language("sk-SK"),       // NOI18N  // Slovak (Slovakia)
        new Language("sl"),       // NOI18N  // Slovenian
        new Language("sl-SI"),       // NOI18N  // Slovenian (Slovenia)
        
        new Language("sm"),       // NOI18N  // Samoan 
        new Language("sm-AS"),       // NOI18N  // Samoan (American Samoa)
        new Language("sm-WS"),       // NOI18N  // Samoan (Samoa)
        new Language("sn"),       // NOI18N  // Shona
        new Language("so"),       // NOI18N  // Somali
        new Language("so-SO"),       // NOI18N  // Somali (Somalia)
        new Language("sq"),       // NOI18N  // Albanian
        new Language("sq-AL"),       // NOI18N  // Albanian (Albania)
        new Language("sh"),       // NOI18N  // Serbo-Croatian
        new Language("sh-YU"),       // NOI18N  // Serbo-Croatian (Yugoslavia)
        new Language("sr"),       // NOI18N  // Serbian
        new Language("sr-CS"),       // NOI18N  // Serbian (Serbia and Montenegro)
                
        new Language("sq"),       // NOI18N  // Albanian
        new Language("sq-AL"),       // NOI18N  // Albanian (Albania)
                
        new Language("ss"),       // NOI18N  // Swati
        new Language("st"),       // NOI18N  // Sotho, Southern 
        new Language("su"),       // NOI18N  // Sundanese 
        
        new Language("sv"),       // NOI18N  // Swedish
        new Language("sv-SE"),       // NOI18N  // Swedish (Sweden)
        
        new Language("sw"),       // NOI18N  // Swahili
        new Language("ta"),       // NOI18N  // Tamil
        new Language("te"),       // NOI18N  // Telugu
        new Language("tg"),       // NOI18N  // Tajik
        new Language("tg-TJ"),       // NOI18N  // Tajik (Tajikistan)
        
        new Language("th"),       // NOI18N  // Thai
        new Language("th-TH"),       // NOI18N  // Thai (Thailand)
        
        new Language("ti"),       // NOI18N  // Tigrinya
        new Language("tk"),       // NOI18N  // Turkmen
        new Language("tk-TM"),       // NOI18N  // Turkmen (Turkmenistan)
        new Language("tl"),       // NOI18N  // Tagalog
        new Language("tn"),       // NOI18N  // Tswana
        new Language("to"),       // NOI18N  // Tonga
        new Language("to-TO"),       // NOI18N  // Tonga (Tonga Islands)

        new Language("tr"),       // NOI18N  // Turkish
        new Language("tr-TR"),       // NOI18N  // Turkish (Turkey)
        
        new Language("ts"),       // NOI18N  // Tsonga
        new Language("tt"),       // NOI18N  // Tatar 
        new Language("tw"),       // NOI18N  // Twi
        new Language("ty"),       // NOI18N  // Tahitian
        new Language("ug"),       // NOI18N  // Uighur; Uyghur
        
        new Language("uk"),       // NOI18N  // Ukrainian
        new Language("uk-UA"),       // NOI18N  // Ukrainian (Ukraine)
        
        new Language("ur"),       // NOI18N  // Urdu
        new Language("ur-IN"),       // NOI18N  // Urdu (India)
        new Language("ur-PK"),       // NOI18N  // Urdu (Pakistan)
                
        new Language("uz"),       // NOI18N  // Uzbek
        new Language("uz-UZ"),       // NOI18N  // Uzbek (Uzbekistan)
        new Language("ve"),       // NOI18N  // Venda 
        new Language("vi"),       // NOI18N  // Vietnamese
        new Language("vi-VN"),       // NOI18N  // Vietnamese (Vietnam)
        new Language("vo"),       // NOI18N  // Volap?k
        new Language("wa"),       // NOI18N  // Walloon
        new Language("wo"),       // NOI18N  // Wolof
        new Language("xh"),       // NOI18N  // Xhosa
        new Language("yo"),       // NOI18N  // Yoruba
        new Language("za"),       // NOI18N  // Zhuang; Chuang
        
        new Language("zh"),       // NOI18N  // Chinese
        new Language("zh-CN"),       // NOI18N  // Chinese (China)
        new Language("zh-HK"),       // NOI18N  // Chinese (Hong Kong)
        new Language("zh-TW"),       // NOI18N  // Chinese (Taiwan)
        
        new Language("zu"),       // NOI18N  // Zulu
        
    };

}
