/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Thomas Cordonnier, Aaron Madlon-Kay
               2013-2014 Aaron Madlon-Kay
               2014 Alex Buloichik, Piotr Kulik
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
package org.omegat.gui.editor;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import org.omegat.core.data.TMXEntry;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StaticUtils;
import org.omegat.util.VarExpansion;

/**
 * This class is used to generate modification info for a TMXEntry to a text
 * visible above source segment according to the given template containing variables.
 * 
 * @author Thomas CORDONNIER
 * @author Aaron Madlon-Kay
 * @Alex Buloichik
 * @author Piotr Kulik
 */

public class ModificationInfoManager {

    // ------------------------------ definitions -------------------
    public static final String VAR_CREATION_ID = "${creationId}";
    public static final String VAR_CREATION_DATE = "${creationDate}";
    public static final String VAR_CREATION_DATE_COUNTRY = "${creationDateCountry}";
    public static final String VAR_CREATION_DATE_SHORT = "${creationDateShort}";
    public static final String VAR_CREATION_DATE_SHORT_COUNTRY = "${creationDateShortCountry}";
    public static final String VAR_CREATION_TIME = "${creationTime}";
    public static final String VAR_CREATION_TIME_COUNTRY = "${creationTimeCountry}";
    public static final String VAR_CREATION_TIME_SHORT = "${creationTimeShort}";
    public static final String VAR_CREATION_TIME_SHORT_COUNTRY = "${creationTimeShortCountry}";
    public static final String VAR_CHANGED_ID = "${changedId}";
    public static final String VAR_CHANGED_DATE = "${changedDate}";
    public static final String VAR_CHANGED_DATE_COUNTRY = "${changedDateCountry}";
    public static final String VAR_CHANGED_DATE_SHORT = "${changedDateShort}";
    public static final String VAR_CHANGED_DATE_SHORT_COUNTRY = "${changedDateShortCountry}";
    public static final String VAR_CHANGED_TIME = "${changedTime}";
    public static final String VAR_CHANGED_TIME_COUNTRY = "${changedTimeCountry}";
    public static final String VAR_CHANGED_TIME_SHORT = "${changedTimeShort}";
    public static final String VAR_CHANGED_TIME_SHORT_COUNTRY = "${changedTimeShortCountry}";


    public static final String[] MOD_INFO_VARIABLES = {
        VAR_CREATION_ID, VAR_CREATION_DATE, VAR_CREATION_DATE_COUNTRY,
        VAR_CREATION_DATE_SHORT, VAR_CREATION_DATE_SHORT_COUNTRY,
        VAR_CREATION_TIME, VAR_CREATION_TIME_COUNTRY,
        VAR_CREATION_TIME_SHORT, VAR_CREATION_TIME_SHORT_COUNTRY,
        VAR_CHANGED_ID, VAR_CHANGED_DATE, VAR_CHANGED_DATE_COUNTRY,
        VAR_CHANGED_DATE_SHORT, VAR_CHANGED_DATE_SHORT_COUNTRY,
        VAR_CHANGED_TIME, VAR_CHANGED_TIME_COUNTRY,
        VAR_CHANGED_TIME_SHORT, VAR_CHANGED_TIME_SHORT_COUNTRY
    };

    public static final String[] MOD_INFO_VARIABLES_NO_DATE = {
        VAR_CREATION_ID, VAR_CHANGED_ID
    };

    public static final String DEFAULT_TEMPLATE =
            "${creationId} - ${changedId} ${changedDate} ${changedTime}";

    public static final String DEFAULT_TEMPLATE_NO_DATE =
            "${creationId} - ${changedId}";

    // ------------------------------ variables --------------------
    private static final DateFormat dateFormat;
    private static final DateFormat dateFormatCountry;
    private static final DateFormat dateFormatShort;
    private static final DateFormat dateFormatShortCountry;
    private static final DateFormat timeFormat;
    private static final DateFormat timeFormatCountry;
    private static final DateFormat timeFormatShort;
    private static final DateFormat timeFormatShortCountry;
    private static ModificationInfoVarExpansion defaultTemplate;
    private static ModificationInfoVarExpansion defaultTemplateND;

    /** Private constructor, because this file is singleton */
    static {
        reset();

        Locale defaultLocale = Locale.getDefault();
        dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, defaultLocale);
        dateFormatShort = DateFormat.getDateInstance(DateFormat.SHORT, defaultLocale);
        timeFormat = DateFormat.getTimeInstance(DateFormat.DEFAULT, defaultLocale);
        timeFormatShort = DateFormat.getTimeInstance(DateFormat.SHORT, defaultLocale);

