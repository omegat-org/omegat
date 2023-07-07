/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2012 Guido Leenders, Didier Briel
               2013 Aaron Madlon-Kay, Yu Tang
               2014 Aaron Madlon-Kay, Alex Buloichik
               2015 Aaron Madlon-Kay
               2017 Didier Briel
               2018 Thomas Cordonnier
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.omegat.core.segmentation.SRX;
import org.omegat.filters2.master.FilterMaster;
import org.omegat.filters2.master.PluginUtils;
import org.omegat.util.FileUtil;
import org.omegat.util.Language;
import org.omegat.util.Log;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Platform;
import org.omegat.util.StringUtil;

import gen.core.filters.Filters;
import gen.core.project.RepositoryDefinition;
import gen.core.project.RepositoryMapping;

/**
 * Storage for project properties. May read and write project from/to disk.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Guido Leenders
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Yu Tang
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Thomas Cordonnier
 */
public class ProjectProperties {

    private static final String[] DEFAULT_EXCLUDES = {
            "**/.svn/**",
            "**/CVS/**",
            "**/.cvs/**",
            "**/.git/**",
            "**/.hg/**",
            "**/.repositories/**",
            "**/desktop.ini",
            "**/Thumbs.db",
            "**/.DS_Store",
            "**/~$*" // MS Office owner file; see https://superuser.com/a/405258/93651
            };

    public static List<String> getDefaultExcludes() {
        return Collections.unmodifiableList(Arrays.asList(DEFAULT_EXCLUDES));
    }

    /**
     * Constructor for tests only.
     */
    protected ProjectProperties() {
    }

    /**
     * Default constructor to initialize fields (to get no NPEs). Real values
     * should be applied after creation.
     */
    public ProjectProperties(File projectDir) throws Exception {
        projectRootDir = projectDir;
        projectName = projectDir.getName();
        setSourceRoot(getProjectRoot() + OConsts.DEFAULT_SOURCE + File.separator);
        sourceRootExcludes.addAll(Arrays.asList(DEFAULT_EXCLUDES));
        setTargetRoot(getProjectRoot() + OConsts.DEFAULT_TARGET + File.separator);
        setGlossaryRoot(getProjectRoot() + OConsts.DEFAULT_GLOSSARY + File.separator);
        setWriteableGlossary(getProjectRoot() + OConsts.DEFAULT_GLOSSARY + File.separator + OConsts.DEFAULT_W_GLOSSARY);
        setTMRoot(getProjectRoot() + OConsts.DEFAULT_TM + File.separator);
        setExportTMRoot(getProjectRoot() + File.separator);
        setDictRoot(getProjectRoot() + OConsts.DEFAULT_DICT + File.separator);

        setExportTmLevels(StringUtil.convertToList(OConsts.DEFAULT_EXPORT_TM_LEVELS.toLowerCase()));

        setSentenceSegmentingEnabled(true);
        setSupportDefaultTranslations(true);
        setRemoveTags(false);

        setSourceLanguage("AR-LB");
        setTargetLanguage("UK-UA");

        loadProjectSRX();
        loadProjectFilters();

        setSourceTokenizer(PluginUtils.getTokenizerClassForLanguage(getSourceLanguage()));
        setTargetTokenizer(PluginUtils.getTokenizerClassForLanguage(getTargetLanguage()));
    }

    /** Returns The Target (Compiled) Files Directory */
    public String getTargetRoot() {
        return targetDir.getAsString();
    }

    /** Sets The Target (Compiled) Files Directory */
    public void setTargetRoot(String targetRoot) {
        targetDir.setRelativeOrAbsolute(targetRoot);
    }

    public ProjectPath getTargetDir() {
        return targetDir;
    }

    /** Returns The Glossary Files Directory */
    public String getGlossaryRoot() {
        return glossaryDir.getAsString();
    }

    /** Sets The Glossary Files Directory */
    public void setGlossaryRoot(String glossaryRoot) {
        glossaryDir.setRelativeOrAbsolute(glossaryRoot);
    }

    public ProjectPath getGlossaryDir() {
        return glossaryDir;
    }

    public ProjectPath getWritableGlossaryFile() {
        return writableGlossaryFile;
    }
    /** Returns The Glossary File Location */
    public String getWriteableGlossary() {
        return writableGlossaryFile.getAsString();
    }

