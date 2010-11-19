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
            langstring += "-"+getCountryCode();                                 
        return langstring;
    }

    /**
     * Returns a string representation
     * as an Java locale (xx_YY).
     */
    public String getLocale()
    {
        String localestring = getLanguageCode().toLowerCase();
        if( localestring.length()>0 && getCountryCode().length()>0 )
            localestring += "_"+getCountryCode();                               
        return localestring;
    }
    
    /**
     * Returns only a language (XX).
     */
    public String getLanguageCode()
    {
        if( locale==null || locale.toString().length()==0 )
            return "";                                                          
        else
        {
            return locale.getLanguage().toUpperCase();
        }
    }
    
    /**
     * Returns only a country (YY).
     */
    public String getCountryCode()
    {
        if( locale==null || locale.getCountry()==null || locale.getCountry().length()==0 )
            return "";                                                          
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
     * <p>
     * I've commented out a lot of rarely used ones to make a combobox list shorter.
     */
    public static final Language[] LANGUAGES = new Language[]
    {
//        new Language("aa"),         // Afar
//        new Language("aa-ET"),         // Afar (Ethiopia)
//        new Language("aa-DJ"),         // Afar (Djibouti)
//        new Language("aa-ER"),         // Afar (Eritrea)
//        new Language("ab"),         // Abkhazian
//        new Language("ae"),         // Avestan
        new Language("af"),         // Afrikaans
//        new Language("af-NA"),         // Afrikaans (Namibia)
//        new Language("af-ZA"),         // Afrikaans (South Africa)
//        new Language("af-ZW"),         // Afrikaans (Zimbabwae)
//        new Language("ak"),         // Akan
//        new Language("am"),         // Amharic
//        new Language("an"),         // Aragonese
        new Language("ar"),         // Arabic
//        new Language("ar-AE"),         // Arabic (United Arab Emirates)
//        new Language("ar-BH"),         // Arabic (Bahrain)
//        new Language("ar-DZ"),         // Arabic (Algeria)
//        new Language("ar-EG"),         // Arabic (Egypt)
//        new Language("ar-IQ"),         // Arabic (Iraq)
//        new Language("ar-JO"),         // Arabic (Jordan)
//        new Language("ar-KW"),         // Arabic (Kuwait)
//        new Language("ar-LB"),         // Arabic (Lebanon)
//        new Language("ar-LY"),         // Arabic (Libya)
//        new Language("ar-MA"),         // Arabic (Morocco)
//        new Language("ar-OM"),         // Arabic (Oman)
//        new Language("ar-QA"),         // Arabic (Qatar)
//        new Language("ar-SA"),         // Arabic (Saudi Arabia)
//        new Language("ar-SD"),         // Arabic (Sudan)
//        new Language("ar-SY"),         // Arabic (Syria)
//        new Language("ar-TN"),         // Arabic (Tunisia)
//        new Language("ar-US"),         // Arabic (United States)
//        new Language("ar-YE"),         // Arabic (Yemen)
                
//        new Language("as"),         // Assamese
//        new Language("av"),         // Avaric
//        new Language("ay"),         // Aymara
//        new Language("az"),         // Azerbaijani
//        new Language("az-AZ"),         // Azerbaijani (Azerbaijan)
//        new Language("ba"),         // Bashkir
                
        new Language("be"),         // Byelorussian
//        new Language("be-BY"),         // Byelorussian (Belarus)
        new Language("bg"),         // Bulgarian
//        new Language("bg-BG"),         // Bulgarian (Bulgaria)
        
//        new Language("bh"),         // Bihari
//        new Language("bi"),         // Bislama
//        new Language("bm"),         // Bambara
//        new Language("bn"),         // Bengali
//        new Language("bo"),         // Tibetan
//        new Language("br"),         // Breton
//        new Language("bs"),         // Bosnian
//        new Language("bs-BA"),         // Bosnian (Bosnia and Herzegovina)
        
//        new Language("ca"),         // Catalan
//        new Language("ca-ES"),         // Catalan (Spain)
        
//        new Language("ce"),         // Chechen 
//        new Language("ch"),         // Chamorro
//        new Language("co"),         // Corsican
//        new Language("cr"),         // Cree
        
        new Language("cs"),         // Czech
//        new Language("cs-CZ"),         // Czech (Czech Republic)
        
//        new Language("cu"),         // Church Slavic; Old Slavonic; Church Slavonic; Old Bulgarian; Old Church Slavonic
//        new Language("cv"),         // Chuvash
        new Language("cy"),         // Welsh
        
        new Language("da"),         // Danish
//        new Language("da-DK"),         // Danish (Denmark)
        new Language("de"),         // German
//        new Language("de-AT"),         // German (Austria)
//        new Language("de-CH"),         // German (Switzerland)
//        new Language("de-DE"),         // German (Germany)
//        new Language("de-LU"),         // German (Luxembourg)
        
//        new Language("dv"),         // Divehi
//        new Language("dz"),         // Dzongkha
//        new Language("dz-BT"),         // Dzongkha (Bhutan)
//        new Language("ee"),         // Ewe
        
        new Language("el"),         // Greek
//        new Language("el-GR"),         // Greek (Greece)
        new Language("en"),         // English
//        new Language("en-AU"),         // English (Australia)
//        new Language("en-CA"),         // English (Canada)
        new Language("en-GB"),         // English (United Kingdom)
//        new Language("en-IE"),         // English (Ireland)
//        new Language("en-IN"),         // English (India)
//        new Language("en-NZ"),         // English (New Zealand)
        new Language("en-US"),         // English (United States)
//        new Language("en-ZA"),         // English (South Africa)
        
        new Language("eo"),         // Esperanto
        
        new Language("es"),         // Spanish
        new Language("es-AR"),         // Spanish (Argentina)
//        new Language("es-BO"),         // Spanish (Bolivia)
//        new Language("es-CL"),         // Spanish (Chile)
//        new Language("es-CO"),         // Spanish (Colombia)
//        new Language("es-CR"),         // Spanish (Costa Rica)
//        new Language("es-DO"),         // Spanish (Dominican Republic)
//        new Language("es-EC"),         // Spanish (Ecuador)
        new Language("es-ES"),         // Spanish (Spain)
//        new Language("es-GT"),         // Spanish (Guatemala)
//        new Language("es-HN"),         // Spanish (Honduras)
        new Language("es-MX"),         // Spanish (Mexico)
//        new Language("es-NI"),         // Spanish (Nicaragua)
//        new Language("es-PA"),         // Spanish (Panama)
//        new Language("es-PE"),         // Spanish (Peru)
//        new Language("es-PR"),         // Spanish (Puerto Rico)
//        new Language("es-PY"),         // Spanish (Paraguay)
//        new Language("es-SV"),         // Spanish (El Salvador)
//        new Language("es-US"),         // Spanish (United States)
//        new Language("es-UY"),         // Spanish (Uruguay)
//        new Language("es-VE"),         // Spanish (Venezuela)

        new Language("et"),         // Estonian
//        new Language("et-EE"),         // Estonian (Estonia)
        
//        new Language("eu"),         // Basque
                
//        new Language("fa"),         // Persian; Farsi
//        new Language("fa-AF"),         // Persian; Farsi (Afganistan)
//        new Language("fa-IR"),         // Persian; Farsi (Iran)
                
//        new Language("ff"),         // Fulah
        
        new Language("fi"),         // Finnish
//        new Language("fi-FI"),         // Finnish (Finland)
        
//        new Language("fj"),         // Fijian
//        new Language("fj-FJ"),         // Fijian (Fiji)
//        new Language("fo"),         // Faroese
        
        new Language("fr"),         // French
//        new Language("fr-BE"),         // French (Belgium)
        new Language("fr-CA"),         // French (Canada)
//        new Language("fr-CH"),         // French (Switzerland)
        new Language("fr-FR"),         // French (France)
//        new Language("fr-LU"),         // French (Luxembourg)
        
//        new Language("fy"),         // Frisian
                
        new Language("ga"),         // Irish
//        new Language("ga-IE"),         // Irish (Ireland)
                
        new Language("gd"),         // Gaelic; Scottish Gaelic
                
//        new Language("gl"),         // Gallegan
//        new Language("gn"),         // Guarani
//        new Language("gu"),         // Gujarati
//        new Language("gv"),         // Manx
//        new Language("ha"),         // Hausa
        
        new Language("he"),         // Hebrew
//        new Language("he-IL"),         // Hebrew (Israel)
                
        new Language("hi"),         // Hindi
//        new Language("hi-IN"),         // Hindi (India)
        
//        new Language("ho"),         // Hiri Motu
        
//        new Language("hr"),         // Croatian
//        new Language("hr-HR"),         // Croatian (Croatia)
        
//        new Language("ht"),         // Haitian; Haitian Creole
//        new Language("ht-HT"),         // Haitian; Haitian Creole (Haiti)
        
        new Language("hu"),         // Hungarian
//        new Language("hu-HU"),         // Hungarian (Hungary)
        
        new Language("hy"),         // Armenian
//        new Language("hy-AM"),         // Armenian (Armenia)

//        new Language("hz"),         // Herero
//        new Language("ia"),         // Interlingua; International Auxiliary
//        new Language("id"),         // Indonesian
//        new Language("id-ID"),         // Indonesian (Indonesia)
//        new Language("ig"),         // Igbo
//        new Language("ii"),         // Sichuan Yi
//        new Language("ik"),         // Inupiaq
//        new Language("io"),         // Ido
        
        new Language("is"),         // Icelandic
//        new Language("is-IS"),         // Icelandic (Iceland)
                
        new Language("it"),         // Italian
//        new Language("it-CH"),         // Italian (Switzerland)
//        new Language("it-IT"),         // Italian (Italy)
        
//        new Language("iu"),         // Inuktitut
        
        new Language("ja"),         // Japanese
//        new Language("ja-JP"),         // Japanese (Japan)

//        new Language("ji"),         // Yiddish
        
//        new Language("jv"),         // Javanese
                
        new Language("ka"),         // Georgian
//        new Language("ka-GE"),         // Georgian (Georgia)
                
//        new Language("kg"),         // Kongo
//        new Language("ki"),         // Kikuyu; Gikuyu
//        new Language("kj"),         // Kuanyama; Kwanyama
                
        new Language("kk"),         // Kazakh
//        new Language("kk-KZ"),         // Kazakh (Kazakhstan)
                
//        new Language("kl"),         // Kalaallisut; Greenlandic
//        new Language("km"),         // Khmer
//        new Language("km-KH"),         // Khmer (Cambodia)
//        new Language("kn"),         // Kannada
        
        new Language("ko"),         // Korean
//        new Language("ko-KR"),         // Korean (South Korea)
        
//        new Language("kr"),         // Kanuri
//        new Language("ks"),         // Kashmiri
//        new Language("ku"),         // Kurdish
//        new Language("kv"),         // Komi
//        new Language("kw"),         // Cornish
                
//        new Language("ky"),         // Kirghiz
//        new Language("ky-KG"),         // Kirghiz (Kyrgyzstan)
                
        new Language("la"),         // Latin
        new Language("lb"),         // Luxembourgish; Letzeburgesch
//        new Language("lb-LU"),         // Luxembourgish (Luxembourg)
//        new Language("lg"),         // Ganda
//        new Language("li"),         // Limburgan; Limburger; Limburgish
//        new Language("ln"),         // Lingala
//        new Language("lo"),         // Lao
//        new Language("lo-LA"),         // Lao (Lao)
        
        new Language("lt"),         // Lithuanian
//        new Language("lt-LT"),         // Lithuanian (Lithuania)
        
//        new Language("lu"),         // Luba-Katanga
        
        new Language("lv"),         // Latvian (Lettish)
//        new Language("lv-LV"),         // Latvian (Lettish) (Latvia)
        
//        new Language("mg"),         // Malagasy
//        new Language("mg-MG"),         // Malagasy (Madagascar)
//        new Language("mh"),         // Marshallese
//        new Language("mh-MH"),         // Marshallese (Marshall Islands)
//        new Language("mi"),         // Maori
        
//        new Language("mk"),         // Macedonian
//        new Language("mk-MK"),         // Macedonian (Macedonia)
        
//        new Language("ml"),         // Malayalam
                
        new Language("mn"),         // Mongolian
//        new Language("mn-MN"),         // Mongolian (Mongolia)
                
        new Language("mo"),         // Moldavian
//        new Language("mo-MD"),         // Moldavian (Moldova)
                
//        new Language("mr"),         // Marathi
//        new Language("ms"),         // Malay
//        new Language("ms-MY"),         // Malay (Malaysia)
//        new Language("mt"),         // Maltese
//        new Language("mt-MT"),         // Maltese (Malta)
//        new Language("my"),         // Burmese
//        new Language("my-MM"),         // Burmese (Myanmar)
//        new Language("na"),         // Nauru
                
        new Language("nb"),         // Norwegian Bokm?l; Bokm?l, Norwegian
//        new Language("nb-NO"),         // Bokm?l (Norway)
                
//        new Language("nd"),         // Ndebele, North; North Ndebele
//        new Language("ne"),         // Nepali
//        new Language("ne-NP"),         // Nepali (Nepal)
//        new Language("ng"),         // Ndonga
        
        new Language("nl"),         // Dutch
//        new Language("nl-BE"),         // Dutch (Belgium)
//        new Language("nl-NL"),         // Dutch (Netherlands)
        
        new Language("nn"),         // Norwegian Nynorsk; Nynorsk, Norwegian
//        new Language("nn-NO"),         // Nynorsk (Norway)
        
        new Language("no"),         // Norwegian
//        new Language("no-NO"),         // Norwegian (Norway)
        
//        new Language("nr"),         // Ndebele, South; South Ndebele
//        new Language("nv"),         // Navajo; Navaho
//        new Language("ny"),         // Chichewa; Chewa; Nyanja
//        new Language("oc"),         // Occitan (post 1500); Proven?al
//        new Language("oj"),         // Ojibwa
//        new Language("om"),         // Oromo
//        new Language("or"),         // Oriya
//        new Language("os"),         // Ossetian; Ossetic
//        new Language("pa"),         // Panjabi; Punjabi
//        new Language("pa-IN"),         // Panjabi; Punjabi (India)
//        new Language("pa-PK"),         // Panjabi; Punjabi (Pakistan)
//        new Language("pi"),         // Pali
        
        new Language("pl"),         // Polish
//        new Language("pl-PL"),         // Polish (Poland)
        
//        new Language("ps"),         // Pushto
        
        new Language("pt"),         // Portuguese
        new Language("pt-BR"),         // Portuguese (Brazil)
        new Language("pt-PT"),         // Portuguese (Portugal)
        
//        new Language("qu"),         // Quechua
//        new Language("rm"),         // Raeto-Romance
//        new Language("rn"),         // Rundi
        
        new Language("ro"),         // Romanian
//        new Language("ro-RO"),         // Romanian (Romania)
                
        new Language("ru"),         // Russian
//        new Language("ru-BY"),         // Russian (Belorus)
//        new Language("ru-RU"),         // Russian (Russia)
        
//        new Language("rw"),         // Kinyarwanda
//        new Language("sa"),         // Sanskrit
//        new Language("sc"),         // Sardinian
//        new Language("sd"),         // Sindhi
//        new Language("sd-IN"),         // Sindhi (India)
//        new Language("sd-PK"),         // Sindhi (Pakistan)
//        new Language("se"),         // Northern Sami
//        new Language("sg"),         // Sango
        
//        new Language("si"),         // Sinhala; Sinhalese
        
        new Language("sk"),         // Slovak
//        new Language("sk-SK"),         // Slovak (Slovakia)
                
        new Language("sl"),         // Slovenian
//        new Language("sl-SI"),         // Slovenian (Slovenia)
        
//        new Language("sm"),         // Samoan 
//        new Language("sm-AS"),         // Samoan (American Samoa)
//        new Language("sm-WS"),         // Samoan (Samoa)
                
//        new Language("sn"),         // Shona
//        new Language("so"),         // Somali
//        new Language("so-SO"),         // Somali (Somalia)
                
        new Language("sq"),         // Albanian
//        new Language("sq-AL"),         // Albanian (Albania)
                
        new Language("sr"),         // Serbian
//        new Language("sr-CS"),         // Serbian (Serbia and Montenegro)
                
//        new Language("ss"),         // Swati
//        new Language("st"),         // Sotho, Southern 
//        new Language("su"),         // Sundanese 
        
        new Language("sv"),         // Swedish
//        new Language("sv-SE"),         // Swedish (Sweden)
        
//        new Language("sw"),         // Swahili
//        new Language("ta"),         // Tamil
//        new Language("te"),         // Telugu
//        new Language("tg"),         // Tajik
//        new Language("tg-TJ"),         // Tajik (Tajikistan)
        
        new Language("th"),         // Thai
//        new Language("th-TH"),         // Thai (Thailand)
        
//        new Language("ti"),         // Tigrinya
//        new Language("tk"),         // Turkmen
//        new Language("tk-TM"),         // Turkmen (Turkmenistan)
//        new Language("tl"),         // Tagalog
//        new Language("tn"),         // Tswana
//        new Language("to"),         // Tonga
//        new Language("to-TO"),         // Tonga (Tonga Islands)

        new Language("tr"),         // Turkish
//        new Language("tr-TR"),         // Turkish (Turkey)
        
//        new Language("ts"),         // Tsonga
                
        new Language("tt"),         // Tatar 
                
//        new Language("tw"),         // Twi
//        new Language("ty"),         // Tahitian
//        new Language("ug"),         // Uighur; Uyghur
        
        new Language("uk"),         // Ukrainian
//        new Language("uk-UA"),         // Ukrainian (Ukraine)
        
//        new Language("ur"),         // Urdu
//        new Language("ur-IN"),         // Urdu (India)
//        new Language("ur-PK"),         // Urdu (Pakistan)
                
//        new Language("uz"),         // Uzbek
//        new Language("uz-UZ"),         // Uzbek (Uzbekistan)
//        new Language("ve"),         // Venda 
//        new Language("vi"),         // Vietnamese
//        new Language("vi-VN"),         // Vietnamese (Vietnam)
//        new Language("vo"),         // Volap?k
//        new Language("wa"),         // Walloon
//        new Language("wo"),         // Wolof
//        new Language("xh"),         // Xhosa
//        new Language("yo"),         // Yoruba
//        new Language("za"),         // Zhuang; Chuang
        
//        new Language("zh"),         // Chinese
        new Language("zh-CN"),         // Chinese simplified (China)
//        new Language("zh-HK"),         // Chinese (Hong Kong)
        new Language("zh-TW"),         // Chinese traditional (Taiwan)
        
//        new Language("zu"),         // Zulu
        
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
        return toString().equals(lang.toString());
    }

    /**
     * Returns a hash code value for the language.
     * Basically returns a hashcode of the underlying Locale object.
     */
    public int hashCode() 
    {
        if( locale!=null )
            return locale.hashCode();
        else
            return new String().hashCode();
    }

}
