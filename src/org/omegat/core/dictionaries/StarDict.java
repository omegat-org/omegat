/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.core.dictionaries;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.omegat.util.LFileCopy;

/**
 * Dictionary implementation for StarDict format.
 * 
 * StarDict format described on
 * http://code.google.com/p/babiloo/wiki/StarDict_format
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public class StarDict implements IDictionary {
    protected final File ifoFile;
    protected static final String UTF8 = "UTF-8";
    protected static final int BUFFER_SIZE = 64 * 1024;

    /** Dictionary type, from 'sametypesequence' header. */
    protected final String contentType;

    /**
     * @param ifoFile
     *            ifo file with dictionary
     */
    public StarDict(File ifoFile) throws Exception {
        this.ifoFile = ifoFile;

        Map<String, String> header = readIFO(ifoFile);
        String version = header.get("version");
        if (!"2.4.2".equals(version)) {
            throw new Exception("Invalid version of dictionary: " + version);
        }
        contentType = header.get("sametypesequence");
        if (!"g".equals(contentType) && !"m".equals(contentType)) {
            throw new Exception("Invalid type of dictionary: " + contentType);
        }
    }

    /**
     * Read dictionarie's header.
     */
    public Map<String, Object> readHeader() throws IOException {
        Map<String, Object> result = new HashMap<String, Object>();
        String f = ifoFile.getPath();
        if (f.endsWith(".ifo")) {
            f = f.substring(0, f.length() - 4);
        }
        File idxFile = new File(f + ".idx");
        File dataFile = new File(f + ".dict");

        byte[] idxBytes = readFile(idxFile);
        byte[] dataBytes = readFile(dataFile);

        DataInputStream idx = new DataInputStream(new ByteArrayInputStream(
                idxBytes));

        ByteArrayOutputStream mem = new ByteArrayOutputStream();
        while (true) {
            int b = idx.read();
            if (b == -1) {
                break;
            }
            if (b == 0) {
                String key = new String(mem.toByteArray(), 0, mem.size(), UTF8);
                mem.reset();
                int bodyOffset = idx.readInt();
                int bodyLength = idx.readInt();
                String text = readArticleText(dataBytes, bodyOffset, bodyLength);
                result.put(key, text);
            } else {
                mem.write(b);
            }
        }

        return result;
    }

    /**
     * Get one article.
     */
    public String readArticle(String word, Object acticleData) {
        return (String) acticleData;
    }

    /**
     * Load acticle's text.
     */
    private String readArticleText(byte[] data, int off, int len)
            throws UnsupportedEncodingException {
        return new String(data, off, len, UTF8).replace("\n", "<br>");
    }

    /**
     * Reads plain file or compressed with .gz and .dz suffixes.
     * 
     * @param file
     *            file to read without suffixes
     * @return
     */
    private byte[] readFile(File file) throws IOException {
        File gzFile;
        InputStream in;
        if (file.exists()) {
            in = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE);
        } else {
            gzFile = new File(file.getPath() + ".gz");
            if (!gzFile.exists()) {
                gzFile = new File(file.getPath() + ".dz");
            }
            if (gzFile.exists()) {
                in = new GZIPInputStream(new BufferedInputStream(
                        new FileInputStream(gzFile), BUFFER_SIZE));
            } else {
                throw new FileNotFoundException(file.getPath());
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(BUFFER_SIZE);
        try {
            LFileCopy.copy(in, out);
        } finally {
            in.close();
        }
        return out.toByteArray();
    }

    /**
     * Read header.
     */
    private Map<String, String> readIFO(File ifoFile) throws Exception {
        BufferedReader rd = new BufferedReader(new InputStreamReader(
                new FileInputStream(ifoFile), UTF8));
        try {
            String line = null;
            String first = rd.readLine();
            if (!"StarDict's dict ifo file".equals(first)) {
                throw new Exception("Invalid header of .ifo file: " + first);
            }
            Map<String, String> result = new TreeMap<String, String>();
            while ((line = rd.readLine()) != null) {
                if (line.trim().length() == 0) {
                    continue;
                }
                int pos = line.indexOf('=');
                if (pos < 0) {
                    throw new Exception("Invalid format of .ifo file: " + line);
                }
                result.put(line.substring(0, pos), line.substring(pos + 1));
            }
            return result;
        } finally {
            rd.close();
        }
    }
}
