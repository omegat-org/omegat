/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Thomas Cordonnier
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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

package org.omegat.filters4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.FilterContext;
import org.omegat.filters2.TranslationException;
import org.omegat.filters3.xml.XMLReader;
import org.omegat.filters4.xml.AbstractXmlFilter;
import org.omegat.util.Log;

/**
 * Filter for a file format which is an archive containing XML.
 *
 * @author Thomas Cordonnier
 */
public abstract class AbstractZipFilter extends AbstractFilter {

    @Override
    public boolean isSourceEncodingVariable() {
        return true;
    }

    @Override
    public boolean isTargetEncodingVariable() {
        return true;
    }

    /**
     * Indicate that this file contained in the archive should be considered
     **/
    protected abstract boolean acceptInternalFile(ZipEntry entry, FilterContext context);

    @Override
    public boolean isFileSupported(File inFile, Map<String, String> config, FilterContext context) {
        try {
            ZipFile file = new ZipFile(inFile);
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (acceptInternalFile(entry, context)) {
                    file.close();
                    return true;
                }
            }
            file.close();
        } catch (IOException e) {
        }
        return false;
    }

    /**
     * Indicate that this file contained in the archive should be translated
     **/
    protected abstract boolean mustTranslateInternalFile(ZipEntry entry, boolean writeMode,
            FilterContext context);

    /**
     * Indicate that this file contained in the archive should not appear in the
     * target at all. Defaults to false.
     **/
    protected boolean mustDeleteInternalFile(ZipEntry entry, boolean writeMode, FilterContext context) {
        return false;
    }

    protected abstract AbstractXmlFilter getFilter(ZipEntry ze);

    /**
     * If not null, indicates that we want to see internal XML files in a
     * certain order.
     **/
    protected Comparator<ZipEntry> getEntryComparator() {
        return null;
    }

    /** Processes a ZIP file. */
    @Override
    public void processFile(File inFile, File outFile, FilterContext fc)
            throws IOException, TranslationException {
        ZipFile zf = new ZipFile(inFile);
        ZipOutputStream zipout = null;
        if (outFile != null) {
            zipout = new ZipOutputStream(new FileOutputStream(outFile));
        }

        try {
            Enumeration<? extends ZipEntry> zipcontents = zf.entries();
            List<ZipEntry> toTranslate = new LinkedList<>();
            Comparator<ZipEntry> cmp = getEntryComparator();
            while (zipcontents.hasMoreElements()) {
                ZipEntry ze = zipcontents.nextElement();
                if (mustTranslateInternalFile(ze, outFile != null, fc)) {
                    if ((cmp == null) || (outFile != null)) {
                        translateEntry(zf, zipout, fc, ze);
                    } else {
                        toTranslate.add(ze); // need sort before treatment
                    }
                } else if (!mustDeleteInternalFile(ze, outFile != null, fc)) {
                    if (zipout != null) {
                        ZipEntry outEntry = new ZipEntry(ze.getName());
                        zipout.putNextEntry(outEntry);

                        org.apache.commons.io.IOUtils.copy(zf.getInputStream(ze), zipout);
                        zipout.closeEntry();
                    }
                }
            }
            if (cmp != null) {
                Collections.sort(toTranslate, cmp);
            }
            for (ZipEntry ze : toTranslate) {
                translateEntry(zf, zipout, fc, ze);
            }
        } finally {
            if (zipout != null) {
                zipout.close();
            }
            zf.close();
        }
    }

    private void translateEntry(ZipFile zf, ZipOutputStream zipout, FilterContext fc, ZipEntry ze) {
        try {
            AbstractXmlFilter xmlfilter = getFilter(ze);
            XMLReader xReader = new XMLReader(zf.getInputStream(ze));
            BufferedReader reader = new BufferedReader(xReader);
            if (zipout == null) {
                xmlfilter.processFile(reader, null, fc);
            } else {
                ZipEntry outEntry = new ZipEntry(ze.getName());
                zipout.putNextEntry(outEntry);
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(zipout, xReader.getEncoding()));
                xmlfilter.processFile(reader, writer, fc);
                zipout.closeEntry();
            }
        } catch (Exception e) {
            Log.log(e);
            // continue: an error in one file should not prevent generation of
            // other files!
        }
    }

    protected void processFile(BufferedReader inFile, BufferedWriter outFile, FilterContext fc)
            throws IOException, TranslationException {
        throw new IOException("Not implemented");
    }

}
