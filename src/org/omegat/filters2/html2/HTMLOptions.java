/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel, Martin Fleurke
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

package org.omegat.filters2.html2;

import java.io.Serializable;

/**
 * Options for (X)HTML filter.
 * Serializable to allow saving to / reading from configuration file.
 * <p>
 * HTML filter would have the following options
 * ([+] means default on).
 * Add or rewrite encoding declaration in HTML and XHTML files:
 * <ul>
 * <li>[] Always
 * <li>[+] Only if HTML file has a header
 * <li>[]  Only if HTML file has an encoding declaration
 * <li>[] Never
 * </ul>
 * Translatable attributes:
 * <ul>[+] href
 * <ul>[+] src
 * <ul>[+] lang
 * <ul>[+] hreflang
 * <ul>[+] value
 * </ul>
 * Start a new paragraph on breaks (<br>: [ ]
 * Skip text matchin regExp []
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Martin Fleurke
 */
public class HTMLOptions implements Serializable
{
    /** (X)HTML filter should always add/rewrite encoding declaration. */
    public static final int REWRITE_ALWAYS = 1;
    /** Default. (X)HTML filter should rewrite encoding declaration if HTML file has a header. */
    public static final int REWRITE_IFHEADER = 2;
    /** (X)HTML filter should rewrite encoding declaration meta-tag if HTML file has one. */
    public static final int REWRITE_IFMETA = 3;
    /** (X)HTML filter should never rewrite encoding declaration. */
    public static final int REWRITE_NEVER = 4;

    /** Holds value of property. */
    private int rewriteEncoding = REWRITE_IFHEADER;

    /**
     * Returns whether and when (X)HTML filter adds/rewrites encoding declaration.
     * @return One of {@link #REWRITE_ALWAYS}, {@link #REWRITE_IFHEADER}, 
     *                  {@link #REWRITE_IFMETA}, {@link #REWRITE_NEVER}.
     */
    public int getRewriteEncoding()
    {
        return this.rewriteEncoding;
    }

    /**
     * Sets when (X)HTML filter should add/rewrite encoding declaration.
     * @param rewriteEncoding One of {@link #REWRITE_ALWAYS}, {@link #REWRITE_IFHEADER}, 
     *                                  {@link #REWRITE_IFMETA}, {@link #REWRITE_NEVER}.
     */
    public void setRewriteEncoding(int rewriteEncoding)
    {
        this.rewriteEncoding = rewriteEncoding;
    }
    
    /** Hold value of properties. */
    private boolean translateHref = true;
    private boolean translateSrc = true;
    private boolean translateLang = true;
    private boolean translateHreflang = true;
    private boolean translateValue = true;
    private boolean paragraphOnBr = false;
    
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
    
    private String skipRegExp="";
    
    /**
     * Returns the regular expression that matches text not to be translated
     */
    public String getskipRegExp()
    {
        return this.skipRegExp;
    }

    /**
     * Sets whether a new paragraph should be started on BR.
     */
    public void setSkipRegExp(String skipRegExp)
    {
        this.skipRegExp = skipRegExp;
    }
    
}