    /** Returns The Glossary File Directory */
    public String getWriteableGlossaryDir() {
        ProjectPath dir = new ProjectPath(true);
        dir.setRelativeOrAbsolute(writableGlossaryFile.getAsFile().getParent());
        return dir.getAsString();
    }

    /** Sets The Writeable Glossary File Location */
    public void setWriteableGlossary(String writeableGlossaryFile) {
        writableGlossaryFile.setRelativeOrAbsolute(writeableGlossaryFile);
    }

    public boolean isDefaultWriteableGlossaryFile() {
        return computeDefaultWriteableGlossaryFile().equals(writableGlossaryFile.getAsString());
    }

    public String computeDefaultWriteableGlossaryFile() {
        // Default glossary file name depends on where glossaryDir is:
        //  - Inside project folder: glossary.txt
        //  - Outside project folder: ${projectName}-glossary.txt
        String glossaryDir = getGlossaryRoot();
        if (glossaryDir.startsWith(getProjectRoot())) {
            return glossaryDir + OConsts.DEFAULT_W_GLOSSARY;
        } else {
            return glossaryDir + projectName + OConsts.DEFAULT_W_GLOSSARY_SUFF;
        }
    }

    public ProjectPath getTmDir() {
        return tmDir;
    }

    /** Returns The Translation Memory (TMX) Files Directory */
    public String getTMRoot() {
        return tmDir.getAsString();
    }

    /** Returns The Translation Memory (TMX) Files Directory */
    public String getExportTMRoot() {
        return exportTMDir.getAsString();
    }

    /** Sets The Translation Memory (TMX) Files Directory */
    public void setTMRoot(String tmRoot) {
        tmDir.setRelativeOrAbsolute(tmRoot);
    }

    /** Sets The Export Translation Memory (TMX) Files Directory */
    public void setExportTMRoot(String exportTMRoot) {
        exportTMDir.setRelativeOrAbsolute(exportTMRoot);
    }

    /** Returns The Translation Memory (TMX) with translations to other languages Files Directory */
    public String getTMOtherLangRoot() {
        return tmDir.getAsString() + OConsts.DEFAULT_OTHERLANG + '/';
    }

    /** Returns The Translation Memory (TMX) Files Directory for automatically applied files. */
    public String getTMAutoRoot() {
        return tmDir.getAsString() + OConsts.AUTO_TM + '/';
    }
    
    /** Returns The Translation Memory (TMX) Files Directory for automatically enforced files. */
    public String getTMEnforceRoot() {
        return tmDir.getAsString() + OConsts.AUTO_ENFORCE_TM + '/';
    }
    
    /** Returns The Translation Memory (TMX) Files Directory for machine translation files. */
    public String getTMMTRoot() {
        return tmDir.getAsString() + OConsts.MT_TM + '/';
    }
    
    /** Returns The Translation Memory (TMX) Files Directory for files with penalties. */
    public String getTMPenaltyRoot() {
        return tmDir.getAsString() + OConsts.PENALTY_TM + '/';
    }
    
    public ProjectPath getDictDir() {
        return dictDir;
    }

    /** Returns The Dictionaries Files Directory */
    public String getDictRoot() {
        return dictDir.getAsString();
    }

    /** Sets Dictionaries Files Directory */
    public void setDictRoot(String dictRoot) {
        dictDir.setRelativeOrAbsolute(dictRoot);
    }

    public String getDictRootRelative() {
        return dictDir.getAsString();
    }

    /** Returns the name of the Project */
    public String getProjectName() {
        return projectName;
    }

    /** Returns The Project Root Directory */
    public String getProjectRoot() {
        String p = projectRootDir.getPath().replace('\\', '/');
        if (!p.endsWith("/")) {
            p += '/';
        }
        return p;
    }

    public File getProjectRootDir() {
        return projectRootDir;
    }

    /** Sets The Project Root Directory. For unit tests only !!! */
    protected void setProjectRoot(String projectRoot) {
        this.projectRootDir = new File(projectRoot);
    }

    /** Returns The Project's Translation Memory (TMX) File */
    public String getProjectInternal() {
        return getProjectRoot() + OConsts.DEFAULT_INTERNAL + '/';
    }

