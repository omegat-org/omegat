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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import tokyo.northside.swing.data.HtmlTipData;
import tokyo.northside.swing.tips.DefaultTip;
import tokyo.northside.swing.tips.Tip;
import tokyo.northside.swing.tips.TipOfTheDayModel;

import org.omegat.util.Log;

public final class OmegaTTipOfTheDayModel implements TipOfTheDayModel {

    private final List<Tip> tips;
    private final ObjectMapper mapper;

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
        try (InputStream is = TipOfTheDayUtils.getIndexStream(TipOfTheDayController.INDEX_YAML)) {
            if (is == null) {
                return;
            }
            Records data = mapper.readValue(is, Records.class);
            if (data != null) {
                data.tips.forEach(this::addIfExist);
            }
        } catch (IOException e) {
            Log.log(e);
        }
    }

    private void addIfExist(TipRecord tip) {
        String title = tip.name;
        String filename = tip.file;
        URI uri = TipOfTheDayUtils.getTipsFileURI(filename);
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

    public static class Records {
        private List<TipRecord> tips;

        /**
         * Return list of TipRecord object.
         * @return list of TipRecord object.
         */
        public List<TipRecord> getTips() {
            return tips;
        }

        /**
         * Set list of TipRecord object.
         * @param tips list of TipRecord object to set.
         */
        public void setTips(final List<TipRecord> tips) {
            this.tips = tips;
        }
    }

    public static class TipRecord {
        private String name;
        private String file;

        /**
         * Get a name of Tip record.
         * @return name.
         */
        public String getName() {
            return name;
        }

        /**
         * Set a name of Tip record.
         * @param name to set.
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * Get a HTML file.
         * @return a HTML file.
         */
        public String getFile() {
            return file;
        }

        /**
         * set a HTML file.
         * @param file to set.
         */
        public void setFile(final String file) {
            this.file = file;
        }
    }
}
