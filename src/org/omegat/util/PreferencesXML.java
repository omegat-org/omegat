/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               2007 Zoltan Bartko
               2008-2009 Didier Briel
               2010 Wildrich Fourie, Antonio Vilei, Didier Briel
               2011 John Moran, Didier Briel
               2012 Martin Fleurke, Wildrich Fourie, Didier Briel, Thomas Cordonnier,
                    Aaron Madlon-Kay
               2013 Aaron Madlon-Kay, Zoltan Bartko
               2014 Piotr Kulik, Aaron Madlon-Kay
               2015 Aaron Madlon-Kay, Yu Tang, Didier Briel, Hiroshi Miura
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

package org.omegat.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.omegat.filters2.TranslationException;
import org.omegat.util.PreferencesImpl.IPrefsPersistence;
import org.omegat.util.xml.XMLBlock;
import org.omegat.util.xml.XMLStreamReader;

public class PreferencesXML implements IPrefsPersistence {

    private final File loadFile;
    private final File saveFile;

    public PreferencesXML(File loadFile, File saveFile) {
        this.loadFile = loadFile;
        this.saveFile = saveFile;
    }

    /**
     * Loads the preferences from disk, from the specified file or, if the
     * file is null, it attempts to load from a prefs file bundled inside
     * the JAR (not supplied by default).
     */
    @Override
    public void load(List<String> keys, List<String> values) {

        try (XMLStreamReader xml = new XMLStreamReader()) {
            xml.killEmptyBlocks();
            if (loadFile == null) {
                // If no prefs file is present, look inside JAR for
                // defaults. Useful for e.g. Web Start.
                try (InputStream is = getClass().getResourceAsStream(Preferences.FILE_PREFERENCES)) {
                    if (is != null) {
                        xml.setStream(is);
                        readXmlPrefs(xml, keys, values);
                    }
                }
            } else {
                xml.setStream(loadFile);
                readXmlPrefs(xml, keys, values);
            }
        } catch (TranslationException te) {
            // error loading preference file - keep whatever was
            // loaded then return gracefully to calling function
            // print an error to the console as an FYI
            Log.logWarningRB("PM_WARNING_PARSEERROR_ON_READ");
            Log.log(te);
            makeBackup(loadFile);
        } catch (IndexOutOfBoundsException e3) {
            // error loading preference file - keep whatever was
            // loaded then return gracefully to calling function
            // print an error to the console as an FYI
            Log.logWarningRB("PM_WARNING_PARSEERROR_ON_READ");
            Log.log(e3);
            makeBackup(loadFile);
        } catch (UnsupportedEncodingException e3) {
            // unsupported encoding - forget about it
            Log.logErrorRB(e3, "PM_UNSUPPORTED_ENCODING");
            makeBackup(loadFile);
        } catch (IOException e4) {
            // can't read file - forget about it and move on
            Log.logErrorRB(e4, "PM_ERROR_READING_FILE");
            makeBackup(loadFile);
        }
    }

    private static void makeBackup(File file) {
        if (file == null || !file.isFile()) {
            return;
        }
        String timestamp = new SimpleDateFormat("yyyyMMddHHmm").format(new Date());
        File bakFile = new File(file.getAbsolutePath() + "." + timestamp + ".bak");
        try {
            FileUtils.copyFile(file, bakFile);
            Log.logWarningRB("PM_BACKED_UP_PREFS_FILE", bakFile.getAbsolutePath());
        } catch (IOException ex) {
            Log.logErrorRB(ex, "PM_ERROR_BACKING_UP_PREFS_FILE");
        }
    }

    static void readXmlPrefs(XMLStreamReader xml, List<String> keys, List<String> values)
            throws TranslationException {
        XMLBlock blk;
        List<XMLBlock> lst;

        String pref;
        String val;
        // advance to omegat tag
        if (xml.advanceToTag("omegat") == null) {
            return;
        }
        // advance to project tag
        if ((blk = xml.advanceToTag("preference")) == null) {
            return;
        }
        String ver = blk.getAttribute("version");
        if (ver != null && !ver.equals("1.0")) {
            // unsupported preference file version - abort read
            return;
        }
        lst = xml.closeBlock(blk);
        if (lst == null) {
            return;
        }
        for (int i = 0; i < lst.size(); i++) {
            blk = lst.get(i);
            if (blk.isClose()) {
                continue;
            }
            if (!blk.isTag()) {
                continue;
            }
            pref = blk.getTagName();
            blk = lst.get(++i);
            if (blk.isClose()) {
                // allow empty string as a preference value
                val = "";
            } else {
                val = blk.getText();
            }
            if (pref != null && val != null) {
                // valid match - record these
                keys.add(pref);
                values.add(val);
            }
        }
    }

    @Override
    public void save(List<String> keys, List<String> values) throws Exception {
        try (BufferedWriter out = Files.newBufferedWriter(saveFile.toPath(), StandardCharsets.UTF_8)) {
            out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            out.write("<omegat>\n");
            out.write("  <preference version=\"1.0\">\n");

            for (int i = 0; i < keys.size(); i++) {
                String name = keys.get(i);
                String val = StringUtil.makeValidXML(values.get(i).toString());
                out.write("    <" + name + ">");
                out.write(val);
                out.write("</" + name + ">\n");
            }
            out.write("  </preference>\n");
            out.write("</omegat>\n");
        }
    }
}