    public File getProjectInternalDir() {
        return new File(projectRootDir, OConsts.DEFAULT_INTERNAL);
    }

    public String getProjectInternalRelative() {
        return OConsts.DEFAULT_INTERNAL + '/';
    }

    /** Returns The Source (to be translated) Files Directory */
    public String getSourceRoot() {
        return sourceDir.getAsString();
    }

    /** Sets The Source (to be translated) Files Directory */
    public void setSourceRoot(String sourceRoot) {
        if (!StringUtil.isEmpty(sourceRoot)) {
            sourceDir.setRelativeOrAbsolute(sourceRoot);
        }
    }

    public void setSourceRootRelative(String sourceRootRelative) {
        if (!StringUtil.isEmpty(sourceRootRelative)) {
            sourceDir.setRelativeOrAbsolute(sourceRootRelative);
        }
    }

    public ProjectPath getSourceDir() {
        return sourceDir;
    }

    public List<String> getSourceRootExcludes() {
        return sourceRootExcludes;
    }

    /**
     * Returns The Source Language (language of the source files) of the Project
     */
    public Language getSourceLanguage() {
        return sourceLanguage;
    }

    /** Sets The Source Language (language of the source files) of the Project */
    public void setSourceLanguage(Language sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    /** Sets The Source Language (language of the source files) of the Project */
    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = new Language(sourceLanguage);
    }

    /**
     * Returns The Target Language (language of the translated files) of the Project
     */
    public Language getTargetLanguage() {
        return targetLanguage;
    }

    /**
     * Sets The Target Language (language of the translated files) of the Project
     */
    public void setTargetLanguage(Language targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    /**
     * Sets The Target Language (language of the translated files) of the Project
     */
    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = new Language(targetLanguage);
    }

    /**
     * Returns the class name of the source language tokenizer for the Project.
     */
    public Class<?> getSourceTokenizer() {
        if (sourceTokenizer == null) {
            Class<?> cls = PluginUtils.getTokenizerClassForLanguage(getSourceLanguage());
            setSourceTokenizer(cls);
        }
        return sourceTokenizer;
    }

    /**
     * Sets the class name of the source language tokenizer for the Project.
     */
    public void setSourceTokenizer(Class<?> sourceTokenizer) {
        this.sourceTokenizer = sourceTokenizer;
    }

    /**
     * Returns the class name of the target language tokenizer for the Project.
     */
    public Class<?> getTargetTokenizer() {
        return targetTokenizer;
    }

    /**
     * Sets the class name of the target language tokenizer for the Project.
     */
    public void setTargetTokenizer(Class<?> targetTokenizer) {
        this.targetTokenizer = targetTokenizer;
    }

    /**
     * Returns whether The Sentence Segmenting is Enabled for this Project. Default, Yes.
     */
    public boolean isSentenceSegmentingEnabled() {
        return sentenceSegmentingEnabled;
    }

    /** Sets whether The Sentence Segmenting is Enabled for this Project */
    public void setSentenceSegmentingEnabled(boolean sentenceSegmentingEnabled) {
        this.sentenceSegmentingEnabled = sentenceSegmentingEnabled;
    }

    /**
     * Sets level(s) of TMs to be exported by project.
     * Accepts three booleans as arguments, corresponding to
     * OmegaT, Level 1 and Level 2
     */
    public void setExportTmLevels(boolean omT, boolean level1, boolean level2) {
        List<String> exportTmLevels = new ArrayList<>();
        if (omT) exportTmLevels.add("omegat");
        if (level1) exportTmLevels.add("level1");
        if (level2) exportTmLevels.add("level2");
        this.exportTmLevels = exportTmLevels;
    }

    /**
     * Sets level(s) of TMs to be exported by project.
     * Accepts list of levels
     */
    public void setExportTmLevels(List<String> levels) {
        boolean omegat = false;
        boolean level1 = false;
        boolean level2 = false;
        for (String level: levels) {
            if ("omegat".equalsIgnoreCase(level)) {
                omegat = true;
                continue;
            }
            if ("level1".equalsIgnoreCase(level)) {
                level1 = true;
                continue;
            }
            if ("level2".equalsIgnoreCase(level)) {
                level2 = true;
            }
        }
        this.setExportTmLevels(omegat, level1, level2);
    }

