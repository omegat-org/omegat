/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
               2026 Stephan Pakebusch
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

package org.omegat.core.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.jar.Manifest;

import org.junit.Test;

import org.omegat.filters2.master.PluginUtils;


/**
 * Test for PluginInformation class.
 * @author Hiroshi Miura
 */
public class PluginInformationTest {

   @Test
   public void test1() throws IOException {
      File manifest = new File("src/test/resources/data/plugin/simple/MANIFEST.MF");
      URL mu = manifest.toURI().toURL();
      try (InputStream in = Files.newInputStream(manifest.toPath())) {
         Manifest m = new Manifest(in);
         String pluginClass = m.getMainAttributes().getValue("OmegaT-Plugins");
         PluginInformation pluginInformation = PluginInformation.Builder
                 .fromManifest(pluginClass, m, mu, PluginInformation.Status.INSTALLED);
         assertEquals("Filters for OmegaT", pluginInformation.getName());
         assertEquals("Example Author", pluginInformation.getAuthor());
         assertEquals(PluginUtils.PluginType.FILTER, pluginInformation.getCategory());
         assertEquals("https://example.com", pluginInformation.getLink());
         assertEquals(mu, pluginInformation.getUrl());
         assertFalse(pluginInformation.isBundled());
      }
   }

   @Test
   public void test2() throws IOException {
      File manifest = new File("src/test/resources/data/plugin/bundled/MANIFEST.MF");
      URL mu = manifest.toURI().toURL();
      try (InputStream in = Files.newInputStream(manifest.toPath())) {
         Manifest m = new Manifest(in);
         String pluginClass = "org.omegat.core.machinetranslators.BelazarTranslate";
         PluginInformation pluginInformation = PluginInformation.Builder
                 .fromManifest(pluginClass, m, mu, PluginInformation.Status.BUNDLED);
         assertEquals("OmegaT team", pluginInformation.getAuthor());
         assertTrue(pluginInformation.getDescription().startsWith("Bundled machine translator service "
                 + "connectors."));
         assertEquals("MT connector[bundle]: BelazarTranslate", pluginInformation.getName());
      }
   }

   @Test
   public void test3() throws IOException {
      File manifest = new File("src/test/resources/data/plugin/bundled/MANIFEST.MF");
      URL mu = manifest.toURI().toURL();
      try (InputStream in = Files.newInputStream(manifest.toPath())) {
         Manifest m = new Manifest(in);
         String pluginClass = "org.omegat.core.machinetranslators.DeepLTranslate";
         PluginInformation pluginInformation = PluginInformation.Builder
                 .fromManifest(pluginClass, m, mu, PluginInformation.Status.BUNDLED);
         assertEquals("OmegaT team", pluginInformation.getAuthor());
         String description = pluginInformation.getDescription();
         assertNotNull(description);
         assertTrue(description.startsWith("Bundled machine translator service connectors."));
         assertEquals("MT connector[bundle]: DeepLTranslate", pluginInformation.getName());
      }
   }

   @Test
   public void test4() throws IOException {
      File manifest = new File("src/test/resources/data/plugin/bundled/MANIFEST.MF");
      URL mu = manifest.toURI().toURL();
      try (InputStream in = Files.newInputStream(manifest.toPath())) {
         Manifest m = new Manifest(in);
         String pluginClass = "org.omegat.filters3.xml.openxml.OpenXMLFilter";
         PluginInformation pluginInformation = PluginInformation.Builder
                 .fromManifest(pluginClass, m, mu, PluginInformation.Status.BUNDLED);
         assertEquals("OmegaT team", pluginInformation.getAuthor());
         assertEquals("XML filters[bundle]: OpenXMLFilter", pluginInformation.getName());
         String description = pluginInformation.getDescription();
         assertNotNull(description);
         assertEquals("Bundled filters for various XML files includes MSOffice (OOXML), LibreOffice (ODF) "
                 + "and XLIFF", description);
      }
   }

   /**
    * A jar declaring several plugins under one main-attributes name gets the
    * class appended, so the plugin list rows stay distinguishable.
    */
   @Test
   public void testSharedMainNameGetsClassAppended() {
      Manifest m = new Manifest();
      m.getMainAttributes().putValue("Manifest-Version", "1.0");
      m.getMainAttributes().putValue("Plugin-Name", "Modern light/dark theme");
      m.getMainAttributes().putValue("OmegaT-Plugins",
              "org.omegat.gui.theme.DefaultFlatDarkTheme org.omegat.gui.theme.DefaultFlatLightTheme");
      PluginInformation info = PluginInformation.Builder.fromManifest("org.omegat.gui.theme.DefaultFlatDarkTheme",
              m, null, PluginInformation.Status.BUNDLED);
      assertEquals("Modern light/dark theme: DefaultFlatDarkTheme", info.getName());
   }

   /**
    * A single-plugin jar keeps its main-attributes name untouched, and the
    * name lookup without a class (version comparison path) stays as before.
    */
   @Test
   public void testSinglePluginNameStaysUntouched() {
      Manifest m = new Manifest();
      m.getMainAttributes().putValue("Manifest-Version", "1.0");
      m.getMainAttributes().putValue("Plugin-Name", "German plugin");
      m.getMainAttributes().putValue("OmegaT-Plugins", "org.omegat.languages.de.GermanPlugin");
      PluginInformation info = PluginInformation.Builder.fromManifest("org.omegat.languages.de.GermanPlugin",
              m, null, PluginInformation.Status.BUNDLED);
      assertEquals("German plugin", info.getName());
      PluginInformation anonymous = PluginInformation.Builder.fromManifest(null, m, null,
              PluginInformation.Status.BUNDLED);
      assertEquals("German plugin", anonymous.getName());
   }
}
