/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Thomas Cordonnier
               2013 Aaron Madlon-Kay
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
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.core.data.CommandVarExpansion;
import org.omegat.core.data.ProjectProperties;

/**
 * This class is used to transform a string by expansion of variables it
 * contains. It can expand:
 * <ul>
 * <li>Entries in Bundle.properties: see {@link #expandBundleEntries(String)}
 * for syntax
 * <li>Variables: see {@link #expandVariables(Object)} for syntax
 * </ul>
 * Here we define variables which depends only on the project. This class should
 * be overriden to define more specific substitutions.
 *
 * @author Thomas CORDONNIER
 * @author Aaron Madlon-Kay
 */
public abstract class VarExpansion<Param> {

    // ------------------------------ definitions -------------------

    public static final String VAR_SOURCE_TEXT = "${sourceText}";
    public static final String VAR_TARGET_TEXT = "${targetText}";
    public static final String VAR_PROJECT_SOURCE_LANG = "${projectSourceLang}";
    public static final String VAR_PROJECT_SOURCE_LANG_CODE = "${projectSourceLangCode}";
    public static final String VAR_PROJECT_TARGET_LANG = "${projectTargetLang}";
    public static final String VAR_PROJECT_TARGET_LANG_CODE = "${projectTargetLangCode}";
    public static final String VAR_FILE_NAME = "${fileName}";
    public static final String VAR_FILE_NAME_ONLY = "${fileNameOnly}";
    public static final String VAR_FILE_EXTENSION = "${fileExtension}";
    public static final String VAR_FILE_PATH = "${filePath}";
    public static final String VAR_FILE_SHORT_PATH = "${fileShortPath}";

    public static final Pattern PATTERN_BUNDLE_ENTRY = Pattern.compile("#\\{([\\w\\.]+?)\\}(\\[.+?\\])*");

    protected String template;

    public VarExpansion(String template) {
        // Optimisation : pre-expand all what is not one-match-dependant

        // Bundle entries
        template = expandBundleEntries(template);

        // Variables
        if (Core.getProject().isProjectLoaded()) {
            ProjectProperties prop = Core.getProject().getProjectProperties();
            template = template.replace(VAR_PROJECT_TARGET_LANG, prop.getTargetLanguage().getLanguage());
            template = template.replace(VAR_PROJECT_TARGET_LANG_CODE,
                    prop.getTargetLanguage().getLanguageCode());
            template = template.replace(VAR_PROJECT_SOURCE_LANG, prop.getSourceLanguage().getLanguage());
            template = template.replace(VAR_PROJECT_SOURCE_LANG_CODE,
                    prop.getSourceLanguage().getLanguageCode());
        }

        this.template = template;
    }

    // ------------------------------ functions -------------------

    /**
     * Replace bundle entries with their translation <br>
     * Format : #{BUNDLE_ENTRY_NAME}[param0][param1][param2]... (parameters can
     * contain expanded variables but not with [ or ])
     * 
     * @param localTemplate
     *            Initial template.
     * @return Expanded template
     */
    protected static String expandBundleEntries(String localTemplate) {
        Matcher matcher;
        while ((matcher = PATTERN_BUNDLE_ENTRY.matcher(localTemplate)).find()) {
            String original = matcher.group();
            String translation = OStrings.getString(matcher.group(1));
            if (!StringUtil.isEmpty(matcher.group(2))) {
                String vars = matcher.group(2);
                List<String> values = new ArrayList<String>();
                matcher = Pattern.compile("\\[(.+?)\\]").matcher(vars);
                while (matcher.find()) {
                    values.add(matcher.group(1));
                }
                translation = MessageFormat.format(translation, values.toArray());
            }
            localTemplate = localTemplate.replace(original, translation);
        }
        return localTemplate;
    }

    /**
     * Expands all variables relating to file name :
     * <ul>
     * <li>${filePath} = full file path
     * <li>${fileShortPath} = file path relative to given root
     * <li>${fileName} = full file name (w/o path but with extension)
     * <li>${fileNameOnly} = file name without extension
     * <li>${fileNameOnly-1}, ${fileNameOnly-2}, ... = filename with 1, 2, ...
     * extensions
     * <li>${fileExtension} = all extensions after '.'
     * <li>${fileExtension-1}, ${fileExtension-2}, ... = ${fileExtension} after
     * removing 1, 2, ... extensions
     * </ul>
     *
     * @param localTemplate
     *            initial template. If null, use instance's template but does
     *            not modify it
     * @param filePaths
     *            path used by variable ${fileShortPath}
     * @param baseDir
     *            base directory to expand against
     * @return Copy of the template with mentioned variables expanded. Other
     *         variables remain unchanged
     */
    public String expandFileNames(String localTemplate, String[] filePaths, String baseDir) {
        if (localTemplate == null) {
            localTemplate = this.template; // copy
        }
        String filePath = filePaths[0];
        String numHint = "";
        if (filePaths.length > 1) {
            numHint = filePath.equals("") ? OStrings.getString("MATCHES_THIS_PROJECT") : "";
            numHint += " "
                    + StringUtil.format(OStrings.getString("MATCHES_MULTI_FILE_HINT"), filePaths.length - 1);
        }
        localTemplate = localTemplate.replace(VAR_FILE_PATH,
                CommandVarExpansion.fixSeparatorChar(filePath + numHint));
        try {
            filePath = Paths.get(baseDir).relativize(Paths.get(filePath)).toString();
        } catch (IllegalArgumentException ex) {
        }
        localTemplate = localTemplate.replace(VAR_FILE_SHORT_PATH, filePath + numHint); // path
                                                                                        // without
                                                                                        // TMRoot
        if (filePath.contains(File.separator)) {
            filePath = filePath.substring(filePath.lastIndexOf(File.separator) + 1);
        }
        localTemplate = localTemplate.replace(VAR_FILE_NAME, filePath + numHint);
        if (filePath.contains(".")) {
            String[] splitName = filePath.split("\\.");
            StringBuilder nameOnlyBuf = new StringBuilder(splitName[0]);
            StringBuilder extensionBuf = new StringBuilder(splitName[splitName.length - 1]);
            localTemplate = localTemplate.replace(VAR_FILE_NAME_ONLY, nameOnlyBuf.toString());
            localTemplate = localTemplate.replace(VAR_FILE_EXTENSION, extensionBuf.toString());
            for (int i = 0; i < splitName.length; i++) {
                localTemplate = localTemplate.replaceAll("\\$\\{fileNameOnly-" + i + "\\}",
                        nameOnlyBuf.toString());
                localTemplate = localTemplate.replaceAll("\\$\\{fileExtension-" + i + "\\}",
                        extensionBuf.toString());
                if (i + 1 < splitName.length) {
                    nameOnlyBuf.append(".").append(splitName[i + 1]);
                    extensionBuf.insert(0, splitName[splitName.length - i - 2] + '.');
                }
            }
        }
        // prevent unexpanded fileName variables in case the file has less
        // extensions than expected
        localTemplate = localTemplate.replaceAll("\\$\\{fileNameOnly(-\\d+)?\\}", filePath);
        localTemplate = localTemplate.replaceAll("\\$\\{fileExtension(-\\d+)?\\}", "");
        return localTemplate;
    }

    public String expandFileName(String localTemplate, String filePath, String baseDir) {
        return expandFileNames(localTemplate, new String[] { filePath }, baseDir);
    }

    public abstract String expandVariables(Param param);

}
