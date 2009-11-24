/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Alex Buloichik
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

package org.omegat.filters2.text.bundles;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.NullBufferedWriter;
import org.omegat.util.OStrings;

/**
 * Filter to support Java Resource Bundles - the files that are used to I18ze
 * Java applications.
 *
 * @author Maxym Mykhalchuk
 * @author Keith Godfrey
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ResourceBundleFilter extends AbstractFilter {
    
    protected Map<String, String> align;
    
    public String getFileFormatName()
    {
        return OStrings.getString("RBFILTER_FILTER_NAME");
    }
    
    public boolean isSourceEncodingVariable()
    {
        return false;
    }
    
    public boolean isTargetEncodingVariable()
    {
        return false;
    }
    
    public Instance[] getDefaultInstances()
    {
        return new Instance[]
        {
            new Instance("*.properties", null, null,                            
                    TFP_NAMEONLY+"_"+TFP_TARGET_LOCALE+"."+TFP_EXTENSION)       
        };
    }
    
    /**
     * Creating an input stream to read the source resource bundle.
     * <p>
     * NOTE: resource bundles use always ISO-8859-1 encoding.
     */
    public BufferedReader createReader(File infile, String encoding)
    throws UnsupportedEncodingException, IOException
    {
        return new BufferedReader(
                new InputStreamReader(new FileInputStream(infile), "ISO-8859-1")); 
    }
    
    /**
     * Creating an output stream to save a localized resource bundle.
     * <p>
     * NOTE: resource bundles use always ISO-8859-1 encoding.
     * <p>
     * NOTE: the name of localized resource bundle is different from
     *       the name of original one.
     *       e.g. "Bundle.properties" -> Russian = "Bundle_ru.properties"
     */
    public BufferedWriter createWriter(File outfile, String encoding)
    throws UnsupportedEncodingException, IOException
    {
        // resource bundles use ASCII encoding
        return new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outfile), "ISO-8859-1")); 
    }
    
    /**
     * Reads next line from the input and:
     * <ul>
     * <li>Converts ascii-encoded \\uxxxx chars to normal characters.
     * <li>Converts \r, \n and \t to CR, line feed and tab.
     * <li>But! Keeps a backspace in '\ ', '\=', '\:' etc
     *     (non-trimmable space or non-key-value-breaking :-) equals).
     * <ul>
     */
    protected String getNextLine(LinebreakPreservingReader reader) throws IOException // fix for bug 1462566
    {
        String ascii = reader.readLine();
        if( ascii==null )
            return null;
        
        StringBuffer result = new StringBuffer();
        for(int i=0; i<ascii.length(); i++)
        {
            char ch = ascii.charAt(i);
            if( ch == '\\' && i!=ascii.length()-1 )
            {
                i++;
                ch = ascii.charAt(i);
                if( ch!='u' )
                {
                    if( ch=='n' )
                        ch='\n';
                    else if( ch=='r')
                        ch='\r';
                    else if( ch=='t')
                        ch='\t';
                    else
                        result.append('\\');
                }
                else
                {
                    // checking if the string is long enough
                    if( ascii.length() >= i+1+4 )
                    {
                        ch=(char)Integer.parseInt(ascii.substring(i+1, i+1+4), 16);
                        i+=4;
                    }
                    else
                        throw new IOException(
                                OStrings.getString("RBFH_ERROR_ILLEGAL_U_SEQUENCE"));
                }
            }
            result.append(ch);
        }
        
        return result.toString();
    }
    
    /**
     * Converts normal strings to ascii-encoded ones.
     *
     * @param text  Text to convert.
     * @param key   Whether it's a key of the key-value pair
     *              (' ', ':', '=' MUST be escaped in a key
     *               and MAY be escaped in value, but we don't escape these).
     */
    private String toAscii(String text, boolean key)
    {
        StringBuffer result = new StringBuffer();
        
        for(int i=0; i<text.length(); i++)
        {
            char ch = text.charAt(i);
            if( ch=='\\' )
                result.append("\\\\");                                          
            else if( ch=='\n' )
                result.append("\\n");                                           
            else if( ch=='\r' )
                result.append("\\r");                                           
            else if( ch=='\t' )
                result.append("\\t");                                           
            else if( key && ch==' ' )
                result.append("\\ ");                                           
            else if( key && ch=='=' )
                result.append("\\=");                                           
            else if( key && ch==':' )
                result.append("\\:");                                           
            else if( ch>=32 && ch<127 )
                result.append(ch);
            else
            {
                String code = Integer.toString(ch, 16);
                while( code.length()<4 )
                    code = '0'+code;
                result.append("\\u"+code);                                      
            }
        }
        
        return result.toString();
    }
    
    /**
     * Removes extra slashes from, e.g. "\ ", "\=" and "\:" typical in
     * machine-generated resource bundles.  A slash at the end of a string
     * means a mandatory space has been trimmed.
     * <p>
     * See also bugreport
     * <a href="http://sourceforge.net/support/tracker.php?aid=1606595">#1606595</a>.
     */
    private String removeExtraSlashes(String string)
    {
        StringBuffer result = new StringBuffer(string.length());
        for(int i=0; i<string.length(); i++)
        {
            char ch = string.charAt(i);
            if (ch=='\\')
            {
            	// Fix for [ 1812183 ] Properties: space before "=" shouldn't 
                // be part of the key, contributed by Arno Peters
                if (i+1 < string.length()) 
            	{      
            	    i++;
            	    ch = string.charAt(i);
            	}
            	else
            	    ch = ' ';
            }
            result.append(ch);
        }
        return result.toString();
    }
    
    /**
     * Trims the string from left.
     * Also contains some code to strip a backspace from '\ '
     * (non-trimmable space), but doesn't trim this space.
     */
    private String leftTrim(String s)
    {
        int i;
        for(i=0; i<s.length() && (s.charAt(i)==' ' || s.charAt(i)=='\t'); i++);
        s = s.replaceAll("\\\\ ", " ");                                         
        return s.substring(i, s.length());
    }
    
    /**
     * Doing the processing of the file...
     */
    public void processFile(BufferedReader reader, BufferedWriter outfile)
    throws IOException
    {
        LinebreakPreservingReader lbpr = new LinebreakPreservingReader(reader); // fix for bug 1462566
        String str;
        boolean noi18n=false;
        while( (str=getNextLine(lbpr))!=null )
        {
            String trimmed = str.trim();
            
            // skipping empty strings
            if( trimmed.length()==0 )
            {
                outfile.write(toAscii(str, false)+lbpr.getLinebreak());
                continue;
            }
            
            // skipping comments
            char firstChar = trimmed.charAt(0);
            if( firstChar=='#' || firstChar=='!' )
            {
                outfile.write(toAscii(str, false)+lbpr.getLinebreak());
                
                // checking if the next string shouldn't be internationalized
                if( trimmed.indexOf("NOI18N")>=0 )                              
                    noi18n=true;
                
                continue;
            }
            
            // reading the glued lines
            while( str.charAt(str.length()-1)=='\\' )
            {
                String next = getNextLine(lbpr);
                if( next==null )
                    next="";                                                    
                
                // gluing this line (w/o '\' on this line)
                //        with next line (w/o leading spaces)
                str=str.substring(0, str.length()-1)+leftTrim(next);
            }
            
            // key=value pairs
            int equalsPos = searchEquals(str);
            
            // writing out key
            String key;
            if (equalsPos>=0)
                key = str.substring(0, equalsPos).trim();
            else
                key = str.trim();
            key = removeExtraSlashes(key);
            outfile.write(toAscii(key, true));
            
            // advance if there're spaces or tabs after =
            if (equalsPos>=0)
            {
                int equalsEnd = equalsPos+1;
                while(equalsEnd<str.length())
                {
                    char ch = str.charAt(equalsEnd);
                    if (ch!=' ' && ch!='\t')
                        break;
                    equalsEnd++;
                }
                String equals = str.substring(equalsPos, equalsEnd);
                outfile.write(equals);
                
                // value, if any
                String value;
                if (equalsEnd<str.length())
                    value = removeExtraSlashes(str.substring(equalsEnd));
                else
                    value = "";
                
                if( noi18n )
                {
                    // if we don't need to internationalize
                    outfile.write(toAscii(value, false));
                    noi18n = false;
                }
                else
                {
                    value = value.replaceAll("\\n\\n", "\n \n");                    
                    String trans = process(key, value);
                    trans = trans.replaceAll("\\n\\s\\n", "\n\n");                  
                    trans = toAscii(trans, false);
                    if( trans.length()>0 && trans.charAt(0)==' ' )
                        trans = '\\'+trans;
                    outfile.write(trans);
                }
            }
            
            outfile.write(lbpr.getLinebreak()); // fix for bug 1462566
        }
    }
    
    /**
     * Looks for the key-value separator (=,: or ' ') in the string.
     * <p>
     * See also bugreport
     * <a href="http://sourceforge.net/support/tracker.php?aid=1606595">#1606595</a>.
     *
     * @return
     *      The char number of key-value separator in a string.
     *      Not that if the string does not contain any separator this string
     *      is considered to be a key with empty string value, and this method
     *      returns <code>-1</code> to indicate there's no equals.
     */
    private int searchEquals(String str)
    {
        char prevChar = 'a';
        for(int i=0; i<str.length(); i++)
        {
            char ch = str.charAt(i);
            if (prevChar!='\\')
            {
                if (ch=='=' || ch==':')
                    return i;
                else if (ch==' ' || ch=='\t')
                {
                    for (int j=i+1; j<str.length(); j++)
                    {
                        char c2 = str.charAt(j);
                        if (c2==':' || c2=='=')
                            return j;
                        if (c2!=' ' && c2!='\t')
                            return i;
                    }
                    return i;
                }
            }
            prevChar = ch;
        }
        return -1;
    }
    
    protected String process(String key, String value) {
        if (entryParseCallback != null) {
            entryParseCallback.addEntry(key, value, null, false, null, this);
            return value;
        } else if (entryTranslateCallback != null) {
            return entryTranslateCallback.getTranslation(key, value);
        } else if (entryAlignCallback != null) {
            align.put(key, value);
        }
        return value;
    }

    protected void alignFile(BufferedReader sourceFile,
            BufferedReader translatedFile) throws Exception {
        Map<String, String> source = new HashMap<String, String>();
        Map<String, String> translated = new HashMap<String, String>();

        align = source;
        processFile(sourceFile, new NullBufferedWriter());
        align = translated;
        processFile(translatedFile, new NullBufferedWriter());
        for (Map.Entry<String, String> en : source.entrySet()) {
            String tr = translated.get(en.getKey());
            if (tr != null) {
                entryAlignCallback.addTranslation(en.getKey(), en.getValue(),
                        tr);
            }
        }
    }
}
