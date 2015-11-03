/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Hiroshi Miura
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

package org.omegat.core.dictionaries;

/**
 *
 * @author Hiroshi Miura
 */
public class StarDictData {
    private String article = null;
    private int len = 0;
    private int off = 0;

    public StarDictData(String articleData){
        this.article = articleData;
    }

    public StarDictData(int offset, int len){
        this.len = len;
        this.off = offset;
    }

    /*
     * @return String article
     *         null when article never load yet.
    */
    public String getArticle(){
        return this.article;
    }

    /*
     * @param String article
    */
    public void setArticle(String text) {
        this.article = text;
    }

    public int length(){
        return this.len;
    }

    public int offset() {
        return this.off;
    }
}
