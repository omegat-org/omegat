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
import java.security.MessageDigest;
import java.text.MessageFormat;
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
    }

    @Override
    public void init(RepositoryDefinition repo, File dir, ProjectTeamSettings teamSettings) throws Exception {
        logger.atDebug().log("Initialize HTTP remote repository");
        config = repo;
        baseDirectory = dir;
    }

    /**
     * Use SHA-1 as file version.
     */
    @Override
    public String getFileVersion(String file) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        sha1.reset();

        // calculate SHA-1
        byte[] buffer = new byte[8192];
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            while (true) {
                int len = in.read(buffer);
                if (len < 0) {
                    break;
                }
                sha1.update(buffer, 0, len);
            }
        } finally {
            in.close();
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
    protected Properties loadETags() throws Exception {
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
    protected void saveETags(Properties props) throws Exception {
        try (FileOutputStream out = new FileOutputStream(new File(baseDirectory, ".etags"))) {
            props.store(out, null);
        }
    }

    /**
     * Retrieve remote URL with non-modified checking by ETag. If server doesn't
     * support ETag, file will be always retrieved.
     */
    protected void retrieve(Properties etags, String file, String url, final File out) throws Exception {
        String etag = etags.getProperty(file);
        logger.atDebug().setMessage("Retrieve {0} into {1} with ETag={2}").addArgument(url)
                .addArgument(out::getAbsolutePath).addArgument(etag).log();

        out.getParentFile().mkdirs();
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        try {
            if (etag != null) {
                // use ETag if we know it
                conn.setRequestProperty("If-None-Match", etag);
            }
            switch (conn.getResponseCode()) {
            case HttpURLConnection.HTTP_OK:
                etag = conn.getHeaderField("ETag");
                logger.atDebug().setMessage("Retrieve {0}: 200 with ETag={1}").addArgument(url).addArgument(etag).log();
                break;
            case HttpURLConnection.HTTP_NOT_MODIFIED:
                // not modified - just return
                logger.atDebug().setMessage("Retrieve {0}: not modified").addArgument(url).log();
                return;
            case HttpURLConnection.HTTP_FORBIDDEN:
                throw new NetworkException(MessageFormat.format("Access to {0} is forbidden.", url));
            case HttpURLConnection.HTTP_UNAUTHORIZED:
                throw new NetworkException(MessageFormat.format("Access to {0} is rejected by authentication error.", url));
            case HttpURLConnection.HTTP_NOT_FOUND:
                throw new NetworkException(MessageFormat.format("Contents at {0} is not found.", url));
            default:
                throw new NetworkException(String.format("HTTP connection %s response with code: %d", url,
                        conn.getResponseCode()));
            }

            // load into .tmp file
            File temp = new File(out.getAbsolutePath() + ".tmp");
            try (InputStream in = conn.getInputStream()) {
                FileUtils.copyInputStreamToFile(in, temp);
            }

            // rename into file
            if (out.exists()) {
                if (!out.delete()) {
                    throw new IOException();
                }
            }
            if (!temp.renameTo(out)) {
                throw new IOException();
            }
            try {
                etags.setProperty(file, etag);
            } catch (Exception ex) {
                // Etags are optionnal, we eat the exception is there is none
            }
        } catch (UnknownHostException | SocketException e) {
            throw new NetworkException(e);
        } finally {
            conn.disconnect();
        }
        logger.atDebug().setMessage("Retrieve {0} finished").addArgument(url).log();
    }
}
