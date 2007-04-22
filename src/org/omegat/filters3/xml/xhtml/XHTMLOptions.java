/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Didier Briel
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

package org.omegat.filters3.xml.xhtml;

import java.awt.Dialog;
import java.io.Serializable;


/**
 * * Options for XHTML filter.
 * Serializable to allow saving to / reading from configuration file.
 * <p>
 * XHTML filter have the following options
 * ([+] means default on).
 * Translatable attributes:
 * <ul>[+] href
 * <ul>[+] src
 * <ul>[+] lang
 * <ul>[+] hreflang
 * </ul>
 * @author Didier Briel
 */
public class XHTMLOptions implements Serializable
{    
     
    /** Hold value of properties. */
    private boolean translateHref = true;
    private boolean translateSrc = true;
    private boolean translateLang = true;
    private boolean translateHreflang = true;
    
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
}
