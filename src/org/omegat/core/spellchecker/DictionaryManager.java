/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2007 Zoltan Bartko
               2009-2011 Didier Briel
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

package org.omegat.core.spellchecker;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

import org.apache.commons.io.FilenameUtils;

import org.omegat.util.HttpConnectionUtils;
import org.omegat.util.OConsts;
import org.omegat.util.PatternConsts;
import org.omegat.util.Preferences;

/**
 * Dictionary manager. Spell checking dictionaries' utility functions.
 *
 * @author Zoltan Bartko - bartkozoltan@bartkozoltan.com
 * @author Didier Briel
 */
public class DictionaryManager {

    /** the directory string */
    private final File dir;

    /**
     * Creates a new instance of DictionaryManager.
     *
     * @param dir
     *            : the directory where the spell checking dictionary files
     *            (*.(aff|dic) are available locally
     */
    public DictionaryManager(File dir) {
        this.dir = dir;
    }

    /**
     * returns the dictionary directory
     */
    public String getDirectory() {
        return dir.getAbsolutePath();
    }

    /**
     * returns a list of full names of dictionaries from a dictionary code list
     */
    public List<String> getDictionaryNameList(List<String> aList) {
        List<String> result = new ArrayList<>();

        for (String dic : aList) {
            String[] parts = dic.split("_");
            Locale locale;
            if (parts.length == 1) {
                locale = new Locale(parts[0]);
            } else {
                locale = new Locale(parts[0], parts[1]);
            }
            result.add(dic + " - " + locale.getDisplayName());
        }

        return result;
    }

    /**
     * return a list of full names of the local dictionaries
     */
    public List<String> getLocalDictionaryNameList() {
        return getDictionaryNameList(getLocalDictionaryCodeList());
    }

    /**
     * returns a list of available dictionaries in the xx_YY form
     */
    public List<String> getLocalDictionaryCodeList() {
        List<String> result = new ArrayList<>();
        for (DictionaryEntry entry : getLocalDictionaryEntries()) {
            result.add(entry.languageCode);
        }
        return result;
    }

    /**
     * returns a list of available dictionaries.
     */
    public List<DictionaryEntry> getLocalDictionaryEntries() {
        List<DictionaryEntry> result = new ArrayList<>();

        // get all affix files
        String[] affixFiles = dir.list((d, name) -> name.endsWith(OConsts.SC_AFFIX_EXTENSION));

        // get all dictionary files
        String[] dictionaryFiles = dir.list((d, name) -> name.endsWith(OConsts.SC_DICTIONARY_EXTENSION));

        // match them
        if (affixFiles != null && dictionaryFiles != null) {
            for (String affixFile : affixFiles) {
                boolean match = false;

                // get the affix file name
                String affixName = FilenameUtils.getBaseName(affixFile);
                if (affixName == null || affixName.isEmpty()) {
                    continue;
                }
                // cycle through the dictionary names
                for (String dictionaryFile : dictionaryFiles) {
                    // get the dic file name
                    String dicName = FilenameUtils.getBaseName(dictionaryFile);
                    if (dicName == null || dicName.isEmpty()) {
                        continue;
                    }
                    if (affixName.equals(dicName)) {
                        match = true;
                        break;
                    }
                }

                if (match) {
                    result.add(new DictionaryEntry(affixName, SpellCheckDictionaryType.HUNSPELL));
                }
            }
        }

        String[] morfologikFiles = dir.list((d, name) -> name.endsWith(OConsts.SC_MORFOLOGIK_EXTENSION));
        if (morfologikFiles != null) {
            for (String morfologikFile : morfologikFiles) {
                String baseName = FilenameUtils.getBaseName(morfologikFile);
                result.add(new DictionaryEntry(baseName, SpellCheckDictionaryType.MORFOLOGIK));
            }
        }

        return result;
    }

    /**
     * Uninstall (delete) a given dictionary from the dictionary directory
     *
     * @param lang
     *            : the language code (xx_YY) of the dictionary to be deleted
     * @return true upon success, otherwise false
     */
    public boolean uninstallDictionary(String lang) {
        if (lang == null || lang.isEmpty()) {
            return false;
        }
        var target = getLocalDictionaryEntries().stream().filter(it -> it.languageCode.equals(lang)).findFirst();
        if (target.isPresent()) {
            String base = getDirectory() + File.separator + lang;
            if (target.get().type.equals(SpellCheckDictionaryType.HUNSPELL)) {
                File affFile = new File(base + OConsts.SC_AFFIX_EXTENSION);
                if (!affFile.delete()) {
                    return false;
                }
                File dicFile = new File(base + OConsts.SC_DICTIONARY_EXTENSION);
                return dicFile.delete();
            } else {
                File dictFile = new File(base + OConsts.SC_MORFOLOGIK_EXTENSION);
                return dictFile.delete();
            }
        }
        return false;
    }

    /**
     * return a list of names of installable dictionaries (e.g. en_US - english
     * (USA))
     */
    public List<String> getInstallableDictionaryNameList() throws IOException {
        return getDictionaryNameList(getInstallableDictionaryCodeList());
    }

    /**
     * returns a list of codes (xx_YY) of installable dictionaries
     */
    public List<String> getInstallableDictionaryCodeList() throws IOException {
        List<String> localDicList = getLocalDictionaryCodeList();

        List<String> remoteDicList = getRemoteDictionaryCodeList();

        List<String> result = new ArrayList<>();

        // compare the two lists
        for (String dicCode : remoteDicList) {
            if (!localDicList.contains(dicCode)) {
                result.add(dicCode);
            }
        }

        return result;
    }

    /**
     * downloads the list of available dictionaries from the net
     */
    private List<String> getRemoteDictionaryCodeList() throws IOException {
        List<String> result = new ArrayList<>();

        // download the file
        URL url = new URL(Preferences.getPreference(Preferences.SPELLCHECKER_DICTIONARY_URL));
        String htmlfile = HttpConnectionUtils.getURL(url);

        // build a list of available language codes
        Matcher matcher = PatternConsts.DICTIONARY_ZIP.matcher(htmlfile);

        while (matcher.find()) {
            // strip the quotes from the ends
            String match = matcher.group();
            int dotPosition = match.indexOf(".");
            // delete the '.zip"'
            result.add(match.substring(1, dotPosition));
        }

        return result;
    }

    /**
     * installs a remote dictionary by downloading the corresponding zip file
     * from the net and by installing the aff and dic file to the dictionary
     * directory.
     *
     * @param langCode
     *            : the language code (xx_YY)
     */
    public void installRemoteDictionary(String langCode) throws IOException {
        String from = Preferences.getPreference(Preferences.SPELLCHECKER_DICTIONARY_URL) + "/" + langCode + ".zip";

        // Dirty hack for the French dictionary. Since it is named
        // fr_FR_1-3-2.zip, we remove the "_1-3-2" portion
        // [ 2138846 ] French dictionary cannot be downloaded and installed
        int pos = langCode.indexOf("_1-3-2", 0);
        if (pos != -1) {
            langCode = langCode.substring(0, pos);
        }
        List<String> expectedFiles = Arrays.asList(langCode + OConsts.SC_AFFIX_EXTENSION,
                langCode + OConsts.SC_DICTIONARY_EXTENSION);
        HttpConnectionUtils.downloadZipFileAndExtract(new URL(from), dir, expectedFiles);
    }

    public static class DictionaryEntry {
        public String languageCode;
        public SpellCheckDictionaryType type;

        public DictionaryEntry(String languageCode, SpellCheckDictionaryType type) {
            this.languageCode = languageCode;
            this.type = type;
        }
    }

}
