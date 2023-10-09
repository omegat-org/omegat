/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2008 Alex Buloichik
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

package org.omegat.filters;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

import org.omegat.core.Core;
import org.omegat.core.data.IProject;
import org.omegat.filters2.IAlignCallback;
import org.omegat.filters2.IFilter;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.text.bundles.ResourceBundleFilter;

public class ResourceBundleFilterTest extends TestFilterBase {
    @Test
    public void testParse() throws Exception {
        parse(new ResourceBundleFilter(),
                "test/data/filters/resourceBundle/file-ResourceBundleFilter.properties");
    }

    @Test
    public void testTranslate() throws Exception {
        Map<String, String> options = new TreeMap<>();
        options.put(ResourceBundleFilter.OPTION_FORCE_JAVA8_LITERALS_ESCAPE, "true");
        translateText(new ResourceBundleFilter(),
                "test/data/filters/resourceBundle/file-ResourceBundleFilter.properties", options);
    }

    @Test
    public void testAlign() throws Exception {
        final AlignResult ar = new AlignResult();
        align(new ResourceBundleFilter(), "resourceBundle/file-ResourceBundleFilter.properties",
                "resourceBundle/file-ResourceBundleFilter_be.properties", new IAlignCallback() {
                    public void addTranslation(String id, String source, String translation, boolean isFuzzy,
                            String path, IFilter filter) {
                        ar.found = id.equals("ID") && source.equals("Value") && translation.equals("test");
                    }
                });
        assertTrue(ar.found);
    }

    public static class AlignResult {
        boolean found = false;
    }

    @Test
    public void testLoad() throws Exception {
        String f = "test/data/filters/resourceBundle/file-ResourceBundleFilter.properties";
        ResourceBundleFilter filter = new ResourceBundleFilter();
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("Value", "ID", null, null, null, null);
        checkMulti("Value2", "ID2", null, null, null, null);
        checkMulti("Value3", "ID3", null, null, null, "# some comment");
        checkMulti("Value4", "ID4", null, null, null, "# multiple line\n# comment");
        checkMulti("Value5", "ID5", null, null, null, "! alternate comment style");
        checkMulti("Value\u2603", "ID6", null, null, null, "# Unicode escape \u2603"); // U+2603 SNOWMAN
        checkMultiEnd();

        f = "test/data/filters/resourceBundle/file-ResourceBundleFilter-SMP.properties";
        fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("\uD835\uDC00\uD835\uDC01\uD835\uDC02", "ID", null, null, null, null);
        checkMulti("\uD835\uDC03\uD835\uDC04\uD835\uDC05", "ID2", null, null, null, null);
        checkMultiEnd();
    }

    @Test
    public void testDoNotEscapeUnicodeLiterals() throws Exception {
        String f = "test/data/filters/resourceBundle/file-ResourceBundleFilter-UnicodeLiterals.properties";
        ResourceBundleFilter filter = new ResourceBundleFilter();
        Map<String, String> options = new HashMap<String, String>();
        options.put(ResourceBundleFilter.OPTION_DONT_UNESCAPE_U_LITERALS, "true");
        IProject.FileInfo fi = loadSourceFiles(filter, f, options);

        checkMultiStart(fi, f);
        checkMulti("a\nb\\u0020\\ad", "MU", null, null, null, "# \\u00ad");
        checkMultiEnd();

        translateText(filter, f, options);
    }

    @Test
    public void testBadUnicodeLiterals() throws Exception {
        String base = "test/data/filters/resourceBundle/";
        ResourceBundleFilter filter = new ResourceBundleFilter();
        try {
            loadSourceFiles(filter, base + "file-ResourceBundleFilter-BadLiteral1.properties");
            fail("Failed to catch invalid Unicode literal: too short");
        } catch (TranslationException ex) {
        }
        try {
            loadSourceFiles(filter, base + "file-ResourceBundleFilter-BadLiteral2.properties");
        } catch (TranslationException ex) {
            fail("Actual Java ResourceBundle loader doesn't prevent you from including characters "
                    + "for which Character.isDefined() returns false.");
        }
        try {
            loadSourceFiles(filter, base + "file-ResourceBundleFilter-BadLiteral3.properties");
            fail("Failed to catch invalid Unicode literal: not hex code");
        } catch (TranslationException ex) {
        }
    }