    public List<String> getExportTmLevels() {
        return this.exportTmLevels;
    }

    /** Returns whether a given TM level will be exported **/
    public boolean isExportTm(String level) {
        return this.exportTmLevels.contains(level.toLowerCase());
    }

    public boolean isSupportDefaultTranslations() {
        return supportDefaultTranslations;
    }

    public void setSupportDefaultTranslations(boolean supportDefaultTranslations) {
        this.supportDefaultTranslations = supportDefaultTranslations;
    }

    public boolean isRemoveTags() {
        return removeTags;
    }

    public void setRemoveTags(boolean removeTags) {
        this.removeTags = removeTags;
    }

    public boolean hasRepositories() {
        return repositories != null && !repositories.isEmpty();
    }

    public List<RepositoryDefinition> getRepositories() {
        return repositories;
    }

    public boolean isTeamProject() {
        if (repositories == null) {
            return false;
        }
        for (RepositoryDefinition repositoryDefinition: repositories) {
            for (RepositoryMapping repositoryMapping: repositoryDefinition.getMapping()) {
                if ("".equals(repositoryMapping.getLocal()) || "/".equals(repositoryMapping.getLocal())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setRepositories(List<RepositoryDefinition> repositories) {
        this.repositories = repositories;
    }

    public SRX getProjectSRX() {
        return projectSRX;
    }

    public void setProjectSRX(SRX projectSRX) {
        this.projectSRX = projectSRX;
    }

    /**
     * Loads segmentation.conf if found in the /omegat folder of the project
     */
    public void loadProjectSRX() {
        this.projectSRX = SRX.loadFromDir(new File(getProjectInternal()));
    }

    public Filters getProjectFilters() {
        return projectFilters;
    }

    public void setProjectFilters(Filters projectFilters) {
        this.projectFilters = projectFilters;
    }

    /**
     * Loads filters.xml if found in the /omegat filter of the project
     * @throws IOException
     */
    public void loadProjectFilters() throws IOException {
        projectFilters = FilterMaster.loadConfig(new File(getProjectInternal(), FilterMaster.FILE_FILTERS));
    }

    public String getExternalCommand() {
        return externalCommand;
    }

    public void setExternalCommand(String command) {
        this.externalCommand = command;
    }

    public boolean isProjectValid() {
        boolean returnValue;
        try {
            verifyProject();
            returnValue = true;
        } catch (ProjectException ex) {
            returnValue = false;
        }
        return returnValue;
    }

    /**
     * Verify project and print any problems.
     */
    public void verifyProject() throws ProjectException {
        //
        // Now check whether these directories are where they're suposed to be.
        //
        String srcDir = getSourceRoot();
        File src = new File(srcDir);
        if (!src.exists()) {
            throw new ProjectException(StringUtil.format(OStrings.getString("PROJECT_SOURCE_FOLDER"), srcDir));
        }
        //
        String tgtDir = getTargetRoot();
        File tgt = new File(tgtDir);
        if (!tgt.exists()) {
            throw new ProjectException(StringUtil.format(OStrings.getString("PROJECT_TARGET_FOLDER"), tgtDir));
        }
        //
        String glsDir = getGlossaryRoot();
        File gls = new File(glsDir);
        if (!gls.exists()) {
            throw new ProjectException(StringUtil.format(OStrings.getString("PROJECT_GLOSSARY_FOLDER"), glsDir));
        }
        String wGlsDir = getWriteableGlossaryDir();
        if (!wGlsDir.contains(getGlossaryRoot())) {
            throw new ProjectException(StringUtil.format(OStrings.getString("PROJECT_W_GLOSSARY"), glsDir));
        }

        //
        String tmxDir = getTMRoot();
        File tmx = new File(tmxDir);
        if (!tmx.exists()) {
            throw new ProjectException(StringUtil.format(OStrings.getString("PROJECT_TM_FOLDER"), tmxDir));
        }

        String exportTMXDir = getExportTMRoot();
        File exportTMX = new File(exportTMXDir);
        if (!exportTMX.exists()) {
            throw new ProjectException(StringUtil.format(OStrings.getString("PROJECT_EXPORT_TM_FOLDER"), exportTMXDir));
        }

        // Dictionary folder is always created automatically when it does not exist, for ascending
        // compatibility reasons.
        // There is no exception handling when a failure occurs during folder creation.
        //
        File dict = new File(getDictRoot());
        if (!dict.exists()) {
            if (getDictRoot().equals(getProjectRoot() + OConsts.DEFAULT_DICT + '/')) {
                dict.mkdirs();
            }
        }
    }

    public void autocreateDirectories() {
        autocreateOneDirectory(getProjectInternalDir());
        autocreateOneDirectory(sourceDir.getAsFile());
        autocreateOneDirectory(targetDir.getAsFile());
        autocreateOneDirectory(glossaryDir.getAsFile());
        autocreateOneDirectory(tmDir.getAsFile());
        autocreateOneDirectory(exportTMDir.getAsFile());
        autocreateOneDirectory(dictDir.getAsFile());
    }

    private void autocreateOneDirectory(File dir) {
        if (!dir.exists()) {
            Log.logInfoRB("CT_AUTOCREATE_DIRECTORY", dir);
            dir.mkdirs();
        }
    }

    private String projectName;
    private final List<String> sourceRootExcludes = new ArrayList<String>();
    private List<RepositoryDefinition> repositories;

    private Language sourceLanguage;
    private Language targetLanguage;

    private Class<?> sourceTokenizer;
    private Class<?> targetTokenizer;

    private boolean sentenceSegmentingEnabled;
    private boolean supportDefaultTranslations;
    private boolean removeTags;
    private List<String> exportTmLevels;

    private SRX projectSRX;
    private Filters projectFilters;

    private String externalCommand;

    protected File projectRootDir;
    protected ProjectPath sourceDir = new ProjectPath(true);
    protected ProjectPath targetDir = new ProjectPath(true);
    protected ProjectPath glossaryDir = new ProjectPath(true);
    protected ProjectPath writableGlossaryFile = new ProjectPath(false);
    protected ProjectPath tmDir = new ProjectPath(true);
    protected ProjectPath exportTMDir = new ProjectPath(true);
    protected ProjectPath dictDir = new ProjectPath(true);

    /**
     * Class for support project path functionality, like relative path, etc.
     */
    public final class ProjectPath {
        private final boolean isDirectory;
        private File fs;
        /** Null if path is not under project root */
        private String underRoot;

        /**
         * @param isDirectory
         *            true if directory(i.e. should be ended by '/'), false if file
         */
        public ProjectPath(boolean isDirectory) {
            this.isDirectory = isDirectory;
        }

        /**
         * path is directory(or file) as declared in the omegat.project, but not __DEFAULT__. I.e. caller can
         * send something like "/some/project/source", or "source", or "source/".
         *
         * Absolute paths from Windows will be treated as relative on Linux/MacOS, and vice versa.
         */
        public void setRelativeOrAbsolute(String path) {
            underRoot = null;
            if (FileUtil.isRelative(path)) {
                Path p = projectRootDir == null ? Paths.get(path) : projectRootDir.toPath().resolve(path);
                fs = p.normalize().toFile();
                if (!path.contains("..")) {
                    underRoot = path.replace('\\', '/');
                    if (isDirectory && !underRoot.endsWith("/")) {
                        underRoot += '/';
                    }
                }
            } else {
                fs = new File(FileUtil.absoluteForSystem(path));
                // probably relative?
                try {
                    String p = FileUtil.computeRelativePath(projectRootDir, fs);
                    if (!p.contains("..")) {
                        underRoot = p.replace('\\', '/');
                        if (isDirectory && !underRoot.endsWith("/")) {
                            underRoot += '/';
                        }
                    }
                } catch (IOException ex) {
                    // absolute
                }
            }
        }

        public File getAsFile() {
            return fs;
        }

        public String getAsString() {
            String p = fs.getPath().replace('\\', '/');
            if (isDirectory && !p.endsWith("/")) {
                p += '/';
            }
            return p;
        }

        public boolean isUnderRoot() {
            return underRoot != null;
        }

        /**
         * Returns path relative to project root with '/' at the end for directories, or null if directory outside
         * of project.
         */
        public String getUnderRoot() {
            return underRoot;
        }
    }

}
