/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2015 Hiroshi Miura, Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.dict.zip.DictZipHeader;
import org.dict.zip.DictZipInputStream;
import org.dict.zip.RandomAccessInputStream;
import org.omegat.util.LFileCopy;
import org.omegat.util.OConsts;

/**
 * Dictionary implementation for StarDict format.
 * <p>
 * StarDict format described on http://code.google.com/p/babiloo/wiki/StarDict_format
 * <p>
 * <h1>Files</h1>
 * Every dictionary consists of these files:
 * <ol><li>somedict.ifo
 * <li>somedict.idx or somedict.idx.gz
 * <li>somedict.dict or somedict.dict.dz
 * <li>somedict.syn (optional)
 * </ol>
 *
 * @author Alex Buloichik <alex73mail@gmail.com>
 * @author Hiroshi Miura
 * @author Aaron Madlon-Kay
 */
public class StarDict implements IDictionary {

    public enum DictType {
        DICTZIP,
        DICTFILE
    }

    protected static final int BUFFER_SIZE = 64 * 1024;

    protected final File ifoFile;

    /** Dictionary type, from 'sametypesequence' header. */
    protected final String contentType;
    private DictZipHeader fHeader;
    private DictType dictType;
    private String dictName;
    private String dataFile;

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
        if (!"g".equals(contentType) && 
            !"m".equals(contentType) && 
            !"x".equals(contentType) &&
            !"h".equals(contentType)) {
            throw new Exception("Invalid type of dictionary: " + contentType);
        }

        String f = ifoFile.getPath();
        if (f.endsWith(".ifo")) {
            f = f.substring(0, f.length() - ".ifo".length());
        }
        dictName = f;

