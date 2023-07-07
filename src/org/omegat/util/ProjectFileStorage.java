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

package org.omegat.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import org.omegat.core.data.ProjectProperties;
import org.omegat.filters2.TranslationException;
import org.omegat.filters2.master.PluginUtils;

import gen.core.project.Masks;
import gen.core.project.Omegat;
import gen.core.project.Project;
import gen.core.project.Project.Repositories;
import gen.core.project.RepositoryDefinition;

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
public final class ProjectFileStorage {

    private static final XmlMapper mapper;

    static {
        final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        // You should NOT use XMLInputFactor.getXMLInputFactory
        // that returns system default object.
        // Modifying a global object leads breakage of SuperTMXMerge
        // library.
        // https://sourceforge.net/p/omegat/bugs/1170/
        xmlInputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, Boolean.TRUE);
        XmlFactory xmlFactory = new XmlFactory(xmlInputFactory);
        mapper = XmlMapper.builder(xmlFactory).defaultUseWrapper(false)
                .enable(MapperFeature.USE_WRAPPER_NAME_AS_PROPERTY_NAME).build();
        mapper.registerModule(new JaxbAnnotationModule());
        SimpleModule module = new SimpleModule();
        TypeFactory typeFactory = mapper.getTypeFactory();
        final RepositoryDefinition def = new RepositoryDefinition();
        module.addSerializer(new ProjectOtherAttributesSerializer(typeFactory
                .constructMapType(def.getOtherAttributes().getClass(), QName.class, String.class)));
        mapper.registerModule(module);
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private ProjectFileStorage() {
    }

    /**
     * A marker that tells OmegaT that project's subfolder has default location.
     */
    public static final String DEFAULT_FOLDER_MARKER = "__DEFAULT__";

    public static Omegat parseProjectFile(File file) throws Exception {
        return parseProjectFile(FileUtils.readFileToByteArray(file));
    }

    public static Omegat parseProjectFile(byte[] projectFile) throws Exception {
        return mapper.readValue(projectFile, Omegat.class);
    }

    /**
     * Load the project properties file for the project at the specified
     * directory. The properties file is assumed to exist at the root of the
     * project and have the default name, {@link OConsts#FILE_PROJECT}. This is
     * a convenience method for {@link #loadPropertiesFile(File, File)}.
     * <p>
     * If the supplied {@link File} is not a directory, an
     * {@link IllegalArgumentException} will be thrown.
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
     * Load the specified project properties file for the project at the
     * specified directory.
     * <p>
     * If <code>projectDir</code> is not a directory or <code>projectFile</code>
     * is not a file, an {@link IllegalArgumentException} will be thrown.
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
            throw new IllegalArgumentException("Project file " + projectFile + " was not a file");
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
            throw new TranslationException(
                    StringUtil.format(OStrings.getString("PFR_ERROR_UNSUPPORTED_PROJECT_VERSION"),
                            om.getProject().getVersion()));
        }

        result.setTargetRoot(normalizeLoadedPath(om.getProject().getTargetDir(), OConsts.DEFAULT_TARGET));
        result.setSourceRoot(normalizeLoadedPath(om.getProject().getSourceDir(), OConsts.DEFAULT_SOURCE));
        result.getSourceRootExcludes().clear();
        if (om.getProject().getSourceDirExcludes() != null) {
            result.getSourceRootExcludes().addAll(om.getProject().getSourceDirExcludes().getMask());
        } else {
            // sourceRootExclude was not defined
            result.getSourceRootExcludes().addAll(ProjectProperties.getDefaultExcludes());
        }
        result.setTMRoot(normalizeLoadedPath(om.getProject().getTmDir(), OConsts.DEFAULT_TM));
        result.setExportTMRoot(
                normalizeLoadedPath(om.getProject().getExportTmDir(), OConsts.DEFAULT_EXPORT_TM));
        result.setExportTmLevels(normalizeLoadedExportTmLevels(om.getProject().getExportTmLevels(),
                OConsts.DEFAULT_EXPORT_TM_LEVELS));

        result.setGlossaryRoot(
                normalizeLoadedPath(om.getProject().getGlossaryDir(), OConsts.DEFAULT_GLOSSARY));

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

        result.setDictRoot(normalizeLoadedPath(om.getProject().getDictionaryDir(), OConsts.DEFAULT_DICT));

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
     * Saves project file to disk to default `omegat.project` file.
     */
    public static void writeProjectFile(ProjectProperties props) throws Exception {
        writeProjectFile(new File(props.getProjectRoot(), OConsts.FILE_PROJECT), props);
    }

