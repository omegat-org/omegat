/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2010 Didier Briel
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

package org.omegat.filters3.xml.opendoc;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * Dialect of OpenDocument files.
 *
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 */
public class OpenDocDialect extends DefaultXMLDialect
{
    
    /** Creates a new instance of OpenDocDialect */
    public OpenDocDialect()
    {
    }
 
    /**
     * Actually defines the dialect.
     * It cannot be done during creation, because options are not known
     * at that step.
     */
    public void defineDialect(OpenDocOptions options) {
        defineShortcuts(new String[]{
            "text:line-break", "br",                                            
            "text:a", "a",                                                      
            "text:span", "f",                                                   
            "text:s", "s",                                                      
            "text:alphabetical-index-mark", "i",                                
            "text:alphabetical-index-mark-start", "is",                         
            "text:alphabetical-index-mark-end", "ie",                           
            "text:tab-stop", "t",                                               
            "text:user-defined", "ud",                                          
            "text:sequence", "seq",                                             
        
            // Donated by Didier Briel
            // http://sourceforge.net/support/tracker.php?aid=1458673
            "draw:image", "di",                                                 
            "draw:frame", "df",                                                 
            "draw:object-ole", "do",                                            

            "text:bookmark", "bk",                                              
            "text:bookmark-start", "bs",                                        
            "text:bookmark-end", "be",                                          
            "text:bookmark-ref", "bf",                                          
            "text:reference-mark", "rm",                                        
            "text:reference-mark-start", "rs",                                  
            "text:reference-mark-end", "re",                                    
            "text:reference-ref", "rf",                                         
        
            "text:change", "tc",                                                
            "text:change-start", "ts",                                          
            "text:change-end", "te",                                            
            "dc:creator", "dc",                                                 
            "dc:date", "dd",                                                    
            // End of contribution

            // http://sourceforge.net/support/tracker.php?aid=1461154
            "text:note-citation", "nc",                                         
            "text:note-body", "nb",                                             
        });
        
        defineParagraphTags(new String[]{
            "text:p",                                                           
            "text:h",                                                           
            "dc:title",                                                         
            "dc:description",                                                   
            "dc:subject",                                                       
            "meta:keyword",                                                     
            "dc:language",                                                      
            "meta:user-defined",                                                
            "text:tab",                                                         
        });
        
        defineOutOfTurnTags(new String[] {
            // Drawing Shapes from OO specifications
            // Correction for [ 1541277 ] OO: Segmenting on inline drawings
            // Commented until [ 1642994 ] Subtexts are not segmented is solved
/*
            "draw:rect",                                                        
            "draw:line",                                                        
            "draw:polyline",                                                    
            "draw:polygon",                                                     
            "draw:regular-polygon",                                             
            "draw:path",                                                        
            "draw:circle",                                                      
            "draw:ellipse",                                                      
            "draw:g",                                                           
            "draw:page-thumbnail",                                              
            "draw:frame",                                                       
            "draw:measure",                                                         
            "draw:caption",                                                     
            "draw:connector",                                                   
            "draw:control",                                                     
            "dr3d:scene",                                                       
            "draw:custom-shape",                                                
*/ 
        });
        defineIntactTags(new String[] {
            "text:note-citation",                                               
            "text:change",                                                      
            "text:tracked-changes",                                             
            
            "office:scripts",                                                   
            "office:font-face-decls",                                           
            "office:automatic-styles",                                          
            "office:styles",                                                    
            
            "meta:generator",                                                   
            "meta:initial-creator",                                             
            "meta:creation-date",                                               
            "meta:print-date",                                                  
            "dc:creator",                                                       
            "dc:date",                                                          
            "dc:language",                                                      
            "meta:editing-cycles",                                              
            "meta:editing-duration",                                            
            "meta:user-defined",                                                
        });
        if (options.getTranslateNotes()) {
            defineOutOfTurnTag("text:note");                                    
            // OOo 1.x
            defineOutOfTurnTag("text:footnote");                                
        } else {
            defineIntactTag("text:note");                                       
            // OOo 1.x
            defineIntactTag("text:footnote");                                   
        }
        if (options.getTranslateComments())
            defineOutOfTurnTag("office:annotation");                            
        else    
            defineIntactTag("office:annotation");                                       
   
        if (options.getTranslateIndexes())
            defineTranslatableTagAttributes(
                "text:alphabetical-index-mark",                                 
                new String[] { "text:string-value", "text:key1", "text:key2" });
        if (options.getTranslateBookmarks()) {
            defineTranslatableTagsAttribute(
                new String[] 
                    {"text:bookmark", "text:bookmark-start",                    
                     "text:bookmark-end"},                                      
                     "text:name");                                              
            defineTranslatableTagAttribute("text:bookmark-ref", "text:ref-name");
        }
        if (!options.getTranslateBookmarkRefs()) {
            defineIntactTag("text:bookmark-ref");                               
        }
        if (!options.getTranslatePresNotes()) {
            defineIntactTag("presentation:notes");
        }
    
    }

}
