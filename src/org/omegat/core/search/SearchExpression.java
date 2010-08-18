/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Henry Pijffers
               2009 Didier Briel
               2010 Martin Fleurke, Antonio Vilei
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
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.core.search;

/**
 * Storage for what to search for (search text and options).
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Henry Pijffers
 * @author Didier Briel
 * @author Martin Fleurke
 * @author Antonio Vilei
 */
public class SearchExpression {

    /**
     * Creates a new search expression based on a given search text.
     * @param text The text to search for
     */
    public SearchExpression(String text)
    {
        this.text = text;

        // set default search conditions
        this.rootDir = null;
        this.recursive = true;
        this.exact = true;
        this.keyword = false;
        this.regex = false;
        this.caseSensitive = false;
        this.tm = true;
        this.allResults = false;
        this.searchSource = true;
        this.searchTarget = true;
        this.searchAuthor = false;
        this.author = "";
        this.searchDateAfter = false;
        this.dateAfter = 0;
        this.searchDateBefore = false;
        this.dateBefore = 0;

    }

    /**
     * Creates a new search expression based on specified search text and options.
     * @param text The text to search for
     * @param rootDir The folder to search in
     * @param recursive Allow searching in subfolders of rootDir
     * @param exact Search for a substring, including wildcards (*?)
     * @param keyword Search for keywords, including wildcards (*?)
     * @param regex Search based on regular expressions
     * @param caseSensitive Search case sensitive
     * @param tm Search also in legacy and orphan TM strings
     * @param allResults Include duplicate results
     * @param searchSource Search in source text
     * @param searchTarget Search in target text
     * @param searchAuthor Search for tmx segments modified by author id/name
     * @param author String to search for in TMX attribute modificationId
     * @param searchDateAfter Search for translation segments modified after the given date
     * @param dateAfter The date after which the modification date has to be
     * @param searchDateBefore Search for translation segments modified before the given date
     * @param dateBefore The date before which the modification date has to be
     */
    public SearchExpression(String  text,
                            String  rootDir,
                            boolean recursive,
                            boolean exact,
                            boolean keyword,
                            boolean regex,
                            boolean caseSensitive,
                            boolean tm,
                            boolean allResults,
                            boolean searchSource,
                            boolean searchTarget,
                            boolean searchAuthor,
                            String  author,
                            boolean searchDateAfter,
                            long    dateAfter,
                            boolean searchDateBefore,
                            long    dateBefore
                            )
    {
        this.text = text;
        this.rootDir = rootDir;
        this.recursive = recursive;
        this.exact = exact;
        this.keyword = keyword;
        this.regex = regex;
        this.caseSensitive = caseSensitive;
        this.tm = tm;
        this.allResults = allResults;
        this.searchSource = searchSource;
        this.searchTarget = searchTarget;
        this.searchAuthor = searchAuthor;
        this.author = author;
        this.searchDateAfter = searchDateAfter;
        this.dateAfter = dateAfter;
        this.searchDateBefore = searchDateBefore;
        this.dateBefore = dateBefore;
    }

    public String  text;
    public String  rootDir;
    public boolean recursive;
    public boolean exact;
    public boolean keyword;
    public boolean regex;
    public boolean caseSensitive;
    public boolean tm;
    public boolean allResults;
    public boolean searchSource;
    public boolean searchTarget;
    public boolean searchAuthor;
    public String  author;
    public boolean searchDateAfter;
    public long    dateAfter;
    public boolean searchDateBefore;
    public long    dateBefore;
}
