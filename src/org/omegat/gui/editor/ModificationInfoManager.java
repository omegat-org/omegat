/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2012 Thomas Cordonnier, Aaron Madlon-Kay
               2013-2014 Aaron Madlon-Kay
               2014 Alex Buloichik, Piotr Kulik
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
package org.omegat.gui.editor;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.omegat.core.data.TMXEntry;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.VarExpansion;

/**
 * This class is used to generate modification info for a TMXEntry to a text
 * visible above source segment according to the given template containing
 * variables.
 *
 * @author Thomas CORDONNIER
 * @author Aaron Madlon-Kay
 * @author Alex Buloichik
 * @author Piotr Kulik
 */

public final class ModificationInfoManager {

    private ModificationInfoManager() {
    }

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

    private static final String[] MOD_INFO_VARIABLES = {
        VAR_CREATION_ID, VAR_CREATION_DATE, VAR_CREATION_DATE_COUNTRY,
        VAR_CREATION_DATE_SHORT, VAR_CREATION_DATE_SHORT_COUNTRY,
        VAR_CREATION_TIME, VAR_CREATION_TIME_COUNTRY,
        VAR_CREATION_TIME_SHORT, VAR_CREATION_TIME_SHORT_COUNTRY,
        VAR_CHANGED_ID, VAR_CHANGED_DATE, VAR_CHANGED_DATE_COUNTRY,
        VAR_CHANGED_DATE_SHORT, VAR_CHANGED_DATE_SHORT_COUNTRY,
        VAR_CHANGED_TIME, VAR_CHANGED_TIME_COUNTRY,
        VAR_CHANGED_TIME_SHORT, VAR_CHANGED_TIME_SHORT_COUNTRY
    };

    public static List<String> getModInfoVariables() {
        return Collections.unmodifiableList(Arrays.asList(MOD_INFO_VARIABLES));
    }

    private static final String[] MOD_INFO_VARIABLES_NO_DATE = {
        VAR_CREATION_ID, VAR_CHANGED_ID
    };

    public static List<String> getModInfoVariablesNoDate() {
        return Collections.unmodifiableList(Arrays.asList(MOD_INFO_VARIABLES_NO_DATE));
    }

    public static final String DEFAULT_TEMPLATE =
            "${creationId} - ${changedId} ${changedDate} ${changedTime}";

    public static final String DEFAULT_TEMPLATE_NO_DATE =
            "${creationId} - ${changedId}";

    // ------------------------------ variables --------------------
    private static ModificationInfoVarExpansion defaultTemplateVarExpansion;
    private static ModificationInfoVarExpansion defaultTemplateNdVarExpansion;

    static {
        reset();
    }

    public static void reset() {
        defaultTemplateVarExpansion = new ModificationInfoVarExpansion(
                Preferences.getPreferenceDefault(Preferences.VIEW_OPTION_MOD_INFO_TEMPLATE, DEFAULT_TEMPLATE));
        defaultTemplateNdVarExpansion = new ModificationInfoVarExpansion(Preferences.getPreferenceDefault(
                Preferences.VIEW_OPTION_MOD_INFO_TEMPLATE_WO_DATE, DEFAULT_TEMPLATE_NO_DATE));
    }

    public static String apply(TMXEntry trans) {
        if (trans.changeDate == 0) {
            return defaultTemplateNdVarExpansion.apply(trans);
        } else {
            return defaultTemplateVarExpansion.apply(trans);
        }
    }

    public static class ModificationInfoVarExpansion extends VarExpansion<TMXEntry> {

        private final DateTimeFormatter dateFormat;
        private final DateTimeFormatter dateFormatCountry;
        private final DateTimeFormatter dateFormatShort;
        private final DateTimeFormatter dateFormatShortCountry;
        private final DateTimeFormatter timeFormat;
        private final DateTimeFormatter timeFormatCountry;
        private final DateTimeFormatter timeFormatShort;
        private final DateTimeFormatter timeFormatShortCountry;

        public ModificationInfoVarExpansion(String template) {
            super(template);

            ZoneId zone = ZoneId.systemDefault();
            Locale defaultLocale = Locale.getDefault();
            dateFormat = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(defaultLocale)
                    .withZone(zone);
            dateFormatShort = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(defaultLocale)
                    .withZone(zone);
            timeFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(defaultLocale)
                    .withZone(zone);
            timeFormatShort = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(defaultLocale)
                    .withZone(zone);

            Locale countryLocale = defaultLocale;
            Locale[] locales = Locale.getAvailableLocales();
            for (Locale l : locales) {
                if (l.getCountry().equals(defaultLocale.getCountry())) {
                    countryLocale = l;
                    break;
                }
            }
            dateFormatCountry = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(countryLocale)
                    .withZone(zone);
            dateFormatShortCountry = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                    .withLocale(countryLocale).withZone(zone);
            timeFormatCountry = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(countryLocale)
                    .withZone(zone);
            timeFormatShortCountry = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
                    .withLocale(countryLocale).withZone(zone);
        }

        @Override
        public String expandVariables(TMXEntry trans) {
            Instant creationDate = Instant.ofEpochMilli(trans.creationDate);
            Instant changeDate = Instant.ofEpochMilli(trans.changeDate);

            // do not modify template directly, so that we can reuse for another change
            String localTemplate = this.template;

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
