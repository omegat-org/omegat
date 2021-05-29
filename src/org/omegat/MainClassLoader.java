/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
               2018 Mordechai Meisels (Original licensed Apache-2.0)
               Home page: http://www.omegat.org/
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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

/**
 * @author Hiroshi Miura
 */
public final class MainClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    private String name;

    /**
     * Java9 compatible class loader constructor.
     * @param name name of class loader
     * @param parent
     */
    public MainClassLoader(String name, ClassLoader parent) {
        this(parent);
        this.name = name;
    }

    /*
     * Required when this classloader is used as the system classloader
     */
    public MainClassLoader(ClassLoader parent) {
        this(new URL[0], parent);
    }

    public MainClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public MainClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Main class can add jar classpath for plugins in dynamic manner.
     * @param url
     */
    synchronized void add(URL url) {
        addURL(url);
    }

    synchronized void addJarToClasspath(String jarName)
            throws MalformedURLException {
        URL url = new File(jarName).toURI().toURL();
        addURL(url);
    }

    public static MainClassLoader findAncestor(ClassLoader cl) {
        do {
            if (cl instanceof MainClassLoader) {
                return (MainClassLoader) cl;
            }
            cl = cl.getParent();
        } while (cl != null);
        return null;
    }

    /*
     *  Required for Java Agents when this classloader is used as the system classloader
     */
    @SuppressWarnings("unused")
    private void appendToClassPathForInstrumentation(String jarfile) throws IOException {
        add(Paths.get(jarfile).toRealPath().toUri().toURL());
    }
}
