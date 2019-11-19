/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Zoltan Bartko
               2016 Aaron Madlon-Kay
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

package org.omegat.gui.editor.autotext;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.omegat.util.Log;
import org.omegat.util.StaticUtils;

/**
 *
 * @author bartkoz
 * @author Aaron Madlon-Kay
 */
public final class Autotext {

    private Autotext() {
    }

    private static final String AUTOTEXT_FILE_NAME = "omegat.autotext";
    private static final File DEFAULT_FILE = new File(StaticUtils.getConfigDir(), AUTOTEXT_FILE_NAME);

    private static volatile List<AutotextItem> list = Collections.emptyList();

    static {
        if (DEFAULT_FILE.isFile()) {
            try {
                list = load(DEFAULT_FILE);
            } catch (IOException ex) {
                Log.log(ex);
            }
        }
    }

    public static List<AutotextItem> getItems() {
        return Collections.unmodifiableList(list);
    }

    public static void setList(Collection<AutotextItem> items) {
        list = new ArrayList<>(items);
    }

    public static List<AutotextItem> load(File file) throws IOException {
        return Files.lines(file.toPath()).filter(line -> !line.trim().isEmpty())
                .map(line -> line.split("\t")).filter(parts -> parts.length >= 2)
                .map(parts -> new AutotextItem(parts[0], parts[1],
                        Arrays.copyOfRange(parts, 2, parts.length)))
                .collect(Collectors.toList());
    }

    public static void save(Collection<AutotextItem> items, File file) throws IOException {
        Files.write(file.toPath(), items.stream().map(AutotextItem::toString).collect(Collectors.toList()),
                StandardCharsets.UTF_8);
    }

    public static void save() throws IOException {
        save(list, DEFAULT_FILE);
    }

    public static class AutotextItem {
        public final String source;
        public final String target;
        public final String comment;

        public AutotextItem() {
            this("", "");
        }

        public AutotextItem(String source, String target, String... comment) {
            this.source = source == null ? "" : source;
            this.target = target == null ? "" : target;
            this.comment = comment.length == 0 || comment[0] == null ? "" : comment[0];
        }

        @Override
        public String toString() {
            return source + '\t' + target + '\t' + comment;
        }
    }
}
