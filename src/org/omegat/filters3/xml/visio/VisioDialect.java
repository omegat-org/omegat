/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007-2010 Didier Briel
               2012 Phillip Hall
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.filters3.xml.visio;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies Visio XML Dialect.
 * Works on Visio 2003 vdx files (other versions of Visio have not been tested)
 * 
 * @author Didier Briel
 * @author Phillip Hall
 * 
 */
public class VisioDialect extends DefaultXMLDialect {
    public VisioDialect() {
        defineParagraphTags(new String[] {
            "Text",                                                           
        });


       defineIntactTags(new String[] {
           "DocumentProperties",                                                       
           "DocumentSettings",                                                       
           "Colors",                                                       
           "FaceNames",                                                       
           "StyleSheets",                                                       
           "DocumentSheet",                                                       
           "Masters",                                                       
           "Misc",                                                       
           "TextBlock",                                                       
           "Geom",                                                       
           "Para",                                                       
           "Char",                                                       
           "Connection",                                                       
           "XForm",                                                       
           "Line",                                                       
           "Fill",                                                       
           "Event",                                                       
           "PageSheet",                                                       
           "PageProps",                                                       
           "PageLayout",                                                       
           "PrintProps",                                                       
           "PageHeight",                                                       
           "PageWidth",                                                       
           "Image",                                                       
           "PinY",                                                       
           "Width",                                                       
           "Height",                                                       
           "XForm1D",                                                       
           "EndX",                                                       
           "LayerMem",                                                       
           "TextXForm",                                                       
           "Control",                                                       
           "ForeignData",                                                       
           "Foreign",                                                       
           "Menu",                                                       
           "Act",                                                       
           "User",                                                       
           "Help",                                                       
           "Copyright",                                                       
           "VBProjectData",                                                       
           "FooterMargin",                                                       
           "HeaderMargin",                                                       
           "HeaderFooter",                                                       
           "Window",                                                       
           "Windows",                                                       
           "EventList",                                                       
           "Scratch",                                                       
           "TextBlock",                                                       
           "Fill",                                                       
           "Protection",                                                       
           "Layout",                                                       
       });
    }

}
