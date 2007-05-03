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

package org.omegat.filters3.xml.opendoc;

import org.omegat.core.threads.CommandThread;
import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * Dialect of OpenDocument files.
 *
 * @author Maxym Mykhalchuk
 */
public class OpenDocDialect extends DefaultXMLDialect
{
    
    /** Creates a new instance of OpenDocDialect */
    public OpenDocDialect()
    {
        defineShortcuts(new String[]{
            "text:line-break", "br",                                            // NOI18N
            "text:a", "a",                                                      // NOI18N
            "text:span", "f",                                                   // NOI18N
            "text:s", "s",                                                      // NOI18N
            "text:alphabetical-index-mark", "i",                                // NOI18N
            "text:alphabetical-index-mark-start", "is",                         // NOI18N
            "text:alphabetical-index-mark-end", "ie",                           // NOI18N
            "text:tab-stop", "t",                                               // NOI18N
            "text:user-defined", "ud",                                          // NOI18N
            "text:sequence", "seq",                                             // NOI18N
        
            // Donated by Didier Briel
            // http://sourceforge.net/support/tracker.php?aid=1458673
            "draw:image", "di",                                                 // NOI18N
            "draw:frame", "df",                                                 // NOI18N
            "draw:object-ole", "do",                                            // NOI18N

            "text:bookmark", "bk",                                              // NOI18N
            "text:bookmark-start", "bs",                                        // NOI18N
            "text:bookmark-end", "be",                                          // NOI18N
            "text:bookmark-ref", "bf",                                          // NOI18N
            "text:reference-mark", "rm",                                        // NOI18N
            "text:reference-mark-start", "rs",                                  // NOI18N
            "text:reference-mark-end", "re",                                    // NOI18N
            "text:reference-ref", "rf",                                         // NOI18N
        
            "text:change", "tc",                                                // NOI18N
            "text:change-start", "ts",                                          // NOI18N
            "text:change-end", "te",                                            // NOI18N
            "dc:creator", "dc",                                                 // NOI18N
            "dc:date", "dd",                                                    // NOI18N
            // End of contribution

            // http://sourceforge.net/support/tracker.php?aid=1461154
            "text:note-citation", "nc",                                         // NOI18N
            "text:note-body", "nb",                                             // NOI18N
        });
        
        defineParagraphTags(new String[]
        {
            "text:p",                                                           // NOI18N
            "text:h",                                                           // NOI18N
            "dc:title",                                                         // NOI18N
            "dc:description",                                                   // NOI18N
            "dc:subject",                                                       // NOI18N
            "meta:keyword",                                                     // NOI18N
            "dc:language",                                                      // NOI18N
            "meta:user-defined",                                                // NOI18N
            "text:tab",                                                         // NOI18N
        });
        
        defineOutOfTurnTags(new String[]
        {
            "text:note",                                                        // NOI18N
            // Comments [1628890]
            "office:annotation",                                                // NOI18N
            // Drawing Shapes from OO specifications
            // Correction for [ 1541277 ] OO: Segmenting on inline drawings
            // Commented until [ 1642994 ] Subtexts are not segmented is solved
/*
            "draw:rect",                                                        // NOI18N
            "draw:line",                                                        // NOI18N
            "draw:polyline",                                                    // NOI18N
            "draw:polygon",                                                     // NOI18N
            "draw:regular-polygon",                                             // NOI18N
            "draw:path",                                                        // NOI18N
            "draw:circle",                                                      // NOI18N
            "draw:ellipse",                                                     // NOI18N 
            "draw:g",                                                           // NOI18N
            "draw:page-thumbnail",                                              // NOI18N
            "draw:frame",                                                       // NOI18N
            "draw:measure",                                                     // NOI18N    
            "draw:caption",                                                     // NOI18N
            "draw:connector",                                                   // NOI18N
            "draw:control",                                                     // NOI18N
            "dr3d:scene",                                                       // NOI18N
            "draw:custom-shape",                                                // NOI18N
*/ 
        });
        defineIntactTags(new String[]
        {
            "text:note-citation",                                               // NOI18N
            "text:change",                                                      // NOI18N
            "text:tracked-changes",                                             // NOI18N
            
            "office:scripts",                                                   // NOI18N
            "office:font-face-decls",                                           // NOI18N
            "office:automatic-styles",                                          // NOI18N
            "office:styles",                                                    // NOI18N
            
            "meta:generator",                                                   // NOI18N
            "meta:initial-creator",                                             // NOI18N
            "meta:creation-date",                                               // NOI18N
            "meta:print-date",                                                  // NOI18N
            "dc:creator",                                                       // NOI18N
            "dc:date",                                                          // NOI18N
            "dc:language",                                                      // NOI18N
            "meta:editing-cycles",                                              // NOI18N
            "meta:editing-duration",                                            // NOI18N
            
        });
        defineTranslatableTagAttributes(
                "text:alphabetical-index-mark",                                 // NOI18N
                new String[] { "text:string-value", "text:key1", "text:key2" });// NOI18N
        defineTranslatableTagsAttribute(
                new String[] {"text:bookmark", "text:bookmark-start", "text:bookmark-end"}, // NOI18N
                "text:name");                                                   // NOI18N
        defineTranslatableTagAttribute("text:bookmark-ref", "text:ref-name");   // NOI18N
    }
}
