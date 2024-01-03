/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
               2018 Mordechai Meisels (Original licensed Apache-2.0)
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

package org.omegat;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * @author Hiroshi Miura
 */
public class MainClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    private String name;

    /**
     * {@inheritDoc}
     */
    public MainClassLoader(String name, ClassLoader parent) {
        super(name, new URL[0], parent);
    }

    /**
     * {@inheritDoc}
     */
    public MainClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * {@inheritDoc}
     */
    public MainClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }

    /**
     * {@inheritDoc}
     */
    public MainClassLoader(URL[] urls) {
        super(urls, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a new class loader using the ClassLoader returned by the method
     * Thread.currentThread().getContextClassLoader() as the parent class
     * loader.
     */
    public MainClassLoader() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Add a jar classpath.
     * @param url jar file classpath.
     */
    public void addJarToClassPath(URL url) {
        addURL(url);
    }

    void addJarToClassPath(String jarName) throws MalformedURLException {
        URL url = new File(jarName).toURI().toURL();
        addURL(url);
    }

    public List<URL> getUrlList() {
        return Arrays.asList(super.getURLs());
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

    /**
     * Creates a new instance of URLClassLoader for the specified
     * URLs and parent class loader.
     */
    public static MainClassLoader newInstance(URL[] urls, ClassLoader parent) {
        return new MainClassLoader(urls, parent);
    }

    /**
     * Required for Java Agents when this classloader is used as the system classloader.
     * <p>
     * It is not required to be public.
     * @see
     *  java.lang.instrument.Instrumentation#appendToSystemClassLoaderSearch
     */
    @SuppressWarnings("unused")
    private void appendToClassPathForInstrumentation(String jarfile) throws IOException {
        addURL(Paths.get(jarfile).toRealPath().toUri().toURL());
    }
}
