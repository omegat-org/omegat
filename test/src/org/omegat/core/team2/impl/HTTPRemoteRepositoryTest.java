/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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

package org.omegat.core.team2.impl;

import com.github.tomakehurst.wiremock.client.WireMock;
import gen.core.project.ObjectFactory;
import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;
import org.junit.Test;
import org.omegat.core.TestCoreWireMock;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class HTTPRemoteRepositoryTest extends TestCoreWireMock {

    @Test
    public void testRetrieveRetrievesFileSuccessfully() throws Exception {
        HTTPRemoteRepository repository = new HTTPRemoteRepository();
        RepositoryDefinition mockConfig = new RepositoryDefinition();
        int port = wireMockRule.port();
        String url = String.format("http://localhost:%d/repository/", port);
        mockConfig.setUrl(url);

        Path tempDir = Files.createTempDirectory("omegat");
        repository.init(mockConfig, tempDir.toFile(), null);
        Properties etags = new Properties();
        etags.setProperty("file.txt", "TestETag");
        File outputFile = tempDir.resolve("file.txt").toFile();

        WireMock.stubFor(WireMock.head(WireMock.anyUrl())
                .willReturn(WireMock.aResponse().withStatus(200)
                        .withHeader("ETag", "TestETag")
                        .withHeader("Content-Type", "text/plain")));
        WireMock.stubFor(WireMock.get(WireMock.anyUrl())
                .willReturn(WireMock.aResponse().withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withHeader("ETag", "TestETag")
                        .withBody("Test file contents")));

        // Mock URL connection for successful retrieval
        repository.retrieve(etags, "file.txt", url + "file.txt", outputFile);

        assertTrue("File should exist after retrieve", outputFile.exists());
        assertEquals("File contents should match", "Test file contents", Files.readString(outputFile.toPath()));
    }

    @Test
    public void testSwitchToVersionThrowsExceptionWhenVersionIsNotNull() throws Exception {
        HTTPRemoteRepository repository = new HTTPRemoteRepository();
        RepositoryDefinition mockConfig = new RepositoryDefinition();
        int port = wireMockRule.port();
        String url = String.format("http://localhost:%d/repository/", port);
        mockConfig.setUrl(url);
        Path tempDir = Files.createTempDirectory("omegat");
        repository.init(mockConfig, tempDir.toFile(), null);

        Exception exception = null;

        try {
            repository.switchToVersion("1.0");
        } catch (RuntimeException e) {
            exception = e;
        }

        assertNotNull("Expected RuntimeException to be thrown", exception);
        assertEquals("Not supported", exception.getMessage());
    }

    @Test
    public void testSwitchToVersionUpdatesToLatest() throws Exception {
        HTTPRemoteRepository repository = new HTTPRemoteRepository();
        RepositoryDefinition mockConfig = new RepositoryDefinition();
        int port = wireMockRule.port();
        String url = String.format("http://localhost:%d/repository/", port);
        mockConfig.setUrl(url);
        //
        ObjectFactory factory = new ObjectFactory();
        RepositoryMapping mapping = factory.createRepositoryMapping();
        mapping.setRepository("file.txt");
        mapping.setLocal("file.txt");
        //
        mockConfig.getMapping().add(mapping);
        //
        Path tempDir = Files.createTempDirectory("omegat");
        repository.init(mockConfig, tempDir.toFile(), null);

        WireMock.stubFor(WireMock.head(WireMock.anyUrl())
                        .willReturn(WireMock.aResponse().withStatus(200)));
        WireMock.stubFor(WireMock.get(WireMock.anyUrl())
                .willReturn(WireMock.aResponse().withStatus(200)
                        .withHeader("Content-Type", "text/plain")
                        .withBody("Test file contents")));

        // Simulate calling switchToVersion
        repository.switchToVersion(null);

        File outputFile = tempDir.resolve("file.txt").toFile();
        assertTrue("File should exist after retrieve", outputFile.exists());
        assertEquals("File contents should match", "Test file contents", Files.readString(outputFile.toPath()));
    }
}
