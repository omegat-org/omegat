/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Didier Briel, Alex Buloichik
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 **************************************************************************/

package org.omegat.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import org.omegat.core.data.ProjectProperties;
import org.omegat.filters2.TranslationException;
import org.omegat.util.xml.XMLBlock;
import org.omegat.util.xml.XMLStreamReader;

/**
 * Class that reads and saves project definition file.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class ProjectFileStorage {

    public static ProjectProperties loadProjectProperties(File projectDir)
            throws IOException, TranslationException {
        ProjectProperties result = new ProjectProperties(projectDir);

        File inFile = new File(projectDir, OConsts.FILE_PROJECT);

        XMLStreamReader m_reader = new XMLStreamReader();
        m_reader.killEmptyBlocks();
        m_reader.setStream(inFile.getAbsolutePath(), "UTF-8"); // NOI18N

        // verify valid project file
        XMLBlock blk;
        List<XMLBlock> lst;

        // advance to omegat tag
        if (m_reader.advanceToTag("omegat") == null) // NOI18N
            return result;

        // advance to project tag
        if ((blk = m_reader.advanceToTag("project")) == null) // NOI18N
            return result;

        String ver = blk.getAttribute("version"); // NOI18N
        if (ver != null && !ver.equals(OConsts.PROJ_CUR_VERSION)) {
            throw new TranslationException(StaticUtils.format(OStrings
                    .getString("PFR_ERROR_UNSUPPORTED_PROJECT_VERSION"),
                    new Object[] { ver }));
        }

        // if folder is in default locations, name stored as __DEFAULT__
        String m_root = inFile.getParentFile().getAbsolutePath()
                + File.separator;

        lst = m_reader.closeBlock(blk);
        if (lst == null)
            return result;

        for (int i = 0; i < lst.size(); i++) {
            blk = lst.get(i);
            if (blk.isClose())
                continue;

            if (blk.getTagName().equals("target_dir")) // NOI18N
            {
                if (++i >= lst.size())
                    break;
                blk = lst.get(i);
                result.setTargetRoot(computeAbsolutePath(m_root, blk.getText(),
                        OConsts.DEFAULT_TARGET));
            } else if (blk.getTagName().equals("source_dir")) // NOI18N
            {
                if (++i >= lst.size())
                    break;
                blk = lst.get(i);
                result.setSourceRoot(computeAbsolutePath(m_root, blk.getText(),
                        OConsts.DEFAULT_SOURCE));
            } else if (blk.getTagName().equals("tm_dir")) // NOI18N
            {
                if (++i >= lst.size())
                    break;
                blk = lst.get(i);
                result.setTMRoot(computeAbsolutePath(m_root, blk.getText(),
                        OConsts.DEFAULT_TM));
            } else if (blk.getTagName().equals("glossary_dir")) // NOI18N
            {
                if (++i >= lst.size())
                    break;
                blk = lst.get(i);
                result.setGlossaryRoot(computeAbsolutePath(m_root, blk
                        .getText(), OConsts.DEFAULT_GLOSSARY));
            } else if (blk.getTagName().equals("dictionary_dir")) // NOI18N
            {
                if (++i >= lst.size())
                    break;
                blk = lst.get(i);
                result.setDictRoot(computeAbsolutePath(m_root, blk
                        .getText(), OConsts.DEFAULT_DICT));
            } else if (blk.getTagName().equals("source_lang")) // NOI18N
            {
                if (++i >= lst.size())
                    break;
                blk = lst.get(i);
                if (blk != null)
                    result.setSourceLanguage(blk.getText());
            } else if (blk.getTagName().equals("target_lang")) // NOI18N
            {
                if (++i >= lst.size())
                    break;
                blk = lst.get(i);
                if (blk != null)
                    result.setTargetLanguage(blk.getText());
            } else if (blk.getTagName().equals("sentence_seg")) // NOI18N
            {
                if (++i >= lst.size())
                    break;
                blk = lst.get(i);
                if (blk != null)
                    result.setSentenceSegmentingEnabled(Boolean
                            .parseBoolean(blk.getText()));
            }
        }

        return result;
    }

    /**
     * Saves project file to disk.
     */
    public static void writeProjectFile(ProjectProperties props)
            throws IOException {
        File outFile = new File(props.getProjectRoot(), OConsts.FILE_PROJECT);
        String m_root = outFile.getParentFile().getAbsolutePath()
                + File.separator;

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outFile), OConsts.UTF8));
        out.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"); // NOI18N
        out.write("<omegat>\n"); // NOI18N
        out.write("  <project version=\"1.0\">\n"); // NOI18N
        out.write("    <source_dir>" + // NOI18N
                computeRelativePath(m_root, props.getSourceRoot(),
                        OConsts.DEFAULT_SOURCE) + "</source_dir>\n"); // NOI18N
        out.write("    <target_dir>" + // NOI18N
                computeRelativePath(m_root, props.getTargetRoot(),
                        OConsts.DEFAULT_TARGET) + "</target_dir>\n"); // NOI18N
        out.write("    <tm_dir>" + // NOI18N
                computeRelativePath(m_root, props.getTMRoot(),
                        OConsts.DEFAULT_TM) + "</tm_dir>\n"); // NOI18N
        out.write("    <glossary_dir>" + // NOI18N
                computeRelativePath(m_root, props.getGlossaryRoot(),
                        OConsts.DEFAULT_GLOSSARY) + "</glossary_dir>\n"); // NOI18N
        out.write("    <dictionary_dir>" + // NOI18N
                computeRelativePath(m_root, props.getDictRoot(),
                        OConsts.DEFAULT_DICT) + "</dictionary_dir>\n"); // NOI18N
        out.write("    <source_lang>" + props.getSourceLanguage()
                + "</source_lang>\n"); // NOI18N
        out.write("    <target_lang>" + props.getTargetLanguage()
                + "</target_lang>\n"); // NOI18N
        out.write("    <sentence_seg>" + props.isSentenceSegmentingEnabled()
                + "</sentence_seg>\n"); // NOI18N
        out.write("  </project>\n"); // NOI18N
        out.write("</omegat>\n"); // NOI18N
        out.close();
    }

    /**
     * Returns absolute path for any project's folder. Since 1.6.0 supports
     * relative paths (RFE 1111956).
     * 
     * @param relativePath
     *                relative path from project file.
     * @param defaultName
     *                default name for such a project's folder, if relativePath
     *                is "__DEFAULT__".
     */
    private static String computeAbsolutePath(String m_root,
            String relativePath, String defaultName) {
        if (OConsts.DEFAULT_FOLDER_MARKER.equals(relativePath))
            return m_root + defaultName + File.separator;
        else {
            try {
                // check if path starts with a system root
                boolean startsWithRoot = false;
                for (File root : File.listRoots()) {
                    try // Under Windows and Java 1.4, there is an exception if
                    { // using getCanonicalPath on a non-existent drive letter
                        // [1875331] Relative paths not working under
                        // Windows/Java 1.4
                        startsWithRoot = relativePath.startsWith(root
                                .getCanonicalPath());
                    } catch (IOException e) {
                        startsWithRoot = false;
                    }
                    if (startsWithRoot)
                        // path starts with a root --> path is already absolute
                        return new File(relativePath).getCanonicalPath()
                                + File.separator;
                }

                // path does not start with a system root --> relative to
                // project root
                return new File(m_root, relativePath).getCanonicalPath()
                        + File.separator;
            } catch (IOException e) {
                return relativePath;
            }
        }
    }

    /**
     * Returns relative path for any project's folder. If absolutePath has
     * default location, returns "__DEFAULT__".
     * 
     * @param absolutePath
     *                absolute path to project folder.
     * @param defaultName
     *                default name for such a project's folder.
     * @since 1.6.0
     */
    private static String computeRelativePath(String m_root,
            String absolutePath, String defaultName) {
        if (absolutePath.equals(m_root + defaultName + File.separator))
            return OConsts.DEFAULT_FOLDER_MARKER;

        try {
            // trying to look two folders up
            String res = absolutePath;
            File abs = new File(absolutePath).getCanonicalFile();
            File root = new File(m_root).getCanonicalFile();
            String prefix = new String();
            for (int i = 0; i < 2; i++) {
                // File separator added to prevent "/MyProject EN-FR/"
                // to be undesrtood as being inside "/MyProject/" [1879571]
                if ((abs.getPath() + File.separator).startsWith(root.getPath()
                        + File.separator)) {
                    res = prefix
                            + abs.getPath().substring(root.getPath().length());
                    if (res.startsWith(File.separator))
                        res = res.substring(1);
                    break;
                } else {
                    root = root.getParentFile();
                    prefix += File.separator + ".."; // NOI18N
                }
            }
            return res.replace(File.separatorChar, '/');
        } catch (IOException e) {
            return absolutePath.replace(File.separatorChar, '/');
        }
    }
}