        String dzFile = f + ".dict.dz";
        if (new File(dzFile).isFile()) {
            dictType = DictType.DICTZIP;
            dataFile = dzFile;
        } else {
            String dictFile = f + ".dict";
            if (!new File(dictFile).isFile()) {
                throw new FileNotFoundException("No .dict.dz or .dict files were found for " + dictName);
            }
            dictType = DictType.DICTFILE;
            dataFile = dictFile;
        }
    }

    @Override
    public Map<String, Object> readHeader() throws IOException {
        Map<String, Object> result = new HashMap<String, Object>();
        File file = new File(dictName + ".idx");
        byte[] idxBytes = readFile(file);

        DataInputStream idx = new DataInputStream(new ByteArrayInputStream(idxBytes));

        ByteArrayOutputStream mem = new ByteArrayOutputStream();
        while (true) {
            int b = idx.read();
            if (b == -1) {
                break;
            }
            if (b == 0) {
                String key = new String(mem.toByteArray(), 0, mem.size(), OConsts.UTF8);
                mem.reset();
                int bodyOffset = idx.readInt();
                int bodyLength = idx.readInt();
                addIndex(key, bodyOffset, bodyLength, result);
            } else {
                mem.write(b);
            }
        }
        return result;
    }

    /**
     * Add new index to dictionary map.
     * If index for this words was already added, it create array with all indexes.
     * It required to support multiple translations for one word in dictionary.
     * 
     * @param key
     *            translated word
     * @param index, offset
     *            article index and offset
     * @param result
     *            result map
     */
    private void addIndex(final String key, final int start, final int len, final Map<String, Object> result) {
        Object data = result.get(key);
        if (data == null) {
            Entry d = new Entry(start, len);
            data = d;
        } else {
            if (data instanceof Entry[]) {
                Entry[] dobj = (Entry[]) data;
                Entry[] d = new Entry[dobj.length + 1];
                System.arraycopy(dobj, 0, d, 0, dobj.length);
                d[d.length - 1] = new Entry(start, len);
                data = d;
            } else {
                Entry[] d = new Entry[2];
                d[0] = (Entry) data;
                d[1] = new Entry(start, len);
                data = d;
            }
        }
        result.put(key, data);
    }

    /*
     * (non-Javadoc)
     * @see org.omegat.core.dictionaries.IDictionary#readArticle(java.lang.String, java.lang.Object)
     * 
     * Returns not the raw text, but the formatted article ready for upstream use (\n replaced
     * with <br>, etc.
     */
    @Override
    public String readArticle(String word, Object data) {
        Entry dictData = (Entry) data;
        return dictData.getArticle();
    }

    /**
     * Read an article from the data file on disk. Convenience method
     * that dispatches on {@link #dictType} to call the appropriate
     * format-specific method.
     * <p>
     * Synchronized to prevent concurrent reading of the same file
     * from disk.
     * 
     * @param start Start offset in data file
     * @param len Length of article data
     * @return Raw article text
     */
    private synchronized String readArticle(int start, int len) {
        switch (dictType) {
        case DICTFILE:
            return readDictArticleText(start, len);
        case DICTZIP:
            return readDictZipArticleText(start, len);
        default:
            throw new RuntimeException("Unknown StarDict dictionary type: " + dictType);
        }
    }

    /**
     * Read .dict file data and return article string. Intended to be called only
     * from {@link #readArticle(int, int)}.
     * 
     * @param start Start offset in data file
     * @param len Length of article data
     * @return Raw article text
     */
    private String readDictArticleText(int start, int len) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(dataFile);
            byte[] data = new byte[len];
            in.skip(start);
            in.read(data);
            return new String(data, OConsts.UTF8);
        } catch (IOException e) {
            System.err.println(e);
            return null;
        } finally {
            try {
                in.close();
            } catch (Throwable t) {
            }
            in = null;
        }
    }

    /**
     * Return DictZip header
     *
     * DictZip has a header that indicates data offset.
     * When try retrieving data, use header information
     * as an index for random access.
     */
    private DictZipHeader getDZHeader(DictZipInputStream din) throws IOException {
        if (fHeader == null) {
            fHeader = din.readHeader();
        }
        return fHeader;
    }

    /**
     * Read .dict.dz file data. Intended to be called only from {@link #readArticle(int, int)}.
     *
     * @param start Start offset in data file
     * @param len Length of article data
     * @return Raw article text
     */
    private String readDictZipArticleText(int start, int len) {
        RandomAccessInputStream in = null;
        DictZipInputStream din = null;
        try {
            in = new RandomAccessInputStream(dataFile, "r");
            din = new DictZipInputStream(in);
            DictZipHeader h = getDZHeader(din);
            int off = h.getOffset(start);
            int pos = h.getPosition(start);
            in.seek(pos);
            byte[] b = new byte[off + len];
            din.readFully(b);
            byte[] data = new byte[len];
            System.arraycopy(b, off, data, 0, len);
            return new String(data, OConsts.UTF8);
        } catch (java.io.IOException e) {
            System.err.println(e);
            return null;
        } finally {
            try {
                din.close();
                in.close();
            } catch (Throwable t) {
            }
            din = null;
            in = null;
        }
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
            if (gzFile.exists()) {
                in = new GZIPInputStream(new BufferedInputStream(new FileInputStream(gzFile), BUFFER_SIZE));
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
        BufferedReader rd = new BufferedReader(new InputStreamReader(new FileInputStream(ifoFile), OConsts.UTF8));
        try {
            String line = null;
            String first = rd.readLine();
            if (!"StarDict's dict ifo file".equals(first)) {
                throw new Exception("Invalid header of .ifo file: " + first);
            }
            Map<String, String> result = new TreeMap<String, String>();
            while ((line = rd.readLine()) != null) {
                if (line.trim().isEmpty()) {
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
    
    private class Entry {
        private volatile String cache;
        private final int start;
        private final int len;
        
        public Entry(int start, int len) {
            this.start = start;
            this.len = len;
        }
        
        public String getArticle() {
            String article = cache;
            if (article == null) {
                synchronized (this) {
                    article = cache;
                    if (article == null) {
                        article = cache = loadArticle();
                    }
                }
            }
            return article;
        }
        
        private String loadArticle() {
            return readArticle(start, len).replace("\n", "<br>");
        }
    }
}
