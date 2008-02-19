/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Thomas Huriaux
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

package org.omegat.filters2.po;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.util.OStrings;

/**
 * Filter to support po files (in various encodings).
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Thomas Huriaux
 * @author Martin Fleurke
 */
public class PoFilter extends AbstractFilter
{
    
    public String getFileFormatName()
    {
        return OStrings.getString("POFILTER_FILTER_NAME");
    }

    public Instance[] getDefaultInstances()
    {
        return new Instance[] 
            { 
                new Instance("*.po"),                                           // NOI18N
                new Instance("*.pot")                                           // NOI18N
            };
    }

    public boolean isSourceEncodingVariable()
    {
        return true;
    }

    public boolean isTargetEncodingVariable()
    {
        return true;
    }
    
    public void processFile(BufferedReader in, BufferedWriter out)
            throws IOException
    {
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch!=0xFEFF)
            in.reset();
        
        processPoFile(in, out);
    }
    
    private void processPoFile(BufferedReader in, Writer out)
            throws IOException
    {
        StringBuffer nontrans = new StringBuffer();
        StringBuffer trans = new StringBuffer();
        StringBuffer trans_plural = new StringBuffer();
        String s;
        boolean msgid = false;
        boolean msgstr = false;
        boolean msgid_plural = false;
        boolean msgstr_plural = false;
        boolean potential_header = false;
        boolean header = false;
        boolean nowrap = false;
        while( (s=in.readLine())!=null )
        {
            //removing the fuzzy markers, as it has no meanings after being
            //processed by omegat
            if (s.matches("#, fuzzy"))
            {
                continue;
            }
            else if (s.matches("#,.* fuzzy.*"))
            {
                s=s.replaceAll("(.*), fuzzy(.*)", "$1$2");
            }
            
            //FSM for po files
            if (s.matches("#,.* no-wrap.*"))
            {
                nowrap = true;
                nontrans.append(s);
                nontrans.append("\n");
            }
            else if (s.matches("msgid \"\""))
            {
                potential_header = true;
                msgid = true;
                nontrans.append(s+"\n");
            }
            else if (s.matches("msgid \".*\""))
            {
                msgid = true;
                if (nowrap)
                {
                    trans.append(s.replaceAll("msgid (.*)","$1"));
                    trans.append("\n");
                }
                else
                {
                    trans.append(s.replaceAll("msgid \"(.*)\"","$1"));
                }
                nontrans.append(s+"\n");
            }
            else if ((s.matches("\"(.*)\"")) && msgid)
            {
                potential_header = false;
                nontrans.append(s);
                nontrans.append("\n");
                if (nowrap)
                {
                    trans.append(s);
                    trans.append("\n");
                }
                else
                {
                    trans.append(s.replaceAll("\"(.*)\"","$1"));
                }
            }
            else if (s.matches("\"(.*)\"") && msgid_plural)
            {
                potential_header = false;
                nontrans.append(s);
                nontrans.append("\n");
                if (nowrap)
                {
                    trans_plural.append(s);
                    trans_plural.append("\n");
                }
                else
                {
                    trans_plural.append(s.replaceAll("\"(.*)\"","$1"));
                }
            }
            else if (s.matches("msgid_plural \".*\"")) 
            {
                potential_header = false;
                msgid = false;
                msgid_plural = true;
                if (nowrap)
                {
                    trans_plural.append(s.replaceAll("msgid_plural (.*)","$1"));
                    trans_plural.append("\n");
              }
              else
              {
                  trans_plural.append(s.replaceAll("msgid_plural \"(.*)\"","$1"));
              }
              nontrans.append(s+"\n");
            }
            else if ((s.matches("\"(.*)\"")) && msgid_plural)
            {
                nontrans.append(s);
                nontrans.append("\n");
                if (nowrap)
                {
                    trans_plural.append(s);
                    trans_plural.append("\n");
                }
                else
                {
                    trans_plural.append(s.replaceAll("\"(.*)\"","$1"));
                }
            }
            else if (s.matches("msgstr \".*\""))
            {
                msgid = false;
                if (potential_header)
                {
                    header = true;
                    msgstr = true;
                    nontrans.append(s);
                    nontrans.append("\n");
                }
                else
                {
                    nontrans.append("msgstr ");
                    if (!nowrap)
                    {
                        nontrans.append("\"");
                    }
                    msgstr = true;
                    out.write(nontrans.toString());
                    nontrans.setLength(0);
                    
                    out.write(processEntry(trans.toString()));
                    trans.setLength(0);
                    if (!nowrap)
                        out.write("\"");
                    out.write("\n");
                }
            }
            else if ((s.matches("\"(.*)\"")) && msgstr)
            {
                if (header)
                {
                    trans.append(s);
                    trans.append("\n");
                }
            }
            
            else if (s.matches("msgstr\\[0\\] \".*\""))
            {
                msgid_plural = false;
                nontrans.append("msgstr[0] ");
                if (!nowrap)
                {
                    nontrans.append("\"");
                }
                msgstr = true;
                out.write(nontrans.toString());
                nontrans.setLength(0);
                    
                out.write(processEntry(trans.toString()));
                trans.setLength(0);
                if (!nowrap)
                    out.write("\"");
                out.write("\n");
            }
            else if (s.matches("msgstr\\[1\\] \".*\""))
            {
                msgstr = false;
                nontrans.append("msgstr[1] ");
                if (!nowrap)
                {
                    nontrans.append("\"");
                }
                msgstr_plural = true;
                out.write(nontrans.toString());
                nontrans.setLength(0);
                
                out.write(processEntry(trans_plural.toString()));
                trans_plural.setLength(0);
                if (!nowrap)
                    out.write("\"");
                out.write("\n");
            }
            else if ((s.matches("\"(.*)\"")) && (msgstr || msgstr_plural) )
            {
                if (header)
                {
                    trans.append(s);
                    trans.append("\n");
                }
            }
            else if (header)
            {
                header = false;
                potential_header = false;
                msgstr = false;
                out.write(nontrans.toString());
                nontrans.setLength(0);
                
                out.write(processEntry(trans.toString()));
                trans.setLength(0);
                out.write("\n");
            }
            else
            {
                nowrap = false;
                msgstr = false;
                msgstr_plural = false;
                potential_header = false;
                nontrans.append(s);
                nontrans.append("\n");
            }
        }
        
        if( nontrans.length()>=0 )
            out.write(nontrans.toString());
        if( trans.length()>=0 )
            out.write(processEntry(trans.toString()));
        if( trans_plural.length()>=0 )
          out.write(processEntry(trans_plural.toString()));
    }
}
