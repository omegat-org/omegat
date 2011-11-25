/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Didier Briel
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

package org.omegat.filters3.xml;

/**
 * A processing instruction in an XML file. For example,
 * <code>&lt;? here is the processing instruction ?&gt;</code>.
 * 
 * @author Didier Briel
 */
public class ProcessingInstruction extends XMLPseudoTag {
    private String data;
    private String target;

    /** Creates a new instance of ProcessingInstruction */
    public ProcessingInstruction(String data, String target) {
        this.data = data;
        this.target = target;
    }

    /**
     * Returns the processing instruction in its original form as it was in the
     * original document.
     */
    public String toOriginal() {
        if (target.length() > 0)
            return "<?" + data + " " + target + "?>";
        else
            return "<?" + data + "?>";
    }
}
