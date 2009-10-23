/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
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

package org.omegat.convert.v20to21.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Wrapper around a single file filter class Manages entries in XML config file
 * and provides a table model.
 * 
 * @author Maxym Mykhalchuk
 */
public class OneFilter {
    /** Holds the class name of the filter */
    private String className = null;

    /**
     * Returns the class name of the filter.
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the class name of the filter.
     */
    public void setClassName(String value) {
        className = value;
    }

    /** Holds instances property. */
    private List<Instance> instances = new ArrayList<Instance>();

    /**
     * Returns all the instances of the filter.
     */
    public Instance[] getInstance() {
        return instances.toArray(new Instance[0]);
    }

    /**
     * Sets all the instances of the filter at once.
     */
    public void setInstance(Instance[] instance) {
        instances = new ArrayList<Instance>(Arrays.asList(instance));
    }
}
