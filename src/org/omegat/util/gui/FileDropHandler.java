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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * Handles receiving dragged-and-dropped files. Override {@link #handleFiles(List)}
 * as appropriate. Optionally wraps another {@link TransferHandler} so as to not
 * clobber built-in functionality. If 
 * <a href="http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4830695">JDK-4830695</a>
 * is ever resolved then this wrapping may not be necessary.
 * 
 * @author Aaron Madlon-Kay
 */
@SuppressWarnings("serial")
public abstract class FileDropHandler extends TransferHandler {

    private static final Logger LOGGER = Logger.getLogger(FileDropHandler.class.getName());

    private TransferHandler wrappedHandler;

    public FileDropHandler wrapExisting(JComponent comp) {
    	this.wrappedHandler = comp.getTransferHandler();
    	return this;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        return  isFileData(support) || (wrappedHandler != null && wrappedHandler.canImport(support));
    }
    
    protected boolean isFileData(TransferHandler.TransferSupport support) {
    	return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
    }
    
    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        
        if (!support.isDrop() || !isFileData(support)) {
        	return wrappedHandler != null && wrappedHandler.importData(support);
        }

        List<File> files = extractFiles(support);
        if (files.isEmpty()) {
            return false;
        }
        return handleFiles(files);
    }
    
    @SuppressWarnings("unchecked")
    protected List<File> extractFiles(TransferHandler.TransferSupport support) {
        try {
            Object payload = support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
            if (!(payload instanceof List<?>)) {
              return Collections.EMPTY_LIST;
            }
            return (List<File>) payload;
        } catch (UnsupportedFlavorException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.EMPTY_LIST;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            return Collections.EMPTY_LIST;
        }
    }
    
    protected abstract boolean handleFiles(List<File> files);
    
    // Delegating-only methods
    
    @Override
    public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
    	return wrappedHandler == null ? super.canImport(comp, transferFlavors)
    			:wrappedHandler.canImport(comp, transferFlavors);
    }
    
    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
    	if (wrappedHandler == null) {
    		super.exportAsDrag(comp, e, action);
    	} else {
    		wrappedHandler.exportAsDrag(comp, e, action);
    	}
    }
    
    @Override
    public void exportToClipboard(JComponent comp, Clipboard clip, int action)
    		throws IllegalStateException {
    	if (wrappedHandler == null) {
    		super.exportToClipboard(comp, clip, action);
    	} else {
    		wrappedHandler.exportToClipboard(comp, clip, action);
    	}
    }
    
    @Override
    public int getSourceActions(JComponent c) {
    	return wrappedHandler == null ? super.getSourceActions(c)
    			: wrappedHandler.getSourceActions(c);
    }
    
    @Override
    public Icon getVisualRepresentation(Transferable t) {
    	return wrappedHandler == null ? super.getVisualRepresentation(t)
    			:wrappedHandler.getVisualRepresentation(t);
    }
    
    @Override
    public boolean importData(JComponent comp, Transferable t) {
    	return wrappedHandler == null ? super.importData(comp, t)
    			: wrappedHandler.importData(comp, t);
    }
    
    @Override
    protected Transferable createTransferable(JComponent c) {
        if (wrappedHandler != null) {
            try {
                Method m = wrappedHandler.getClass().getDeclaredMethod("createTransferable", JComponent.class);
                m.setAccessible(true);
                return (Transferable) m.invoke(wrappedHandler, c);
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        return super.createTransferable(c);
    }
    
    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if (wrappedHandler != null) {
            try {
                Method m = wrappedHandler.getClass().getDeclaredMethod("exportDone", JComponent.class, Transferable.class, Integer.class);
                m.setAccessible(true);
                m.invoke(wrappedHandler, source, data, action);
                return;
            } catch (SecurityException e) {
            } catch (NoSuchMethodException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
            }
        }
        super.exportDone(source, data, action);
    }
}
