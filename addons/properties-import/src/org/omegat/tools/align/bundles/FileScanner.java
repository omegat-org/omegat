/**************************************************************************
 OmegaT Addon - Import of legacy translations of Java(TM) Resource Bundles
 Copyright (C) 2004-05  Maxym Mykhalchuk
                        mihmax@gmail.com

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

package org.omegat.tools.align.bundles;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * Class that scans Bundle.properties / Bundle_ru.properties pairs
 * and passes found legacy translations to TMXSaver.
 *
 * @author  Maxym Mykhalchuk
 */
public class FileScanner
{
    private String filename, locale;
    private TMXSaver saver;
    
    /** Creates a new instance of FileScanner */
    public FileScanner(String filename, String locale, TMXSaver saver)
    {
        this.filename = filename;
        this.locale = locale;
        this.saver = saver;
    }
    
    public void scan() throws IOException
    {
        String name = filename.substring(0, filename.lastIndexOf('.'));
        String locfilename = name + '_' + locale + ".properties";       
        ResourceBundle parent = new PropertyResourceBundle(new FileInputStream(filename));
        ResourceBundle bundle;
        try
        {
            bundle = new PropertyResourceBundle(new FileInputStream(locfilename));
        }
        catch( IOException ioe )
        {
            return;
        }
        
        Enumeration keys = bundle.getKeys();
        while( keys.hasMoreElements() )
        {
            String key = (String) keys.nextElement();
            try
            {
                String locvalue = bundle.getString(key);
                String srcvalue = parent.getString(key);
                saver.add(srcvalue, locvalue);
            }
            catch( MissingResourceException mre )
            {
                mre.printStackTrace();
            }
        }
    }
    
}
