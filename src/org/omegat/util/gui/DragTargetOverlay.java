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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TooManyListenersException;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.omegat.gui.main.MainWindow;

/**
 * @author Aaron Madlon-Kay
 */
public class DragTargetOverlay {
    
    private static int MARGIN = 0;

    public static void apply(final JComponent comp, final IDropInfo info) {
        DropTargetListener listener = new DropTargetAdapter() {
            private JPanel panel = null;
            
            @Override
            public void dragEnter(DropTargetDragEvent dtde) {
                if (!dtde.isDataFlavorSupported(info.getDataFlavor()) || !info.canAcceptDrop()) {
                    return;
                }
                final JLayeredPane layeredPane = SwingUtilities.getRootPane(comp).getLayeredPane();
                if (panel == null) {
                    panel = createOverlayPanel(comp, layeredPane, info);
                }
                layeredPane.add(panel, JLayeredPane.MODAL_LAYER);
                Rectangle rect = calculateBounds(info.getComponentToOverlay());
                panel.setBounds(rect);
                panel.doLayout();
                layeredPane.repaint();
                // Repaint again later because the panel might paint itself again if it wraps.
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        layeredPane.repaint();
                    }
                });
            }
            
            private Rectangle calculateBounds(Component overlayComponent) {
                JRootPane rootPane = SwingUtilities.getRootPane(overlayComponent);
                Rectangle rect = SwingUtilities.convertRectangle(overlayComponent.getParent(),
                        overlayComponent.getBounds(),
                        rootPane.getContentPane());
                JMenuBar menuBar = rootPane.getJMenuBar();
                rect.x += MARGIN;
                rect.y += MARGIN + (menuBar == null ? 0 : menuBar.getHeight());
                rect.width -= MARGIN * 2;
                rect.height -= MARGIN * 2;
                return rect;
            }

            @Override
            public void drop(DropTargetDropEvent dtde) {}
        };
        
        addListener(comp, listener);
    }
    
    private static JPanel createOverlayPanel(final JComponent comp, final JLayeredPane layeredPane, final IDropInfo info) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        JLabel label = new JLabel("<html><center>" + info.getOverlayMessage() + "</center></html>");
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        label.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.add(label, BorderLayout.CENTER);
        panel.setBackground(new Color(0, 0, 0, 150));
        addListener(panel, new FileDropListener(info) {
            
            @Override
            public void drop(DropTargetDropEvent dtde) {
                super.drop(dtde);
                restore();
            }
            
            @Override
            public void dragExit(DropTargetEvent dte) {
                restore();
            }

            private void restore() {
                layeredPane.remove(panel);
                layeredPane.repaint();
            }
        });
        // This listener shouldn't be necessary(?), but sometimes dragExit() isn't called
        // properly and the overlay can get stuck even though the drag has ended.
        // This lets you kill the overlay by moving the mouse in and out of it.
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                layeredPane.remove(panel);
                layeredPane.repaint();
            }
        });
        return panel;
    }
    
    private static class FileDropListener extends DropTargetAdapter {
        private final IDropInfo info;
        
        public FileDropListener(IDropInfo info) {
            this.info = info;
        }
        
        @Override
        public void drop(DropTargetDropEvent dtde) {
            dtde.acceptDrop(DnDConstants.ACTION_COPY);
            Transferable transferable = dtde.getTransferable();
            boolean success = false;
            try {
                Object result = transferable.getTransferData(info.getDataFlavor());
                success = info.handleDroppedObject(result);
            } catch (UnsupportedFlavorException e) {
            } catch (IOException e) {
            }
            dtde.dropComplete(success);
        }
    }
    
    private static void addListener(JComponent comp, DropTargetListener listener) {
        DropTarget target = comp.getDropTarget();
        if (target == null) {
            comp.setDropTarget(new DropTarget(comp, listener));
        } else {
            try {
                target.addDropTargetListener(listener);
            } catch (TooManyListenersException e) {
                e.printStackTrace();
            }
        }
    }
    
    public interface IDropInfo {
        public DataFlavor getDataFlavor();
        public boolean canAcceptDrop();
        public Component getComponentToOverlay();
        public String getOverlayMessage();
        public boolean handleDroppedObject(Object dropped);
    }
    
    public static abstract class FileDropInfo implements IDropInfo {
        private final MainWindow mw;
        private final boolean doReset;
        
        public FileDropInfo(MainWindow mw, boolean doReset) {
            this.mw = mw;
            this.doReset = doReset;
        }

        @Override
        public DataFlavor getDataFlavor() {
            return DataFlavor.javaFileListFlavor;
        }
        
        private List<File> filterFiles(List<File> files) {
            List<File> filtered = new ArrayList<File>(files.size());
            for (File file : files) {
                if (file.exists() && file.canRead() && acceptFile(file)) {
                    filtered.add(file);
                }
            }
            return filtered;
        }
        
        @Override
        public boolean handleDroppedObject(Object dropped) {
            return handleFiles((List<File>) dropped);
        };

        protected boolean handleFiles(List<File> files) {
            files = filterFiles(files);
            if (files.isEmpty()) {
                return false;
            }
            mw.importFiles(getImportDestination(), files.toArray(new File[0]), doReset);
            return true;
        }
        
        protected abstract boolean acceptFile(File pathname);
        protected abstract String getImportDestination();
    }
}
