/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Didier Briel, Alex Buloichik
               2009 Didier Briel
               2012 Didier Briel, Aaron Madlon-Kay
               2013 Aaron Madlon-Kay, Guido Leenders
               2014 Aaron Madlon-Kay, Alex Buloichik
               2015 Aaron Madlon-Kay
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

package org.omegat.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.io.FileUtils;
import org.omegat.core.data.ProjectProperties;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.PluginUtils;

import gen.core.project.Masks;
import gen.core.project.Omegat;
import gen.core.project.Project;
import gen.core.project.Project.Repositories;

/**
 * Class that reads and saves project definition file.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Didier Briel
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Guido Leenders
 */
public class ProjectFileStorage {

    /**
     * A marker that tells OmegaT that project's subfolder has default location.
     */
    public static final String DEFAULT_FOLDER_MARKER = "__DEFAULT__";

    static private final JAXBContext CONTEXT;
    static {
        try {
            CONTEXT = JAXBContext.newInstance(Omegat.class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static Omegat parseProjectFile(File file) throws Exception {
        return parseProjectFile(FileUtils.readFileToByteArray(file));
    }

    public static Omegat parseProjectFile(byte[] projectFile) throws Exception {
        return (Omegat) CONTEXT.createUnmarshaller().unmarshal(new ByteArrayInputStream(projectFile));
    }

    /**
     * Load the project properties file for the project at the specified directory. The properties file is
     * assumed to exist at the root of the project and have the default name, {@link OConsts#FILE_PROJECT}.
     * This is a convenience method for {@link #loadPropertiesFile(File, File)}.
     * <p>
     * If the supplied {@link File} is not a directory, an {@link IllegalArgumentException} will be thrown.
     * 
     * @param projectDir
     *            The directory of the project
     * @return The loaded project properties
     * @throws Exception
     */
    public static ProjectProperties loadProjectProperties(File projectDir) throws Exception {
        return loadPropertiesFile(projectDir, new File(projectDir, OConsts.FILE_PROJECT));
    }

    /**
     * Load the specified project properties file for the project at the specified directory.
     * <p>
     * If <code>projectDir</code> is not a directory or <code>projectFile</code> is not a file, an
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param projectDir
     *            The directory of the project
     * @param projectFile
     *            The project properties file to load
     * @return The loaded project properties
     * @throws Exception
     */
    public static ProjectProperties loadPropertiesFile(File projectDir, File projectFile) throws Exception {
        if (!projectFile.isFile()) {
            throw new IllegalArgumentException("Project file was not a file");
        }
        Omegat om = parseProjectFile(projectFile);
        return loadPropertiesFile(projectDir, om);
    }

    static ProjectProperties loadPropertiesFile(File projectDir, Omegat om) throws Exception {
        if (!projectDir.isDirectory()) {
            throw new IllegalArgumentException("Project directory was not a directory");
        }

        ProjectProperties result = new ProjectProperties(projectDir);

        if (!OConsts.PROJ_CUR_VERSION.equals(om.getProject().getVersion())) {
            throw new TranslationException(StringUtil.format(
                    OStrings.getString("PFR_ERROR_UNSUPPORTED_PROJECT_VERSION"),
                    om.getProject().getVersion()));
        }

        // if folder is in default locations, name stored as __DEFAULT__
        String m_root = projectDir.getAbsolutePath() + File.separator;

        result.setTargetRootRelative(computeRelative(om.getProject().getTargetDir(), OConsts.DEFAULT_TARGET));
        result.setTargetRoot(computeAbsolutePath(m_root, om.getProject().getTargetDir(),
                OConsts.DEFAULT_TARGET));
        result.setSourceRootRelative(computeRelative(om.getProject().getSourceDir(), OConsts.DEFAULT_SOURCE));
        result.setSourceRoot(computeAbsolutePath(m_root, om.getProject().getSourceDir(),
                OConsts.DEFAULT_SOURCE));
        result.getSourceRootExcludes().clear();
        if (om.getProject().getSourceDirExcludes() != null) {
            result.getSourceRootExcludes().addAll(om.getProject().getSourceDirExcludes().getMask());
        } else {
            // sourceRootExclude was not defined
            result.getSourceRootExcludes().addAll(Arrays.asList(ProjectProperties.DEFAULT_EXCLUDES));
        }
        result.setTMRoot(computeAbsolutePath(m_root, om.getProject().getTmDir(), OConsts.DEFAULT_TM));
        result.setTMRootRelative(computeRelative(om.getProject().getTmDir(), OConsts.DEFAULT_TM));

        result.setGlossaryRoot(computeAbsolutePath(m_root, om.getProject().getGlossaryDir(),
                OConsts.DEFAULT_GLOSSARY));
        result.setGlossaryRootRelative(computeRelative(om.getProject().getGlossaryDir(),
                OConsts.DEFAULT_GLOSSARY));

        // Compute glossary file location
        String glossaryFile = om.getProject().getGlossaryFile();
        if (StringUtil.isEmpty(glossaryFile)) {
            glossaryFile = DEFAULT_FOLDER_MARKER;
        }
        if (glossaryFile.equalsIgnoreCase(DEFAULT_FOLDER_MARKER)) {
            glossaryFile = result.computeDefaultWriteableGlossaryFile();
        } else {
            glossaryFile = result.getGlossaryDir().getAsString() + glossaryFile;
        }
        result.setWriteableGlossary(glossaryFile);

        result.setDictRoot(computeAbsolutePath(m_root, om.getProject().getDictionaryDir(),
                OConsts.DEFAULT_DICT));
        result.setDictRootRelative(computeRelative(om.getProject().getDictionaryDir(), OConsts.DEFAULT_DICT));

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

        if (om.getProject().getRepositories() != null) {
            result.setRepositories(om.getProject().getRepositories().getRepository());
        }

        return result;
    }

    /**
     * Saves project file to disk.
     */
    public static void writeProjectFile(ProjectProperties props) throws Exception {
        File outFile = new File(props.getProjectRoot(), OConsts.FILE_PROJECT);
        String m_root = outFile.getAbsoluteFile().getParent() + File.separator;

        Omegat om = new Omegat();
        om.setProject(new Project());
        om.getProject().setVersion(OConsts.PROJ_CUR_VERSION);

        om.getProject().setSourceDir(
                computeRelativePath(m_root, props.getSourceRoot(), OConsts.DEFAULT_SOURCE));
        om.getProject().setSourceDirExcludes(new Masks());
        om.getProject().getSourceDirExcludes().getMask().addAll(props.getSourceRootExcludes());
        om.getProject().setTargetDir(
                computeRelativePath(m_root, props.getTargetRoot(), OConsts.DEFAULT_TARGET));
        om.getProject().setTmDir(computeRelativePath(m_root, props.getTMRoot(), OConsts.DEFAULT_TM));
        om.getProject().setGlossaryDir(
                computeRelativePath(m_root, props.getGlossaryRoot(), OConsts.DEFAULT_GLOSSARY));

        // Compute glossary file location
        String glossaryFile = computeRelativePath(props.getGlossaryRoot(), props.getWriteableGlossary(), null); // Rel file name
        String glossaryDir = computeRelativePath(m_root, props.getGlossaryRoot(), OConsts.DEFAULT_GLOSSARY);
        if (glossaryDir.equalsIgnoreCase(DEFAULT_FOLDER_MARKER) && props.isDefaultWriteableGlossaryFile()) {
            // Everything equals to default
            glossaryFile = DEFAULT_FOLDER_MARKER;
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

        if (props.getRepositories() != null && !props.getRepositories().isEmpty()) {
            om.getProject().setRepositories(new Repositories());
            om.getProject().getRepositories().getRepository().addAll(props.getRepositories());
        }

        Marshaller m = CONTEXT.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(om, outFile);
    }

    private static String computeRelative(String relativePath, String defaultName) {
        if (DEFAULT_FOLDER_MARKER.equals(relativePath)) {
            return asDirectory(defaultName);
        } else {
            return asDirectory(relativePath);
        }
    }

    /**
     * Constructs directory name like 'some/dir/'.
     */
    private static String asDirectory(String path) {
        String r = path.replace(File.separatorChar, '/');
        r = r.replaceAll("//+", "/");
        if (r.startsWith("/")) {
            r = r.substring(1);
        }
        if (!r.endsWith("")) {
            r += "/";
        }
        return r;
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
        if (DEFAULT_FOLDER_MARKER.equals(relativePath))
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
        if (defaultName != null && new File(absolutePath).equals(new File(m_root, defaultName))) {
            return DEFAULT_FOLDER_MARKER;
        }

        try {
            // trying to look two folders up
            String res = absolutePath;
            File abs = new File(absolutePath).getCanonicalFile();
            File root = new File(m_root).getCanonicalFile();
            String prefix = "";
            //
            // Try to derive the absolutePath as a relative path
            // from root.
            // First test whether the exact match is possible.
            // Then on each try, one folder is moved up from the root.
            //
            // Currently, maximum MAX_PARENT_DIRECTORIES_ABS2REL levels up.
            // More than these directory levels different seems to be that the paths
            // were not intended to be related.
            //
            for (int i = 0; i <= OConsts.MAX_PARENT_DIRECTORIES_ABS2REL; i++) {
                //
                // File separator added to prevent "/MyProject EN-FR/"
                // to be understood as being inside "/MyProject/" [1879571]
                //
                 if ((abs.getPath() + File.separator).startsWith(root.getPath() + File.separator)) {
                     res = prefix + abs.getPath().substring(root.getPath().length());
                     if (res.startsWith(File.separator))
                        res = res.substring(1);
                    break;
                } else {
                    root = root.getParentFile();
                    prefix += File.separator + "..";
                    //
                    // There are no more parent paths.
                    //
                    if (root == null) {
                      break;
                    }
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
        if (!StringUtil.isEmpty(className)) {
            try {
                return ProjectFileStorage.class.getClassLoader().loadClass(className);
            } catch (ClassNotFoundException e) {
                Log.log(e.toString());
            }
        }
        return PluginUtils.getTokenizerClassForLanguage(fallback);
    }
}
