/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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
package org.omegat.core;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Smoke tests against the installed distribution: module fat jars no longer
 * bundle third-party jars that the application already ships in lib/, and
 * modules load through a parent-first class loader, so these tests prove that
 * plugin loading, the bundled spell checker dictionaries and LanguageTool
 * still work with exactly the jars a user receives.
 *
 * The distribution location is taken from the omegat.dist.dir system property
 * (set by the distributionSmokeTest Gradle task).
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@Category(DistributionSmokeTests.class)
public class ModuleDistributionSmokeTest {

    private static final String EN_DICT_BASE = "/org/languagetool/resource/en/hunspell/en_GB";

    private static File distDir;
    private static URLClassLoader parentLoader;

    @BeforeClass
    public static void setUpClass() throws IOException {
        distDir = new File(System.getProperty("omegat.dist.dir", "build/install/OmegaT"));
        assertTrue("Installed distribution not found at " + distDir + "; run installDist first",
                new File(distDir, "OmegaT.jar").isFile());
        // Mirrors PluginUtils: the application class path (OmegaT.jar + lib/*)
        // is the parent of every module class loader.
        List<URL> urls = new ArrayList<>();
        urls.add(new File(distDir, "OmegaT.jar").toURI().toURL());
        for (File jar : listJars(new File(distDir, "lib"))) {
            urls.add(jar.toURI().toURL());
        }
        parentLoader = new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
    }

    @AfterClass
    public static void tearDownClass() throws IOException {
        if (parentLoader != null) {
            parentLoader.close();
        }
    }

    /**
     * Every class named in a module manifest's OmegaT-Plugins attribute must
     * load and initialize with the module jar as the only child of the
     * application class loader.
     */
    @Test
    public void testAllPluginClassesLoad() throws Exception {
        File[] moduleJars = listJars(new File(distDir, "modules"));
        assertTrue("No module jars found in " + distDir, moduleJars.length > 0);
        List<String> failures = new ArrayList<>();
        int loaded = 0;
        for (File moduleJar : moduleJars) {
            String plugins;
            try (JarFile jar = new JarFile(moduleJar)) {
                plugins = jar.getManifest().getMainAttributes().getValue("OmegaT-Plugins");
            }
            if (plugins == null) {
                continue;
            }
            try (URLClassLoader moduleLoader = newModuleLoader(moduleJar)) {
                for (String className : plugins.split("[,\\s]+")) {
                    if (className.isEmpty()) {
                        continue;
                    }
                    try {
                        Class.forName(className, true, moduleLoader);
                        loaded++;
                    } catch (Throwable e) {
                        failures.add(moduleJar.getName() + ": " + className + " -> " + e);
                    }
                }
            }
        }
        assertTrue("Plugin classes failed to load: " + failures, failures.isEmpty());
        assertTrue("Suspiciously few plugin classes found: " + loaded, loaded >= 40);
    }

    /**
     * The bundled Morfologik spelling dictionary must be reachable through
     * the module class loader - the same route LanguageDataBroker falls back
     * to at runtime - and must actually spell words.
     */
    @Test
    public void testBundledSpellCheckerDictionary() throws Exception {
        try (URLClassLoader moduleLoader = newModuleLoader(moduleJar("omegat-language-en.jar"))) {
            Class<?> langClass = Class.forName("org.languagetool.language.BritishEnglish", true, moduleLoader);
            try (InputStream dict = langClass.getResourceAsStream(EN_DICT_BASE + ".dict");
                    InputStream info = langClass.getResourceAsStream(EN_DICT_BASE + ".info")) {
                assertNotNull("en_GB.dict is not reachable through the module class loader", dict);
                assertNotNull("en_GB.info is not reachable through the module class loader", info);
                Class<?> dictionaryClass = Class.forName("morfologik.stemming.Dictionary", true, moduleLoader);
                Object dictionary = dictionaryClass.getMethod("read", InputStream.class, InputStream.class)
                        .invoke(null, dict, info);
                Class<?> spellerClass = Class.forName("morfologik.speller.Speller", true, moduleLoader);
                Object speller = spellerClass.getConstructor(dictionaryClass, int.class).newInstance(dictionary, 1);
                Method isMisspelled = spellerClass.getMethod("isMisspelled", String.class);
                assertFalse("'colour' must be accepted by the bundled en_GB dictionary",
                        (Boolean) isMisspelled.invoke(speller, "colour"));
                assertTrue("'colourxyz' must be flagged by the bundled en_GB dictionary",
                        (Boolean) isMisspelled.invoke(speller, "colourxyz"));
            }
        }
    }

    /**
     * LanguageTool must initialize an English module language and flag a
     * grammar error using only the jars shipped in the distribution.
     */
    @Test
    public void testLanguageToolEnglish() throws Exception {
        assertLanguageToolFindsError("omegat-language-en.jar", "en-GB", "This are a test.");
    }

    /**
     * Same for German, whose module lost the largest amount of duplicated
     * third-party code.
     */
    @Test
    public void testLanguageToolGerman() throws Exception {
        assertLanguageToolFindsError("omegat-language-de.jar", "de-DE",
                "Das ist ein Beispieltext mit einem Fehhler.");
    }

    private void assertLanguageToolFindsError(String moduleJarName, String languageCode, String sentence)
            throws Exception {
        // LanguageTool's default resource broker resolves against the loader
        // of languagetool-core, so core and module must share one loader
        // here; the runtime split-loader route is covered by
        // testBundledSpellCheckerDictionary. The language must come from the
        // Languages registry - LT languages are one-instance-per-loader.
        try (URLClassLoader flatLoader = newFlatLoader(moduleJar(moduleJarName))) {
            Class<?> languagesClass = Class.forName("org.languagetool.Languages", true, flatLoader);
            Object language = languagesClass.getMethod("getLanguageForShortCode", String.class)
                    .invoke(null, languageCode);
            assertNotNull("Language " + languageCode + " not registered by " + moduleJarName, language);
            Class<?> languageBase = Class.forName("org.languagetool.Language", true, flatLoader);
            Class<?> jltClass = Class.forName("org.languagetool.JLanguageTool", true, flatLoader);
            Object languageTool = jltClass.getConstructor(languageBase).newInstance(language);
            List<?> matches = (List<?>) jltClass.getMethod("check", String.class).invoke(languageTool, sentence);
            assertFalse("LanguageTool found no issue in \"" + sentence + "\" via " + moduleJarName,
                    matches.isEmpty());
        }
    }

    private static URLClassLoader newModuleLoader(File moduleJarFile) throws IOException {
        return new URLClassLoader(new URL[] { moduleJarFile.toURI().toURL() }, parentLoader);
    }

    private static URLClassLoader newFlatLoader(File moduleJarFile) throws IOException {
        List<URL> urls = new ArrayList<>();
        urls.add(new File(distDir, "OmegaT.jar").toURI().toURL());
        for (File jar : listJars(new File(distDir, "lib"))) {
            urls.add(jar.toURI().toURL());
        }
        urls.add(moduleJarFile.toURI().toURL());
        return new URLClassLoader(urls.toArray(new URL[0]), ClassLoader.getPlatformClassLoader());
    }

    private static File moduleJar(String name) {
        File jar = new File(new File(distDir, "modules"), name);
        assertTrue("Module jar not found: " + jar, jar.isFile());
        return jar;
    }

    private static File[] listJars(File dir) {
        File[] jars = dir.listFiles((d, n) -> n.endsWith(".jar"));
        assertNotNull("Not a directory: " + dir, jars);
        return jars;
    }
}