        Locale[] locales = Locale.getAvailableLocales();
        for(Locale l : locales) {
            if (l.getCountry().equals(defaultLocale.getCountry())) {
                defaultLocale = l;
                break;
            }
        }
        dateFormatCountry = DateFormat.getDateInstance(DateFormat.DEFAULT, defaultLocale);
        dateFormatShortCountry = DateFormat.getDateInstance(DateFormat.SHORT, defaultLocale);
        timeFormatCountry = DateFormat.getTimeInstance(DateFormat.DEFAULT, defaultLocale);
        timeFormatShortCountry = DateFormat.getTimeInstance(DateFormat.SHORT, defaultLocale);
    }

    public static void reset() {
        defaultTemplate = new ModificationInfoVarExpansion(
                Preferences.getPreferenceDefault(Preferences.VIEW_OPTION_MOD_INFO_TEMPLATE, DEFAULT_TEMPLATE));
        defaultTemplateND = new ModificationInfoVarExpansion(
                Preferences.getPreferenceDefault(Preferences.VIEW_OPTION_MOD_INFO_TEMPLATE_WO_DATE, DEFAULT_TEMPLATE_NO_DATE));
    }

    public static String apply(TMXEntry trans) {
        if (trans.changeDate == 0) {
            return defaultTemplateND.apply(trans);
        } else {
            return defaultTemplate.apply(trans);
        }
    }
    
    public static class ModificationInfoVarExpansion extends VarExpansion<TMXEntry> {

        public ModificationInfoVarExpansion(String template) {
            super(template);
        }

        @Override
        public String expandVariables(TMXEntry trans) {
            Date creationDate = new Date(trans.creationDate);
            Date changeDate = new Date(trans.changeDate);

            String localTemplate = this.template; // do not modify template directly, so that we can reuse for another change

            localTemplate = localTemplate.replace(VAR_CREATION_ID, trans.creator == null
                    ? OStrings.getString("TF_CUR_SEGMENT_UNKNOWN_AUTHOR") : trans.creator);
            localTemplate = localTemplate.replace(VAR_CREATION_DATE,
                    trans.creationDate == 0 ? "" : dateFormat.format(creationDate));
            localTemplate = localTemplate.replace(VAR_CREATION_DATE_COUNTRY,
                    trans.creationDate == 0 ? "" : dateFormatCountry.format(creationDate));
            localTemplate = localTemplate.replace(VAR_CREATION_DATE_SHORT,
                    trans.creationDate == 0 ? "" : dateFormatShort.format(creationDate));
            localTemplate = localTemplate.replace(VAR_CREATION_DATE_SHORT_COUNTRY,
                    trans.creationDate == 0 ? "" : dateFormatShortCountry.format(creationDate));
            localTemplate = localTemplate.replace(VAR_CREATION_TIME,
                    trans.creationDate == 0 ? "" : timeFormat.format(creationDate));
            localTemplate = localTemplate.replace(VAR_CREATION_TIME_COUNTRY,
                    trans.creationDate == 0 ? "" : timeFormatCountry.format(creationDate));
            localTemplate = localTemplate.replace(VAR_CREATION_TIME_SHORT,
                    trans.creationDate == 0 ? "" : timeFormatShort.format(creationDate));
            localTemplate = localTemplate.replace(VAR_CREATION_TIME_SHORT_COUNTRY,
                    trans.creationDate == 0 ? "" : timeFormatShortCountry.format(creationDate));

            localTemplate = localTemplate.replace(VAR_CHANGED_ID, trans.changer == null
                    ? OStrings.getString("TF_CUR_SEGMENT_UNKNOWN_AUTHOR") : trans.changer);
            localTemplate = localTemplate.replace(VAR_CHANGED_DATE,
                    trans.changeDate == 0 ? "" : dateFormat.format(changeDate));
            localTemplate = localTemplate.replace(VAR_CHANGED_DATE_COUNTRY,
                    trans.changeDate == 0 ? "" : dateFormatCountry.format(changeDate));
            localTemplate = localTemplate.replace(VAR_CHANGED_DATE_SHORT,
                    trans.changeDate == 0 ? "" : dateFormatShort.format(changeDate));
            localTemplate = localTemplate.replace(VAR_CHANGED_DATE_SHORT_COUNTRY,
                    trans.changeDate == 0 ? "" : dateFormatShortCountry.format(changeDate));
            localTemplate = localTemplate.replace(VAR_CHANGED_TIME,
                    trans.changeDate == 0 ? "" : timeFormat.format(changeDate));
            localTemplate = localTemplate.replace(VAR_CHANGED_TIME_COUNTRY,
                    trans.changeDate == 0 ? "" : timeFormatCountry.format(changeDate));
            localTemplate = localTemplate.replace(VAR_CHANGED_TIME_SHORT,
                    trans.changeDate == 0 ? "" : timeFormatShort.format(changeDate));
            localTemplate = localTemplate.replace(VAR_CHANGED_TIME_SHORT_COUNTRY,
                    trans.changeDate == 0 ? "" : timeFormatShortCountry.format(changeDate));

            return localTemplate;
        }

        public String apply(TMXEntry trans) {
            return this.expandVariables(trans);
        }
    }
}
