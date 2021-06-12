/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2021 Hiroshi Miura
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

package org.omegat.core.data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.Manifest;

import org.junit.Test;


/**
 * Test for RemotePluginInformation class.
 * @author Hiroshi Miura
 */
public class RemotePluginInInformationTest {

   @Test
   public void test1() throws IOException {
      try (InputStream in = new FileInputStream("test/data/plugin/plugins.MF")) {
         Manifest m = new Manifest(in);
         String pluginClass = m.getMainAttributes().getValue("OmegaT-Plugins");
         RemotePluginInformation remotePluginInformation = new RemotePluginInformation(pluginClass, m);
         assert (remotePluginInformation.getJarFilename().equals("omegat-textra-plugin-2020.2.2.jar"));
      }
   }
}