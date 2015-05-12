/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2015 Aaron Madlon-Kay
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
package org.omegat.util.gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.TransferHandler;

/**
 *
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public abstract class FileDropHandler extends TransferHandler {

    private static final Logger LOGGER = Logger.getLogger(FileDropHandler.class.getName());

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }
    
    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        
        List<File> files;
        try {
            Object payload = support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if (!(payload instanceof List<?>)) {
              return false;
            }
            files = (List<File>) payload;
        } catch (UnsupportedFlavorException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return false;
        }
        
        if (files.isEmpty()) {
            return false;
        }
        
        return handleFiles(files);
    }
    
    protected abstract boolean handleFiles(List<File> files);
}
