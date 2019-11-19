/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2015-2016 Hiroshi Miura, Aaron Madlon-Kay
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
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

    @Override
    public boolean isSupportedFile(File file) {
        return file.getPath().endsWith(".ifo");
    }

    @Override
    public IDictionary loadDict(File file) throws Exception {
        return loadDict(file, new Language(Locale.getDefault()));
    }

    @Override
    public IDictionary loadDict(File file, Language language) throws Exception {
        return new StarDictDict(file, language);
    }

    static class StarDictDict implements IDictionary {

        private final Language language;

        /**
         * Field in StarDict .ifo file, added in version 3.0.0. This must be
         * retained in order to support idxoffsetbits=64 dictionaries (not yet
         * implemented).
         *
         * @see <a href="http://www.stardict.org/StarDictFileFormat">StarDict
         *      File Format</a>
         */
        private int idxoffsetbits = 32;

        private final String dictName;
        private final DictType dictType;
        private final String dataFile;

        protected final DictionaryData<Entry> data;

        /**
         * @param ifoFile
         *            ifo file with dictionary
         */
        StarDictDict(File ifoFile, Language language) throws Exception {

            this.language = language;

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

            try {
                dataFile = getFile(".dict.dz", ".dict").get().getPath();
                dictType = dataFile.endsWith(".dz") ? DictType.DICTZIP : DictType.DICTFILE;
            } catch (NoSuchElementException ex) {
                throw new FileNotFoundException("No .dict.dz or .dict files were found for " + dictName);
            }

            try {
                data = loadData(getFile(".idx.gz", ".idx").get());
            } catch (NoSuchElementException ex) {
                throw new FileNotFoundException("No .idx file could be found");
            }
        }

        private Optional<File> getFile(String... suffixes) {
            return Stream.of(suffixes).map(suff -> new File(dictName + suff)).filter(f -> f.isFile())
                    .findFirst();
        }

        private DictionaryData<Entry> loadData(File idxFile) throws IOException {
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

        @Override
        public List<DictionaryEntry> readArticles(String word) throws Exception {
            return data.lookUp(word).stream().map(e -> new DictionaryEntry(e.getKey(), e.getValue().getArticle()))
                    .collect(Collectors.toList());
        }

        @Override
        public List<DictionaryEntry> readArticlesPredictive(String word) {
            return data.lookUpPredictive(word).stream()
                    .map(e -> new DictionaryEntry(e.getKey(), e.getValue().getArticle())).collect(Collectors.toList());
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
                result = new String(data, 0, readLen, StandardCharsets.UTF_8);
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
                result = new String(data, StandardCharsets.UTF_8);
            } catch (IOException e) {
                Log.log(e);
            }
            return result;
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

        class Entry {
            private volatile String cache;
            private final int start;
            private final int len;

            Entry(int start, int len) {
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
