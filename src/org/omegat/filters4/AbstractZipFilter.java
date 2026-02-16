/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2017 Thomas Cordonnier
               2025 Hiroshi Miura
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

package org.omegat.filters4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
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

    private static final Pattern ENCODING_PATTERN = Pattern.compile("<\\?xml.*encoding=['\"](.*?)['\"]");

    protected AbstractZipFilter() {
        this(StandardCharsets.UTF_8);
    }

    protected AbstractZipFilter(Charset encoding) {
        internalEncoding = encoding;
    }

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

    protected Charset internalEncoding;

    protected Charset detectInternalEncoding(ZipFile zipfFle, ZipEntry entry) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(zipfFle.getInputStream(entry))) {
            String xmlContent = readZipEntryContent(zis);
            Matcher matcher = ENCODING_PATTERN.matcher(xmlContent);
            if (matcher.find()) {
                return Charset.forName(matcher.group(1));
            }
        } catch (Exception ignored) {
            // when failed to detect, we fall back to UTF-8
        }
        return StandardCharsets.UTF_8;
    }

    @Override
    public boolean isFileSupported(File inFile, Map<String, String> config, FilterContext context) {
        try (ZipFile file = new ZipFile(inFile)) {
            Enumeration<? extends ZipEntry> entries = file.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (acceptInternalFile(entry, context)) {
                    internalEncoding = detectInternalEncoding(file, entry);
                    return true;
                }
            }
        } catch (IOException ignored) {
            // always considered non-supported
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
        List<ZipEntry> translatableEntries = new LinkedList<>();
        Comparator<ZipEntry> entryComparator = getEntryComparator();

        try (ZipFile zipFile = new ZipFile(inFile);
                ZipOutputStream zipOutputStream = createZipOutputStream(outFile);
                BufferedWriter writer = createWriter(zipOutputStream)) {

            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();

            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();

                if (mustTranslateInternalFile(zipEntry, zipOutputStream != null, fc)) {
                    processTranslatableEntry(zipFile, zipOutputStream, writer, fc, translatableEntries,
                            entryComparator, zipEntry);
                } else if (!mustDeleteInternalFile(zipEntry, zipOutputStream != null, fc)
                        && zipOutputStream != null) {
                    copyUnchangedEntry(zipFile, zipOutputStream, zipEntry);
                }
            }

            finalizeProcessing(zipFile, zipOutputStream, writer, fc, translatableEntries, entryComparator);
        }
    }

    /**
     * Creates a ZipOutputStream for the specified output file.
     *
     * @param outFile
     *            the file to which the ZIP output will be written; passing null
     *            will result in a null return.
     * @return a ZipOutputStream for writing to the specified file, or null if
     *         the outFile is null.
     * @throws IOException
     *             if an I/O error occurs while creating the output stream.
     */
    private ZipOutputStream createZipOutputStream(File outFile) throws IOException {
        return outFile == null ? null : new ZipOutputStream(new FileOutputStream(outFile));
    }

    /**
     * Creates a BufferedWriter for the provided ZipOutputStream, using the
     * specified internal encoding. If the ZipOutputStream is null, the method
     * returns null.
     *
     * @param zipOutputStream
     *            the ZipOutputStream for which the BufferedWriter will be
     *            created; passing null will result in a null return.
     * @return a BufferedWriter wrapping the given ZipOutputStream, or null if
     *         the ZipOutputStream is null.
     */
    private BufferedWriter createWriter(ZipOutputStream zipOutputStream) {
        return (zipOutputStream == null) ? null
                : new BufferedWriter(new OutputStreamWriter(zipOutputStream, internalEncoding));
    }

    private void processTranslatableEntry(ZipFile zipFile, ZipOutputStream zipOutputStream,
            BufferedWriter writer, FilterContext filterContext, List<ZipEntry> translatableEntries,
            Comparator<ZipEntry> entryComparator, ZipEntry zipEntry) {
        if (entryComparator == null || zipOutputStream != null) {
            translateEntry(zipFile, zipOutputStream, writer, filterContext, zipEntry);
        } else {
            translatableEntries.add(zipEntry);
        }
    }

    private void copyUnchangedEntry(ZipFile zipFile, ZipOutputStream zipOutputStream, ZipEntry zipEntry)
            throws IOException {
        ZipEntry outputEntry = new ZipEntry(zipEntry.getName());
        zipOutputStream.putNextEntry(outputEntry);
        org.apache.commons.io.IOUtils.copy(zipFile.getInputStream(zipEntry), zipOutputStream);
        zipOutputStream.closeEntry();
    }

    private void finalizeProcessing(ZipFile zipFile, ZipOutputStream zipOutputStream, BufferedWriter writer,
            FilterContext filterContext, List<ZipEntry> translatableEntries,
            Comparator<ZipEntry> entryComparator) {
        if (entryComparator != null) {
            translatableEntries.sort(entryComparator);
        }
        if (zipOutputStream == null) {
            translateEntries(zipFile, filterContext, translatableEntries);
        } else {
            for (ZipEntry zipEntry : translatableEntries) {
                translateEntry(zipFile, zipOutputStream, writer, filterContext, zipEntry);
            }
        }
    }

    private void translateEntries(ZipFile zf, FilterContext fc, List<ZipEntry> toTranslate) {
        for (ZipEntry ze : toTranslate) {
            try (XMLReader xReader = new XMLReader(zf.getInputStream(ze))) {
                AbstractXmlFilter xmlfilter = getFilter(ze);
                try (BufferedReader reader = new BufferedReader(xReader)) {
                    xmlfilter.processFile(reader, null, fc);
                }
            } catch (Exception e) {
                Log.log(e);
            }
        }
    }

    private void translateEntry(ZipFile zf, ZipOutputStream zipout, BufferedWriter writer, FilterContext fc,
            ZipEntry ze) {
        try (XMLReader xReader = new XMLReader(zf.getInputStream(ze))) {
            AbstractXmlFilter xmlfilter = getFilter(ze);
            try (BufferedReader reader = new BufferedReader(xReader)) {
                ZipEntry outEntry = new ZipEntry(ze.getName());
                zipout.putNextEntry(outEntry);
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

    /**
     * Read the content of the entry from the ZipInputStream as a string.
     *
     * @param zipInputStream
     *            The ZipInputStream to read from.
     * @return The content of the input stream as a string.
     */
    public static String readZipEntryContent(ZipInputStream zipInputStream) throws IOException {
        byte[] buffer = new byte[1024];
        StringBuilder stringBuilder = new StringBuilder();
        int bytesRead;

        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
            stringBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            // Reading only a limited portion to detect encoding
            if (stringBuilder.length() > 500) {
                break;
            }
        }

        return stringBuilder.toString();
    }

}
