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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
**************************************************************************/

package org.omegat.tools.align.bundles;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that stores TMX entries in memory and saves them to disk
 *
 * @author  Maxym Mykhalchuk
 */
public class TMXSaver
{
    
    private String sourcelang;
    private String targetlang;
    private String tmxfile;
    private Map map;
    
    /** Creates a new instance of TMXSaver */
    public TMXSaver(String sourcelang, String targetlang, String tmxfile)
    {
        map = new HashMap();
        this.sourcelang = sourcelang;
        this.targetlang = targetlang;
        this.tmxfile = tmxfile;
    }
    
    public void add(String source, String target)
    {
        map.put(source, target);
    }
    
    public void save() throws IOException
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>   \n");       
        buffer.append("<!DOCTYPE tmx SYSTEM \"tmx11.dtd\">          \n");       
        buffer.append("<tmx version=\"1.1\">                        \n");       
        buffer.append("  <header                                    \n");       
        buffer.append("    creationtool=\"OTimp\"                   \n");       
        buffer.append("    creationtoolversion=\"1.3\"              \n");       
        buffer.append("    segtype=\"paragraph\"                    \n");       
        buffer.append("    o-tmf=\"OmegaT TMX\"                     \n");       
        buffer.append("    adminlang=\"EN-US\"                      \n");       
        buffer.append("    srclang=\"EN-US\"                        \n");       
        buffer.append("    datatype=\"plaintext\"                   \n");       
        buffer.append("  >                                          \n");       
        buffer.append("  </header>                                  \n");       
        buffer.append("  <body>                                     \n");       
        
        Object keys[] = map.keySet().toArray();
        for(int i=0; i<keys.length; i++)
        {
            String key = replaceSpecialChars((String) keys[i]);
            String value = replaceSpecialChars((String) map.get(keys[i]));
            buffer.append("    <tu>                                 \n");       
            buffer.append("      <tuv lang=\""+sourcelang+"\">      \n");       
            buffer.append("        <seg>"+key+"</seg>               \n");       
            buffer.append("      </tuv>                             \n");       
            buffer.append("      <tuv lang=\""+targetlang+"\">      \n");       
            buffer.append("        <seg>"+value+"</seg>             \n");       
            buffer.append("      </tuv>                             \n");       
            buffer.append("    </tu>                                \n");       
        }
        
        buffer.append("  </body>                                   \n");        
        buffer.append("</tmx>                                      \n");        
        
        Writer writer = new OutputStreamWriter(new FileOutputStream(tmxfile), "UTF-8");       
        writer.write(buffer.toString());
        writer.close();
    }
    
    public String replaceSpecialChars(String s)
    {
        String st = s;
        st = st.replaceAll("&", "&amp;");                                       
        st = st.replaceAll("<", "&lt;");                                        
        st = st.replaceAll(">", "&gt;");                                        
        st = st.replaceAll("\"", "&quot;");                                     
        return st;
    }
}
