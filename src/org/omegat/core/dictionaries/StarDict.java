/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
               2015-2016 Hiroshi Miura, Aaron Madlon-Kay
               2020 Suguru Oho, Aaron Madlon-Kay
               2022 Hiroshi Miura
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

import java.io.File;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import io.github.eb4j.stardict.StarDictDictionary;

import org.omegat.util.Language;

/**
 * Stardict Dictionary support.
 * <p>
 * StarDict format described on
 * https://github.com/huzheng001/stardict-3/blob/master/dict/doc/StarDictFileFormat
 * <p>
 * A stardict dictionary plugin uses Stardict4j access library.
 * Every dictionary consists of these files:
 * <ol><li>somedict.ifo
 * <li>somedict.idx or somedict.idx.gz
 * <li>somedict.dict or somedict.dict.dz
 * <li>somedict.syn (optional)
 * </ol>
 *
 * This driver handle entry types 'm': mean, 'h': html and 't': phonetics.
 * Other entries such as mediawiki markup are not shown.
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
    public IDictionary loadDict(final File file, final Language language) throws Exception {
        return new StarDictDict(file, language);
    }

    static class StarDictDict implements IDictionary {

        protected final StarDictDictionary dictionary;
        protected final Language language;

        StarDictDict(final File file, Language language) throws Exception {
            dictionary = StarDictDictionary.loadDictionary(file, 1_000, Duration.ofMinutes(30));
            // Max cache size  to 1,000 items and expiry to 30 min.
            this.language = language;
        }

        @Override
        public List<DictionaryEntry> readArticles(String word) throws Exception {
            List<StarDictDictionary.Entry> result = dictionary.readArticles(word);
            if (result.isEmpty()) {
                result = dictionary.readArticles(word.toLowerCase(language.getLocale()));
            }
            return result.stream()
                    .filter(StarDictDict::useEntry)
                    .map(StarDictDict::convertEntry)
                    .collect(Collectors.toList());
        }

        @Override
        public List<DictionaryEntry> readArticlesPredictive(String word) {
            List<StarDictDictionary.Entry> result = dictionary.readArticlesPredictive(word);
            if (result.isEmpty()) {
                result = dictionary.readArticlesPredictive(word.toLowerCase(language.getLocale()));
            }
            return result.stream()
                    .filter(StarDictDict::useEntry)
                    .map(StarDictDict::convertEntry)
                    .collect(Collectors.toList());
        }

        private static boolean useEntry(StarDictDictionary.Entry entry) {
            StarDictDictionary.EntryType type = entry.getType();
            return type == StarDictDictionary.EntryType.MEAN
                    || type == StarDictDictionary.EntryType.PHONETIC
                    || type == StarDictDictionary.EntryType.HTML;
        }

        private static DictionaryEntry convertEntry(StarDictDictionary.Entry entry) {
            return new DictionaryEntry(entry.getWord(), entry.getArticle().replace("\n", "<br>"));
        }

    }
}
