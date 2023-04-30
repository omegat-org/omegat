/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.convert.v20to21.data;

import java.io.Serializable;
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
        return instances.toArray(new Instance[instances.size()]);
    }

    /**
     * Sets all the instances of the filter at once.
     */
    public void setInstance(Instance[] instance) {
        instances = new ArrayList<Instance>(Arrays.asList(instance));
    }

    /** Holds whether the filter's source encoding can be varied by user */
    private boolean sourceEncodingVariable;

    /**
     * Returns whether the filter's source encoding can be varied by user
     */
    public boolean isSourceEncodingVariable() {
        return sourceEncodingVariable;
    }

    /**
     * Sets whether the filter's source encoding can be varied by user
     */
    public void setSourceEncodingVariable(boolean value) {
        sourceEncodingVariable = value;
    }

    /** Holds whether the filter's target encoding can be varied by user */
    private boolean targetEncodingVariable;

    /**
     * Returns whether the filter's target encoding can be varied by user
     */
    public boolean isTargetEncodingVariable() {
        return targetEncodingVariable;
    }

    /**
     * Sets whether the filter's target encoding can be varied by user
     */
    public void setTargetEncodingVariable(boolean value) {
        targetEncodingVariable = value;
    }

    /** If the filter is used. */
    private boolean on = true;

    /**
     * Returns whether the filter is on (used by OmegaT).
     */
    public boolean isOn() {
        return on;
    }

    /**
     * Sets whether the filter is on (used by OmegaT).
     */
    public void setOn(boolean value) {
        on = value;
    }

    /** Holds options of the filter. */
    private Serializable options = null;

    /**
     * Returns filter's options.
     *
     * @return Filter options object.
     */
    public Serializable getOptions() {
        return this.options;
    }

    /**
     * Setter for property options.
     *
     * @param options
     *            New value of property options.
     */
    public void setOptions(Serializable options) {
        this.options = options;
    }

}
