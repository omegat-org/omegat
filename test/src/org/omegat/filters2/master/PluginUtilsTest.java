/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2023 Briac Pilpré
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

package org.omegat.filters2.master;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.omegat.util.FileUtil;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PluginUtilsTest {

    @Test
    public void testLoadLatestPluginVersionOnly()
            throws JsonProcessingException, JAXBException, MalformedURLException, URISyntaxException {
        final List<File> pluginsDirs = new ArrayList<>();
        File testDataDir = new File("test/data/plugin/jar");
        URL testDataDirURL = testDataDir.toURI().toURL();
        pluginsDirs.add(testDataDir);

        // list all jars in /plugins/
        List<File> fileList = pluginsDirs.stream().flatMap(
                dir -> FileUtil.findFiles(dir, pathname -> pathname.getName().endsWith(".jar")).stream())
                .collect(Collectors.toList());

        assertEquals(5, fileList.size());
        List<URL> urlList = PluginUtils.populatePluginUrlList(pluginsDirs);
        assertEquals(2, urlList.size());

        final Set<URI> expected = new HashSet<>();
        expected.add(new URL(testDataDirURL, "anotherPlugin-0.3.1.jar").toURI());
        expected.add(new URL(testDataDirURL, "testPlugin-1.13-1.45.0.jar").toURI());
        assertTrue(urlList.stream().map(u -> {
            try {
                return u.toURI();
            } catch (URISyntaxException e) {
                return null;
            }
        }).collect(Collectors.toList()).containsAll(expected));
    }

}
