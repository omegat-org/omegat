/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023 Hiroshi Miura.
 *                Home page: https://www.omegat.org/
 *                Support center: https://omegat.org/support
 *
 *  This file is part of OmegaT.
 *
 *  OmegaT is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  OmegaT is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.omegat.gui.tipoftheday;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import tokyo.northside.swing.data.HtmlTipData;
import tokyo.northside.swing.tips.DefaultTip;
import tokyo.northside.swing.tips.Tip;
import tokyo.northside.swing.tips.TipOfTheDayModel;

import org.omegat.util.Log;
import org.omegat.util.StaticUtils;

public class OmegaTTipOfTheDayModel implements TipOfTheDayModel {

    private final String indexYaml = "tips.yaml";
    private final List<Tip> tips;
    private ObjectMapper mapper;

    public OmegaTTipOfTheDayModel() {
        tips = new ArrayList<>();
        mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        initTips();
    }

    @Override
    public Tip getTipAt(int index) {
        return tips.get(index);
    }

    @Override
    public int getTipCount() {
        return tips.size();
    }

    private void initTips() {
        try (InputStream is = getIndexStream(indexYaml)) {
            Records data = mapper.readValue(is, Records.class);
            if (data != null) {
                data.tips.stream().forEach(tip -> addIfExist(tip.name, tip.file));
            }
        } catch (IOException e) {
            Log.log(e);
        }
    }

    private void addIfExist(String title, String filename) {
        URI uri = getTipsFileURI(filename);
        if (uri == null) {
            Log.logWarningRB("TIPOFTHEDAY_FILE_NOT_FOUND", filename);
            return;
        }
        try {
            boolean ignored = tips.add(DefaultTip.of(title, HtmlTipData.from(uri)));
        } catch (IOException e) {
            Log.logWarningRB("TIPOFTHEDAY_FILE_LOAD_EXCEPTION", e);
        }
    }

    private static URI getTipsFileURI(String filename) {
        return getTipsFileURI(filename, getLocale());
    }

    private static String getLocale() {
        // Get the system locale (language and country)
        String language = Locale.getDefault().getLanguage().toLowerCase(Locale.ENGLISH);
        String country = Locale.getDefault().getCountry().toUpperCase(Locale.ENGLISH);
        String lang;
        if (language.equals("zh") || country.equals("BR")) {
            lang = language + "_" + country;
        } else {
            lang = language;
        }
        return lang;
    }

    private static URI getTipsFileURI(String filename, String lang) {
        String installDir = StaticUtils.installDir();
        File file = Paths.get(installDir, "docs", "tips", lang, filename).toFile();
        if (file.isFile()) {
            return file.toURI();
        }
        // find in classpath
        URL url = OmegaTTipOfTheDayModel.class.getResource("/tips/" + lang + '/' + filename);
        if (url != null) {
            try {
                return url.toURI();
            } catch (URISyntaxException ignored) {
            }
        }
        return null;
    }

    private static InputStream getIndexStream(String filename) throws IOException {
        String lang = getLocale();
        String installDir = StaticUtils.installDir();
        Path path = Paths.get(installDir, "docs", "tips", lang, filename);
        if (path.toFile().isFile()) {
            return Files.newInputStream(path);
        }
        return OmegaTTipOfTheDayModel.class.getResourceAsStream("/tips/" + lang + '/' + filename);
    }

    public static class Records {
        private List<TipRecord> tips;

        public List<TipRecord> getTips() {
            return tips;
        }

        public void setTips(final List<TipRecord> tips) {
            this.tips = tips;
        }
    }

    public static class TipRecord {
        private String name;
        private String file;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public String getFile() {
            return file;
        }

        public void setFile(final String file) {
            this.file = file;
        }
    }
}
