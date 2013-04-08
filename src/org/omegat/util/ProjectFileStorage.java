/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Didier Briel, Alex Buloichik
               2009 Didier Briel
               2012 Didier Briel, Aaron Madlon-Kay
               2013 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util;

import gen.core.project.Omegat;
import gen.core.project.Project;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.omegat.core.data.ProjectProperties;
import org.omegat.core.segmentation.SRX;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.PluginUtils;

/**
 * Class that reads and saves project definition file.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 */
public class ProjectFileStorage {

    static private final JAXBContext CONTEXT;
    static {
        try {
            CONTEXT = JAXBContext.newInstance(Omegat.class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static ProjectProperties loadProjectProperties(File projectDir) throws Exception {
        ProjectProperties result = new ProjectProperties(projectDir);

        File inFile = new File(projectDir, OConsts.FILE_PROJECT);

        Omegat om = (Omegat) CONTEXT.createUnmarshaller().unmarshal(inFile);

        if (!OConsts.PROJ_CUR_VERSION.equals(om.getProject().getVersion())) {
            throw new TranslationException(StaticUtils.format(OStrings
                    .getString("PFR_ERROR_UNSUPPORTED_PROJECT_VERSION"), new Object[] { om.getProject()
                    .getVersion() }));
        }

        // if folder is in default locations, name stored as __DEFAULT__
        String m_root = inFile.getParentFile().getAbsolutePath() + File.separator;

        result.setTargetRoot(computeAbsolutePath(m_root, om.getProject().getTargetDir(),
                OConsts.DEFAULT_TARGET));
        result.setSourceRoot(computeAbsolutePath(m_root, om.getProject().getSourceDir(),
                OConsts.DEFAULT_SOURCE));
        result.setTMRoot(computeAbsolutePath(m_root, om.getProject().getTmDir(), OConsts.DEFAULT_TM));
        result.setGlossaryRoot(computeAbsolutePath(m_root, om.getProject().getGlossaryDir(),
                OConsts.DEFAULT_GLOSSARY));
        
        // Compute glossary file location
        String glossaryFile = om.getProject().getGlossaryFile();
        String glossaryDir = null;
        if (glossaryFile != null) {
            glossaryDir = new File(glossaryFile).getParent();
        } else {
            glossaryDir = result.getGlossaryRoot();
        }
        if (glossaryDir == null) {
            glossaryDir = ".";
        }
        glossaryDir = computeAbsolutePath(m_root, glossaryDir, OConsts.DEFAULT_GLOSSARY);
        glossaryFile = om.getProject().getGlossaryFile();
        if (glossaryFile == null) {
            glossaryFile = OConsts.DEFAULT_FOLDER_MARKER;
        }
        if (glossaryFile.equalsIgnoreCase(OConsts.DEFAULT_FOLDER_MARKER)) {
            glossaryDir = result.getGlossaryRoot();
            if (!glossaryDir.endsWith(File.separator)) {
                glossaryDir += File.separator;
            }
            glossaryFile = glossaryDir + result.getProjectName() + OConsts.DEFAULT_W_GLOSSARY;
        } else {
            glossaryFile = glossaryDir + new File(glossaryFile).getName();
        }
        result.setWriteableGlossary(glossaryFile);

        result.setDictRoot(computeAbsolutePath(m_root, om.getProject().getDictionaryDir(),
                OConsts.DEFAULT_DICT));

        result.setSourceLanguage(om.getProject().getSourceLang());
        result.setTargetLanguage(om.getProject().getTargetLang());

        result.setSourceTokenizer(loadTokenizer(om.getProject().getSourceTok(), result.getSourceLanguage()));
        result.setTargetTokenizer(loadTokenizer(om.getProject().getTargetTok(), result.getTargetLanguage()));
        
        if (om.getProject().isSentenceSeg() != null) {
            result.setSentenceSegmentingEnabled(om.getProject().isSentenceSeg());
        }
        if (om.getProject().isSupportDefaultTranslations() != null) {
            result.setSupportDefaultTranslations(om.getProject().isSupportDefaultTranslations());
        }
        if (om.getProject().isRemoveTags() != null) {
            result.setRemoveTags(om.getProject().isRemoveTags());
        }
        if (om.getProject().getExternalCommand() != null) {
            result.setExternalCommand(om.getProject().getExternalCommand());
        }

        return result;
    }

    /**
     * Saves project file to disk.
     */
    public static void writeProjectFile(ProjectProperties props) throws Exception {
        File outFile = new File(props.getProjectRoot(), OConsts.FILE_PROJECT);
        String m_root = outFile.getParentFile().getAbsolutePath() + File.separator;

        Omegat om = new Omegat();
        om.setProject(new Project());
        om.getProject().setVersion(OConsts.PROJ_CUR_VERSION);

        om.getProject().setSourceDir(
                computeRelativePath(m_root, props.getSourceRoot(), OConsts.DEFAULT_SOURCE));
        om.getProject().setTargetDir(
                computeRelativePath(m_root, props.getTargetRoot(), OConsts.DEFAULT_TARGET));
        om.getProject().setTmDir(computeRelativePath(m_root, props.getTMRoot(), OConsts.DEFAULT_TM));
        om.getProject().setGlossaryDir(
                computeRelativePath(m_root, props.getGlossaryRoot(), OConsts.DEFAULT_GLOSSARY));

        // Compute glossary file location
        String glossaryFile = new File(props.getWriteableGlossary()).getName(); // File name
        String glossaryDir = computeRelativePath(m_root, props.getGlossaryRoot(), OConsts.DEFAULT_GLOSSARY);
        if (glossaryDir.equalsIgnoreCase(OConsts.DEFAULT_FOLDER_MARKER)) { // Standard path for glossary
            if (!props.getWriteableGlossary().equalsIgnoreCase(props.getGlossaryRoot() +
                    props.getProjectName() + OConsts.DEFAULT_W_GLOSSARY)) {
                glossaryDir = props.getWriteableGlossaryDir();
                glossaryDir = computeRelativePath(m_root, glossaryDir, OConsts.DEFAULT_GLOSSARY);
                if (!StringUtil.isEmpty(glossaryDir)) {
                    glossaryDir += "/"; // Separator is always "/" internally
                }
                glossaryFile = glossaryDir + glossaryFile;
            } else { // Everything equals to default
                glossaryFile = OConsts.DEFAULT_FOLDER_MARKER;
            }
        } else {
            if (!glossaryDir.endsWith(File.separator)) {
                glossaryDir += "/"; // Separator is always "/" internally
            }
            glossaryFile = glossaryDir + glossaryFile;
        }
        om.getProject().setGlossaryFile(glossaryFile);

        om.getProject().setDictionaryDir(
                computeRelativePath(m_root, props.getDictRoot(), OConsts.DEFAULT_DICT));
        om.getProject().setSourceLang(props.getSourceLanguage().toString());
        om.getProject().setTargetLang(props.getTargetLanguage().toString());
        om.getProject().setSourceTok(props.getSourceTokenizer().getCanonicalName());
        om.getProject().setTargetTok(props.getTargetTokenizer().getCanonicalName());
        om.getProject().setSentenceSeg(props.isSentenceSegmentingEnabled());
        om.getProject().setSupportDefaultTranslations(props.isSupportDefaultTranslations());
        om.getProject().setRemoveTags(props.isRemoveTags());
        om.getProject().setExternalCommand(props.getExternalCommand());

        Marshaller m = CONTEXT.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(om, outFile);

        SRX.saveTo(props.getProjectSRX(), new File(props.getProjectInternal(), SRX.CONF_SENTSEG));
    }

    /**
     * Returns absolute path for any project's folder. Since 1.6.0 supports relative paths (RFE 1111956).
     * 
     * @param relativePath
     *            relative path from project file.
     * @param defaultName
     *            default name for such a project's folder, if relativePath is "__DEFAULT__".
     */
    private static String computeAbsolutePath(String m_root, String relativePath, String defaultName) {
        if (relativePath == null) {
            // Not exist in project file ? Use default.
            return m_root + defaultName + File.separator;
        }
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
                        String platformRelativePath = relativePath.replace('/', File.separatorChar);
                        // If a plaform-dependent form of relativePath is not
                        // used, startWith will always fail under Windows,
                        // because Windows uses C:\, while the path is stored as
                        // C:/ in omegat.project
                        startsWithRoot = platformRelativePath.startsWith(root.getCanonicalPath());
                    } catch (IOException e) {
                        startsWithRoot = false;
                    }
                    if (startsWithRoot)
                        // path starts with a root --> path is already absolute
                        return new File(relativePath).getCanonicalPath() + File.separator;
                }

                // path does not start with a system root --> relative to
                // project root
                return new File(m_root, relativePath).getCanonicalPath() + File.separator;
            } catch (IOException e) {
                return relativePath;
            }
        }
    }

    /**
     * Returns relative path for any project's folder. If absolutePath has default location, returns
     * "__DEFAULT__".
     * 
     * @param absolutePath
     *            absolute path to project folder.
     * @param defaultName
     *            default name for such a project's folder.
     * @since 1.6.0
     */
    private static String computeRelativePath(String m_root, String absolutePath, String defaultName) {
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
                if ((abs.getPath() + File.separator).startsWith(root.getPath() + File.separator)) {
                    res = prefix + abs.getPath().substring(root.getPath().length());
                    if (res.startsWith(File.separator))
                        res = res.substring(1);
                    break;
                } else {
                    root = root.getParentFile();
                    prefix += File.separator + "..";
                }
            }
            return res.replace(File.separatorChar, '/');
        } catch (IOException e) {
            return absolutePath.replace(File.separatorChar, '/');
        }
    }
    
    /**
     * Load a tokenizer class from its canonical name.
     * @param className Name of tokenizer class
     * @return Class object of specified tokenizer, or of fallback tokenizer
     * if the specified one could not be loaded for whatever reason.
     */
    private static Class<?> loadTokenizer(String className, Language fallback) {
        if (className != null && className.length() > 0) {
            try {
                return ProjectFileStorage.class.getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                Log.log(e.toString());
            }
        }
        return PluginUtils.getTokenizerClassForLanguage(fallback);
    }
}
