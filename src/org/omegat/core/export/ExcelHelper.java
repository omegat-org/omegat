/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 OmegaT contributors
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

package org.omegat.core.export;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around Apache POI for reading and writing .xlsx bilingual files.
 *
 * <p>
 * Apache POI is an optional dependency. This class uses direct imports so that
 * a build error occurs at compile time only if POI is absent from the
 * classpath.
 * At runtime the {@link ExportBilingualHandler} catches any exception and
 * reports
 * it as "Excel support not available".
 *
 * <p>
 * If you do not need Excel support, this file can be excluded from the build
 * and
 * the CSV functions in {@link ExportBilingualHandler} will still work fully.
 *
 * @author OmegaT contributors
 */
final class ExcelHelper {

    private ExcelHelper() {
        // Utility class
    }

    /**
     * Writes an ordered source→target map to an .xlsx workbook.
     *
     * @param outputFile destination file
     * @param segments   ordered map of source text → translation text (may be empty
     *                   string)
     * @throws IOException                  on I/O error
     * @throws ReflectiveOperationException if Apache POI is not on the classpath
     */
    static void write(File outputFile, Map<String, String> segments)
            throws IOException, ReflectiveOperationException {

        // Use Apache POI – compile-time dependency (poi-ooxml)
        try (org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Bilingual Export");

            // Bold header style
            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            // Header row
            org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
            org.apache.poi.ss.usermodel.Cell srcHeader = headerRow.createCell(0);
            srcHeader.setCellValue(ExportBilingualHandler.HEADER_SOURCE);
            srcHeader.setCellStyle(headerStyle);
            org.apache.poi.ss.usermodel.Cell tgtHeader = headerRow.createCell(1);
            tgtHeader.setCellValue(ExportBilingualHandler.HEADER_TARGET);
            tgtHeader.setCellStyle(headerStyle);

            // Data rows
            int rowIndex = 1;
            for (Map.Entry<String, String> entry : segments.entrySet()) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(entry.getKey());
                row.createCell(1).setCellValue(entry.getValue() != null ? entry.getValue() : "");
            }

            // Auto-size columns for readability
            sheet.autoSizeColumn(0);
            sheet.autoSizeColumn(1);

            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * Reads an .xlsx workbook and returns data rows (header excluded) as
     * [source, target] string pairs.
     *
     * @param inputFile source file
     * @return list of [source, target] arrays
     * @throws IOException                  on I/O error
     * @throws ReflectiveOperationException if Apache POI is not on the classpath
     */
    static List<String[]> read(File inputFile)
            throws IOException, ReflectiveOperationException {

        List<String[]> rows = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(inputFile);
                org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook(
                        fis)) {

            org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(0);

            boolean firstRow = true;
            for (org.apache.poi.ss.usermodel.Row row : sheet) {
                if (firstRow) {
                    firstRow = false; // skip header
                    continue;
                }
                String source = getCellString(row, 0);
                String target = getCellString(row, 1);
                rows.add(new String[] { source, target });
            }
        }
        return rows;
    }

    private static String getCellString(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col,
                org.apache.poi.ss.usermodel.Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Format numeric cells as plain strings to avoid ".0" suffixing
                return org.apache.poi.ss.usermodel.DataFormatter.class
                        .equals(org.apache.poi.ss.usermodel.DataFormatter.class)
                                ? String.valueOf((long) cell.getNumericCellValue())
                                : cell.toString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
