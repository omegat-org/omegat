/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

import java.net.URL;
import java.util.regex.Pattern;
import org.omegat.core.ProjectProperties;
import org.omegat.core.threads.CommandThread;

import org.xml.sax.InputSource;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies XHTML dialect of XML.
 *
 * @author Maxym Mykhalchuk
 */
public class XHTMLDialect extends DefaultXMLDialect
{
    private static final Pattern DOCBOOK_PUBLIC_DTD = 
            Pattern.compile("-//W3C//DTD XHTML.*");                             // NOI18N
    
    public XHTMLDialect()
    {
        defineConstraint(CONSTRAINT_PUBLIC_DOCTYPE, DOCBOOK_PUBLIC_DTD);                    
        
        defineParagraphTags(new String[]
        {
            "html", "head", "title", "body",                                    // NOI18N
            "address", "blockquote", "center", "div",                           // NOI18N
            "h1", "h2", "h3", "h4", "h5",                                       // NOI18N
            "table", "th", "tr", "td",                                          // NOI18N
            "p",                                                                // NOI18N
            "ol", "ul", "li",                                                   // NOI18N
            "dl", "dt", "dd",                                                   // NOI18N
            "form", "textarea", "fieldset", "legend", "label",                  // NOI18N
            "select", "option",                                                 // NOI18N
        });
        
        defineShortcut("br", "br");                                             // NOI18N
        
        definePreformatTags(new String[]
        {
            "textarea",                                                         // NOI18N
            "pre",                                                              // NOI18N
        });
        
        defineIntactTags(new String[]
        {
            "style", "script", "object", "embed",                               // NOI18N
        });
        
        defineTranslatableAttributes(new String[]{
            "abbr",                                                             // NOI18N
            "alt",                                                              // NOI18N
            "content",                                                          // NOI18N
            "href",                                                             // NOI18N
            "lang",                                                             // NOI18N
            "xml:lang",                                                         // NOI18N
            "summary",                                                          // NOI18N
            "title",                                                            // NOI18N
        });
        
        defineTranslatableTagAttribute("img", "src");                           // NOI18N
        defineTranslatableTagAttribute("input", "value");                       // NOI18N
    }

    private static final Pattern PUBLIC_XHTML = 
            Pattern.compile("-//W3C//DTD XHTML 1\\..//.*");                     // NOI18N
    private static final String DTD = "/org/omegat/filters3/xml/xhtml/res/xhtml11-flat.dtd"; // NOI18N
    
    /**
     * Resolves external entites if child filter needs it.
     * Default implementation returns <code>null</code>.
     */
    public InputSource resolveEntity(String publicId, String systemId)
    {
        if (publicId!=null && PUBLIC_XHTML.matcher(publicId).matches())
        {
            URL dtdresource = getClass().getResource(DTD);
            return new InputSource(dtdresource.toExternalForm());
        }
        else
            return null;
    }
}
