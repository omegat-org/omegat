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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.dict.zip.DictZipInputStream;
import org.dict.zip.RandomAccessInputStream;
import org.omegat.util.Log;
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
public class StarDict implements IDictionaryFactory {

    private enum DictType {
        DICTZIP,
        DICTFILE
    }

    private static final int BUFFER_SIZE = 64 * 1024;

    @Override
    public boolean isSupportedFile(File file) {
        return file.getPath().endsWith(".ifo");
    }

    @Override
    public IDictionary loadDict(File file) throws Exception {
        return new StarDictDict(file);
    }

    static class StarDictDict implements IDictionary {

        /**
         * Field in StarDict .ifo file, added in version 3.0.0. This must be
         * retained in order to support idxoffsetbits=64 dictionaries (not yet
         * implemented).
         * 
         * @see <a href="http://www.stardict.org/StarDictFileFormat">StarDict
         *      File Format</a>
         */
        private int idxoffsetbits = 32;

        private DictType dictType;
        private String dictName;
        private String dataFile;

        protected final Map<String, Object> data;

        /**
         * @param ifoFile
         *            ifo file with dictionary
         */
        public StarDictDict(File ifoFile) throws Exception {
    
            Map<String, String> header = readIFO(ifoFile);
            String version = header.get("version");
            if (!"2.4.2".equals(version) && !"3.0.0".equals(version)) {
                throw new Exception("Invalid version of dictionary: " + version);
            }
            String sametypesequence = header.get("sametypesequence");
            if (!"g".equals(sametypesequence) && 
                !"m".equals(sametypesequence) && 
                !"x".equals(sametypesequence) &&
                !"h".equals(sametypesequence)) {
                throw new Exception("Invalid type of dictionary: " + sametypesequence);
            }
            
            if ("3.0.0".equals(version)) {
                String bitsString = header.get("idxoffsetbits");
                if (bitsString != null) {
                    idxoffsetbits = Integer.parseInt(bitsString);
                }
            }
    
            if (idxoffsetbits != 32) {
                throw new Exception("StarDict dictionaries with idxoffsetbits=64 are not supported.");
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

            data = readHeader();
        }

        private Map<String, Object> readHeader() throws IOException {
            File file = new File(dictName + ".idx");
            byte[] idxBytes;
            if (file.exists()) {
                idxBytes = readIDX(file);
            } else {
                file = new File(dictName + ".idx.gz");
                if (file.exists()) {
                    idxBytes = readIDXGZ(file);
                } else {
                    throw new FileNotFoundException("No .idx file could be found");
                }
            }

            Map<String, Object> result = new HashMap<>();
            try (ByteArrayInputStream bais = new ByteArrayInputStream(idxBytes);
                  DataInputStream idx = new DataInputStream(bais);
                  ByteArrayOutputStream mem = new ByteArrayOutputStream()) {
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
            }
            return result;
        }

        /**
         * Add new index to dictionary map. If index for this words was already
         * added, it create array with all indexes. It required to support
         * multiple translations for one word in dictionary.
         * 
         * @param key
         *            translated word
         * @param start
         *            offset article index
         * @param len
         *            article offset
         * @param result
         *            result map
         */
        private void addIndex(final String key, final int start, final int len, final Map<String, Object> result) {
            Object data = result.get(key);
            if (data == null) {
                data = new Entry(start, len);
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
         * 
         * @see org.omegat.core.dictionaries.IDictionary#readArticle(java.lang.
         * String, java.lang.Object)
         * 
         * Returns not the raw text, but the formatted article ready for
         * upstream use (\n replaced with <br>, etc.
         */
        @Override
        public List<DictionaryEntry> readArticles(String word) {
            Object dictData = data.get(word);
            if (dictData == null) {
                return Collections.emptyList();
            }
            List<DictionaryEntry> result = new ArrayList<>();
            if (dictData instanceof Entry) {
                result.add(new DictionaryEntry(word, ((Entry) dictData).getArticle()));
            } else if (dictData instanceof Entry[]) {
                for (Entry entry : (Entry[]) dictData) {
                    result.add(new DictionaryEntry(word, entry.getArticle()));
                }
            }
            return result;
        }

        /**
         * Read an article from the data file on disk. Convenience method that
         * dispatches on {@link #dictType} to call the appropriate
         * format-specific method.
         * <p>
         * Synchronized to prevent concurrent reading of the same file from
         * disk.
         * 
         * @param start
         *            Start offset in data file
         * @param len
         *            Length of article data
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
         * Read .dict file data and return article string. Intended to be called
         * only from {@link #readArticle(int, int)}.
         * 
         * @param start
         *            Start offset in data file
         * @param len
         *            Length of article data
         * @return Raw article text
         */
        private String readDictArticleText(int start, int len) {
            String result = null;
            try (FileInputStream in = new FileInputStream(dataFile)) {
                byte[] data = new byte[len];
                long moved = in.skip(start);
                if (moved < start) {
                    long moved2 = in.skip(start - moved);
                    if (moved2 < start - moved) {
                        throw new IOException("Cannot seek dictionary.");
                    }
                }
                int readLen = in.read(data);
                result = new String(data, 0, readLen, OConsts.UTF8);
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
            return result;
        }


        /**
         * Read .dict.dz file data. Intended to be called only from
         * {@link #readArticle(int, int)}.
         *
         * @param start
         *            Start offset in data file
         * @param len
         *            Length of article data
         * @return Raw article text
         */
        private String readDictZipArticleText(int start, int len) {
            String result = null;
            try (DictZipInputStream din = new DictZipInputStream(new
                    RandomAccessInputStream(dataFile, "r"))) {
                din.seek(start);
                byte[] data = new byte[len];
                din.readFully(data);
                result = new String(data, OConsts.UTF8);
                din.close();
            } catch (IOException e) {
                Log.log(e);
            }
            return result;
        }

        /**
         * Reads plain idx file.
         * 
         * @param file
         *            file to read.
         * @return byte array which contents is IDX file.
         * @throws IOException if I/O error occurred.
         */
        private byte[] readIDX(File file) throws IOException {
            byte[] result;
            try ( FileInputStream fis = new FileInputStream(file);
                  BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE)) {
                result = IOUtils.toByteArray(bis);
            }
            return result;
        }

        /**
         * Reads Idx.gz file.
         * @param file to read.
         * @return byte array which contents is IDX file.
         * @throws IOException if I/O error occurred.
         */
        private byte[] readIDXGZ(File file) throws IOException {
            byte[] result;
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis, BUFFER_SIZE);
                 InputStream is = new GZIPInputStream(bis)) {
                result = IOUtils.toByteArray(is);
            }
            return result;
        }

        /**
         * Read header.
         */
        private Map<String, String> readIFO(File ifoFile) throws Exception {
            Map<String, String> result = new TreeMap<>();
            try (FileInputStream fis = new FileInputStream(ifoFile);
                    InputStreamReader isr = new InputStreamReader(fis, OConsts.UTF8);
                    BufferedReader rd = new BufferedReader(isr)) {
                String line;
                String first = rd.readLine();
                if (!"StarDict's dict ifo file".equals(first)) {
                    throw new Exception("Invalid header of .ifo file: " + first);
                }
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
            }
            return result;
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
}