    /**
     * Saves project file to disk.
     */
    public static void writeProjectFile(File outFile, ProjectProperties props) throws Exception {
        String root = outFile.getAbsoluteFile().getParent();

        Omegat om = new Omegat();
        om.setProject(new Project());
        om.getProject().setVersion(OConsts.PROJ_CUR_VERSION);

        om.getProject().setSourceDir(getPathForStoring(root, props.getSourceRoot(), OConsts.DEFAULT_SOURCE));
        om.getProject().setSourceDirExcludes(new Masks());
        om.getProject().getSourceDirExcludes().getMask().addAll(props.getSourceRootExcludes());
        om.getProject().setTargetDir(getPathForStoring(root, props.getTargetRoot(), OConsts.DEFAULT_TARGET));
        om.getProject().setTmDir(getPathForStoring(root, props.getTMRoot(), OConsts.DEFAULT_TM));
        om.getProject()
                .setExportTmDir(getPathForStoring(root, props.getExportTMRoot(), OConsts.DEFAULT_EXPORT_TM));
        om.getProject().setExportTmLevels(String.join(" ", props.getExportTmLevels()));
        String glossaryDir = getPathForStoring(root, props.getGlossaryRoot(), OConsts.DEFAULT_GLOSSARY);
        om.getProject().setGlossaryDir(glossaryDir);

        // Compute glossary file location: must be relative to glossary root
        String glossaryFile = getPathForStoring(props.getGlossaryRoot(), props.getWriteableGlossary(), null);
        if (glossaryDir.equalsIgnoreCase(DEFAULT_FOLDER_MARKER) && props.isDefaultWriteableGlossaryFile()) {
            // Everything equals to default
            glossaryFile = DEFAULT_FOLDER_MARKER;
        }
        om.getProject().setGlossaryFile(glossaryFile);

        om.getProject().setDictionaryDir(getPathForStoring(root, props.getDictRoot(), OConsts.DEFAULT_DICT));
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

        mapper.writeValue(outFile, om);
    }

    private static String normalizeLoadedPath(String path, String defaultValue) {
        // Older project files can be missing path definitions, in which case
        // path will be null here. In that case return the default.
        if (StringUtil.isEmpty(path) || DEFAULT_FOLDER_MARKER.equals(path)) {
            return defaultValue;
        } else {
            return normalizeSlashes(path);
        }
    }

    private static List<String> normalizeLoadedExportTmLevels(String levels, String defaultValue) {
        // Older project files can be missing the information of which TM levels
        // to export, in which case levels will be null here. In that case
        // return the default (converted to ArrayList)
        String normalizedLevels = levels;
        if (levels == null) {
            normalizedLevels = defaultValue;
        }

        // Convert space-separated items into ArrayList,
        // downcasing first
        return StringUtil.convertToList(normalizedLevels.toLowerCase());
    }

    /**
     * Converts a path to the format stored on disk. If
     * <code>absolutePath</code> has the default location given by
     * <code>root/defaultName</code>, returns <code>__DEFAULT__</code>.
     * <p>
     * Otherwise it attempts to compute a relative path based at
     * <code>root</code>. If this isn't possible (e.g. the paths don't share a
     * filesystem root) or if the relative path is more than
     * {@link OConsts#MAX_PARENT_DIRECTORIES_ABS2REL} levels away then it gives
     * up and returns the original <code>absolutePath</code>.
     *
     * @param root
     *            Root path against which to evaluate
     * @param absolutePath
     *            Absolute path to a folder
     * @param defaultName
     *            Default name for the folder
     * @since 1.6.0
     * @see <a href=
     *      "https://sourceforge.net/p/omegat/feature-requests/734/">RFE#734</a>
     * @see OConsts#MAX_PARENT_DIRECTORIES_ABS2REL
     */
    private static String getPathForStoring(String root, String absolutePath, String defaultName) {
        if (defaultName != null && new File(absolutePath).equals(new File(root, defaultName))) {
            return DEFAULT_FOLDER_MARKER;
        }

        // Fall back to using the input path if all else fails.
        String result = absolutePath;
        try {
            // Path.normalize() will resolve any remaining "../"
            Path absPath = Paths.get(absolutePath).normalize();
            String rel = Paths.get(root).relativize(absPath).toString();
            if (StringUtils.countMatches(rel,
                    ".." + File.separatorChar) <= OConsts.MAX_PARENT_DIRECTORIES_ABS2REL) {
                // Use the relativized path as it is "near" enough.
                result = rel;
            } else {
                //
                result = absPath.toString();
            }
        } catch (IllegalArgumentException e) {
        }
        return normalizeSlashes(result);
    }

    /**
     * Load a tokenizer class from its canonical name.
     *
     * @param className
     *            Name of tokenizer class
     * @return Class object of specified tokenizer, or of fallback tokenizer if
     *         the specified one could not be loaded for whatever reason.
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

    /**
     * Replace \ with / and remove / from the end if present. Within OmegaT we
     * generally require a / on the end of directories, but for storage we
     * prefer no trailing /.
     */
    static String normalizeSlashes(String path) {
        return withoutTrailingSlash(path.replace('\\', '/'));
    }

    static String withoutTrailingSlash(String path) {
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    @SuppressWarnings("serial")
    protected static class ProjectOtherAttributesSerializer extends StdSerializer<Map<QName, String>> {

        protected ProjectOtherAttributesSerializer(MapType type) {
            super(type);
        }

        @Override
        public void serialize(final Map<QName, String> value, final JsonGenerator gen,
                final SerializerProvider provider) throws IOException {
            if (value.size() > 0) {
                gen.writeStartObject();
                for (Map.Entry<QName, String> item : value.entrySet()) {
                    if (item.getValue() == null) {
                        continue;
                    }
                    gen.writeFieldName(item.getKey().getLocalPart());
                    gen.writeString(item.getValue());
                }
                gen.writeEndObject();
            }
        }
    }
}
