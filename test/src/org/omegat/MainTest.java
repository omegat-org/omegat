/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Aaron Madlon-Kay
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;

import org.omegat.core.data.ProjectProperties;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.ProjectFileStorage;
import org.omegat.util.TMXReader2;

public class MainTest {

    /**
     *  Flag whether TMXWriter or TMXWriter2 is used in Main functions
     *  to alter expectations according to its difference.
     */
    private static final boolean USE_WRITER2 = false;

    private static Path tmpDir;
    private static Path confDir;

    @BeforeClass
    public static void setUpClass() throws Exception {
        tmpDir = Files.createTempDirectory("omegat");
        confDir = tmpDir.resolve("conf");
        File prefsFile = confDir.resolve(Preferences.FILE_PREFERENCES).toFile();
        FileUtils.createParentDirectories(prefsFile);
        assertTrue(confDir.toFile().isDirectory());
        PrintWriter out = new PrintWriter(prefsFile, "UTF-8");
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        out.println("<omegat><preference version=\"1.0\">");
        out.println("</preference></omegat>");
        out.close();
        FileUtils.copyFile(
                Paths.get("test/data/main/segmentation.conf").toFile(),
                confDir.resolve("segmentation.conf").toFile());
        FileUtils.copyFile(
                Paths.get("test/data/main/filters.xml").toFile(),
                confDir.resolve("filters.xml").toFile());
        assertTrue(prefsFile.canRead());
        assertTrue(confDir.resolve("filters.xml").toFile().canRead());
        assertTrue(confDir.resolve("segmentation.conf").toFile().canRead());
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FileUtils.deleteDirectory(tmpDir.toFile());
        assertFalse(tmpDir.toFile().exists());
    }

    @Before
    public final void setUp() {
        XMLUnit.setControlEntityResolver(TMXReader2.TMX_DTD_RESOLVER);
        XMLUnit.setTestEntityResolver(TMXReader2.TMX_DTD_RESOLVER);
        XMLUnit.setIgnoreWhitespace(true);
    }

    @After
    public final void tearDown() throws Exception {
        XMLUnit.setControlEntityResolver(null);
        XMLUnit.setTestEntityResolver(null);
    }

    @Test
    public void testConsoleTranslate() throws Exception {
        Path projectDir = createProject("console-translate");
        String fileName = "foo.txt";
        List<String> fileContent = Arrays.asList("Foo");

        Path srcFile = projectDir.resolve(OConsts.DEFAULT_SOURCE).resolve(fileName);
        Files.write(srcFile, fileContent);

        Main.main(new String[] {
                "--mode=console-translate",
                "--config-dir=" + confDir.toAbsolutePath(),
                projectDir.toString()
        });

        Path trgFile = projectDir.resolve(OConsts.DEFAULT_TARGET).resolve(fileName);
        assertTrue(trgFile.toFile().isFile());
        assertEquals(fileContent, Files.readAllLines(trgFile));
    }

    @Test
    public void testCreatePseudoTranslateTMX() throws Exception {
        Path expectedTemplatePath = Paths.get("test/data/main/allsegments.tmx");
        Path projectDir = createProject("console-pseudo-translatetmx");

        String fileName = "foo.txt";
        List<String> fileContent = Arrays.asList("Foo");
        Path srcFile = projectDir.resolve(OConsts.DEFAULT_SOURCE).resolve(fileName);
        Files.write(srcFile, fileContent);

        Path targetFile = projectDir.resolve("allsegments.tmx");

        Main.main(new String[] {
                projectDir.toString(),
                "--config-dir=" + confDir.toAbsolutePath(),
                "--mode=console-createpseudotranslatetmx",
                "--pseudotranslatetmx=" + targetFile,
                "--pseudotranslatetype=equal",
                "--no-team"
        });

        assertTrue(targetFile.toFile().isFile());
        List<String> expectedContent = Files.readAllLines(expectedTemplatePath).stream()
                .map(line -> line.replaceAll("%creationtoolversion%", getVersion()))
                .collect(Collectors.toList());
        Path expectedFile = projectDir.resolve("expected.tmx");
        Files.write(expectedFile, expectedContent);
        compareXML(expectedFile.toFile(), targetFile.toFile());
    }

    @Test
    public void runConsoleAlign() throws Exception {
        Path expectedTemplatePath = Paths.get("test/data/main/align.tmx");
        Path projectDir = createProject("console-align");

        String fileName = "foo.properties";
        List<String> fileContent = Arrays.asList("Foo=Foo.", "Boo=Boo.");
        Path srcFile = projectDir.resolve(OConsts.DEFAULT_SOURCE).resolve(fileName);
        Files.write(srcFile, fileContent);
        String targetName = "foo_fr.properties";
        List<String> targetContent = Arrays.asList("Foo=Foo1.", "Boo=Boo1.");
        Path alignFile = projectDir.resolve(OConsts.DEFAULT_TARGET).resolve(targetName);
        Files.write(alignFile, targetContent);
        Path alignDir = projectDir.resolve(OConsts.DEFAULT_TARGET);
        Path targetFile = projectDir.resolve("omegat/align.tmx");

        Main.main(new String[] {
                projectDir.toString(),
                "--config-dir=" + confDir.toAbsolutePath(),
                "--mode=console-align",
                "--alignDir=" + alignDir
        });

        assertTrue(targetFile.toFile().isFile());
        List<String> expectedContent = Files.readAllLines(expectedTemplatePath).stream()
                .map(line -> line.replaceAll("%creationtoolversion%", getVersion()))
                .collect(Collectors.toList());
        Path expectedFile = projectDir.resolve("expected.tmx");
        Files.write(expectedFile, expectedContent);
        compareXML(expectedFile.toFile(), targetFile.toFile());
    }

    /* TMXWriter and TMXWriter2 produce different version string for creationtoolversion attribute.
     */
    private String getVersion() {
        if (USE_WRITER2) {
            return OStrings.getVersion();
        }
        // version by old TMXWriter
        String version = OStrings.VERSION;
        if (!OStrings.UPDATE.equals("0")) {
            version = version + "_" + OStrings.UPDATE;
        }
        return version;
    }

    private Path createProject(final String name) throws Exception {
        Path projectDir = tmpDir.resolve(name);
        FileUtils.createParentDirectories(projectDir.resolve(OConsts.DEFAULT_INTERNAL).toFile());
        // Create project properties
        ProjectProperties props = new ProjectProperties(projectDir.toFile());
        // set languages
        props.setSourceLanguage("en");
        props.setTargetLanguage("fr");
        // Create project internal directories
        props.autocreateDirectories();
        // Create version-controlled glossary file
        assertTrue(props.getWritableGlossaryFile().getAsFile().createNewFile());
        ProjectFileStorage.writeProjectFile(props);
        return projectDir;
    }

    private void compareXML(File f1, File f2) throws Exception {
        compareXML(f1.toURI().toURL(), f2.toURI().toURL());
    }

    private void compareXML(URL f1, URL f2) throws Exception {
        XMLAssert.assertXMLEqual(new InputSource(f1.toExternalForm()), new InputSource(f2.toExternalForm()));
    }

}
