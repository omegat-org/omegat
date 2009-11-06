/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2008 Martin Fleurke
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

package org.omegat.filters2.html2;

import java.awt.Dialog;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.htmlparser.Parser;
import org.htmlparser.util.ParserException;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.filters2.TranslationException;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * A filter to translate HTML and XHTML files.
 * <p>
 * Some useful discussion why HTML filter should behave like it does,
 * happened on a
 * <a href="http://sourceforge.net/support/tracker.php?aid=1364265">bug report</a>
 * devoted to compressing space.
 *
 * @author Maxym Mykhalchuk
 * @author Martin Fleurke
 */
public class HTMLFilter2 extends AbstractFilter
{
    /** Creates a new instance of HTMLFilter2 */
    public HTMLFilter2()
    {
    }

    /** Stores the source encoding of HTML file. */
    private String sourceEncoding;

    /** Stores the target encoding of HTML file. */
    private String targetEncoding;

    /** A regular Expression Pattern to be matched to the strings to be translated.
     * If there is a match, the string should not be translated
     */
    private Pattern skipRegExpPattern;

    /** A map of attribute-name and attribute value pairs that,
     *  if it exist in a meta-tag, indicates that the meta-tag 
     *  should not be translated
     */
    private HashMap<String, String> skipMetaAttributes;

    /** The options of this filter */
    private HTMLOptions options;


    /**
     * Customized version of creating input reader for HTML files,
     * aware of encoding by using <code>EncodingAwareReader</code> class.
     *
     * @see HTMLReader
     */
    public BufferedReader createReader(File infile, String encoding)
            throws UnsupportedEncodingException, IOException
    {
        HTMLReader hreader = new HTMLReader(infile.getAbsolutePath(), encoding);
        sourceEncoding = hreader.getEncoding();
        return new BufferedReader(hreader);
    }
    /**
     * Customized version of creating an output stream for HTML files,
     * appending charset meta by using <code>HTMLWriter</code> class.
     *
     * @see HTMLWriter
     */
    public BufferedWriter createWriter(File outfile, String encoding)
            throws UnsupportedEncodingException, IOException
    {
        HTMLWriter hwriter;
        HTMLOptions options = (HTMLOptions) getOptions();
        if (encoding==null)
            this.targetEncoding = sourceEncoding;
        else
            this.targetEncoding = encoding;

        hwriter = new HTMLWriter(outfile.getAbsolutePath(),
                this.targetEncoding, options);
        return new BufferedWriter(hwriter);
    }

    public void processFile(BufferedReader infile, BufferedWriter outfile)
            throws IOException, TranslationException
    {
        StringBuffer all = null;
        try
        {
            all = new StringBuffer();
            char cbuf[] = new char[1000];
            int len = -1;
            while( (len=infile.read(cbuf))>0 )
                all.append(cbuf, 0, len);
        }
        catch( OutOfMemoryError e )
        {
            // out of memory?
            all = null;
            System.gc();
            throw new IOException(OStrings.getString("HTML__FILE_TOO_BIG"));
        }

        if (this.hasOptions()) // HHC filter has no options
        {
            this.options = (HTMLOptions) this.getOptions();
            if (this.options == null)
                this.options = new HTMLOptions();
        }

        // Prepare matcher
        String skipRegExp = options.getSkipRegExp();
        if (skipRegExp != null && skipRegExp.length()>0)
        {
            try
            {
        	this.skipRegExpPattern = Pattern.compile(skipRegExp, Pattern.CASE_INSENSITIVE);
            }
            catch (PatternSyntaxException e)
            {
        	Log.log(e);
            }
        }

        //prepare set of attributes that indicate not to translate a meta-tag
        String skipMetaString = options.getSkipMeta();
        skipMetaAttributes = new HashMap<String, String>();
        String[] skipMetaAttributesStringarray = skipMetaString.split(",");
        for (int i=0; i<skipMetaAttributesStringarray.length; i++) {
            String keyvalue = skipMetaAttributesStringarray[i].trim().toUpperCase();
            skipMetaAttributes.put(keyvalue, "");
        }

        Parser parser = new Parser();
        try
        {
            parser.setInputHTML(all.toString());
            parser.visitAllNodesWith(new FilterVisitor(this, outfile));
        }
        catch( ParserException pe )
        {
            System.out.println(pe);
        }
    }

    //////////////////////////////////////////////////////////////////////////

    /** Package-internal processEntry to give it to FilterVisitor */
    String privateProcessEntry(String entry)
    {
         if (skipRegExpPattern != null)
         {
             if (skipRegExpPattern.matcher(entry).matches())
             {
//               System.out.println("Skipping \""+entry+"\"");
                 return entry;
             }
             else
             {
//              System.out.println("Using: \""+entry+"\"");
                return super.processEntry(entry);
            }
        }
        return super.processEntry(entry);
    }

    //////////////////////////////////////////////////////////////////////////

    public boolean isTargetEncodingVariable()
    {
        return true;
    }

    public boolean isSourceEncodingVariable()
    {
        return true;
    }

    public String getFileFormatName()
    {
        return OStrings.getString("HTML__FILTER_NAME");
    }

    public Instance[] getDefaultInstances()
    {
        return new Instance[]
        {
            new Instance("*.htm", null, "UTF-8"),                      // NOI18N
            new Instance("*.html", null, "UTF-8"),                     // NOI18N
            new Instance("*.xhtml", null, "UTF-8"),                     // NOI18N
            new Instance("*.xht", null, "UTF-8")                     // NOI18N
        };
    }

    /**
     * Returns the editing hint for HTML filter.
     * <p>
     * In English, the hint is as follows:
     * <br>
     * Note: Source File Encoding setting affects only the HTML files that
     * have no encoding declaration inside. If HTML file has the encoding
     * declaration, it will be used disregarding any value you set in this
     * dialog.
     */
    public String getHint()
    {
        return OStrings.getString("HTML_NOTE");
    }

    /**
     * Returns true to indicate that (X)HTML filter has options.
     * @return True, because (X)HTML filter has options.
     */
    public boolean hasOptions()
    {
        return true;
    }
    
    public Class getOptionsClass() {
        return HTMLOptions.class;
    }

    /**
     * (X)HTML Filter shows a <b>modal</b> dialog to edit its own options.
     *
     * @param currentOptions Current options to edit.
     * @return Updated filter options if user confirmed the changes, and current options otherwise.
     */
    public Serializable changeOptions(Dialog parent, Serializable currentOptions)
    {
        try
        {
            HTMLOptions options = (HTMLOptions) currentOptions;
            EditOptionsDialog dialog = new EditOptionsDialog(parent, options);
            dialog.setVisible(true);
            if( EditOptionsDialog.RET_OK==dialog.getReturnStatus() )
                return dialog.getOptions();
            else
                return currentOptions;
        }
        catch( Exception e )
        {
            Log.logErrorRB("HTML_EXC_EDIT_OPTIONS");
            Log.log(e);
            return currentOptions;
        }
    }

    /**
     * Returns the encoding of the html writer (if already set)
     * @return the target encoding
     */
    public String getTargetEncoding()
    {
        return this.targetEncoding;
    }

    public boolean checkDoSkipMetaTag(String key, String value) {
        return skipMetaAttributes.
                containsKey(key.toUpperCase() + "=" + value.toUpperCase());
    }
}
