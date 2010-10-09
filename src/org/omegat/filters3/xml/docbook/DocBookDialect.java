/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Didier Briel
               2008 Fabian Mandelbaum, Didier Briel
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

package org.omegat.filters3.xml.docbook;

import java.util.regex.Pattern;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies DocBook XML Dialect.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Fabian Mandelbaum
 */
public class DocBookDialect extends DefaultXMLDialect
{
    public static final Pattern DOCBOOK_PUBLIC_DTD = 
            Pattern.compile("-//OASIS//DTD DocBook.*");                         
    public static final Pattern DB5_XMLNS = Pattern.compile(
            "xmlns(:\\w+)?=\"http://docbook.org/ns/docbook\"");
    private String nsString = null; // the value of the NS, if any
    
    public DocBookDialect()
    {
        defineConstraint(CONSTRAINT_PUBLIC_DOCTYPE, DOCBOOK_PUBLIC_DTD);                    
        
        defineParagraphTags(new String[]
        {
            "book",                                                             
            "bookinfo",                                                         
            "title",                                                            
            "subtitle",                                                         
            "authorgroup",                                                      
            "author",                                                           
            "firstname",                                                        
            "surname",                                                          
            "affiliation",                                                      
            "orgname",                                                          
            "address",                                                          
            "email",                                                            
            "edition",                                                          
            "pubdate",                                                          
            "copyright",                                                        
            "year",                                                             
            "holder",                                                           
            "isbn",                                                             
            "keywordset",                                                       
            "keyword",                                                          
            "preface",                                                          
            "title",                                                            
            "para",                                                             
            "chapter",                                                          
            "table",                                                            
            "tgroup",                                                           
            "thead",                                                            
            "tbody",                                                            
            "row",                                                              
            "entry",                                                            
            "revhistory",                                                       
            "revision",                                                         
            "revnumber",                                                        
            "date",                                                             
            "authorinitials",                                                   
            "revremark",                                                        
            "itemizedlist",                                                     
            "listitem",                                                         
            "member",                                                           
        });
        
        defineOutOfTurnTags(new String[]
        {
            "indexterm",                                                        
        });

        definePreformatTags(new String[]
        {
            "screen",                                                           
            "programlisting",                                                   
        });
        
        defineTranslatableAttributes(new String[]
        {        
                "url",                                                          
                "lang",                                                         
                "xml:lang"                                                      
        });                                                 
        
    }
    
    // TODO: Can we can read db xml content here to try to determinate if
    // the root element has a NS declaration to be able to handle
    // namespaced-tags properly? We'd actually need to read only the
    // root element together with its attributes.
}
