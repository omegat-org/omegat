/*
 *  OmegaT - Computer Assisted Translation (CAT) tool
 *           with fuzzy matching, translation memory, keyword search,
 *           glossaries, and translation leveraging into updated projects.
 *
 *  Copyright (C) 2023-2025 Hiroshi Miura.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import tokyo.northside.tipoftheday.data.HtmlTipData;
import tokyo.northside.tipoftheday.tips.DefaultTip;
import tokyo.northside.tipoftheday.tips.Tip;
import tokyo.northside.tipoftheday.tips.TipOfTheDayModel;

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
        try (InputStream is = TipOfTheDayUtils.getIndexStream()) {
            JsonNode data = mapper.readTree(is);
            if (data != null) {
                data.get("tips").forEach(this::addIfExist);
            }
        } catch (IOException e) {
            Log.log(e);
        }
    }

    private void addIfExist(JsonNode tip) {
        String title = tip.get("name").asText();
        String filename = tip.get("file").asText();
        URI uri = TipOfTheDayUtils.getTipsFileURI(filename);
        try (InputStream is = uri.toURL().openStream()) { // validate exists
            tips.add(DefaultTip.of(title, HtmlTipData.from(uri)));
        } catch (IOException e) {
            Log.logWarningRB("TIPOFTHEDAY_FILE_NOT_FOUND", filename);
        }
    }
}
