/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Alex Buloichik
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

package org.omegat.gui.main;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.omegat.core.data.ProjectProperties;
import org.omegat.util.FileUtil;
import org.omegat.util.OConsts;

/**
 * Class for support some MED-specific operations.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public final class ProjectMedProcessing {

    private ProjectMedProcessing() {
    }

    /**
     * It creates project internals from MED zip file.
     */
    public static void extractFromMed(File medZip, ProjectProperties props) throws Exception {
        String medName = medZip.getName().replaceAll("\\.zip$", "");

        // extract source and target languages
        Properties p = new Properties();
        try (ZipFile zip = new ZipFile(medZip)) {
            ZipEntry e = zip.getEntry(medName + "/dossier/workflow/translation");
            if (e == null) {
                throw new Exception("Wrong MED zip structure");
            }
            try (InputStream in = zip.getInputStream(e)) {
                p.load(in);
            }
        }
        // get source language
        String slang = p.getProperty("slang");
        if (slang == null) {
            throw new Exception("Bad MED format: slang not defined");
        }
        // get target language
        String tlang = p.getProperty("tlang");
        if (tlang == null) {
            throw new Exception("Bad MED format: tlang not defined");
        }
        props.setSourceLanguage(slang);
        props.setTargetLanguage(tlang);

        // copy source files
        try (ZipFile zip = new ZipFile(medZip)) {
            extractFiles(zip, medName + "/doc/main/" + props.getSourceLanguage().getLanguage() + "/", null,
                    props.getSourceDir().getAsFile());
            extractFiles(zip, medName + "/doc/support/" + props.getSourceLanguage().getLanguage() + "/",
                    ".tmx", new File(props.getTMAutoRoot()));
            extractFiles(zip, medName + "/doc/support/" + props.getSourceLanguage().getLanguage() + "/term/",
                    null, props.getGlossaryDir().getAsFile());
        }

        // copy original zip
        File outZip = new File(props.getProjectInternalDir(), "med/" + medZip.getName());
        outZip.getParentFile().mkdirs();
        FileUtils.copyFile(medZip, outZip);
    }

    private static void extractFiles(ZipFile zip, String zipPrefix, String zipSuffix, File projectDir)
            throws Exception {
        for (Enumeration<? extends ZipEntry> en = zip.entries(); en.hasMoreElements();) {
            ZipEntry e = en.nextElement();
            if (e.isDirectory()) {
                continue;
            }
            if (!e.getName().startsWith(zipPrefix)) {
                continue;
            }
            if (zipSuffix != null && !e.getName().endsWith(zipSuffix)) {
                continue;
            }
            File outFile = new File(projectDir, e.getName().substring(zipPrefix.length()));
            outFile.getParentFile().mkdirs();
            try (InputStream in = zip.getInputStream(e)) {
                FileUtils.copyInputStreamToFile(in, outFile);
            }
        }
    }

    /**
     * Finds one .zip file inside omegat/med/ dir.
     */
    public static File getOriginMedFile(ProjectProperties props) throws Exception {
        File[] files = new File(props.getProjectInternalDir(), "med/").listFiles(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isFile() && f.getName().endsWith(".zip");
            }
        });
        if (files != null && files.length > 1) {
            throw new Exception("Too many source med files in the <project>/omegat/med directory");
        }
        File originMedFile = files != null && files.length > 0 ? files[0] : null;
        return originMedFile;
    }

    public static void createMed(File medZip, ProjectProperties props) throws Exception {
        if (medZip.getAbsolutePath().startsWith(props.getProjectRoot())) {
            throw new Exception("Med can't be inside project");
        }
        File sourceMedFile = getOriginMedFile(props);

        Properties translationContent = new Properties();

        String slang = props.getSourceLanguage().getLanguage();
        String tlang = props.getTargetLanguage().getLanguage();
        String medName = medZip.getName().replaceAll("\\.zip$", "");
        try (ZipOutputStream outZip = new ZipOutputStream(new FileOutputStream(medZip))) {
            if (sourceMedFile != null) {
                String oldMedName = sourceMedFile.getName().replaceAll("\\.zip$", "");
                String[] skipPrefixes = new String[] { oldMedName + "/doc/main/" + slang + "/",
                        oldMedName + "/doc/main/" + tlang + "/" };
                String[] skipFiles = new String[] { oldMedName + "/dossier/workflow/translation",
                        oldMedName + "/doc/support/" + slang + "/tm/" + props.getProjectName()
                                + OConsts.LEVEL2_TMX + OConsts.TMX_EXTENSION,
                        oldMedName + "/doc/support/" + slang + "/term/glossary.txt" };

                try (ZipFile zip = new ZipFile(sourceMedFile)) {
                    // extract old file
                    ZipEntry tre = zip.getEntry(oldMedName + "/dossier/workflow/translation");
                    if (tre == null) {
                        throw new Exception("Wrong MED zip structure");
                    }
                    try (InputStream in = zip.getInputStream(tre)) {
                        translationContent.load(in);
                    }
                    for (Enumeration<? extends ZipEntry> en = zip.entries(); en.hasMoreElements();) {
                        ZipEntry e = en.nextElement();
                        if (e.isDirectory()) {
                            continue;
                        }
                        boolean add = true;
                        for (String skip : skipFiles) {
                            if (e.getName().equals(skip)) {
                                add = false;
                                break;
                            }
                        }
                        if (add) {
                            for (String skip : skipPrefixes) {
                                if (e.getName().startsWith(skip)) {
                                    add = false;
                                    break;
                                }
                            }
                        }
                        if (!add) {
                            continue;
                        }
                        String newName = medName + e.getName().substring(oldMedName.length());
                        outZip.putNextEntry(new ZipEntry(newName));
                        try (InputStream in = zip.getInputStream(e)) {
                            IOUtils.copy(in, outZip);
                        }
                    }
                }
            }
            translationContent.put("slang", slang);
            translationContent.put("tlang", tlang);
            outZip.putNextEntry(new ZipEntry(medName + "/dossier/workflow/translation"));
            translationContent.store(outZip, null);
            // source files
            packDir(props.getSourceDir().getAsFile(), medName + "/doc/main/" + slang + "/", outZip);
            // translations
            packDir(props.getTargetDir().getAsFile(), medName + "/doc/main/" + tlang + "/", outZip);

            // level2.tmx
            outZip.putNextEntry(new ZipEntry(medName + "/doc/support/" + slang + "/tm/"
                    + props.getProjectName() + OConsts.LEVEL2_TMX + OConsts.TMX_EXTENSION));
            FileUtils.copyFile(new File(props.getProjectRootDir(),
                    props.getProjectName() + OConsts.LEVEL2_TMX + OConsts.TMX_EXTENSION), outZip);

            // statistics
            outZip.putNextEntry(new ZipEntry(medName + "/dossier/post/" + OConsts.STATS_FILENAME));
            FileUtils.copyFile(new File(props.getProjectInternalDir(), OConsts.STATS_FILENAME), outZip);

            // writable glossary
            if (props.getWritableGlossaryFile().getAsFile().exists()) {
                outZip.putNextEntry(new ZipEntry(medName + "/doc/support/" + slang + "/term/glossary.txt"));
                FileUtils.copyFile(props.getWritableGlossaryFile().getAsFile(), outZip);
            }
        }
    }

    private static void packDir(File dir, String namePrefix, ZipOutputStream zip) throws Exception {
        List<String> srcPathList = FileUtil.buildRelativeFilesList(dir, null, null);
        for (String f : srcPathList) {
            if (f.startsWith("/")) {
                f = f.substring(1);
            }
            zip.putNextEntry(new ZipEntry(namePrefix + f));
            FileUtils.copyFile(new File(dir, f), zip);
        }
    }
}
