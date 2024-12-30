/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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

package org.omegat.core.threads;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.omegat.util.PluginInstaller;
import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.Log;

public class PluginDownloadThread extends LongProcessThread {

    private final URL url;
    private final String archive;
    private final String checksum;
    private final HashMap<String, String> headers = new HashMap<>();
    private static final Set<String> mimetype = new HashSet<>();

    static {
        mimetype.add("application/octet-stream");
        mimetype.add("application/java-archive");
    }

    public PluginDownloadThread(URL url, String sha256Sum, String filename) throws UnsupportedEncodingException {
        this.url = url;
        this.checksum = sha256Sum;
        this.archive = filename;
    }

    @Override
    public void run() {
        try {
            Path temporaryDir = Files.createTempDirectory("omegat");
            File temporaryFilePath = new File(temporaryDir.toFile(), archive);
            temporaryDir.toFile().deleteOnExit();
            Log.log("Start downloading from " + url.toString());
            boolean result = HttpConnectionUtils.downloadBinaryFile(url, headers, mimetype, temporaryFilePath);
            if (!result) {
                Log.log("Failed to download plugin file.");
            } else if (!checksum.equals(calculateSha256(temporaryFilePath))) {
                Log.log("Checksum error of plugin file.");
            } else {
                PluginInstaller.getInstance().install(temporaryFilePath, true);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String calculateSha256(final File targetFilePath) throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        sha256.reset();

        byte[] buffer = new byte[8192];
        try (InputStream in = new BufferedInputStream(new FileInputStream(targetFilePath))) {
            while (true) {
                int len = in.read(buffer);
                if (len < 0) {
                    break;
                }
                sha256.update(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // out as hex
        Formatter formatter = new Formatter();
        try {
            for (byte b : sha256.digest()) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } finally {
            formatter.close();
        }
    }
}