    @Test
    public void testWhiteSpace() throws Exception {
        // We want to see full whitespace for this test
        boolean removeSpacesOrig = Core.getFilterMaster().getConfig().isRemoveSpacesNonseg();
        Core.getFilterMaster().getConfig().setRemoveSpacesNonseg(false);

        String f = "test/data/filters/resourceBundle/file-ResourceBundleFilter-WhiteSpace.properties";
        ResourceBundleFilter filter = new ResourceBundleFilter();
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("Value", "KEY", null, null, null, "# Tab->\t<-Tab");
        checkMulti("Value    ", "KEY2", null, null, null, "# Trailing whitespace must be preserved");
        checkMulti("Value1\tValue2", "KEY3", null, null, null, "# Significant whitespace on continuation line");
        checkMulti("Value1 Value2", "KEY4", null, null, null, null);
        checkMultiEnd();

        translate(filter, f);
        compareBinary(new File("test/data/filters/resourceBundle/file-ResourceBundleFilter-WhiteSpace-gold.properties"),
                outFile);

        // Restore old value
        Core.getFilterMaster().getConfig().setRemoveSpacesNonseg(removeSpacesOrig);
    }

    @Test
    public void testNOI18N() throws Exception {
        String f = "test/data/filters/resourceBundle/file-ResourceBundleFilter-NOI18N.properties";
        ResourceBundleFilter filter = new ResourceBundleFilter();
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("Value", "ID", null, null, null, null);
        checkMultiEnd();

        translateText(filter, f);
    }

    @Test
    public void testCommentEscaping() throws Exception {
        String f = "test/data/filters/resourceBundle/file-ResourceBundleFilter-Comments.properties";
        ResourceBundleFilter filter = new ResourceBundleFilter();
        IProject.FileInfo fi = loadSourceFiles(filter, f);

        checkMultiStart(fi, f);
        checkMulti("Value", "ID", null, null, null, "# Foo\\");
        checkMultiEnd();

        translateText(filter, f);
    }

    @Test
    public void testRegressionGithub227() throws Exception {
        String f = "test/data/filters/resourceBundle/file-ResourceBundleFilter-NonASCIIComments.properties";
        ResourceBundleFilter filter = new ResourceBundleFilter();

        assertTrue(filter.isSourceEncodingVariable());
        Map<String, String> config = new HashMap<>();
        config.put(ResourceBundleFilter.OPTION_FORCE_JAVA8_LITERALS_ESCAPE, "false");
        IProject.FileInfo fi = loadSourceFiles(filter, f, config);
        checkMultiStart(fi, f);
        checkMulti("Unpack project from OMT file...", "omt.menu.import", null, null, null,
                "#/**************************************************************************\n" +
                        "# OmegaT Plugin - OMT Package Manager\n" +
                        "#\n" +
                        "# Copyright (C) 2019 Briac Pilpr\u00E9\n" +
                        "# Home page: http://www.omegat.org/\n" +
                        "# Support center: http://groups.yahoo.com/group/OmegaT/\n" +
                        "#\n" +
                        "# This program is free software: you can redistribute it and/or modify\n" +
                        "# it under the terms of the GNU General Public License as published by\n" +
                        "# the Free Software Foundation, either version 3 of the License, or\n" +
                        "# (at your option) any later version.\n" +
                        "#\n" +
                        "# This program is distributed in the hope that it will be useful,\n" +
                        "# but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
                        "# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" +
                        "# GNU General Public License for more details.\n" +
                        "#\n" +
                        "# You should have received a copy of the GNU General Public License\n" +
                        "# along with this program. If not, see <http://www.gnu.org/licenses/>.\n" +
                        "#\n" +
                        "# **************************************************************************/"
        );
        checkMulti("Pack project as OMT file...", "omt.menu.export", null, null, null, null);
        checkMulti("Pack and delete project...", "omt.menu.export.delete", null, null, null, null);
        checkMulti("The project already has an ongoing translation.\nDo you want to overwrite it with the translation from the package ?",
                "omt.dialog.overwrite_project_save", null, null, null, null);
        checkMulti("Deleting project...", "omt.status.delete_project", null, null, null, null);
        checkMultiEnd();

        translateText(filter, f, config);
    }
}
