/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2009 Alex Buloichik
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

package org.omegat.util.gui;

/**
 * Some utilities for display text tables with aligning.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
public class TextUtil {

    /**
     * Draw text table with columns align.
     * 
     * @param columnHeaders
     *            column headers
     * @param table
     *            table data
     * @return text
     */
    public static String showTextTable(String[] columnHeaders, String[][] table, boolean[] alignRight) {
        StringBuilder out = new StringBuilder();

        // calculate max column size
        int maxColSize[] = new int[columnHeaders.length];
        for (int c = 0; c < columnHeaders.length; c++) {
            maxColSize[c] = columnHeaders[c].length();
        }
        for (int r = 0; r < table.length; r++) {
            for (int c = 0; c < table[r].length; c++) {
                maxColSize[c] = Math.max(maxColSize[c], table[r][c].length());
            }
        }

        for (int c = 0; c < columnHeaders.length; c++) {
            appendField(out, columnHeaders[c], maxColSize[c], alignRight[c]);
        }
        out.append('\n');
        for (int r = 0; r < table.length; r++) {
            for (int c = 0; c < table[r].length; c++) {
                appendField(out, table[r][c], maxColSize[c], alignRight[c]);
            }
            out.append('\n');
        }
        return out.toString();
    }

    /**
     * Output field with specified length.
     * 
     * @param out
     *            output stream
     * @param data
     *            field data
     * @param colSize
     *            field size
     */
    public static void appendField(StringBuilder out, String data, int colSize, boolean alignRight) {
        if (!alignRight) {
            out.append(data);
        }
        for (int i = data.length(); i < colSize; i++) {
            out.append(' ');
        }
        if (alignRight) {
            out.append(data);
        }
        out.append("\t");
    }
}
