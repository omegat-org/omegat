/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Didier Briel
               2008 Martin Fleurke
               Home page: http://www.omegat.org/
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

package org.omegat.convert.v20to21.data.xml.xhtml;

import java.io.Serializable;


/**
 * * Options for XHTML filter.
 * Serializable to allow saving to / reading from configuration file.
 * <p>
 * XHTML filter have the following options
 * ([+] means default on).<br>
 * Translatable attributes:
 * <ul>
 * <li>[+] href
 * <li>[+] src
 * <li>[+] lang
 * <li>[+] hreflang
 * <li>[+] value
 * <li>[+] value (of buttons)
 * </ul>
 * Start a new paragraph on breaks (&lt;br&gt;) []<br>
 * Skip text matchin regExp []
 * Skip content of meta-tag when any of the given attibutename-value pairs is present in the tag
 * @author Didier Briel
 * @author Martin Fleurke
 */
public class XHTMLOptions implements Serializable
{

    /** Hold value of properties. */
    private boolean translateHref = true;
    private boolean translateSrc = true;
    private boolean translateLang = true;
    private boolean translateHreflang = true;
    private boolean translateValue = true;
    private boolean translateButtonValue = true;
    private boolean paragraphOnBr = false;

    private String skipRegExp = "";
    private String skipMeta= "http-equiv=Content-Type," +
                             "http-equiv=refresh," +
                             "name=robots," +
                             "name=revisit-after," +
                             "http-equiv=expires," + 
                             "http-equiv=content-style-type," +
                             "http-equiv=content-script-type";

    /**
     * Returns whether href attributes should be translated.
     */
    public boolean getTranslateHref()
    {
        return this.translateHref;
    }

    /**
     * Sets whether href attributes should be translated.
     */
    public void setTranslateHref(boolean translateHref)
    {
        this.translateHref = translateHref;
    }

    /**
     * Returns whether src attributes should be translated.
     */
    public boolean getTranslateSrc()
    {
        return this.translateSrc;
    }

    /**
     * Sets whether src attributes should be translated.
     */
    public void setTranslateSrc(boolean translateSrc)
    {
        this.translateSrc = translateSrc;
    }

    /**
     * Returns whether lang attributes should be translated.
     */
    public boolean getTranslateLang()
    {
        return this.translateLang;
    }

    /**
     * Sets whether lang attributes should be translated.
     */
    public void setTranslateLang(boolean translateLang)
    {
        this.translateLang = translateLang;
    }

   /**
     * Returns whether hreflang attributes should be translated.
     */
    public boolean getTranslateHreflang()
    {
        return this.translateHreflang;
    }

    /**
     * Sets whether hreflang attributes should be translated.
     */
    public void setTranslateHreflang(boolean translateHreflang)
    {
        this.translateHreflang = translateHreflang;
    }

    /**
     * Sets whether value attributes should be translated.
     */
    public void setTranslateValue(boolean translateValue)
    {
        this.translateValue = translateValue;
    }

    /**
     * Returns whether value attributes should be translated.
     */
    public boolean getTranslateValue()
    {
        return this.translateValue;
    }

    /**
     * Sets whether button value attributes should be translated.
     */
    public void setTranslateButtonValue(boolean translateButtonValue)
    {
        this.translateButtonValue = translateButtonValue;
    }

    /**
     * Returns whether button value attributes should be translated.
     */
    public boolean getTranslateButtonValue()
    {
        return this.translateButtonValue;
    }


   /**
     * Returns whether a new paragraph should be started on BR.
     */
    public boolean getParagraphOnBr()
    {
        return this.paragraphOnBr;
    }

    /**
     * Sets whether a new paragraph should be started on BR.
     */
    public void setParagraphOnBr(boolean paragraphOnBr)
    {
        this.paragraphOnBr = paragraphOnBr;
    }

    /**
     * Returns the regular expression that matches text not to be translated
     */
    public String getSkipRegExp()
    {
        return this.skipRegExp;
    }

    /**
     * Sets the regular expression that matches text not to be translated
     */
    public void setSkipRegExp(String skipRegExp)
    {
        this.skipRegExp = skipRegExp;
    }

    /**
     * Returns the meta-tag attribute key-value pairs of which meta-tags should not be translated
     */
    public String getSkipMeta()
    {
        return this.skipMeta;
    }

    /**
     * Sets the meta-tag attribute key-value pairs of which meta-tags should not be translated
     */
    public void setSkipMeta(String skipMeta)
    {
        this.skipMeta = skipMeta;
    }

}
