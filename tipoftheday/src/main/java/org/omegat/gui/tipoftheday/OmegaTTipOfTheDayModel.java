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
import org.jspecify.annotations.Nullable;
import tokyo.northside.tipoftheday.data.HtmlTipData;
import tokyo.northside.tipoftheday.tips.DefaultTip;
import tokyo.northside.tipoftheday.tips.Tip;
import tokyo.northside.tipoftheday.tips.TipOfTheDayModel;

import org.omegat.help.Help;
import org.omegat.util.Log;

import static org.omegat.gui.tipoftheday.TipOfTheDayUtils.TIPS_DIR;
import static org.omegat.gui.tipoftheday.TipOfTheDayUtils.getLocale;

/**
 * The OmegaTTipOfTheDayModel class is responsible for managing and providing
 * tips for the "Tip of the Day" feature in the OmegaT application.
 * <p>
 * This implementation retrieves tips data from a predefined JSON index file
 * and supports localized tips.
 * The class initializes the tip data upon instantiation by reading the index
 * file and validating the individual tips. Tips can be accessed in a sequential
 * and indexed manner.
 */
public final class OmegaTTipOfTheDayModel implements TipOfTheDayModel {

    private final List<Tip> tips;
    private final ObjectMapper mapper;

    public OmegaTTipOfTheDayModel() {
        tips = new ArrayList<>();
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
        initTips();
    }

    @Override
    public @Nullable Tip getTipAt(int index) {
        if (index < 0 || index >= tips.size()) {
            return null;
        }
        return tips.get(index);
    }

    @Override
    public int getTipCount() {
        return tips.size();
    }

    private void initTips() {
        try (InputStream is = TipOfTheDayUtils.getIndexStream()) {
            if (is == null) {
                return;
            }
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
        URI uri = Help.getHelpFileURI(TIPS_DIR, getLocale(), filename);
        if (isValidTipUri(uri)) {
            tips.add(DefaultTip.of(title, HtmlTipData.from(uri)));
        } else {
            Log.logWarningRB("TIPOFTHEDAY_FILE_NOT_FOUND", filename);
        }
    }

    private boolean isValidTipUri(@Nullable URI uri) {
        if (uri == null) {
            return false;
        }
        try (InputStream is = uri.toURL().openStream()) {
            return is != null;
        } catch (IOException e) {
            return false;
        }
    }
}
