/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2015-2016 Hiroshi Miura, Aaron Madlon-Kay
               2020 Suguru Oho, Aaron Madlon-Kay
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

package org.omegat.core.dictionaries;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import org.dict.zip.DictZipInputStream;
import org.dict.zip.RandomAccessInputStream;
import org.omegat.util.Language;
import org.omegat.util.Log;

/**
 * Dictionary implementation for StarDict format.
 * <p>
 * StarDict format described on
 * https://github.com/huzheng001/stardict-3/blob/master/dict/doc/StarDictFileFormat
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
 * @author Suguru Oho
 */
public class StarDict implements IDictionaryFactory {

    @Override
    public boolean isSupportedFile(File file) {
        return file.getPath().endsWith(".ifo");
    }

    @Override
    public IDictionary loadDict(File file) throws Exception {
        return loadDict(file, new Language(Locale.getDefault()));
    }

    @Override
    public IDictionary loadDict(File ifoFile, Language language) throws Exception {
        Map<String, String> header = readIFO(ifoFile);
        String version = header.get("version");
        if (!"2.4.2".equals(version) && !"3.0.0".equals(version)) {
            throw new Exception("Invalid version of dictionary: " + version);
        }
        String sametypesequence = header.get("sametypesequence");
        if (!"g".equals(sametypesequence)
                && !"m".equals(sametypesequence)
                && !"x".equals(sametypesequence)
                && !"h".equals(sametypesequence)) {
            throw new Exception("Invalid type of dictionary: " + sametypesequence);
        }

        /*
         * Field in StarDict .ifo file, added in version 3.0.0. This must be
         * retained in order to support idxoffsetbits=64 dictionaries (not yet
         * implemented).
         */
        int idxoffsetbits = 32;
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
        String dictName = f;

        File idxFile = getFile(dictName, ".idx.gz", ".idx")
                .orElseThrow(() -> new FileNotFoundException("No .idx file could be found"));
        DictionaryData<Entry> data = loadData(idxFile, language);

        File dictFile = getFile(dictName, ".dict.dz", ".dict")
                .orElseThrow(() -> new FileNotFoundException("No .dict.dz or .dict files were found for " + dictName));

        try {
            if (dictFile.getName().endsWith(".dz")) {
                DictZipInputStream dataFile = new DictZipInputStream(new RandomAccessInputStream(new RandomAccessFile(dictFile, "r")));
                return new StarDictZipDict(dataFile, data);
            } else {
                RandomAccessFile dataFile = new RandomAccessFile(dictFile, "r");
                return new StarDictFileDict(dataFile, data);
            }
        } catch (FileNotFoundException ex) {
            throw new FileNotFoundException("No .dict.dz or .dict files were found for " + dictName);
        }
    }

    /**
     * Read header.
     */
    private Map<String, String> readIFO(File ifoFile) throws Exception {
        Map<String, String> result = new TreeMap<>();
        try (BufferedReader rd = Files.newBufferedReader(ifoFile.toPath(), StandardCharsets.UTF_8)) {
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

    private Optional<File> getFile(String basename, String... suffixes) {
        return Stream.of(suffixes).map(suff -> new File(basename + suff)).filter(f -> f.isFile())
                .findFirst();
    }

    private DictionaryData<Entry> loadData(File idxFile, Language language) throws IOException {
        InputStream is = new FileInputStream(idxFile);
        if (idxFile.getName().endsWith(".gz")) {
            // BufferedInputStream.DEFAULT_BUFFER_SIZE = 8192
            is = new GZIPInputStream(is, 8192);
        }
        DictionaryData<Entry> newData = new DictionaryData<>(language);
        try (DataInputStream idx = new DataInputStream(new BufferedInputStream(is));
              ByteArrayOutputStream mem = new ByteArrayOutputStream()) {
            while (true) {
                int b = idx.read();
                if (b == -1) {
                    break;
                }
                if (b == 0) {
                    String key = new String(mem.toByteArray(), 0, mem.size(), StandardCharsets.UTF_8);
                    mem.reset();
                    int bodyOffset = idx.readInt();
                    int bodyLength = idx.readInt();
                    newData.add(key, new Entry(bodyOffset, bodyLength));
                } else {
                    mem.write(b);
                }
            }
        }
        is.close();
        newData.done();
        return newData;
    }

    /**
     * Simple container for offsets+lengths of entries in StarDict dictionary.
     * Subclasses of StarDictDict know how to read this from the underlying data
     * file.
     */
    static class Entry {
        private final int start;
        private final int len;

        Entry(int start, int len) {
            this.start = start;
            this.len = len;
        }

        @Override
        public int hashCode() {
            return Objects.hash(len, start);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Entry other = (Entry) obj;
            return len == other.len && start == other.start;
        }
    }

    static abstract class StarDictBaseDict implements IDictionary {

        protected final DictionaryData<Entry> data;

        private final Map<Entry, String> cache = new HashMap<>();

        /**
         * @param data collection of <code>Entry</code>s loaded from file
         */
        StarDictBaseDict(DictionaryData<Entry> data) {
            this.data = data;
        }

        @Override
        public List<DictionaryEntry> readArticles(String word) throws Exception {
            return data.lookUp(word).stream().map(e -> new DictionaryEntry(e.getKey(), getArticle(e.getValue())))
                    .collect(Collectors.toList());
        }

        @Override
        public List<DictionaryEntry> readArticlesPredictive(String word) {
            return data.lookUpPredictive(word).stream()
                    .map(e -> new DictionaryEntry(e.getKey(), getArticle(e.getValue()))).collect(Collectors.toList());
        }

        private synchronized String getArticle(Entry entry) {
            return cache.computeIfAbsent(entry, (e) -> {
                return readArticle(e.start, e.len).replace("\n", "<br>");
            });
        }

        /**
         * Read data from the underlying file.
         *
         * @param start
         *            Start offset in data file
         * @param len
         *            Length of article data
         * @return Raw article text
         */
        protected abstract String readArticle(int start, int len);
    }

    static private class StarDictFileDict extends StarDictBaseDict {
        private final RandomAccessFile dataFile;

        public StarDictFileDict(RandomAccessFile dataFile, DictionaryData<Entry> data) {
            super(data);
            this.dataFile = dataFile;
        }

        @Override
        protected String readArticle(int start, int len) {
            String result = null;
            try {
                byte[] data = new byte[len];
                dataFile.seek(start);
                int readLen = dataFile.read(data);
                result = new String(data, 0, readLen, StandardCharsets.UTF_8);
            } catch (IOException e) {
                Log.log(e);
            }
            return result;
        }

        @Override
        public void close() throws IOException {
            dataFile.close();
        }
    }

    static private class StarDictZipDict extends StarDictBaseDict {
        private final DictZipInputStream dataFile;

        public StarDictZipDict(DictZipInputStream dataFile, DictionaryData<Entry> data) {
            super(data);
            this.dataFile = dataFile;
        }

        @Override
        protected String readArticle(int start, int len) {
            String result = null;
            try {
                dataFile.seek(start);
                byte[] data = new byte[len];
                dataFile.readFully(data);
                result = new String(data, StandardCharsets.UTF_8);
            } catch (IOException e) {
                Log.log(e);
            }
            return result;
        }

        @Override
        public void close() throws IOException {
            dataFile.close();
        }
    }
}
