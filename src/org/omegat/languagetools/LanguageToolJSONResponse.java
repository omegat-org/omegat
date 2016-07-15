/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
 with fuzzy matching, translation memory, keyword search,
 glossaries, and translation leveraging into updated projects.

 Copyright (C) 2016 Lev Abashkin
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

package org.omegat.languagetools;

import java.math.BigDecimal;
import java.util.ArrayList;

public class LanguageToolJSONResponse {

    private final static String currentApiVersion = "1";

    class Software {
        public String name;
        public String version;
        public String buildDate;
        public String apiVersion;
        public String status;
    }

    class Language {
        public String name;
        public String code;
    }

    class Match {

        class Value {
            public String value;
        }

        class Context {
            public String text;
            public BigDecimal offset;
            public BigDecimal length;
        }

        class Rule {

            class Category {
                public String id;
                public String name;
            }

            public String id;
            public String subId;
            public String description;
            public ArrayList<Value> urls;
            public String issueType;
            public Category category;

            public Rule() {
                this.urls = new ArrayList<>();
                this.category = new Category();
            }
        }

        public String message;
        public String shortMessage;
        public BigDecimal offset;
        public BigDecimal length;
        public ArrayList<Value> replacements;
        public Context context;
        public Rule rule;

        public Match() {
            this.replacements = new ArrayList<>();
            this.context = new Context();
            this.rule = new Rule();
        }
    }

    public Software software;
    public Language language;
    public ArrayList<Match> matches;

    public LanguageToolJSONResponse() {
        this.software = new Software();
        this.language = new Language();
        this.matches = new ArrayList<>();
    }
}