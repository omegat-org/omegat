/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay, Yu Tang
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

package org.omegat.core.data;

import java.util.ArrayList;
import java.util.Map.Entry;
import org.omegat.core.Core;
import org.omegat.util.StringUtil;

import org.omegat.util.VarExpansion;

/**
 * Expand variables representing project properties.
 * Intended for use in preparing external commands for execution.
 * 
 * @author Aaron Madlon-Kay
 * @author Yu Tang
 */
public class CommandVarExpansion extends VarExpansion<ProjectProperties> {

    // ------------------------------ definitions -------------------
    
    public static final String PROJECT_NAME = "${projectName}";
    public static final String PROJECT_ROOT = "${projectRoot}";
    public static final String SOURCE_ROOT = "${sourceRoot}";
    public static final String TARGET_ROOT = "${targetRoot}";
    public static final String GLOSSARY_ROOT = "${glossaryRoot}";
    public static final String WRITABLE_GLOSSARY_FILE = "${glossaryFile}";
    public static final String TM_ROOT = "${tmRoot}";
    public static final String TM_AUTO_ROOT = "${tmAutoRoot}";
    public static final String DICT_ROOT = "${dictRoot}";
    public static final String TM_OTHER_LANG_ROOT = "${tmOtherLangRoot}";
    public static final String SOURCE_LANGUAGE = "${sourceLang}";
    public static final String TARGET_LANGUAGE = "${targetLang}";
    
    public static final String[] COMMAND_VARIABLES;
    
    public CommandVarExpansion(String template) {
        super(template);
    }
    
    @Override
    public String expandVariables(ProjectProperties props) {
        String localTemplate = this.template;
        localTemplate = localTemplate.replace(PROJECT_NAME, props.getProjectName());
        localTemplate = localTemplate.replace(PROJECT_ROOT, props.getProjectRoot());
        localTemplate = localTemplate.replace(SOURCE_ROOT, props.getSourceRoot());
        localTemplate = localTemplate.replace(TARGET_ROOT, props.getTargetRoot());
        localTemplate = localTemplate.replace(GLOSSARY_ROOT, props.getGlossaryRoot());
        localTemplate = localTemplate.replace(WRITABLE_GLOSSARY_FILE, props.getWriteableGlossary());
        localTemplate = localTemplate.replace(TM_ROOT, props.getTMRoot());
        localTemplate = localTemplate.replace(TM_AUTO_ROOT, props.getTMAutoRoot());
        localTemplate = localTemplate.replace(DICT_ROOT, props.getDictRoot());
        localTemplate = localTemplate.replace(TM_OTHER_LANG_ROOT, props.getTMOtherLangRoot());
        localTemplate = localTemplate.replace(SOURCE_LANGUAGE, props.getSourceLanguage().getLanguage());
        localTemplate = localTemplate.replace(TARGET_LANGUAGE, props.getTargetLanguage().getLanguage());
        for (Entry<String, String> e : System.getenv().entrySet()) {
            localTemplate = localTemplate.replace(fixEnvarName(e.getKey()), e.getValue());
        }
        
        String currentFile = Core.getEditor().getCurrentFile();
        if (!StringUtil.isEmpty(currentFile)) {
            String sourceRoot = props.getSourceRoot();
            localTemplate = expandFileName(localTemplate, sourceRoot + currentFile, sourceRoot);
        }
        
        return localTemplate;
    }
    
    private static String fixEnvarName(String varname) {
        return String.format("${%s}", varname);
    }
    
    static {
        ArrayList<String> vars = new ArrayList<String>();
        vars.add(PROJECT_NAME);
        vars.add(PROJECT_ROOT);
        vars.add(SOURCE_ROOT);
        vars.add(TARGET_ROOT);
        vars.add(GLOSSARY_ROOT);
        vars.add(WRITABLE_GLOSSARY_FILE);
        vars.add(TM_ROOT);
        vars.add(TM_AUTO_ROOT);
        vars.add(DICT_ROOT);
        vars.add(TM_OTHER_LANG_ROOT);
        vars.add(SOURCE_LANGUAGE);
        vars.add(TARGET_LANGUAGE);
        vars.add(VarExpansion.VAR_FILE_PATH);
        vars.add(VarExpansion.VAR_FILE_SHORT_PATH);
        vars.add(VarExpansion.VAR_FILE_NAME);
        vars.add(VarExpansion.VAR_FILE_NAME_ONLY);
        vars.add(VarExpansion.VAR_FILE_EXTENSION);
        for (Entry<String, String> e : System.getenv().entrySet()) {
            vars.add(fixEnvarName(e.getKey()));
        }
        
        COMMAND_VARIABLES = vars.toArray(new String[0]);
    }
}
