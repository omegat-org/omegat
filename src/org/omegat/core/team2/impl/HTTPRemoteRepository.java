/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Alex Buloichik
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.omegat.util.OStrings;
import tokyo.northside.logging.ILogger;
import tokyo.northside.logging.LoggerFactory;

import org.omegat.core.team2.IRemoteRepository2;
import org.omegat.core.team2.ProjectTeamSettings;
import org.omegat.core.team2.RemoteRepositoryFactory;

import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * HTTP/HTTPS repository connection implementation.
 *
 * It can be used as read-only repository for retrieve sources, external TMX,
 * glossaries, etc. Since HTTP protocol doesn't support multiple files, each URL
 * should be mapped to separate file, i.e. directory mapping is not supported.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class HTTPRemoteRepository implements IRemoteRepository2 {
    private final ILogger logger;

    private RepositoryDefinition config;
    private File baseDirectory;

    public HTTPRemoteRepository() {
        logger = LoggerFactory.getLogger(HTTPRemoteRepository.class, OStrings.getResourceBundle());
    }

    /**
     * Plugin loader.
     */
    public static void loadPlugins() {
        RemoteRepositoryFactory.addRepositoryConnector("http", HTTPRemoteRepository.class);
    }

    /**
     * Plugin unloader.
     */
    public static void unloadPlugins() {
        // there is no way to remove the connector.
    }

    @Override
    public void init(RepositoryDefinition repo, File dir, ProjectTeamSettings teamSettings) throws Exception {
        logger.atDebug().log("Initialize HTTP remote repository");
        config = repo;
        baseDirectory = dir;
    }

    /**
     * Use SHA-1 as a file version.
     */
    @Override
    public String getFileVersion(String file) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        sha1.reset();

        // calculate SHA-1
        byte[] buffer = new byte[8192];
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            while (true) {
                int len = in.read(buffer);
                if (len < 0) {
                    break;
                }
                sha1.update(buffer, 0, len);
            }
        }

        // out as hex
        try (Formatter formatter = new Formatter()) {
            for (byte b : sha1.digest()) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        }
    }

    @Override
    public void switchToVersion(String version) throws Exception {
        if (version != null) {
            throw new RuntimeException("Not supported");
        }

        logger.atDebug().log("Update to latest");
        // retrieve all mapped files
        Properties etags = loadETags();
        for (RepositoryMapping m : config.getMapping()) {
            String url = config.getUrl() + m.getRepository();
            File out = new File(baseDirectory, m.getRepository());
            retrieve(etags, m.getRepository(), url, out);
        }
        saveETags(etags);
    }

    @Override
    public void addForCommit(String path) throws Exception {
        logger.atDebug().setMessage("Cannot add files for commit for HTTP repositories. Skipping \"{0}\".")
                .addArgument(path).log();
    }

    @Override
    public void addForDeletion(String path) throws Exception {
        logger.atDebug().setMessage("Cannot add files for deletion for HTTP repositories. Skipping \"{0}\".")
                .addArgument(path).log();
    }

    @Override
    public File getLocalDirectory() {
        return baseDirectory;
    }

    @Override
    public String[] getRecentlyDeletedFiles() throws Exception {
        return new String[0];
    }

    @Override
    public String commit(String[] onVersions, String comment) throws Exception {
        logger.atDebug().log("Commit not supported for HTTP repositories.");

        return null;
    }

    /**
     * Load all ETags.
     */
    protected Properties loadETags() throws IOException {
        Properties props = new Properties();
        File f = new File(baseDirectory, ".etags");
        if (f.exists()) {
            try (FileInputStream in = new FileInputStream(f)) {
                props.load(in);
            }
        }
        return props;
    }

    /**
     * Save all ETags.
     */
    protected void saveETags(Properties props) throws IOException {
        try (FileOutputStream out = new FileOutputStream(new File(baseDirectory, ".etags"))) {
            props.store(out, null);
        }
    }

    private static final String HEADER_ETAG = "ETag";
    private static final String HEADER_IF_NONE_MATCH = "If-None-Match";

    protected void retrieve(Properties etags, String fileName, String fileUrl, final File outputFile)
            throws IOException, NetworkException {

        String currentEtag = etags.getProperty(fileName);

        logger.atDebug().setMessage("Retrieve {0} into {1} with ETag={2}")
                .addArgument(fileUrl)
                .addArgument(outputFile::getAbsolutePath)
                .addArgument(currentEtag).log();

        outputFile.getParentFile().mkdirs();

        HttpURLConnection connection = (HttpURLConnection) new URL(fileUrl).openConnection();
        try {
            // Set ETag if available
            if (currentEtag != null) {
                connection.setRequestProperty(HEADER_IF_NONE_MATCH, currentEtag);
            }

            // Handles the HTTP response code and performs necessary actions based on the code.
            switch (connection.getResponseCode()) {
                case HttpURLConnection.HTTP_OK:
                    logger.atDebug().setMessage("Retrieve {0}: 200 OK").addArgument(fileUrl).log();
                    break;
                case HttpURLConnection.HTTP_NOT_MODIFIED:
                    // not modified - just return
                    logger.atDebug().setMessage("Retrieve {0}: not modified").addArgument(fileUrl).log();
                    return;
                case HttpURLConnection.HTTP_FORBIDDEN:
                    throw new NetworkException(OStrings.getString("TEAM_HTTP_FORBIDDEN", fileUrl));
                case HttpURLConnection.HTTP_UNAUTHORIZED:
                    throw new NetworkException(OStrings.getString("TEAM_HTTP_UNAUTHORIZED", fileUrl));
                case HttpURLConnection.HTTP_NOT_FOUND:
                    throw new NetworkException(OStrings.getString("TEAM_HTTP_NOT_FOUND", fileUrl));
                default:
                    throw new NetworkException(OStrings.getString("TEAM_HTTP_OTHER_ERRORS", fileUrl, connection.getResponseCode()));
            }

            // Load into .tmp file
            File tempFile = new File(outputFile.getAbsolutePath() + ".tmp");
            try (InputStream inputStream = connection.getInputStream()) {
                FileUtils.copyInputStreamToFile(inputStream, tempFile);
            }

            // Safely rename temporary file to output file
            safelyRenameFile(tempFile, outputFile);

            // Store new ETag if provided
            String newEtag = connection.getHeaderField(HEADER_ETAG);
            if (newEtag != null) {
                etags.setProperty(fileName, newEtag);
            }

        } catch (UnknownHostException | SocketException e) {
            throw new NetworkException(e);
        } finally {
            connection.disconnect();
        }

        logger.atDebug().setMessage("Retrieve {0} finished").addArgument(fileUrl).log();
    }

    /**
     * Safely renames the temporary file to the output file, ensuring no remnants of old files.
     */
    private void safelyRenameFile(File tempFile, File outputFile) throws IOException {
        if (outputFile.exists()) {
            Files.delete(outputFile.toPath());
        }
        if (!tempFile.renameTo(outputFile)) {
            throw new IOException("Failed to rename temporary file to output file");
        }
    }
}
