/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2025 Hiroshi Miura
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
package org.omegat.convert.segmentation;

import org.omegat.core.segmentation.SRX;
import org.omegat.util.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ExceptionListener;
import java.beans.XMLDecoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hiroshi Miura
 */
public class SegmentationConfMigrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SegmentationConfMigrator.class);

    public static void main(String[] args) {
        String targetDir = ".";
        Path confFilePath = Paths.get(targetDir).resolve(SRX.CONF_SENTSEG);
        Path srxFilePath = Paths.get(targetDir).resolve(SRX.SRX_SENTSEG);
        ValidationResult validationResult = checkConfigFile(confFilePath);
        if (!validationResult.isValid()) {
            LOGGER.error(validationResult.getErrorMessage());
            System.exit(2);
        }
        SRX srx = convertToSrx(confFilePath, srxFilePath);
        if (srx == null) {
            System.exit(1);
        }
    }

    static ValidationResult checkConfigFile(Path configPath) {
        if (!configPath.toFile().exists()) {
            return ValidationResult.failure("File " + SRX.CONF_SENTSEG + " is not found!");
        }
        SegmentationConfValidator validator = new SegmentationConfValidator(configPath);
        return validator.validate();
    }

    static SRX convertToSrx(Path configPath, Path srxFilePath) {
        try {
            if (srxFilePath.toFile().exists()) {
                Files.delete(srxFilePath);
            }
            File srxParent = srxFilePath.getParent().toFile();
            SRX srx = loadConfFile(configPath.toFile());
            SRX.saveToSrx(srx, srxParent);
            return srx;
        } catch (Exception e) {
            LOGGER.error("Error occurred during conversion!", e);
        }
        return null;
    }

    /**
     * Loads segmentation rules from an XML file.
     */
    private static SRX loadConfFile(File configFile) throws IOException {
        SRX res;
        MyExceptionListener myel = new MyExceptionListener();
        // Need to use the vulnerable java.beans.XMLDecoder method for compatibility.
        try (XMLDecoder xmldec = new XMLDecoder(new FileInputStream(configFile), null, myel, SRX.class.getClassLoader())) {
            res = (SRX) xmldec.readObject();
        }
        if (myel.isExceptionOccured()) {
            StringBuilder sb = new StringBuilder();
            for (Exception ex : myel.getExceptionsList()) {
                sb.append("    ");
                sb.append(ex);
                sb.append("\n");
            }
            throw new IllegalStateException(sb.toString());
        }
        return res;
    }

    /**
     * My Own Class to listen to exceptions, occured while loading filters
     * configuration.
     */
    static class MyExceptionListener implements ExceptionListener {
        private final List<Exception> exceptionsList = new ArrayList<>();
        private boolean exceptionOccured = false;

        public void exceptionThrown(Exception e) {
            exceptionOccured = true;
            exceptionsList.add(e);
        }

        /**
         * Returns whether any exceptions occured.
         */
        public boolean isExceptionOccured() {
            return exceptionOccured;
        }

        /**
         * Returns the list of occured exceptions.
         */
        public List<Exception> getExceptionsList() {
            return exceptionsList;
        }
    }
}
