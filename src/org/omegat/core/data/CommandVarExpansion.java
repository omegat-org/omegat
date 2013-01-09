/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2013 Aaron Madlon-Kay
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
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 **************************************************************************/

package org.omegat.core.data;

import org.omegat.util.VarExpansion;

/**
 * Expand variables representing project properties.
 * Intended for use in preparing external commands for execution.
 * 
 * @author aaron.madlon-kay
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
        
        return localTemplate;
    }
}