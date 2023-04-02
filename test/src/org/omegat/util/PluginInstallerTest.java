/*
 **************************************************************************
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2022 Hiroshi Miura.
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.omegat.core.data.PluginInformation;
import org.omegat.filters2.master.PluginUtils;

public class PluginInstallerTest {
        private File tmpDir;

    @Before
    public final void setUp() throws Exception {
        tmpDir = Files.createTempDirectory("oemgat").toFile();
        assertTrue(tmpDir.isDirectory());
    }

    @After
    public final void tearDown() throws Exception {
        FileUtils.deleteDirectory(tmpDir);
    }

    @Test
    public final void testUnpackPlugin() throws Exception {
        File zipFile = new File("test/data/plugin/ex-filter.zip");
        PluginInstaller.unpackPlugin(zipFile, tmpDir.toPath());
        Path target = tmpDir.toPath().resolve("ex-filter-1.0.0.jar");
        assertTrue(target.toFile().exists());
    }

    @Test
    public final void testParseManifest() throws Exception {
        File manifest = new File("test/data/plugin/simple/MANIFEST.MF");
        final String expectedName = "Filters for OmegaT";
        URL mu = manifest.toURI().toURL();
        Set<PluginInformation> pluginInformationSet = PluginInstaller.parsePluginJarFileManifest(mu);
        assertEquals(1, pluginInformationSet.size());
        PluginInformation pluginInformation = pluginInformationSet.stream().findFirst().orElse(null);
        assertNotNull(pluginInformation);
        assertEquals("1.0.0", pluginInformation.getVersion());
        assertEquals(expectedName, pluginInformation.getName());
    }

    @Test
    public final void testParseLegacyManifest() throws Exception {
        File manifest = new File("test/data/plugin/legacy/MANIFEST.MF");
        String[] expected = {"Xliff1Filter", "Xliff2Filter", "SdlXliff", "SdlProject", "MqXliff", "MsOfficeFileFilter"};
        final Set<String> expectedSet = new HashSet<>();
        Collections.addAll(expectedSet, expected);
        URL mu = manifest.toURI().toURL();
        Set<PluginInformation> pluginInformationSet = PluginInstaller.parsePluginJarFileManifest(mu);
        assertEquals(expected.length, pluginInformationSet.size());
        for (PluginInformation pluginInformation: pluginInformationSet) {
            String pluginName = pluginInformation.getName();
            assertTrue(expectedSet.contains(pluginName));
            assertEquals("", pluginInformation.getVersion());
            assertEquals(PluginUtils.PluginType.FILTER, pluginInformation.getCategory());
            if (pluginName.equals("MsOfficeFileFilter")) {
                assertEquals("org.omegat.filters4.xml.openxml.MsOfficeFileFilter",
                        pluginInformation.getClassName());
            } else {
                assertEquals("org.omegat.filters4.xml.xliff." + pluginName,
                        pluginInformation.getClassName());
            }
        }
    }

    @Test
    public final void testParseLegacyWithNameManifest() throws Exception {
        File manifest = new File("test/data/plugin/withName/MANIFEST.MF");
        URL mu = manifest.toURI().toURL();
        Set<PluginInformation> pluginInformationSet = PluginInstaller.parsePluginJarFileManifest(mu);
        assertEquals(6, pluginInformationSet.size());
        for (PluginInformation pluginInformation: pluginInformationSet) {
            String pluginName = pluginInformation.getName();
            assertEquals("Stax plugin", pluginName);
            assertEquals("2.1.3", pluginInformation.getVersion());
            assertEquals(PluginUtils.PluginType.FILTER, pluginInformation.getCategory());
            assertTrue(pluginInformation.getClassName().startsWith("org.omegat.filters4.xml"));
        }
    }
}
